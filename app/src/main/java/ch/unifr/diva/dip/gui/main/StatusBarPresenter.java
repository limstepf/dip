package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
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
 * a simple status message. Status of running workers takes precedence (and
 * newer workers take precedence over older ones), as illustrated below:
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
	private final ProgressBar progress = new ProgressBar();
	private final LinkedBlockingDeque<StatusWorkerEvent<?>> stack = new LinkedBlockingDeque<>();
	private Transition messageTransition;

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
	public void handleStatusEvent(StatusMessageEvent event) {
		/*
		 * Just overwrite the last message and (re-)start the fade out transition.
		 * The message will be visible as long as no StatusWorkerEvent is active.
		 */
		message.textProperty().bind(messageProperty);
		messageProperty.set(event.message);
		messageTransition = startFadeTransition(messageTransition, message);
	}

	@Subscribe
	public void handleStatusEvent(StatusWorkerEvent<?> event) {
		/*
		 * Latest worker is shown, once done we check for older workers and
		 * bind to them if still running, or, finally, bind back to the regular
		 * status message.
		 */
		stack.add(event);

		final ChangeListener<Worker.State> listener = new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
				if (isDone(newValue)) {
					event.worker.stateProperty().removeListener(this);
					removeStatusWorkerEvent(event);
				}
			}
		};
		// bind before we start listening to the worker's state
		bindToStatusWorkerEvent(event);
		event.worker.stateProperty().addListener(listener);
		// small jobs may as well be already done before the listener could be
		// attached; make sure the listener fires at least once to ensure the
		// event will be properly cleaned up
		listener.changed(
				event.worker.stateProperty(),
				event.worker.stateProperty().get(),
				event.worker.stateProperty().get()
		);
	}

	private void removeStatusWorkerEvent(StatusWorkerEvent<?> event) {
		stack.remove(event);

		if (stack.isEmpty()) {
			unbindStatusWorkerEvent(); // done, show regular msg again
		} else {
			final StatusWorkerEvent<?> next = stack.peek();
			if (isDone(next)) {
				removeStatusWorkerEvent(next); // pop, try next
			} else {
				bindToStatusWorkerEvent(next); // bind to next worker
			}
		}
	}

	private void unbindStatusWorkerEvent() {
		message.textProperty().unbind();
		messageTransition = startFadeTransition(messageTransition, message);
		progress.progressProperty().unbind();
		enableProgress(false);
	}

	private void bindToStatusWorkerEvent(StatusWorkerEvent<?> event) {
		if (messageTransition != null) {
			messageTransition.stop();
		}
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
				Duration.millis(UIStrategyGUI.Animation.displayDuration)
		);
		final FadeTransition f = new FadeTransition(
				Duration.millis(UIStrategyGUI.Animation.fadeOutDuration),
				node
		);
		f.setFromValue(1.0);
		f.setToValue(0.0);

		t = new SequentialTransition(p, f);
		t.play();
		return t;
	}

	/**
	 * Checks whether the worker of a worker event is done already.
	 *
	 * @param event the worker event.
	 * @return {@code true} if done, {@code false} otherwise.
	 */
	private boolean isDone(StatusWorkerEvent<?> event) {
		return isDone(event.worker.getState());
	}

	/**
	 * Checks whether a worker is done already.
	 *
	 * @param state a worker's state.
	 * @return {@code true} if done, {@code false} otherwise.
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
	 * @param enable {@code true} to enable the progress bar, {@code false} to
	 * disable it.
	 */
	private void enableProgress(boolean enable) {
		if (enable) {
			final int ms = UIStrategyGUI.Animation.fadeInDuration;
			final FadeTransition fade = new FadeTransition(Duration.millis(ms), progress);
			fade.setFromValue(0.4);
			fade.setToValue(1.0);
			fade.play();
		} else {
			progress.setProgress(0);
			final int ms = UIStrategyGUI.Animation.fadeOutDuration;
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
