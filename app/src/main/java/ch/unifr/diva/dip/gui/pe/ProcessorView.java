package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.Transmutable;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;

/**
 * Processor view base class.
 */
public abstract class ProcessorView extends BorderPane {

	public static final DataFormat PIPELINE_NODE = new DataFormat("pipeline-editor/node");
	protected static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
	protected static final PseudoClass UNAVAILABLE = PseudoClass.getPseudoClass("unavailable");

	protected final PipelineEditor editor;
	protected final ProcessorWrapper wrapper;
	protected final List<PortView> inputPorts;
	protected final List<PortView> outputPorts;

	protected final Label title;
	private ParameterView parameterView;
	protected double anchorX;
	protected double anchorY;
	protected Node minimalNodeX;
	protected Node minimalNodeY;

	// TO IMPL: (1) add info and input/output panes
	//				add title to info pane
	//			(2) setupDraggable(infoPane)
	//			(3) add inputs/outputs to panes
	//			(4) setupPortViewListener();
	//				also attach portListener to main/infoPane
	//			(5) call setupEditingListeners()
	//
	public ProcessorView(PipelineEditor editor, ProcessorWrapper wrapper) {
		this.editor = editor;
		this.wrapper = wrapper;

		if (!wrapper.isAvailable()) {
			this.pseudoClassStateChanged(UNAVAILABLE, true);
		}
		wrapper.availableProperty().addListener(availableListener);

		if (wrapper.processor() instanceof Transmutable) {
			final Transmutable t = (Transmutable) wrapper.processor();
			t.transmuteProperty().addListener(transmutableListener);
		}

		this.inputPorts = new ArrayList<>();
		for (Map.Entry<String, InputPort> e : wrapper.processor().inputs().entrySet()) {
			final InputPort input = e.getValue();
			final PortView view = new PortView(editor, e.getKey(), input);
			editor.editorPane().registerPort(input, view);
			inputPorts.add(view);
		}

		this.outputPorts = new ArrayList<>();
		for (Map.Entry<String, OutputPort> e : wrapper.processor().outputs().entrySet()) {
			final OutputPort output = e.getValue();
			final PortView view = new PortView(editor, e.getKey(), output);
			editor.editorPane().registerPort(output, view);
			outputPorts.add(view);
		}

		this.selectedProperty.addListener(selectedListener);

		this.layoutXProperty().bindBidirectional(wrapper.layoutXProperty());
		this.layoutYProperty().bindBidirectional(wrapper.layoutYProperty());

		this.getStyleClass().add("dip-processor");
		this.setBackground(Background.EMPTY);
		this.title = new Label(wrapper.processor().name());
	}

	protected void setupEditingListeners() {
		this.editingProperty.addListener(editingListener);
		this.editingProperty.set(wrapper.isEditing());
	}

