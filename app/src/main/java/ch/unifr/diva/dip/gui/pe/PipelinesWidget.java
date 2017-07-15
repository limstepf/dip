package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.KeyEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler.ClickCount;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.PipelineLayoutStrategy;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.gui.AbstractWidget;
import ch.unifr.diva.dip.gui.layout.DraggableListCell;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.osgi.OSGiVersionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline management widget. Used in the pipeline editor to manage the
 * pipelines (select, create, delete, ...).
 */
public class PipelinesWidget extends AbstractWidget {

	private static final Logger log = LoggerFactory.getLogger(PipelinesWidget.class);
	protected final ApplicationHandler handler;
	protected final PipelineEditor editor;
	protected final View view;

	/**
	 * Creates a new pipelines widget.
	 *
	 * @param handler the application handler.
	 * @param editor the pipeline editor.
	 */
	public PipelinesWidget(ApplicationHandler handler, PipelineEditor editor) {
		this.handler = handler;
		this.editor = editor;
		this.view = new View(this);

		setWidget(this.view);
		setTitle(localize("widget.pipelines"));

		view.group.selectedToggleProperty().addListener((
				ObservableValue<? extends Toggle> observable,
				Toggle oldValue, Toggle newValue) -> {
					if (newValue == null) {
						return;
					}
					final int id = (int) newValue.getUserData();
					editor.selectPipeline(id);
				});

		view.listView.setItems(editor.pipelineManager().pipelines());
	}

	private void newPipeline() {
		editor.createPipeline(localize("pipeline.new"));
	}

	private void clonePipeline(Pipeline<ProcessorWrapper> pipeline) {
		editor.clonePipeline(pipeline);
	}

	private void selectDefaultPipeline(Pipeline<ProcessorWrapper> pipeline) {
		// toggle if already selected
		if (editor.pipelineManager().getDefaultPipelineId() == pipeline.id) {
			editor.pipelineManager().setDefaultPipelineId(-1);
			return;
		}
		editor.pipelineManager().setDefaultPipelineId(pipeline.id);
	}

	private void deletePipelines(List<Pipeline<ProcessorWrapper>> pipelines) {
		final List<Integer> ids = getPipelineIds(pipelines);
		final List<Pipeline<ProcessorWrapper>> inUse = getPipelinesStillInUse(pipelines);
		if (inUse.isEmpty()) {
			// no pipeline is in use by a page, no problem
			if (!editor.pipelineManager().deletePipelines(pipelines)) {
				return;
			}
		} else {
			// we can't delete pipelines still in use
			final List<ProjectPage> pages = getPagesStillInUse(ids);
			final DeletePipelineDialog dialog = new DeletePipelineDialog(
					this.handler,
					pages,
					inUse,
					ids
			);
			dialog.showAndWait();
			if (dialog.isOk() && dialog.isValid()) {
				dialog.repair(); // fix offending pages first
				editor.pipelineManager().deletePipelines(pipelines, false);
			} else {
				return; // cancelled
			}
		}

		// deselect if selected pipeline got deleted
		if (pipelines.contains(editor.selectedPipeline())) {
			editor.selectPipeline(null);
		}
	}

	private List<Integer> getPipelineIds(List<Pipeline<ProcessorWrapper>> pipelines) {
		return pipelines.stream().map((p) -> p.id).collect(Collectors.toList());
	}

	private List<Pipeline<ProcessorWrapper>> getPipelinesStillInUse(List<Pipeline<ProcessorWrapper>> pipelines) {
		final List<Pipeline<ProcessorWrapper>> inUse = new ArrayList<>();
		for (Pipeline<ProcessorWrapper> p : pipelines) {
			final int usage = this.handler.getProject().getPipelineUsage(p.id);
			if (usage > 0) {
				inUse.add(p);
			}
		}
		return inUse;
	}

	private List<ProjectPage> getPagesStillInUse(List<Integer> pipelineIds) {
		final List<ProjectPage> pages = new ArrayList<>();
		for (ProjectPage page : this.handler.getProject().pages()) {
			if (pipelineIds.contains(page.getPipelineId())) {
				pages.add(page);
			}
		}
		return pages;
	}

	private void importPipelines() {
		final PipelineImportDialog dialog = new PipelineImportDialog(handler);
		dialog.show();
	}

	private void exportPipelines(List<Pipeline<ProcessorWrapper>> pipelines) {
		final PipelineExportDialog dialog = new PipelineExportDialog(handler, pipelines);
		dialog.show();
	}

