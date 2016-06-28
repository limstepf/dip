package ch.unifr.diva.dip.gui.dialogs;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ApplicationSettings;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * About dialog.
 */
public class AboutDialog extends AbstractDialog {

	private final ApplicationHandler handler;
	private final View view = new View();
	private final Button ok;

	public AboutDialog(Window owner, ApplicationHandler handler) {
		super(owner);
		setTitle(localize("about"));
		this.handler = handler;

		ok = getDefaultButton(localize("close"));
		buttons.add(ok);
		ok.setOnAction(e -> stage.hide());

		view.title.setText(ApplicationSettings.applicationTitle);

		view.javaLabel.setText(localize("system.java.version") + ":");
		view.java.setText(
				System.getProperty("java.runtime.version") + " "
				+ System.getProperty("java.vm.name") + " "
				+ System.getProperty("java.vm.version")
		);

		view.osLabel.setText(localize("system.os") + ":");
		view.os.setText(
				System.getProperty("os.name") + ", "
				+ System.getProperty("os.version") + " ("
				+ System.getProperty("os.arch") + ")"
		);

		view.appDirLabel.setText(localize("directory.application") + ":");
		view.appDir.setText(handler.dataManager.appDir.toString());

		view.userDirLabel.setText(localize("directory.user") + ":");
		view.userDir.setText(handler.dataManager.appDataDir.toString());

		root.setCenter(view);
	}

	private static class View extends VBox {

		private final Label title = new Label();
		private final Label javaLabel = new Label();
		private final Label java = new Label();
		private final Label osLabel = new Label();
		private final Label os = new Label();
		private final Label appDirLabel = new Label();
		private final Label appDir = new Label();
		private final Label userDirLabel = new Label();
		private final Label userDir = new Label();
		private final FormGridPane form = new FormGridPane();

		public View() {
			title.getStyleClass().add("dip-title");

			form.addRow(javaLabel, java);
			form.addRow(osLabel, os);
			form.addRow(appDirLabel, appDir);
			form.addRow(userDirLabel, userDir);

			this.getChildren().addAll(title, form);
		}
	}

}
