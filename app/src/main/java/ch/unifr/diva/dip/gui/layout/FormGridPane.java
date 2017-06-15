package ch.unifr.diva.dip.gui.layout;

import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import java.util.Arrays;
import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * A GridPane to easily build up simple forms.
 */
public class FormGridPane extends GridPane {

	private int row = 0;

	/**
	 * Creates a new FormGridPane.
	 */
	public FormGridPane() {
		this.getStyleClass().add("dip-form-grid-pane");

		final ColumnConstraints labelConstraint = new ColumnConstraints();
		final ColumnConstraints valueConstraint = new ColumnConstraints();
		labelConstraint.setHgrow(Priority.SOMETIMES);
		valueConstraint.setHgrow(Priority.ALWAYS);
		getColumnConstraints().addAll(labelConstraint, valueConstraint);
	}

	/**
	 * Creates a new FormGridPane with column constraints.
	 *
	 * @param constraints column constraints.
	 */
	public FormGridPane(ColumnConstraints... constraints) {
		this.getStyleClass().add("dip-form-grid-pane");
		getColumnConstraints().addAll(constraints);
	}

	@Override
	public void addRow(int i, Node... nodes) {
		this.row = i;
		addRow(nodes);
	}

	/**
	 * Clears the form grid pane and resets the internal row counter.
	 */
	public void clear() {
		this.getChildren().clear();
		this.row = 0;
	}

	/**
	 * Adds a row.
	 *
	 * @param <T> a node class.
	 * @param nodes nodes that make up the row, a cell/column per node.
	 */
	public <T extends Node> void addRow(T... nodes) {
		addRow(Arrays.asList(nodes));
	}

	/**
	 * Adds a row.
	 *
	 * @param <T> a node class.
	 * @param nodes nodes that make up the row, a cell/column per node.
	 */
	public <T extends Node> void addRow(List<T> nodes) {
		int column = 0;
		for (Node node : nodes) {
			if (node != null) {
				GridPane.setValignment(node, VPos.TOP);
				add(node, column, row);
			}
			column++;
		}
		row++;
	}

	/**
	 * Adds a node spanning multiple columns.
	 *
	 * @param <T> a node class.
	 * @param node the node.
	 * @param colspan the column span.
	 */
	public <T extends Node> void addSpanRow(T node, int colspan) {
		addSpanRow(node, 0, colspan, 1);
	}

	/**
	 * Adds a node spanning multiple columns and/or rows.
	 *
	 * @param <T> a node class.
	 * @param node the node.
	 * @param colspan the column span.
	 * @param rowspan the row span.
	 */
	public <T extends Node> void addSpanRow(T node, int colspan, int rowspan) {
		addSpanRow(node, 0, colspan, rowspan);
	}

	/**
	 * Adds a node spanning multiple columns and/or rows.
	 *
	 * @param <T> a node class.
	 * @param node the node.
	 * @param coloffset the column offset, 0 to start at the first column.
	 * @param colspan the column span.
	 * @param rowspan the row span.
	 */
	public <T extends Node> void addSpanRow(T node, int coloffset, int colspan, int rowspan) {
		this.add(node, coloffset, row, colspan, rowspan);
		row = row + rowspan;
	}

	/**
	 * Adds parameters to the form grid, one row per parameter. This needs a
	 * grid of 2 columns: one for the label, the other for the parameter view.
	 *
	 * @param parameters DIP parameters.
	 */
	public void addParameters(Parameter... parameters) {
		addParameters(Arrays.asList(parameters));
	}

	/**
	 * Adds a list of parameters to the form grid, one row per parameter. This
	 * needs a grid of 2 columns: one for the label, the other for the parameter
	 * view.
	 *
	 * @param parameters a list of DIP parameters.
	 */
	public void addParameters(List<Parameter> parameters) {
		for (Parameter p : parameters) {
			final Label label;
			if (p.isPersistent()) {
				final PersistentParameter pp = (PersistentParameter) p;
				label = newLabel((pp.label().isEmpty()) ? "" : pp.label() + ":");
			} else {
				label = newLabel("");
			}

			this.addRow(label, p.view().node());
		}
	}

	/**
	 * Returns a styled label for FormGridPanes.
	 *
	 * @param text text of the label.
	 * @return the label.
	 */
	public static Label newLabel(String text) {
		final Label label = new Label(text);
		label.getStyleClass().add("dip-small");
		return label;
	}

}
