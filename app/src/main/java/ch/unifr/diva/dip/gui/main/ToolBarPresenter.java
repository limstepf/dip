package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.gui.VisibilityMode;
import ch.unifr.diva.dip.api.services.Editable;
import ch.unifr.diva.dip.api.tools.GestureEventHandler;
import ch.unifr.diva.dip.api.tools.MultiTool;
import ch.unifr.diva.dip.api.tools.SimpleTool;
import ch.unifr.diva.dip.api.tools.Tool;
import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.model.RunnablePipeline;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.eventbus.events.ProcessorNotification;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.editor.EditorPresenter;
import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A simple tool bar.
 */
public class ToolBarPresenter implements Presenter {

	protected final static NamedGlyph GLYPH_DEFAULT = MaterialDesignIcons.CURSOR_POINTER;
	protected final static Color COLOR_SELECTED = UIStrategyGUI.Colors.accent;
	protected final static Color COLOR_DEFAULT = UIStrategyGUI.Colors.accent_inverted;

	private final ApplicationHandler handler;
	private final Stage stage;
	private final EditorPresenter editor;
	private final VBox root;
	private final List<ToolButton<? extends Tool>> globalTools;
	private final List<ToolButton<? extends Tool>> tools;
	private final ObjectProperty<VisibilityMode> visibilityModeProperty;
	private final OptionsBar optionsBar;

	private ToolButton<? extends Tool> selectedTool;
	private Glyph.Size glyphSize;

	private ProjectPage currentPage;
	private RunnableProcessor currentRunnable;
	private Editable currentProcessor;

	/**
	 * Creates a new tool bar.
	 *
	 * @param handler the application handler.
	 * @param editor the main editor.
	 */
	public ToolBarPresenter(ApplicationHandler handler, EditorPresenter editor) {

		this.handler = handler;
		this.stage = handler.uiStrategy.getStage();
		this.editor = editor;
		this.root = new VBox();
		this.root.getStyleClass().add("dip-tool-bar");
		this.optionsBar = new OptionsBar(handler);
		this.glyphSize = Glyph.Size.MEDIUM; // TODO: settings?
		this.globalTools = new ArrayList<>();
		this.tools = new ArrayList<>();
		this.visibilityModeProperty = new SimpleObjectProperty<VisibilityMode>(
				readVisibilityMode()
		) {
			@Override
			public void set(VisibilityMode mode) {
				super.set(mode);
				rebuild();
			}
		};
		this.handler.settings.primaryStage.toolBarVisibility.addListener((c) -> {
			final VisibilityMode mode = readVisibilityMode();
			if (!mode.equals(getVisibilityMode())) {
				this.visibilityModeProperty.set(mode);
			}
		});
		this.handler.settings.primaryStage.optionsBarVisibility.addListener(
				(c) -> rebuildOptionsBar()
		);

		if (getVisibilityMode().equals(VisibilityMode.ALWAYS)) {
			rebuild();
		}
	}

	private Glyph.Size getGlyphSize() {
		return this.glyphSize;
	}

	private VisibilityMode readVisibilityMode() {
		return VisibilityMode.get(
				this.handler.settings.primaryStage.toolBarVisibility.get()
		);
	}

	private VisibilityMode getVisibilityMode() {
		return this.visibilityModeProperty.get();
	}

	@Override
	public Parent getComponent() {
		return this.root;
	}

	/**
	 * Adds/registers global tools. Global tools are available for all
	 * processors.
	 *
	 * @param tools list of global tools.
	 * @return list of wrapped global tools (in tool buttons). Keep them around
	 * if you whish to remove/unregister them again at some point.
	 */
	public List<ToolButton<Tool>> addGlobalTool(Tool... tools) {
		final List<ToolButton<Tool>> tbs = new ArrayList<>();
		for (Tool tool : tools) {
			this.globalTools.add(newToolButton(tool));
		}
		rebuild();
		return tbs;
	}

	/**
	 * Removes/unregisters a global tool.
	 *
	 * @param tb the wrapped global tool to be removed.
	 */
	public void removeGlobalTool(ToolButton<Tool> tb) {
		if (this.globalTools.contains(tb)) {
			this.globalTools.remove(tb);
			rebuild();
		}
	}

	/**
	 * Removes/unregisters all global tools.
	 */
	public void clearGlobalTools() {
		this.globalTools.clear();
		rebuild();
	}

