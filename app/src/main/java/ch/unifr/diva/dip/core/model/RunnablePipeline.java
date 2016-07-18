package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.core.ApplicationHandler;
import java.util.Map;
import javafx.collections.ListChangeListener;

/**
 * A RunnablePipeline is a Pipeline "in use".
 */
public class RunnablePipeline extends Pipeline<RunnableProcessor> {

	public final ProjectPage page;
	private volatile PipelineStages<RunnableProcessor> stages;
	private final ListChangeListener<? super RunnableProcessor> stageListener;

	private volatile Map<OutputPort, ProcessorWrapper.PortMapEntry> outputPortMap;
	private volatile Map<InputPort, ProcessorWrapper.PortMapEntry> inputPortMap;

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

		// rebuild portmaps and stages in case the processors should have changed
		this.stageListener = (ListChangeListener.Change<? extends RunnableProcessor> c) -> {
			updatePortMapsAndStages();
		};
		this.processors().addListener(stageListener);

		this.modifiedPipelineProperty.addObservedProperty(this.processors);
	}

	// needed if processors should have changed, or underlying processor OSGi services
	// got swapped out, since that returns new port objects too!
	private void updatePortMapsAndStages() {
		if (this.outputPortMap != null) {
			this.outputPortMap = null; // reset
			this.outputPortMap = this.outputPortMap();
		}
		if (this.inputPortMap != null) {
			this.inputPortMap = null; // reset
			this.inputPortMap = this.inputPortMap();
		}
		if (this.stages != null) {
			this.stages = null; // reset
			this.stages = this.stages();
		}
	}

	/**
	 * Returns the input port map.
	 *
	 * @return the input port map.
	 */
	public Map<InputPort, ProcessorWrapper.PortMapEntry> inputPortMap() {
		if (this.inputPortMap == null) {
			this.inputPortMap = ProcessorWrapper.getInputPortMap(this.processors());
		}
		return this.inputPortMap;
	}

	/**
	 * Returns the output port map.
	 *
	 * @return the output port map.
	 */
	public Map<OutputPort, ProcessorWrapper.PortMapEntry> outputPortMap() {
		if (this.outputPortMap == null) {
			this.outputPortMap = ProcessorWrapper.getOutputPortMap(this.processors());
		}
		return this.outputPortMap;
	}

	/**
	 * Returns the stages of the pipeline.
	 *
	 * @return the stages of the pipeline.
	 */
	public final PipelineStages<RunnableProcessor> stages() {
		if (this.stages == null) {
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
	}

}
