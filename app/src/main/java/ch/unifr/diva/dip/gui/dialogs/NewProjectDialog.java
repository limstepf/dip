package ch.unifr.diva.dip.gui.dialogs;

import ch.unifr.diva.dip.core.ApplicationSettings;
import ch.unifr.diva.dip.core.ImageFormat;
import ch.unifr.diva.dip.gui.layout.SelectableFile;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.gui.layout.SelectableFileCell;
import ch.unifr.diva.dip.utils.FileFinder;
import ch.unifr.diva.dip.utils.FileFinderOption;
import ch.unifr.diva.dip.utils.IOUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
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

	private final FormGridPane form = new FormGridPane();
	private final TextField projectName = new TextField();
	private boolean validProjectName = false;

	private final TextField projectFile = new TextField();
	private final FileChooser projectFileChooser = new FileChooser();
	private final Button projectFileBrowse = new Button(localize("save.as"));

	private final ChoiceBox projectPipeline = new ChoiceBox();

	private final ListView<SelectableFile> imageSetListView = new ListView<>();
	private final FormGridPane imageSetButtonPane = new FormGridPane();
	private final Button imageSetImportFiles = new Button(localize("files.import"));
	private final Button imageSetImportDirectory = new Button(localize("directory.import"));
	private final FileChooser imageSetFileChooser = new FileChooser();
	private final DirectoryChooser imageSetDirectoryChooser = new DirectoryChooser();

	private final Button ok = getDefaultButton(localize("project.create"));
	private final Button cancel = getCancelButton(stage);
	private boolean done = false;

	private File savefile = null;

	/**
	 * Creates a new project dialog/wizard.
	 *
	 * @param owner parent window
	 * @param initialDirectory initial directory for file/directory chooser.
	 */
	public NewProjectDialog(Window owner, Path initialDirectory) {
		this(owner, initialDirectory.toFile());
	}

	/**
	 * Creates a new project dialog/wizard.
	 *
	 * @param owner parent window
	 * @param initialDirectory initial directory for file/directory chooser.
	 */
	public NewProjectDialog(Window owner, File initialDirectory) {
		super(owner);
		setTitle(localize("project.new"));

		projectName.textProperty().addListener((observable, oldValue, newValue) -> {
			validProjectName = !projectName.getText().trim().equals("");
			validate();
		});

		projectFile.setDisable(true);
		projectFileBrowse.setMaxWidth(Double.MAX_VALUE);

		projectFileChooser.setTitle(localize("save.as"));
		projectFileChooser.setInitialDirectory(initialDirectory);
		ApplicationSettings.setProjectExtensionFilter(projectFileChooser);
		projectFileBrowse.setOnAction((e) -> {
			projectFileChooser.setInitialFileName(getProjectFilename());
			final File file = projectFileChooser.showSaveDialog(owner);
			if (file != null) {
				savefile = file;
				projectFile.setText(file.toString());
			}
			validate();
		});

		projectPipeline.setMaxWidth(Double.MAX_VALUE);
		projectPipeline.getItems().addAll(localize("pipeline.new"), new Separator(),
				"GraphManuscribble", "Pipeline X");
		projectPipeline.getSelectionModel().selectFirst();

		imageSetListView.setCellFactory((c) -> new SelectableFileCell());
		imageSetListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		imageSetButtonPane.setStyle("-fx-hgap:0;");

		imageSetImportFiles.setMaxWidth(Double.MAX_VALUE);
		imageSetButtonPane.addRow(imageSetImportFiles);
		imageSetFileChooser.setTitle(localize("files.import"));
		imageSetFileChooser.setInitialDirectory(initialDirectory);
		ImageFormat.setExtensionFilter(imageSetFileChooser);
		imageSetImportFiles.setOnAction((e) -> {
			final List<File> files = imageSetFileChooser.showOpenMultipleDialog(owner);
			if (files != null) {
				for (File file : files) {
					addFile(file);
				}
			}
		});

		imageSetImportDirectory.setMaxWidth(Double.MAX_VALUE);
		imageSetButtonPane.addRow(imageSetImportDirectory);
		imageSetDirectoryChooser.setTitle(localize("directory.import"));
		imageSetDirectoryChooser.setInitialDirectory(initialDirectory);
		imageSetImportDirectory.setOnAction((e) -> {
			final File directory = imageSetDirectoryChooser.showDialog(owner);
			if (directory != null) {
				try {
					final FileFinder finder = new FileFinder("*.*");
					finder.walkFileTree(directory, FileFinderOption.NONRECURSIVE);
					for (File file : finder.getFiles()) {
						addFile(file);
					}
				} catch (IOException ex) {
					log.error("failed to import directory: {}", directory, ex);
				}
			}
		});

		form.addRow(new Label(localize("project.name") + ":"), projectName);
		form.addRow(new Label(localize("project.file") + ":"), projectFile, projectFileBrowse);
		form.addRow(new Label(localize("pipeline") + ":"), projectPipeline);
		form.addRow(new Label(localize("image.set") + ":"), imageSetListView, imageSetButtonPane);

		ok.setDisable(true);
		ok.setOnAction((e) -> {
			stage.hide();
			done = true;
		});
		buttons.add(ok);
		buttons.add(cancel);

		root.setCenter(form);
	}

	private void addFile(File file) {
		final SelectableFile item = new SelectableFile(file);
		if (!imageSetListView.getItems().contains(item)) {
			final String extension = IOUtils.getFileExtension(file);

			if (!ImageFormat.isSupported(extension)) {
				item.setDisable(true);
			}

			imageSetListView.getItems().add(item);
		}
	}

	private String getProjectFilename() {
		String name = "project";
		if (validProjectName) {
			name = projectName.getText()
					.trim()
					.replaceAll("[^a-zA-Z0-9.-]", "_");
		}
		return name + "." + ApplicationSettings.projectFileExtension;
	}

	private void validate() {
		boolean isValid = (savefile != null) && validProjectName;
		ok.setDisable(!isValid);
	}

	public boolean isOk() {
		return done;
	}

	public String getProjectName() {
		return projectName.getText();
	}

	public Path getProjectFile() {
		return savefile.toPath();
	}

	public String getProcessingPipeline() {
		return projectPipeline.getSelectionModel().getSelectedItem().toString();
	}

	public List<Path> getImageSet() {
		final List<Path> set = new ArrayList<>();
		for (SelectableFile file : imageSetListView.getItems()) {
			if (file.isSelected()) {
				set.add(file.getPath());
			}
		}
		return set;
	}
}
