package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import static ch.unifr.diva.dip.gui.pe.PipelineEditor.PORT_RATIO;
import static ch.unifr.diva.dip.gui.pe.PipelineEditor.WIRE_RADIUS;
import static ch.unifr.diva.dip.gui.pe.PipelineEditor.hashColor;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * I/O Port view.
 *
 * @param <T>
 */
public class PortView<T extends Port> extends Pane {

	private final PipelineEditor editor;
	public final String key;
	public final T port;
	private final List<ConnectionView> wires;
	private final DoubleProperty centerXProperty;
	private final DoubleProperty centerYProperty;
	private final Circle[] circles = new Circle[3];

	public PortView(PipelineEditor editor, String key, T port) {
		this.editor = editor;
		this.key = key;
		this.port = port;
		this.wires = new ArrayList<>();

		final Color colorFill = Color.rgb(96, 96, 96);
		final Color colorStroke = Color.rgb(34, 34, 34);

		circles[0] = new Circle(WIRE_RADIUS, WIRE_RADIUS, WIRE_RADIUS, colorFill);
		circles[1] = new Circle(WIRE_RADIUS, WIRE_RADIUS, WIRE_RADIUS * PORT_RATIO, colorFill);
		circles[2] = new Circle(WIRE_RADIUS, WIRE_RADIUS, WIRE_RADIUS * 0.43, colorFill);

		this.centerXProperty = new SimpleDoubleProperty(0);
		this.centerYProperty = new SimpleDoubleProperty(0);

		final Tooltip toolTip = new Tooltip(
				String.format("%s\n%s", key, port.getDataType().dataFormat())
		);
		for (Circle c : circles) {
			c.setStroke(colorStroke);
			c.setStrokeWidth(0.5);
			Tooltip.install(c, toolTip);
		}

		final Color colorT = hashColor(port);

		if (this.port instanceof InputPort) {
			circles[2].setFill(colorT);
		} else {
			circles[1].setFill(colorT);
		}

		this.getChildren().addAll(circles);
		setupDraggable();
	}

	// we remember wires connected to a port s.t. we can bring them visually to
	// front together with their selected processor.
	public void addWire(ConnectionView wire) {
		wires.add(wire);
	}

	public void removeWire(ConnectionView wire) {
		wires.remove(wire);
	}

	public boolean hasWires() {
		return !wires.isEmpty();
	}

	public List<ConnectionView> wires() {
		return wires;
	}

	public void updateCenter() {
		final Point2D p = editor.editorPane().sceneToPane(centerInScene());
		centerXProperty().set(p.getX());
		centerYProperty().set(p.getY());
	}

	public DoubleProperty centerXProperty() {
		return this.centerXProperty;
	}

	public DoubleProperty centerYProperty() {
		return this.centerYProperty;
	}

	private Point2D centerInScene() {
		return circles[0].localToScene(
				circles[0].getCenterX(),
				circles[0].getCenterY()
		);
	}

	private void setupDraggable() {
		final Node node = this;
		if (port instanceof InputPort) {
			node.setOnDragDetected(e -> onDragDetected(e, (InputPort) port));
			node.setOnDragDone(e -> onDragDone(e, (InputPort) port));
			node.setOnMouseEntered(e -> onMouseEntered(e));
			node.setOnMouseExited(e -> onMouseExited(e));
		} else {
			node.setOnDragOver(e -> onDragOver(e));
			node.setOnDragEntered(e -> onDragEntered(e));
			node.setOnDragExited(e -> onDragExited(e));
			node.setOnDragDropped(e -> onDragDropped(e, (OutputPort) port));
		}
	}

	private void onMouseEntered(MouseEvent e) {
		editor.setCursor(Cursor.HAND);
	}

	private void onMouseExited(MouseEvent e) {
		editor.setCursor(Cursor.DEFAULT);
	}

	private void onDragDetected(MouseEvent e, InputPort input) {
		if (input.isConnected()) {
			editor.editorPane().setWire(editor.editorPane().getConnectionView(input));
			editor.editorPane().wire().unbind();
			editor.editorPane().moveWire(e);
		} else {
			editor.editorPane().setWire(editor.editorPane().newWire(input));

			final Point2D start = editor.editorPane().sceneToPane(this.centerInScene());
			editor.editorPane().wire().setStart(start);
			editor.editorPane().moveWire(e);
		}
		editor.editorPane().wire().setLoose(true);

		final Dragboard db = this.startDragAndDrop(TransferMode.LINK);
		final ClipboardContent content = new ClipboardContent();
		content.put(input.getDataType().dataFormat(), true);
		db.setContent(content);

		e.consume();
	}

	private void onDragOver(DragEvent e) {
		final Dragboard db = e.getDragboard();
		if (db.hasContent(port.getDataType().dataFormat())) {
			e.acceptTransferModes(TransferMode.LINK);
		}

		editor.editorPane().moveWire(e);
		e.consume();
	}

	private void onDragEntered(DragEvent e) {
		final Dragboard db = e.getDragboard();
		if (db.hasContent(port.getDataType().dataFormat())) {
			editor.editorPane().wire().setLoose(false);
		} else {
			editor.editorPane().wire().setLoose(true);
		}

		e.consume();
	}

	private void onDragExited(DragEvent e) {
		editor.editorPane().wire().setLoose(true);
		e.consume();
	}

	private void onDragDropped(DragEvent e, OutputPort output) {
		editor.editorPane().setSelectedPort(output);

		e.setDropCompleted(true);
		e.consume();
	}

	private void onDragDone(DragEvent e, InputPort input) {
		if (editor.editorPane().hasSelectedPort()) {
			// re-connect -> disconnect first (wire is already unbound)
			if (input.isConnected()) {
				input.disconnect();
			}

			// connect to OutputPort and bind wire
			editor.editorPane().newConnection(
					editor.editorPane().selectedPort(),
					input,
					editor.editorPane().wire()
			);
			editor.editorPane().wire().setLoose(false);
		} else {
			if (input.isConnected()) {
				// remove connection
				editor.editorPane().removeConnection(input);
			} else {
				// just remove wire
				editor.editorPane().removeWire(editor.editorPane().wire());
			}
		}

		editor.editorPane().setSelectedPort(null);
		editor.editorPane().setWire(null);

		e.consume();
	}

}
