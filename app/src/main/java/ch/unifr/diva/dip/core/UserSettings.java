package ch.unifr.diva.dip.core;

import ch.unifr.diva.dip.utils.FxUtils;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.core.model.PipelineLayoutStrategy;
import ch.unifr.diva.dip.gui.pe.ConnectionView;
import java.nio.file.Path;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * User settings. As oppposed to the ApplicationSettings the UserSettings are
 * free to be changed by the user.
 *
 * @see ApplicationSettings
 */
@XmlRootElement(name = "settings")
public class UserSettings {

	/**
	 * User locale.
	 */
	public Locale locale = new Locale();

	@XmlRootElement
	public static class Locale {

		@XmlAttribute
		public String language = "en";

		@XmlAttribute
		public String country = null;
	}

	/**
	 * Returns the user's Locale.
	 *
	 * @return a Locale with defined language, possibly a country too.
	 */
	public java.util.Locale getLocale() {
		if (locale.country == null) {
			return new java.util.Locale(locale.language);
		}
		return new java.util.Locale(locale.language, locale.country);
	}

	/**
	 * Main primary stage settings.
	 */
	public PrimaryStage primaryStage = new PrimaryStage();

	/**
	 * Pipeline editor stage settings.
	 */
	public PrimaryStage pipelineStage = new PrimaryStage();

	@XmlRootElement
	public static class PrimaryStage {

		@XmlAttribute
		public int x = -1;

		@XmlAttribute
		public int y = -1;

		@XmlAttribute
		public int width = 640;

		@XmlAttribute
		public int height = 380;

		@XmlAttribute
		public boolean maximized = false;

		@XmlAttribute
		public double sideBarDivider = 0.75;
	}

	/**
	 * Saves the settings of a stage ({@literal i.e.} a dialog or a window).
	 * This includes the position (x, y) and size (width, height) of the stage,
	 * as well as whether the stage is maximized. In the latter case position
	 * and size are *not* updated/written, s.t. the stage will restore to the
	 * settings prior to maximizing the stage once no longer maximized.
	 *
	 * @param stage live stage to read out current settings from.
	 * @param settings a PrimaryStage container to save the settings to.
	 */
	public static void saveStage(Stage stage, PrimaryStage settings) {
		// see note about splitpane divider bug/weirdness below in the body
		// of restoreStage(), which is why we set/get the "window" dimensions
		// by the root region of the stage instead of querying stage.getHeight()
		// and stage.getHeight()...
		final Region root = (Region) stage.getScene().getRoot();

		settings.maximized = stage.isMaximized();

		if (!settings.maximized) {
			settings.x = (int) stage.getX();
			settings.y = (int) stage.getY();
			settings.width = (int) root.getWidth();
			settings.height = (int) root.getHeight();
		}
	}

	/**
	 * Saves the divider positions of a primary stage with a sidebar.
	 *
	 * @param splitPane a SplitPane (with no or one divider).
	 * @param settings a PrimaryStage container to save the settings to.
	 */
	public static void saveDividerPositions(SplitPane splitPane, PrimaryStage settings) {
		final double d = FxUtils.dividerPosition(splitPane, 0);
		if (d >= 0) {
			settings.sideBarDivider = d;
		}
	}

	/**
	 * Restores stage settings from a PrimaryStage container.
	 *
	 * @param stage live stage to be restored.
	 * @param settings a PrimaryStage container to read the settings from.
	 */
	public static void restoreStage(Stage stage, PrimaryStage settings) {
		stage.setMinWidth(ApplicationSettings.Stage.minWidth);
		stage.setMinHeight(ApplicationSettings.Stage.minHeight);

		// note: we can't set `stage.setWidth/stage.setHeight` since this will
		// fuck up the splitPane and its divider positions, since somehow a
		// second layout-pass will be triggered that resets the divider position
		// That's why we have to `setPrefWidth/setPrefHeight` the root region
		// instead... See:
		// https://bugs.openjdk.java.net/browse/JDK-8097039
		// https://bugs.openjdk.java.net/browse/JDK-8115114
		final Region root = (Region) stage.getScene().getRoot();

		if (settings.width >= ApplicationSettings.Stage.minWidth) {
			//stage.setWidth(settings.width);
			root.setPrefWidth(settings.width);
		}
		if (settings.height >= ApplicationSettings.Stage.minHeight) {
			//stage.setHeight(settings.height);
			root.setPrefHeight(settings.height);
		}
		if (settings.x >= 0) {
			stage.setX(settings.x);
		}
		if (settings.y >= 0) {
			stage.setY(settings.y);
		}
		stage.setMaximized(settings.maximized);
	}

	/**
	 * General pipeline editor settings.
	 */
	public PipelineEditor pipelineEditor = new PipelineEditor();

	@XmlRootElement
	public static class PipelineEditor {

		@XmlAttribute
		public String connectionType = ConnectionView.Type.getDefault().name();

		@XmlAttribute
		public String pipelineLayout = PipelineLayoutStrategy.getDefault().name();

		/**
		 * Returns the connection (or wire) type preferred by the user.
		 *
		 * @return preferred connection (or wire) type used to display
		 * connections in the pipeline editor.
		 */
		public ConnectionView.Type getDefaultConnectionType() {
			return ConnectionView.Type.get(connectionType);
		}

		/**
		 * Returns the default pipeline layout strategy preferred by the user.
		 *
		 * @return default pipeline layout strategy initially used for newly
		 * created pipelines.
		 */
		public PipelineLayoutStrategy getDefaultPipelineLayout() {
			return PipelineLayoutStrategy.get(pipelineLayout);
		}
	}

	/**
	 * Reads/unmarshalls UserSettings from an XML file.
	 *
	 * @param file an XML file with UserSettings.
	 * @return the user settings.
	 * @throws JAXBException in case of unexpected errors during unmarshalling.
	 */
	public static UserSettings load(Path file) throws JAXBException {
		return XmlUtils.unmarshal(UserSettings.class, file);
	}

	/**
	 * Saves/marshalls the UserSettings to disk.
	 *
	 * @param file the XML File to store the UserSettings.
	 * @throws JAXBException in case of unexpected errors during marshalling.
	 */
	public void save(Path file) throws JAXBException {
		XmlUtils.marshal(this, file);
	}

}