	/**
	 * Return the options bar linked to the toolbar.
	 *
	 * @return the options bar.
	 */
	public Presenter getOptionsBar() {
		return this.optionsBar;
	}

	@Subscribe
	public void projectNotification(ProjectNotification event) {
		switch (event.type) {
			case OPENED:
				onPageSelected();
				break;
			case SELECTED:
				onPageSelected();
				break;
			case CLOSING:
				onPageClosed();
				break;
			default:
				break;
		}
	}

	private void onPageSelected() {
		clearToolBar();
	}

	private void onPageClosed() {
		clearToolBar();
	}

	@Subscribe
	public void processorNotification(ProcessorNotification event) {
		switch (event.type) {
			case SELECTED:
				onProcessorSelected(event.processorId);
				break;
		}
	}

	private void rebuild() {
		final int processorId = (this.currentRunnable == null)
				? -1 : this.currentRunnable.id;

		if (!loadToolBar(processorId)) {
			clearToolBar();
		}

		rebuildOptionsBar();
	}

	// fired from main presenter listening to processor notifications
	private void onProcessorSelected(int processorId) {
		if (processorId < 0) {
			clearToolBar();
		} else {
			if (!loadToolBar(processorId)) {
				clearToolBar();
			}
		}
	}

	private boolean loadToolBar(int processorId) {
		final VisibilityMode mode = getVisibilityMode();
		if (mode.equals(VisibilityMode.NEVER)) {
			return false;
		}

		if (this.handler.getProject() == null) {
			return false;
		}

		this.currentPage = this.handler.getProject().getSelectedPage();
		if (this.currentPage == null) {
			return false;
		}

		final RunnablePipeline pipeline = this.currentPage.getPipeline();
		if (pipeline == null) {
			return false;
		}

		this.currentRunnable = pipeline.getProcessor(processorId);
		if (this.currentRunnable == null) {
			return false;
		}

		if (this.currentRunnable.processor().hasTools()) {
			this.currentProcessor = this.currentRunnable.processor().asEditableProcessor();
		} else {
			this.currentProcessor = null;
			return false;
		}

		resetToolBar();
		for (Tool tool : this.currentProcessor.tools()) {
			this.tools.add(newToolButton(tool));
		}
		buildToolBar(mode);

		return true;
	}

	// clear list + 1 default non-tool tool
	private void resetToolBar() {
		this.tools.clear();
		for (ToolButton<? extends Tool> tb : this.globalTools) {
			this.tools.add(tb);
		}
		// TODO: keep global tool selected?
		setSelectedTool((this.globalTools.size() > 0) ? this.globalTools.get(0) : null);
	}

	private void clearToolBar() {
		final VisibilityMode mode = getVisibilityMode();
		resetToolBar();
		buildToolBar(mode);
	}

	private void buildToolBar(VisibilityMode mode) {
		this.root.getChildren().clear();

		if (mode.equals(VisibilityMode.NEVER)
				|| (mode.equals(VisibilityMode.AUTO) && this.tools.size() < 2)) {
			// hide/remove toolbar
			return;
		}

		for (ToolButton<? extends Tool> t : this.tools) {
			this.root.getChildren().add(t.node());
		}

		setSelectedTool(this.tools.get(0));
	}

	protected void setSelectedTool(ToolButton<? extends Tool> tb) {
		if (tb == null || !this.tools.contains(tb)) {
			if (this.selectedTool != null) {
				this.selectedTool.setSelected(false);
				unbindTool(this.selectedTool.tool());
			}
			return;
		}
		if (this.selectedTool != null) {
			this.selectedTool.setSelected(false);
			bindTool(this.selectedTool.tool(), tb.tool());
		} else {
			bindTool(null, tb.tool());
		}
		setSelectedSimpleTool(tb);
	}

	protected void setSelectedMultiTool(ToolButton<? extends Tool> tb, MultiTool mt, SimpleTool newTool) {
		final Tool currentTool = (this.selectedTool != null)
				? this.selectedTool.simpleTool()
				: null;
		if (!this.tools.contains(tb)) {
			return;
		}
		if (this.selectedTool != null && !this.selectedTool.equals(tb)) {
			this.selectedTool.setSelected(false);
		}
		mt.setSelectTool(newTool);
		bindTool(currentTool, newTool);
		setSelectedSimpleTool(tb);
	}

