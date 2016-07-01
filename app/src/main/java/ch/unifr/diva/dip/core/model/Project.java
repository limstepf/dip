package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategy.Answer;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import ch.unifr.diva.dip.utils.Modifiable;
import ch.unifr.diva.dip.utils.ModifiedProperty;
import ch.unifr.diva.dip.gui.pe.PipelineEditor;
import ch.unifr.diva.dip.utils.ZipFileSystem;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A project is a list of pages.
 */
public class Project implements Modifiable, Localizable {

	private static final Logger log = LoggerFactory.getLogger(Project.class);
	private final ApplicationHandler handler;

	/**
	 * Original savefile of the project. This file is generally not
	 * opened/loaded as a zip filesystem. We're working with a temp. working
	 * copy of this file that gets copied back to this file upon saving the
	 * project.
	 */
	private Path file;

	/**
	 * Temporary working copy of {@code file}, the original savefile.
	 */
	private final Path zipFile;

	/**
	 * The (opened) zip file system holding the contents of {@code zipFile},
	 * which is a working copy of {@code file}, the original savefile. This
	 * means that we can always make this file system dirty, remove and
	 * overwrite files or what not, without having to fear that we messed up the
	 * original savefile, e.g. in case the user doesn't really want to save his
	 * work.
	 */
	private ZipFileSystem zip;

	/**
	 * Pipeline manager holding the project's processing pipelines. Each page is
	 * assigned to one such pipeline, and while a page still might overwrite
	 * some parameters of certain processors the structure of the pipline itself
	 * can not be altered (on a per page level).
	 */
	private final PipelineManager pipelineManager;

	/**
	 * The project's pipeline editor.
	 */
	private PipelineEditor pipelineEditor;

	private int maxPageId = 0;
	private final StringProperty projectNameProperty = new SimpleStringProperty();
	private final ModifiedProperty modifiedProjectProperty = new ModifiedProperty();
	private final IntegerProperty selectedPageIdProperty = new SimpleIntegerProperty(-1);
	private final ObservableList<ProjectPage> pages = FXCollections.observableArrayList();

	/**
	 * Factory method to create a new project.
	 *
	 * @param data initial project data.
	 * @param handler
	 * @return a project.
	 * @throws Exception in case of I/O errors.
	 */
	public static Project newProject(ProjectData data, ApplicationHandler handler) throws Exception {
		data.zipFile = handler.dataManager.tmpFile(true);
		final ZipFileSystem zip = ZipFileSystem.create(data.zipFile);

		try (OutputStream stream = new BufferedOutputStream(zip.getOutputStream(ProjectData.PROJECT_ROOT_XML))) {
			data.save(stream);
			data.zip = zip;
		} catch (Throwable throwable) {
			throw (throwable);
		}

		return new Project(data, handler);
	}

	/**
	 * Factory method to open an existing project.
	 *
	 * @param data project data assumed to be already validated.
	 * @param handler
	 * @return a project.
	 */
	public static Project openProject(ProjectData data, ApplicationHandler handler) {
		return new Project(data, handler);
	}

	/**
	 * Default constructor.
	 *
	 * @param data project data (with an open zip filesystem).
	 * @param handler
	 */
	private Project(ProjectData data, ApplicationHandler handler) {
		this.handler = handler;
		this.file = data.file;
		this.zipFile = data.zipFile;
		this.zip = data.zip;

		// read/parse project data
		projectNameProperty.set(data.name);

		// images -> pages
		for (ProjectData.Page image : data.getPages()) {
			if (image.id > maxPageId) {
				maxPageId = image.id;
			}
			pages.add(new ProjectPage(this, image));
		}

		// pipelines
		pipelineManager = new PipelineManager(
				this.handler,
				data.pipelines(),
				data.defaultPipeline
		);

		// hook up properties, attach listeners (for modifications, ...)
		selectPage(data.getSelectedPage());

		// listen to properties for modifications
		modifiedProjectProperty.addObservedProperty(projectNameProperty);
		modifiedProjectProperty.addObservedProperty(selectedPageIdProperty);

		// listen to (and manage) modifiables for modifications
		modifiedProjectProperty.addManagedProperty(pipelineManager);
		for (ProjectPage page : pages) {
			modifiedProjectProperty.addManagedProperty(page);
		}

	}

