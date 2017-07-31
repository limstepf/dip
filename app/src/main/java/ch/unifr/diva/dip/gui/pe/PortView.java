package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.core.model.PipelineLayoutStrategy;
import static ch.unifr.diva.dip.gui.pe.PipelineEditor.PORT_RATIO;
import static ch.unifr.diva.dip.gui.pe.PipelineEditor.WIRE_RADIUS;
import static ch.unifr.diva.dip.gui.pe.PipelineEditor.hashColor;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * I/O Port view.
 *
 * @param <T> type of the port ({@code InputPort} or {@code OutputPort}).
 */
public class PortView<T extends Port<?>> extends Pane {

	private final ProcessorView processorView;
	private final PipelineEditor editor;
	private final List<ConnectionView> wires;
	private final DoubleProperty centerXProperty;
	private final DoubleProperty centerYProperty;
	private final Circle[] circles = new Circle[3];
	private Label label;

	/**
	 * The key of the port.
	 */
	public final String key;

	/**
	 * The port.
	 */
	public final T port;

	/**
	 * The port color.
	 */
	public final Color color;

	/**
	 * Creates a new port view.
	 *
	 * @param processorView the parent processor view.
	 * @param key the key of the port.
	 * @param port the port.
	 */
	public PortView(ProcessorView processorView, String key, T port) {
		this.processorView = processorView;
		this.editor = processorView.editor();
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

		for (Circle c : circles) {
			c.setStroke(colorStroke);
			c.setStrokeWidth(0.5);
		}

		this.color = hashColor(port);

		if (this.port instanceof InputPort) {
			circles[2].setFill(color);
		} else {
			circles[1].setFill(color);
		}

		this.getChildren().addAll(circles);
		setupDraggable();
	}

	/**
	 * Returns the port label.
	 *
	 * @return the port label.
	 */
	public Label<T> getLabel() {
		if (label == null) {
			label = new Label(this, port.getLabel(), color);
		}
		return label;
	}

	/*
	 * we remember wires connected to a port s.t. we can bring them visually to
	 * front together with their selected processor.
	 */
	//
	/**
	 * Adds a wire.
	 *
	 * @param wire the wire.
	 */
	public void addWire(ConnectionView wire) {
		wires.add(wire);
	}

	/**
	 * Remove a wire.
	 *
	 * @param wire the wire.
	 */
	public void removeWire(ConnectionView wire) {
		wires.remove(wire);
	}

	/**
	 * Checks whether some wire(s) are registered/connected.
	 *
	 * @return {@code true} if some wire(s) are registered/connected,
	 * {@code false} otherwise.
	 */
	public boolean hasWires() {
		return !wires.isEmpty();
	}

	/**
	 * Returns the list of registered/connected wires.
	 *
	 * @return the list of registered/connected wires.
	 */
	public List<ConnectionView> wires() {
		return wires;
	}

	/**
	 * Updates the center properties.
	 */
	public void updateCenter() {
		final Point2D p = editor.editorPane().sceneToPane(getPortCenterInScene());
		centerXProperty().set(p.getX());
		centerYProperty().set(p.getY());
	}

	/**
	 * The centerX property.
	 *
	 * @return the centerX property.
	 */
	public DoubleProperty centerXProperty() {
		return this.centerXProperty;
	}

	/**
	 * The centerY property.
	 *
	 * @return the centerY property.
	 */
	public DoubleProperty centerYProperty() {
		return this.centerYProperty;
	}

	private Point2D getPortCenterInScene() {
		return circles[0].localToScene(
				circles[0].getCenterX(),
				circles[0].getCenterY()
		);
	}

	private void setupDraggable() {
		if (port instanceof InputPort) {
			final InputPort<?> input = (InputPort) port;

			setOnMouseEntered(e -> onMouseEntered(e));
			setOnMouseExited(e -> onMouseExited(e));
			setOnDragDetected(e -> onDragDetected(e, input));
			setOnDragDone(e -> onDragDone(e, input));
		} else {
			setOnMouseEntered(e -> onMouseEnteredOutputPort(e));
			setOnMouseExited(e -> onMouseExitedOutputPort(e));

			setOnDragOver(e -> onDragOver(e));
			setOnDragEntered(e -> onDragEntered(e));
			setOnDragExited(e -> onDragExited(e));
			setOnDragDropped(e -> onDragDropped(e, (OutputPort) port));
		}
	}

