package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.Transmutable;
import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.glyphs.MaterialDesignIcons;
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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processor view base class.
 */
public abstract class ProcessorView extends BorderPane {

	protected static final Logger log = LoggerFactory.getLogger(ProcessorView.class);

	/**
	 * Data format of a node in the pane of the pipeline editor. A node is the
	 * view of a processor in the pipeline.
	 */
	public static final DataFormat PIPELINE_NODE = new DataFormat("pipeline-editor/node");
	protected static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
	protected static final PseudoClass UNAVAILABLE = PseudoClass.getPseudoClass("unavailable");

	protected static final NamedGlyph IMPORT_GLYPH = MaterialDesignIcons.IMPORT;
	protected static final NamedGlyph EXPORT_GLYPH = MaterialDesignIcons.EXPORT;

	protected final PipelineEditor editor;
	protected final ProcessorWrapper wrapper;
	protected final List<PortView> inputPorts;
	protected final List<PortView> outputPorts;

	protected final ProcessorHead head;
	protected ParameterView parameterView;
	protected ClosedParameterView closedParameterView;
	protected double anchorX;
	protected double anchorY;
	protected Node minimalNodeX;
	protected Node minimalNodeY;

	/**
	 * Creates the base of a processor view.
	 *
	 * <p>
	 * Implementing sub-classes need to:
	 *
	 * <ol>
	 * <li>add info and input/output panes (add title to info pane)</li>
	 * <li>setupDraggable(infoPane)</li>
	 * <li>add inputs/outputs to panes</li>
	 * <li>setupPortViewListener(); also attach portListener to
	 * main/infoPane</li>
	 * <li>call setupEditingListeners()</li>
	 * </ol>
	 *
	 * @param editor the (parent) pipeline editor.
	 * @param wrapper the processor wrapper to create the view for.
	 */
	public ProcessorView(PipelineEditor editor, ProcessorWrapper wrapper) {
		this.editor = editor;
		this.wrapper = wrapper;

		if (!wrapper.isAvailable()) {
			this.pseudoClassStateChanged(UNAVAILABLE, true);
		}
		wrapper.availableProperty().addListener(availableListener);

		this.inputPorts = new ArrayList<>();
		this.outputPorts = new ArrayList<>();
		if (wrapper.isAvailable()) {
			for (Map.Entry<String, InputPort> e : wrapper.processor().inputs().entrySet()) {
				final InputPort input = e.getValue();
				if (input != null) {
					final PortView view = new PortView(editor, e.getKey(), input);
					editor.editorPane().registerPort(input, view);
					inputPorts.add(view);
				} else {
					log.warn("invalid input port: {}", e.getKey());
				}
			}

			for (Map.Entry<String, OutputPort> e : wrapper.processor().outputs().entrySet()) {
				final OutputPort output = e.getValue();
				if (output != null) {
					final PortView view = new PortView(editor, e.getKey(), output);
					editor.editorPane().registerPort(output, view);
					outputPorts.add(view);
				} else {
					log.warn("invalid output port: {}", e.getKey());
				}
			}
		}

		this.selectedProperty.addListener(selectedListener);

		this.layoutXProperty().bindBidirectional(wrapper.layoutXProperty());
		this.layoutYProperty().bindBidirectional(wrapper.layoutYProperty());

		this.getStyleClass().add("dip-processor");
		this.setBackground(Background.EMPTY);

		this.head = new ProcessorHead(editor.applicationHandler(), wrapper);
	}

	/**
	 * Processor head. Shows the title and generic processor functionality (e.g.
	 * im-/export of presets).
	 */
	public static class ProcessorHead implements Localizable {

		private final ApplicationHandler handler;
		private final Window owner;
		private final BorderPane pane;
		private final HBox title;
		private final HBox menu;

		/**
		 * Creates a new processor head.
		 *
		 * @param handler the application handler.
		 * @param wrapper the processor wrapper.
		 */
		public ProcessorHead(ApplicationHandler handler, ProcessorWrapper wrapper) {
			this.handler = handler;
			this.owner = handler.uiStrategy.getStage();
			this.pane = new BorderPane();
			pane.setMaxWidth(Double.MAX_VALUE);

			final Glyph glyph = UIStrategyGUI.Glyphs.newGlyph(wrapper.glyph(), Glyph.Size.NORMAL);
			final Label label = new Label(
					wrapper.isAvailable()
							? wrapper.processor().name()
							: wrapper.pid()
			);
			final Tooltip tooltip = new Tooltip(String.format(
					"%s\nv.%s",
					wrapper.pid(),
					wrapper.version().toString()
			));
			label.setTooltip(tooltip);
			this.title = new HBox();
			title.setMaxWidth(Double.MAX_VALUE);
			title.setSpacing(UIStrategyGUI.Stage.insets);
			title.setAlignment(Pos.CENTER_LEFT);
			title.getChildren().setAll(glyph, label);

			final Glyph importGlyph = UIStrategyGUI.Glyphs.newGlyph(IMPORT_GLYPH, Glyph.Size.NORMAL);
			importGlyph.enableHoverEffect(true);
			importGlyph.setTooltip(localize("preset.load"));
			importGlyph.setOnMouseClicked((e) -> {
				final ProcessorPresetImportDialog dialog = new ProcessorPresetImportDialog(handler, wrapper);
				dialog.show();
			});
			final Glyph exportGlyph = UIStrategyGUI.Glyphs.newGlyph(EXPORT_GLYPH, Glyph.Size.NORMAL);
			exportGlyph.enableHoverEffect(true);
			exportGlyph.setTooltip(localize("preset.save"));
			exportGlyph.setOnMouseClicked((e) -> {
				final ProcessorPresetExportDialog dialog = new ProcessorPresetExportDialog(handler, wrapper);
				dialog.show();
			});
			this.menu = new HBox();
			menu.setSpacing(UIStrategyGUI.Stage.insets);
			menu.setAlignment(Pos.CENTER_RIGHT);
			menu.getChildren().setAll(importGlyph, exportGlyph);

			pane.setCenter(title);
			pane.setRight(menu);
		}

