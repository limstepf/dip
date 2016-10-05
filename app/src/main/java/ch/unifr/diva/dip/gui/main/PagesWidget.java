package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.api.ui.KeyEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler.ClickCount;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PipelineManager;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import ch.unifr.diva.dip.eventbus.events.ProjectRequest;
import ch.unifr.diva.dip.gui.AbstractWidget;
import ch.unifr.diva.dip.gui.layout.DraggableListCell;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import com.google.common.eventbus.Subscribe;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PagesWidget allows browsing through the pages in a project.
 */
public class PagesWidget extends AbstractWidget {

	private static final Logger log = LoggerFactory.getLogger(PagesWidget.class);
	private final View view;
	private final ApplicationHandler handler;

	/**
	 * Creates a new pages widget.
	 *
	 * @param handler the application handler.
	 */
	public PagesWidget(ApplicationHandler handler) {
		super();

		this.handler = handler;
		this.view = new View(handler);

		setWidget(this.view);
		setTitle(localize("widget.pages"));

		view.group.selectedToggleProperty().addListener((
				ObservableValue<? extends Toggle> observable,
				Toggle oldValue, Toggle newValue) -> {
					if (newValue == null) {
						return;
					}
					final int id = (int) newValue.getUserData();
					if (handler.getProject().getSelectedPageId() == id) {
						return;
					}

					handler.eventBus.post(
							new ProjectRequest(ProjectRequest.Type.SELECT, id)
					);
				});
	}

	@Subscribe
	public void projectNotification(ProjectNotification event) {
		switch (event.type) {
			case OPENED:
				view.listView.setItems(handler.getProject().pages());
				break;
			case SELECTED:
				break;
			case MODIFIED:
				break;
			case PAGE_REMOVED:
				break;
			case CLOSING:
				view.listView.getItems().clear();
				break;
			case CLOSED:
				break;
			default:
				log.warn("unhandled project notification: {}", event.type);
				break;
		}
	}

	/**
	 * View of the pages widget.
	 */
	public static class View extends VBox implements Localizable {

		private final ListView<ProjectPage> listView = new ListView<>();
		private final ToggleGroup group = new ToggleGroup();
		private final ContextMenu contextMenu = new ContextMenu();
		private final BooleanProperty hasSelectionProperty = new SimpleBooleanProperty(false);
		private final InvalidationListener selectionListener = (obs) -> {
			hasSelectionProperty.set(
					!listView.getSelectionModel().isEmpty()
			);
		};

		/**
		 * Creates a new view of the pages widget.
		 *
		 * @param handler the application handler.
		 */
		public View(ApplicationHandler handler) {
			setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(listView, Priority.ALWAYS);

			listView.setEditable(true);
			listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			listView.setPrefHeight(0);
			listView.setMaxHeight(Double.MAX_VALUE);
			listView.setCellFactory((ListView<ProjectPage> param) -> new ProjectPageCell(handler, group));

			final MenuItem importItem = new MenuItem(localize("page.import"));
			importItem.setOnAction((e) -> {
				handler.eventBus.post(
						new ProjectRequest(ProjectRequest.Type.IMPORT_PAGES)
				);
			});

			final MenuItem deleteItem = new MenuItem(localize("page.delete.selected"));
			deleteItem.disableProperty().bind(Bindings.not(hasSelectionProperty));
			deleteItem.setOnAction((e) -> handler.getProject().deletePages(getSelectedItems()));

			contextMenu.getItems().addAll(importItem, deleteItem);
			listView.getSelectionModel().getSelectedItems().addListener(selectionListener);
			listView.setContextMenu(contextMenu);

			this.getChildren().addAll(listView);
		}

		/**
		 * Returns the selected project page.
		 *
		 * @return the selected project page.
		 */
		public final ProjectPage getSelectedItem() {
			return listView.getSelectionModel().getSelectedItem();
		}

		/**
		 * Returns a list of selected project pages.
		 *
		 * @return a list of selected project pages.
		 */
		public final List<ProjectPage> getSelectedItems() {
			// ye, we really need to make a copy, or otherwise feeding that list
			// back to listView (e.g. to delete those items) wont behave as expected...
			return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
		}
	}