	/**
	 * Closes the project and its resources.
	 */
	public void close() {
		closePage();
		pipelineEditor = null;

		if (zip != null) {
			try {
				zip.close();
			} catch (IOException ex) {
				log.warn("closing zip failed: {}", zip, ex);
			}
		}
	}

	/**
	 * Closes the currently selected page.
	 */
	private void closePage() {
		final ProjectPage currentPage = getSelectedPage();
		if (currentPage != null) {
			currentPage.close();
		}
	}

	/**
	 * Saves the project.
	 *
	 * @throws Exception in case of an I/O error.
	 */
	public void save() throws Exception {
		saveInternal();
	}

	/**
	 * Sets the file (or savefile) of the project. The original savefile is left
	 * untouched (even if marked as modified), while a new copy of the project
	 * is created at the given location.
	 *
	 * @param file path to the new project file.
	 * @throws Exception in case of an I/O error.
	 */
	public void saveAs(Path file) throws Exception {
		this.file = file;
		saveInternal();
	}

	/**
	 * Internal save method used by {@code save} and {@code saveAs} methods.
	 *
	 * @throws Exception in case of an I/O error.
	 */
	private void saveInternal() throws Exception {
		final ProjectData data = new ProjectData(this);

		// save current pipeline (e.g. the object map)
		this.getSelectedPage().getPipeline().save();

		// write project root xml
		Files.deleteIfExists(zip.getPath(ProjectData.PROJECT_ROOT_XML));

		try (OutputStream stream = new BufferedOutputStream(zip.getOutputStream(ProjectData.PROJECT_ROOT_XML))) {
			data.save(stream);
		} catch (Throwable throwable) {
			throw (throwable);
		}

		// write pipeline data
		Files.deleteIfExists(zip.getPath(ProjectData.PROJECT_PIPELINES_XML));
		try (OutputStream stream = new BufferedOutputStream(zip.getOutputStream(ProjectData.PROJECT_PIPELINES_XML))) {
			pipelineManager.exportPipelines(stream);
		} catch (Throwable throwable) {
			throw (throwable);
		}

		// copy tmp. working copy back to original file
		zip.close();
		Files.copy(zipFile, file, StandardCopyOption.REPLACE_EXISTING);
		zip = ZipFileSystem.open(zipFile);

		// mark project (and managed modifiables) as clean/unmodified
		modifiedProjectProperty.set(false);
	}

	/**
	 * Returns the file (or savefile) of the project.
	 *
	 * @return path to the project file.
	 */
	public Path getFile() {
		return this.file;
	}

	/**
	 * Returns the (opened) zip file system (or savefile) of the project.
	 * @return the zip file system of the project.
	 */
	public ZipFileSystem zipFileSystem() {
		return this.zip;
	}

	/**
	 * Sets the name of the project.
	 *
	 * @param name the name of the project.
	 */
	public void setProjectName(String name) {
		projectNameProperty.set(name);
	}

	/**
	 * Returns the name of the project.
	 *
	 * @return the name of the project.
	 */
	public String getProjectName() {
		return projectNameProperty.get();
	}

	/**
	 * Property of the project name.
	 *
	 * @return projectNameProperty.
	 */
	public StringProperty projectNameProperty() {
		return projectNameProperty;
	}

	/**
	 * Returns the filename of the projects savefile.
	 *
	 * @return the projects filename.
	 */
	public String getFilename() {
		return file.getFileName().toString();
	}

	/**
	 * Returns the pages of the project.
	 *
	 * @return a list of all pages of the project.
	 */
	public ObservableList<ProjectPage> pages() {
		return pages;
	}

	/**
	 * Returns the first page of the project.
	 *
	 * @return the first project page.
	 */
	public ProjectPage getFirstPage() {
		if (pages.isEmpty()) {
			return null;
		}
		return pages.get(0);
	}

