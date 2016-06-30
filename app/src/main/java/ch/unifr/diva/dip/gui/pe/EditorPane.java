package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import static ch.unifr.diva.dip.gui.pe.ProcessorsWidget.ProcessorListCell.OSGI_SERVICE_PROCESSOR;
import ch.unifr.diva.dip.utils.FxUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline editor pane.
 */
public class EditorPane {

	private static final Logger log = LoggerFactory.getLogger(EditorPane.class);

	private final PipelineEditor editor;
	private final Pane pane = new Pane();
	private final Circle dot = new Circle(1, Color.TRANSPARENT);
	private ConnectionView selectedWire = null;
	private OutputPort selectedPort = null;
	private final Map<ProcessorWrapper, ProcessorView> processorViews = new HashMap<>();
	private final Map<InputPort, ConnectionView> connections = new HashMap<>();
	private final Map<Port, PortView> ports = new HashMap<>();

	public EditorPane(PipelineEditor editor) {
		this.editor = editor;

		this.dot.setMouseTransparent(true); // invisible helper node

		this.pane.getStyleClass().add("dip-pipeline-editor");
		FxUtils.expandInRegion(this.pane);

		setupDraggable();
	}

	public Region getComponent() {
		return pane;
	}

	public Collection<ProcessorView> processorViews() {
		return this.processorViews.values();
	}

	public Map<ProcessorWrapper, ProcessorView> processorViewMap() {
		return this.processorViews;
	}

	public Point2D sceneToPane(double x, double y) {
		return pane.sceneToLocal(x, y);
	}

	public Point2D sceneToPane(Point2D p) {
		return pane.sceneToLocal(p);
	}

	public Point2D sceneToPane(MouseEvent e) {
		return pane.sceneToLocal(e.getSceneX(), e.getSceneY());
	}

	public Point2D sceneToPane(DragEvent e) {
		return pane.sceneToLocal(e.getSceneX(), e.getSceneY());
	}

	public void bringToFront(ProcessorWrapper wrapper) {
		final ProcessorView view = processorViews.get(wrapper);
		bringToFront(view);
	}

	public void bringToFront(ProcessorView view) {
		// insert dot to keep pane-bounds intact and prevent viewport-jitter
		pane.getChildren().add(dot);
		final Bounds bounds = view.getBoundsInParent();
		dot.relocate(bounds.getMaxX(), bounds.getMaxY());

		pane.getChildren().remove(view);
		pane.getChildren().add(view);
		for (ConnectionView wire : view.wires()) {
			pane.getChildren().remove(wire.node());
			pane.getChildren().add(wire.node());
		}

		// remove dot
		pane.getChildren().remove(dot);
	}

	public void registerPort(Port port, PortView view) {
		ports.put(port, view);
	}

	public void clear() {
		pane().getChildren().clear();
		processorViews.clear();
		connections.clear();
		ports.clear();
	}

	private Pipeline pipeline() {
		return editor.selectedPipelineProperty().get();
	}

	public final Pane pane() {
		return pane;
	}

	public ConnectionView wire() {
		return selectedWire;
	}

	public boolean hasWire() {
		return (this.selectedWire != null);
	}

	public void setWire(ConnectionView wire) {
		this.selectedWire = wire;
	}

	public void moveWire(DragEvent e) {
		final Point2D end = this.sceneToPane(e);
		wire().setEnd(end);
	}

	public void moveWire(MouseEvent e) {
		final Point2D end = this.sceneToPane(e);
		wire().setEnd(end);
	}

	public boolean hasSelectedPort() {
		return (this.selectedPort != null);
	}

	public void setSelectedPort(OutputPort output) {
		this.selectedPort = output;
	}

	public OutputPort selectedPort() {
		return this.selectedPort;
	}

	public void setupConnections() {
		for (Map.Entry<ProcessorWrapper, ProcessorView> e : processorViews.entrySet()) {
			final ProcessorWrapper wrapper = e.getKey();
			setupConnections(wrapper);
		}
	}

	public void setupConnections(ProcessorWrapper wrapper) {
		for (Map.Entry<String, InputPort> port : wrapper.processor().inputs().entrySet()) {
			final InputPort input = port.getValue();
			if (input.isConnected()) {
				newConnection(input);
			}
		}
	}

