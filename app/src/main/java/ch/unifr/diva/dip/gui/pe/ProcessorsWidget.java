package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.KeyEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler.ClickCount;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.AbstractWidget;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.osgi.OSGiService;
import ch.unifr.diva.dip.osgi.OSGiServiceCollection;
import ch.unifr.diva.dip.osgi.ServiceMonitor;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ProcessorsWidget offers processors to be dragged into the pane of the
 * pipeline editor.
 *
 * <p>
 * TODO: a TreeView (level 1: bundle/category, level 2: processor) might work
 * better here, eh?
 */
public class ProcessorsWidget extends AbstractWidget {

	private static final Logger log = LoggerFactory.getLogger(ProcessorsWidget.class);
	private final View view;
	private final ServiceMonitor services;

	/**
	 * Creates a new processor widget.
	 *
	 * @param label label of the widget.
	 * @param services the processor service monitor.
	 */
	public ProcessorsWidget(String label, ServiceMonitor services) {
		super();

		this.services = services;
		this.view = new View(services);

		setWidget(view);
		setTitle(label);
	}

	/**
	 * Disables/enables the widget.
	 *
	 * @param disable True to disable, False to (re-)enable.
	 */
	public void setDisable(boolean disable) {
		this.view.setDisable(disable);
	}

	/**
	 * Processor list cell.
	 */
	public static class ProcessorListCell extends ListCell<OSGiServiceCollection<Processor>> {

		/**
		 * DataFormat for an OSGi service reference.
		 */
		public static final DataFormat OSGI_SERVICE_PROCESSOR = new DataFormat("dip-service/processor");

		private final BorderPane pane = new BorderPane();
		private final VBox vbox = new VBox();
		private final Label name = new Label();
		private final Label description = new Label();
		private final ComboBox version = new ComboBox();
		private final Label versionLabel = new Label();
		private NamedGlyph namedGlyph;
		private Glyph glyph;
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
		private OSGiServiceCollection<Processor> currentCollection;

		/**
		 * Creates a new ProcessorListCell.
		 */
		public ProcessorListCell() {
			name.setMaxWidth(Double.MAX_VALUE);
			name.setAlignment(Pos.CENTER_LEFT);
			description.setMaxWidth(Double.MAX_VALUE);
			description.setAlignment(Pos.CENTER_LEFT);
			description.setTextOverrun(OverrunStyle.ELLIPSIS);
			description.setMinWidth(0);
			description.setPrefWidth(15);
			description.getStyleClass().add("dip-small");
			version.getStyleClass().add("dip-small");
			versionLabel.getStyleClass().add("dip-small");
			final BorderPane titleBox = new BorderPane();
			titleBox.setCenter(name);
			final BorderPane descriptionBox = new BorderPane();
			descriptionBox.setCenter(description);
			descriptionBox.setRight(versionLabel);
			vbox.setPadding(new Insets(0, 0, 0, UIStrategyGUI.Stage.insets + 2));
			vbox.getChildren().setAll(titleBox, descriptionBox);
			pane.setCenter(vbox);

			setupDraggable();
		}

		private void setupDraggable() {
			this.setOnDragDetected(e -> onDragDetected(e));
			this.setOnDragDone(e -> onDragDone(e));
		}

		private void onDragDetected(MouseEvent e) {
			final ListView<OSGiServiceCollection<Processor>> listView = this.getListView();
			final int n = listView.getSelectionModel().getSelectedIndices().size();
			if (n == 0 || getItem() == null) {
				e.consume();
				return;
			}

			final Dragboard db = this.startDragAndDrop(TransferMode.COPY);
			final ClipboardContent content = new ClipboardContent();

			db.setDragView(getDragView());

			final int index = listView.getSelectionModel().getSelectedIndex();
			content.put(
					OSGI_SERVICE_PROCESSOR,
					listView.getItems().get(index).getFxContext().getOSGiServiceReference()
			);
			db.setContent(content);

			e.consume();
		}

		private void onDragDone(DragEvent e) {
			e.consume();
		}

		private final static SnapshotParameters snapshotParams;

		static {
			snapshotParams = new SnapshotParameters();
			snapshotParams.setFill(Color.TRANSPARENT);
		}

		private Image getDragView() {
			final Glyph g = UIStrategyGUI.Glyphs.newGlyph(namedGlyph, Glyph.Size.HUGE);
			g.setBackground(Color.TRANSPARENT);
			g.setColor(UIStrategyGUI.Colors.accent_inverted);
			final Scene s = new Scene(g, Color.TRANSPARENT);
			final WritableImage image = g.snapshot(snapshotParams, null);
			return image;
		}

		private static Insets glyphInsets = new Insets(2, 0, 0, 0);

		private Glyph getGlyph(Processor p) {
			namedGlyph = ProcessorWrapper.glyph(p);
			glyph = UIStrategyGUI.Glyphs.newGlyph(namedGlyph, Glyph.Size.MEDIUM);
			updateColor();
			BorderPane.setMargin(glyph, glyphInsets);
			BorderPane.setAlignment(glyph, Pos.TOP_CENTER);
			return glyph;
		}

		private final InvalidationListener glyphListener = (c) -> updateColor();

