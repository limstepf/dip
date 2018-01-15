package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
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

	protected static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
	protected static final PseudoClass UNAVAILABLE = PseudoClass.getPseudoClass("unavailable");
	protected static final NamedGlyph IMPORT_GLYPH = MaterialDesignIcons.IMPORT;
	protected static final NamedGlyph EXPORT_GLYPH = MaterialDesignIcons.EXPORT;
	protected static final NamedGlyph INFO_GLYPH = MaterialDesignIcons.INFORMATION_OUTLINE;

	// we may want to make this a user setting in the future, but keep in mind that
	// padding should be large enough to accommodate for port labels, and directly
	// influences pipeline layout strategies/auto. arrangement of processors.
	protected static final double verticalPadding = 64;
	protected static final double horizontalPadding = verticalPadding * 1.667;

	protected final PipelineEditor editor;
	protected final PrototypeProcessor wrapper;
	protected final List<PortView<InputPort<?>>> inputPorts;
	protected final List<PortView<OutputPort<?>>> outputPorts;

	protected final ProcessorHead head;
	protected ParameterView parameterView;
	protected ParameterView closedParameterView;
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
	public ProcessorView(PipelineEditor editor, PrototypeProcessor wrapper) {
		this.editor = editor;
		this.wrapper = wrapper;
		this.inputPorts = new ArrayList<>();
		this.outputPorts = new ArrayList<>();

		if (wrapper.isAvailable()) {
			for (Map.Entry<String, InputPort<?>> e : wrapper.serviceObject().inputs().entrySet()) {
				final InputPort<?> input = e.getValue();
				if (input != null) {
					final PortView<InputPort<?>> view = new PortView<>(this, e.getKey(), input);
					editor.editorPane().registerPort(input, view);
					inputPorts.add(view);
				} else {
					log.warn("invalid input port: {}", e.getKey());
				}
			}

			for (Map.Entry<String, OutputPort<?>> e : wrapper.serviceObject().outputs().entrySet()) {
				final OutputPort<?> output = e.getValue();
				if (output != null) {
					final PortView<OutputPort<?>> view = new PortView<>(this, e.getKey(), output);
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
		this.setPadding(new Insets(
				verticalPadding,
				horizontalPadding,
				verticalPadding,
				horizontalPadding
		));
		/*
		 * TODO: the problem with this is that the padding/insets also steal
		 * mouse events, which is a problem since the padding may easily overlap
		 * other processor views. Unfortunately setPickOnBounds doesn't help us
		 * here, and neither can we just setMouseTransparent the root, since we
		 * can't undo it for child nodes s.t. still recieve the mouse events...
		 */
//		this.setPickOnBounds(false);

		if (!wrapper.isAvailable()) {
			this.pseudoClassStateChanged(UNAVAILABLE, true);
		}
		wrapper.availableProperty().addListener(availableListener);

		this.head = new ProcessorHead(editor.applicationHandler(), wrapper);
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
	 * Initializes the view and starts listening to repaint invalidation events.
	 * Needs to be called once the view has been registered to the
	 * processorViews in the EditorPane.
	 */
	public void init() {
		if (wrapper.serviceObject().hasRepaintProperty()) {
			wrapper.serviceObject().repaintProperty().addListener(repaintListener);
		}
	}

	/**
	 * Cleanly shuts down the view. Should be called before deletion to remove
	 * some listeners that otherwise might complain. Also removes port labels
	 * that might still hang around...
	 */
	public void stop() {
		if (wrapper.serviceObject().hasRepaintProperty()) {
			wrapper.serviceObject().repaintProperty().removeListener(repaintListener);
		}
		removePortLabels();
	}

	/**
	 * Returns the horizontal padding. Applied to the right and left side of the
	 * processor view.
	 *
	 * @return the horizontal padding.
	 */
	public double getHorizontalPadding() {
		return horizontalPadding;
	}

	/**
	 * Returns the vertical padding. Applied to the top and the bottom side of
	 * the processor view.
	 *
	 * @return the vertical padding.
	 */
	public double getVerticalPadding() {
		return verticalPadding;
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

	// listen to processor's repaint requests
	protected final InvalidationListener repaintListener = (observable) -> {
		editor().editorPane().updateProcessor(wrapper());
	};

	// listen to "editing" (opened/closed parameters) processors
	protected final BooleanProperty editingProperty = new SimpleBooleanProperty(false);

	final InvalidationListener editingListener = (observable) -> {
		if (editingProperty.get()) {
			wrapper().editingProperty().set(true);
			showParameters(true);
		} else {
			wrapper().editingProperty().set(false);
			showParameters(false);
		}

		updatePorts();
		repaint();
	};

	protected void repaint() {
		editor.repaint();
	}

	protected ParameterView closedParameterView() {
		if (!this.hasParameters()) {
			return null;
		}

		if (this.closedParameterView == null) {
			this.closedParameterView = new ClosedParameterView();
		}

		return this.closedParameterView;
	}

	protected final ParameterView parameterView() {
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
	// processor view) which actually moves.
	protected final InvalidationListener portListener = (observable) -> {
		updatePorts();
	};

	protected final void updatePorts() {
		for (PortView<?> v : this.inputPorts) {
			v.updateCenter();
		}
		for (PortView<?> v : this.outputPorts) {
			v.updateCenter();
		}
	}

	protected boolean hasConnectedInputPortsOrNone() {
		if (inputPorts.isEmpty()) {
			return true;
		}
		for (PortView<InputPort<?>> pv : inputPorts) {
			if (pv.port.isConnected()) {
				return true;
			}
		}
		return false;
	}

	public List<ConnectionView> wires() {
		final List<ConnectionView> wires = new ArrayList<>();
		addWires(wires, this.inputPorts);
		addWires(wires, this.outputPorts);
		return wires;
	}

	protected void addWires(List<ConnectionView> wires, List<? extends PortView<?>> ports) {
		for (PortView<?> v : ports) {
			if (v.hasWires()) {
				wires.addAll(v.wires());
			}
		}
	}

	// safely remove wires and unregister ports
	public void unregister() {
		if (wrapper.serviceObject().hasRepaintProperty()) {
			wrapper.serviceObject().repaintProperty().removeListener(repaintListener);
		}

		for (PortView<InputPort<?>> v : this.inputPorts) {
			final InputPort<?> input = v.port;

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

		for (PortView<OutputPort<?>> v : this.outputPorts) {
			final OutputPort<?> output = v.port;

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

	/**
	 * Returns the processor of this processor view.
	 *
	 * @return the processor.
	 */
	public final PrototypeProcessor wrapper() {
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

			// auto-rearrange is fine and all, but not for unconnected processor views
			// hanging around there... since they would be pushed to stage 1 (the front)
			// which is confusing/annoying as hell... technically we should make sure
			// to not do so for sub-pipelines/-graphs not fully connected to the root,
			// even if this node has an input connection, one of its predecessors might
			// not, ...but not sure if this is really worth the trouble? Well, if anyone
			// should complain, here's the place to tweak this.
			if (editor.handler.settings.pipelineEditor.autoRearrangeOnProcessorFold
					&& hasConnectedInputPortsOrNone()) {
				editor.editorPane().rearrangeProcessors();
			}

			e.consume();
		}
	}

	protected void onMouseEntered(MouseEvent e) {
		editor.setCursor(Cursor.OPEN_HAND);
		if (this.closedParameterView != null) {
			this.closedParameterView.onMouseEntered(e);
		}
		addPortLabels();
	}

	protected void onMouseExited(MouseEvent e) {
		editor.setCursor(Cursor.DEFAULT);
		if (this.closedParameterView != null) {
			this.closedParameterView.onMouseExited(e);
		}
		removePortLabels();
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

		repaint();
	}

	protected void findMinimalNodes(Set<? extends Node> nodes) {
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

	protected boolean hasParameters() {
		return hasParameters(wrapper());
	}

	protected static boolean hasParameters(PrototypeProcessor wrapper) {
		if (!wrapper.isAvailable()) {
			return false;
		}
		return wrapper.serviceObject().parameters().size() > 0;
	}

	// parameter view factory
	protected abstract ParameterView newParameterView();

	// add/remove parameterView from main/info pane
	// needs to be overwritten, and call repaint() at the end
	protected void showParameters(boolean show) {
		head.showMenu(show);
	}

	/**
	 * The selected property. This property is {@code true} if the processor
	 * view is currently selected.
	 *
	 * @return the selected property.
	 */
	public final BooleanProperty selectedProperty() {
		return selectedProperty;
	}

	/**
	 * Adds/shows port labels.
	 */
	public void addPortLabels() {
		if (!isPortLabelsVisible()) {
			addProcessorViewLabels(getInputPortLabels());
			addProcessorViewLabels(getOutputPortLabels());
			setPortLabelsVisible(true);
		}
	}

	/**
	 * Removes/hides port labels.
	 */
	public void removePortLabels() {
		if (isPortLabelsVisible()) {
			removeProcessorViewLabels(getInputPortLabels());
			removeProcessorViewLabels(getOutputPortLabels());
			setPortLabelsVisible(false);
		}
	}

	protected <T extends Port<?>> void addProcessorViewLabels(List<PortView.Label<T>> labels) {
		for (PortView.Label<T> label : labels) {
			editor.editorPane().pane().getChildren().add(label);
			label.bind();
		}
	}

	protected <T extends Port<?>> void removeProcessorViewLabels(List<PortView.Label<T>> labels) {
		for (PortView.Label<T> label : labels) {
			label.unbind();
			editor.editorPane().pane().getChildren().remove(label);
		}
	}

	private boolean isPortLabelsVisible;

	/**
	 * Checks whether the port labels are currently visible.
	 *
	 * @return {@code true} if the port labels are visible, {@code false}
	 * otherwise.
	 */
	public boolean isPortLabelsVisible() {
		return isPortLabelsVisible;
	}

	/**
	 * Sets the visibility of the port labels.
	 *
	 * @param visible {@code true} if the port labels are visible, {@code false}
	 * otherwise.
	 */
	protected void setPortLabelsVisible(boolean visible) {
		isPortLabelsVisible = visible;
	}

	private List<PortView.Label<InputPort<?>>> inputPortLabels;

	/**
	 * Returns the input port labels.
	 *
	 * @return the input port labels.
	 */
	protected List<PortView.Label<InputPort<?>>> getInputPortLabels() {
		if (inputPortLabels == null) {
			updatePorts();
			inputPortLabels = new ArrayList<>();
			for (PortView<InputPort<?>> pv : inputPorts) {
				inputPortLabels.add(pv.getLabel());
			}
		}
		return inputPortLabels;
	}

	private List<PortView.Label<OutputPort<?>>> outputPortLabels;

	/**
	 * Returns the output port labels.
	 *
	 * @return the output port labels.
	 */
	protected List<PortView.Label<OutputPort<?>>> getOutputPortLabels() {
		if (outputPortLabels == null) {
			updatePorts();
			outputPortLabels = new ArrayList<>();
			for (PortView<OutputPort<?>> pv : outputPorts) {
				outputPortLabels.add(pv.getLabel());
			}
		}
		return outputPortLabels;
	}

	/**
	 * Parameter view interface.
	 */
	public static interface ParameterView {

		/**
		 * The root node of the parameter view.
		 *
		 * @return the root node of the parameter view.
		 */
		public Parent node();

		/**
		 * The children of the parameter view. S.t. we can check if there are
		 * any parameters at all.
		 *
		 * @return the children of the parameter view.
		 */
		public ObservableList<Node> getChildren();

		/**
		 * Passthrough of the processor view's mouse entered event.
		 *
		 * @param e the mouse event.
		 */
		default void onMouseEntered(MouseEvent e) {

		}

		/**
		 * Passthrough of the processor view's mouse exited event.
		 *
		 * @param e the mouse event.
		 */
		default void onMouseExited(MouseEvent e) {

		}

	}

	/**
	 * Closed parameter view. Will only be used for processors that actually
	 * have some parameters, and indicates exactly this.
	 */
	public static class ClosedParameterView implements ParameterView {

		protected final HBox hbox;
		protected final Glyph glyph;

		/**
		 * Creates a new closed parameter view.
		 */
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

		@Override
		public void onMouseEntered(MouseEvent e) {
			glyph.disabledHoverEffectProperty().set(false);
		}

		@Override
		public void onMouseExited(MouseEvent e) {
			glyph.disabledHoverEffectProperty().set(true);
		}

		@Override
		public Parent node() {
			return hbox;
		}

		@Override
		public ObservableList<Node> getChildren() {
			return FXCollections.emptyObservableList();
		}

	}

	/**
	 * Parameter view base class.
	 *
	 * @param <T> Type of root node.
	 */
	public static abstract class ParameterViewBase<T extends Parent> implements ParameterView {

		protected final T root;

		/**
		 * Creates a new parameter view.
		 *
		 * @param root root node.
		 */
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
			// of parameters changes (e.g. on repaint/init).
			//
			// Thus... we first get params/keys without further action...
			final int n = processor.parameters().size();
			final Parameter<?>[] params = new Parameter<?>[n];
			//final String[] keys = new String[n];

			int j = 0;
			for (Map.Entry<String, Parameter<?>> e : processor.parameters().entrySet()) {
				params[j] = e.getValue();
				//keys[j] = e.getKey();
				j++;
			}

			// ...and do our thing now.
			if (hasLabels(params)) {
				for (int i = 0; i < n; i++) {
					final Parameter<?> p = params[i];
					//final String key = keys[i];
					final Parameter.View v = p.view();
					final Label label;

					if (p.isPersistent()) {
						final PersistentParameter<?> pp = p.asPersitentParameter();
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

		private boolean hasLabels(Parameter<?>[] params) {
			for (int i = 0; i < params.length; i++) {
				if (params[i].isPersistent()) {
					if (!params[i].asPersitentParameter().label().isEmpty()) {
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

	/**
	 * Processor head. Shows the title and generic processor functionality (e.g.
	 * im-/export of presets).
	 */
	public static class ProcessorHead implements Localizable {

		protected final ApplicationHandler handler;
		protected final Window owner;
		protected final PrototypeProcessor wrapper;
		protected final BorderPane pane;
		protected final HBox title;
		protected final HBox menu;
		protected final Glyph glyph;

		/**
		 * Creates a new processor head.
		 *
		 * @param handler the application handler.
		 * @param wrapper the processor wrapper.
		 */
		public ProcessorHead(ApplicationHandler handler, PrototypeProcessor wrapper) {
			this.handler = handler;
			this.owner = handler.uiStrategy.getStage();
			this.wrapper = wrapper;
			wrapper.stateProperty().addListener(stateListener);

			final boolean hasParameters = hasParameters(wrapper);

			this.pane = new BorderPane();
			pane.setMaxWidth(Double.MAX_VALUE);

			this.glyph = UIStrategyGUI.Glyphs.newGlyph(wrapper.glyph(), Glyph.Size.NORMAL);
			final Label label = new Label(
					wrapper.isAvailable()
							? wrapper.serviceObject().name()
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

			this.menu = new HBox();
			menu.setSpacing(UIStrategyGUI.Stage.insets);
			menu.setPadding(new Insets(0, 0, 0, UIStrategyGUI.Stage.insets));
			menu.setAlignment(Pos.CENTER_RIGHT);

			if (hasParameters) {
				final Glyph importGlyph = UIStrategyGUI.Glyphs.newGlyph(IMPORT_GLYPH, Glyph.Size.NORMAL);
				importGlyph.enableHoverEffect(true);
				importGlyph.setTooltip(localize("preset.load") + "...");
				importGlyph.setOnMouseClicked((e) -> {
					final ProcessorPresetImportDialog dialog = new ProcessorPresetImportDialog(handler, wrapper);
					dialog.show();
				});
				menu.getChildren().add(importGlyph);

				final Glyph exportGlyph = UIStrategyGUI.Glyphs.newGlyph(EXPORT_GLYPH, Glyph.Size.NORMAL);
				exportGlyph.enableHoverEffect(true);
				exportGlyph.setTooltip(localize("preset.save") + "...");
				exportGlyph.setOnMouseClicked((e) -> {
					final ProcessorPresetExportDialog dialog = new ProcessorPresetExportDialog(handler, wrapper);
					dialog.show();
				});
				menu.getChildren().add(exportGlyph);
			}

			final Glyph infoGlyph = UIStrategyGUI.Glyphs.newGlyph(INFO_GLYPH, Glyph.Size.NORMAL);
			infoGlyph.setPadding(new Insets(0, 0, 0, 5));
			infoGlyph.enableHoverEffect(true);
			infoGlyph.setTooltip(localize("processor.info"));
			infoGlyph.setOnMouseClicked((e) -> showProcessorDocumentation());
			menu.getChildren().add(infoGlyph);

			pane.setCenter(title);
			pane.setRight(menu);

			onStateChanged();
		}

		protected final InvalidationListener stateListener = (e) -> onStateChanged();

		protected final void onStateChanged() {
			// intendet to show if a processor is fully connected in the pipeline editor,
			// so either the glyph is red (not good), or in default accent color (all fine).
			switch (wrapper.getState()) {
				case ERROR:
				case UNAVAILABLE:
				case UNCONNECTED:
					glyph.setColor(UIStrategyGUI.Colors.error);
					break;
				case WAITING:
				case PROCESSING:
				case READY:
				default:
					glyph.setColor(UIStrategyGUI.Colors.accent);
					break;
			}
		}

		public final void showProcessorDocumentation() {
			final ProcessorInformationDialog<PrototypeProcessor> dialog = new ProcessorInformationDialog<>(
					handler,
					wrapper
			);
			dialog.show();
		}

		public Node getNode() {
			return pane;
		}

		public void showMenu(boolean show) {
			pane.setRight(show ? menu : null);
		}
	}

}
