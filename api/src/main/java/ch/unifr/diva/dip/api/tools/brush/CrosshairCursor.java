package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * A crosshair cursor.
 */
public class CrosshairCursor extends Group implements Cursor {

	protected final static int CROSSHAIR_RADIUS = 6;
	protected final static int CROSSHAIR_DIA = 2 * CROSSHAIR_RADIUS;
	protected final Line hline;
	protected final Line vline;
	protected double crosshairThreshold;
	protected double pxc; // pixel center offset

	/**
	 * Creates a new crosshair cursor.
	 */
	public CrosshairCursor() {
		this.hline = new Line(0, 0, CROSSHAIR_DIA, 0);
		this.vline = new Line(0, 0, 0, CROSSHAIR_DIA);
		crosshairThreshold = CROSSHAIR_DIA + 1;
		pxc = .5;

		getChildren().setAll(hline, vline);
		ShapeUtils.setStroke(Color.WHITE, hline, vline);
		setBlendMode(BlendMode.EXCLUSION);
	}

	@Override
	public void setZoom(double zoom) {
		setZoom(zoom, 1.0 / zoom);
	}

	protected void setZoom(double zoom, double inv) {
		final double dia = CROSSHAIR_DIA * inv;
		final double rad = CROSSHAIR_RADIUS * inv;
		crosshairThreshold = dia + inv * 5;

		this.pxc = .5 * inv; // hit center of pixel for sharp lines
		final double offset = pxc - rad;
		hline.setStrokeWidth(inv);
		hline.setStartY(pxc);
		hline.setEndY(pxc);
		hline.setStartX(offset);
		hline.setEndX(dia + offset);

		vline.setStrokeWidth(inv);
		vline.setStartX(pxc);
		vline.setEndX(pxc);
		vline.setStartY(offset);
		vline.setEndY(dia + offset);
	}

}
