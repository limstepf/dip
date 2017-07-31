package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ApplicationSettings;
import ch.unifr.diva.dip.core.ImageFormat;
import ch.unifr.diva.dip.core.model.PipelineData;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.gui.layout.VerticalSplitPane;
import ch.unifr.diva.dip.gui.pe.DataItemListView;
import ch.unifr.diva.dip.gui.pe.PipelineLoadDialog;
import ch.unifr.diva.dip.utils.FileFinder;
import ch.unifr.diva.dip.utils.FileFinderOption;
import ch.unifr.diva.dip.utils.IOUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wizard to create a new project.
 */
public class NewProjectDialog extends AbstractDialog {

	private static final Logger log = LoggerFactory.getLogger(NewProjectDialog.class);
	private final Window owner;
	private final ApplicationHandler handler;
	private final VBox vbox;
	private final FormGridPane form;
	private final TextField projectName;
	private final TextField projectFile;
	private final Button projectFileBrowse;
	private final FileChooser projectFileChooser;
	private final FileChooser imageFileChooser;
	private final DirectoryChooser imageDirectoryChooser;
	private final VerticalSplitPane vspane;
	private final DataItemListView<ImageItem> images;
	private final DataItemListView<PipelineData.PipelineItem> pipelines;
	private final Button ok;
	private final Button cancel;

	private boolean isOk = false;
	private File savefile = null;
	private File initialDirectory;
	private PipelineData.PipelineItem defaultPipeline;

	/**
	 * Creates a new project dialog.
	 *
	 * @param owner the owner of the dialog.
	 * @param handler the application handler.
	 */
	public NewProjectDialog(Window owner, ApplicationHandler handler) {
		super(owner);
		setTitle(localize("project.new"));
		this.owner = owner;
		this.handler = handler;

		initialDirectory = handler.getRecentSaveDirectory().toFile();

		this.projectName = new TextField();
		projectName.textProperty().addListener(updateListener);
		this.projectFile = new TextField();
		projectFile.setDisable(true);
		this.projectFileBrowse = new Button(localize("save.as"));
		projectFileBrowse.setMaxWidth(Double.MAX_VALUE);
		this.projectFileChooser = new FileChooser();
		projectFileChooser.setTitle(localize("save.as"));
		ApplicationSettings.setProjectExtensionFilter(projectFileChooser);
		projectFileBrowse.setOnAction((e) -> {
			projectFileChooser.setInitialFileName(getProjectFilename());
			projectFileChooser.setInitialDirectory(initialDirectory);
			final File file = projectFileChooser.showSaveDialog(owner);
			if (file != null) {
				savefile = file;
				projectFile.setText(file.toString());
			}
			update();
		});
		this.form = new FormGridPane();
		form.setPadding(new Insets(0, 0, UIStrategyGUI.Stage.insets * 2, 0));
		form.addRow(new Label(localize("project.name") + ":"), projectName);
		form.addRow(new Label(localize("project.file") + ":"), projectFile, projectFileBrowse);

		this.vspane = new VerticalSplitPane();

		this.images = DataItemListView.newEditableDataItemListView();
		this.images.setEditable(false);
		images.setSelectionMode(SelectionMode.MULTIPLE);
		this.imageFileChooser = new FileChooser();
		imageFileChooser.setTitle(localize("files.import"));
		this.imageDirectoryChooser = new DirectoryChooser();
		imageDirectoryChooser.setTitle(localize("directory.import"));
		final MenuItem importFiles = new MenuItem(localize("files.import"));
		importFiles.setOnAction((e) -> onImportImageFiles());
		images.addMenuItem(importFiles);
		final MenuItem importDirectory = new MenuItem(localize("directory.import"));
		importDirectory.setOnAction((e) -> onImportImageDirectory());
		images.addMenuItem(importDirectory);
		images.addMenuItem(images.getDeleteItemMenuItem(localize("image.any")));

		this.pipelines = DataItemListView.newEditableDataItemListView();
		this.pipelines.setEditable(false);
		pipelines.setSelectionMode(SelectionMode.MULTIPLE);
		final MenuItem setDefaultPipeline = new MenuItem(localize("pipeline.default.set"));
		setDefaultPipeline.disableProperty().bind(Bindings.not(pipelines.hasOneSelectedProperty()));
		setDefaultPipeline.setOnAction((e) -> onSetDefaultPipeline());
		final MenuItem importPipelines = new MenuItem(localize("pipeline.import"));
		importPipelines.setOnAction((e) -> onImportPipelines());
		final MenuItem deletePipelines = pipelines.getDeleteItemMenuItem(
				localize("pipeline.any"),
				(e) -> {
					final List<PipelineData.PipelineItem> selection = pipelines.getSelectedItems();
					if (selection.contains(defaultPipeline)) {
						defaultPipeline = null;
					}
					pipelines.getItems().removeAll(selection);
				}
		);
		pipelines.addMenuItem(setDefaultPipeline);
		pipelines.addMenuItem(importPipelines);
		pipelines.addMenuItem(deletePipelines);
		this.vspane.getLeftChildren().setAll(
				new Label(localize("images") + ":"),
				this.images.getNode()
		);
		this.vspane.getRightChildren().setAll(
				new Label(localize("pipelines") + ":"),
				this.pipelines.getNode()
		);

		this.vbox = new VBox();
		this.vbox.getChildren().setAll(
				this.form,
				this.vspane.getNode()
		);
		this.root.setCenter(this.vbox);

		this.ok = getDefaultButton(localize("project.create"));
		this.cancel = getCancelButton(stage);
		ok.setDisable(true);
		ok.setOnAction(actionEventHandler);
		buttons.add(ok);
		buttons.add(cancel);
	}

