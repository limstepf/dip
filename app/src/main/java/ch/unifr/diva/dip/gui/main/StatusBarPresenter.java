package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.core.ApplicationSettings;
import ch.unifr.diva.dip.eventbus.events.StatusMessageEvent;
import ch.unifr.diva.dip.eventbus.events.StatusWorkerEvent;
import ch.unifr.diva.dip.gui.Presenter;
import com.google.common.eventbus.Subscribe;
import java.util.concurrent.LinkedBlockingDeque;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

/**
 * StatusBar.
 *
 * This implementation has a stack for running workers and a StringProperty for
 * a simple status message. Status of running workers takes precedence, as
 * illustrated below:
 *
 * <pre>
 * workers:
 *            |--- A ---|      |----- C -----|
 *                 |-- B --|     |- D - |
 *
 *        |---------------------------------------> t
 *
 * shown:
 *        mmmmAAAAABBBBBBBBBmmmCCDDDDDDDDCCCCCmmm ...
 *
 * </pre>
 */
public class StatusBarPresenter implements Presenter {

	private final BorderPane statusBar = new BorderPane();
	private final Label message = new Label();
	private final StringProperty messageProperty = new SimpleStringProperty();
	private Transition messageTransition;
	private final ProgressBar progress = new ProgressBar();
	private final LinkedBlockingDeque<StatusWorkerEvent> stack = new LinkedBlockingDeque<>();

	/**
	 * Creates a {@code StatusBar}.
	 */
	public StatusBarPresenter() {
		statusBar.getStyleClass().add("dip-status-bar");
		statusBar.setCenter(message);
		statusBar.setRight(progress);
		message.setAlignment(Pos.CENTER_LEFT);
		message.setMaxWidth(Double.MAX_VALUE);
		message.textProperty().bind(messageProperty);
		enableProgress(false);
	}

	@Subscribe
	public void handleStatusEvent(StatusWorkerEvent event) {
		message.textProperty().unbind();
		progress.progressProperty().unbind();

		stack.add(event);
		final ChangeListener listener = new ChangeListener() {
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				final Worker.State state = (Worker.State) newValue;
				if (isDone(state)) {
					event.worker.stateProperty().removeListener(this);
					popStatusWorkerEvent(event);
				}
			}
		};
		event.worker.stateProperty().addListener(listener);

		message.textProperty().bind(event.worker.messageProperty());
		message.setOpacity(1.0);
		progress.progressProperty().bind(event.worker.progressProperty());
		progress.setOpacity(1.0);
		enableProgress(true);
	}

	/**
	 * Starts a new fade (out) transition of the message. Already running
	 * transitions are stopped first.
	 *
	 * @param t already existing transition on the same node (optional, can be
	 * null).
	 * @param node node to be faded out.
	 * @return the new transition.
	 */
	private Transition startFadeTransition(Transition t, Node node) {
		if (t != null) {
			t.stop();
		}
		final PauseTransition p = new PauseTransition(
				Duration.millis(ApplicationSettings.Animations.displayDuration)
		);
		final FadeTransition f = new FadeTransition(
				Duration.millis(ApplicationSettings.Animations.fadeOutDuration),
				node
		);
		f.setFromValue(1.0);
		f.setToValue(0.0);

		t = new SequentialTransition(p, f);
		t.play();
		return t;
	}

	@Subscribe
	public void handleStatusEvent(StatusMessageEvent event) {
		messageProperty.set(event.message);
		messageTransition = startFadeTransition(messageTransition, message);
	}

	/**
	 * Removes a finished worker from the stack, binding to the next worker
	 * still running, or back to the default message.
	 *
	 * @param event the finished worker event.
	 */
	public void popStatusWorkerEvent(StatusWorkerEvent event) {
		stack.remove(event);
		if (!stack.isEmpty()) {
			final StatusWorkerEvent next = stack.peek();

			if (isDone(next.worker.getState())) {
				popStatusWorkerEvent(next);
			} else {
				handleStatusEvent(next);
			}
		} else {
			message.textProperty().bind(messageProperty);
			progress.progressProperty().unbind();
			enableProgress(false);
		}
	}

	/**
	 * Checks whether a worker is done already.
	 *
	 * @param state a worker's state.
	 * @return True if done, False otherwise.
	 */
	private boolean isDone(Worker.State state) {
		if (state == null) {
			return true;
		}
		switch (state) {
			case SUCCEEDED:
			case CANCELLED:
			case FAILED:
				return true;
		}
		return false;
	}

	/**
	 * Visually enables/disables the progress bar.
	 *
	 * @param enable True to enable the progress bar, False to disable it.
	 */
	private void enableProgress(boolean enable) {
		if (enable) {
			final int ms = ApplicationSettings.Animations.fadeInDuration;
			final FadeTransition fade = new FadeTransition(Duration.millis(ms), progress);
			fade.setFromValue(0.4);
			fade.setToValue(1.0);
			fade.play();
		} else {
			progress.setProgress(0);
			final int ms = ApplicationSettings.Animations.fadeOutDuration;
			final FadeTransition fade = new FadeTransition(Duration.millis(ms), progress);
			fade.setFromValue(1.0);
			fade.setToValue(0.4);
			fade.play();
		}
	}

	@Override
	public Parent getComponent() {
		return statusBar;
	}
}
