package ch.unifr.diva.dip.core;

import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.core.ui.UIStrategy;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.model.ProjectData;
import ch.unifr.diva.dip.eventbus.EventBus;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import ch.unifr.diva.dip.eventbus.events.StatusMessageEvent;
import ch.unifr.diva.dip.core.model.Project;
import ch.unifr.diva.dip.eventbus.events.ProjectRequest;
import ch.unifr.diva.dip.osgi.OSGiFramework;
import ch.unifr.diva.dip.utils.BackgroundTask;
import ch.unifr.diva.dip.utils.ZipFileSystem;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ApplicationHandler (Chain of Responsibility).
 */
public class ApplicationHandler implements Localizable {

	private static final Logger log = LoggerFactory.getLogger(ApplicationHandler.class);
	private final ApplicationContext context;

	/**
	 * The application data manager.
	 */
	public final ApplicationDataManager dataManager;
	/**
	 * Application wide thread pool/executor service.
	 */
	public final DipThreadPool threadPool;
	/**
	 * OSGi framework.
	 */
	public final OSGiFramework osgi;
	/**
	 * The user settings.
	 */
	public final UserSettings settings;
	/**
	 * The UI strategy.
	 */
	public final UIStrategy uiStrategy;
	/**
	 * The application's event bus.
	 */
	public final EventBus eventBus;

	// open/current project
	private volatile Project project = null;
	// pointers to invalid/corrupt project data; might be fixed and still opened
	private volatile ProjectData projectData = null;
	private volatile ProjectData.ValidationResult projectValidation = null;

	private final ReadOnlyBooleanWrapper hasProjectProperty = new ReadOnlyBooleanWrapper();
	private final ReadOnlyBooleanWrapper modifiedProjectProperty = new ReadOnlyBooleanWrapper();
	private final ReadOnlyIntegerWrapper selectedPageProperty = new ReadOnlyIntegerWrapper();

	/**
	 * ApplicationHandler constructor.
	 *
	 * @param context the ApplicationContext.
	 * @param uiStrategy an UIStrategy (for error handling, confirmations, ...).
	 * @param eventBus the eventbus.
	 */
	public ApplicationHandler(
			ApplicationContext context,
			UIStrategy uiStrategy,
			EventBus eventBus
	) {
		this.context = context;
		this.dataManager = context.dataManager;
		this.threadPool = context.threadPool;
		this.osgi = context.osgi;
		this.settings = context.settings;
		this.uiStrategy = uiStrategy;
		this.eventBus = eventBus;
	}

	public void newProject(String name, Path saveFile, String pipeline, List<Path> imageSet) {
		if (hasProject()) {
			closeProject();
		}

		BackgroundTask<Void> task = new BackgroundTask<Void>(
				uiStrategy,
				eventBus
		) {
			@Override
			protected Void call() throws Exception {
				updateTitle(localize("project.new"));
				updateMessage(localize("project.creating"));
				updateProgress(-1, Double.NaN);

				final ProjectData projectData = new ProjectData(
						name,
						saveFile,
						imageSet
				);

				project = Project.newProject(projectData, ApplicationHandler.this);

				return null;
			}

			@Override
			protected void cleanUp() {
				cleanUpProject();
			}

			@Override
			protected void succeeded() {
				runLater(() -> {
					initProject();
					eventBus.post(new ProjectNotification(
							ProjectNotification.Type.OPENED
					));
					eventBus.post(new StatusMessageEvent(
							localize("project.created")
					));
				});
			}
		};
		task.start();
	}

	public void openProject(Path saveFile) {
		if (hasProject()) {
			closeProject();
		}

		BackgroundTask<ProjectData> task = new BackgroundTask<ProjectData>(
				uiStrategy,
				eventBus
		) {
			@Override
			protected ProjectData call() throws Exception {
				updateTitle(localize("project.open"));
				updateMessage(localize("project.open"));
				updateProgress(-1, Double.NaN);

				// tmp. working copy
				final Path zipFile = dataManager.tmpCopy(saveFile);

				final ZipFileSystem zip = ZipFileSystem.open(zipFile);
				ProjectData projectData;
				Exception loadingException = null;
				try (InputStream stream = new BufferedInputStream(zip.getInputStream(ProjectData.PROJECT_ROOT_XML))) {
					projectData = ProjectData.load(stream);
					projectData.file = saveFile;
					projectData.zipFile = zipFile;
					projectData.zip = zip;
				} catch (Exception ex) {
					log.error("failed to unmarshall project data: {}", saveFile, ex);
					throw (ex);
				}

				return projectData;
			}

			@Override
			protected void cleanUp() {
				final ProjectData data = getValue();
				if (data != null) {
					final ZipFileSystem zip = data.zip;
					if (zip != null) {
						try {
							zip.close();
						} catch (IOException ex) {
							log.warn("closing zip failed: {}", zip, ex);
						}
					}
				}
				cleanUpProject();
			}

			@Override
			protected void failed() {
				// try to repair project data
				cleanUp();
				// ...or show error if unfixable
				uiStrategy.showError(this.getException());
			}

			@Override
			protected void succeeded() {

				// validation comes here!
				ProjectData.ValidationResult validation = getValue().validate(
						ApplicationHandler.this
				);

				// TODO: auto-repair for moved/modified-only files (-> user settings)
				if (!validation.isValid()) {
					log.warn("failed to open project: {}", getValue());
					log.warn("invalid/corrupt project data: {}", validation);

					projectData = getValue();
					projectValidation = validation;

					runLater(() -> {
						eventBus.post(new ProjectRequest(ProjectRequest.Type.REPAIR));
					});
				} else {
					project = Project.openProject(getValue(), ApplicationHandler.this);
					broadcastOpenProject();
				}
			}
		};
		task.start();
	}

