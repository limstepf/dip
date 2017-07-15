package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.StringMatrix;
import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Expression matrix parameter. Backed by a {@code StringMatrix} where elements
 * are assumed to be math. expressions. Can be converted to a
 * {@code FloatMatrix} or {@code DoubleMatrix}.
 *
 * <p>
 * Note that if you're trying to alter the already set {@code StringMatrix}
 * directly a call to {@code set()} will not have the desired effect, since that
 * value is already set. Use {@code invalidate()}.
 *
 * <h4>Example:</h4>
 * Instead of:
 * <pre>
 * <code>
 * ExpMatrixParameter matrix = new ExpMatrixParameter("mat", new StringMatrix(3,3));
 * matrix.set(matrix.get().fill("e")); // WRONG! (will not invalidate the property!)
 * </code>
 * </pre>
 * <br />
 * ...you should do this:
 * <pre>
 * <code>
 * ExpMatrixParameter matrix = new ExpMatrixParameter("mat", new StringMatrix(3,3));
 * matrix.get().fill("e");
 * matrix.invalidate();
 * </code>
 * </pre>
 *
 * <p>
 * Have a look the Javadoc for {@code ExpParameter} to see what of math.
 * operators, functions, and numberical constants are supported.
 *
 * @see ExpParameter
 * @see PersistentParameterBase
 */
public class ExpMatrixParameter extends PersistentParameterBase<StringMatrix, ExpMatrixParameter.StringMatrixView> {

	protected double cellSpacing = 3.0;
	protected double minCellWidth = 15.0;
	protected double prefCellWidth = 95.0;
	protected double maxCellWidth = Double.MAX_VALUE;

	/**
	 * Creates a new expression matrix parameter.
	 *
	 * @param label label of the parameter.
	 * @param defaultValue default string matrix.
	 */
	public ExpMatrixParameter(String label, StringMatrix defaultValue) {
		super(label, defaultValue);
	}

	/**
	 * Returns the minimum width of a cell/an element of the matrix.
	 *
	 * @return the minimum cell width.
	 */
	public double getMinCellWidth() {
		return this.minCellWidth;
	}

	/**
	 * Returns the preferred width of a cell/an element of the matrix.
	 *
	 * @return the preferred cell width.
	 */
	public double getPrefCellWidth() {
		return this.prefCellWidth;
	}

	/**
	 * Returns the maximum width of a cell/an element of the matrix.
	 *
	 * @return the maximum cell width.
	 */
	public double getMaxCellWidth() {
		return this.maxCellWidth;
	}

	/**
	 * Sets the width of a cell/an element of the matrix. This sets the minimum,
	 * preferred, and maximum width on all {@code ColumnConstraints} in the grid
	 * (to which the {@code TextField}s expand to).
	 *
	 * <p>
	 * To get the {@code TextField} of a cell/an element of the matrix to an
	 * exact size, you need to add the cell spacing/margin twice to the
	 * cellWidth, i.e. the desired width + 2 * {@code getCellSpacing()}.
	 *
	 * @param cellWidth the width of a cell/an element in the matrix.
	 */
	public void setCellWidth(double cellWidth) {
		setCellWidth(cellWidth, cellWidth, cellWidth);
	}

	/**
	 * Sets the width of a cell/an element of the matrix. This sets the minimum,
	 * preferred, and maximum width on all {@code ColumnConstraints} in the grid
	 * (to which the {@code TextField}s expand to).
	 *
	 * @param minCellWidth the minimum width of a cell/an element in the matrix.
	 * @param prefCellWidth the preferred width of a cell/an element in the
	 * matrix.
	 * @param maxCellWidth the maximum width of a cell/an element in the matrix.
	 */
	public void setCellWidth(double minCellWidth, double prefCellWidth, double maxCellWidth) {
		this.minCellWidth = minCellWidth;
		this.prefCellWidth = prefCellWidth;
		this.maxCellWidth = maxCellWidth;

		if (view != null) {
			view.updateCellWidth(minCellWidth, prefCellWidth, maxCellWidth);
		}
	}

	/**
	 * Returns the cell spacing (or margin) between cells/elements of the
	 * matrix.
	 *
	 * @return the cell spacing (or margin).
	 */
	public double getCellSpacing() {
		return this.cellSpacing;
	}

	/**
	 * Sets the cell spacing (or margin) between cells/elements of the matrix.
	 *
	 * @param cellSpacing the spacing (or margin).
	 */
	public void setCellSpacing(double cellSpacing) {
		this.cellSpacing = cellSpacing;

		if (view != null) {
			view.updateCellSpacing(cellSpacing);
		}
	}

	@Override
	protected StringMatrixView newViewInstance() {
		return new StringMatrixView(this);
	}

	/**
	 * StringMatrix view.
	 */
	public static class StringMatrixView extends PersistentParameterBase.ParameterViewBase<ExpMatrixParameter, StringMatrix, BorderPane> {

		protected final GridPane grid;
		protected final Region leftBorder;
		protected final Region rightBorder;
		protected Insets margin;
		protected int currentPrecision = -1;
		protected String format;
		protected TextField[] textfields; // linear indexing
		protected Tooltip[] tooltips;
		protected InvalidationListener[] listeners;
		protected StringMatrix.Layout currentLayout;
		protected int currentRows = -1;
		protected int currentColumns = -1;
		protected boolean enabledListeners = true;

