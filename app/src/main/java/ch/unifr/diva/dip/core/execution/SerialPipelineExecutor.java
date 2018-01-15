package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.core.model.RunnablePipeline;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import java.util.List;

/**
 * Serial pipeline executor. Executes one processor after the other. This
 * executor doesn't offer any pipeline parallelism, but multiple cores may still
 * be used by the processors themselves.
 */
public class SerialPipelineExecutor extends PipelineExecutor {

	protected volatile boolean interrupted;

	/**
	 * Creates a new serial pipeline executor.
	 *
	 * @param runnable the runnable pipeline.
	 */
	public SerialPipelineExecutor(RunnablePipeline runnable) {
		this(runnable, new NullPipelineExecutionLogger());
	}

	/**
	 * Creates a new serial pipeline executor.
	 *
	 * @param runnable the runnable pipeline.
	 * @param logger the pipeline execution logger.
	 */
	public SerialPipelineExecutor(RunnablePipeline runnable, PipelineExecutionLogger logger) {
		super(runnable, logger);
	}

	@Override
	public void cancel() {
		interrupted = true;
		executionThread.interrupt();
	}

	@Override
	public boolean isCancelled() {
		return interrupted;
	}

	@Override
	protected void doProcess() {
		final List<RunnableProcessor> processing = getProcessing();
		for (RunnableProcessor p : processing) {
			if (interrupted) {
				break;
			}
			processProcessor(p, 1);
			processDependentProcessors(p, 2);
		}
	}

	protected void processProcessor(RunnableProcessor processor, int pipelineStage) {
		if (interrupted) {
			return;
		}
		logger.onStartProcessor(processor, pipelineStage);
		processor.process();
		logger.onStopProcessor(processor);
	}

	/**
	 * Process dependent processors. Dependent processors are those immediately
	 * following the given processor in the pipeline. The given processor has
	 * been processed now, so we check if these dependent processor can be
	 * processed now too, and do so, if that's the case. We repeat this
	 * recursively for all processed processors.
	 *
	 * @param processor the processor that just has been processed.
	 * @param pipelineStage
	 */
	protected void processDependentProcessors(RunnableProcessor processor, int pipelineStage) {
		final int nextStage = pipelineStage + 1;
		processor.applyToDependentProcessors((q) -> {
			if (!interrupted && isProcessing(q)) {
				processProcessor(q, pipelineStage);
				processDependentProcessors(q, nextStage);
			}
			return null;
		});
	}

}
