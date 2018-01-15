package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.KeyEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler.ClickCount;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PipelineManager;
import ch.unifr.diva.dip.core.model.Project;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import ch.unifr.diva.dip.eventbus.events.ProjectRequest;
import ch.unifr.diva.dip.gui.AbstractWidget;
import ch.unifr.diva.dip.gui.layout.DraggableListCell;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
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
				view.selectItem(handler.getProject().getPage(event.page));
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
		private final BooleanBinding hasNoSelectionBinding = Bindings.not(hasSelectionProperty);
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

			final MenuItem changePipelineItem = new MenuItem(localize("page.change.pipeline.selected"));
			changePipelineItem.disableProperty().bind(hasNoSelectionBinding);
			changePipelineItem.setOnAction((e) -> {
				final ChangePipelineDialog dialog = new ChangePipelineDialog(
						handler,
						getSelectedItems()
				);
				dialog.showAndWait();
			});

			final MenuItem deleteItem = new MenuItem(localize("page.delete.selected"));
			deleteItem.disableProperty().bind(hasNoSelectionBinding);
			deleteItem.setOnAction((e) -> handler.getProject().deletePages(getSelectedItems()));

			contextMenu.getItems().addAll(importItem, changePipelineItem, deleteItem);
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

		/**
		 * Marks the item/page as selected. Used as a callback in case a page got
		 * selected over the eventbus (as opposed to the user clicking the radio button).
		 * @param page the item/page to mark as select.
		 */
		public final void selectItem(ProjectPage page) {
			if (page == null) {
				return;
			}
			final ProjectPage selectedPage = listView.getSelectionModel().getSelectedItem();
			if (selectedPage != null) {
				if (page.id == selectedPage.id) {
					return;
				}
			}

			listView.scrollTo(page);
			listView.getSelectionModel().select(page);
		}

	}

	/**
	 * A project page cell.
	 */
	public static class ProjectPageCell extends DraggableListCell<ProjectPage> implements Localizable {

		private static final ColumnConstraints[] cc;
		private static final RowConstraints rc;

		static {
			cc = new ColumnConstraints[]{
				new ColumnConstraints(),
				new ColumnConstraints()
			};
			cc[0].setMinWidth(40);
			cc[0].setHgrow(Priority.SOMETIMES);
			cc[1].setHgrow(Priority.ALWAYS);
			rc = new RowConstraints();
			rc.setValignment(VPos.TOP);
		}
		private final ApplicationHandler handler;
		private final ToggleGroup group;
		private final EventHandler<KeyEvent> onEnterHandler;
		private final EventHandler<MouseEvent> onDoubleClickHandler;
		private final InvalidationListener selectionListener;
		private final InvalidationListener pipelineListener;
		private final BorderPane pane;
		private final RadioButton radioButton;
		private final FormGridPane grid;

		private final Label pageName;
		private final Label pageDescription;
		private final Glyph expandGlyph;
		private final BorderPane pagePane;

		private final TextField pageNameField;
		private final Label fileLabel;
		private final Label fileDescription;
		private final Label imageLabel;
		private final Label imageDescription;
		private final Label pipelineLabel;

		private ComboBox<PipelineManager.PipelineItem> pipelineCombo;
		private ProjectPage currentPage;

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
			this.onEnterHandler = new KeyEventHandler(
					KeyCode.ENTER,
					(e) -> doCancelEdit(e)
			);
			this.onDoubleClickHandler = new MouseEventHandler(
					ClickCount.DOUBLE_CLICK,
					(e) -> doCancelEdit(e)
			);
			this.pipelineListener = (c) -> onUpdatePipeline();
			this.selectionListener = (c) -> onUpdateSelection();
			this.selectedProperty().addListener(selectionListener);

			final int d = UIStrategyGUI.Stage.insets;
			this.radioButton = new RadioButton();
			radioButton.setPadding(new Insets(0, d, 0, 0));
			this.grid = new FormGridPane(0, 1);
			grid.setMaxWidth(Double.MAX_VALUE);
			grid.setHgap(d);
			this.pane = new BorderPane();
			pane.setMaxWidth(Double.MAX_VALUE);
			pane.setLeft(radioButton);
			pane.setCenter(grid);

			this.pageName = newLabel("", false, true);
			this.pageDescription = newLabel();
			this.expandGlyph = UIStrategyGUI.Glyphs.newGlyph(
					MaterialDesignIcons.CHEVRON_DOWN,
					Glyph.Size.NORMAL
			);
			expandGlyph.setAlignment(Pos.CENTER_RIGHT);
			expandGlyph.disabledHoverEffectProperty().set(true);
			this.pagePane = new BorderPane();
			GridPane.setFillWidth(pagePane, Boolean.TRUE);
			GridPane.setHgrow(pagePane, Priority.ALWAYS);
			BorderPane.setAlignment(pageName, Pos.CENTER_LEFT);
			BorderPane.setAlignment(expandGlyph, Pos.CENTER_RIGHT);
			pagePane.setCenter(pageName);
			pagePane.setRight(expandGlyph);

			this.pageNameField = new TextField();
			this.fileLabel = newLabel(localize("file") + ":", true, false);
			this.fileDescription = newLabel();
			this.imageLabel = newLabel(localize("image") + ":", true, false);
			this.imageDescription = newLabel();
			this.pipelineLabel = newLabel(localize("pipeline") + ":", true, false);
		}

		private void onUpdateSelection() {
			radioButton.setSelected(isPageSelected());
		}

		private void onUpdatePipeline() {
			setPageDescription();
		}

		private void setPageDescription() {
			pageDescription.setText(String.format(
					"%d\u00D7%d\u00D7%d, %s (%s)",
					currentPage.getWidth(),
					currentPage.getHeight(),
					currentPage.getNumBands(),
					currentPage.getPipelineName(),
					currentPage.getState().name()
			));
		}

		private void initGrid() {
			grid.clear();
			grid.getColumnConstraints().setAll(cc);
			grid.getRowConstraints().setAll(rc);
		}

		private void setDefaultGrid() {
			pageName.setText(currentPage.getName());
			setPageDescription();

			initGrid();
			grid.addSpanRow(pagePane, 2);
			grid.addSpanRow(pageDescription, 2);
		}

		private void setEditGrid() {
			pageNameField.setText(pageName.getText());
			fileDescription.setText(currentPage.file.toString());
			imageDescription.setText(String.format(
					"%d\u00D7%d\u00D7%d",
					currentPage.getWidth(),
					currentPage.getHeight(),
					currentPage.getNumBands()
			));
			pipelineCombo = handler.getProject().pipelineManager().getComboBox();
			pipelineCombo.getStyleClass().add("dip-small");
			// item's hashCode is the pipeline id!
			final int id = currentPage.getPipelineId();
			final PipelineManager.PipelineItem item = new PipelineManager.PipelineItem(id, "");
			pipelineCombo.getSelectionModel().select(item);
			pipelineCombo.setOnKeyPressed(onEnterHandler);

			initGrid();
			grid.addSpanRow(pageNameField, 2);
			grid.addRow(fileLabel, fileDescription);
			grid.addRow(imageLabel, imageDescription);
			grid.addRow(pipelineLabel, pipelineCombo);
		}

		private void updateChangedValues() {
			final String tf = pageNameField.getText();
			if (!tf.equals(pageName.getText())) {
				pageName.setText(pageNameField.getText());
				currentPage.setName(pageNameField.getText());
			}

			if (pipelineCombo != null) {
				final PipelineManager.PipelineItem selected = pipelineCombo.getSelectionModel().getSelectedItem();
				if (selected != null) {
					if (currentPage.getPipelineId() != selected.id) {
						currentPage.setPipelineId(selected.id);
						onUpdatePipeline();
					}
				}
				pipelineCombo = null;
			}
		}

		private static Label newLabel() {
			return newLabel("", true, true);
		}

		private static Label newLabel(String text) {
			return newLabel(text, true, true);
		}

		private static Label newLabel(String text, boolean small, boolean wrap) {
			final Label label = new Label(text);
			if (wrap) {
				label.setWrapText(true);
			}
			if (small) {
				label.getStyleClass().add("dip-small");
			}
			return label;
		}

		private <E extends Event> boolean doCancelEdit(E event) {
			cancelEdit();
			return true;
		}

		private boolean isPageSelected() {
			final Project project = handler.getProject();
			final ProjectPage item = getItem();
			if (project == null || item == null) {
				return false;
			}
			return project.getSelectedPageId() == item.id;
		}

		@Override
		public final void updateItem(ProjectPage item, boolean empty) {
			super.updateItem(item, empty);

			setText(null);
			setGraphic(null);

			if (currentPage != null) {
				currentPage.stateProperty().removeListener(pipelineListener);
				currentPage.pipelineIdProperty().removeListener(pipelineListener);
				currentPage.pipelineNameProperty().removeListener(pipelineListener);
			}
			currentPage = item;

			if (!empty) {
				currentPage.pipelineIdProperty().addListener(pipelineListener);
				currentPage.pipelineNameProperty().addListener(pipelineListener);
				currentPage.stateProperty().addListener(pipelineListener);
				radioButton.setUserData(this.getItem().id);
				radioButton.setToggleGroup(group);
				radioButton.setSelected(isPageSelected());
				setDefaultGrid();
				setGraphic(pane);

				// manage pane width to get wrapping text
				pane.prefWidthProperty().bind(getListView().widthProperty().subtract(15));
			} else {
				pane.prefWidthProperty().unbind();
			}
		}

		@Override
		public final void startEdit() {
			super.startEdit();

			getListView().addEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			getListView().addEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			setEditGrid();
		}

		@Override
		public final void cancelEdit() {
			super.cancelEdit();

			getListView().removeEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			getListView().removeEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			updateChangedValues();
			setDefaultGrid();
		}

	}

}
