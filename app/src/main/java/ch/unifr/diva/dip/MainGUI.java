package ch.unifr.diva.dip;

import ch.unifr.diva.dip.core.ApplicationContext;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.UIStrategy;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.core.UserSettings;
import ch.unifr.diva.dip.eventbus.EventBus;
import ch.unifr.diva.dip.eventbus.EventBusGuava;
import ch.unifr.diva.dip.gui.editor.EditorPresenter;
import ch.unifr.diva.dip.gui.dialogs.ErrorDialog;
import ch.unifr.diva.dip.gui.main.MainPresenter;
import ch.unifr.diva.dip.gui.main.MainView;
import ch.unifr.diva.dip.gui.main.MainViewImpl;
import ch.unifr.diva.dip.gui.main.MenuBarPresenter;
import ch.unifr.diva.dip.gui.main.StatusBarPresenter;
import ch.unifr.diva.dip.gui.main.PagesWidget;
import ch.unifr.diva.dip.gui.main.SideBarPresenter;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Graphical user interface (GUI) of the application.
 */
public class MainGUI extends Application {
	private static final Logger log = LoggerFactory.getLogger(MainGUI.class);
	private final EventBus eventBus = new EventBusGuava();
	private final ApplicationContext context = Main.getApplicationContext();
	private final UIStrategy uiStrategy = new UIStrategyGUI();
	private final ApplicationHandler handler = new ApplicationHandler(
			context,
			uiStrategy,
			eventBus
	);
	private Stage primaryStage;

	// Due to how JavaFX works (lots of magic/reflection involved in launching a
	// JavaFX application...) we can *not* have a constructor here!
	// Similarly you can't do "new MainApplication();" so use the static method
	// Application.launch() instead.
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		this.uiStrategy.setStage(primaryStage);

		// verify that we have a valid context - or shut down.
		if (!context.getErrors().isEmpty()) {
			showError(context.getErrors().get(0));
			return; // bye
		}

		// init (and hook up) GUI components
		final StatusBarPresenter statusBar = new StatusBarPresenter();
		eventBus.register(statusBar);
		final MenuBarPresenter menuBar = new MenuBarPresenter(handler, primaryStage);
		final EditorPresenter editor = new EditorPresenter(handler);
		eventBus.register(editor);

		// sidebar
		final SideBarPresenter sideBar = new SideBarPresenter();
		final PagesWidget pages = new PagesWidget(handler);
		eventBus.register(pages);
		sideBar.addAuxiliaryWidget(editor.navigatorWidget());
		sideBar.addMainWidget(pages);
		sideBar.addMainWidget(editor.layersWidget());

		final MainView view = new MainViewImpl();

		// setup main presenter
		final MainPresenter presenter = new MainPresenter(
				handler,
				primaryStage,
				view,
				editor,
				menuBar,
				statusBar,
				sideBar
		);
		eventBus.register(presenter);

		// ready, set, go...
		presenter.show();
	}

	@Override
	public void stop() {
		// auto-save user settings on exit
		UserSettings.saveStage(primaryStage, context.settings.primaryStage);

		log.info(
				"saving user settings: {}",
				context.dataManager.appDataDir.settingsFile
		);

		handler.saveUserSettings();
	}

	/**
	 * Shows an error dialog.
	 * @param error An error (wrapped exception and an error message).
	 */
	public void showError(ApplicationContext.ContextError error) {
		log.error(error.message, error.exception);
		final ErrorDialog dialog = new ErrorDialog(error.message, error.exception);
		dialog.showAndWait();
	}

}