		private void updateColor() {
			if (isFocused() || isSelected()) {
				glyph.setColor(UIStrategyGUI.Colors.accent_inverted);
			} else {
				glyph.setColor(UIStrategyGUI.Colors.accent);
			}
		}

		@Override
		public final void updateItem(OSGiServiceCollection<Processor> collection, boolean empty) {
			super.updateItem(collection, empty);
			setText(null);
			setGraphic(null);

			currentCollection = collection;

			this.focusedProperty().removeListener(glyphListener);
			this.selectedProperty().removeListener(glyphListener);

			if (!empty) {
				this.focusedProperty().addListener(glyphListener);
				this.selectedProperty().addListener(glyphListener);

				final OSGiServiceCollection.FxContext<Processor> fx = collection.getFxContext();
				final OSGiService<Processor> s = fx.getSelectedVersion();

				name.setText(s.serviceObject.name());
				description.setText(collection.pid());
				setVersionLabelText(collection);

				pane.setLeft(getGlyph(s.serviceObject));
				setGraphic(pane);

			}
		}

		private void setVersionLabelText(OSGiServiceCollection<Processor> collection) {
			final String v = collection.numVersions() > 0
					? getVersionLabelText(collection)
					: "-";
			versionLabel.setText(v);
		}

		// don't show the qualifier, since it's pretty much meaningless...
		// Also it will still show up in the combo box, so we wont miss anything.
		private String getVersionLabelText(OSGiServiceCollection<Processor> collection) {
			final Version v = collection.getFxContext().getSelectedVersion().version;
			return String.format("%d.%d.%d", v.getMajor(), v.getMinor(), v.getMicro());
		}
		private ProcessorCellEditBox editBox;

		private ProcessorCellEditBox editBox() {
			if (editBox == null) {
				editBox = new ProcessorCellEditBox(onEnterHandler);
			}
			return editBox;
		}

		@Override
		public final void startEdit() {
			super.startEdit();

			getListView().addEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			getListView().addEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			final ProcessorCellEditBox edit = editBox();
			edit.init(currentCollection);

			versionLabel.setVisible(false);
			vbox.getChildren().add(edit);
			getListView().layout();
		}

		@Override
		public final void cancelEdit() {
			super.cancelEdit();

			getListView().removeEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			getListView().removeEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			final ProcessorCellEditBox edit = editBox();
			final OSGiServiceCollection.FxContext<Processor> fx = currentCollection.getFxContext();

			final int versionIndex = edit.getSelectedVersionIndex();
			if (versionIndex != fx.getSelctedIndex()) {
				fx.setSelectedVersion(versionIndex);
				setVersionLabelText(currentCollection);
			}
			versionLabel.setVisible(true);

			vbox.getChildren().remove(edit);
			getListView().layout();
		}
	}

	/**
	 * ProcessorCellEditBox.
	 */
	public static class ProcessorCellEditBox extends VBox {

		private final FormGridPane grid;
		private final Label versionLabel;
		private final ComboBox versionList;

		/**
		 * Creates a new processor cell edit box.
		 *
		 * @param onEnterHandler the enter handler to close and save/update.
		 */
		public ProcessorCellEditBox(EventHandler<KeyEvent> onEnterHandler) {
			final int b = UIStrategyGUI.Stage.insets * 2;
			this.grid = new FormGridPane();
			this.grid.setPadding(new Insets(b, 0, b, 0));
			this.versionLabel = FormGridPane.newLabel(L10n.getInstance().getString("version") + ":");
			this.versionList = new ComboBox();
			versionList.getStyleClass().add("dip-small");
			versionList.setOnKeyPressed(onEnterHandler);
			grid.addRow(versionLabel, versionList);

			this.setSpacing(UIStrategyGUI.Stage.insets);
			this.getChildren().setAll(grid);
		}

		/**
		 * Init the edit box with a new service collection.
		 *
		 * @param collection the service collection.
		 */
		public void init(OSGiServiceCollection<Processor> collection) {
			versionList.setItems(collection.getFxContext().getVersionList());
			versionList.getSelectionModel().select(collection.getFxContext().getSelctedIndex());

			versionList.setDisable(collection.numVersions() < 2);
		}

		/**
		 * Returns the index of the selected version.
		 *
		 * @return the index of the selected version.
		 */
		public int getSelectedVersionIndex() {
			return versionList.getSelectionModel().getSelectedIndex();
		}

	}

	/**
	 * Processors widget view.
	 */
	public static class View extends VBox {

		private final ListView<OSGiServiceCollection<Processor>> listView = new ListView<>();

		/**
		 * Creates a new processors widget view.
		 *
		 * @param services the service monitor.
		 */
		public View(ServiceMonitor services) {
			setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(listView, Priority.ALWAYS);

			listView.setEditable(true);
			listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			listView.setPrefHeight(0);
			listView.setMaxHeight(Double.MAX_VALUE);
			listView.setCellFactory((ListView<OSGiServiceCollection<Processor>> param) -> new ProcessorListCell());

			listView.setItems(services.getServiceCollectionList());
			listView.getSelectionModel().select(-1);
			this.getChildren().addAll(listView);
		}
	}

}
