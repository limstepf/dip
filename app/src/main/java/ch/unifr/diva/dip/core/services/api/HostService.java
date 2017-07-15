package ch.unifr.diva.dip.core.services.api;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.core.services.RgbPage;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.osgi.framework.Version;
import org.slf4j.LoggerFactory;

/**
 * Host processor/service base class (and configuration).
 */
public abstract class HostService implements HostProcessor {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(HostService.class);

	/**
	 * Package to look for host processors/services.
	 */
	public static final String PACKAGE = "ch.unifr.diva.dip.core.services";

	/**
	 * Version of all host processors/services.
	 */
	public static final Version VERSION = new Version(1, 0, 0);

	/**
	 * Canonical name of the default generator processor/service.
	 */
	public static final String DEFAULT_GENERATOR = RgbPage.class.getCanonicalName();

	protected final String name;
	protected final Map<String, InputPort<?>> inputs;
	protected final Map<String, OutputPort<?>> outputs;

	/**
	 * Creates a new host service.
	 *
	 * @param name name of the host service.
	 */
	public HostService(String name) {
		this(name, new HashMap<>(), new LinkedHashMap<>());
	}

	/**
	 * Creates a new host service.
	 *
	 * @param name name of the host service.
	 * @param inputs input port map (port key to input port).
	 * @param outputs output port map (port key to output port).
	 */
	public HostService(String name, Map<String, InputPort<?>> inputs, Map<String, OutputPort<?>> outputs) {
		this.name = name;
		this.inputs = inputs;
		this.outputs = outputs;
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.CUBE_SEND;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Map<String, InputPort<?>> inputs() {
		return inputs;
	}

	@Override
	public Map<String, OutputPort<?>> outputs() {
		return outputs;
	}

}
