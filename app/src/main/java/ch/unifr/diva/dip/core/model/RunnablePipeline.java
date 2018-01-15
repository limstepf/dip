package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.core.execution.PipelineExecutor;
import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.execution.PipelineExecutionLogger;
import ch.unifr.diva.dip.gui.pe.PipelineLayoutStrategy;
import ch.unifr.diva.dip.osgi.OSGiVersionPolicy;
import ch.unifr.diva.dip.utils.FileFinder;
import ch.unifr.diva.dip.utils.IOUtils;
import ch.unifr.diva.dip.utils.UniqueHashSet;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javax.xml.bind.JAXBException;

/**
 * A RunnablePipeline is a Pipeline "in use".
 */
public class RunnablePipeline extends Pipeline<RunnableProcessor> {

	/**
	 * The project page to be processed by this pipeline.
	 */
	public final ProjectPage page;

	private final List<Set<RunnableProcessor>> processorStateSets;
	// set of processors in an error state (error, unavailable, unconnected)
	private final Set<RunnableProcessor> errorProcessors;
	// set of processors in processing state, but can't be processed automatically
	// i.e. manual editing is needed (not to be confused with the waiting state of a processor)
	private final Set<RunnableProcessor> waitingProcessors;
	// set of processors that can be (auto) processed next.
	private final Set<RunnableProcessor> processingProcessors;
	// don't update pipeline state during initialization (or this goes from ERROR
	// to WARNING and to PROCESSING/READY eventually...)
	private boolean doUpdateState;

	// so far added and removed processors set the dirty bit to true, but that's
	// not really enough: we'd also need to update the stages if connections change!
	// On the bright side: this shouldn't ever happen with runnable pipelines, so
	// we're good for now, since that's the only place where we use the stages,
	// but technically we should listen to all ports too.
	protected boolean dirtyStages = true;
	protected PipelineStages<RunnableProcessor> stages;

	// two locks are needed, since resources need to be accessed by processors
	// while the pipelineLock is being held.
	private final Object pipelineLock = new Object();
	private final Object resourceLock = new Object();

	/**
	 * Creates a new runnable pipeline from pipeline data.
	 *
	 * @param handler the application handler.
	 * @param page the project page.
	 * @param pipeline the pipeline data/specification.
	 */
	public RunnablePipeline(ApplicationHandler handler, ProjectPage page, PipelineData.Pipeline pipeline) {
		super(
				handler,
				pipeline.id,
				pipeline.name,
				PipelineExecutor.Type.get(pipeline.pipelineExecutor),
				PipelineLayoutStrategy.get(pipeline.layoutStrategy),
				OSGiVersionPolicy.get(pipeline.versionPolicy)
		);
		this.page = page;

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

		doUpdateState = true;
		updateState();
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
		// do not update the pipeline state if we're about to release this pipeline
		if (releaseLock || !doUpdateState) {
			return;
		}

		page.setState(getPipelineState());
	}

	/**
	 * Returns the (execution) state of the pipeline.
	 *
	 * @return the (execution) state of the pipeline.
	 */
	protected PipelineState getPipelineState() {
		if (!errorProcessors.isEmpty()) {
			return PipelineState.ERROR;
		}
		if (!waitingProcessors.isEmpty() && processingProcessors.isEmpty()) {
			return PipelineState.WAITING;
		}
		if (!processingProcessors.isEmpty()) {
			return PipelineState.PROCESSING;
		}
		return PipelineState.READY;
	}

	private void registerProcessorState(RunnableProcessor p) {
		switch (p.getState()) {
			case WAITING:
				registerProcessorState(p, null);
				break;
			case PROCESSING:
				if (p.serviceObject().canProcess()) {
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

	/**
	 * Release lock. Prevents updating the pipeline state while releasing the
	 * pipeline. May be also checked by {@code RunnableProcessor}s.
	 */
	protected boolean releaseLock = false;

	@Override
	protected void release() {
		releaseLock = true;
		for (RunnableProcessor p : this.processors) {
			p.release();
		}
		super.release();
		this.stages = null;
		this.errorProcessors.clear();
		this.waitingProcessors.clear();
		this.processingProcessors.clear();
		this.processorStateListeners.clear();
		releaseLock = false;
	}

	private final Map<Integer, InvalidationListener> processorStateListeners = new HashMap<>();

	@Override
	protected void registerProcessor(RunnableProcessor wrapper) {
		super.registerProcessor(wrapper);
		this.dirtyStages = true;
		final WeakReference<RunnableProcessor> weakWrapper = new WeakReference<>(wrapper);
		final int weakId = wrapper.id;
		final InvalidationListener listener = (e) -> {
			final RunnableProcessor p = weakWrapper.get();
			if (p == null) {
				processorStateListeners.remove(weakId);
				return;
			}
			updateState(p);
		};
		processorStateListeners.put(wrapper.id, listener);
		wrapper.stateProperty().addListener(listener);
		updateState(wrapper);
	}

	@Override
	protected void unregisterProcessor(RunnableProcessor wrapper) {
		super.unregisterProcessor(wrapper);
		this.dirtyStages = true;
		final InvalidationListener listener = processorStateListeners.get(wrapper.id);
		if (listener != null) {
			wrapper.stateProperty().removeListener(listener);
		}
		processorStateListeners.remove(wrapper.id);
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
		synchronized (pipelineLock) {
			for (RunnableProcessor p : processors) {
				p.save();
			}
			savePipelinePatch();
			FxUtils.run(() -> modifiedProperty().set(false));
		}
	}

	/**
	 * Switches/updates the context on all processors in the pipeline.
	 */
	public void contextSwitch() {
		synchronized (pipelineLock) {
			for (RunnableProcessor p : processors()) {
				p.switchContext(false);
			}
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
	 * Processes the pipeline.
	 */
	public void process() {
		process(handler.getPipelineExecutorLogger());
	}

	/**
	 * Processes the pipeline.
	 *
	 * @param logger the pipeline execution logger.
	 */
	public void process(PipelineExecutionLogger logger) {
		synchronized (pipelineLock) {
			final PipelineExecutor executor = newPipelineExecutor(logger);
			executor.processAndWaitForStop();
		}
	}

	/**
	 * Creates a new pipeline executor.
	 *
	 * @param logger the pipeline execution logger.
	 * @return the pipeline executor.
	 */
	public PipelineExecutor newPipelineExecutor(PipelineExecutionLogger logger) {
		return getPipelineExecutor().newInstance(this, logger);
	}

	/**
	 * Resets the (runnable) pipeline. Probably should be run on some
	 * background/worker thread, or something...
	 *
	 * @param unpatch if {@code true} resets all parameters/pipeline patches,
	 * otherwise the persistent processor data only is deleted.
	 */
	public void reset(boolean unpatch) {
		synchronized (pipelineLock) {
			for (RunnableProcessor p : processors()) {
				if (p.serviceObject().canReset()) {
					p.reset();
					FxUtils.runAndWait(() -> p.updateState(true));
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

	/**
	 * Returns the path to a processor's dedicated data directory. The directory
	 * will be created if it doesn't exist yet.
	 *
	 * @param processorDataDirectory the processor's data directory.
	 * @return path to the processor's data directory.
	 * @throws IOException
	 */
	protected Path getProcessorDirectory(String processorDataDirectory) throws IOException {
		final Path path = page.project().zipFileSystem().getPath(processorDataDirectory);
		synchronized (resourceLock) {
			final Path directory = IOUtils.getRealDirectories(path);
			return directory;
		}
	}

}
