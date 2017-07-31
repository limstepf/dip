package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.gui.pe.DataItemListView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.osgi.framework.Version;

/**
 * Processor preset data. Used to store parameters of processors.
 */
@XmlRootElement(name = "presets")
@XmlAccessorType(XmlAccessType.NONE)
public class PresetData {

	/**
	 * List of presets.
	 */
	@XmlElement(name = "preset")
	public List<Preset> list = new ArrayList<>();

	/**
	 * Creates a new, empty preset data object.
	 */
	public PresetData() {

	}

	/**
	 * Returns a list of available versions (with some presets) for the
	 * processor with the given pid.
	 *
	 * @param pid the pid of the processor.
	 * @return a list of available versions (with some presets).
	 */
	public ObservableList<String> getVersions(String pid) {
		return getVersions(pid, null);
	}

	/**
	 * Returns a list of available versions (with some presets) for the
	 * processor with the given pid.
	 *
	 * @param pid the pid of the processor.
	 * @param requiredVersion a version to be included in the returned list, no
	 * matter what, or {@code null}.
	 * @return a list of available versions (with some presets).
	 */
	public ObservableList<String> getVersions(String pid, String requiredVersion) {
		final ObservableList<String> versions = FXCollections.observableArrayList();
		for (Preset p : this.list) {
			if (p.pid.equals(pid)) {
				if (!versions.contains(p.version)) {
					versions.add(p.version);
				}
			}
		}
		if (requiredVersion != null && !versions.contains(requiredVersion)) {
			versions.add(requiredVersion);
		}
		versions.sort(String.CASE_INSENSITIVE_ORDER);
		return versions;
	}

	/**
	 * Returns the truncanated preset version. Presets ignore the micro-part and
	 * qualifier of the version.
	 *
	 * @param version version of the service/processor.
	 * @return truncanated preset version.
	 */
	public static String toPresetVersion(Version version) {
		return String.format("%d.%d", version.getMajor(), version.getMinor());
	}

	/**
	 * Adds a preset to the preset data.
	 *
	 * @param preset the new preset.
	 */
	public void addPreset(Preset preset) {
		this.list.add(preset);
	}

	/**
	 * Adds a preset to the preset data.
	 *
	 * @param item the new preset item.
	 */
	public void addPreset(PresetItem item) {
		addPreset(item.toPresetData());
	}

	/**
	 * Returns a list of presets for the processor with given pid and version.
	 *
	 * @param pid the pid of the processor.
	 * @param version the version of the processor.
	 * @return a list of presets.
	 */
	public ObservableList<PresetItem> getPresets(String pid, Version version) {
		return getPresets(pid, toPresetVersion(version));
	}

	/**
	 * Returns a list of presets for the processor with given pid and version.
	 *
	 * @param pid the pid of the processor.
	 * @param version the version of the processor.
	 * @return a list of presets.
	 */
	public ObservableList<PresetItem> getPresets(String pid, String version) {
		final ObservableList<PresetItem> presets = FXCollections.observableArrayList();
		for (Preset p : this.list) {
			if (!p.pid.equals(pid) || !p.version.equals(version)) {
				continue;
			}
			presets.add(new PresetItem(p));
		}
		return presets;
	}

	/**
	 * Sets all presets of given pid and version. This replaces(!) all presets
	 * for the same processor (pid and version) with the new set of presets.
	 *
	 * @param pid the pid of the processor.
	 * @param version the version of the processor.
	 * @param presets the set of presets.
	 */
	public void setPresets(String pid, Version version, ObservableList<PresetItem> presets) {
		setPresets(pid, toPresetVersion(version), presets);
	}

	/**
	 * Sets all presets of given pid and version. This replaces(!) all presets
	 * for the same processor (pid and version) with the new set of presets.
	 *
	 * @param pid the pid of the processor.
	 * @param version the version of the processor.
	 * @param presets the set of presets.
	 */
	public void setPresets(String pid, String version, ObservableList<PresetItem> presets) {
		// remove all modified set of presets
		final ArrayList<Preset> deprecated = new ArrayList<>();
		for (Preset p : this.list) {
			if (p.pid.equals(pid) && p.version.equals(version)) {
				deprecated.add(p);
			}
		}
		this.list.removeAll(deprecated);

		// (re-)add modified set of presets
		for (PresetItem item : presets) {
			addPreset(item);
		}
	}

