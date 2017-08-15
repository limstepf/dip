package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.core.ApplicationDataManager;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ApplicationSettings;
import ch.unifr.diva.dip.core.ImageFormat;
import ch.unifr.diva.dip.core.UserSettings;
import ch.unifr.diva.dip.core.model.Project;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.ui.StylesheetManager;
import ch.unifr.diva.dip.eventbus.EventBus;
import ch.unifr.diva.dip.eventbus.events.ApplicationRequest;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import ch.unifr.diva.dip.eventbus.events.ProjectRequest;
import ch.unifr.diva.dip.eventbus.events.StatusMessageEvent;
import ch.unifr.diva.dip.gui.AbstractPresenter;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.dialogs.ConfirmationDialog;
import ch.unifr.diva.dip.gui.dialogs.ErrorDialog;
import ch.unifr.diva.dip.gui.editor.EditorPresenter;
import ch.unifr.diva.dip.gui.layout.Zoomable;
import ch.unifr.diva.dip.utils.BackgroundTask;
import ch.unifr.diva.dip.utils.CursorLock;
import com.google.common.eventbus.Subscribe;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MainPresenter. Mother of all GUI components, and servant of
 * {@code ApplicationHandler}.
 */
public class MainPresenter extends AbstractPresenter<MainView> {

	private static final Logger log = LoggerFactory.getLogger(MainPresenter.class);

	private final ApplicationHandler handler;
	private final EventBus eventBus;
	private final Scene scene;
	private final Stage stage;

	private final Presenter menuBar;
	private final Presenter statusBar;
	private final EditorPresenter editor;
	private final Presenter sideBar;
	private final Presenter toolBar;
	private final Presenter optionsBar;

	private final List<Parent> splitPaneComponents = new ArrayList<>();

	/**
	 * Creates a new main presenter.
	 *
	 * @param handler application handler.
	 * @param stage JavaFX main stage.
	 * @param view the main view.
	 * @param editor the editor.
	 * @param menuBar the menu bar.
	 * @param statusBar the status bar.
	 * @param sideBar the side bar.
	 * @param toolBar the tool bar.
	 * @param optionsBar the options bar.
	 */
	public MainPresenter(
			ApplicationHandler handler,
			Stage stage,
			MainView view,
			EditorPresenter editor,
			Presenter menuBar,
			Presenter statusBar,
			Presenter sideBar,
			Presenter toolBar,
			Presenter optionsBar
	) {
		super(view);
		this.handler = handler;
		this.eventBus = handler.eventBus;
		this.stage = stage;
		this.menuBar = menuBar;
		this.statusBar = statusBar;
		this.editor = editor;
		this.sideBar = sideBar;
		this.toolBar = toolBar;
		this.optionsBar = optionsBar;

		this.scene = new Scene(this.view.getComponent());
		initView();

		// register shutdown hook
		this.scene.getWindow().setOnCloseRequest(e -> {
			if (!confirmClosingProject()) {
				e.consume(); // consume event, since not allowed to close yet
				return;
			}

			UserSettings.saveDividerPositions(view.getSplitPane(), handler.settings.primaryStage);
		});
	}

	private void initView() {
		view.menuBarProperty().set(menuBar.getComponent());
		view.statusBarProperty().set(statusBar.getComponent());
		view.toolBarProperty().set(toolBar.getComponent());
		view.optionsBarProperty().set(optionsBar.getComponent());
		splitPaneComponents.add(editor.getComponent());
		if (handler.settings.primaryStage.sideBarVisibility.get()) {
			splitPaneComponents.add(sideBar.getComponent());
		}
		updateSplitPaneComponents();
		handler.settings.primaryStage.sideBarVisibility.addListener((c) -> {
			showSideBar(
					handler.settings.primaryStage.sideBarVisibility.get()
			);
		});

		// resize editor and not the sideBar upon resizing the scene/window
		SplitPane.setResizableWithParent(sideBar.getComponent(), false);

		StylesheetManager.getInstance().init(scene);
		stage.setScene(scene);
		stage.setTitle(ApplicationSettings.applicationTitle);

		UserSettings.restoreStage(stage, handler.settings.primaryStage);

	}

	private void showSideBar(boolean show) {
		if (show) {
			if (!splitPaneComponents.contains(sideBar.getComponent())) {
				splitPaneComponents.add(sideBar.getComponent());
			}
		} else {
			UserSettings.saveDividerPositions(view.getSplitPane(), handler.settings.primaryStage);
			splitPaneComponents.remove(sideBar.getComponent());
		}
		updateSplitPaneComponents();
	}

