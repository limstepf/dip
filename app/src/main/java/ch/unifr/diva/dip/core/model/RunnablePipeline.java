package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.core.ApplicationHandler;

/**
 * A RunnablePipeline is a Pipeline "in use".
 */
public class RunnablePipeline extends Pipeline<RunnableProcessor> {

	/**
	 * The project page to be processed by this pipeline.
	 */
	public final ProjectPage page;

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

		// reattach listener we temp. removed (see above)
		this.modifiedPipelineProperty.addObservedProperty(this.processors);
	}

	/**
	 *
	 */
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
	}

}
