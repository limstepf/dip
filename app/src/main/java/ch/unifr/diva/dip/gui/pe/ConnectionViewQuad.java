package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.utils.FxUtils;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * A wire modelled with a quadratic parameteric curve segment.
 */
public class ConnectionViewQuad extends ConnectionViewBase<QuadCurve> {

	public ConnectionViewQuad(InputPort<?> input) {
		super(input, newWire());

		this.wire.startXProperty().addListener((e) -> updateControlPoint());
		this.wire.startYProperty().addListener((e) -> updateControlPoint());
		this.wire.endXProperty().addListener((e) -> updateControlPoint());
		this.wire.endYProperty().addListener((e) -> updateControlPoint());

		// this one listener would be all that is needed, but then the bouncy thing
		// doesn't work anymore, since a bouncy wire changes the layoutBounds, duh...
//		this.wire.layoutBoundsProperty().addListener((e) -> updateControlPoint());
	}

	private static QuadCurve newWire() {
		final QuadCurve w = new QuadCurve();
		w.getStyleClass().add("dip-wire");
		w.setStrokeLineCap(StrokeLineCap.ROUND);
		w.setStrokeWidth(PipelineEditor.WIRE_RADIUS * PipelineEditor.PORT_RATIO);
		return w;
	}

	private void updateControlPoint() {
		final double minX = Math.min(wire.startXProperty().get(), wire.endXProperty().get());
		final double maxX = Math.max(wire.startXProperty().get(), wire.endXProperty().get());
		final double dx = maxX - minX;

		final double minY = Math.min(wire.startYProperty().get(), wire.endYProperty().get());
		final double maxY = Math.max(wire.startYProperty().get(), wire.endYProperty().get());
		final double dy = maxY - minY;

		// f <= 1.0
		final double f = (dx < dy) ? dx / dy : dy / dx;

		wire.setControlX(minX + dx * .5);
		wire.setControlY(maxY + Math.log(1 + f) * 128);
	}

	private Transition transition;

	private void bounce() {
		if (this.transition != null) {
			return;
		}

		updateControlPoint();

		final int cycles = (Math.random() < .5) ? 1 : 2; // bounce twice as long, sometimes
		final double v = (Math.random() * 2 + 1) * 37; // quicker vertical (more cycles)
		final double h = (Math.random() * 2 + 1) * 107; // slower horizontal (less cycles)
		final double vd = Math.random() * 5 + 1; // shorter vertical distance/bounce
		final double hd = Math.random() * 15 + 9; // longer vertical distance/bounce
		final Timeline tX = newBounceTimeline(wire.controlXProperty(), v, vd, 6 * cycles);
		final Timeline tY = newBounceTimeline(wire.controlYProperty(), h, hd, 2 * cycles);
		final ParallelTransition t = new ParallelTransition(tX, tY);
		this.transition = t;
		this.transition.setOnFinished((e) -> {
			this.transition = null;
		});

		t.play();
	}

	private void playBounce() {
		// the control point is wrong/displaced if we don't have a slight delay
		// here (in/after bind()), so...
		FxUtils.delay(Duration.millis(52), (e) -> {
			bounce();
		});
	}

	private Timeline newBounceTimeline(DoubleProperty property, double duration, double distance, int cycle) {
		final Timeline t = new Timeline();
		final double rt = Math.random() + 1;
		final double rx = (Math.random() + .5) * ((Math.random() < .5) ? 1 : -1);
		t.setCycleCount(cycle);
		t.setAutoReverse(true);
		t.getKeyFrames().add(new KeyFrame(
				Duration.millis(duration * rt),
				new KeyValue(
						property,
						property.get() + distance * rx,
						Interpolator.EASE_BOTH
				)
		));
		return t;
	}

	@Override
	public void bind(PortView<OutputPort<?>> output, PortView<InputPort<?>> input) {
		super.bind(output, input);

		playBounce();
	}

	@Override
	public void setEnd(Point2D p) {
		wire.endXProperty().set(p.getX());
		wire.endYProperty().set(p.getY());
	}

	@Override
	public void setStart(Point2D p) {
		wire.startXProperty().set(p.getX());
		wire.startYProperty().set(p.getY());
	}

	@Override
	public DoubleProperty startXProperty() {
		return wire.startXProperty();
	}

	@Override
	public DoubleProperty startYProperty() {
		return wire.startYProperty();
	}

	@Override
	public DoubleProperty endXProperty() {
		return wire.endXProperty();
	}

	@Override
	public DoubleProperty endYProperty() {
		return wire.endYProperty();
	}
}
