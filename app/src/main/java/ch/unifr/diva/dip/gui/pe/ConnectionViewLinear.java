package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/**
 * A wire modelled with a line.
 */
public class ConnectionViewLinear extends ConnectionViewBase<Line> {

	public ConnectionViewLinear(InputPort<?> input) {
		super(input, newWire());
	}

	private static Line newWire() {
		final Line w = new Line();
		w.getStyleClass().add("dip-wire");
		w.setStrokeLineCap(StrokeLineCap.ROUND);
		w.setStrokeWidth(PipelineEditor.WIRE_RADIUS * PipelineEditor.PORT_RATIO);
		return w;
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