	/**
	 * Returns the id of the first page of the project.
	 *
	 * @return id of the first project page.
	 */
	public int getFirstPageId() {
		if (pages.isEmpty()) {
			return -1;
		}
		return pages.get(0).id;
	}

	/**
	 * Returns the project's pipeline manager.
	 *
	 * @return the project's pipeline manager.
	 */
	public PipelineManager pipelineManager() {
		return pipelineManager;
	}

	/**
	 * Returns the application handler.
	 *
	 * @return the application handler.
	 */
	protected ApplicationHandler applicationHandler() {
		return handler;
	}

	/**
	 * Shows/hides the project's pipeline editor.
	 *
	 * @param stage the parent stage.
	 * @param show True to show, False to hide the pipeline editor.
	 */
	public void openPipelineEditor(Stage stage, boolean show) {
		if (pipelineEditor == null) {
			pipelineEditor = new PipelineEditor(stage, handler, pipelineManager());
		}

		if (show) {
			pipelineEditor.show();
		} else {
			pipelineEditor.close();
		}
	}

	/**
	 * Property of the currently selected page id.
	 *
	 * @return selectedPageIdProperty.
	 */
	public ReadOnlyIntegerProperty selectedPageIdProperty() {
		return selectedPageIdProperty;
	}

	/**
	 * Selects a project page by id.
	 *
	 * @param id an id of a project page.
	 */
	public final void selectPage(int id) {
		if (getSelectedPageId() == id) {
			return;
		}

		closePage();

		final ProjectPage page = getPage(id);
		if (page != null) {
			page.open();
			selectedPageIdProperty.set(id);
		} else {
			selectedPageIdProperty.set(-1);
		}
	}

	/**
	 * Returns the id of the currently selected project page.
	 *
	 * @return id of the selected project page.
	 */
	public int getSelectedPageId() {
		return selectedPageIdProperty.get();
	}

	/**
	 * Returns the currently selected page.
	 *
	 * @return the selected project page or null if no page is selected.
	 */
	public ProjectPage getSelectedPage() {
		if (getSelectedPageId() < 0) {
			return null;
		}

		return getPage(getSelectedPageId());
	}

	/**
	 * Returns a project page by id.
	 *
	 * @param id id of a project page.
	 * @return a project page - or null if not found.
	 */
	private ProjectPage getPage(int id) {
		for (ProjectPage page : pages) {
			if (id == page.id) {
				return page;
			}
		}
		return null;
	}

	public void addPage(File file) throws IOException {
		addPage(file.toPath());
	}

	public void addPage(Path file) throws IOException {
		addPage(new ProjectPage(this, file));
	}

	public void addPage(ProjectPage page) {
		this.modifiedProjectProperty.addManagedProperty(page);
		pages.add(page);
	}

	public void deletePage(int id) {
		deletePage(getPage(id));
	}

	public void deletePage(ProjectPage page) {
		final List<ProjectPage> list = Arrays.asList(page);
		deletePages(list);
	}

	public void deletePages(List<ProjectPage> selection) {
		deletePages(selection, true);
	}

	public void deletePages(List<ProjectPage> selection, boolean confirm) {
		if (confirm) {
			final List<String> names = new ArrayList<>();
			for (ProjectPage page : selection) {
				names.add(page.getName());
			}
			final String msg = localize("delete.confirm", String.join(", ", names));
			final Answer answer = handler.uiStrategy.getAnswer(msg);

			switch (answer) {
				case YES:
					break;
				case NO:
				case CANCEL:
					return;
			}
		}

		for (ProjectPage page : selection) {
			if (!pages.contains(page)) {
				continue;
			}

			// TODO: clean zip-filesystem
			log.info("deleting page: {}", page);
			this.modifiedProjectProperty.removeManagedProperty(page);
			pages.remove(page);
			this.handler.eventBus.post(
					new ProjectNotification(ProjectNotification.Type.PAGE_REMOVED, page.id)
			);
		}
	}

	protected int newPageId() {
		maxPageId++;
		return maxPageId;
	}

	@Override
	public ModifiedProperty modifiedProperty() {
		return modifiedProjectProperty;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "name=" + this.getProjectName()
				+ ", pages=" + pages.size()
				+ "}";
	}

}
