package ch.unifr.diva.dip.gui.dialogs;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PipelineData;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.core.model.ProjectData;
import ch.unifr.diva.dip.core.ui.Localizable;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getCancelButton;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getDefaultButton;
import ch.unifr.diva.dip.gui.layout.ArrowHead;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.gui.layout.HLine;
import ch.unifr.diva.dip.gui.layout.Lane;
import ch.unifr.diva.dip.gui.layout.Listable;
import ch.unifr.diva.dip.osgi.ServiceMonitor.Service;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
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
	 * Resource states.
	 */
	private enum ResourceState {

		/**
		 * File not found.
		 */
		NOT_FOUND,
		/**
		 * File has been modified (differing checksum).
		 */
		MODIFIED,
		/**
		 * File has been moved (prior state: NOT_FOUND).
		 */
		MOVED,
		/**
		 * File has been replaced (prior state: MODIFIED).
		 */
		REPLACED
	}

//	private static Label newLabel(String text) {
//		final Label label = new Label(text);
//		label.setMaxWidth(Double.MAX_VALUE);
//		label.setAlignment(Pos.CENTER_LEFT);
//		return label;
//	}
	/**
	 * Invalid Image line.
	 */
	private static class InvalidImage implements Listable, Localizable {

		private final Stage stage;
		private final ProjectData.Page page;
		private final String latestChecksum;
		private final BorderPane parent = new BorderPane();
		private final VBox vbox = new VBox();
		private final FormGridPane form;
		private final Label primaryLabel = new Label();
		private final Label primaryValue = new Label();
		private final Label secondaryLabel = new Label();
		private final Label secondaryValue = new Label();
		private final Label pageLabel = new Label();
		private final Lane actionLane = new Lane();
		private final Button locate = new Button(localize("locate"));
		private final CheckBox ignore = new CheckBox(localize("ignore"));
		private final BooleanProperty repairedProperty = new SimpleBooleanProperty(false);
		private ResourceState state;
		private Path newPath;
		private String newChecksum;

		public InvalidImage(Stage stage, ProjectData.Page page, ResourceState state, String checksum) {
			this.stage = stage;
			this.page = page;
			this.latestChecksum = checksum;
			this.newChecksum = this.latestChecksum;

			pageLabel.setText(String.format("%s (id: %d)", page.name, page.id));

			final int numCC = 2;
			final ColumnConstraints[] cc = new ColumnConstraints[numCC];
			for (int i = 0; i < numCC; i++) {
				cc[i] = new ColumnConstraints();
				cc[i].setHgrow(Priority.SOMETIMES);
			}
			cc[1].setHgrow(Priority.ALWAYS);

			form = new FormGridPane(cc);
			primaryLabel.getStyleClass().add("dip-small");
			secondaryLabel.getStyleClass().add("dip-small");
			form.addRow(primaryLabel, primaryValue);
			form.addRow(secondaryLabel, secondaryValue);
			vbox.getChildren().addAll(pageLabel, form);

			locate.getStyleClass().add("dip-small");
			locate.setOnAction((e) -> {
				final FileChooser chooser = new FileChooser();
				final File file = chooser.showOpenDialog(stage);
				if (file != null) {
					setPath(file.toPath());
				}
			});
			actionLane.setPadding(new Insets(0, 0, 0, 10));
			actionLane.getChildren().addAll(locate, ignore);
			parent.setCenter(vbox);
			parent.setRight(actionLane);

			ignore.selectedProperty().addListener((obs, a, b) -> {
				updateRepairedProperty();
			});
			if (state.equals(ResourceState.MODIFIED)) {
				this.ignore.setSelected(true);
			}

			setState(state);
		}

		public ResourceState getState() {
			return this.state;
		}

		private void setLabels(ResourceState state) {
			switch (state) {
				case MODIFIED:
					primaryLabel.setText(localize("path") + ":");
					secondaryLabel.setText("");
					break;
				default:
					primaryLabel.setText(localize("path.old") + ":");
					secondaryLabel.setText(localize("path.new") + ":");
					break;
			}
		}

		public final void setState(ResourceState state) {
			this.state = state;

			final String s;
			switch (state) {
				default:
				case NOT_FOUND:
					secondaryValue.setText("???");
					s = localize("state.notfound");
					break;
				case MODIFIED:
					secondaryValue.setText(
							String.format(
									"%s != %s",
									this.page.checksum,
									this.newChecksum
							)
					);
					s = localize("state.modified");
					break;
				case MOVED:
					secondaryValue.setText(this.newPath.toString());
					s = localize("state.moved");
					break;
				case REPLACED:
					secondaryValue.setText(this.newPath.toString());
					primaryValue.setText(page.file.toString());
					s = localize("state.modified");
					break;
			}

			primaryValue.setText(String.format("%s (%s)", page.file, s));
			setLabels(state);
			updateRepairedProperty();
		}

		private void updateRepairedProperty() {
			this.repairedProperty.set(
					doIgnore() || (!isNotFound() && !isModified())
			);
		}

		public ReadOnlyBooleanProperty repairedProperty() {
			return this.repairedProperty;
		}

		public boolean doIgnore() {
			return this.ignore.isSelected();
		}

		public boolean isNotFound() {
			return this.state.equals(ResourceState.NOT_FOUND);
		}

		public boolean isModified() {
			return this.state.equals(ResourceState.MODIFIED);
		}

		public void setPath(Path file) {
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

		public void apply() {
			if (this.doIgnore()) {
				// ignore (PageGenerator handles non existing images just fine...)
			} else {
				this.page.file = this.newPath;
				this.page.checksum = this.newChecksum;
			}
		}

		@Override
		public Parent node() {
			return parent;
		}
	}

	/**
	 * Repair missing pages/images section.
	 */
	private static class RepairImageSection implements RepairSection, Localizable {

		private final VBox parent = new VBox();
		private final Label title = new Label(localize("page.images.missing"));
		private final ListView<Listable> listView = Listable.newListView();
		private final List<InvalidImage> items = new ArrayList<>();
		private final Map<FileFinderTask.FileDescriptor, InvalidImage> finderFiles = new HashMap<>();
		private final Label finderDirectory = new Label();
		private final ProgressBar finderProgressBar = new ProgressBar(0);
		private final Label searchLabel = new Label(localize("searching"));
		private final Button searchFrom = new Button(localize("search.from"));
		private final Button searchCancel = new Button(localize("cancel"));
		private final Lane action = new Lane();
		private final DirectoryChooser searchChooser = new DirectoryChooser();
		private final BooleanProperty repairedProperty = new SimpleBooleanProperty(false);
		private final ChangeListener repairedListener = (obs, a, b) -> updateRepairedProperty();
		private final Path finderRoot;
		private final FileFinderService finder;

		public RepairImageSection(Stage stage, ProjectData projectData, ProjectData.ValidationResult validation) {
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
			parent.getChildren().addAll(title, listView);

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

			if (finder.numQueries() > 0) { // start searching for missing files...
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
		public Parent getComponent() {
			return parent;
		}

		private void updateRepairedProperty() {
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

		@Override
		public ReadOnlyBooleanProperty repairedProperty() {
			return repairedProperty;
		}

	}

	/**
	 * Invalid Service line.
	 */
	private static class InvalidService implements Listable, Localizable {

		private final String pid;
		private final VBox parent = new VBox();
		// known/used ports we need to map to ports on a compatible processor
		//    map: key, type-string
		private final Map<String, String> inputs = new LinkedHashMap<>();
		private final Map<String, String> outputs = new LinkedHashMap<>();
		private final List<String> compatibleProcessors = new ArrayList<>();
		private final ComboBox processorCombo;
		private final List<ComboBox> inputCombos = new ArrayList<>();
		private final List<ComboBox> outputCombos = new ArrayList<>();
		private final CheckBox ignore;
		private final List<List<String>> inputKeys = new ArrayList<>();
		private final List<List<String>> outputKeys = new ArrayList<>();
		private final BooleanProperty repairedProperty = new SimpleBooleanProperty(false);
		private final ChangeListener repairedListener = (obs, a, b) -> updateRepairedProperty();

		public InvalidService(String pid, ProjectData projectData, ApplicationHandler handler) {
			this.pid = pid;

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

			final List<Service<Processor>> compatibleServices = handler.osgi.services.getCompatibleProcessors(
					inputs.values(),
					outputs.values()
			);

			for (Service<Processor> p : compatibleServices) {
				compatibleProcessors.add(p.pid);
			}

			this.processorCombo = new ComboBox();
			processorCombo.setOnAction((e) -> {
				final int index = processorCombo.getSelectionModel().getSelectedIndex();
				final Service<Processor> p = handler.osgi.services.getService(compatibleProcessors.get(index));
				if (p != null) {

					int i = 0;
					for (Map.Entry<String, String> in : inputs.entrySet()) {
						inputKeys.add(p.service.inputs(in.getValue()));
						inputCombos.get(i).getItems().setAll(
								inputKeys.get(i)
						);
						inputCombos.get(i).getSelectionModel().select(0);
						i++;
					}
					i = 0;
					for (Map.Entry<String, String> out : outputs.entrySet()) {
						outputKeys.add(p.service.outputs(out.getValue()));
						outputCombos.get(i).getItems().setAll(
								outputKeys.get(i)
						);
						outputCombos.get(i).getSelectionModel().select(0);
						i++;
					}
				}
			});
			processorCombo.getItems().setAll(compatibleProcessors);

			for (int i = 0; i < inputs.size(); i++) {
				final ComboBox combo = new ComboBox();
				combo.getSelectionModel().selectedIndexProperty().addListener(repairedListener);
				inputCombos.add(combo);
			}

			for (int i = 0; i < outputs.size(); i++) {
				final ComboBox combo = new ComboBox();
				combo.getSelectionModel().selectedIndexProperty().addListener(repairedListener);
				outputCombos.add(combo);
			}

			final int numCC = 7;
			final ColumnConstraints[] cc = new ColumnConstraints[numCC];
			for (int i = 0; i < numCC; i++) {
				cc[i] = new ColumnConstraints();
				cc[i].setHgrow(Priority.SOMETIMES);
			}
			cc[2].setHgrow(Priority.ALWAYS);
			cc[4].setHgrow(Priority.ALWAYS);
			cc[6].setHgrow(Priority.ALWAYS);

			final FormGridPane form = new FormGridPane(cc);

			final Label serviceLabel = new Label(pid);
			GridPane.setColumnSpan(serviceLabel, 2);

			final Label replaceLabel = new Label(localize("replace.with").toLowerCase());
			final HLine arrowStart = new HLine();
			final HLine arrowEnd = new HLine();
			final ArrowHead head = new ArrowHead();
			head.setFillColor(Color.WHITE);
			arrowEnd.addArrowHead(head);

			final Lane actionLane = new Lane();
			actionLane.setPadding(new Insets(0, 0, 0, 5));
			actionLane.setAlignment(Pos.CENTER_RIGHT);
			this.ignore = new CheckBox(localize("ignore"));
			this.ignore.selectedProperty().addListener(repairedListener);
			actionLane.getChildren().addAll(ignore);
			form.addRow(0, serviceLabel, new Label(), arrowStart, replaceLabel, arrowEnd, processorCombo, actionLane);

			int k = 1;
			k = populateForm(form, k, localize("processor.inputs"), inputs, inputCombos);
			populateForm(form, k, localize("processor.outputs"), outputs, outputCombos);

			this.parent.setPadding(new Insets(2));
			this.parent.getChildren().addAll(form);
		}

		private void centerLabel(Label label) {
			label.setAlignment(Pos.CENTER);
			label.setMaxWidth(Double.MAX_VALUE);
		}

		private int populateForm(FormGridPane form, int row, String title, Map<String, String> ports, List<ComboBox> combos) {
			int i = 0;
			for (Map.Entry<String, String> port : ports.entrySet()) {
				final Label portLabel = new Label(title);
				GridPane.setRowSpan(portLabel, 2);
				portLabel.getStyleClass().add("dip-small");
				final Parent portBox = formatPort(port.getKey(), port.getValue());
				final Label mapToLabel = new Label(localize("map.to").toLowerCase());
				centerLabel(mapToLabel);
				form.addRow(
						row++,
						portLabel, portBox, null, mapToLabel, null, combos.get(i)
				);
				title = "";
				i++;
			}

			return row;
		}

		private Parent formatPort(String port, String type) {
			final VBox box = new VBox();
			final Label portLabel = new Label(port);
			final Label typeLabel = new Label(type);
			typeLabel.getStyleClass().add("dip-small");
			box.getChildren().addAll(portLabel, typeLabel);
			return box;
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

		private void updateRepairedProperty() {
			this.repairedProperty.set(
					doIgnore() || hasMapping()
			);
		}

		public ReadOnlyBooleanProperty repairedProperty() {
			return this.repairedProperty;
		}

		public boolean doIgnore() {
			return this.ignore.isSelected();
		}

		public boolean hasMapping() {
			final int procIdx = this.processorCombo.getSelectionModel().getSelectedIndex();
			if (procIdx < 0) {
				return false;
			}
			final String toPID = this.compatibleProcessors.get(procIdx);
			for (ComboBox combo : inputCombos) {
				if (combo.getSelectionModel().getSelectedIndex() < 0) {
					return false;
				}
			}
			for (ComboBox combo : outputCombos) {
				if (combo.getSelectionModel().getSelectedIndex() < 0) {
					return false;
				}
			}
			return true;
		}

		public PipelineData.ProcessorSwap apply() {
			final int procIdx = this.processorCombo.getSelectionModel().getSelectedIndex();
			if (procIdx < 0) {
				return null;
			}
			final String toPID = this.compatibleProcessors.get(procIdx);

			final Map<String, String> inMap = new HashMap<>();
			final Map<String, String> outMap = new HashMap<>();

			final int[] inIdx = new int[inputs.size()];

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

			return new PipelineData.ProcessorSwap(pid, toPID, inMap, outMap);
		}

		@Override
		public Parent node() {
			return parent;
		}

	}

	/**
	 * Repair missing processor/OSGI services section.
	 */
	private static class RepairServiceSection implements RepairSection, Localizable {

		private final ProjectData projectData;
		private final VBox parent = new VBox();
		private final Label title = new Label(localize("pipeline.services.missing"));
		private final ListView<Listable> listView = Listable.newListView();
		private final List<InvalidService> items = new ArrayList<>();
		private final BooleanProperty repairedProperty = new SimpleBooleanProperty(false);
		private final ChangeListener repairedListener = (obs, a, b) -> updateRepairedProperty();

		public RepairServiceSection(ApplicationHandler handler, Stage stage, ProjectData projectData, ProjectData.ValidationResult validation) {
			this.projectData = projectData;
			for (String pid : validation.missingServices) {
				final InvalidService is = new InvalidService(pid, projectData, handler);
				is.repairedProperty().addListener(repairedListener);
				items.add(is);
				listView.getItems().add(is);
			}

			parent.getChildren().addAll(title, listView);

			/**/
			repairedProperty.set(true);
		}

		@Override
		public Parent getComponent() {
			return parent;
		}

		private void updateRepairedProperty() {
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

		@Override
		public ReadOnlyBooleanProperty repairedProperty() {
			return repairedProperty;
		}

	}

}
