package ch.unifr.diva.dip.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * JavaFx utilities.
 */
public class FxUtils {

	private FxUtils() {
		/* nope */
	}

	/**
	 * Initializes the JavaFX toolkit. The JavaFX toolkit needs to be manually
	 * initialized (e.g. to use the {@code Image} class or to run things on the
	 * FXApplicationThread) in case the main application doesn't extend
	 * {@code javafx.application.Application} (e.g. why running without GUI or
	 * executing tests).
	 */
	public static void initToolkit() {
		// This does the job, although this very much looks like a dirty hack...
		final JFXPanel jfx = new JFXPanel();
	}

	/**
	 * Executes an EventHandler after the specified delay on the JavaFX
	 * application thread.
	 *
	 * @param delay the (minimum) duration to wait before the event handler is
	 * executed.
	 * @param afterDelay the event handler to exectue after the delay.
	 */
	public static void delay(Duration delay, EventHandler<ActionEvent> afterDelay) {
		final Timeline t = new Timeline(new KeyFrame(
				delay,
				afterDelay
		));

		t.play();
	}

	/**
	 * Executes a Runnable on the JavaFX application thread. This method is save
	 * to be called from any thread.
	 *
	 * @param runnable the Runnable.
	 */
	public static void run(final Runnable runnable) {
		if (Platform.isFxApplicationThread()) {
			runnable.run();
		} else {
			Platform.runLater(runnable);
		}
	}

	/**
	 * Executes a Callable as a FutureTask on the JavaFX application thread to
	 * return some result once done. This method is save to be called from any
	 * thread.
	 *
	 * @param <T> class of the result.
	 * @param callable callable returning T.
	 * @return the result.
	 * @throws Exception
	 */
	public static <T> T runFutureTask(final Callable<T> callable) throws Exception {
		final FutureTask query = new FutureTask(callable);
		final T value;

		if (Platform.isFxApplicationThread()) {
			value = callable.call();
		} else {
			Platform.runLater(query);
			value = (T) query.get();
		}

		return value;
	}

	/**
	 * Makes sure a region adapts to its parent region by setting minimum
	 * height/width to zero and maximum height/width to infinity.
	 *
	 * @param region region to expand in its parent area.
	 */
	public static void expandInRegion(Region region) {
		region.setMinHeight(0);
		region.setMinWidth(0);
		region.setMaxHeight(Double.MAX_VALUE);
		region.setMaxWidth(Double.MAX_VALUE);
	}

	/**
	 * Fully expands the given child-nodes in its parent AnchorPane.
	 *
	 * @param node child-node to be fully expanded to all sides.
	 */
	public static void expandInAnchorPane(Node node) {
		AnchorPane.setTopAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
		AnchorPane.setBottomAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
	}

	/**
	 * Returns a divider position of a SplitPane by index.
	 *
	 * @param splitPane a SplitPane.
	 * @param index index of the divider (first divider has an index of 0).
	 * @return the divider position, or a negative number in case there is
	 * divider at the given index.
	 */
	public static double dividerPosition(SplitPane splitPane, int index) {
		final double[] dividers = splitPane.getDividerPositions();

		if (index < dividers.length) {
			return dividers[index];
		}

		return -1;
	}

}
