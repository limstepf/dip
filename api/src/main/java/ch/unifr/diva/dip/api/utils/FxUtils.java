package ch.unifr.diva.dip.api.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
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
	 * @return the started timeline.
	 */
	public static Timeline delay(Duration delay, EventHandler<ActionEvent> afterDelay) {
		final Timeline t = new Timeline(new KeyFrame(
				delay,
				afterDelay
		));

		t.play();
		return t;
	}

	/**
	 * Executes a Runnable on the JavaFX application thread. This method is safe
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
	 * return some result once done. This method is safe to be called from any
	 * thread.
	 *
	 * @param <T> class of the result.
	 * @param callable callable returning T.
	 * @return the result.
	 * @throws Exception
	 */
	public static <T> T runFutureTask(final Callable<T> callable) throws Exception {
		final FutureTask<T> query = new FutureTask<>(callable);
		final T value;

		if (Platform.isFxApplicationThread()) {
			value = callable.call();
		} else {
			Platform.runLater(query);
			value = query.get();
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

	/**
	 * Computes the intersection of two rectangles. Returns a new
	 * {@code Rectangle2D} that represents the intersection of the two
	 * rectangles. If the two rectangles do not intersect, the result will be an
	 * empty rectangle.
	 *
	 * @param r the first rectangle.
	 * @param t the second rectangle.
	 * @return the largest rectangle contained in both given rectangles, or an
	 * empty rectangle if they do not intersect.
	 */
	public static Rectangle2D intersection(Rectangle2D r, Rectangle2D t) {
		double tx1 = t.getMinX();
		double ty1 = t.getMinY();
		double rx1 = r.getMinX();
		double ry1 = r.getMinY();
		double tx2 = t.getMaxX();
		double ty2 = t.getMaxY();
		double rx2 = r.getMaxX();
		double ry2 = r.getMaxY();
		if (tx1 < rx1) {
			tx1 = rx1;
		}
		if (ty1 < ry1) {
			ty1 = ry1;
		}
		if (tx2 > rx2) {
			tx2 = rx2;
		}
		if (ty2 > ry2) {
			ty2 = ry2;
		}
		tx2 -= tx1;
		ty2 -= ty1;

		if (tx2 <= 0 || ty2 <= 0) {
			return new Rectangle2D(tx1, ty2, 0, 0);
		}

		return new Rectangle2D(tx1, ty1, tx2, ty2);
	}

	/**
	 * Determines whether the rectangle is empty. When the {@code Rectangle2D}
	 * is empty, it encloses no area.
	 *
	 * @param r the rectangle.
	 * @return {@code true} if the rectangle is empty, {@code false} otherwise.
	 */
	public static boolean isEmpty(Rectangle2D r) {
		return (r.getWidth() <= 0 || r.getHeight() <= 0);
	}

	/**
	 * Return the RGB hex string of a color.
	 *
	 * @param color the color.
	 * @return the RGB hex string.
	 */
	public static String toHexString(Color color) {
		return String.format(
				"#%02X%02X%02X",
				(int) (color.getRed() * 255),
				(int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255)
		);
	}

}
