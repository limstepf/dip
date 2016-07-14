package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * Base class for composite grids. The number of columns is determined by the
 * number of ColumnConstraints, so better set them in one way or another. If no
 * ColumnConstraints are defined, the number of columns will equal the number of
 * children (i.e. only one row will be used).
 *
 * @param <T> class of the parameter's value.
 */
public abstract class CompositeGridBase<T> extends CompositeBase<T, CompositeGridBase.GridView<? extends CompositeGridBase<T>, T>> {

	protected final List<ColumnConstraints> columnConstraints;
	protected final List<RowConstraints> rowConstraints;

	protected final double[] horizontalSpacing = new double[]{0, 0};
	protected final double[] verticalSpacing = new double[]{0, 0};

	/**
	 * Creates a composite grid.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 * @param initialValue initial value.
	 */
	public CompositeGridBase(String label, T defaultValue, T initialValue) {
		super(label, defaultValue, initialValue);

		this.columnConstraints = new ArrayList<>();
		this.rowConstraints = new ArrayList<>();
	}

	/**
	 * Sets the horizontal spacing between cells.
	 *
	 * @param cell horizontal spacing between cells.
	 */
	public void setHorizontalSpacing(double cell) {
		setHorizontalSpacing(cell, 0);
	}

	/**
	 * Sets the horizontal spacing.
	 *
	 * @param cell horizontal spacing between cells.
	 * @param border horizontal spacing at the border.
	 */
	public void setHorizontalSpacing(double cell, double border) {
		this.horizontalSpacing[0] = cell;
		this.horizontalSpacing[1] = border;
	}

	/**
	 * Sets the vertical spacing between cells.
	 *
	 * @param cell vertical spacing between cells.
	 */
	public void setVerticalSpacing(double cell) {
		setVerticalSpacing(cell, 0);
	}

	/**
	 * Sets the vertical spacing.
	 *
	 * @param cell vertical spacing between cells.
	 * @param border vertical spacing at the border.
	 */
	public void setVerticalSpacing(double cell, double border) {
		this.verticalSpacing[0] = cell;
		this.verticalSpacing[1] = border;
	}

	/**
	 * Returns the defined column constraints.
	 *
	 * @return a list of column constraints.
	 */
	public List<ColumnConstraints> getColumnConstraints() {
		return this.columnConstraints;
	}

	/**
	 * Returns the number of columns. The number of columns is determined by the
	 * number of defined column constraints, or the number of children in case
	 * no column constraints are defined.
	 *
	 * @return number of columns of the grid.
	 */
	public int numColumns() {
		if (this.columnConstraints.isEmpty()) {
			return this.getChildren().size();
		}

		return this.columnConstraints.size();
	}

	/**
	 * Sets not further specified column constraints, therby defining the number
	 * of columns in the grid. Once initialized this way, the constraints can be
	 * retrieved to be further defined.
	 *
	 * @param numColumns the number of columns in the grid.
	 */
	public void setColumnConstraints(int numColumns) {
		this.columnConstraints.clear();
		for (int i = 0; i < numColumns; i++) {
			this.columnConstraints.add(new ColumnConstraints());
		}
	}

	/**
	 * Sets custom column constraints.
	 *
	 * @param constraints column constraints.
	 */
	public void setColumnConstraints(ColumnConstraints... constraints) {
		setColumnConstraints(Arrays.asList(constraints));
	}

	/**
	 * Sets custom column constraints.
	 *
	 * @param constraints column constraints.
	 */
	public void setColumnConstraints(List<ColumnConstraints> constraints) {
		this.columnConstraints.clear();
		for (ColumnConstraints c : constraints) {
			this.columnConstraints.add(c);
		}
	}

	/**
	 * Sets simple column constraints given a list of relative widths each
	 * column should expand to.
	 *
	 * @param widths relative widths of the columns (in the range of
	 * {@code [0.0, 1.0]}).
	 */
	public void setColumnWidthConstraints(Double... widths) {
		setColumnWidthConstraints(Arrays.asList(widths));
	}

	/**
	 * Sets simple column constraints given a list of relative widths each
	 * column should expand to.
	 *
	 * @param widths relative widths of the columns (in the range of
	 * {@code [0.0, 1.0]}).
	 */
	public void setColumnWidthConstraints(List<Double> widths) {
		setColumnPercentWidthConstraints(
				widths.stream().map((d) -> {
					return d * 100.0;
				}).collect(toList())
		);
	}

