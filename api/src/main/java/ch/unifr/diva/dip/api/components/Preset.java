package ch.unifr.diva.dip.api.components;

import java.util.HashMap;
import java.util.Map;

/**
 * A service preset. Service presets that will be installed the first time a
 * user's system sees a particular version ({@code major.minor} part only) of a
 * service.
 *
 * <p>
 * It might be convenient to extend this class, and offer a more convenient
 * constructor. Take care that the correct parameters are set, and everything is
 * typed correctly (e.g. values of {@code ExpParameter}s are stored as
 * {@code String}, so don't pass a {@code double}).
 */
public class Preset {

	/**
	 * Name of the preset.
	 */
	private final String name;

	/**
	 * The parameter values of the preset.
	 */
	private final Map<String, Object> parameters;

	/**
	 * Creates a new, empty preset.
	 *
	 * @param name the name of the preset.
	 */
	public Preset(String name) {
		this(name, new HashMap<>());
	}

	/**
	 * Creates a new preset.
	 *
	 * @param name the name of the preset.
	 * @param parameters the parameter values of the preset.
	 */
	public Preset(String name, Map<String, Object> parameters) {
		this.name = name;
		this.parameters = parameters;
	}

	/**
	 * Returns the name of the preset.
	 *
	 * @return the name of the preset.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parameter values of the preset.
	 *
	 * @return the parameter values of the preset.
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * Adds a parameter to the preset.
	 *
	 * @param key key of the parameter.
	 * @param value value of the parameter.
	 */
	public void addParameter(String key, Object value) {
		this.parameters.put(key, value);
	}

}