		/**
		 * Creates a new expression matrix view.
		 *
		 * @param parameter the expression matrix parameter.
		 */
		public StringMatrixView(ExpMatrixParameter parameter) {
			super(parameter, new BorderPane());

			this.margin = new Insets(parameter.cellSpacing);

			this.grid = new GridPane();
			this.grid.setMinWidth(parameter.minCellWidth);
			this.grid.setMaxWidth(Double.MAX_VALUE);
			this.grid.setPadding(new Insets(parameter.cellSpacing, 0, parameter.cellSpacing, 0));

			this.root.setCenter(this.grid);
			this.root.setMinWidth(parameter.minCellWidth);

			this.leftBorder = new Region();
			this.leftBorder.getStyleClass().add("dip-matrix-left");
			this.root.setLeft(this.leftBorder);

			this.rightBorder = new Region();
			this.rightBorder.getStyleClass().add("dip-matrix-right");
			this.root.setRight(this.rightBorder);

			set(parameter.get());
		}

		protected TextField newTextField(String initialValue) {
			final TextField tf = new TextField(initialValue);
			tf.getStyleClass().add("dip-text-input");
			tf.setMaxWidth(Double.MAX_VALUE);
			GridPane.setMargin(tf, margin);
			return tf;
		}

		protected String getExpression(int index) {
			return this.textfields[index].getText();
		}

		protected void updateTooltip(int index) {
			tooltips[index].setText(Double.toString(
					parameter.get().getDouble(index)
			));
		}

		protected void setGrid(StringMatrix value) {
			currentLayout = value.layout;
			currentColumns = value.columns;
			currentRows = value.rows;

			if (textfields != null) {
				// clean up, remove listeners
				for (int i = 0; i < textfields.length; i++) {
					textfields[i].textProperty().removeListener(listeners[i]);
				}
			}

			textfields = new TextField[value.data.length];
			tooltips = new Tooltip[value.data.length];
			listeners = new InvalidationListener[value.data.length];

			for (int i = 0; i < value.data.length; i++) {
				// textfield view hook!
				textfields[i] = newTextField(format(value.data[i]));
				tooltips[i] = new Tooltip();
				textfields[i].setTooltip(tooltips[i]);

				// attach listeners
				final int index = i;
				listeners[i] = (obs) -> {
					final String expression = getExpression(index);
					final boolean isValid = isValidExpression(expression);
					textfields[index].pseudoClassStateChanged(PersistentParameter.ALERT, !isValid);
					parameter.get().set(index, expression);
					updateTooltip(index);
					invalidateMatrix();
				};
				textfields[i].textProperty().addListener(listeners[i]);
				updateTooltip(index);
			}

			// populate grid
			final ColumnConstraints cc = new ColumnConstraints(
					parameter.minCellWidth, parameter.prefCellWidth, parameter.maxCellWidth,
					Priority.ALWAYS, HPos.LEFT, true
			);
			this.grid.getColumnConstraints().clear();
			for (int i = 0; i < value.columns; i++) {
				this.grid.getColumnConstraints().add(cc);
			}

			grid.getChildren().clear();
			for (int i = 0; i < value.rows; i++) {
				for (int j = 0; j < value.columns; j++) {
					final int index = currentLayout.index(i, j, value.rows, value.columns);
					grid.add(textfields[index], j, i);
				}
			}

			// a new matrix (size) requires a different sized dialog/window
			final Scene scene = this.root.getScene();
			if (scene != null) {
				final Window window = this.root.getScene().getWindow();
				if (window != null) {
					window.sizeToScene();
				}
			}
		}

		protected void invalidateMatrix() {
			if (enabledListeners) {
				parameter.valueProperty.invalidate();
			}
		}

		public void updateCellWidth(double minCellWidth, double prefCellWidth, double maxCellWidth) {
			for (ColumnConstraints cc : this.grid.getColumnConstraints()) {
				cc.setMinWidth(minCellWidth);
				cc.setPrefWidth(prefCellWidth);
				cc.setMaxWidth(maxCellWidth);
			}
		}

		public void updateCellSpacing(double cellSpacing) {
			this.margin = new Insets(parameter.cellSpacing);
			this.grid.setPadding(new Insets(cellSpacing, 0, cellSpacing, 0));
			for (int i = 0; i < this.textfields.length; i++) {
				GridPane.setMargin(textfields[i], margin);
			}
		}

		protected void setValues(StringMatrix value) {
			this.enabledListeners = false;
			for (int i = 0; i < value.data.length; i++) {
				textfields[i].setText(format(value.data[i]));
			}
			this.enabledListeners = true;
			invalidateMatrix();
		}

		protected boolean needsNewGrid(StringMatrix value) {
			return value.rows != currentRows
					|| value.columns != currentColumns
					|| !value.layout.equals(currentLayout);
		}

		protected boolean isValidExpression(String expression) {
			try {
				Expression exp = new ExpressionBuilder(expression.toLowerCase()).build();
				if (exp.validate().isValid()) {
					return true;
				}
			} catch (Exception ex) {
				// invalid
			}

			return false;
		}

		protected String format(String value) {
			return (value == null) ? Double.toString(parameter.get().getNullValue()) : value;
		}

		@Override
		public final void set(StringMatrix value) {
			if (needsNewGrid(value)) {
				setGrid(value);
			} else {
				setValues(value);
			}
		}

	}

}