		public Node getNode() {
			return pane;
		}

		public void showMenu(boolean show) {
			pane.setRight(show ? menu : null);
		}
	}

	/**
	 * Initializes the editing property. Should be called once panes and ports
	 * are setup, so at the end of the constructor of a sub-class.
	 */
	protected void setupEditingListeners() {
		showParameters(wrapper.isEditing());
		this.editingProperty.addListener(editingListener);
		this.editingProperty.set(wrapper.isEditing());
	}

	/**
	 * Initializes the view and starts listening to transmutable invalidation
	 * events. Needs to be called once the view has been registered to the
	 * processorViews in the EditorPane.
	 */
	public void init() {
		if (wrapper.processor() instanceof Transmutable) {
			final Transmutable t = (Transmutable) wrapper.processor();
			t.transmuteProperty().addListener(transmutableListener);
		}
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

	protected ClosedParameterView closedParameterView() {
		if (!this.hasParameters()) {
			return null;
		}

		if (this.closedParameterView == null) {
			this.closedParameterView = new ClosedParameterView();
		}

		return this.closedParameterView;
	}

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
			} else {
				// fixes stuck OPEN_HAND from onMouseEntered but onMouseExited
				// is never fired upon resizing the processor view with the mouse
				// cursor left out in the open...
				onMouseExited(null);
			}

			if (editor.handler.settings.pipelineEditor.autoRearrangeOnProcessorFold) {
				editor.editorPane().rearrangeProcessors();
			}

			e.consume();
		}
	}

	protected void onMouseEntered(MouseEvent e) {
		editor.setCursor(Cursor.OPEN_HAND);
		if (this.closedParameterView != null) {
			this.closedParameterView.glyph.disabledHoverEffectProperty().set(false);
		}
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
		if (this.closedParameterView != null) {
			this.closedParameterView.glyph.disabledHoverEffectProperty().set(true);
		}
	}

	protected boolean hasParameters() {
		if (!wrapper().isAvailable()) {
			return false;
		}
		return wrapper().processor().parameters().size() > 0;
	}

	// parameter view factory
	protected abstract ParameterView newParameterView();

	// add/remove parameterView from main/info pane
	protected void showParameters(boolean show) {
		head.showMenu(show);
	}

	public final BooleanProperty selectedProperty() {
		return selectedProperty;
	}

	/**
	 * Closed parameter view. Will only be used for processors that actually
	 * have some parameters, and indicates exactly this.
	 */
	public static class ClosedParameterView {

		protected final HBox hbox;
		protected final Glyph glyph;

		public ClosedParameterView() {
			glyph = UIStrategyGUI.Glyphs.newGlyph(
					MaterialDesignIcons.CHEVRON_DOWN,
					Glyph.Size.MEDIUM
			);
			glyph.setAlignment(Pos.CENTER);
			glyph.disabledHoverEffectProperty().set(true);

			hbox = new HBox();
			hbox.setPadding(new Insets(UIStrategyGUI.Stage.insets));
			hbox.setAlignment(Pos.CENTER);
			hbox.getChildren().add(glyph);
		}

		public Parent node() {
			return hbox;
		}
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

		/**
		 * Creates a new grid parameter view for the given processor.
		 *
		 * @param processor the processor.
		 */
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
			if (hasLabels(params)) {
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
			} else {
				for (int i = 0; i < n; i++) {
					final Parameter.View v = params[i].view();
					root.addRow(v.node());
				}
			}
		}

		private boolean hasLabels(Parameter[] params) {
			for (int i = 0; i < params.length; i++) {
				if (params[i].isPersistent()) {
					final PersistentParameter pp = (PersistentParameter) params[i];
					if (!pp.label().isEmpty()) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public ObservableList<Node> getChildren() {
			return root.getChildren();
		}
	}

}
