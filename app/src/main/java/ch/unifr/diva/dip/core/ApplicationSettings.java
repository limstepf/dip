package ch.unifr.diva.dip.core;

import ch.unifr.diva.dip.api.utils.L10n;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Application settings (and constants). The ApplicationSettings is a static
 * class whose contents can't be changed by the user, or at runtime or anything.
 * Have a look at the UserSettings for that kind of thing.
 *
 * @see UserSettings
 */
public class ApplicationSettings {

	/**
	 * Title of the application.
	 */
	public static final String applicationTitle = "Document Image Processor";

	/**
	 * File extension of project savefiles of this application.
	 */
	public static final String projectFileExtension = "dip";

	/**
	 * File extension filter for project savefiles of this application.
	 */
	public static final ExtensionFilter projectFileExtensionFilter = new ExtensionFilter(
			L10n.getInstance().getString("project.file"),
			"*.dip"
	);

	/**
	 * File extension filter for pipeline presets files.
	 */
	public static final ExtensionFilter pipelinePresetsFileExtensionFilter = new ExtensionFilter(
			L10n.getInstance().getString("pipeline.presets.file"),
			"*.xml"
	);

	/**
	 * File extension filter for processor presets files.
	 */
	public static final ExtensionFilter processorPresetsFileExtensionFilter = new ExtensionFilter(
			L10n.getInstance().getString("processor.presets.file"),
			"*.xml"
	);

	/**
	 * The name of the directory for the application data ({@literal e.g.} user
	 * settings) somewhere in user-land. By convention this name starts with a
	 * dot (to make it a hidden directory), and should be unique enough to not
	 * overwrite/mess up some unrelated directory.
	 */
	public static final String appDataDirName = ".ch.unifr.diva.dip";

	/**
	 * Main/global stylesheet.
	 */
	public static final String stylesheet = "/styles/main.css";

	/**
	 * Sets the project files extension filter on a {@code FileChooser} to
	 * save/open a project.
	 *
	 * @param chooser a FileChooser.
	 */
	public static void setProjectExtensionFilter(FileChooser chooser) {
		setExtensionFilter(chooser, ApplicationSettings.projectFileExtensionFilter);
	}

	/**
	 * Sets an extension filter on a FileChooser.
	 *
	 * @param chooser
	 * @param filter
	 */
	public static void setExtensionFilter(FileChooser chooser, ExtensionFilter filter) {
		chooser.getExtensionFilters().add(filter);
		chooser.setSelectedExtensionFilter(filter);
	}

	/**
	 * Application-wide animation/transition settings.
	 */
	public static class Animations {

		/**
		 * Time in milliseconds to display auto-disappearing nodes.
		 */
		public static final int displayDuration = 5500;

		/**
		 * Time in milliseconds to fade out nodes.
		 */
		public static final int fadeOutDuration = 3000;

		/**
		 * Time in milliseconds to fade in nodes.
		 */
		public static final int fadeInDuration = fadeOutDuration / 4;
	}

	/**
	 * Default stage/window settings.
	 */
	public static class Stage {

		/**
		 * Minimum width of a stage/window.
		 */
		public static final int minWidth = 480;

		/**
		 * Minimum height of a stage/window.
		 */
		public static final int minHeight = 360;

		/**
		 * Default insets spacing (or padding). If in doubt, use this global value
		 * (e.g. for spacing between elements in a lane or what not) instead of
		 * introducing yet another magic variable.
		 */
		public static final int insets = 5;
	}

}