	private void setSelectedSimpleTool(ToolButton<? extends Tool> tb) {
		this.selectedTool = tb;
		this.selectedTool.setSelected(true);
		this.selectedTool.tool.onSelected();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void bindTool(Tool currentTool, Tool newTool) {
		final Pane pane = getContentPane();
		unbindTool(pane, currentTool);

		for (GestureEventHandler h : newTool.getGesture().eventHandlers()) {
			if (h.isKeyEvent()) {
				stage.addEventHandler(h.eventType, h.eventHandler);
			} else {
				pane.addEventHandler(h.eventType, h.eventHandler);
			}
		}

		editor.getZoomPane().cursorProperty().bind(newTool.cursorProperty());
		buildOptionsBar(newTool);
	}

	private void unbindTool(Tool currentTool) {
		final Pane pane = getContentPane();
		unbindTool(pane, currentTool);
		optionsBar.clear();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void unbindTool(Pane pane, Tool currentTool) {
		if (currentTool != null) {
			for (GestureEventHandler h : currentTool.getGesture().eventHandlers()) {
				if (h.isKeyEvent()) {
					stage.removeEventHandler(h.eventType, h.eventHandler);
				} else {
					pane.removeEventHandler(h.eventType, h.eventHandler);
				}
			}

			editor.getZoomPane().cursorProperty().unbind();
			currentTool.onDeselected();
		}
		editor.getZoomPane().setCursor(Cursor.DEFAULT);
	}

	private void rebuildOptionsBar() {
		buildOptionsBar(this.selectedTool.tool());
	}

	private void buildOptionsBar(Tool tool) {
		optionsBar.build(
				(currentProcessor == null) ? null : currentProcessor.options(),
				(tool == null) ? null : tool.options()
		);
	}

	private Pane getContentPane() {
		return this.editor.getZoomPane().getContentPane();
	}

	private ToolButton<? extends Tool> newToolButton(Tool tool) {
		return tool.isMultiTool()
				? new MultiToolButton(this, tool.asMultiTool())
				: new ToolButton<>(this, tool);
	}

	/**
	 * A tool button.
	 *
	 * @param <T> class of the tool.
	 */
	public static class ToolButton<T extends Tool> {

		protected final ToolBarPresenter toolBar;
		protected final T tool;
		protected final AnchorPane node;

		protected Glyph currentGlyph;
		protected boolean isSelected;

		/**
		 * Creates a new tool button.
		 *
		 * @param toolBar the toolbar.
		 * @param tool the tool.
		 */
		public ToolButton(ToolBarPresenter toolBar, T tool) {
			this.toolBar = toolBar;
			this.tool = tool;
			this.node = new AnchorPane();
			this.isSelected = false;
			this.currentGlyph = newGlyph();
			this.node.getChildren().add(this.currentGlyph);

			if (!tool.isMultiTool()) {
				this.node.setOnMousePressed((e) -> selectThisTool(e));
			}
		}

		final protected void selectThisTool(MouseEvent e) {
			if (e.isPrimaryButtonDown()) {
				selectThisTool();
			}
		}

		protected void selectThisTool() {
			this.toolBar.setSelectedTool(this);
		}

		final protected Glyph newGlyph() {
			final NamedGlyph g = this.tool.getGlyph();
			final Glyph glyph = UIStrategyGUI.Glyphs.newGlyph(
					(g == null) ? GLYPH_DEFAULT : g,
					this.toolBar.getGlyphSize(),
					getColor()
			);
			// TODO: glyph size -> padding amount
			glyph.setTooltip(this.tool.getName());
			glyph.enableHoverEffect(true);
			glyph.setPadding(new Insets(UIStrategyGUI.Stage.insets));
			return glyph;
		}

		final protected Color getColor() {
			return this.isSelected ? COLOR_SELECTED : COLOR_DEFAULT;
		}

		/**
		 * Returns the tool of this tool button.
		 *
		 * @return the tool.
		 */
		public T tool() {
			return this.tool;
		}

		/**
		 * Returns the simple tool. This is equivalent to {@code tool()} for
		 * simple tools, but returns the selected (simple) tool for multi tools.
		 *
		 * @return the simple tool.
		 */
		public Tool simpleTool() {
			return this.tool.isMultiTool()
					? this.tool.asMultiTool().getSelectedTool()
					: this.tool;
		}

		/**
		 * Returns the (root-) node of the tool button.
		 *
		 * @return the node of the tool button.
		 */
		public Node node() {
			return this.node;
		}

		/**
		 * Marks the tool of this tool button as de-/selected.
		 *
		 * @param selected {@code true} to mark the tool as selected,
		 * {@code false} to mark it as deselected.
		 */
		public void setSelected(boolean selected) {
			this.isSelected = selected;
			this.currentGlyph.enableHoverEffect(!selected);
			this.currentGlyph.setColor(getColor());
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName()
					+ "@" + Integer.toHexString(this.hashCode())
					+ "{"
					+ "tool=" + this.tool
					+ ", isSelected=" + this.isSelected
					+ "}";
		}

	}

	/**
	 * A tool button for multi-tools.
	 */
	public static class MultiToolButton extends ToolButton<MultiTool> {

		protected final Polygon triangle;
		protected final ContextMenu menu;
		protected Transition delay;

		/**
		 * Creates a new multi-tool button.
		 *
		 * @param toolBar the toolbar.
		 * @param tool the multi-tool.
		 */
		MultiToolButton(ToolBarPresenter toolBar, MultiTool tool) {
			super(toolBar, tool);

			// mark as multi-tool with a tiny triangle at bottom-right corner
			this.triangle = newTrianlge(5.0);
			final double triBorder = 3.0;
			AnchorPane.setBottomAnchor(triangle, triBorder);
			AnchorPane.setRightAnchor(triangle, triBorder);
			this.node.getChildren().add(triangle);

			this.menu = new ContextMenu();

			for (SimpleTool t : tool.getSimpleTools()) {
				final MenuItem item = newMenuItem(t);
				item.setOnAction((e) -> onSelectSimpleTool(t));
				this.menu.getItems().add(item);
			}
			// open context menu if primary mouse button is pressed for some time
			this.node.setOnMousePressed((e) -> {
				if (e.isPrimaryButtonDown()) {
					stopDelay();
					this.delay = new PauseTransition(
							Duration.millis(UIStrategyGUI.Animation.delayDuration)
					);
					this.delay.setOnFinished((f) -> {
						final Bounds screen = getScreenBounds();
						menu.show(
								this.node,
								screen.getMaxX(),
								screen.getMinY()
						);
					});
					this.delay.play();
				}
			});
			// just select the multi-tool's current/selected tool if primary mouse
			// button isn't pressed long enough
			this.node.setOnMouseReleased((e) -> {
				if (this.delay != null && this.delay.getStatus().equals(Animation.Status.RUNNING)) {
					this.delay.stop();
					selectThisTool();
				}
			});
			// nevermind...
			this.node.setOnMouseExited((e) -> stopDelay());
		}

		protected final Polygon newTrianlge(double size) {
			final Polygon poly = new Polygon(
					size, 0,
					size, size,
					0, size
			);
			poly.setFill(ToolBarPresenter.COLOR_DEFAULT);
			return poly;
		}

		@Override
		protected final void selectThisTool() {
			this.toolBar.setSelectedMultiTool(this, this.tool, this.tool.getSelectedTool());
		}

		protected final void onSelectSimpleTool(SimpleTool tool) {
			this.toolBar.setSelectedMultiTool(this, this.tool, tool);
		}

		@Override
		public void setSelected(boolean selected) {
			// (re-)draw glyph of newly selected SimpleTool
			this.currentGlyph = newGlyph();
			this.node.getChildren().setAll(
					this.currentGlyph,
					this.triangle
			);

			super.setSelected(selected);
		}

		protected final void stopDelay() {
			if (this.delay != null) {
				this.delay.stop();
			}
		}

		protected final MenuItem newMenuItem(Tool tool) {
			final BorderPane pane = new BorderPane();
			final Glyph glyph = newMenuGlyph(tool);
			final Label label = new Label(tool.getName());
			pane.setLeft(glyph);
			pane.setCenter(label);
			final CustomMenuItem item = new CustomMenuItem(pane);
			item.getStyleClass().add("dip-menuitem");
			return item;
		}

		final protected Glyph newMenuGlyph(Tool tool) {
			final NamedGlyph g = tool.getGlyph();
			final Glyph glyph = UIStrategyGUI.Glyphs.newGlyph(
					(g == null) ? GLYPH_DEFAULT : g,
					Glyph.Size.NORMAL
			);
			glyph.getStyleClass().add("dip-glyph");
			glyph.setPadding(new Insets(0, UIStrategyGUI.Stage.insets, 0, 0));
			return glyph;
		}

		protected final Bounds getScreenBounds() {
			final Bounds local = this.node.getBoundsInLocal();
			return this.node.localToScreen(local);
		}

	}

}