	/**
	 * View of the pipelines widget.
	 */
	public static class View extends VBox implements Localizable {

		private static final org.slf4j.Logger log = LoggerFactory.getLogger(PipelinesWidget.View.class);

		private final PipelinesWidget widget;
		protected final ListView<Pipeline<ProcessorWrapper>> listView = new ListView<>();
		private final ToggleGroup group = new ToggleGroup();
		private final ContextMenu contextMenu = new ContextMenu();
		private final BooleanProperty hasSelectionProperty = new SimpleBooleanProperty(false);
		private final BooleanProperty hasOneSelectedProperty = new SimpleBooleanProperty(false);
		private final InvalidationListener selectionListener = (obs) -> {
			hasSelectionProperty.set(
					!listView.getSelectionModel().isEmpty()
			);
			hasOneSelectedProperty.set(
					listView.getSelectionModel().getSelectedItems().size() == 1
			);
		};

		/**
		 * Creates a new view of the pipelines widget.
		 *
		 * @param widget the pipelines widget.
		 */
		public View(PipelinesWidget widget) {
			this.widget = widget;
			setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(listView, Priority.ALWAYS);

			listView.setEditable(true);
			listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			listView.setPrefHeight(0);
			listView.setMaxHeight(Double.MAX_VALUE);
			listView.setCellFactory((ListView<Pipeline<ProcessorWrapper>> param) -> new PipelineCell(widget.editor, group));

			final MenuItem selectDefaultItem = new MenuItem(localize("pipeline.default.set"));
			selectDefaultItem.disableProperty().bind(Bindings.not(hasOneSelectedProperty));
			selectDefaultItem.setOnAction((e) -> widget.selectDefaultPipeline(getSelectedItem()));

			final MenuItem newItem = new MenuItem(localize("pipeline.create"));
			newItem.setOnAction((e) -> widget.newPipeline());

			final MenuItem cloneItem = new MenuItem(localize("pipeline.clone"));
			cloneItem.disableProperty().bind(Bindings.not(hasOneSelectedProperty));
			cloneItem.setOnAction((e) -> widget.clonePipeline(getSelectedItem()));

			final MenuItem importItem = new MenuItem(localize("pipeline.import"));
			importItem.setOnAction((e) -> widget.importPipelines());

			final MenuItem exportItem = new MenuItem(localize("pipeline.export.selected"));
			exportItem.disableProperty().bind(Bindings.not(hasSelectionProperty));
			exportItem.setOnAction((e) -> widget.exportPipelines(getSelectedItems()));

			final MenuItem deleteItem = new MenuItem(localize("pipeline.delete.selected"));
			deleteItem.disableProperty().bind(Bindings.not(hasSelectionProperty));
			deleteItem.setOnAction((e) -> widget.deletePipelines(getSelectedItems()));

			contextMenu.getItems().addAll(
					selectDefaultItem, newItem, cloneItem,
					importItem, exportItem, deleteItem
			);
			listView.getSelectionModel().getSelectedItems().addListener(selectionListener);
			listView.setContextMenu(contextMenu);

			this.getChildren().addAll(listView);
		}

		public final Pipeline<ProcessorWrapper> getSelectedItem() {
			return listView.getSelectionModel().getSelectedItem();
		}

		public final List<Pipeline<ProcessorWrapper>> getSelectedItems() {
			// ye, we really need to make a copy, or otherwise feeding that list
			// back to listView (e.g. to delete those items) wont behave as expected...
			return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
		}

	}

	/**
	 * A pipeline cell.
	 */
	public static class PipelineCell extends DraggableListCell<Pipeline<ProcessorWrapper>> implements Localizable {

		private Pipeline<ProcessorWrapper> currentPipeline;
		private Glyph currentGlyph;
		private final PipelineEditor editor;
		private final ToggleGroup group;
		private final BorderPane pane = new BorderPane();
		private final RadioButton radioButton = new RadioButton();
		private final VBox vbox = new VBox();
		private final Label label = new Label();
		private final Label usage = new Label();
		private static final Tooltip defaultPipelineTooltip = new Tooltip();
		private final InvalidationListener puListener = (c) -> setUsageLabel();
		private final InvalidationListener defaultListener = (obs) -> {
			updateDefault();
			this.layout();
		};
		private final InvalidationListener selectionListener = (c) -> {
			if (isPipelineSelected()) {
				updateItem(currentPipeline, currentPipeline == null);
			}
		};
		private final EventHandler<KeyEvent> onEnterHandler = new KeyEventHandler(
				KeyCode.ENTER,
				(e) -> {
					cancelEdit();
					return true;
				}
		);
		private final EventHandler<MouseEvent> onDoubleClickHandler = new MouseEventHandler(
				ClickCount.DOUBLE_CLICK,
				(e) -> {
					cancelEdit();
					return true;
				}
		);

