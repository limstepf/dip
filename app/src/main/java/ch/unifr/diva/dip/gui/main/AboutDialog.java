package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.StructuredText;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ApplicationSettings;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

	/**
	 * Creates a new about dialog.
	 *
	 * @param owner the owner/parent window of the dialog.
	 * @param handler the application handler.
	 */
	public AboutDialog(Window owner, ApplicationHandler handler) {
		super(owner);
		setTitle(localize("about"));
		this.handler = handler;

		ok = getDefaultButton(localize("close"));
		buttons.add(ok);
		ok.setOnAction(e -> stage.hide());
		attachCancelOnEscapeHandler();

		view.title.setText(ApplicationSettings.applicationTitle);

		// doesn't look like we get a proper version while running from the IDE
		final String version = getClass().getPackage().getImplementationVersion();
		view.version.setText(version == null ? "IDE-SNAPSHOT" : version);

		final Map<Object, Object> info = new LinkedHashMap<>();
		info.put(
				localize("system.java.version"),
				System.getProperty("java.runtime.version") + " "
				+ System.getProperty("java.vm.name") + " "
				+ System.getProperty("java.vm.version")
		);
		info.put(
				localize("system.os"),
				System.getProperty("os.name") + ", "
				+ System.getProperty("os.version") + " ("
				+ System.getProperty("os.arch") + ")"
		);
		info.put(
				localize("directory.application"),
				handler.dataManager.appDir.toString()
		);
		info.put(
				localize("directory.user"),
				handler.dataManager.appDataDir.toString()
		);
		final StructuredText infoPane = StructuredText.smallDescriptionList(info);
		view.getChildren().add(infoPane);

		root.setCenter(view);
	}

	/**
	 * The inner about dialog view.
	 */
	private static class View extends VBox {

		private final Label title = new Label();
		private final Label version = new Label();

		/**
		 * Creates a new inner view.
		 */
		public View() {
			this.setAlignment(Pos.CENTER);
			title.getStyleClass().add("dip-title");
			setLeft(title);
			setLeft(version);

			final Glyph g = UIStrategyGUI.Glyphs.defaultProcessor.get(128).setColor(UIStrategyGUI.Colors.accent);
			final double m = UIStrategyGUI.Stage.insets;
			VBox.setMargin(g, new Insets(m, 0, m * 2, 0));

			this.getChildren().addAll(title, version, g);
		}

		private void setLeft(Label label) {
			label.setMaxWidth(Double.MAX_VALUE);
			label.setAlignment(Pos.CENTER_LEFT);
		}
	}

}
