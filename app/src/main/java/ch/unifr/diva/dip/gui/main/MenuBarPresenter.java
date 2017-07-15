package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.gui.VisibilityMode;
import ch.unifr.diva.dip.api.ui.ToggleGroupValue;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.eventbus.EventBus;
import ch.unifr.diva.dip.eventbus.events.ApplicationRequest;
import ch.unifr.diva.dip.eventbus.events.ProjectRequest;
import ch.unifr.diva.dip.eventbus.events.SelectionMaskRequest;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.editor.EditorPresenter;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * The main menu bar of the application.
 */
public class MenuBarPresenter implements Presenter, Localizable {

	private final ApplicationHandler handler;
	private final EditorPresenter editor;
	private final EventBus eventBus;
	private final Stage stage;
	private final MenuBar menuBar;
	private final Menu fileMenu, editMenu, selectionMenu, processMenu, viewMenu, helpMenu;
	private final BooleanBinding hasNoProjectBinding;

	/**
	 * Creates a new main menu bar.
	 *
	 * @param handler the application handler.
	 * @param editor the editor presenter.
	 * @param stage the stage.
	 */
	public MenuBarPresenter(ApplicationHandler handler, EditorPresenter editor, Stage stage) {
		this.handler = handler;
		this.eventBus = handler.eventBus;
		this.editor = editor;
		this.stage = stage;

		this.hasNoProjectBinding = Bindings.not(handler.hasProjectProperty());

		menuBar = new MenuBar();

		fileMenu = getFileMenu();
		editMenu = getEditMenu();
		selectionMenu = getSelectionMenu();
		processMenu = getProcessMenu();
		viewMenu = getViewMenu();
		helpMenu = getHelpMenu();

		menuBar.getMenus().addAll(
				fileMenu, editMenu, selectionMenu, processMenu, viewMenu, helpMenu
		);
	}

