package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.utils.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.collections.ObservableList;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.osgi.framework.Version;
import org.w3c.dom.Element;

/**
 * Dip export data. A composite storage format. This is the only format that
 * should be used to write external/standalone XML files (other than, and
 * outside of the usual dip savefile), and allows to save different things in
 * the same XML file (e.g. pipelines and presets). All elements below the root
 * are optional.
 */
@XmlRootElement(name = "dip-data")
@XmlAccessorType(XmlAccessType.NONE)
public class DipData {

	/**
	 * The root tag of the dip data XML file.
	 */
	final public static String ROOT_TAG_NAME = "dip-data";

	/**
	 * Preset data. May be null.
	 */
	@XmlElement(name = "presets")
	public PresetData presets;

	/**
	 * Pipeline data. May be null.
	 */
	@XmlElement(name = "pipelines")
	public PipelineData pipelines;

	/**
	 * Creates a new, empty DipData object.
	 */
	public DipData() {

	}

	/**
	 * Checks whether preset data is available.
	 *
	 * @return {@code true} if there are presets, {@code false} otherwise.
	 */
	public boolean hasPresetData() {
		return this.presets != null;
	}

	/**
	 * Savely returns the preset data. Returns a new, empty preset data object
	 * if no presets are available.
	 *
	 * @return the preset data.
	 */
	public PresetData getPresetData() {
		if (!hasPresetData()) {
			return new PresetData();
		}
		return this.presets;
	}

	/**
	 * Sets the preset data.
	 *
	 * @param presets the preset data.
	 */
	public void setPresetData(PresetData presets) {
		this.presets = presets;
	}

	/**
	 * Sets all presets of given pid and version. This replaces(!) all presets
	 * for the same processor (pid and version) with the new set of presets.
	 *
	 * @param pid the pid of the processor.
	 * @param version the version of the processor.
	 * @param presets the set of presets.
	 */
	public void setPresets(String pid, Version version, ObservableList<PresetData.PresetItem> presets) {
		if (this.presets == null) {
			this.presets = new PresetData();
		}
		this.presets.setPresets(pid, version, presets);
	}

	/**
	 * Checks whether pipeline data is available.
	 *
	 * @return {@code true} if there are pipelines, {@code false} otherwise.
	 */
	public boolean hasPipelineData() {
		return this.pipelines != null;
	}

	/**
	 * Savely returns the pipeline data. Returns a new, empty pipeline data
	 * object if no pipelines are available.
	 *
	 * @return the preset data.
	 */
	public PipelineData getPipelineData() {
		if (!hasPipelineData()) {
			return new PipelineData();
		}
		return this.pipelines;
	}

	/**
	 * Sets the pipeline data.
	 *
	 * @param pipelines the pipeline data.
	 */
	public void setPipelineData(PipelineData pipelines) {
		this.pipelines = pipelines;
	}

	/**
	 * Sets all pipelines. This replaces(!) all pipelines with the new set of
	 * pipelines.
	 *
	 * @param pipelines the set of pipeline items.
	 */
	public void setPipelines(ObservableList<PipelineData.PipelineItem> pipelines) {
		if (this.pipelines == null) {
			this.pipelines = new PipelineData();
		}
		this.pipelines.setPipelines(pipelines);
	}

	/**
	 * Checks whether or not a file is a dip data file.
	 *
	 * @param file path to the file.
	 * @return {@code true} if it is a dip data file, {@code false} otherwise.
	 */
	public static boolean isDipDataFile(Path file) {
		if (Files.exists(file)) {
			try (InputStream is = Files.newInputStream(file)) {
				return isDipDataStream(is);
			} catch (IOException ex) {
				// fall-through
			}
		}
		return false;
	}

	/**
	 * Checks whether or not the input stream is a dip data file.
	 *
	 * @param is the input stream.
	 * @return {@code true} if it is a dip data file, {@code false} otherwise.
	 */
	public static boolean isDipDataStream(InputStream is) {
		final Element root = IOUtils.getRootElement(is);
		if (root == null) {
			return false;
		}
		return root.getTagName().equals(DipData.ROOT_TAG_NAME);
	}

	/**
	 * Loads or creates new dip data. Depending on whether the file exists, and
	 * whether it is a valid dip data file, 3 cases can occur: 1) the file does
	 * not exist, then new/empty dip data is returned, 2) the file exists and is
	 * valid dip data, then that dip data is loaded and returned, 3) the file
	 * exists but is not valid dip data, then an exception is thrown.
	 *
	 * @param file the dip data file.
	 * @return dip data.
	 * @throws java.io.IOException
	 * @throws javax.xml.bind.JAXBException
	 * @throws java.lang.ClassCastException
	 */
	public static DipData load(Path file) throws IOException, JAXBException, ClassCastException {
		if (!Files.exists(file)) {
			return new DipData();
		}
		try (InputStream is = Files.newInputStream(file)) {
			return XmlUtils.unmarshal(DipData.class, is);
		} catch (IOException | JAXBException | ClassCastException ex) {
			throw (ex);
		}
	}

	/**
	 * Saves dip data.
	 *
	 * @param file file to store the dip data.
	 * @throws JAXBException in case of unexpected errors during marshalling.
	 */
	public void save(Path file) throws JAXBException {
		XmlUtils.marshal(this, file);
	}

}
