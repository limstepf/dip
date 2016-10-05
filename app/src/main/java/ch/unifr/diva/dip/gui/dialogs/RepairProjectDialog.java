package ch.unifr.diva.dip.gui.dialogs;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.RadioChoiceBox;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PipelineData;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.core.model.ProjectData;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getCancelButton;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getDefaultButton;
import ch.unifr.diva.dip.gui.layout.ArrowHead;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.gui.layout.HLine;
import ch.unifr.diva.dip.gui.layout.Lane;
import ch.unifr.diva.dip.gui.layout.Listable;
import ch.unifr.diva.dip.osgi.OSGiFramework;
import ch.unifr.diva.dip.osgi.OSGiService;
import ch.unifr.diva.dip.osgi.ServiceCollection;
import ch.unifr.diva.dip.utils.FileFinderService;
import ch.unifr.diva.dip.utils.FileFinderTask;
import ch.unifr.diva.dip.utils.IOUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repair project dialog. Select/search paths to missing files, replace
 * unavailable services used in the project, such stuff...
 */
public class RepairProjectDialog extends AbstractDialog {

	private static final Logger log = LoggerFactory.getLogger(RepairProjectDialog.class);
	private final ApplicationHandler handler;
	private final ProjectData projectData;
	private final ProjectData.ValidationResult validation;
	private final List<RepairSection> sections = new ArrayList<>();
	private final VBox box = new VBox();
	private final Button ok = getDefaultButton(localize("ok"));
	private final Button cancel = getCancelButton(stage);
	private final BooleanProperty repairedProperty = new SimpleBooleanProperty(false);
	private final InvalidationListener repairedListener = (obs) -> updateRepairedProperty();
	private RepairImageSection repairImageSection = null;
	private boolean done = false;

	/**
	 * Creates a new repair project dialog.
	 *
	 * @param owner the window owner of the dialog.
	 * @param handler the application handler.
	 */
	public RepairProjectDialog(Window owner, ApplicationHandler handler) {
		super(owner);

		this.handler = handler;
		this.projectData = handler.getRepairData();
		this.validation = handler.getRepairValidation();

		setTitle(localize("project.open.warning"));

		box.setPrefWidth(owner.getWidth() * .78);
		box.setPrefHeight(owner.getHeight() * .42);
		box.setSpacing(10);

		if (!(validation.missingImages.isEmpty() && validation.movedImages.isEmpty())) {
			repairImageSection = new RepairImageSection(stage, projectData, validation);
			sections.add(repairImageSection);
		}
		if (!validation.missingServices.isEmpty()) {
			sections.add(new RepairServiceSection(handler, stage, projectData, validation));
		}

		for (RepairSection section : sections) {
			section.repairedProperty().addListener(repairedListener);
			VBox.setVgrow(section.getComponent(), Priority.ALWAYS);
			box.getChildren().add(section.getComponent());
		}

		ok.setDisable(true);
		ok.disableProperty().bind(Bindings.not(repairedProperty));
		ok.setOnAction((e) -> {
			applyRepairs();
			this.done = true;
			stage.hide();
		});
		cancel.setOnAction((e) -> {
			cleanUpOnClose();
			stage.hide();
		});
		buttons.add(ok);
		buttons.add(cancel);

		this.setOnCloseRequest((e) -> cleanUpOnClose());

		root.setCenter(box);

		updateRepairedProperty();
	}

	private void cleanUpOnClose() {
		if (repairImageSection != null) {
			repairImageSection.shutdownFinder();
		}
	}

	private void updateRepairedProperty() {
		for (RepairSection section : sections) {
			if (!section.isRepaired()) {
				this.repairedProperty.set(false);
				return;
			}
		}
		this.repairedProperty.set(true);
	}

	public boolean isOk() {
		return this.done;
	}

	private void applyRepairs() {
		for (RepairSection section : sections) {
			section.applyRepairs();
		}
	}

	/**
	 * Interface for repair sections.
	 */
	private interface RepairSection {

		public Parent getComponent();

		public ReadOnlyBooleanProperty repairedProperty();

		default boolean isRepaired() {
			return repairedProperty().get();
		}

		public void applyRepairs();
	}

	/**
	 * Repair status interface.
	 */
	private interface RepairStatus {

		final public static Glyph.Size GLYPH_SIZE = Glyph.Size.SMALL;
		final public static String STATE_NOT_FOUND = L10n.getInstance().getString("state.notfound");
		final public static String STATE_MODIFIED = L10n.getInstance().getString("state.modified");
		final public static String STATE_MOVED = L10n.getInstance().getString("state.moved");
		//
		final public static String STATE_UNAVAILABLE = L10n.getInstance().getString("state.unavailable");
		final public static String STATE_UPGRADED = L10n.getInstance().getString("state.updowngraded");
		final public static String STATE_REPLACED = L10n.getInstance().getString("state.replaced");

		/**
		 * Returns the repair status message.
		 *
		 * @return the repair status message.
		 */
		public String getMessage();