		/**
		 * Creates a new pipeline cell.
		 *
		 * @param editor the pipeline editor.
		 * @param group the toggle group of all pipelines.
		 */
		public PipelineCell(PipelineEditor editor, ToggleGroup group) {
			this.editor = editor;
			this.group = group;

			label.setAlignment(Pos.CENTER_LEFT);
			label.setMaxWidth(Double.MAX_VALUE);

			radioButton.setPadding(new Insets(0, UIStrategyGUI.Stage.insets, 0, 0));

			usage.getStyleClass().add("dip-small");
			vbox.getChildren().setAll(label, usage);

			pane.setLeft(radioButton);
			pane.setCenter(vbox);

			defaultPipelineTooltip.setText(localize("pipeline.default"));
			editor.pipelineManager().defaultPipelineIdProperty().addListener(defaultListener);
			editor.selectedPipelineProperty().addListener(selectionListener);
		}

		// lazily initialize the editing box
		private PipelineCellEditBox editBox;

		private PipelineCellEditBox editBox() {
			if (editBox == null) {
				editBox = new PipelineCellEditBox(onEnterHandler);
			}
			return editBox;
		}

		private boolean isPipelineSelected() {
			if (currentPipeline == null || editor.selectedPipeline() == null) {
				return false;
			}
			return editor.selectedPipeline().id == currentPipeline.id;
		}

		private boolean isDefaultPipeline() {
			if (currentPipeline == null) {
				return false;
			}
			return (editor.pipelineManager().getDefaultPipelineId() == currentPipeline.id);
		}

		private void updateDefault() {
			if (isDefaultPipeline()) {
				pane.setRight(getDefaultPipelineGlyph());
				updateGlyphColor();
			} else {
				pane.setRight(null);
				currentGlyph = null;
				this.selectedProperty().removeListener(glyphListener);
			}
		}

		private Glyph getDefaultPipelineGlyph() {
			if (currentGlyph == null) {
				currentGlyph = UIStrategyGUI.Glyphs.newGlyph(
						MaterialDesignIcons.CROWN,
						Glyph.Size.NORMAL
				);
				currentGlyph.setTooltip(defaultPipelineTooltip);
				currentGlyph.setPadding(new Insets(0, 0, 0, UIStrategyGUI.Stage.insets));
				this.selectedProperty().addListener(glyphListener);
			}
			return currentGlyph;
		}

		// selection listener to alter glyph color
		private final InvalidationListener glyphListener = (c) -> {
			updateGlyphColor();
		};

		private void updateGlyphColor() {
			if (currentGlyph == null) {
				return;
			}
			currentGlyph.setColor(
					this.selectedProperty().get()
							? UIStrategyGUI.Colors.accent_inverted
							: UIStrategyGUI.Colors.accent
			);
		}

		@Override
		public final void updateItem(Pipeline<ProcessorWrapper> item, boolean empty) {
			super.updateItem(item, empty);

			setText(null);
			setGraphic(null);
			if (currentPipeline != null) {
				this.editor.applicationHandler().getProject().pipelineUsageProperty(currentPipeline.id).removeListener(puListener);
			}

			currentPipeline = item;

			if (!empty && item != null) {
				group.setUserData(currentPipeline.id);
				this.editor.applicationHandler().getProject().pipelineUsageProperty(currentPipeline.id).addListener(puListener);

				radioButton.setUserData(this.getItem().id);
				radioButton.setToggleGroup(group);
				radioButton.setSelected(isPipelineSelected());

				label.setText(item.getName());
				setUsageLabel();
				updateDefault();

				setGraphic(pane);
			}
		}

		private void setUsageLabel() {
			final int n = editor.applicationHandler().getProject().pipelineUsageProperty(currentPipeline.id).get();
			usage.setText(
					(n == 0) ? localize("pipeline.usage.none")
							: (n == 1) ? localize("pipeline.usage.one")
									: localize("pipeline.usage", n)
			);
		}

		@Override
		public final void startEdit() {
			super.startEdit();

			getListView().addEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			getListView().addEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			final PipelineCellEditBox edit = editBox();
			edit.init(currentPipeline, label.getText(), usage.getText());

			pane.setCenter(edit);
			pane.setRight(null);

			// this seems to be necessary since we ommit commitEdit and just
			// use cancelEdit. Might also be a bug, who knows...
			getListView().layout();
		}

