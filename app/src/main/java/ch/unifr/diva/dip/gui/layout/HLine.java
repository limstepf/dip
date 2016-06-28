
package ch.unifr.diva.dip.gui.layout;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

/**
 * A resizable, horizontal {@code Line}.
 */
public class HLine extends Pane {

	private final Line line;
	private DoubleProperty yProperty;

	public HLine() {
		this.setMinSize(0,0);
		this.yProperty = new SimpleDoubleProperty();
		this.line = new Line();
		this.line.setManaged(false);
		this.line.startXProperty().set(0);
		this.line.endXProperty().bind(this.widthProperty());

		this.heightProperty().addListener((e) -> {
			final double y = ((int) (HLine.this.getHeight() * .5)) + 1.5;
			this.yProperty.set(y);
			this.line.startYProperty().set(y);
			this.line.endYProperty().set(y);
		});

		this.getChildren().add(this.line);
	}

	public Line line() {
		return this.line;
	}

	public ReadOnlyDoubleProperty yProperty() {
		return this.yProperty;
	}

	public void addArrowHead(ArrowHead head) {
		head.bind(this.widthProperty(), this.yProperty());
		this.getChildren().add(head);
	}

	public void removeArrowHead(ArrowHead head) {
		head.unbind();
		this.getChildren().remove(head);
	}
}