		/**
		 * Returns the repair status glyph.
		 *
		 * @return the repair status glyph.
		 */
		public Glyph getGlyph();
	}

	/**
	 * Listable base class for items that need to be repaired, or ignored.
	 */
	private static abstract class ListableBase<T extends Parent> implements Listable, Localizable {

		private static final Insets margin = new Insets(UIStrategyGUI.Stage.insets, 0, UIStrategyGUI.Stage.insets, 0);
		protected final BorderPane parent;
		protected final Header header;
		protected final T body;
		protected final BooleanProperty repairedProperty = new SimpleBooleanProperty(false);
		protected final ChangeListener repairedListener = (obs, a, b) -> updateRepairedProperty();

		/**
		 * Creates a new listable base object.
		 *
		 * @param title the title of the listable object.
		 * @param body the body of the listable object.
		 */
		public ListableBase(String title, T body) {
			this.header = new Header(title, localize("ignore"));
			this.header.ignore.selectedProperty().addListener(repairedListener);
			this.body = body;
			this.parent = new BorderPane();
			this.parent.setTop(this.header.grid);
			this.parent.setCenter(body);
			BorderPane.setMargin(body, margin);
		}

		/**
		 * Header of a listable object.
		 */
		public static class Header {

			private final static Glyph.Size statusGlyphSize = Glyph.Size.SMALL;
			public final Label title;
			public final Label status;
			public final CheckBox ignore;
			public final FormGridPane grid;
			private Glyph glyph;

			/**
			 * Creates a new listable object header.
			 *
			 * @param title title of the listable object.
			 * @param ignore the ignore label.
			 */
			public Header(String title, String ignore) {
				this.title = new Label(title);
				this.title.getStyleClass().add("dip-title");
				this.status = new Label();
				this.ignore = new CheckBox(ignore);

				final int n = 4;
				final ColumnConstraints[] cc = new ColumnConstraints[n];
				for (int i = 0; i < n; i++) {
					cc[i] = new ColumnConstraints();
					cc[i].setHgrow(Priority.SOMETIMES);
					cc[i].setHalignment(HPos.LEFT);
				}
				cc[0].setHgrow(Priority.ALWAYS);
				cc[0].setPercentWidth(66.7);
				cc[1].setHgrow(Priority.NEVER);
				cc[1].setHalignment(HPos.RIGHT);
				cc[n - 1].setHalignment(HPos.RIGHT);

				this.grid = new FormGridPane(cc);
				setupGrid();
			}

			private void setupGrid() {
				this.grid.getChildren().clear();
				this.grid.add(this.title, 0, 0);
				// 1 reserved for status glyph
				this.grid.add(this.status, 2, 0);
				this.grid.add(this.ignore, 3, 0);
			}

			private void updateStatus(Glyph glyph, String message) {
				// maintain opactiy from setIgnored!
				final double opacity = this.glyph == null ? 1.0 : this.glyph.getOpacity();
				this.glyph = glyph;
				this.glyph.setOpacity(opacity);
				setupGrid();
				this.grid.add(this.glyph, 1, 0);
				this.status.setText(message);
			}

			public void setStatus(RepairStatus state) {
				updateStatus(state.getGlyph(), state.getMessage());
			}

			public void setIgnored(boolean ignored) {
				if (this.glyph != null) {
					this.glyph.setOpacity(ignored ? 0.33 : 1.0);
				}
				this.status.setOpacity(ignored ? 0.33 : 1.0);
			}
		}

		// overwrite, but call super
		protected void updateRepairedProperty() {
			// don't show body if ignored
			this.parent.setCenter(doIgnore() ? null : this.body);
			this.header.setIgnored(doIgnore());
		}

		public ReadOnlyBooleanProperty repairedProperty() {
			return this.repairedProperty;
		}

		final public boolean doIgnore() {
			return this.header.ignore.isSelected();
		}

		@Override
		public Parent node() {
			return parent;
		}
	}

	/**
	 * RepairSection base class.
	 */
	private static abstract class RepairSectionBase implements RepairSection, Localizable {

		protected final Stage stage;
		protected final ProjectData projectData;
		protected final ProjectData.ValidationResult validation;
		protected final ListView<Listable> listView;
		protected final VBox parent;
		protected final Label title;
		protected final BooleanProperty repairedProperty = new SimpleBooleanProperty(false);
		protected final ChangeListener repairedListener = (obs, a, b) -> updateRepairedProperty();

		public RepairSectionBase(Stage stage, ProjectData projectData, ProjectData.ValidationResult validation, String title) {
			this.stage = stage;
			this.projectData = projectData;
			this.validation = validation;

			this.parent = new VBox();
			this.title = new Label(title + ":");
			this.listView = Listable.newListView();
			parent.getChildren().addAll(this.title, this.listView);
		}

		@Override
		public ReadOnlyBooleanProperty repairedProperty() {
			return repairedProperty;
		}

		abstract void updateRepairedProperty();

		@Override
		public Parent getComponent() {
			return parent;
		}
	}