	public ConnectionView getConnectionView(InputPort input) {
		return connections.get(input);
	}

	public void removeConnections(ProcessorWrapper wrapper) {
		final Set<InputPort> trash = new HashSet<>();

		for (InputPort input : wrapper.processor().inputs().values()) {
			trash.add(input);
		}

		for (Set<InputPort> inputs : wrapper.processor().dependentInputs().values()) {
			for (InputPort input : inputs) {
				trash.add(input);
			}
		}

		for (InputPort input : trash) {
			removeConnection(input);
		}
	}

	// removes connection, but not the wire
	private void disconnect(InputPort input) {
		input.disconnect();
	}

	public void removeConnection(InputPort input) {
		if (!input.isConnected()) {
			return;
		}

		final ConnectionView wire = connections.remove(input);
		removeWire(wire);
		input.disconnect();
	}

	// if already connected (e.g. for loading)
	private void newConnection(InputPort input) {
		final ConnectionView wire = newWire(input);
		connections.put(input, wire);
		bindWire(input, wire, input.connection(), false);
	}

	// connection with existing wire (e.g. while dragging)
	public void newConnection(OutputPort output, InputPort input, ConnectionView wire) {
//		input.connectTo(output);
		connections.put(input, wire);
		bindWire(input, wire, output, true);
	}

	private void bindWire(InputPort input, ConnectionView wire, OutputPort output, boolean doConnect) {
		final PortView<InputPort> ip = this.ports.get(input);
		final PortView<OutputPort> op = this.ports.get(output);

		// unregister wire from previous ports
		if (wire.inputPort() != null) {
			final PortView pv = this.ports.get(wire.inputPort());
			if (pv != null) {
				pv.removeWire(wire);
			}
		}
		if (wire.outputPort() != null) {
			final PortView pv = this.ports.get(wire.outputPort());
			if (pv != null) {
				pv.removeWire(wire);
			}
		}

		if (!doConnect && !input.isConnected()) {
			log.warn(
					"can't bind wire {} of unconnected input={}, port={}",
					wire, input, ip
			);
			return;
		}
		if (ip == null || op == null) {
			log.warn(
					"could not bind wire {} on input={} to output={}",
					wire, ip, op
			);
			return;
		}

		wire.bind(op, ip);
		// remember connected output port for hotswap removal of all wires
		wire.setOutput(op);
		// register wire on ports (such that we can bring them visually to front
		// together with its selected processor later on in the editor pane)
		op.addWire(wire);
		ip.addWire(wire);

		// this might be a bit awkward (why not just connect input and output
		// before calling bindWire()?), but this order is crucial to make the
		// transmutable feature work. Otherwise we create a newConnection() which
		// causes transmute to fire, so we updateProcessor() a second time, bind
		// to new wire(s) and back to the first call to newConnection that hasnt
		// finished yet, we finally bind the now deprecated wire, thereby overwriting
		// the new one... UGH.
		//
		// Transmute synchronization problem, illustrated:
		//
		//	newConnection {									// manually creating a connection
		//		connectTo -> transmute -> newConnection {	// 2nd call to newConnection!
		//			bindWire								// binds new wire, but...
		//		}
		//		...
		//		bindWire	// old wire overrides the new one!
		//  }
		//
		// I.e. connecting to a port causes a transmute event, thus there is a
		// second call to newConnection (updateProcessor), the new wire binds
		// immmediately, and back to the first call to newConnection we finally
		// bind the now deprecated wire, thereby overriding the new one...
		if (doConnect) {
			input.connectTo(output);
		}
	}

	// wires aren't connections yet (only connected to input so far)
	public ConnectionView newWire(InputPort input) {
		final ConnectionView.Type type = this.editor.handler.settings.pipelineEditor.getDefaultConnectionType();
		final ConnectionView view = type.newConnection(input);

		view.node().setMouseTransparent(true);
		pane().getChildren().add(view.node());
		return view;
	}

	public void removeWire(InputPort input) {
		final ConnectionView wire = connections.get(input);
		removeWire(wire);
	}