	private void broadcastOpenProject() {
		Platform.runLater(() -> {
			initProject();
			eventBus.post(new ProjectNotification(
					ProjectNotification.Type.OPENED
			));
			eventBus.post(new StatusMessageEvent(
					localize("project.opened")
			));
		});
	}

	public boolean hasRepairData() {
		return (this.projectData != null && this.projectValidation != null);
	}

	public ProjectData getRepairData() {
		return this.projectData;
	}

	public ProjectData.ValidationResult getRepairValidation() {
		return this.projectValidation;
	}

	public void clearRepairData() {
		this.projectData = null;
		this.projectValidation = null;
	}

	public void applyRepairsAndOpen() {
		if (!hasRepairData()) {
			return;
		}
		this.project = Project.openProject(this.projectData, ApplicationHandler.this);
		this.project.modifiedProperty().set(true);
		broadcastOpenProject();
	}

	public void saveProject() {
		if (!hasProject()) {
			return;
		}

		BackgroundTask<Integer> task = new BackgroundTask<Integer>(
				uiStrategy,
				eventBus
		) {
			@Override
			protected Integer call() throws Exception {
				updateTitle(localize("project.save"));
				updateMessage(localize("project.save"));
				updateProgress(-1, Double.NaN);

				project.save();
				return 0;
			}

			@Override
			protected void succeeded() {
				eventBus.post(new StatusMessageEvent(localize("project.saved")));
			}
		};
		task.start();
	}

	public void saveAsProject(Path saveFile) {
		if (!hasProject()) {
			return;
		}

		BackgroundTask<Integer> task = new BackgroundTask<Integer>(
				uiStrategy,
				eventBus
		) {
			@Override
			protected Integer call() throws Exception {
				updateTitle(localize("project.save"));
				updateMessage(localize("project.save"));
				updateProgress(-1, Double.NaN);

				project.saveAs(saveFile);
				return 0;
			}

			@Override
			protected void succeeded() {
				eventBus.post(new StatusMessageEvent(localize("project.saved")));
			}
		};
		task.start();
	}

	/**
	 * Inits/hooks up a project after creating a new one or loading a saved one.
	 */
	private void initProject() {
		if (project == null) {
			return;
		}

		// wire up project
		hasProjectProperty.set(true);
		modifiedProjectProperty.bind(project.modifiedProperty().getObservableValue());
		selectedPageProperty.bind(project.selectedPageIdProperty());
	}

	public void closeProject() {
		if (!hasProject()) {
			return;
		}

		// broadcast: pre-close
		eventBus.post(new ProjectNotification(ProjectNotification.Type.CLOSING));

		cleanUpProject();

		modifiedProjectProperty.unbind();
		modifiedProjectProperty.set(false);
		selectedPageProperty.unbind();
		selectedPageProperty.set(-1);

		// broadcast: post-close
		eventBus.post(new ProjectNotification(ProjectNotification.Type.CLOSED));
	}

	/**
	 * Cleans up/frees project resources. Called upon closing a project, or
	 * after failing to load one.
	 */
	private void cleanUpProject() {
		if (project != null) {
			project.close();
		}
		project = null;
		hasProjectProperty.set(false);
	}

	public Project getProject() {
		return project;
	}

	public boolean hasProject() {
		return hasProjectProperty.get();
	}

	public ReadOnlyBooleanProperty hasProjectProperty() {
		return hasProjectProperty.getReadOnlyProperty();
	}

	public ReadOnlyIntegerProperty selectedPageProperty() {
		return selectedPageProperty.getReadOnlyProperty();
	}

	public ReadOnlyBooleanProperty modifiedProjectProperty() {
		return modifiedProjectProperty.getReadOnlyProperty();
	}

	public boolean isProjectModified() {
		if (!hasProject()) {
			return false;
		}

		return modifiedProjectProperty.get();
	}

	public String getProjectFileName() {
		return project.getFilename();
	}

	/**
	 * Writes application settings back to disk.
	 *
	 * @return True in case of success, false otherwise.
	 */
	public boolean saveUserSettings() {
		try {
			settings.save(dataManager.appDataDir.settingsFile);
			return true;
		} catch (JAXBException ex) {
			log.error(
					"error writing: {}",
					dataManager.appDataDir.settingsFile,
					ex
			);
			return false;
		}
	}

}
