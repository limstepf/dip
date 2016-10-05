package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.ui.KeyEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler.ClickCount;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ApplicationSettings;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.PipelineLayoutStrategy;
import ch.unifr.diva.dip.core.model.PipelineManager;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.AbstractWidget;
import ch.unifr.diva.dip.gui.dialogs.ErrorDialog;
import ch.unifr.diva.dip.gui.layout.DraggableListCell;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.osgi.OSGiVersionPolicy;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
import javafx.stage.FileChooser;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline management widget. Used in the pipeline editor to manage the
 * pipelines (select, create, delete, ...).
 */
public class PipelinesWidget extends AbstractWidget {

	private static final Logger log = LoggerFactory.getLogger(PipelinesWidget.class);
	private final ApplicationHandler handler;
	private final PipelineEditor editor;
	private final View view;

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

	private void selectDefaultPipeline(Pipeline pipeline) {
		editor.pipelineManager().setDefaultPipelineId(pipeline.id);
	}

	private void deletePipelines(List<Pipeline> pipelines) {
		editor.pipelineManager().deletePipelines(pipelines);

		if (pipelines.contains(editor.selectedPipeline())) {
			editor.selectPipeline(null);
		}
	}

	private void importPipelines() {
		final FileChooser chooser = newFileChooser(localize("pipeline.import"));
		final File file = chooser.showOpenDialog(editor.stage());
		if (file != null) {
			try {
				// TODO: offer dialog to select what pipelines to import...
				this.editor.pipelineManager().importPipelines(file.toPath(), null);
			} catch (JAXBException ex) {
				log.error("failed to import piplines: {}", file, ex);
				final ErrorDialog dialog = new ErrorDialog(ex);
				dialog.showAndWait();
			}
		}
		view.layout();
	}

	private void exportPipelines(List<Pipeline> pipelines) {
		final FileChooser chooser = newFileChooser(localize("pipeline.export"));
		final File file = chooser.showSaveDialog(editor.stage());
		if (file != null) {
			try {
				PipelineManager.exportPipelines(pipelines, file.toPath());
			} catch (JAXBException ex) {
				log.error("failed to export piplines to: {}", file, ex);
				final ErrorDialog dialog = new ErrorDialog(ex);
				dialog.showAndWait();
			}
		}
	}

	private FileChooser newFileChooser(String title) {
		final FileChooser chooser = new FileChooser();
		chooser.setTitle(title);
		chooser.setInitialDirectory(
				handler.dataManager.appDataDir.pipelinePresetsDir.toFile()
		);
		ApplicationSettings.setExtensionFilter(
				chooser,
				ApplicationSettings.pipelinePresetsFileExtensionFilter
		);
		return chooser;
	}

	/**
	 * View of the pipelines widget.
	 */
	public static class View extends VBox implements Localizable {

		private static final org.slf4j.Logger log = LoggerFactory.getLogger(PipelineEditor.class);

		private final PipelinesWidget widget;
		protected final ListView<Pipeline> listView = new ListView<>();
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
			listView.setCellFactory((ListView<Pipeline> param) -> new PipelineCell(widget.editor, group));

			final MenuItem selectDefaultItem = new MenuItem(localize("pipeline.default.set"));
			selectDefaultItem.disableProperty().bind(Bindings.not(hasOneSelectedProperty));
			selectDefaultItem.setOnAction((e) -> widget.selectDefaultPipeline(getSelectedItem()));

			final MenuItem newItem = new MenuItem(localize("pipeline.create"));
			newItem.setOnAction((e) -> widget.newPipeline());

			final MenuItem importItem = new MenuItem(localize("pipeline.import"));
			importItem.setOnAction((e) -> widget.importPipelines());

			final MenuItem exportItem = new MenuItem(localize("pipeline.export.selected"));
			exportItem.disableProperty().bind(Bindings.not(hasSelectionProperty));
			exportItem.setOnAction((e) -> widget.exportPipelines(getSelectedItems()));

			final MenuItem deleteItem = new MenuItem(localize("pipeline.delete.selected"));
			deleteItem.disableProperty().bind(Bindings.not(hasSelectionProperty));
			deleteItem.setOnAction((e) -> widget.deletePipelines(getSelectedItems()));

			contextMenu.getItems().addAll(
					selectDefaultItem, newItem, importItem, exportItem, deleteItem
			);
			listView.getSelectionModel().getSelectedItems().addListener(selectionListener);
			listView.setContextMenu(contextMenu);

			this.getChildren().addAll(listView);
		}

		public final Pipeline getSelectedItem() {
			return listView.getSelectionModel().getSelectedItem();
		}

		public final List<Pipeline> getSelectedItems() {
			// ye, we really need to make a copy, or otherwise feeding that list
			// back to listView (e.g. to delete those items) wont behave as expected...
			return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
		}
	}

	/**
	 * A pipeline cell.
	 */
	public static class PipelineCell extends DraggableListCell<Pipeline> implements Localizable {

		private final PipelineEditor editor;
		private final ToggleGroup group;
		private final BorderPane pane = new BorderPane();
		private final RadioButton radioButton = new RadioButton();
		private final Label label = new Label();
		private final Label statusLabel = new Label();
		private static final Tooltip defaultPipelineTooltip = new Tooltip();
		private final InvalidationListener defaultListener = (obs) -> {
			updateDefault();
			this.layout();
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
		private Pipeline currentPipeline;

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

			final int d = UIStrategyGUI.Stage.insets;
			radioButton.setPadding(new Insets(0, d, 0, 0));
			statusLabel.setPadding(new Insets(0, 0, 0, d));

			pane.setLeft(radioButton);
			pane.setCenter(label);
			pane.setRight(statusLabel);

			defaultPipelineTooltip.setText(localize("pipeline.default"));
			editor.pipelineManager().defaultPipelineIdProperty().addListener(defaultListener);
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
			if (editor.selectedPipeline() == null) {
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
				statusLabel.setText(localize("pipeline.default.abbrev"));

				statusLabel.setTooltip(defaultPipelineTooltip);
			} else {
				statusLabel.setText("");
				statusLabel.setTooltip(null);
			}
		}

		@Override
		public final void updateItem(Pipeline item, boolean empty) {
			super.updateItem(item, empty);

			setText(null);
			setGraphic(null);

			currentPipeline = item;

			if (!empty && item != null) {
				group.setUserData(currentPipeline.id);

				radioButton.setUserData(this.getItem().id);
				radioButton.setToggleGroup(group);
				radioButton.setSelected(isPipelineSelected());

				label.setText(item.getName());
				updateDefault();

				setGraphic(pane);
			}
		}

		@Override
		public final void startEdit() {
			super.startEdit();

			getListView().addEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			getListView().addEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			final PipelineCellEditBox edit = editBox();
			edit.init(currentPipeline, label.getText());

			pane.setCenter(edit);

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

			pane.setCenter(label);

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
			this.getChildren().setAll(pipelineName, grid);
		}

		/**
		 * Inits the edit box with a new pipeline.
		 *
		 * @param pipeline the pipeline.
		 * @param name the name of the pipeline.
		 */
		public void init(Pipeline pipeline, String name) {
			layoutStrategy.set(pipeline.getLayoutStrategy().name());
			versionPolicy.set(pipeline.getVersionPolicy().name());
			pipelineName.setText(name);
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