	/**
	 * Resource states.
	 */
	private enum ResourceState implements RepairStatus {

		/**
		 * File not found.
		 */
		NOT_FOUND(STATE_NOT_FOUND) {
					@Override
					public Glyph getGlyph() {
						return UIStrategyGUI.Glyphs.newErrorGlyph(GLYPH_SIZE);
					}
				},
		/**
		 * File has been modified (differing checksum).
		 */
		MODIFIED(STATE_MODIFIED) {
					@Override
					public Glyph getGlyph() {
						return UIStrategyGUI.Glyphs.newWarningGlyph(GLYPH_SIZE);
					}
				},
		/**
		 * File has been moved (prior state: NOT_FOUND).
		 */
		MOVED(STATE_MOVED) {
					@Override
					public Glyph getGlyph() {
						return UIStrategyGUI.Glyphs.newOkGlyph(GLYPH_SIZE);
					}
				},
		/**
		 * File has been replaced (prior state: MODIFIED).
		 */
		REPLACED(STATE_MODIFIED) {
					@Override
					public Glyph getGlyph() {
						return UIStrategyGUI.Glyphs.newOkGlyph(GLYPH_SIZE);
					}
				};

		private final String message;

		ResourceState(String message) {
			this.message = message;
		}

		@Override
		public String getMessage() {
			return this.message;
		}
	}

	/**
	 * Invalid Image line.
	 */
	private static class InvalidImage extends ListableBase<FormGridPane> {

		private final Stage stage;
		private final ProjectData.Page page;
		private final String latestChecksum;

		private final Label primaryLabel = new Label();
		private final Label primaryValue = new Label();
		private final Label secondaryLabel = new Label();
		private final Label secondaryValue = new Label();
		private final Button locate = new Button(localize("locate"));

		private ResourceState state = ResourceState.NOT_FOUND;
		private Path newPath;
		private String newChecksum;

		public InvalidImage(Stage stage, ProjectData.Page page, ResourceState state, String checksum) {
			super(getTitle(page), new FormGridPane(getColumnConstraints()));
			this.stage = stage;
			this.page = page;
			this.state = state;
			this.latestChecksum = checksum;
			this.newChecksum = this.latestChecksum;

			locate.getStyleClass().add("dip-small");
			locate.setOnAction((e) -> {
				final FileChooser chooser = new FileChooser();
				final File file = chooser.showOpenDialog(stage);
				if (file != null) {
					setPath(file.toPath());
				}
			});
			if (isModified()) {
				this.header.ignore.setSelected(true);
			}

			this.body.add(primaryLabel, 0, 0);
			this.body.add(primaryValue, 1, 0);
			this.body.add(secondaryLabel, 0, 1);
			this.body.add(secondaryValue, 1, 1);
			this.body.add(locate, 1, 2);

			setState(state);
		}

		private static String getTitle(ProjectData.Page page) {
			return String.format("%s (id: %d)", page.name, page.id);
		}

		private static ColumnConstraints[] getColumnConstraints() {
			final int n = 2;
			final ColumnConstraints[] cc = new ColumnConstraints[n];
			for (int i = 0; i < n; i++) {
				cc[i] = new ColumnConstraints();
				cc[i].setHgrow(Priority.SOMETIMES);
				cc[i].setHalignment(HPos.LEFT);
			}
			cc[1].setHgrow(Priority.ALWAYS);
			return cc;
		}

		public ResourceState getState() {
			return this.state;
		}

		private void setLabels(ResourceState state) {
			switch (state) {
				case MODIFIED:
					primaryLabel.setText(localize("file") + ":");
					secondaryLabel.setText("");
					break;
				default:
					primaryLabel.setText(localize("file.old") + ":");
					secondaryLabel.setText(localize("file.new") + ":");
					break;
			}
		}

		public final void setState(ResourceState state) {
			this.state = state;

			switch (state) {
				default:
				case NOT_FOUND:
					secondaryValue.setText("???");
					break;
				case MODIFIED:
					secondaryValue.setText(
							String.format(
									"%s != %s",
									this.page.checksum,
									this.newChecksum
							)
					);
					break;
				case MOVED:
					secondaryValue.setText(this.newPath.toString());
					break;
				case REPLACED:
					secondaryValue.setText(this.newPath.toString());
					primaryValue.setText(page.file.toString());
					break;
			}

			this.header.setStatus(state);
			primaryValue.setText(page.file.toString());
			setLabels(state);
			updateRepairedProperty();
		}

		final public boolean isNotFound() {
			return this.state.equals(ResourceState.NOT_FOUND);
		}

		final public boolean isModified() {
			return this.state.equals(ResourceState.MODIFIED);
		}

		final public void setPath(Path file) {
			this.newPath = file;
			try {
				this.newChecksum = IOUtils.checksum(file);
			} catch (IOException ex) {
				log.warn("failed to compute checksum: {}", file, ex);
			}
			switch (this.state) {
				case MODIFIED:
				case REPLACED:
					setState(ResourceState.REPLACED);
					break;
				default:
					setState(ResourceState.MOVED);
					break;
			}
		}

