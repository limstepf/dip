package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.utils.FileFinder;
import ch.unifr.diva.dip.utils.UniqueHashSet;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.xml.bind.JAXBException;

/**
 * A RunnablePipeline is a Pipeline "in use".
 */
public class RunnablePipeline extends Pipeline<RunnableProcessor> {

	/**
	 * The project page to be processed by this pipeline.
	 */
	public final ProjectPage page;

	private final ObjectProperty<Pipeline.State> stateProperty;
	private final List<Set<RunnableProcessor>> processorStateSets;
	// set of processors in an error state (error, unavailable, unconnected)
	private final Set<RunnableProcessor> errorProcessors;
	// set of processors in processing state, but can't be processed automatically
	// i.e. manual editing is needed (not to be confused with the waiting state of a processor)
	private final Set<RunnableProcessor> waitingProcessors;
	// set of processors that can be (auto) processed next.
	private final Set<RunnableProcessor> processingProcessors;

	// so far added and removed processors set the dirty bit to true, but that's
	// not really enough: we'd also need to update the stages if connections change!
	// On the bright side: this shouldn't ever happen with runnable pipelines, so
	// we're good for now, since that's the only place where we use the stages,
	// but technically we should listen to all ports too.
	protected boolean dirtyStages = true;
	protected PipelineStages<RunnableProcessor> stages;

	/**
	 * Creates a new runnable pipeline from pipeline data.
	 *
	 * @param handler the application handler.
	 * @param page the project page.
	 * @param pipeline the pipeline data/specification.
	 */
	public RunnablePipeline(ApplicationHandler handler, ProjectPage page, PipelineData.Pipeline pipeline) {
		super(handler, pipeline.id, pipeline.name);
		this.page = page;

		this.stateProperty = new SimpleObjectProperty<>(Pipeline.State.WAITING);
		this.errorProcessors = new UniqueHashSet<>();
		this.waitingProcessors = new UniqueHashSet<>();
		this.processingProcessors = new UniqueHashSet<>();
		this.processorStateSets = new ArrayList<>(Arrays.asList(
				errorProcessors,
				waitingProcessors,
				processingProcessors
		));

		// don't count the following initialization as modifications
		this.modifiedPipelineProperty.removeObservedProperty(this.processors);

		for (PipelineData.Processor p : pipeline.processors.list) {
			final RunnableProcessor run = new RunnableProcessor(p, this);
			run.init();
			addProcessor(run);
			levelMaxProcessorId(p.id);
		}

		initConnections(pipeline, processors);

		// init and update processor states now that they're connected
		for (Stage<RunnableProcessor> stage : stages()) {
			for (RunnableProcessor p : stage.processors) {
				p.initProcessor();
				p.updateState();
			}
		}

		// reattach listener we temp. removed (see above)
		this.modifiedPipelineProperty.addObservedProperty(this.processors);
	}

	/**
	 * Returns the state property of the pipeline.
	 *
	 * @return the state property of the pipeline.
	 */
	public ReadOnlyObjectProperty<Pipeline.State> stateProperty() {
		return this.stateProperty;
	}

	/**
	 * Returns the state of the pipeline.
	 *
	 * @return the state of the pipeline.
	 */
	public Pipeline.State getState() {
		return stateProperty().get();
	}

	/**
	 * Updates the state of the pipeline, given the passed processor changed its
	 * state.
	 *
	 * @param p the processor that just changed its state.
	 */
	private void updateState(RunnableProcessor p) {
		registerProcessorState(p);
		updateState();
	}

	/**
	 * (Re-)evaluates the state of the pipeline.
	 */
	private void updateState() {
		if (!errorProcessors.isEmpty()) {
			stateProperty.set(State.ERROR);
		} else if (!waitingProcessors.isEmpty() && processingProcessors.isEmpty()) {
			stateProperty.set(State.WAITING);
		} else if (!processingProcessors.isEmpty()) {
			stateProperty.set(State.PROCESSING);
		} else {
			stateProperty.set(State.READY);
		}
	}

	private void registerProcessorState(RunnableProcessor p) {
		switch (p.getStateValue()) {
			case WAITING:
				registerProcessorState(p, null);
				break;
			case PROCESSING:
				if (p.processor().canProcess()) {
					registerProcessorState(p, processingProcessors);
				} else {
					registerProcessorState(p, waitingProcessors);
				}
				break;
			case READY:
				registerProcessorState(p, null);
				break;
			default:
			case ERROR:
			case UNAVAILABLE:
			case UNCONNECTED:
				registerProcessorState(p, errorProcessors);
				break;
		}
	}

	private void registerProcessorState(RunnableProcessor p, Set<RunnableProcessor> dstSet) {
		// note that these are UniqueHashSets, not regular HashSets, so that equals
		// doesn't care about the contents of the sets.
		for (Set<RunnableProcessor> stateSet : this.processorStateSets) {
			if (dstSet != null && stateSet.equals(dstSet)) {
				stateSet.add(p);
			} else {
				stateSet.remove(p);
			}
		}
	}

	private final Map<RunnableProcessor, InvalidationListener> processorStateListeners = new HashMap<>();

	@Override
	protected void registerProcessor(RunnableProcessor wrapper) {
		super.registerProcessor(wrapper);
		this.dirtyStages = true;
		final InvalidationListener listener = (e) -> {
			updateState(wrapper);
		};
		processorStateListeners.put(wrapper, listener);
		wrapper.stateProperty().addListener(listener);
		updateState(wrapper);
	}

