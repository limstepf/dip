package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.eventbus.EventBus;
import ch.unifr.diva.dip.eventbus.events.ApplicationRequest;
import ch.unifr.diva.dip.eventbus.events.ProjectRequest;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.dialogs.AboutDialog;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * Main Menu.
 */
public class MenuBarPresenter implements Presenter, Localizable {

	private final ApplicationHandler handler;
	private final EventBus eventBus;
	private final Stage stage;
	private final MenuBar menuBar;
	private final Menu fileMenu, editMenu, viewMenu, toolsMenu, helpMenu;

	public MenuBarPresenter(ApplicationHandler handler, Stage stage) {
		this.handler = handler;
		this.eventBus = handler.eventBus;
		this.stage = stage;

		menuBar = new MenuBar();

		fileMenu = getFileMenu();
		editMenu = getEditMenu();
		viewMenu = getViewMenu();
		toolsMenu = getToolsMenu();
		helpMenu = getHelpMenu();

		menuBar.getMenus().addAll(
				fileMenu, editMenu, viewMenu, toolsMenu, helpMenu
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
		closeProject.disableProperty().bind(Bindings.not(handler.hasProjectProperty()));

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
		saveAsProject.disableProperty().bind(Bindings.not(handler.hasProjectProperty()));

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

		menu.getItems().addAll(undo, redo);

		return menu;
	}

	private Menu getViewMenu() {
		final Menu menu = new Menu(localize("view"));
		final CheckMenuItem viewSideBar = new CheckMenuItem(localize("view.sidebar"));
		viewSideBar.setSelected(true);
		viewSideBar.setOnAction(e -> {
			eventBus.post(new ApplicationRequest(
					viewSideBar.isSelected()
							? ApplicationRequest.Type.SHOW_SIDEBAR
							: ApplicationRequest.Type.HIDE_SIDEBAR
			));
		});

		menu.getItems().addAll(viewSideBar);
		return menu;
	}

	private Menu getToolsMenu() {
		final Menu menu = new Menu(localize("tools"));

		final MenuItem pipelineEditor = new MenuItem(localize("pipeline.editor"));
		pipelineEditor.setOnAction(e -> {
			eventBus.post(new ApplicationRequest(ApplicationRequest.Type.OPEN_PIPELINE_EDITOR));
		});
		final BooleanBinding canOpenPipelineEditor = Bindings.not(
				handler.hasProjectProperty()
		);
		pipelineEditor.disableProperty().bind(canOpenPipelineEditor);
		pipelineEditor.setAccelerator(KeyCombination.keyCombination("Ctrl+P"));

		menu.getItems().addAll(pipelineEditor);
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
