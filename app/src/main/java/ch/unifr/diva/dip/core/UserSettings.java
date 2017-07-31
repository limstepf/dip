package ch.unifr.diva.dip.core;

import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.api.utils.jaxb.BooleanPropertyAdapter;
import ch.unifr.diva.dip.api.utils.jaxb.PathAdapter;
import ch.unifr.diva.dip.api.utils.jaxb.StringPropertyAdapter;
import ch.unifr.diva.dip.core.ui.StylesheetManager;
import ch.unifr.diva.dip.gui.pe.PipelineLayoutStrategy;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.VisibilityMode;
import ch.unifr.diva.dip.gui.layout.Zoomable;
import ch.unifr.diva.dip.gui.pe.ConnectionView;
import ch.unifr.diva.dip.osgi.OSGiVersionPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * User settings. As oppposed to the ApplicationSettings the UserSettings are
 * free to be changed by the user.
 *
 * @see ApplicationSettings
 */
@XmlRootElement(name = "settings")
@XmlAccessorType(XmlAccessType.NONE)
public class UserSettings {

	/**
	 * User locale.
	 */
	@XmlElement
	public Locale locale = new Locale();

	/**
	 * User locale object.
	 */
	@XmlRootElement
	public static class Locale {

		/**
		 * The language of the locale. ISO 639 alpha-2 or alpha-3 language code,
		 * or registered language subtags up to 8 alpha letters (for future
		 * enhancements).
		 */
		@XmlAttribute
		public String language = "en";

		/**
		 * The country (region) of the locale. ISO 3166 alpha-2 country code or
		 * UN M.49 numeric-3 area code.
		 */
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
	 * The skin (or stylesheet).
	 */
	@XmlElement
	public String skin = StylesheetManager.Skin.getDefault().name();

	/**
	 * Main primary stage settings.
	 */
	@XmlElement
	public PrimaryStage primaryStage = new PrimaryStage();

	/**
	 * Pipeline editor stage settings.
	 */
	@XmlElement
	public PrimaryStage pipelineStage = new PrimaryStage();

	/**
	 * Primary stage object.
	 */
	@XmlRootElement
	public static class PrimaryStage {

		/**
		 * The X position of the primary stage.
		 */
		@XmlAttribute
		public int x = -1;

		/**
		 * The Y position of the primary stage.
		 */
		@XmlAttribute
		public int y = -1;

		/**
		 * The width of the primary stage.
		 */
		@XmlAttribute
		public int width = 640;

		/**
		 * The height of the primary stage.
		 */
		@XmlAttribute
		public int height = 380;

		/**
		 * Whether or not the stage is maximized.
		 */
		@XmlAttribute
		public boolean maximized = false;

		/**
		 * Whether or not the sidebar is shown. This is a property to bind to
		 * bidirectionally for controls, or to listen to for the presenter.
		 */
		@XmlAttribute
		@XmlJavaTypeAdapter(BooleanPropertyAdapter.class)
		public BooleanProperty sideBarVisibility = new SimpleBooleanProperty(true);

		/**
		 * The position of the side bar divider.
		 */
		@XmlAttribute
		public double sideBarDivider = 0.75;

		/**
		 * The toolbar visibility mode. This is a property to bind to
		 * bidirectionally for controls, or to listen to for the presenter.
		 */
		@XmlAttribute
		@XmlJavaTypeAdapter(StringPropertyAdapter.class)
		public StringProperty toolBarVisibility = new SimpleStringProperty(
				VisibilityMode.getDefault().name()
		);

		/**
		 * The options bar visibility mode. This is a property to bind to
		 * bidirectionally for controls, or to listen to for the presenter.
		 */
		@XmlAttribute
		@XmlJavaTypeAdapter(StringPropertyAdapter.class)
		public StringProperty optionsBarVisibility = new SimpleStringProperty(
				VisibilityMode.getDefault().name()
		);