	@Override
	protected void unregisterProcessor(RunnableProcessor wrapper) {
		super.unregisterProcessor(wrapper);
		this.dirtyStages = true;
		final InvalidationListener listener = processorStateListeners.get(wrapper);
		if (listener != null) {
			wrapper.stateProperty().removeListener(listener);
		}
		registerProcessorState(wrapper, null);
		updateState();
	}

	@Override
	public RunnableProcessor addProcessor(String pid, String version, double x, double y, Map<String, Object> parameters, boolean editing) {
		final RunnableProcessor wrapper = new RunnableProcessor(
				new PipelineData.Processor(
						newProcessorId(),
						pid,
						version
				),
				this
		);
		wrapper.editingProperty().set(editing);
		wrapper.init();
		if (parameters != null) {
			wrapper.setParameters(parameters);
		}
		wrapper.initProcessor();
		addProcessor(wrapper);
		return wrapper;
	}

	@Override
	public RunnablePipeline clonePipeline() {
		final PipelineData.Pipeline data = new PipelineData.Pipeline(this);
		return new RunnablePipeline(handler, page, data);
	}

	/**
	 * Returns the stages of the pipeline.
	 *
	 * @return the stages of the pipeline.
	 */
	public final PipelineStages<RunnableProcessor> stages() {
		if (stages == null || dirtyStages) {
			this.stages = new Pipeline.PipelineStages<>(this, outputPortMap());
		}
		return this.stages;
	}

	/**
	 * Saves the state of the pipeline including all its
	 * {@code RunnableProcessor}s.
	 */
	public void save() {
		for (RunnableProcessor p : processors) {
			p.save();
		}
		savePipelinePatch();
		FxUtils.run(() -> modifiedProperty().set(false));
	}

	/**
	 * Switches/updates the context on all processors in the pipeline.
	 */
	public void contextSwitch() {
		for (RunnableProcessor p : processors()) {
			p.switchContext(false);
		}
	}

	private boolean savePipelinePatch() {
		this.deletePipelinePatch();

		final PrototypePipeline prototype = this.page.project().pipelineManager().getPipeline(id);
		final PipelinePatch patch = PipelinePatch.createPatch(prototype, this);

		if (!patch.isEmpty()) {
			try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(this.page.pipelinePatchXml()))) {
				patch.save(stream);
			} catch (JAXBException | IOException ex) {
				log.error("failed to save pipeline patch: {}", this, ex);
				FxUtils.run(() -> handler.uiStrategy.showError(ex));
				return false;
			}
		}

		return true;
	}

	/**
	 * Deletes the pipeline patch.
	 *
	 * @return {@code true} if the pipeline patch got delete, {@code false}
	 * otherwise (e.g. if it did not exist in the first place...).
	 */
	private boolean deletePipelinePatch() {
		try {
			return Files.deleteIfExists(this.page.pipelinePatchXml());
		} catch (IOException ex) {
			log.error("failed to clear the pipeline patch: {}", this, ex);
			FxUtils.run(() -> handler.uiStrategy.showError(ex));
		}
		return false;
	}

	/**
	 * Processes the pipeline. Keeps processing all processable processors in
	 * the pipeline until all processors are {@code READY} or can't be
	 * automatically processed.
	 *
	 * Probably should be run on some background/worker thread, or something...
	 */
	public synchronized void process() {
		final List<RunnableProcessor> processing = getProcessing();
		for (RunnableProcessor p : processing) {
			p.process();
			processDependentProcessors(p);
		}
	}

	/**
	 * Process dependent processors. Dependent processors are those immediately
	 * following the given processor in the pipeline. The given processor has
	 * been processed now, so we check if these dependent processor can be
	 * processed now too, and do so, if that's the case. We repeat this
	 * recursively for all processed processors.
	 *
	 * @param p the processor that just has been processed.
	 */
	private void processDependentProcessors(RunnableProcessor p) {
		p.applyToDependentProcessors((q) -> {
			if (isProcessing(q)) {
				q.process();
				processDependentProcessors(q);
			}
			return null;
		});
	}

	// can we process this now?
	private boolean isProcessing(RunnableProcessor p) {
		return Processor.State.PROCESSING.equals(p.getStateValue()) && p.processor().canProcess();
	}

	// inital set of processor that can be processed
	private List<RunnableProcessor> getProcessing() {
		final ArrayList<RunnableProcessor> processing = new ArrayList<>();
		for (RunnableProcessor p : processors()) {
			if (isProcessing(p)) {
				processing.add(p);
			}
		}
		return processing;
	}

	/**
	 * Resets the (runnable) pipeline. Probably should be run on some
	 * background/worker thread, or something...
	 *
	 * @param unpatch if {@code true} resets all parameters/pipeline patches,
	 * otherwise the persistent processor data only is deleted.
	 */
	public synchronized void reset(boolean unpatch) {
		for (RunnableProcessor p : processors()) {
			if (p.processor().canReset()) {
				p.reset();
				FxUtils.run(() -> p.updateState(true));
			}
		}

		try {
			if (Files.exists(this.page.processorRootDirectory())) {
				FileFinder.deleteDirectory(this.page.processorRootDirectory());
			}
		} catch (IOException ex) {
			log.error("failed to clear the project page's processor data: {}", this, ex);
			handler.uiStrategy.showError(ex);
		}

		if (unpatch) {
			deletePipelinePatch();
		}
	}

}
