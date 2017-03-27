package ch.unifr.diva.dip.api.ui;

import java.util.Arrays;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

/**
 * A shape with an animated dashed outline (marching ants).
 *
 * @param <T> class of the shape.
 */
public class AnimatedDashedShape<T extends Shape> {

	protected final T shape;
	protected Timeline timeline;
	protected final List<Double> pattern;
	protected final double strokeWidth;
	protected final double duration; // milli
	protected final Pane snapPane;

	/**
	 * Creates a new animated, dashed shape.
	 *
	 * @param shape the shape.
	 */
	public AnimatedDashedShape(T shape) {
		this(shape, 1.0, Arrays.asList(5d, 5d), 450);
	}

	/**
	 * Creates a new animated, dashed shape.
	 *
	 * @param shape the shape.
	 * @param strokeWidth the stroke width.
	 * @param pattern the dashed pattern.
	 * @param duration the animation duration.
	 */
	public AnimatedDashedShape(T shape, double strokeWidth, List<Double> pattern, double duration) {
		this.shape = shape;
		this.strokeWidth = strokeWidth;
		this.pattern = pattern;
		this.duration = duration;
		this.snapPane = new Pane(shape);
		setup(1.0);
	}

	/**
	 * Returns the animated shape.
	 *
	 * @return the animated shape.
	 */
	public T getShape() {
		return shape;
	}

	/**
	 * Returns the animated shape, snapped to the next pixel.
	 *
	 * @return the animated shape, snapped to the next pixel.
	 */
	public Node getSnappedShape() {
		// no need to adjust some offset s.t. our strokes hit the pixel center
		// when the zoom changes... we get that for free by wrapping the shape
		// in a pane (snapToPixel).
		return snapPane;
	}

	protected final void setup(double inv) {
		shape.setStrokeWidth(strokeWidth * inv);
		shape.getStrokeDashArray().clear();

		double sum = 0;
		for (Double d : pattern) {
			final double v = d * inv;
			sum += v;
			shape.getStrokeDashArray().add(v);
		}

		timeline = new Timeline(
				new KeyFrame(
						Duration.ZERO,
						new KeyValue(
								shape.strokeDashOffsetProperty(),
								0,
								Interpolator.LINEAR
						)
				),
				new KeyFrame(
						Duration.millis(duration),
						new KeyValue(
								shape.strokeDashOffsetProperty(),
								sum,
								Interpolator.LINEAR
						)
				)
		);
		timeline.setCycleCount(Timeline.INDEFINITE);
	}

	/**
	 * Sets the zoom to adjust the stroke width.
	 *
	 * @param zoom the zoom.
	 */
	public void setZoom(double zoom) {
		final double inv = 1.0 / zoom;
		final boolean running;
		if (timeline.getStatus().equals(Animation.Status.RUNNING)) {
			running = true;
			timeline.stop();
		} else {
			running = false;
		}
		setup(inv);
		if (running) {
			timeline.play();
		}
	}

	/**
	 * Starts/plays the animation.
	 */
	public void play() {
		shape.setVisible(true);
		timeline.play();
	}

	/**
	 * Stops the animation.
	 */
	public void stop() {
		shape.setVisible(false);
		timeline.stop();
	}

}
