package ch.unifr.diva.dip.core.ui;

import java.util.Arrays;
import java.util.List;
import javafx.scene.Scene;

/**
 * Simple stylesheet manager.
 */
public class StylesheetManager {

	// main/base stylesheet (light)
	private static final String main = "/styles/main.css";
	// dark stylesheet as addon for main/base stylesheet
	private static final String dark = "/styles/dark.css";

	/**
	 * Skins.
	 */
	public enum Skin {

		/**
		 * A light skin. The default skin.
		 */
		LIGHT() {
					@Override
					public List<String> getStylesheets() {
						return Arrays.asList(main);
					}
				},
		/**
		 * A dark skin. Experimental; not much time spent here yet, probably
		 * needs some tweaking.
		 */
		DARK() {
					@Override
					public List<String> getStylesheets() {
						return Arrays.asList(main, dark);
					}
				};

		/**
		 * Returns the stylesheets thate make up a skin.
		 *
		 * @return a list of stylesheets.
		 */
		abstract protected List<String> getStylesheets();

		/**
		 * Returns all stylesheets used by any skin. Used to remove all
		 * stylesheets before adding new/different ones.
		 *
		 * @return all stylesheets used by any skin.
		 */
		protected static List<String> allStylesheets() {
			return Arrays.asList(main, dark);
		}

		/**
		 * Returns the default skin.
		 *
		 * @return the default skin.
		 */
		public static Skin getDefault() {
			return LIGHT;
		}

		/**
		 * Savely returns a skin by its name.
		 *
		 * @param name the name of the skin.
		 * @return the skin with the given name, or the default skin.
		 */
		public static Skin get(String name) {
			try {
				return Skin.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return getDefault();
			}
		}

	}

	private static StylesheetManager instance;
	private Skin currentSkin = Skin.LIGHT;

	/**
	 * Creates a new stylesheet manager. This is a singleton. Use
	 * {@code getInstance()} to retrieve the stylesheet manager.
	 */
	private StylesheetManager() {

	}

	/**
	 * Returns an instance of the stylesheet manager.
	 *
	 * @return an instance of the stylesheet manager.
	 */
	public static StylesheetManager getInstance() {
		if (instance == null) {
			instance = new StylesheetManager();
		}
		return instance;
	}

	/**
	 * Sets the skin to be used. Current scenes need to be reinitialized (by a
	 * call to {@code init(scene)}) in order to take any effect.
	 *
	 * @param skin the skin to be used.
	 */
	public void setSkin(Skin skin) {
		currentSkin = skin;
	}

	/**
	 * Sets the skin to be used (by name). Current scenes need to be
	 * reinitialized (by a call to {@code init(scene)}) in order to take any
	 * effect.
	 *
	 * @param name the name of the skin.
	 */
	public void setSkin(String name) {
		setSkin(Skin.get(name));
	}

	/**
	 * Returns the current skin.
	 *
	 * @return the current skin.
	 */
	public Skin getSkin() {
		return currentSkin;
	}

	/**
	 * (Re-)initializes (or applies) all stylesheets of the current skin.
	 *
	 * @param scene the scene to apply the current skin to.
	 */
	public void init(Scene scene) {
		addStylesheet(scene, main);

		for (String stylesheet : Skin.allStylesheets()) {
			removeStylesheet(scene, stylesheet);
		}

		for (String stylesheet : currentSkin.getStylesheets()) {
			addStylesheet(scene, stylesheet);
		}
	}

	private void addStylesheet(Scene scene, String stylesheet) {
		if (!scene.getStylesheets().contains(stylesheet)) {
			scene.getStylesheets().add(stylesheet);
		}
	}

	private void removeStylesheet(Scene scene, String stylesheet) {
		if (scene.getStylesheets().contains(stylesheet)) {
			scene.getStylesheets().remove(stylesheet);
		}
	}

}