	private void updateSplitPaneComponents() {
		view.getSplitPane().getItems().setAll(splitPaneComponents);
		view.getSplitPane().setDividerPositions(handler.settings.primaryStage.sideBarDivider);
	}

	public void show() {
		stage.show();
	}

	/**
	 * Shows/hides the pipeline editor of the current project.
	 *
	 * @param show {@code true} to show, {@code false} to hide the pipeline
	 * editor.
	 */
	public void openPipelineEditor(boolean show) {
		if (!this.handler.hasProject()) {
			return;
		}
		this.handler.getProject().openPipelineEditor(stage, show);
	}

	/**
	 * Opens the user settings window.
	 */
	public void openUserSettings() {
		final UserSettingsWindow settings = new UserSettingsWindow(stage, this.handler);
		settings.show();
	}

	@Subscribe
	public void applicationRequest(ApplicationRequest event) {
		switch (event.type) {
			case OPEN_PIPELINE_EDITOR:
				openPipelineEditor(true);
				break;
			case OPEN_USER_SETTINGS:
				openUserSettings();
				break;
			case EDITOR_INTERPOLATION:
				this.editor.setInterpolation(
						Zoomable.Interpolation.get(this.handler.settings.editor.interpolation)
				);
				break;
			case EXIT:
				exit();
				break;
			default:
				log.warn("unhandled application request: {}", event.type);
				break;
		}
	}

	@Subscribe
	public void projectRequest(ProjectRequest event) {
		switch (event.type) {
			case NEW:
				newProject();
				break;
			case OPEN:
				openProject();
				break;
			case REPAIR:
				repairProject();
				break;
			case SELECT:
				selectProjectPage(event.page);
				break;
			case IMPORT_PAGES:
				importProjectPages();
				break;
			case PROCESS_PAGE:
				processPage(event.page);
				break;
			case RESET_PAGE:
				resetPage(event.page);
				break;
			case SAVE:
				saveProject();
				break;
			case SAVE_AS:
				saveAsProject();
				break;
			case CLOSE:
				closeProject(true);
				break;
			default:
				log.warn("unhandled project request: {}", event.type);
				break;
		}
	}

	/**
	 * Exit the application.
	 */
	public void exit() {
		if (confirmClosingProject()) {
			Platform.exit();
		}
	}

	/**
	 * Checks if we're save to close the project without losing data. If the
	 * current project has been modified, a save-dialog is openend first to ask
	 * how to proceed (save, discard changes/continue, or cancel).
	 *
	 * @return {@code true} if we're save/allowed to continue, {@code false}
	 * otherwise.
	 */
	public final boolean confirmClosingProject() {
		if (!handler.hasProject()) {
			return true;
		}

		boolean confirmed = true;

		if (handler.isProjectModified()) {
			final ConfirmationDialog confirmation = new ConfirmationDialog(
					localize("save.confirm", handler.getProjectFileName())
			);
			confirmation.showAndWait();
			final ButtonType result = confirmation.getResult();
			switch (result.getButtonData()) {
				case YES:
					handler.saveProject();
					break;
				case NO:
					break;
				case CANCEL_CLOSE:
				default:
					confirmed = false;
					break;
			}
		}

		return confirmed;
	}

	/**
	 * Opens a dialog to create a new project.
	 */
	public void newProject() {
		if (confirmClosingProject()) {
			final NewProjectDialog dialog = new NewProjectDialog(
					stage,
					handler
			);
			dialog.showAndWait();
			if (dialog.isOk()) {
				this.closeProject(false);
				handler.newProject(
						dialog.getProjectName(),
						dialog.getProjectFile(),
						dialog.getImages(),
						dialog.getPipelines(),
						dialog.getDefaultPipeline()
				);
			}
		}
	}

	/**
	 * Opens a dialog to open a project.
	 */
	public void openProject() {
		if (confirmClosingProject()) {
			final FileChooser chooser = new FileChooser();
			chooser.setInitialDirectory(handler.getRecentSaveDirectory().toFile());
			chooser.setTitle(localize("project.open"));
			ApplicationSettings.setProjectExtensionFilter(chooser);
			final File file = chooser.showOpenDialog(stage);

			if (file != null) {
				this.closeProject(false);
				handler.openProject(file.toPath());
			}
		}
	}