		@Override
		protected void updateRepairedProperty() {
			super.updateRepairedProperty();
			this.repairedProperty.set(
					doIgnore() || (!isNotFound() && !isModified())
			);
			if (doIgnore()) {
				this.header.setStatus(
						this.state.equals(ResourceState.MODIFIED)
								? ResourceState.MODIFIED
								: ResourceState.NOT_FOUND
				);
			} else {
				this.header.setStatus(this.state);
			}
		}

		public void apply() {
			if (this.doIgnore()) {
				// ignore (PageGenerator handles non existing images just fine...)
			} else {
				this.page.file = this.newPath;
				this.page.checksum = this.newChecksum;
			}
		}
	}

	/**
	 * Repair missing pages/images section.
	 */
	private static class RepairImageSection extends RepairSectionBase {

		private final List<InvalidImage> items = new ArrayList<>();
		private final Map<FileFinderTask.FileDescriptor, InvalidImage> finderFiles = new HashMap<>();
		private final Label finderDirectory = new Label();
		private final ProgressBar finderProgressBar = new ProgressBar(0);
		private final Label searchLabel = new Label(localize("searching"));
		private final Button searchFrom = new Button(localize("search.from"));
		private final Button searchCancel = new Button(localize("cancel"));
		private final Lane action = new Lane();
		private final DirectoryChooser searchChooser = new DirectoryChooser();
		private final Path finderRoot;
		private final FileFinderService finder;

		public RepairImageSection(Stage stage, ProjectData projectData, ProjectData.ValidationResult validation) {
			super(stage, projectData, validation, L10n.getInstance().getString("page.images.missing"));

			searchFrom.getStyleClass().add("dip-small");
			searchCancel.getStyleClass().add("dip-small");
			searchLabel.setPadding(new Insets(5, 0, 0, 0));

			finderProgressBar.setPadding(new Insets(1, 0, 0, 1)); // not sure why this guy is a pixel off...

			finderRoot = projectData.getParentDirectory();
			finder = new FileFinderService(
					finderRoot,
					new FileFinderTask.FinderCallback() {

						@Override
						public void onHit(FileFinderTask.FileDescriptor fd, Path file) {
							final InvalidImage item = finderFiles.get(fd);
							item.setPath(file);
						}

						@Override
						public void onFinished() {
							activateSearch(false);
						}
					}
			);

			for (ProjectData.Page page : validation.missingImages) {
				final String checksum = validation.checksums.get(page.id);
				final InvalidImage item = new InvalidImage(stage, page, ResourceState.NOT_FOUND, checksum);
				item.repairedProperty().addListener(repairedListener);
				final FileFinderTask.FileDescriptor fd = finder.addQuery(item.page.file, item.page.checksum);
				finderFiles.put(fd, item);
				items.add(item);
			}
			for (ProjectData.Page page : validation.modifiedImages) {
				final String checksum = validation.checksums.get(page.id);
				final InvalidImage item = new InvalidImage(stage, page, ResourceState.MODIFIED, checksum);
				item.repairedProperty().addListener(repairedListener);
				items.add(item);
			}
			listView.getItems().addAll(items);
			action.setAlignment(Pos.CENTER_LEFT);
			action.setSpacing(5);
			searchChooser.setTitle(localize("search.from"));
			searchFrom.setOnAction((e) -> {
				finder.cancel();
				finderFiles.clear();
				final File file = searchChooser.showDialog(stage);
				if (file != null) {
					finder.reset();
					finder.setRoot(file.toPath());
					finder.clear();
					for (InvalidImage item : items) {
						if (item.isNotFound()) {
							final FileFinderTask.FileDescriptor fd = finder.addQuery(
									item.page.file,
									item.page.checksum
							);
							finderFiles.put(fd, item);
						}
					}
					searchLabel.setDisable(false);
					searchCancel.setDisable(false);
					finderProgressBar.progressProperty().bind(finder.progressProperty());
					finderDirectory.textProperty().bind(finder.currentDirectoryProperty());
					finder.start();
				}
			});
			searchCancel.setOnAction((e) -> finder.cancel());
			// start searching for missing files...
			if (finder.numQueries() > 0) {
				activateSearch(true);
				action.getChildren().addAll(finderProgressBar, searchFrom, searchCancel);
				parent.getChildren().addAll(searchLabel, action, finderDirectory);
				finder.start();
			}
		}

		private void activateSearch(boolean activate) {
			if (activate) {
				searchLabel.setDisable(false);
				searchCancel.setDisable(false);
				finderProgressBar.progressProperty().bind(finder.progressProperty());
				finderDirectory.textProperty().bind(finder.currentDirectoryProperty());
			} else {
				searchLabel.setDisable(true);
				searchCancel.setDisable(true);
				searchFrom.setDisable(!hasMissingFiles(items));
				finderDirectory.textProperty().unbind();
				finderDirectory.setText("");
				finderDirectory.setDisable(true);
				finderProgressBar.progressProperty().unbind();
				finderProgressBar.setProgress(0);
				finderProgressBar.setDisable(true);

				updateRepairedProperty();
			}
		}