	protected void setColumnPercentWidthConstraints(List<Double> widths) {
		this.columnConstraints.clear();
		final int n = widths.size();

		for (int i = 0; i < n; i++) {
			final ColumnConstraints c = new ColumnConstraints();
			c.setHgrow(Priority.SOMETIMES);
			c.setHalignment(HPos.LEFT);
			c.setPercentWidth(widths.get(i)); // this takes [0.0, 100.0]!
			this.columnConstraints.add(c);
		}
	}

	/**
	 * Returns the defined row constraints.
	 *
	 * @return a list of row constraints.
	 */
	public List<RowConstraints> getRowConstraints() {
		return this.rowConstraints;
	}

	/**
	 * Initializes new, not further specified row constraints. Once initialized
	 * this way, the constraints can be retrieved to be further defined.
	 *
	 * @param numRows number of row constraints to be set up.
	 */
	public void setRowConstraints(int numRows) {
		this.rowConstraints.clear();
		for (int i = 0; i < numRows; i++) {
			this.rowConstraints.add(new RowConstraints());
		}
	}

	/**
	 * Sets custom row constraints.
	 *
	 * @param constraints row constraints.
	 */
	public void setRowConstraints(RowConstraints... constraints) {
		setRowConstraints(Arrays.asList(constraints));
	}

	/**
	 * Sets custom row constraints.
	 *
	 * @param constraints row constraints.
	 */
	public void setRowConstraints(List<RowConstraints> constraints) {
		this.rowConstraints.clear();
		for (RowConstraints c : constraints) {
			this.rowConstraints.add(c);
		}
	}

	/**
	 * Updates a composite child parameter, as that child with given
	 * (persistent) index has changed.
	 *
	 * @param index index of the child parameter that has changed.
	 */
	protected abstract void updateValue(int index);

	/**
	 * Sets/updates all child parameters of the composite.
	 *
	 * @param value new values of the child parameters. This collection might be
	 * empty (keep the default then), so handle the update gracefully.
	 */
	protected abstract void updateChildValues(T value);

	@Override
	protected GridView newViewInstance() {
		return new GridView(this);
	}

	/**
	 * Grid view for composite parameters.
	 *
	 * @param <P> class of the parameter, subclass of CompositeBase.
	 * @param <T> class of the parameter's value.
	 */
	public static class GridView<P extends CompositeGridBase<T>, T> extends PersistentParameterBase.ParameterViewBase<P, T, GridPane> {

		public GridView(P parameter) {
			super(parameter, new GridPane());

			set(parameter.get());
			initGrid();
		}

		protected final void initGrid() {
			root.getColumnConstraints().addAll(parameter.columnConstraints);
			root.getRowConstraints().addAll(parameter.rowConstraints);

			final int width = parameter.numColumns();
			final int numRows = row(parameter.getChildren().size(), width);
			int i = 0; // index for all children
			int j = 0; // index for persistent children only

			for (Parameter p : parameter.getChildren()) {
				final Node node = p.view().node();
				final int col = col(i, width);
				final int row = row(i, width);
				root.add(node, col, row);
				setSpacing(
						node,
						(row > 0),
						(col < width),
						(row < numRows),
						(col > 0)
				);
				if (p.isPersistent()) {
					final PersistentParameter pp = (PersistentParameter) p;
					final int index = j;
					pp.property().addListener((obs, oldV, newV) -> {
						parameter.updateValue(index);
					});
					j++;
				}
				i++;
			}
		}

		// bools are true if we require cell-spacing, border-spacing otherwise
		protected void setSpacing(Node node, boolean top, boolean right, boolean bottom, boolean left) {
			final double[] spacing = new double[]{
				parameter.verticalSpacing[getSpacingIndex(top)],
				parameter.horizontalSpacing[getSpacingIndex(right)],
				parameter.verticalSpacing[getSpacingIndex(bottom)],
				parameter.horizontalSpacing[getSpacingIndex(left)]
			};
			if (hasSpacing(spacing)) {
				GridPane.setMargin(node, new Insets(
						spacing[0],
						spacing[1],
						spacing[2],
						spacing[3]
				));
			}
		}

		protected boolean hasSpacing(double[] spacing) {
			for (double d : spacing) {
				if (d > 0 || d < 0) {
					return true;
				}
			}
			return false;
		}

		protected int getSpacingIndex(boolean isCell) {
			return isCell ? 0 : 1;
		}

		protected int row(int index, int width) {
			return index / width;
		}

		protected int col(int index, int width) {
			return index % width;
		}

		@Override
		public final void set(T value) {
			parameter.updateChildValues(value);
		}
	}

}