	public void removeWire(ConnectionView wire) {
		// clean up first, or end up with reappearing dead wires
		final PortView input = this.ports.get(wire.inputPort());
		if (input != null) {
			input.wires().remove(wire);
		}
		final PortView output = this.ports.get(wire.outputPort());
		if (output != null) {
			output.wires().remove(wire);
		}

		pane().getChildren().remove(wire.node());
	}

	public void addProcessor(ProcessorWrapper wrapper) {
		final ProcessorView view = this.editor.selectedPipeline().getLayoutStrategy().newProcessorView(editor, wrapper);
		processorViews.put(wrapper, view);
		pane().getChildren().add(view);
	}

	public void removeProcessor(ProcessorWrapper wrapper) {
		final ProcessorView view = processorViews.get(wrapper);
		if (view != null) {
			pane().getChildren().remove(view);
			removeConnections(wrapper);
			unregisterPorts(wrapper);
			processorViews.remove(wrapper);
		} else {
			log.warn("Couldn't remove processor: processor view not found");
		}
	}

	public void unregisterPorts(ProcessorWrapper wrapper) {
		for (InputPort port : wrapper.processor().inputs().values()) {
			ports.remove(port);
		}
		for (OutputPort port : wrapper.processor().outputs().values()) {
			ports.remove(port);
		}
	}

	public void unregisterPort(Port port) {
		ports.remove(port);
	}

	public void deprecateProcessor(ProcessorWrapper wrapper) {
		// no-op; ProcessorView is bound to isAvailableProperty
	}

	private boolean replaceNode(Node oldNode, Node newNode) {
		final int id = pane().getChildren().indexOf(oldNode);
		if (id < 0) {
			return false;
		}
		pane().getChildren().set(id, newNode);
		return true;
	}

	public void updateProcessor(ProcessorWrapper wrapper) {
		final ProcessorView oldView = processorViews.get(wrapper);
		if (oldView == null) {
			log.warn("Couldn't update processor: processor view not found: {}", wrapper);
			return;
		}

		// remove wires and unregister old ports
		oldView.unregister();

		// replace with new view (conn. already hooked up, just need wires)
		final ProcessorView newView = this.editor.selectedPipeline().getLayoutStrategy().newProcessorView(editor, wrapper);
		processorViews.put(wrapper, newView);

		if (!replaceNode(oldView, newView)) {
			log.warn(
					"Couldn't update processor: failed to replace {} by {}",
					oldView,
					newView
			);
		}

		for (InputPort input : wrapper.processor().inputs().values()) {
			if (input.isConnected()) {
				newConnection(input);
			}
		}

		for (Set<InputPort> inputs : wrapper.processor().dependentInputs().values()) {
			for (InputPort input : inputs) {
				newConnection(input);
			}
		}
	}

	private void setupDraggable() {
		pane().setOnDragOver(e -> onDragOver(e));
//			pane().setOnDragEntered(e -> onDragEntered(e));
//			pane().setOnDragExited(e -> onDragExited(e));
		pane().setOnDragDropped(e -> onDragDropped(e));
	}

	private void onDragOver(DragEvent e) {
		final Dragboard db = e.getDragboard();

		if (db.hasContent(OSGI_SERVICE_PROCESSOR)) {
			e.acceptTransferModes(TransferMode.COPY);
		} else if (editor.editorPane().hasWire()
				&& db.hasContent(editor.editorPane().wire().dataType().dataFormat())) {
			e.acceptTransferModes(TransferMode.LINK);
			editor.editorPane().moveWire(e);
		}

		e.consume();
	}

//		private void onDragEntered(DragEvent e) {
//			e.consume();
//		}
//
//		private void onDragExited(DragEvent e) {
//			e.consume();
//		}
	private void onDragDropped(DragEvent e) {
		final Dragboard db = e.getDragboard();
		if (db.hasContent(OSGI_SERVICE_PROCESSOR)) {
			final String pid = (String) db.getContent(OSGI_SERVICE_PROCESSOR);
			if (editor.selectedPipeline() != null) {
				final Point2D p = editor.editorPane().sceneToPane(e);
				editor.selectedPipeline().addProcessor(pid, p.getX(), p.getY());
			}
		}
//			else if (editor.pane().hasWire() &&
//					db.hasContent(editor.pane().wire().dataType().dataFormat())) {
//				// nothing to do here
//			}
		e.setDropCompleted(true);
		e.consume();
	}
}
