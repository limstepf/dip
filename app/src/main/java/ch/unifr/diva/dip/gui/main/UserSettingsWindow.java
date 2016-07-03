package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PipelineLayoutStrategy;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.layout.AbstractWindow;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.gui.layout.Lane;
import ch.unifr.diva.dip.gui.pe.ConnectionView;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

/**
 * User settings window.
 */
public class UserSettingsWindow extends AbstractWindow implements Presenter {

	private final ApplicationHandler handler;
	private final SplitPane splitPane;
	private final BorderPane contentPane;
	private final static Insets contentPaneInsets = new Insets(10, 10, 10, 10);
	private final ObservableList<Category> categories;
	private final ListView<Category> listView;
	private final Node controls;

	/**
	 * Creates a new user settings window.
	 *
	 * @param owner owner/parent window.
	 * @param handler the application handler.
	 */
	public UserSettingsWindow(Window owner, ApplicationHandler handler) {
		super(owner, L10n.getInstance().getString("settings"));

		this.handler = handler;
		this.categories = newCategories();
		this.listView = newListView(this.categories);
		this.contentPane = newContentPane();
		this.controls = newControls();
		this.contentPane.setBottom(this.controls);
		this.splitPane = newSplitPane(this.listView, this.contentPane);
		this.root.setCenter(this.splitPane);

		this.listView.getSelectionModel().selectFirst();
		this.listView.getSelectionModel().selectedItemProperty().addListener(categoryListener);
		categoryCallback();
	}

	/**
	 * Configures and returns the list of user setting categories. Just add
	 * categories as needed, and fill 'em with items backed by DIP API
	 * parameters.
	 *
	 * @return list of user setting categories.
	 */
	private ObservableList<Category> newCategories() {
		final ObservableList<Category> cats = FXCollections.observableArrayList();

		/* General settings */
		final Category general = new Category(L10n.getInstance().getString("general"));

		// locale/language
		general.addItem(new Item<String>() {
			@Override
			public PersistentParameter parameter() {
				if (this.parameter == null) {
					this.parameter = new EnumParameter(
							L10n.getInstance().getString("language"),
							new ArrayList<>(L10n.availableLanguages()),
							handler.settings.locale.language
					);
				}
				return this.parameter;
			}

			@Override
			public void save() {
				handler.settings.locale.language = this.parameter.get();
			}
		});

		cats.add(general);

		/* pipeline editor settings */
		final Category pe = new Category(L10n.getInstance().getString("pipeline.editor"));

		// connection-view/wire type
		pe.addItem(new Item<String>() {
			@Override
			public PersistentParameter parameter() {
				if (this.parameter == null) {
					this.parameter = new EnumParameter(
							L10n.getInstance().getString("pipeline.connection.type"),
							ConnectionView.Type.class,
							handler.settings.pipelineEditor.connectionType
					);
				}
				return this.parameter;
			}

			@Override
			public void save() {
				handler.settings.pipelineEditor.connectionType = this.parameter.get();
			}
		});

		// pipeline layout strategy (or main direction)
		pe.addItem(new Item<String>() {
			@Override
			public PersistentParameter parameter() {
				if (this.parameter == null) {
					this.parameter = new EnumParameter(
							L10n.getInstance().getString("pipeline.layout.strategy.default"),
							PipelineLayoutStrategy.class,
							handler.settings.pipelineEditor.pipelineLayout
					);
				}
				return this.parameter;
			}

			@Override
			public void save() {
				handler.settings.pipelineEditor.pipelineLayout = this.parameter.get();
			}
		});
		cats.add(pe);

		return cats;
	}

	private final InvalidationListener categoryListener = (c) -> categoryCallback();

	private void categoryCallback() {
		final Category category = this.listView.getSelectionModel().getSelectedItem();
		this.contentPane.setCenter(category.getContentPane());
	}

	private BorderPane newContentPane() {
		final BorderPane pane = new BorderPane();
		return pane;
	}

	private SplitPane newSplitPane(Node... nodes) {
		final SplitPane sp = new SplitPane();
		sp.getItems().setAll(nodes);
		sp.setDividerPositions(0.21);
		return sp;
	}

	private ListView<Category> newListView(ObservableList<Category> items) {
		final ListView<Category> lv = new ListView<>();

		lv.setEditable(false);
		lv.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		lv.setPrefHeight(0);
		lv.setMaxHeight(Double.MAX_VALUE);
		lv.setCellFactory((ListView<Category> cat) -> new CategoryListCell());
		lv.setItems(items);

		return lv;
	}

	private Node newControls() {
		final Lane lane = new Lane();
		lane.setAlignment(Pos.CENTER_RIGHT);
		lane.setPadding(new Insets(0, 0, 0, 10));

		final Button apply = new Button(L10n.getInstance().getString("apply"));
		apply.setOnAction((c) -> apply());

		final Button cancel = new Button(L10n.getInstance().getString("cancel"));
		cancel.setOnAction((c) -> cancel());

		lane.getChildren().addAll(apply, cancel);
		BorderPane.setMargin(lane, contentPaneInsets);
		return lane;
	}

	// save and exit
	private void apply() {
		for (Category category : this.categories) {
			if (category.hasBeenInitialized()) {
				for (Item item : category.items) {
					if (item.hasBeenInitialized()) {
						item.save();
					}
				}
			}
		}
		this.handler.saveUserSettings();
		this.close();
	}

	// exit without saving
	private void cancel() {
		this.close();
	}

	@Override
	public Parent getComponent() {
		return this.root;
	}

	/**
	 * Category list cell.
	 */
	public static class CategoryListCell extends ListCell<Category> {

		@Override
		public final void updateItem(Category cat, boolean empty) {
			super.updateItem(cat, empty);

			setText(empty ? null : cat.label);
			setGraphic(null);
		}
	}

	/**
	 * User settings category.
	 */
	public static class Category {

		public final String label;
		public final List<Item> items;
		private Node contentPane;

		/**
		 * Creates a new user settings category.
		 *
		 * @param label label of the category.
		 */
		public Category(String label) {
			this.label = label;
			this.items = new ArrayList<>();
		}

		/**
		 * Adds an item to the category.
		 *
		 * @param item a user settings item.
		 */
		public void addItem(Item item) {
			this.items.add(item);
		}

		// only save a category if it actually has been initialized/used
		public boolean hasBeenInitialized() {
			return this.contentPane != null;
		}

		public Node getContentPane() {
			if (contentPane == null) {
				this.contentPane = newContentPane();
				BorderPane.setMargin(this.contentPane, contentPaneInsets);
			}

			return this.contentPane;
		}

		private Node newContentPane() {
			final FormGridPane grid = new FormGridPane();

			for (Item item : this.items) {
				final Label title = new Label(item.parameter().label());
				title.getStyleClass().add("dip-small");
				grid.addRow(
						title,
						item.parameter().view().node()
				);
			}

			return grid;
		}

	}

	/**
	 * Settings item interface.
	 *
	 * @param <T> value type of the persistent parameter.
	 */
	public static abstract class Item<T> {

		protected PersistentParameter<T> parameter;

		// only save an item if it actually has been initialized/used
		public boolean hasBeenInitialized() {
			return this.parameter != null;
		}

		// lazy initialization
		public abstract PersistentParameter<T> parameter();

		// first check with hasBeenInitialized, or face a possible NPE
		public abstract void save();
	}

}
