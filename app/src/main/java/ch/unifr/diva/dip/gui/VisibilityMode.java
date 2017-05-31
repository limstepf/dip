package ch.unifr.diva.dip.gui;

import ch.unifr.diva.dip.api.utils.L10n;

/**
 * Visibility mode.
 */
public enum VisibilityMode {

	/**
	 * Automatic visibility.
	 */
	AUTO(L10n.getInstance().getString("automatic")),
	/**
	 * Always visible.
	 */
	ALWAYS(L10n.getInstance().getString("always")),
	/**
	 * Never visibile.
	 */
	NEVER(L10n.getInstance().getString("never"));

	private final String label;

	/**
	 * Creates a new visibility mode.
	 *
	 * @param label label of the visibility mode.
	 */
	private VisibilityMode(String label) {
		this.label = label;
	}

	/**
	 * Return the label of the visibility mode.
	 *
	 * @return the label of the visibility mode.
	 */
	public String label() {
		return this.label;
	}

	/**
	 * Savely returns a visibility mode by its name.
	 *
	 * @param name name of the visibility mode.
	 * @return the visibility mode with the given name, or the default
	 * visibility mode.
	 */
	public static VisibilityMode get(String name) {
		try {
			return VisibilityMode.valueOf(name);
		} catch (IllegalArgumentException ex) {
			return getDefault();
		}
	}

	/**
	 * Returns the default visibility mode.
	 *
	 * @return the default visibility mode.
	 */
	public static VisibilityMode getDefault() {
		return VisibilityMode.AUTO;
	}

}
