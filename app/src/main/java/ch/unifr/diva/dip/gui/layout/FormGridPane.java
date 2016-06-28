package ch.unifr.diva.dip.gui.layout;

import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * A GridPane to easily build up simple forms.
 */
public class FormGridPane extends GridPane {
	private int row = 0;

	public FormGridPane() {
		this.getStyleClass().add("dip-form-grid-pane");

		final ColumnConstraints labelConstraint = new ColumnConstraints();
		final ColumnConstraints valueConstraint = new ColumnConstraints();
		labelConstraint.setHgrow(Priority.SOMETIMES);
		valueConstraint.setHgrow(Priority.ALWAYS);
		getColumnConstraints().addAll(labelConstraint, valueConstraint);
	}

	public FormGridPane(ColumnConstraints... constraints) {
		this.getStyleClass().add("dip-form-grid-pane");
		getColumnConstraints().addAll(constraints);
	}

	@Override
	public void addRow(int i, Node... nodes) {
		this.row = i;
		addRow(nodes);
	}

	public void addRow(Node... nodes) {
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
}