	private Menu getFileMenu() {
		final Menu menu = new Menu(localize("file"));

		final MenuItem newProject = new MenuItem(localize("project.new"));
		newProject.setMnemonicParsing(true);
		newProject.setOnAction(e -> {
			eventBus.post(new ProjectRequest(ProjectRequest.Type.NEW));
		});
		newProject.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));

		final MenuItem openProject = new MenuItem(localize("project.open"));
		openProject.setOnAction(e -> {
			eventBus.post(new ProjectRequest(ProjectRequest.Type.OPEN));
		});
		openProject.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));

		final MenuItem closeProject = new MenuItem(localize("project.close"));
		closeProject.setOnAction(e -> {
			eventBus.post(new ProjectRequest(ProjectRequest.Type.CLOSE));
		});
		closeProject.disableProperty().bind(hasNoProjectBinding);

		final MenuItem saveProject = new MenuItem(localize("project.save"));
		saveProject.setOnAction(e -> {
			eventBus.post(new ProjectRequest(ProjectRequest.Type.SAVE));
		});
		final BooleanBinding canSave = Bindings.and(
				handler.hasProjectProperty(),
				handler.modifiedProjectProperty()
		);
		saveProject.disableProperty().bind(Bindings.not(canSave));
		saveProject.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));

		final MenuItem saveAsProject = new MenuItem(localize("project.save.as"));
		saveAsProject.setOnAction(e -> {
			eventBus.post(new ProjectRequest(ProjectRequest.Type.SAVE_AS));
		});
		saveAsProject.disableProperty().bind(hasNoProjectBinding);

		final MenuItem exitApp = new MenuItem(localize("exit"));
		exitApp.setOnAction(e -> {
			eventBus.post(new ApplicationRequest(ApplicationRequest.Type.EXIT));
		});
		exitApp.setAccelerator(KeyCombination.keyCombination("Alt+F4"));

		menu.getItems().addAll(
				newProject, openProject, closeProject, saveProject, saveAsProject, exitApp
		);
		return menu;
	}

	private Menu getEditMenu() {
		final Menu menu = new Menu(localize("edit"));

		final MenuItem undo = new MenuItem(localize("undo"));
		undo.setDisable(true); // TODO
		undo.setAccelerator(KeyCombination.keyCombination("Ctrl+Z"));

		final MenuItem redo = new MenuItem(localize("redo"));
		redo.setDisable(true); // TODO
		redo.setAccelerator(KeyCombination.keyCombination("Ctrl+Y"));

		final SeparatorMenuItem sep = new SeparatorMenuItem();

		final MenuItem pipelineEditor = new MenuItem(localize("pipeline.editor"));
		pipelineEditor.setOnAction(e -> {
			eventBus.post(new ApplicationRequest(ApplicationRequest.Type.OPEN_PIPELINE_EDITOR));
		});
		pipelineEditor.disableProperty().bind(hasNoProjectBinding);
		pipelineEditor.setAccelerator(KeyCombination.keyCombination("Ctrl+P"));

		final MenuItem settings = new MenuItem(localize("settings"));
		settings.setOnAction(e -> {
			eventBus.post(new ApplicationRequest(ApplicationRequest.Type.OPEN_USER_SETTINGS));
		});

		menu.getItems().addAll(undo, redo, sep, pipelineEditor, settings);

		return menu;
	}

	private Menu getSelectionMenu() {
		final Menu menu = new Menu(localize("select"));
		final MenuItem all = new MenuItem(localize("selection.all"));

		final BooleanBinding noMask = Bindings.not(editor.hasMaskProperty());

		all.setOnAction(e -> {
			eventBus.post(new SelectionMaskRequest(SelectionMaskRequest.Type.ALL));
		});
		all.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
		final MenuItem deselect = new MenuItem(localize("selection.deselect"));
		deselect.setOnAction(e -> {
			eventBus.post(new SelectionMaskRequest(SelectionMaskRequest.Type.DESELECT));
		});
		deselect.setAccelerator(KeyCombination.keyCombination("Ctrl+D"));
		deselect.disableProperty().bind(noMask);
		final MenuItem reselect = new MenuItem(localize("selection.reselect"));
		reselect.setOnAction(e -> {
			eventBus.post(new SelectionMaskRequest(SelectionMaskRequest.Type.RESELECT));
		});
		reselect.setAccelerator(KeyCombination.keyCombination("Shift+Ctrl+D"));
		// to be pedantic, we should disable this also when hasMaskProperty is true
		// but it's actually nice to be able to swap back and forth between the current
		// and previous selection, so eh...
		reselect.disableProperty().bind(Bindings.not(editor.hasPreviousMaskProperty()));
		final MenuItem invert = new MenuItem(localize("selection.invert"));
		invert.setOnAction(e -> {
			eventBus.post(new SelectionMaskRequest(SelectionMaskRequest.Type.INVERT));
		});
		invert.setAccelerator(KeyCombination.keyCombination("Shift+Ctrl+I"));
		invert.disableProperty().bind(noMask);
		menu.getItems().addAll(
				all, deselect, reselect, invert
		);
		return menu;
	}

	private Menu getProcessMenu() {
		final Menu menu = new Menu(localize("process"));

		final MenuItem processPage = new MenuItem(localize("process.page"));
		processPage.disableProperty().bind(Bindings.not(handler.canProcessPageProperty()));
		processPage.setOnAction(e -> {
			eventBus.post(new ProjectRequest(
					ProjectRequest.Type.PROCESS_PAGE,
					handler.getProject().getSelectedPageId()
			));
		});
		final MenuItem processPageAll = new MenuItem(localize("process.page.all"));
		processPageAll.disableProperty().bind(hasNoProjectBinding);
		processPageAll.setOnAction(e -> {
			eventBus.post(new ProjectRequest(
					ProjectRequest.Type.PROCESS_PAGE,
					-1
			));
		});
		final MenuItem resetPage = new MenuItem(localize("reset.page"));
		resetPage.disableProperty().bind(hasNoProjectBinding);
		resetPage.setOnAction(e -> {
			eventBus.post(new ProjectRequest(
					ProjectRequest.Type.RESET_PAGE,
					handler.getProject().getSelectedPageId()
			));
		});
		final MenuItem resetPageAll = new MenuItem(localize("reset.page.all"));
		resetPageAll.disableProperty().bind(hasNoProjectBinding);
		resetPageAll.setOnAction(e -> {
			eventBus.post(new ProjectRequest(
					ProjectRequest.Type.RESET_PAGE,
					-1
			));
		});

		menu.getItems().addAll(
				processPage, processPageAll, resetPage, resetPageAll
		);

		return menu;
	}

	private Menu getViewMenu() {
		final Menu menu = new Menu(localize("view"));
		final CheckMenuItem viewSideBar = new CheckMenuItem(localize("view.sidebar"));
		viewSideBar.selectedProperty().bindBidirectional(handler.settings.primaryStage.sideBarVisibility);

		final Menu viewToolBar = new Menu(localize("view.toolbar"));
		final ToggleGroupValue<String> toolBarGroup = new ToggleGroupValue<>();
		for (VisibilityMode mode : VisibilityMode.values()) {
			final RadioMenuItem item = new RadioMenuItem(mode.label());
			toolBarGroup.add(item, mode.name());
			viewToolBar.getItems().add(item);
		}
		toolBarGroup.valueProperty().bindBidirectional(handler.settings.primaryStage.toolBarVisibility);

		final Menu viewOptionsBar = new Menu(localize("view.optionsbar"));
		final ToggleGroupValue<String> optionsBarGroup = new ToggleGroupValue<>();
		for (VisibilityMode mode : VisibilityMode.values()) {
			final RadioMenuItem item = new RadioMenuItem(mode.label());
			optionsBarGroup.add(item, mode.name());
			viewOptionsBar.getItems().add(item);
		}
		optionsBarGroup.valueProperty().bindBidirectional(handler.settings.primaryStage.optionsBarVisibility);

		menu.getItems().addAll(
				viewSideBar,
				viewToolBar,
				viewOptionsBar
		);
		return menu;
	}

	private Menu getHelpMenu() {
		final Menu menu = new Menu(localize("help"));
		final MenuItem about = new MenuItem(localize("about"));
		about.setOnAction(e -> {
			final AboutDialog aboutDialog = new AboutDialog(stage, handler);
			aboutDialog.showAndWait();
		});

		menu.getItems().addAll(about);
		return menu;
	}

	@Override
	public Parent getComponent() {
		return menuBar;
	}

}