		private boolean hasMissingFiles(List<InvalidImage> items) {
			for (InvalidImage item : items) {
				if (item.isNotFound()) {
					return true;
				}
			}
			return false;
		}

		public void shutdownFinder() {
			if (this.finder != null) {
				if (finder.isRunning()) {
					this.finder.cancel();
				}
			}
		}

		@Override
		void updateRepairedProperty() {
			for (InvalidImage item : items) {
				if (!item.repairedProperty().get()) {
					repairedProperty.set(false);
					return;
				}
			}
			repairedProperty.set(true);
		}

		@Override
		public void applyRepairs() {
			for (InvalidImage item : items) {
				item.apply();
			}
		}
	}

	/**
	 * Service states.
	 */
	private enum ServiceState implements RepairStatus {

		/**
		 * Unavailable service.
		 */
		UNAVAILABLE(STATE_UNAVAILABLE) {
					@Override
					public Glyph getGlyph() {
						return UIStrategyGUI.Glyphs.newErrorGlyph(GLYPH_SIZE);
					}
				},
		/**
		 * Up-/downgraded service.
		 */
		UPGRADED(STATE_UPGRADED) {
					@Override
					public Glyph getGlyph() {
						return UIStrategyGUI.Glyphs.newOkGlyph(GLYPH_SIZE);
					}
				},
		/**
		 * Replaced service.
		 */
		REPLACED(STATE_REPLACED) {
					@Override
					public Glyph getGlyph() {
						return UIStrategyGUI.Glyphs.newOkGlyph(GLYPH_SIZE);
					}
				};

		private final String message;

		ServiceState(String message) {
			this.message = message;
		}

		@Override
		public String getMessage() {
			return this.message;
		}
	}

	/**
	 * Invalid service line.
	 */
	private static class InvalidService extends ListableBase<FormGridPane> {

		private final static Insets comboInsets = new Insets(0, 0, 0, 3);
		private final static Insets mappingInsets = new Insets(UIStrategyGUI.Stage.insets, 0, 0, 0);
		private final String pid;
		private final String version;
		private final boolean canUpgrade;
		private final boolean canReplace;
		private final Map<String, String> inputs = new LinkedHashMap<>();
		private final Map<String, String> outputs = new LinkedHashMap<>();
		private final List<ComboBox> inputCombos = new ArrayList<>();
		private final List<ComboBox> outputCombos = new ArrayList<>();
		private final List<List<String>> inputKeys = new ArrayList<>();
		private final List<List<String>> outputKeys = new ArrayList<>();

		private final ServiceCollection<Processor> compatibleVersions; // same proc, different version
		private final ObservableList<String> compatibleVersionList;
		private final Map<String, ServiceCollection<Processor>> compatibleServices; // replacement procs, any version
		private final ObservableList<String> compatibleServiceList; // replacement procs, as list of pids
		private final Map<String, List<String>> compatibleServiceVersions; // maps pid to list of available version

		private final RadioChoiceBox choice;
		private final HBox upgradeOption;
		private final HBox replaceOption;
		private final ComboBox upgradeVersionOption;
		private final ComboBox replaceProcessorOption;
		private final ComboBox replaceVersionOption;
		private final ObservableList<String> replaceVersionList;

		private ServiceState state = ServiceState.UNAVAILABLE;