	/**
	 * A project page cell.
	 */
	public static class ProjectPageCell extends DraggableListCell<ProjectPage> implements Localizable {

		private final ApplicationHandler handler;
		private final ToggleGroup group;
		private final BorderPane pane = new BorderPane();
		private final VBox vbox = new VBox();
		private final RadioButton radioButton = new RadioButton();
		private final Label label = new Label();
		private final TextField textField = new TextField();
		private final Label pipelineName = new Label();
		private final Label pipelinesLabel = FormGridPane.newLabel(localize("pipeline") + ":");
		private final FormGridPane grid = new FormGridPane();
		private ProjectPage currentPage;
		private ComboBox pipelines;
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
		 * Creates a new project page cell.
		 *
		 * @param handler the application handler.
		 * @param group the toogle group for all project pages (only one can be
		 * loaded/displayed at a time).
		 */
		public ProjectPageCell(ApplicationHandler handler, ToggleGroup group) {
			this.handler = handler;
			this.group = group;

			label.setAlignment(Pos.CENTER_LEFT);
			label.setMaxWidth(Double.MAX_VALUE);
			GridPane.setMargin(textField, new Insets(0, 0, 2, 0));
			textField.setOnKeyPressed(onEnterHandler);

			final int d = UIStrategyGUI.Stage.insets;
			radioButton.setPadding(new Insets(0, d, 0, 0));
			pipelineName.getStyleClass().add("dip-small");
			vbox.setSpacing(UIStrategyGUI.Stage.insets);
			vbox.getChildren().setAll(label, pipelineName);
			pane.setLeft(radioButton);
			pane.setCenter(vbox);
			grid.setPadding(new Insets(0, 0, d * 2, 0));
		}

		private boolean isPageSelected() {
			return handler.getProject().getSelectedPageId() == this.getItem().id;
		}

		private void updatePipelineLabel() {
			pipelineName.setText(String.format(
					"%s: %s",
					localize("pipeline"),
					currentPage.getPipelineName()
			));
		}

		@Override
		public final void updateItem(ProjectPage item, boolean empty) {
			super.updateItem(item, empty);

			setText(null);
			setGraphic(null);

			currentPage = item;

			if (!empty) {

				radioButton.setUserData(this.getItem().id);
				radioButton.setToggleGroup(group);
				radioButton.setSelected(isPageSelected());

				label.setText(item.getName());
				updatePipelineLabel();

				setGraphic(pane);
			}
		}

		@Override
		public final void startEdit() {
			super.startEdit();

			getListView().addEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			getListView().addEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			textField.setText(label.getText());
			pipelines = handler.getProject().pipelineManager().getComboBox();

			// item's hashCode is the pipeline id!
			final int id = currentPage.getPipelineId();
			final PipelineManager.PipelineItem item = new PipelineManager.PipelineItem(id, "");

			pipelines.getSelectionModel().select(item);
			pipelines.setOnKeyPressed(onEnterHandler);
			grid.clear();
			grid.addSpanRow(textField, 2);
			grid.addRow(pipelinesLabel, pipelines);

			pane.setCenter(grid);

			// this seems to be necessary since we ommit commitEdit and just
			// use cancelEdit. Might also be a bug, who knows...
			getListView().layout();
		}

		@Override
		public final void cancelEdit() {
			super.cancelEdit();

			getListView().removeEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			getListView().removeEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			final String tf = textField.getText();
			if (!tf.equals(label.getText())) {
				label.setText(textField.getText());
				currentPage.setName(textField.getText());
			}

			if (pipelines != null) {
				final PipelineManager.PipelineItem selected = (PipelineManager.PipelineItem) pipelines.getSelectionModel().getSelectedItem();
				if (selected != null) {
					if (currentPage.getPipelineId() != selected.id) {
						// TODO: ask for confirmation!
						currentPage.setPipelineId(selected.id);
						updatePipelineLabel();
					}
				}
			}

			pipelines = null;
			pane.setCenter(vbox);

			getListView().layout();
		}
	}

}