	// listen to selected/deselected processors
	protected final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);

	final InvalidationListener selectedListener = (observable) -> {
		this.pseudoClassStateChanged(SELECTED, selectedProperty().get());
	};

	// listen to available/unavailable processors
	final InvalidationListener availableListener = (observable) -> {
		this.pseudoClassStateChanged(UNAVAILABLE, !wrapper().isAvailable());
	};

	// listen to transmuting processors
	protected final InvalidationListener transmutableListener = (observable) -> {
		editor().editorPane().updateProcessor(wrapper());
	};

	// listen to "editing" (opened/closed parameters) processors
	protected final BooleanProperty editingProperty = new SimpleBooleanProperty(false);

	final InvalidationListener editingListener = (observable) -> {
		if (editingProperty.get()) {
			if (!parameterView().getChildren().isEmpty()) {
				wrapper().editingProperty().set(true);
				showParameters(true);
			}
		} else {
			wrapper().editingProperty().set(false);
			if (this.parameterView != null) {
				showParameters(false);
			}
		}

		updatePorts();
	};

	protected ParameterView parameterView() {
		if (this.parameterView == null) {
			this.parameterView = newParameterView();
		}

		return this.parameterView;
	}

	// in case there seems to be a visual bug with wires missing their ports,
	// then this is the first place to check.
	protected void setupPortViewListener() {
		this.layoutXProperty().addListener(portListener);
		this.layoutYProperty().addListener(portListener);
		this.layoutBoundsProperty().addListener(portListener);

		// otherwise ports are wrong if editor is put in a scroll-/zoompane
		editor.getComponent().layoutBoundsProperty().addListener(portListener);
		editor.getComponent().needsLayoutProperty().addListener(portListener); // eh... why is this one also needed? :|
	}

	// port's have positional properties to bind to, since their own layout
	// porperties don't fire (ports aren't ever moving locally). Hence we
	// need to update those extra properties from the parent node (i.e the
	// processor wrapper) which actually moves.
	protected final InvalidationListener portListener = (observable) -> {
		updatePorts();
	};

	protected void updatePorts() {
		for (PortView v : this.inputPorts) {
			v.updateCenter();
		}
		for (PortView v : this.outputPorts) {
			v.updateCenter();
		}
	}

	public List<ConnectionView> wires() {
		final List<ConnectionView> wires = new ArrayList<>();
		addWires(wires, this.inputPorts);
		addWires(wires, this.outputPorts);
		return wires;
	}

	protected void addWires(List<ConnectionView> wires, List<PortView> ports) {
		for (PortView v : ports) {
			if (v.hasWires()) {
				wires.addAll(v.wires());
			}
		}
	}

	// safely remove wires and unregister ports
	public void unregister() {
		if (this.wrapper.processor() instanceof Transmutable) {
			final Transmutable t = (Transmutable) this.wrapper.processor();
			t.transmuteProperty().removeListener(transmutableListener);
		}

		for (PortView v : this.inputPorts) {
			final InputPort input = (InputPort) v.port;

			final List<ConnectionView> remove = new ArrayList<>();
			final List<ConnectionView> wires = v.wires();
			for (ConnectionView wire : wires) {
				if (wire.inputPort().equals(input)) {
					remove.add(wire);
				}
			}
			for (ConnectionView wire : remove) {
				editor.editorPane().removeWire(wire);
			}

			editor.editorPane().unregisterPort(input);
		}

		for (PortView v : this.outputPorts) {
			final OutputPort output = (OutputPort) v.port;

			final List<ConnectionView> remove = new ArrayList<>();
			final List<ConnectionView> wires = v.wires();
			for (ConnectionView wire : wires) {
				if (wire.outputPort().equals(output)) {
					remove.add(wire);
				}
			}
			for (ConnectionView wire : remove) {
				editor.editorPane().removeWire(wire);
			}

			editor.editorPane().unregisterPort(output);
		}
	}

	protected PipelineEditor editor() {
		return this.editor;
	}

	public ProcessorWrapper wrapper() {
		return this.wrapper;
	}

	protected void setupDraggable(Node node) {
		node.setOnMouseClicked(e -> onMouseClicked(e));

		node.setOnMouseEntered(e -> onMouseEntered(e));
		node.setOnMousePressed(e -> onMousePressed(e));
		node.setOnMouseDragged(e -> onMouseDragged(e));
		node.setOnMouseReleased(e -> onMouseReleased(e));
		node.setOnMouseExited(e -> onMouseExited(e));
	}

	protected void onMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			editingProperty.set(!editingProperty.get());
			if (editingProperty.get()) {
				editor.editorPane().bringToFront(wrapper);
			}
			e.consume();
		}
	}

	protected void onMouseEntered(MouseEvent e) {
		editor.setCursor(Cursor.OPEN_HAND);
	}

	protected void onMousePressed(MouseEvent e) {
		editor.editorPane().bringToFront(this);
		editor.setCursor(Cursor.CLOSED_HAND);

		anchorX = e.getScreenX();
		anchorY = e.getScreenY();

		if (editor.selection().contains(this)) {
			findMinimalNodes(editor.selection());
		} else {
			this.minimalNodeX = this;
			this.minimalNodeY = this;
		}
	}

	protected void onMouseDragged(MouseEvent e) {
		if (this.minimalNodeX == null || this.minimalNodeY == null) {
			return;
		}
		double dx = e.getScreenX() - anchorX;
		double dy = e.getScreenY() - anchorY;
		dx = limitMove(this.minimalNodeX.getLayoutX(), dx);
		dy = limitMove(this.minimalNodeY.getLayoutY(), dy);

		anchorX = e.getScreenX();
		anchorY = e.getScreenY();

		if (editor.selection().contains(this)) {
			for (Node node : editor.selection()) {
				moveNode(node, dx, dy);
			}
		} else {
			editor.selection().clear();
			moveNode(this, dx, dy);
		}
	}

	protected void findMinimalNodes(Set<Node> nodes) {
		this.minimalNodeX = this;
		this.minimalNodeY = this;
		for (Node node : nodes) {
			if (node.equals(this)) {
				continue;
			}
			if (node.getLayoutX() < this.minimalNodeX.getLayoutX()) {
				this.minimalNodeX = node;
			}
			if (node.getLayoutY() < this.minimalNodeY.getLayoutY()) {
				this.minimalNodeY = node;
			}
		}
	}

	protected double limitMove(double a, double v) {
		return (a + v < 0) ? 0 : v;
	}

	protected void moveNode(Node node, double dx, double dy) {
		node.layoutXProperty().set(node.getLayoutX() + dx);
		node.layoutYProperty().set(node.getLayoutY() + dy);
	}

	protected void onMouseReleased(MouseEvent e) {
		editor.setCursor(Cursor.OPEN_HAND);
	}

	protected void onMouseExited(MouseEvent e) {
		editor.setCursor(Cursor.DEFAULT);
	}

	// parameter view factory
	protected abstract ParameterView newParameterView();

	// add/remove parameterView from main/info pane
	protected abstract void showParameters(boolean show);

	public final BooleanProperty selectedProperty() {
		return selectedProperty;
	}

	/**
	 * Parameter view interface.
	 */
	public static interface ParameterView {

		public Parent node();

		public ObservableList<Node> getChildren();
	}

	/**
	 * Parameter view base class.
	 *
	 * @param <T> Type of root node.
	 */
	public static abstract class ParameterViewBase<T extends Parent> implements ParameterView {

		protected final T root;

		public ParameterViewBase(T root) {
			this.root = root;
		}

		@Override
		public Parent node() {
			return root;
		}
	}

	/**
	 * Simple grid/form implementation of a parameter view.
	 */
	public static class GridParameterView extends ParameterViewBase<FormGridPane> {

		public GridParameterView(Processor processor) {
			super(new FormGridPane());
			root.setPadding(new Insets(10, 0, 5, 0));

			// We can't do this directly in an ordinary/straightforward:
			//
			// for (Map.Entry<String, Parameter> e : processor.parameters().entrySet()) {
			//    // ...
			//    final Parameter.View v = p.view(); // CME here!
			// }
			//
			// loop, since parameters most likely is a LinkedHashMap, and will
			// throw a java.util.ConcurrentModificationException in case the set
			// of parameters changes (e.g. on transmute/init).
			//
			// Thus... we first get params/keys without further action...
			final int n = processor.parameters().size();
			final Parameter[] params = new Parameter[n];
			final String[] keys = new String[n];

			int j = 0;
			for (Map.Entry<String, Parameter> e : processor.parameters().entrySet()) {
				params[j] = e.getValue();
				keys[j] = e.getKey();
				j++;
			}

			// ...and do our thing now.
			for (int i = 0; i < n; i++) {
				final Parameter p = params[i];
				final String key = keys[i];
				final Parameter.View v = p.view();
				final Label label;

				if (p.isPersistent()) {
					final PersistentParameter pp = (PersistentParameter) p;
					label = new Label((pp.label().isEmpty()) ? "" : pp.label() + ":");
				} else {
					label = new Label("");
				}

				label.getStyleClass().add("dip-small");
				root.addRow(label, v.node());
			}
		}

		@Override
		public ObservableList<Node> getChildren() {
			return root.getChildren();
		}
	}

}