	/**
	 * A preset object.
	 */
	public static class Preset {

		/**
		 * PID of the processor.
		 */
		@XmlAttribute
		public String pid;

		/**
		 * Version of the processor. This is a version string truncanated to
		 * only include the major and minor, but not the micro or qualifier part
		 * of the full semantic version (e.g. "1.0").
		 */
		@XmlAttribute
		public String version;

		/**
		 * Name of the preset.
		 */
		@XmlAttribute
		public String name;

		/**
		 * Parameter values of the preset.
		 */
		@XmlElement
		public Map<String, Object> parameters = new HashMap<>();

		/**
		 * Empty constructor (needed for JAXB).
		 */
		public Preset() {
		}

		/**
		 * Creates a new preset from the given processor.
		 *
		 * @param name the name of the preset.
		 * @param wrapper the processor to copy its parameters from.
		 */
		public Preset(String name, PrototypeProcessor wrapper) {
			this(
					wrapper.pid,
					toPresetVersion(wrapper.version),
					name,
					wrapper.getParameterValues() // no deep copy needed
			);
		}

		/**
		 * Creates a new preset.
		 *
		 * @param pid PID of the processor.
		 * @param version version of the processor.
		 * @param name name of the preset.
		 * @param parameters map of parameter values.
		 */
		public Preset(String pid, String version, String name, Map<String, Object> parameters) {
			this.pid = pid;
			this.version = version;
			this.name = name;
			this.parameters = parameters;
		}

	}

	/**
	 * A preset item. A list item pointing to an existing preset data object, or
	 * to a processor in order to create a new preset.
	 */
	public static class PresetItem implements Localizable, DataItemListView.DataItem {

		final private PresetData.Preset preset; // existing preset, or
		final private PrototypeProcessor wrapper; // new preset
		final private StringProperty nameProperty;
		final private ObjectProperty<NamedGlyph> glyphProperty;

		/**
		 * Creates a new preset item for a processor.
		 *
		 * @param wrapper the processor.
		 */
		public PresetItem(PrototypeProcessor wrapper) {
			this(null, wrapper);
		}

		/**
		 * Creates a new preset item for a preset data object.
		 *
		 * @param preset the preset.
		 */
		public PresetItem(PresetData.Preset preset) {
			this(preset, null);
		}

		/**
		 * Creates a new preset item.
		 *
		 * @param preset the preset, or {@code null}.
		 * @param wrapper the processor, or {@code null}.
		 */
		private PresetItem(PresetData.Preset preset, PrototypeProcessor wrapper) {
			this.preset = preset;
			this.wrapper = wrapper;
			this.nameProperty = new SimpleStringProperty();
			this.glyphProperty = new SimpleObjectProperty<>();
			if (isNewItem()) {
				this.glyphProperty.set(MaterialDesignIcons.FLOPPY);
				this.nameProperty.setValue(localize("preset.new"));
			} else {
				this.nameProperty.setValue(this.preset.name);
			}
		}

		/**
		 * Returns the name property of the preset item.
		 *
		 * @return the name property of the preset item.
		 */
		@Override
		public StringProperty nameProperty() {
			return nameProperty;
		}

		/**
		 * Checks whether this preset item refers to a new preset (to be
		 * exported/saved), or to an existing one.
		 *
		 * @return {@code true} if this item refers to a new preset,
		 * {@code false} otherwise.
		 */
		final public boolean isNewItem() {
			return this.wrapper != null;
		}

		@Override
		public ObjectProperty<NamedGlyph> glyphProperty() {
			return this.glyphProperty;
		}

		/**
		 * Returns the preset data object.
		 *
		 * @return the preset data object.
		 */
		public Preset toPresetData() {
			if (this.preset != null) {
				this.preset.name = this.nameProperty.get();
				return this.preset;
			}
			if (this.wrapper != null) {
				return new Preset(
						this.nameProperty.get(),
						this.wrapper
				);
			}
			return null;
		}

	}

}