	// should have been opened by now, but validation failed. Chances are we can
	// still repair and open the project afterall...
	private void repairProject() {
		if (!handler.hasRepairData()) {
			return;
		}

		final RepairProjectDialog dialog = new RepairProjectDialog(stage, handler);
		dialog.showAndWait();

		if (dialog.isOk()) {
			handler.openRepairedProject();
		}

		handler.clearRepairData();
	}

	/**
	 * Selects a project page/image.
	 *
	 * @param page id of the project page.
	 * @return the started background task.
	 */
	public BackgroundTask<Void> selectProjectPage(int page) {
		final Project project = handler.getProject();
		final ProjectPage currentPage = project.getSelectedPage();
		final boolean isDirty = (currentPage == null) ? false : currentPage.isModified();
		final int selected = handler.getProject().getSelectedPageId();

		final CursorLock cursorLock = new CursorLock(handler, Cursor.WAIT);
		final BackgroundTask<Void> task = new BackgroundTask<Void>(handler) {
			@Override
			protected Void call() throws Exception {
				updateTitle(localize("page.selecting"));
				updateProgress(-1, Double.NaN);
				if (isDirty) {
					// this might take a bit longer, so...
					updateMessage(localize("page.saving"));
					// explicit call to closePage (which otherwise would be called
					// by selectPage below) to separate saving the current, dirty
					// page, and loading of the (assets of) the new page
					project.closePage();
				}
				updateMessage(localize("page.loading"));
				project.selectPage(page);
				return null;
			}

			@Override
			protected void finished(BackgroundTask.Result result) {
				final int id = project.getSelectedPageId();
				if (selected != id) {
					eventBus.post(new ProjectNotification(
							ProjectNotification.Type.SELECTED,
							page
					));
					eventBus.post(new StatusMessageEvent(localize("page.selected")));
				}
				cursorLock.stop();
			}
		};
		task.start();
		return task;
	}

	/**
	 * Opens a dialog to import project pages/images.
	 */
	public void importProjectPages() {
		final Path initialPath = handler.settings.recentFiles.getSaveDirectory();
		final File initialDirectory = (initialPath == null)
				? ApplicationDataManager.userDirectory().toFile()
				: initialPath.toFile();

		final FileChooser chooser = new FileChooser();
		chooser.setTitle(localize("files.import"));
		chooser.setInitialDirectory(initialDirectory);
		ImageFormat.setExtensionFilter(chooser);
		final List<File> files = chooser.showOpenMultipleDialog(this.stage);
		if (files != null) {
			for (File file : files) {
				try {
					handler.getProject().addPage(file);
				} catch (IOException ex) {
					log.warn("failed to import page: {}", file, ex);
					final ErrorDialog dialog = new ErrorDialog(ex);
					dialog.showAndWait();
				}
			}
		}
	}

	/**
	 * Processes the page with the given id, or all pages with {@code -1}.
	 *
	 * @param pageId the page id, or {@code -1}.
	 */
	public void processPage(int pageId) {
		if (!handler.hasProject()) {
			return;
		}
		handler.getProject().processPage(pageId);
	}

	/**
	 * Resets the page with the given id, or all pages with {@code -1}.
	 *
	 * @param pageId the page id, or {@code -1}.
	 */
	public void resetPage(int pageId) {
		if (!handler.hasProject()) {
			return;
		}
		handler.getProject().resetPage(pageId);
	}

	/**
	 * Saves the current project.
	 */
	public void saveProject() {
		handler.saveProject();
	}

	/**
	 * Opens a dialog to save the project at a new location.
	 */
	public void saveAsProject() {
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(ApplicationDataManager.userDirectory().toFile());
		chooser.setTitle(localize("project.save.as"));
		chooser.getExtensionFilters().add(ApplicationSettings.projectFileExtensionFilter);
		final File file = chooser.showSaveDialog(stage);

		if (file != null) {
			handler.saveAsProject(file.toPath());
		}
	}

	/**
	 * Closes a project.
	 *
	 * @param confirm asks for confirmation first if set to {@code true}, closes
	 * the project immediately if {@code false}.
	 */
	public void closeProject(boolean confirm) {
		if (confirm) {
			if (!confirmClosingProject()) {
				return;
			}
		}

		handler.closeProject();
	}

}