		/**
		 * Returns a (human-readable) array of attributes.
		 *
		 * @return a (human-readable) array of attributes.
		 */
		public String[] getAttributes() {
			return new String[]{
				"x: " + x,
				"y: " + y,
				"width: " + width,
				"height: " + height,
				"maximized: " + maximized,
				"sidebar-visibility: " + sideBarVisibility.get(),
				"sidebar-divider: " + sideBarDivider,
				"toolbar-visibility: " + toolBarVisibility.get(),
				"optionsbar-visibility: " + optionsBarVisibility.get()
			};
		}

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
		stage.setMinWidth(UIStrategyGUI.Stage.minWidth);
		stage.setMinHeight(UIStrategyGUI.Stage.minHeight);

		// note: we can't set `stage.setWidth/stage.setHeight` since this will
		// fuck up the splitPane and its divider positions, since somehow a
		// second layout-pass will be triggered that resets the divider position
		// That's why we have to `setPrefWidth/setPrefHeight` the root region
		// instead... See:
		// https://bugs.openjdk.java.net/browse/JDK-8097039
		// https://bugs.openjdk.java.net/browse/JDK-8115114
		final Region root = (Region) stage.getScene().getRoot();

		if (settings.width >= UIStrategyGUI.Stage.minWidth) {
			//stage.setWidth(settings.width);
			root.setPrefWidth(settings.width);
		}
		if (settings.height >= UIStrategyGUI.Stage.minHeight) {
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
	 * General (pixel) editor settings.
	 */
	@XmlElement
	public Editor editor = new Editor();

	/**
	 * Editor object.
	 */
	@XmlRootElement
	public static class Editor {

		/**
		 * The interpolation method/algorithm to be used by the main editor.
		 */
		@XmlAttribute
		public String interpolation = Zoomable.Interpolation.BILINEAR.name();

		/**
		 * Returns a (human-readable) array of attributes.
		 *
		 * @return a (human-readable) array of attributes.
		 */
		public String[] getAttributes() {
			return new String[]{
				"interpolation: " + interpolation
			};
		}

	}

	/**
	 * General pipeline editor settings.
	 */
	@XmlElement
	public PipelineEditor pipelineEditor = new PipelineEditor();

	/**
	 * Pipeline editor object.
	 */
	@XmlRootElement
	public static class PipelineEditor {

		/**
		 * The default connection/wire type.
		 */
		@XmlAttribute
		public String connectionType = ConnectionView.Type.getDefault().name();

		/**
		 * The default pipeline layout.
		 */
		@XmlAttribute
		public String pipelineLayout = PipelineLayoutStrategy.getDefault().name();

		/**
		 * Whether or not to auto-rearrange all processors in the pipeline
		 * editor upon changing the pipeline layout.
		 */
		@XmlAttribute
		public boolean autoRearrangeOnChangedLayout = true;

		/**
		 * Whether or not to auto-rearrange all processors in the pipeline
		 * editor upon un-/folding a single processor.
		 */
		@XmlAttribute
		public boolean autoRearrangeOnProcessorFold = false;

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

		/**
		 * Returns a (human-readable) array of attributes.
		 *
		 * @return a (human-readable) array of attributes.
		 */
		public String[] getAttributes() {
			return new String[]{
				"connection-type: " + getDefaultConnectionType(),
				"pipeline-layout: " + getDefaultPipelineLayout(),
				"auto-rearrange-on-changed-layout: " + autoRearrangeOnChangedLayout,
				"auto-rearrange-on-processor-fold: " + autoRearrangeOnProcessorFold
			};
		}

	}

	/**
	 * OSGi framework settings.
	 */
	@XmlElement
	public OSGi osgi = new OSGi();

	/**
	 * OSGi object.
	 */
	@XmlRootElement
	public static class OSGi {

		/**
		 * The default OSGi service (auto) upgrade policy.
		 */
		@XmlAttribute
		public OSGiVersionPolicy versionPolicy = OSGiVersionPolicy.getDefault();

		/**
		 * Returns a (human-readable) array of attributes.
		 *
		 * @return a (human-readable) array of attributes.
		 */
		public String[] getAttributes() {
			return new String[]{
				"version-policy: " + versionPolicy
			};
		}

	}

	/**
	 * Recently accessed files.
	 */
	@XmlElement
	public RecentFiles recentFiles = new RecentFiles();

	/**
	 * Object to hold recently accessed files.
	 */
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class RecentFiles {

		/**
		 * The most recently accessed directory for DIP save files.
		 */
		@XmlElement
		@XmlJavaTypeAdapter(PathAdapter.class)
		public Path dipSaveDirectory = null;

		/**
		 * Savely returns the most recently accessed DIP save directory.
		 *
		 * @return a path to the most recently accessed DIP save directory
		 * (guaranteed to exist), or {@code null}.
		 */
		public Path getSaveDirectory() {
			if (dipSaveDirectory != null && !Files.exists(dipSaveDirectory)) {
				dipSaveDirectory = null;
			}
			return dipSaveDirectory;
		}

		/**
		 * Updates the most recently accessed DIP save directory.
		 *
		 * @param file a path to the most recently accessed DIP save file,
		 * directory, or {@code null}.
		 */
		public void setSaveDirectory(Path file) {
			if (file == null) {
				dipSaveDirectory = null;
			} else {
				if (Files.isDirectory(file)) {
					dipSaveDirectory = file;
				} else {
					dipSaveDirectory = file.getParent();
				}
			}
		}

		/**
		 * The most recently accessed DIP data directory. Unlike
		 * {@code dipDataFile} this path isn't set back to {@code null} after
		 * accessing a DIP data file located in the user directory, s.t. the
		 * custom directory is remembered upon trying to load a custom DIP data
		 * file again.
		 */
		@XmlElement
		@XmlJavaTypeAdapter(PathAdapter.class)
		public Path dipDataDirectory = null;

		/**
		 * Savely returns the most recently accessed DIP data directory.
		 *
		 * @return a path to the most recently accessed DIP data directory
		 * (guaranteed to exist), or {@code null}.
		 */
		public Path getDataDirectory() {
			if (dipDataDirectory != null && !Files.exists(dipDataDirectory)) {
				dipDataDirectory = null;
			}
			return dipDataDirectory;
		}

		/**
		 * The most recently accessed DIP data file. A DIP data file stores
		 * processor presets and pipelines. This path should only be set if the
		 * file accessed last is located outside of the user directory (i.e. not
		 * one of the application's default files to store such things; if
		 * that's the case, this path should be set to {@code null}).
		 */
		@XmlElement
		@XmlJavaTypeAdapter(PathAdapter.class)
		public Path dipDataFile = null;

		/**
		 * Savely returns the most recently accessed DIP data file.
		 *
		 * @return a path to the most recently accessed DIP data file
		 * (guaranteed to exist), or {@code null}.
		 */
		public Path getDataFile() {
			if (dipDataFile != null && !Files.exists(dipDataFile)) {
				dipDataFile = null;
			}
			return dipDataFile;
		}

		/**
		 * Updates the most recently accessed DIP data file and directory.
		 *
		 * @param file the most recently accessed DIP data file, directory, or
		 * {@code null}.
		 */
		public void setDataFile(Path file) {
			if (file == null || !Files.exists(file)) {
				dipDataFile = null;
			} else {
				if (Files.isDirectory(file)) {
					dipDataFile = null;
					dipDataDirectory = file;
				} else {
					dipDataFile = file;
					dipDataDirectory = file.getParent();
				}
			}
		}

		/**
		 * Returns a (human-readable) array of attributes.
		 *
		 * @return a (human-readable) array of attributes.
		 */
		public String[] getAttributes() {
			return new String[]{
				"data-file: " + getDataFile(),
				"data-directory: " + getDataDirectory(),
				"save-directory: " + getSaveDirectory()
			};
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
