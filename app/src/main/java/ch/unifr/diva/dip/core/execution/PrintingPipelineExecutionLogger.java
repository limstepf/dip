package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.core.model.RunnablePipeline;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import java.io.PrintStream;
import java.util.List;

/**
 * Printing pipeline execution logger. Prints pipeline execution information to
 * the given {@code PrintStream} ({@code System.out} by default).
 */
public class PrintingPipelineExecutionLogger extends TimingPipelineExecutionLogger {

	protected final PrintStream out;

	/**
	 * Creates a new printing pipeline execution logger. Prints to
	 * {@code System.out}.
	 */
	public PrintingPipelineExecutionLogger() {
		this(System.out);
	}

	/**
	 * Creates a new printing pipeline execution logger.
	 *
	 * @param out the print stream to print to.
	 */
	public PrintingPipelineExecutionLogger(PrintStream out) {
		this.out = out;
	}

	@Override
	public void onStartPipeline(RunnablePipeline pipeline) {
		out.println(String.format(
				" - page %d, pipeline %d: start pipeline",
				pipeline.page.id,
				pipeline.id
		));
		super.onStartPipeline(pipeline);
	}

	@Override
	public void onStopPipeline(RunnablePipeline pipeline) {
		super.onStopPipeline(pipeline);
		out.println(String.format(
				" - page %d, pipeline %d: stop pipeline",
				pipeline.page.id,
				pipeline.id
		));
	}

	@Override
	public void onStartProcessor(RunnableProcessor processor, int pipelineStage) {
		out.println(String.format(
				" - page %d, pipeline %d, processor %d: start processor",
				processor.getPage().id,
				processor.getPipeline().id,
				processor.id
		));
		super.onStartProcessor(processor, pipelineStage);
	}

	@Override
	public void onStopProcessor(RunnableProcessor processor) {
		super.onStopProcessor(processor);
		out.println(String.format(
				" - page %d, pipeline %d, processor %d: stop processor",
				processor.getPage().id,
				processor.getPipeline().id,
				processor.id
		));
	}

	@Override
	public void onStopExecution() {
		out.println("done.");
		final List<PipelineTiming> timings = getPipelineTimings();
		out.println();
		out.println("pipeline timings:");
		out.println(PipelineTiming.getCSVHeader());
		for (PipelineTiming pipelineTiming : timings) {
			out.println(pipelineTiming.toCSV());
		}
		out.println();
		out.println("processor timings:");
		out.println(ProcessorTiming.getCSVHeader());
		for (PipelineTiming pipelineTiming : timings) {
			for (ProcessorTiming processorTiming : pipelineTiming.getProcessorTimings()) {
				out.println(processorTiming.toCSV(pipelineTiming));
			}
		}
	}

}
