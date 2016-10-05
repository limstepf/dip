package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.api.parameters.BooleanParameter;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PipelineLayoutStrategy;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.eventbus.events.ApplicationRequest;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.layout.AbstractWindow;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.gui.layout.Lane;
import ch.unifr.diva.dip.gui.layout.ZoomPane;
import ch.unifr.diva.dip.gui.pe.ConnectionView;
import ch.unifr.diva.dip.osgi.OSGiVersionPolicy;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
		final Category general = new Category(localize("general"));

		// locale/language
		general.addItem(new Item<EnumParameter>() {
			@Override
			public PersistentParameter parameter() {
				if (this.parameter == null) {
					this.parameter = new EnumParameter(
							localize("language"),
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

		// default processor/OSGi service version (or auto-upgrade) policy
		general.addItem(new Item<EnumParameter>() {
			@Override
			public PersistentParameter parameter() {
				if (this.parameter == null) {
					this.parameter = new EnumParameter(
							localize("processor.version.policy.default"),
							OSGiVersionPolicy.class,
							(e) -> {
								if (e.equals(OSGiVersionPolicy.getDefault())) {
									return String.format(
											"%s (%s)",
											e.name(),
											localize("default")
									);
								}
								return e.name();
							},
							handler.settings.osgi.versionPolicy.name()
					);
				}
				return this.parameter;
			}

			@Override
			public void save() {
				handler.settings.osgi.versionPolicy = OSGiVersionPolicy.get(this.parameter.get());
			}
		});

		cats.add(general);

		/* main/pixel editor settings */
		final Category me = new Category(localize("editor.main"));

		// display/zoom interpolation method/algorithm
		me.addItem(new Item<EnumParameter>() {
			@Override
			public PersistentParameter parameter() {
				if (this.parameter == null) {
					this.parameter = new EnumParameter(
							localize("interpolation"),
							ZoomPane.Interpolation.class,
							handler.settings.editor.interpolation
					);
				}
				return this.parameter;
			}

			@Override
			public void save() {
				handler.settings.editor.interpolation = this.parameter.get();
				handler.eventBus.post(
						new ApplicationRequest(ApplicationRequest.Type.EDITOR_INTERPOLATION)
				);
			}
		});

		cats.add(me);

		/* pipeline editor settings */
		final Category pe = new Category(localize("pipeline.editor"));

		// connection-view/wire type
		pe.addItem(new Item<EnumParameter>() {
			@Override
			public PersistentParameter parameter() {
				if (this.parameter == null) {
					this.parameter = new EnumParameter(
							localize("pipeline.connection.type"),
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
		pe.addItem(new Item<EnumParameter>() {
			@Override
			public PersistentParameter parameter() {
				if (this.parameter == null) {
					this.parameter = new EnumParameter(
							localize("pipeline.layout.strategy.default"),
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

		// auto rearrange processor views on layout strategy change and/or
		// processor view un-/folding (or editing).
		pe.addItem(new Item<CompositeGrid>() {
			@Override
			public PersistentParameter parameter() {
				if (this.parameter == null) {
					final LabelParameter layoutLabel = new LabelParameter(localize("pipeline.auto.rearrange.layout") + ": ");
					final LabelParameter foldLabel = new LabelParameter(localize("pipeline.auto.rearrange.fold") + ": ");
					final BooleanParameter rearrangeOnLayout = new BooleanParameter(
							"",
							handler.settings.pipelineEditor.autoRearrangeOnChangedLayout,
							localize("yes"),
							localize("no")
					);
					final BooleanParameter rearrangeOnFold = new BooleanParameter(
							"",
							handler.settings.pipelineEditor.autoRearrangeOnProcessorFold,
							localize("yes"),
							localize("no")
					);
					this.parameter = new CompositeGrid(
							localize("pipeline.auto.rearrange"),
							layoutLabel, rearrangeOnLayout,
							foldLabel, rearrangeOnFold
					);
					this.parameter.setColumnConstraints(2);
					this.parameter.getColumnConstraints().get(0).setHgrow(Priority.ALWAYS);
				}
				return this.parameter;
			}

			@Override
			public void save() {
				handler.settings.pipelineEditor.autoRearrangeOnChangedLayout = (boolean) this.parameter.get().get(0);
				handler.settings.pipelineEditor.autoRearrangeOnProcessorFold = (boolean) this.parameter.get().get(1);
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

		final Button apply = new Button(localize("apply"));
		apply.setOnAction((c) -> apply());

		final Button cancel = new Button(localize("cancel"));
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

		/**
		 * The label/name of the category.
		 */
		public final String label;

		/**
		 * List of items in the category.
		 */
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

		/**
		 * Checks whether the category has been initialized (viewed/opened).
		 * Used to only save a category if it actually has been
		 * initialized/used.
		 *
		 * @return True if the category has been initialized, False otherwise.
		 */
		public boolean hasBeenInitialized() {
			return this.contentPane != null;
		}

		/**
		 * Returns the content pane.
		 *
		 * @return the content pane.
		 */
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
				GridPane.setMargin(
						item.parameter().view().node(),
						new Insets(0, 0, UIStrategyGUI.Stage.insets, 0)
				);
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
	 * @param <T> type of the persistent parameter.
	 */
	public static abstract class Item<T extends PersistentParameter> {

		protected T parameter;

		/**
		 * Checks whether the item has been initialized (viewed/opened). Used to
		 * only save an item if it actually has been initialized/used.
		 *
		 * @return True if the item has been initialized, False otherwise.
		 */
		public boolean hasBeenInitialized() {
			return this.parameter != null;
		}

		/**
		 * Returns the parameter backing the item. Lazily initialized!
		 *
		 * @return the parameter backing the item.
		 */
		public abstract PersistentParameter<T> parameter();

		/**
		 * Saves the item. First check if the item {@code hasBeenInitialized},
		 * or face a possible NPE.
		 */
		public abstract void save();
	}

}
