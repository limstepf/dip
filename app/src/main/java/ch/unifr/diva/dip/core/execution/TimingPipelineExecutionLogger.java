package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.core.model.RunnablePipeline;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Timing pipeline execution logger. Gathers timing data of the pipelines and
 * their processors.
 */
public class TimingPipelineExecutionLogger implements PipelineExecutionLogger {

	protected final Map<Integer, PipelineTiming> pipelineTimings;

	/**
	 * Creates a new timing pipeline execution logger.
	 */
	public TimingPipelineExecutionLogger() {
		this.pipelineTimings = new ConcurrentHashMap<>();
	}

	protected PipelineTiming getPipelineTiming(RunnablePipeline pipeline) {
		return pipelineTimings.get(pipeline.page.id);
	}

	protected ProcessorTiming getProcessorTiming(RunnableProcessor processor) {
		final PipelineTiming pipelineTiming = getPipelineTiming(processor.getPipeline());
		return pipelineTiming.getProcessorTimingMap().get(processor.id);
	}

	protected PipelineTiming initTiming(RunnablePipeline pipeline) {
		return pipeline.page.getPipelineTiming();
	}

	@Override
	public Map<Integer, PipelineTiming> getPipelineTimingMap() {
		return pipelineTimings;
	}

	@Override
	public void onStartPipeline(RunnablePipeline pipeline) {
		final PipelineTiming timing = initTiming(pipeline);
		pipelineTimings.put(pipeline.page.id, timing);
		timing.start();
	}

	@Override
	public void onStopPipeline(RunnablePipeline pipeline) {
		getPipelineTiming(pipeline).stop();
	}

	@Override
	public void onStartProcessor(RunnableProcessor processor, int pipelineStage) {
		final ProcessorTiming timing = getProcessorTiming(processor);
		timing.setPipelineStage(pipelineStage);
		timing.start();
	}

	@Override
	public void onStopProcessor(RunnableProcessor processor) {
		final ProcessorTiming timing = getProcessorTiming(processor);
		timing.stop();
	}

}