	private void onMouseEntered(MouseEvent e) {
		editor.setCursor(Cursor.HAND);
		processorView.addPortLabels();
	}

	private void onMouseExited(MouseEvent e) {
		editor.setCursor(Cursor.DEFAULT);
		processorView.removePortLabels();
	}

	private void onMouseEnteredOutputPort(MouseEvent e) {
		processorView.addPortLabels();
	}

	private void onMouseExitedOutputPort(MouseEvent e) {
		processorView.removePortLabels();
	}

	private void onDragDetected(MouseEvent e, InputPort<?> input) {
		if (input.isConnected()) {
			editor.editorPane().setWire(editor.editorPane().getConnectionView(input));
			editor.editorPane().wire().unbind();
			editor.editorPane().moveWire(e);
		} else {
			editor.editorPane().setWire(editor.editorPane().newWire(input));

			final Point2D start = editor.editorPane().sceneToPane(getPortCenterInScene());
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

		processorView.addPortLabels();
		e.consume();
	}

	private void onDragExited(DragEvent e) {
		editor.editorPane().wire().setLoose(true);

		processorView.removePortLabels();
		e.consume();
	}

	private void onDragDropped(DragEvent e, OutputPort<?> output) {
		editor.editorPane().setSelectedPort(output);

		e.setDropCompleted(true);
		e.consume();
	}

	private void onDragDone(DragEvent e, InputPort<?> input) {
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
			editor.selectedPipeline().modifiedProperty().set(true);
		} else {
			if (input.isConnected()) {
				// remove connection
				editor.editorPane().removeConnection(input);
				editor.selectedPipeline().modifiedProperty().set(true);
			} else {
				// just remove wire
				editor.editorPane().removeWire(editor.editorPane().wire());
			}
		}

		editor.editorPane().setSelectedPort(null);
		editor.editorPane().setWire(null);

		e.consume();
	}

	/**
	 * A port label.
	 *
	 * @param <T> type of the port ({@code InputPort} or {@code OutputPort}).
	 */
	public static class Label<T extends Port<?>> extends Pane {

		public final javafx.scene.control.Label label;
		public final PortView<T> port;
		public final PipelineLayoutStrategy layout;
		protected final boolean isInputPort;
		protected final InvalidationListener portPosListener;

		/**
		 * Creates a new port label.
		 *
		 * @param port the port.
		 * @param text the (custom) label.
		 * @param color the port color.
		 */
		public Label(PortView<T> port, String text, Color color) {
			super();
			this.port = port;
			getStyleClass().add("dip-processor-port-label");
			this.setBackground(new Background(new BackgroundFill(
					color,
					new CornerRadii(3),
					Insets.EMPTY
			)));
			this.label = new javafx.scene.control.Label(text);
			this.getChildren().add(label);

			this.layout = port.editor.selectedPipeline().getLayoutStrategy();
			final double rot = layout.getLabelRotation();
			if (rot != 0) {
				this.setRotate(rot);
			}

			this.isInputPort = port.port instanceof InputPort;
			this.portPosListener = (c) -> updatePosition();
		}

		/**
		 * Updates the position of the port label.
		 */
		protected final void updatePosition() {
			layout.setPortLabelPosition(this, port, isInputPort);
		}

		/**
		 * Clips the position. Prevents bumping of the processor view.
		 *
		 * @param value the value.
		 * @return the clipped value.
		 */
		protected double clipPosition(double value) {
			if (value < 0) {
				value = 0;
			}
			return value;
		}

		/**
		 * Binds the port label (position) to its port.
		 */
		public void bind() {
			port.centerXProperty().addListener(portPosListener);
			port.centerYProperty().addListener(portPosListener);
			updatePosition();
		}

		/**
		 * Unbinds the port label (position) from its port.
		 */
		public void unbind() {
			port.centerXProperty().removeListener(portPosListener);
			port.centerYProperty().removeListener(portPosListener);
		}

	}

}