		@Override
		public final void cancelEdit() {
			super.cancelEdit();

			getListView().removeEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			getListView().removeEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			final PipelineCellEditBox edit = editBox();
			final String tf = edit.getPipelineName();
			if (!tf.equals(label.getText())) {
				label.setText(tf);
				currentPipeline.setName(tf);
			}

			final PipelineLayoutStrategy pls = edit.getLayoutStrategy();
			if (!currentPipeline.getLayoutStrategy().equals(pls)) {
				currentPipeline.setLayoutStrategy(pls);
				editor.editorPane().updateAllProcessors();
			}

			final OSGiVersionPolicy v = edit.getVersionPolicy();
			if (!currentPipeline.getVersionPolicy().equals(v)) {
				currentPipeline.setVersionPolicy(v);
			}

			pane.setCenter(vbox);
			pane.setRight(currentGlyph);

			// this seems to be necessary since we ommit commitEdit and just
			// use cancelEdit. Might also be a bug, who knows...
			getListView().layout();
		}

	}

	/**
	 * Edit box of a pipeline cell.
	 */
	public static class PipelineCellEditBox extends VBox {

		private final FormGridPane grid;
		private final TextField pipelineName;
		private final Label usageLabel;
		private final EnumParameter layoutStrategy;
		private final EnumParameter versionPolicy;

		/**
		 * Creates a new edit box for a pipeline cell.
		 *
		 * @param onEnterHandler the enter handler to close and save/update.
		 */
		public PipelineCellEditBox(EventHandler<KeyEvent> onEnterHandler) {
			this.grid = new FormGridPane();
			this.grid.setPadding(new Insets(0, 0, UIStrategyGUI.Stage.insets * 2, 0));
			this.pipelineName = new TextField();
			VBox.setMargin(pipelineName, new Insets(0, 0, 2, 0));
			pipelineName.setOnKeyPressed(onEnterHandler);
			this.usageLabel = new Label();
			this.usageLabel.getStyleClass().add("dip-small");

			this.layoutStrategy = new EnumParameter(
					L10n.getInstance().getString("pipeline.layout.strategy"),
					PipelineLayoutStrategy.class,
					PipelineLayoutStrategy.LEFTRIGHT.name()
			);
			layoutStrategy.view().node().setOnKeyPressed(onEnterHandler);
			layoutStrategy.view().node().getStyleClass().add("dip-small");
			grid.addParameters(layoutStrategy);

			this.versionPolicy = new EnumParameter(
					L10n.getInstance().getString("processor.version.policy"),
					OSGiVersionPolicy.class,
					OSGiVersionPolicy.getDefault().name()
			);
			versionPolicy.view().node().setOnKeyPressed(onEnterHandler);
			versionPolicy.view().node().getStyleClass().add("dip-small");
			grid.addParameters(versionPolicy);

			this.setSpacing(UIStrategyGUI.Stage.insets);
			this.getChildren().setAll(pipelineName, usageLabel, grid);
		}

		/**
		 * Inits the edit box with a new pipeline.
		 *
		 * @param pipeline the pipeline.
		 * @param name the name of the pipeline.
		 * @param usage the usage message of the pipeline.
		 */
		public void init(Pipeline<ProcessorWrapper> pipeline, String name, String usage) {
			layoutStrategy.set(pipeline.getLayoutStrategy().name());
			versionPolicy.set(pipeline.getVersionPolicy().name());
			pipelineName.setText(name);
			usageLabel.setText(usage);
		}

		/**
		 * Returns the (new) name of the pipeline.
		 *
		 * @return the name of the pipeline.
		 */
		public String getPipelineName() {
			return pipelineName.getText();
		}

		/**
		 * Returns the (new) layout strategy of the pipeline.
		 *
		 * @return the layout strategy of the pipeline.
		 */
		public PipelineLayoutStrategy getLayoutStrategy() {
			return EnumParameter.valueOf(
					layoutStrategy.get(),
					PipelineLayoutStrategy.class,
					PipelineLayoutStrategy.LEFTRIGHT
			);
		}

		/**
		 * Returns the (new) version policy of the pipeline.
		 *
		 * @return the version policy of the pipeline.
		 */
		public OSGiVersionPolicy getVersionPolicy() {
			return OSGiVersionPolicy.get(versionPolicy.get());
		}
	}

}