	private final EventHandler<ActionEvent> actionEventHandler = (c) -> onAction();

	private void onAction() {
		this.isOk = true;
		stage.close();
	}

	private final InvalidationListener updateListener = (c) -> update();

	private void update() {
		final boolean isValid = isValidSaveFile() && isValidProjectFilename();
		ok.setDisable(!isValid);
	}

	private boolean isValidSaveFile() {
		return savefile != null;
	}

	private boolean isValidProjectFilename() {
		return !projectName.getText().trim().equals("");
	}

	private String getProjectFilename() {
		final String name;
		if (isValidProjectFilename()) {
			name = projectName.getText()
					.trim()
					.replaceAll("[^a-zA-Z0-9.-]", "_");
		} else {
			name = "project";
		}
		return name + "." + ApplicationSettings.projectFileExtension;
	}

	private void onImportImageFiles() {
		imageFileChooser.setInitialDirectory(initialDirectory);
		final List<File> files = this.imageFileChooser.showOpenMultipleDialog(owner);
		if (files != null) {
			for (File file : files) {
				addImageFile(file);
			}
		}
	}

	private void onImportImageDirectory() {
		imageDirectoryChooser.setInitialDirectory(initialDirectory);
		final File directory = this.imageDirectoryChooser.showDialog(owner);
		if (directory != null) {
			try {
				final FileFinder finder = new FileFinder("*.*");
				finder.walkFileTree(directory, FileFinderOption.NONRECURSIVE);
				for (File file : finder.getFiles()) {
					addImageFile(file);
				}
			} catch (IOException ex) {
				log.error("failed to import directory: {}", directory, ex);
			}
		}
	}

	private void addImageFile(File file) {
		final String ext = IOUtils.getFileExtension(file);
		if (!ImageFormat.isSupported(ext)) {
			log.warn("unsupported image file format. File ignored: {}", file);
			return;
		}

		final ImageItem item = new ImageItem(file);
		if (!this.images.getItems().contains(item)) {
			this.images.getItems().add(item);
		}

		initialDirectory = file.getParentFile();
	}

	private void onSetDefaultPipeline() {
		setDefaultPipeline(this.pipelines.getSelectedItem());
	}

	private void setDefaultPipeline(PipelineData.PipelineItem newDefault) {
		if (this.defaultPipeline != null) {
			this.defaultPipeline.glyphProperty().set(null);
		}

		if (newDefault == null) {
			this.defaultPipeline = null;
			return;
		}
		if (newDefault.equals(this.defaultPipeline)) {
			this.defaultPipeline.glyphProperty().set(null);
			this.defaultPipeline = null;
			return;
		}
		this.defaultPipeline = newDefault;
		this.defaultPipeline.glyphProperty().set(MaterialDesignIcons.CROWN);
	}

	private void onImportPipelines() {
		final PipelineLoadDialog load = new PipelineLoadDialog(handler);
		load.showAndWait();
		this.pipelines.getItems().addAll(load.getPipelineItems());
		if (this.defaultPipeline == null && !this.pipelines.getItems().isEmpty()) {
			setDefaultPipeline(this.pipelines.getItems().get(0));
		}
	}

	/**
	 * Checks whether the dialog has been closed after success, or cancelled.
	 *
	 * @return {@code true} if the dialog has been closed after success,
	 * {@code false} if the dialog has been cancelled.
	 */
	public boolean isOk() {
		return this.isOk;
	}

	/**
	 * Returns the name of the new project.
	 *
	 * @return the name of the new project.
	 */
	public String getProjectName() {
		return projectName.getText();
	}

	/**
	 * Returns the path to the new project file.
	 *
	 * @return the path to the new project file.
	 */
	public Path getProjectFile() {
		return savefile.toPath();
	}

	/**
	 * Returns a list of the initial images of the new project.
	 *
	 * @return a list of images.
	 */
	public List<Path> getImages() {
		final List<Path> list = new ArrayList<>();
		for (ImageItem item : this.images.getItems()) {
			list.add(item.getPath());
		}
		return list;
	}

	/**
	 * Returns a list of the initial pipelines of the new project.
	 *
	 * @return a list of pipelines.
	 */
	public List<PipelineData.Pipeline> getPipelines() {
		final List<PipelineData.Pipeline> list = new ArrayList<>();
		for (PipelineData.PipelineItem item : this.pipelines.getItems()) {
			list.add(item.toPipelineData());
		}
		return list;
	}

	/**
	 * Returns the pipeline marked as default.
	 *
	 * @return the pipeline marked as default, or {@code null}.
	 */
	public PipelineData.Pipeline getDefaultPipeline() {
		if (this.defaultPipeline == null) {
			return null;
		}
		return this.defaultPipeline.toPipelineData();
	}

	/**
	 * And image file data item.
	 */
	public static class ImageItem implements DataItemListView.DataItem {

		final private StringProperty nameProperty;
		final private File file;

		/**
		 * Creates a new image file data item.
		 *
		 * @param file the file of the image.
		 */
		public ImageItem(File file) {
			this.nameProperty = new SimpleStringProperty(file.toString());
			this.file = file;
		}

		@Override
		public StringProperty nameProperty() {
			return nameProperty;
		}

		/**
		 * Returns the file of the image.
		 *
		 * @return the file of the image.
		 */
		public File getFile() {
			return this.file;
		}

		/**
		 * Returns the path to the image file.
		 *
		 * @return the path to the image file.
		 */
		public Path getPath() {
			return this.file.toPath();
		}

		@Override
		public int hashCode() {
			if (file == null) {
				return -1;
			}
			return file.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof ImageItem)) {
				return false;
			}
			final ImageItem item = (ImageItem) obj;
			return this.hashCode() == item.hashCode();
		}

	}

}