		public InvalidService(String pid, String version, ProjectData projectData, ApplicationHandler handler) {
			super(getTitle(pid, version), new FormGridPane(getColumnConstraints()));
			this.pid = pid;
			this.version = version;

			// read all (global) pipeline prototypes
			for (PipelineData.Pipeline<ProcessorWrapper> pipeline : projectData.pipelines()) {
				parsePipeline(pid, pipeline, inputs, outputs);
			}

			// read all page pipelines too
			for (ProjectData.Page page : projectData.getPages()) {
				final String xmlPath = page.pipelineXmlPath();
				final Path xml = projectData.zip.getPath(xmlPath);
				if (Files.exists(xml)) {
					try {
						final PipelineData pd = PipelineData.loadAsStream(xml);
						if (pd.hasPrimaryPipeline()) {
							final PipelineData.Pipeline pipeline = pd.primaryPipeline();
							parsePipeline(pid, pipeline, inputs, outputs);
						}
					} catch (IOException | JAXBException ex) {
						log.warn("failed to read pipeline of {} at {}", page, xml, ex);
					}
				}
			}

			// 1) get list of available versions of the missing processor for an up-/downgrade
			final ServiceCollection<Processor> serviceCollection = handler.osgi.getProcessorCollection(pid);
			// make sure they're also compatible
			if (serviceCollection != null) {
				final List<ServiceCollection<Processor>> collections = OSGiFramework.getCompatibleProcessors(
						Arrays.asList(serviceCollection),
						inputs.values(),
						outputs.values()
				);
				this.compatibleVersions = collections.isEmpty() ? null : collections.get(0);
			} else {
				this.compatibleVersions = null;
			}
			this.canUpgrade = this.compatibleVersions != null;
			this.compatibleVersionList = FXCollections.observableArrayList();
			if (canUpgrade) {
				for (OSGiService v : compatibleVersions.getVersions()) {
					this.compatibleVersionList.add(v.version.toString());
				}
			}

			// 2) get list of compatible processors to replace the processor with a different one
			this.compatibleServices = OSGiFramework.getCompatibleProcessorMap(
					handler.osgi.getProcessorCollectionList(),
					Arrays.asList(pid),
					inputs.values(),
					outputs.values()
			);
			this.canReplace = !compatibleServices.isEmpty();
			this.compatibleServiceList = FXCollections.observableArrayList();
			for (ServiceCollection collection : compatibleServices.values()) {
				this.compatibleServiceList.add(collection.pid());
			}

			this.compatibleServiceVersions = new HashMap<>();
			this.upgradeVersionOption = newComboBox(this.compatibleVersionList);
			this.replaceProcessorOption = newComboBox(this.compatibleServiceList);
			this.replaceVersionList = FXCollections.observableArrayList();
			this.replaceVersionOption = newComboBox(this.replaceVersionList);
			this.upgradeOption = new HBox();
			this.upgradeOption.getChildren().setAll(
					new Label(localize("version.change.to")),
					this.upgradeVersionOption
			);
			this.replaceOption = new HBox();
			this.replaceOption.getChildren().setAll(
					new Label(localize("replace.with")),
					this.replaceProcessorOption,
					this.replaceVersionOption
			);
			this.choice = new RadioChoiceBox(
					this.upgradeOption,
					this.replaceOption
			);

			// make sure to not select an invalid/disabled option
			final RadioChoiceBox.RadioChoice upgradeChoice = this.choice.get(0);
			final RadioChoiceBox.RadioChoice replaceChoice = this.choice.get(1);
			if (!canUpgrade) {
				upgradeChoice.setDisable(true);
				upgradeChoice.radio.setSelected(false);
				if (canReplace) {
					replaceChoice.radio.setSelected(true);
				}
			}
			if (!canReplace) {
				replaceChoice.setDisable(true);
			}

			// 0        | 1         | 2      | 3
			// choice...
			// ins/outs | port-desc | map to | combobox
			int row = 0;
			GridPane.setColumnSpan(choice, GridPane.REMAINING);
			this.body.add(choice, 0, row++);

			final Label mapping = new Label(localize("processor.port.mapping") + ":");
			mapping.setPadding(mappingInsets);
			GridPane.setColumnSpan(mapping, GridPane.REMAINING);
			this.body.add(mapping, 0, row++);

			final int ins = inputs.size();
			if (ins > 0) {
				final Label label = newLabel(localize("processor.inputs"));
				GridPane.setRowSpan(label, ins);
				this.body.add(label, 0, row);

				int i = 0;
				for (Map.Entry<String, String> port : inputs.entrySet()) {
					final ComboBox combo = newComboBox();
					inputCombos.add(combo);
					addPortToGrid(port, combo, row++);
				}
			}

			final int outs = outputs.size();
			if (outs > 0) {
				final Label label = newLabel(localize("processor.outputs"));
				GridPane.setRowSpan(label, outs);
				this.body.add(label, 0, row);

				for (Map.Entry<String, String> port : outputs.entrySet()) {
					final ComboBox combo = newComboBox();
					outputCombos.add(combo);
					addPortToGrid(port, combo, row++);
				}
			}

			this.choice.selectedToggleProperty().addListener((c) -> updatePortMapping());
			this.upgradeVersionOption.setOnAction((c) -> updatePortMapping());
			this.replaceProcessorOption.setOnAction((c) -> updateReplaceVersions());
			this.replaceVersionOption.setOnAction((c) -> updatePortMapping());
			updateReplaceVersions();

			this.choice.selectedToggleProperty().addListener((c) -> updateStatus());
			updateStatus();
		}

		private static ColumnConstraints[] getColumnConstraints() {
			final int n = 4;
			final ColumnConstraints[] cc = new ColumnConstraints[n];
			for (int i = 0; i < n; i++) {
				cc[i] = new ColumnConstraints();
				cc[i].setHgrow(Priority.ALWAYS);
			}
			cc[0].setHgrow(Priority.SOMETIMES);
			cc[1].setHgrow(Priority.SOMETIMES);
			cc[3].setPercentWidth(33.3);
			return cc;
		}

		private void addPortToGrid(Map.Entry<String, String> port, ComboBox combo, int row) {
			final Parent desc = newPortDescription(port.getKey(), port.getValue());
//			final Label map = new Label(localize("map.to"));
			final HLine map = new HLine();
			final ArrowHead head = new ArrowHead();
			map.addArrowHead(head);
			this.body.add(desc, 1, row);
			this.body.add(map, 2, row);
			this.body.add(combo, 3, row);
		}

