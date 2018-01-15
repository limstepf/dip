package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import static ch.unifr.diva.dip.gui.pe.ProcessorsWidget.ProcessorListCell.OSGI_SERVICE_PROCESSOR;
import ch.unifr.diva.dip.osgi.OSGiServiceReference;
import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.core.model.PrototypePipeline;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
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
	private OutputPort<?> selectedPort = null;
	private final Map<PrototypeProcessor, ProcessorView> processorViews = new HashMap<>();
	private final Map<InputPort<?>, ConnectionView> connections = new HashMap<>();
	private final Map<Port<?>, PortView<? extends Port<?>>> ports = new HashMap<>();
	private final InvalidationListener repaintListener;
	private final EventHandler<MouseEvent> mousePositionHandler;
	private double mouseX;
	private double mouseY;

	/**
	 * Creates a new editor pane.
	 *
	 * @param editor the (parent) pipeline editor.
	 */
	public EditorPane(PipelineEditor editor) {
		this.editor = editor;

		this.dot.setMouseTransparent(true); // invisible helper node

		this.pane.getStyleClass().add("dip-pipeline-editor");
		FxUtils.expandInRegion(this.pane);

		setupDraggable();

		this.mousePositionHandler = (e) -> {
			mouseX = e.getX();
			mouseY = e.getY();
		};
		pane.addEventFilter(MouseEvent.MOUSE_MOVED, mousePositionHandler);

		this.repaintListener = (e) -> editor.repaint();
		pane.getChildren().addListener(this.repaintListener);
	}

	/**
	 * Returns the component/node of the editor pane.
	 *
	 * @return the component/node of the editor pane.
	 */
	public Region getComponent() {
		return pane;
	}

	/**
	 * Returns a collection of all processor views.
	 *
	 * @return a collection of all processor views.
	 */
	public Collection<ProcessorView> processorViews() {
		return this.processorViews.values();
	}

	/**
	 * Returns a mapping of processors to their corresponding view.
	 *
	 * @return a mapping of processors to their corresponding view.
	 */
	public Map<PrototypeProcessor, ProcessorView> processorViewMap() {
		return this.processorViews;
	}

	/**
	 * Converts scene coordinates to local editor pane coordinates.
	 *
	 * @param x the X scene coordinate.
	 * @param y the Y scene coordinate.
	 * @return the coordinates in local editor pane coordinates.
	 */
	public Point2D sceneToPane(double x, double y) {
		return pane.sceneToLocal(x, y);
	}

	/**
	 * Converts scene coordinates to local editor pane coordinates.
	 *
	 * @param p a point using scene coordinates.
	 * @return the coordinates in local editor pane coordinates.
	 */
	public Point2D sceneToPane(Point2D p) {
		return pane.sceneToLocal(p);
	}

	/**
	 * Converts scene coordinates to local editor pane coordinates.
	 *
	 * @param e a mouse event using scene coordinates.
	 * @return the coordinates in local editor pane coordinates.
	 */
	public Point2D sceneToPane(MouseEvent e) {
		return pane.sceneToLocal(e.getSceneX(), e.getSceneY());
	}

	/**
	 * Converts scene coordinates to local editor pane coordinates.
	 *
	 * @param e a drag event using scene coordinates.
	 * @return the coordinates in local editor pane coordinates.
	 */
	public Point2D sceneToPane(DragEvent e) {
		return pane.sceneToLocal(e.getSceneX(), e.getSceneY());
	}

	/**
	 * Brings the view of the processor to front (z-index).
	 *
	 * @param wrapper the processor whos view to bring to the front.
	 */
	public void bringToFront(PrototypeProcessor wrapper) {
		final ProcessorView view = processorViews.get(wrapper);
		bringToFront(view);
	}

	/**
	 * Brings the processor view to the front (z-index).
	 *
	 * @param view the processor view.
	 */
	public void bringToFront(ProcessorView view) {
		// insert dot to keep pane-bounds intact and prevent viewport-jitter
		pane.getChildren().add(dot);
		final Bounds bounds = view.getBoundsInParent();
		dot.relocate(bounds.getMaxX(), bounds.getMaxY());

		final boolean isLabelVisible = view.isPortLabelsVisible();
		if (isLabelVisible) {
			view.removePortLabels();
		}

		pane.getChildren().remove(view);
		pane.getChildren().add(view);
		for (ConnectionView wire : view.wires()) {
			pane.getChildren().remove(wire.node());
			pane.getChildren().add(wire.node());
		}

		if (isLabelVisible) {
			view.addPortLabels();
		}

		// remove dot
		pane.getChildren().remove(dot);
	}

	/**
	 * Registers a port to the port map.
	 *
	 * @param port the port to register.
	 * @param view the view and parent of the port.
	 */
	public void registerPort(Port<?> port, PortView<? extends Port<?>> view) {
		ports.put(port, view);
	}

	/**
	 * Clears the editor pane.
	 */
	public void clear() {
		for (ProcessorView view : processorViews.values()) {
			view.stop();
		}
		pane().getChildren().clear();
		processorViews.clear();
		connections.clear();
		ports.clear();
	}

	/**
	 * Returns the selected pipeline.
	 *
	 * @return the selected pipeline.
	 */
	private PrototypePipeline pipeline() {
		return editor.selectedPipelineProperty().get();
	}

	/**
	 * Returns the (actual) pane of the editor pane.
	 *
	 * @return the pane of the editor pane.
	 */
	public final Pane pane() {
		return pane;
	}

	/**
	 * Returns the currently active wire in the editor pane. An active wire is a
	 * wire currently used/dragged by the user, but not fully connected yet.
	 * There can be only a single such active wire in the editor pane.
	 *
	 * @return the currently active wire, or {@code null} if there isn't any.
	 */
	public ConnectionView wire() {
		return selectedWire;
	}

	/**
	 * Checks whether there is an active wire, or not.
	 *
	 * @return {@code true} if there is currently an active wire being used,
	 * {@code false} otherwise.
	 */
	public boolean hasWire() {
		return (this.selectedWire != null);
	}

	/**
	 * Sets the currently active wire.
	 *
	 * @param wire the currently active wire.
	 */
	public void setWire(ConnectionView wire) {
		this.selectedWire = wire;
	}

	/**
	 * Moves the currently active wire by a drag event.
	 *
	 * @param e the drag event.
	 */
	public void moveWire(DragEvent e) {
		final Point2D end = this.sceneToPane(e);
		wire().setEnd(end);
	}

	/**
	 * Moves the currently active wire by a mouse event.
	 *
	 * @param e the mouse event.
	 */
	public void moveWire(MouseEvent e) {
		final Point2D end = this.sceneToPane(e);
		wire().setEnd(end);
	}

	/**
	 * Check whether we have an active/selected port.
	 *
	 * @return {@code true} if there is an active/selected port, {@code false}
	 * otherwise.
	 */
	public boolean hasSelectedPort() {
		return (this.selectedPort != null);
	}

	/**
	 * Sets the active/selected port.
	 *
	 * @param output the active/selected output port connected to the active
	 * wire.
	 */
	public void setSelectedPort(OutputPort<?> output) {
		this.selectedPort = output;
		editor.repaint();
	}

	/**
	 * Return the active/selected port. The active (or selected) port is the
	 * port connected to the currently active wire.
	 *
	 * @return the active/selected port, or {@code null} if there isn't any.
	 */
	public OutputPort<?> selectedPort() {
		return this.selectedPort;
	}

	/**
	 * Initializes the connections of all processors.
	 */
	public void setupConnections() {
		for (Map.Entry<PrototypeProcessor, ProcessorView> e : processorViews.entrySet()) {
			final PrototypeProcessor wrapper = e.getKey();
			setupConnections(wrapper);
		}
	}

	/**
	 * Initializes the connections of the given processor.
	 *
	 * @param wrapper the processor to connect.
	 */
	public void setupConnections(PrototypeProcessor wrapper) {
		if (!wrapper.isAvailable()) {
			return;
		}
		for (Map.Entry<String, InputPort<?>> port : wrapper.serviceObject().inputs().entrySet()) {
			final InputPort<?> input = port.getValue();
			if (input.isConnected()) {
				newConnection(input);
			}
		}
	}

	/**
	 * Returns the wire attached to the given input port.
	 *
	 * @param input the input port.
	 * @return the wire attached to the given input port.
	 */
	public ConnectionView getConnectionView(InputPort<?> input) {
		return connections.get(input);
	}

	/**
	 * Removes all connections from the given processor.
	 *
	 * @param wrapper the processor to unconnect.
	 */
	public void removeConnections(PrototypeProcessor wrapper) {
		if (!wrapper.isAvailable()) {
			return;
		}

		final Set<InputPort<?>> trash = new HashSet<>();

		for (InputPort<?> input : wrapper.serviceObject().inputs().values()) {
			trash.add(input);
		}

		for (Set<InputPort<?>> inputs : wrapper.serviceObject().dependentInputs().values()) {
			for (InputPort<?> input : inputs) {
				trash.add(input);
			}
		}

		for (InputPort<?> input : trash) {
			removeConnection(input);
		}
	}

	/**
	 * Removes a wire/connection.
	 *
	 * @param input the input port owning/connected to the wire.
	 */
	public void removeConnection(InputPort<?> input) {
		if (!input.isConnected()) {
			return;
		}

		final ConnectionView wire = connections.remove(input);
		removeWire(wire);
		input.disconnect();
	}

	// if already connected (e.g. for loading)
	private void newConnection(InputPort<?> input) {
		final ConnectionView wire = newWire(input);
		connections.put(input, wire);
		bindWire(input, wire, input.connection(), false);
	}

	/**
	 * Make a new connection using the selected/active wire.
	 *
	 * @param output the output port of the connection.
	 * @param input the input port of the connection.
	 * @param wire the wire/connection view.
	 */
	public void newConnection(OutputPort<?> output, InputPort<?> input, ConnectionView wire) {
		connections.put(input, wire);
		bindWire(input, wire, output, true);
	}

	private void bindWire(InputPort<?> input, ConnectionView wire, OutputPort<?> output, boolean doConnect) {
		@SuppressWarnings("unchecked")
		final PortView<InputPort<?>> ip = (PortView<InputPort<?>>) this.ports.get(input);
		@SuppressWarnings("unchecked")
		final PortView<OutputPort<?>> op = (PortView<OutputPort<?>>) this.ports.get(output);

		// unregister wire from previous ports
		if (wire.inputPort() != null) {
			final PortView<?> pv = this.ports.get(wire.inputPort());
			if (pv != null) {
				pv.removeWire(wire);
			}
		}
		if (wire.outputPort() != null) {
			final PortView<?> pv = this.ports.get(wire.outputPort());
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
		// repaint feature work. Otherwise we create a newConnection() which
		// causes repaint to fire, so we updateProcessor() a second time, bind
		// to new wire(s) and back to the first call to newConnection that hasnt
		// finished yet, we finally bind the now deprecated wire, thereby overwriting
		// the new one... UGH.
		//
		// Repaint synchronization problem, illustrated:
		//
		//	newConnection {									// manually creating a connection
		//		connectTo -> repaint -> newConnection {	    // 2nd call to newConnection!
		//			bindWire								// binds new wire, but...
		//		}
		//		...
		//		bindWire	// old wire overrides the new one!
		//  }
		//
		// I.e. connecting to a port causes a repaint event, thus there is a
		// second call to newConnection (updateProcessor), the new wire binds
		// immmediately, and back to the first call to newConnection we finally
		// bind the now deprecated wire, thereby overriding the new one...
		if (doConnect) {
			input.connectTo(output);
		}
	}

	/**
	 * Creates a new wire. "Wires" aren't connections yet, and are only owned
	 * by/connected to an input port.
	 *
	 * @param input the input port owning the wire.
	 * @return a new wire.
	 */
	public ConnectionView newWire(InputPort<?> input) {
		final ConnectionView.Type type = this.editor.handler.settings.pipelineEditor.getDefaultConnectionType();
		final ConnectionView view = type.newConnection(input);

		view.node().setMouseTransparent(true);
		pane().getChildren().add(view.node());
		return view;
	}

	/**
	 * Removes the wire owned by an input port.
	 *
	 * @param input the input port owning the wire.
	 */
	public void removeWire(InputPort<?> input) {
		final ConnectionView wire = connections.get(input);
		removeWire(wire);
	}

	/**
	 * Removes a wire.
	 *
	 * @param wire the wire to be removed.
	 */
	public void removeWire(ConnectionView wire) {
		// clean up first, or end up with reappearing dead wires
		final PortView<?> input = this.ports.get(wire.inputPort());
		if (input != null) {
			input.wires().remove(wire);
		}
		final PortView<?> output = this.ports.get(wire.outputPort());
		if (output != null) {
			output.wires().remove(wire);
		}

		pane().getChildren().remove(wire.node());
	}

	/**
	 * Adds a processor to the editor pane.
	 *
	 * @param wrapper the processor.
	 */
	public void addProcessor(PrototypeProcessor wrapper) {
		final ProcessorView view = this.editor.selectedPipeline().getLayoutStrategy().newProcessorView(editor, wrapper);
		processorViews.put(wrapper, view);
		pane().getChildren().add(view);
		view.init();
	}

	/**
	 * Removes the processors of the given processor views.
	 *
	 * @param views a set of processor views.
	 */
	public void removeProcessors(Set<ProcessorView> views) {
		for (ProcessorView view : views) {
			editor.selectedPipeline().removeProcessor(view.wrapper(), false);
		}
	}

	/**
	 * Removes a processor from the editor pane.
	 *
	 * @param wrapper the processor.
	 */
	public void removeProcessor(PrototypeProcessor wrapper) {
		final ProcessorView view = processorViews.get(wrapper);
		if (view != null) {
			view.stop();
			pane().getChildren().remove(view);
			removeConnections(wrapper);
			unregisterPorts(wrapper);
			processorViews.remove(wrapper);
		} else {
			log.warn("Couldn't remove processor: processor view not found");
		}
	}

	/**
	 * Unregisters all ports of a processor.
	 *
	 * @param wrapper the processor.
	 */
	public void unregisterPorts(PrototypeProcessor wrapper) {
		if (!wrapper.isAvailable()) {
			return;
		}

		for (InputPort<?> port : wrapper.serviceObject().inputs().values()) {
			ports.remove(port);
		}
		for (OutputPort<?> port : wrapper.serviceObject().outputs().values()) {
			ports.remove(port);
		}
	}

	/**
	 * Unregisters a port.
	 *
	 * @param port the port.
	 */
	public void unregisterPort(Port<?> port) {
		ports.remove(port);
	}

	/**
	 * Deprecates/disables a processor. This happens when the underlying
	 * processor service disappears.
	 *
	 * @param wrapper the processor.
	 */
	public void deprecateProcessor(PrototypeProcessor wrapper) {
		// no-op; ProcessorView is bound to isAvailableProperty
	}

	/**
	 * Pastes copies of the given processor views.
	 *
	 * @param views the processor views to be copied.
	 */
	protected void pasteProcessors(List<ProcessorView> views) {
		if (views == null || views.isEmpty()) {
			return;
		}
		/*
		 * TODO: also copy/setup within-group connections, that would be nice.
		 */
		double offsetX = Double.MAX_VALUE;
		double offsetY = Double.MAX_VALUE;
		for (ProcessorView view : views) {
			if (view.layoutXProperty().get() < offsetX) {
				offsetX = view.layoutXProperty().get();
			}
			if (view.layoutYProperty().get() < offsetY) {
				offsetY = view.layoutYProperty().get();
			}
		}
		for (ProcessorView view : views) {
			final PrototypeProcessor wrapper = editor.selectedPipeline().addProcessor(
					view.wrapper().pid(),
					view.wrapper().version().toString(),
					clipPosition(
							mouseX
							- offsetX
							+ view.layoutXProperty().get()
							- ProcessorView.horizontalPadding
					),
					clipPosition(
							mouseY
							- offsetY
							+ view.layoutYProperty().get()
							- ProcessorView.verticalPadding
					),
					PersistentParameter.getPreset(view.wrapper().serviceObject().parameters()),
					view.editingProperty.get()
			);
		}
	}

	private boolean replaceNode(Node oldNode, Node newNode) {
		final int id = pane().getChildren().indexOf(oldNode);
		if (id < 0) {
			return false;
		}
		pane().getChildren().set(id, newNode);
		return true;
	}

	/**
	 * Updates a processor (and it's view).
	 *
	 * @param wrapper the processor to update.
	 */
	public void updateProcessor(PrototypeProcessor wrapper) {
		final ProcessorView oldView = processorViews.get(wrapper);
		if (oldView == null) {
			log.warn("Couldn't update processor: processor view not found: {}", wrapper);
			return;
		}

		// insert dot to keep pane-bounds intact and prevent viewport-jitter
		pane.getChildren().add(dot);
		final Bounds bounds = oldView.getBoundsInParent();
		dot.relocate(bounds.getMaxX(), bounds.getMaxY());

		// remove wires and unregister old ports
		oldView.stop();
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

		newView.init();

		for (InputPort<?> input : wrapper.serviceObject().inputs().values()) {
			if (input.isConnected()) {
				newConnection(input);
			}
		}

		for (Set<InputPort<?>> inputs : wrapper.serviceObject().dependentInputs().values()) {
			for (InputPort<?> input : inputs) {
				newConnection(input);
			}
		}

		// remove dot
		pane.getChildren().remove(dot);
	}

	/**
	 * Updates the view of all processors. Called after the layout strategy has
	 * changed.
	 */
	public void updateAllProcessors() {
		final Set<PrototypeProcessor> processors = this.processorViews.keySet();
		for (PrototypeProcessor p : processors) {
			updateProcessor(p);
		}
		if (editor.handler.settings.pipelineEditor.autoRearrangeOnChangedLayout) {
			editor.editorPane().rearrangeProcessors();
		}
	}

	/**
	 * Rearrange all processors according to the pipeline's layout strategy.
	 */
	public void rearrangeProcessors() {
		pane().applyCss();
		pane().layout();
		this.editor.selectedPipeline().getLayoutStrategy().arrange(
				this.editor.selectedPipeline(),
				this.processorViewMap()
		);
		editor.repaint();
	}

	private void setupDraggable() {
		pane().setOnDragOver(e -> onDragOver(e));
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

	private void onDragDropped(DragEvent e) {
		final Dragboard db = e.getDragboard();
		if (db.hasContent(OSGI_SERVICE_PROCESSOR)) {
			final OSGiServiceReference ref = (OSGiServiceReference) db.getContent(OSGI_SERVICE_PROCESSOR);
			if (editor.selectedPipeline() != null) {
				final Point2D p = editor.editorPane().sceneToPane(e);
				editor.selectedPipeline().addProcessor(
						ref.pid,
						ref.version,
						clipPosition(p.getX() - ProcessorView.horizontalPadding),
						clipPosition(p.getY() - ProcessorView.verticalPadding)
				);
			}
		}

		e.setDropCompleted(true);
		e.consume();
	}

	private double clipPosition(double value) {
		if (value < 0) {
			return 0;
		}
		return value;
	}

}
