package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.core.ApplicationHandler;
import java.util.Map;

/**
 * A prototype pipeline to be used in the pipeline editro (can not be run).
 */
public class PrototypePipeline extends Pipeline<PrototypeProcessor> {

	/**
	 * Creates an empty prototype pipeline.
	 *
	 * @param handler the application handler.
	 * @param id id of the pipeline. Needs to be unique among all pipelines in
	 * the project, and is usually assigned by the PipelineManager.
	 * @param name name of the pipeline.
	 */
	public PrototypePipeline(ApplicationHandler handler, int id, String name) {
		super(handler, id, name);
	}

	/**
	 * Creates/restores a prototype pipeline according to the given pipeline
	 * data.
	 *
	 * @param handler the application handler.
	 * @param pipeline the pipeline data.
	 */
	public PrototypePipeline(ApplicationHandler handler, PipelineData.Pipeline pipeline) {
		super(handler, pipeline);
	}

	/**
	 * Creates/restores a prototype pipeline according to the given pipeline
	 * data.
	 *
	 * @param handler the application handler.
	 * @param pipeline the pipeline data.
	 * @param id a new pipeline id, or {@code -1} to keep using the one defined
	 * in the pipeline data.
	 */
	public PrototypePipeline(ApplicationHandler handler, PipelineData.Pipeline pipeline, int id) {
		super(handler, pipeline, id);
	}

	@Override
	public PrototypeProcessor addProcessor(String pid, String version, double x, double y, Map<String, Object> parameters, boolean editing) {
		final PrototypeProcessor wrapper = new PrototypeProcessor(newProcessorId(), pid, version, x, y, handler);
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
	public PrototypePipeline clonePipeline() {
		final PipelineData.Pipeline data = new PipelineData.Pipeline(this);
		return new PrototypePipeline(handler, data);
	}

}