		private Parent newPortDescription(String port, String type) {
			final VBox box = new VBox();
			final Label portLabel = new Label(port);
			final Label typeLabel = newLabel(type);
			box.getChildren().addAll(portLabel, typeLabel);
			return box;
		}

		private static Label newLabel(String text) {
			final Label label = new Label(text);
			label.getStyleClass().add("dip-small");
			return label;
		}

		// for port mappings
		private static ComboBox newComboBox() {
			final ComboBox box = new ComboBox();
			box.getStyleClass().add("dip-small");
			return box;
		}

		// for service/version
		private static ComboBox newComboBox(ObservableList items) {
			final ComboBox box = new ComboBox(items);
			box.getStyleClass().add("dip-small");
			HBox.setMargin(box, comboInsets);
			if (items.size() > 0) {
				box.getSelectionModel().select(0);
			}
			return box;
		}

		private void updateReplaceVersions() {
			final int index = this.replaceProcessorOption.getSelectionModel().getSelectedIndex();
			if (index < 0) {
				this.replaceVersionList.clear();
				updatePortMapping();
				return;
			}
			final String rpid = this.compatibleServiceList.get(index);
			this.replaceVersionList.setAll(this.getReplacementVersions(rpid));
			if (this.replaceVersionList.size() > 0) {
				this.replaceVersionOption.getSelectionModel().select(0);
			}
			updatePortMapping();
		}

		// don't update again...
		private OSGiService<Processor> currentService;

		private void updatePortMapping() {
			final OSGiService<Processor> p = selectedVersion();
			if (p != null) {
				if (p.equals(currentService)) {
					return;
				}
				currentService = p;

				inputKeys.clear();
				int i = 0;
				for (Map.Entry<String, String> port : inputs.entrySet()) {
					final List<String> opt = p.serviceObject.inputs(port.getValue());
					inputKeys.add(opt);
					final ComboBox combo = inputCombos.get(i++);
					combo.getItems().setAll(opt);
					combo.getSelectionModel().select(0);
				}

				outputKeys.clear();
				i = 0;
				for (Map.Entry<String, String> port : outputs.entrySet()) {
					final List<String> opt = p.serviceObject.outputs(port.getValue());
					outputKeys.add(opt);
					final ComboBox combo = outputCombos.get(i++);
					combo.getItems().setAll(opt);
					combo.getSelectionModel().select(0);
				}
			} else {
				for (int i = 0; i < inputs.size(); i++) {
					inputCombos.get(i).getItems().clear();
				}
				for (int i = 0; i < outputs.size(); i++) {
					outputCombos.get(i).getItems().clear();
				}
			}
			updateRepairedProperty();
		}

		private void updateStatus() {
			final int opt = selectedOption();
			switch (opt) {
				case 0:
					this.state = ServiceState.UPGRADED;
					break;
				case 1:
					this.state = ServiceState.REPLACED;
					break;
				default:
					this.state = ServiceState.UNAVAILABLE;
					break;
			}
			this.header.setStatus(this.state);
		}

		// upgrade or replace
		private OSGiService<Processor> selectedVersion() {
			final int opt = selectedOption();
			if (opt < 0) {
				return null;
			}
			return (opt == 0) ? selectedUpgradeVersion() : selectedReplaceVersion();
		}

		// -1: none, 0: upgrade, 1: replace
		private int selectedOption() {
			final RadioChoiceBox.RadioChoice r = this.choice.selectedRadioChoice();
			if (r == null) {
				return -1;
			}
			if (r.node.equals(this.upgradeOption)) {
				return this.choice.get(0).radio.isDisable() ? -1 : 0;
			} else {
				return this.choice.get(1).radio.isDisable() ? -1 : 1;
			}
		}

		private int selectedUpgradeVersionIndex() {
			return this.upgradeVersionOption.getSelectionModel().getSelectedIndex();
		}

		private OSGiService<Processor> selectedUpgradeVersion() {
			final int index = selectedUpgradeVersionIndex();
			if (index < 0 || this.compatibleVersions == null) {
				return null;
			}
			final String v = this.compatibleVersionList.get(index);
			return this.compatibleVersions.getService(v);
		}

		private int selectedReplaceProcessorIndex() {
			return this.replaceProcessorOption.getSelectionModel().getSelectedIndex();
		}

		private int selectedReplaceVersionIndex() {
			return this.replaceVersionOption.getSelectionModel().getSelectedIndex();
		}

		private OSGiService<Processor> selectedReplaceVersion() {
			final int pindex = selectedReplaceProcessorIndex();
			final int vindex = selectedReplaceVersionIndex();
			if (pindex < 0 || vindex < 0) {
				return null;
			}
			final String rpid = this.compatibleServiceList.get(pindex);
			final ServiceCollection<Processor> collection = this.compatibleServices.get(rpid);
			final String v = getReplacementVersions(rpid).get(vindex);
			return collection.getService(v);
		}

		private static String getTitle(String pid, String version) {
			return String.format("%s (v.%s)", pid, version);
		}

