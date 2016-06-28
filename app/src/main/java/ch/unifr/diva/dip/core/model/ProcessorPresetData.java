package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.parameters.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Processor preset data. Used to store parameters of processors.
 */
@XmlRootElement(name = "processor-presets")
public class ProcessorPresetData {

	@XmlElement(name = "preset")
	public List<Preset> list = new ArrayList<>();

	public ProcessorPresetData() {
	}

	public void addPreset(String name, Map<String, Object> parameters) {
		list.add(new Preset(name, parameters));
	}

	public void addPreset(int id, Map<String, Object> parameters) {
		list.add(new Preset(id, parameters));
	}

	public static class Preset {

		// arbitrary name in case of a public presets file (per processor), or
		// string-representation of the internal processor id in a pipeline
		@XmlAttribute
		public String name;

		@XmlElement(name = "parameter")
		public Map<String, Object> map = new HashMap<>();

		public Preset() {
		}

		public Preset(String name, Map<String, Object> parameters) {
			this.name = name;
			this.map = parameters;
		}

		public Preset(int id, Map<String, Object> parameters) {
			this.name = String.format("%d", id);
			this.map = parameters;
		}

		public int id() {
			return Integer.parseInt(name);
		}
	}

}
