package ch.unifr.diva.dip.api.tools.brush;

import static ch.unifr.diva.dip.api.tools.brush.CrosshairCursor.CROSSHAIR_RADIUS;
import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

/**
 * A crosshair cursor with an (arithmetic) symbol in the lower right corner.
 */
public class SymbolCrosshairCursor extends CrosshairCursor {

	/**
	 * Available (arithmetic) symbols.
	 */
	public enum Symbol {

		/**
		 * The empty/no symbol.
		 */
		NONE,
		/**
		 * A circle (or closing) symbol.
		 */
		CIRCLE,
		/**
		 * A plus (or addition) symbol.
		 */
		PLUS,
		/**
		 * A minus (or substraction) symbol.
		 */
		MINUS;
	}

	protected NoneSymbol currentSymbol;
	protected final NoneSymbol noneSymbol = new NoneSymbol();
	protected final NoneSymbol plusSymbol = new PlusSymbol();
	protected final NoneSymbol minusSymbol = new MinusSymbol();
	protected final NoneSymbol circleSymbol = new CircleSymbol();

	/**
	 * Creates a new crosshair cursor with an (arithmetic) symbol in the lower
	 * right corner.
	 */
	public SymbolCrosshairCursor() {
		super();
		this.currentSymbol = noneSymbol;
	}

	@Override
	protected void setZoom(double zoom, double inv) {
		super.setZoom(zoom, inv);
		this.currentSymbol.setZoom(zoom, inv);
	}

	/**
	 * Updates/sets the symbol of the cursor.
	 *
	 * @param symbol the new symbol.
	 */
	public void setSymbol(Symbol symbol) {
		// remove symbol
		if (!this.currentSymbol.getSymbol().equals(Symbol.NONE)) {
			getChildren().removeAll(this.currentSymbol.getShapes());
		}

		// add symbol
		switch (symbol) {
			case NONE:
				this.currentSymbol = noneSymbol;
				break;
			case PLUS:
				this.currentSymbol = plusSymbol;
				break;
			case MINUS:
				this.currentSymbol = minusSymbol;
				break;
			case CIRCLE:
				this.currentSymbol = circleSymbol;
				break;
		}
		if (!this.currentSymbol.getSymbol().equals(Symbol.NONE)) {
			getChildren().addAll(this.currentSymbol.getShapes());
		}
	}

	/**
	 * Returns the current symbol of the cursor.
	 *
	 * @return the current symbol.
	 */
	public Symbol getSymbol() {
		return this.currentSymbol.getSymbol();
	}

	// common ratio w.r.t. the crosshair's radius
	protected final static double RAD_RATIO = .71;

	/**
	 * The none, or empty symbol.
	 */
	protected static class NoneSymbol {

		private final static Shape[] shapes = new Shape[]{};

		/**
		 * Returns the symbol type.
		 *
		 * @return the symbol type.
		 */
		public Symbol getSymbol() {
			return Symbol.NONE;
		}

		/**
		 * Updates/sets the zoom factor.
		 *
		 * @param zoom the zoom factor.
		 * @param inv the inverse of the zoom factor.
		 */
		public void setZoom(double zoom, double inv) {

		}

		/**
		 * Returns the shapes that make up the symbol.
		 *
		 * @return the shapes.
		 */
		public Shape[] getShapes() {
			return shapes;
		}

	}

	/**
	 * A plus symbol.
	 */
	protected static class PlusSymbol extends NoneSymbol {

		protected final Line hline = new Line(0, 0, CROSSHAIR_RADIUS * RAD_RATIO, 0);
		protected final Line vline = new Line(0, 0, 0, CROSSHAIR_RADIUS * RAD_RATIO);
		protected final Shape[] shapes = new Shape[]{
			hline, vline
		};

		/**
		 * Creates a new plus symbol.
		 */
		public PlusSymbol() {
			ShapeUtils.setStroke(Color.WHITE, hline, vline);
		}

		@Override
		public Symbol getSymbol() {
			return Symbol.PLUS;
		}

		@Override
		public void setZoom(double zoom, double inv) {
			final double rad = CROSSHAIR_RADIUS * inv;
			final double pxc = .5 * inv; // hit center of pixel for sharp lines
			final double r2 = rad * .5 * RAD_RATIO;
			final double offset = rad + pxc;
			final double start = offset - r2;
			final double end = offset + r2;

			hline.setStrokeWidth(inv);
			hline.setStartY(offset);
			hline.setEndY(offset);
			hline.setStartX(start);
			hline.setEndX(end);

			vline.setStrokeWidth(inv);
			vline.setStartX(offset);
			vline.setEndX(offset);
			vline.setStartY(start);
			vline.setEndY(end);
		}

		@Override
		public Shape[] getShapes() {
			return shapes;
		}

	}

	/**
	 * A minus symbol.
	 */
	protected static class MinusSymbol extends NoneSymbol {

		protected final Line hline = new Line(0, 0, CROSSHAIR_RADIUS * RAD_RATIO, 0);
		protected final Shape[] shapes = new Shape[]{
			hline
		};

		/**
		 * Creates a new minus symbol.
		 */
		public MinusSymbol() {
			ShapeUtils.setStroke(Color.WHITE, hline);
		}

		@Override
		public Symbol getSymbol() {
			return Symbol.MINUS;
		}

		@Override
		public void setZoom(double zoom, double inv) {
			final double rad = CROSSHAIR_RADIUS * inv;
			final double pxc = .5 * inv; // hit center of pixel for sharp lines
			final double r2 = rad * .5 * RAD_RATIO;
			final double offset = rad + pxc;

			hline.setStrokeWidth(inv);
			hline.setStartY(offset);
			hline.setEndY(offset);
			hline.setStartX(offset - r2);
			hline.setEndX(offset + r2);
		}

		@Override
		public Shape[] getShapes() {
			return shapes;
		}
	}

	/**
	 * A circle symbol
	 */
	protected static class CircleSymbol extends NoneSymbol {

		protected final Circle circle = new Circle();
		protected final Shape[] shapes = new Shape[]{
			circle
		};

		/**
		 * Creates a new circle symbol.
		 */
		public CircleSymbol() {
			ShapeUtils.setStroke(Color.WHITE, circle);
		}

		@Override
		public Symbol getSymbol() {
			return Symbol.CIRCLE;
		}

		@Override
		public void setZoom(double zoom, double inv) {
			final double rad = CROSSHAIR_RADIUS * inv;
			final double pxc = .5 * inv; // hit center of pixel for sharp lines
			final double r2 = rad * .5 * RAD_RATIO;
			final double offset = rad + pxc;

			circle.setStrokeWidth(inv);
			circle.setCenterX(offset);
			circle.setCenterY(offset);
			circle.setRadius(r2);
		}

		@Override
		public Shape[] getShapes() {
			return shapes;
		}
	}

}