		private List<String> getReplacementVersions(String pid) {
			if (!compatibleServiceVersions.containsKey(pid)) {
				final List<String> versions;
				if (compatibleServices.containsKey(pid)) {
					versions = compatibleServices.get(pid).getVersions().stream().map((p) -> {
						return p.version.toString();
					}).collect(Collectors.toList());
				} else {
					versions = Collections.EMPTY_LIST;
				}
				compatibleServiceVersions.put(pid, versions);
				return versions;
			}
			return compatibleServiceVersions.get(pid);
		}

		private void parsePipeline(String pid, PipelineData.Pipeline<ProcessorWrapper> pipeline, Map<String, String> inputs, Map<String, String> outputs) {
			final Set<Integer> ids = new HashSet<>();
			for (PipelineData.Processor processor : pipeline.processors()) {
				if (processor.pid.equals(pid)) {
					ids.add(processor.id);
				}
			}

			for (PipelineData.Connection connection : pipeline.connections()) {
				if (ids.contains(connection.input.id)) {
					inputs.put(connection.input.port, connection.type);
				}
				if (ids.contains(connection.output.id)) {
					outputs.put(connection.output.port, connection.type);
				}
			}
		}

		@Override
		protected void updateRepairedProperty() {
			super.updateRepairedProperty();
			this.repairedProperty.set(
					doIgnore() || hasValidMapping()
			);
			if (doIgnore()) {
				this.header.setStatus(ServiceState.UNAVAILABLE);
			} else {
				this.header.setStatus(this.state);
			}
		}

		public boolean hasValidMapping() {
			// it's currently not possible to have an invalid mapping, unless no
			// option is available at all. This might change with a more clever
			// test for valid mappings (some configurations can be invalid after
			// all, e.g. by reusing the same port...), but until then, this will
			// do...
			return selectedOption() >= 0;
		}

		public PipelineData.ProcessorSwap apply() {
			final OSGiService<Processor> p = selectedVersion();
			if (p == null) {
				return null;
			}
			final String toPID = p.pid;
			final String toVersion = p.version.toString();
			final Map<String, String> inMap = new HashMap<>();
			final Map<String, String> outMap = new HashMap<>();
			int i = 0;
			for (Map.Entry<String, String> in : inputs.entrySet()) {
				final int idx = inputCombos.get(i).getSelectionModel().getSelectedIndex();
				inMap.put(in.getKey(), inputKeys.get(i).get(idx));
				i++;
			}

			i = 0;
			for (Map.Entry<String, String> out : outputs.entrySet()) {
				final int idx = outputCombos.get(i).getSelectionModel().getSelectedIndex();
				outMap.put(out.getKey(), outputKeys.get(i).get(idx));
				i++;
			}
			return new PipelineData.ProcessorSwap(pid, version, toPID, toVersion, inMap, outMap);
		}
	}

	/**
	 * Repair missing processor/OSGI services section.
	 */
	private static class RepairServiceSection extends RepairSectionBase {

		private final List<InvalidService> items = new ArrayList<>();

		public RepairServiceSection(ApplicationHandler handler, Stage stage, ProjectData projectData, ProjectData.ValidationResult validation) {
			super(stage, projectData, validation, L10n.getInstance().getString("pipeline.services.missing"));

			for (String pid : validation.missingServices.keySet()) {
				final Set<String> versions = validation.missingServices.get(pid);
				for (String version : versions) {
					final InvalidService is = new InvalidService(pid, version, projectData, handler);
					is.repairedProperty.addListener(repairedListener);
					items.add(is);
				}
			}
			listView.getItems().addAll(items);
			updateRepairedProperty();
		}

		@Override
		final void updateRepairedProperty() {
			for (InvalidService item : items) {
				if (!item.repairedProperty().get()) {
					repairedProperty.set(false);
					return;
				}
			}
			repairedProperty.set(true);
		}

		@Override
		public void applyRepairs() {
			final List<PipelineData.ProcessorSwap> swaps = new ArrayList<>();
			for (InvalidService item : items) {
				final PipelineData.ProcessorSwap swap = item.apply();
				if (swap != null) {
					swaps.add(swap);
				}
			}

			// apply to all (global) pipelines
			for (PipelineData.ProcessorSwap swap : swaps) {
				projectData.pipelines.swapProcessor(swap);
			}

			// apply to all page pipelines too
			for (ProjectData.Page page : projectData.getPages()) {
				final String xmlPath = page.pipelineXmlPath();
				final Path xml = projectData.zip.getPath(xmlPath);
				if (Files.exists(xml)) {
					try {
						final PipelineData pd = PipelineData.loadAsStream(xml);
						if (pd.hasPrimaryPipeline()) {
							for (PipelineData.ProcessorSwap swap : swaps) {
								pd.swapProcessor(swap);
							}
							Files.delete(xml);
							try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(xml))) {
								XmlUtils.marshal(pd, stream);
							}
						}
					} catch (IOException | JAXBException ex) {
						log.warn("failed to read pipeline of {} at {}", page, xml, ex);
					}
				}
			}
		}
	}

}
