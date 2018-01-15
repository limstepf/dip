package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PipelineState;
import ch.unifr.diva.dip.core.model.Project;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.model.RunnablePipeline;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Window;

/**
 * The pipeline execution controller. A simple controller that just processes
 * one page/pipeline after the other, with an optional dialog to observe
 * progress and cancel execution.
 *
 * <p>
 * There is no parallelization on this level, since we do not want to keep the
 * resources of so and so many pages/pipelines in memory at the same time.
 */
public class PipelineExecutionController {

	protected final ApplicationHandler handler;
	protected final PipelineExecutionLogger logger;
	protected final List<ProjectPage> pages;
	protected final int numPipelines;
	protected final ObjectProperty<ExecutionState> executionStateProperty;
	protected final IntegerProperty currentPageIndexProperty;
	protected final DoubleProperty progressProperty;
	protected volatile PipelineExecutor executor;
	protected volatile boolean interrupted;
	protected volatile Thread thread;

	/**
	 * Creates a new pipeline execution controller.
	 *
	 * @param handler the application handler.
	 * @param pages the pages to process.
	 */
	public PipelineExecutionController(ApplicationHandler handler, List<ProjectPage> pages) {
		this(handler, handler.getPipelineExecutorLogger(), pages);
	}

	/**
	 * Creates a new pipeline execution controller.
	 *
	 * @param handler the application handler.
	 * @param logger the pipeline execution logger.
	 * @param pages the pages to process.
	 */
	public PipelineExecutionController(ApplicationHandler handler, PipelineExecutionLogger logger, List<ProjectPage> pages) {
		this.handler = handler;
		this.logger = logger;
		this.pages = pages;
		this.numPipelines = this.pages.size();
		this.executionStateProperty = new SimpleObjectProperty<>(ExecutionState.READY);
		this.currentPageIndexProperty = new SimpleIntegerProperty(0);
		this.progressProperty = new SimpleDoubleProperty(0.0);
	}

	/**
	 * Starts the execution of the given pages.
	 */
	public void process() {
		final Project project = handler.getProject();
		if (project == null) {
			return;
		}
		this.thread = Thread.currentThread();
		setState(ExecutionState.RUNNING);

		final int lastIndex = numPipelines - 1;
		for (int i = 0; i < numPipelines; i++) {
			setCurrentPageIndex(i);

			final ProjectPage page = pages.get(i);
			if (project.selectPage(page.id)) {
				FxUtils.run(() -> {
					handler.eventBus.post(new ProjectNotification(
							ProjectNotification.Type.SELECTED,
							page.id
					));
				});
			}

			final RunnablePipeline pipeline = page.getPipeline();

			if (PipelineState.PROCESSING.equals(page.getState())) {
				executor = pipeline.newPipelineExecutor(logger);
				executor.processAndWaitForStop();
			} else {
				executor = null;
			}

			setProgress((i + 1) / (double) numPipelines);

			// don't start another if this executor got cancelled
			if (interrupted || (executor != null && executor.isCancelled())) {
				executor = null;
				setState(ExecutionState.CANCELLED);
				return;
			}
		}
		setCurrentPageIndex(numPipelines);

		logger.onStopExecution();
		executor = null;
		setState(ExecutionState.SUCCEEDED);
	}

	/**
	 * Cancels the execution.
	 */
	public void cancel() {
		interrupted = true;
		if (executor == null || executor.isCancelled()) {
			return;
		}
		executor.cancel();
	}

	/**
	 * The thread the pipeline execution controller is being executed on. The
	 * method {@code process()} has to be called before a thread can be
	 * returned.
	 *
	 * @return the thread the pipeline execution controller is being executed
	 * on, or {@code null} if not started yet.
	 */
	public Thread getThread() {
		return thread;
	}

	protected void setCurrentPageIndex(int value) {
		currentPageIndexProperty.set(value);
	}

	/**
	 * Returns the current page index property.
	 *
	 * @return the current page index property.
	 */
	public ReadOnlyIntegerProperty currentPageIndexProperty() {
		return currentPageIndexProperty;
	}

	/**
	 * Returns the index of the page currently being processed.
	 *
	 * @return the index of the page currently being processed.
	 */
	public int getCurrentPageIndex() {
		return currentPageIndexProperty.get();
	}

	protected void setProgress(double value) {
		FxUtils.run(() -> {
			progressProperty.set(value);
		});
	}

	/**
	 * Returns the progress property.
	 *
	 * @return the progress property.
	 */
	public ReadOnlyDoubleProperty progressProperty() {
		return progressProperty;
	}

	/**
	 * Returns the current progress of the overall execution.
	 *
	 * @return the current progress.
	 */
	public double getProgress() {
		return progressProperty.get();
	}

	protected void setState(ExecutionState state) {
		FxUtils.run(() -> {
			executionStateProperty.set(state);
		});
	}

	/**
	 * Returns the execution state property.
	 *
	 * @return the execution state property.
	 */
	public ReadOnlyObjectProperty<ExecutionState> executionStateProperty() {
		return executionStateProperty;
	}

	/**
	 * Returns the current execution state.
	 *
	 * @return the current execution state.
	 */
	public ExecutionState getExecutionState() {
		return executionStateProperty.get();
	}

	/**
	 * Pipeline execution state.
	 */
	public enum ExecutionState {

		/**
		 * Ready. The execution may be started by a call to {@code process()}.
		 */
		READY,
		/**
		 * Running. The execution has been started.
		 */
		RUNNING,
		/**
		 * Cancelled. The execution has been cancelled.
		 */
		CANCELLED,
		/**
		 * Succeeded. The execution has successfully finished.
		 */
		SUCCEEDED
	}

	/**
	 * Returns a new pipeline execution dialog.
	 *
	 * @param owner the owner of the dialog.
	 * @return a new pipeline execution dialog.
	 */
	public PipelineExecutionDialog getDialog(Window owner) {
		return new PipelineExecutionDialog(owner, this);
	}

	/**
	 * Returns the pipeline execution logger.
	 *
	 * @return the pipeline execution logger.
	 */
	public PipelineExecutionLogger getLogger() {
		return logger;
	}

}
