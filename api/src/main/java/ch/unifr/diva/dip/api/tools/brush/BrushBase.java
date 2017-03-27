package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.ui.NamedGlyph;

/**
 * Brush base class.
 */
public abstract class BrushBase implements Brush {

	protected final String name;
	protected final NamedGlyph glyph;
	protected int strokeWidth;
	protected double strokeRadius;

	/**
	 * Creates a new brush base class.
	 *
	 * @param name the name of the brush.
	 * @param glyph the glyph of the brush.
	 */
	public BrushBase(String name, NamedGlyph glyph) {
		this.name = name;
		this.glyph = glyph;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public NamedGlyph getGlyph() {
		return glyph;
	}

	@Override
	public int getStrokeWidth() {
		return strokeWidth;
	}

	/**
	 * Saves the stroke width and radius. Should be called by the
	 * {@code setZoom(double zoom, int strokeWidth)} method of implementing
	 * classes.
	 *
	 * @param strokeWidth the new stroke width.
	 */
	protected void putStrokeWidth(int strokeWidth) {
		this.strokeWidth = strokeWidth;
		this.strokeRadius = strokeWidth / 2.0;
	}

	@Override
	public double getStrokeRadius() {
		return strokeRadius;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ "name=" + this.name
				+ ", glyph=" + this.glyph
				+ ", strokeWidth=" + this.strokeWidth
				+ "}";
	}

}
