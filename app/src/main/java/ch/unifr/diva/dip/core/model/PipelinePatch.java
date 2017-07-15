package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.utils.XmlUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A delta patch for a pipeline. Patches the parameters of processors in a
 * pipeline, leaving the structure of the pipeline (processors and connections)
 * intact.
 */
@XmlRootElement(name = "pipeline-patch")
public class PipelinePatch {

	/**
	 * The ID of the pipeline to be patched.
	 */
	@XmlAttribute
	public int id;

	/**
	 * The processor delta patch list.
	 */
	public ProcessorPatchList processors = new ProcessorPatchList();

	/**
	 * Empty constructor. Needed for JAXB.
	 */
	public PipelinePatch() {
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append('@');
		sb.append(Integer.toHexString(this.hashCode()));
		sb.append('{');
		sb.append("pipeline.id=");
		sb.append(this.id);
		sb.append(", ");

		final Iterator<ProcessorPatch> patches = this.processors.list.iterator();
		while (patches.hasNext()) {
			final ProcessorPatch patch = patches.next();

			sb.append("processor.patch.");
			sb.append(patch.id);
			sb.append("=[");
			final Iterator<String> keys = patch.parameters.keySet().iterator();
			while (keys.hasNext()) {
				final String key = keys.next();
				sb.append(key);
				sb.append('=');
				sb.append(patch.parameters.get(key));
				if (keys.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append(']');

			if (patches.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append('}');
		return sb.toString();
	}

	/**
	 * Checks whether this is an empty patch.
	 *
	 * @return {@code true} if the patch is empty (and can be omitted),
	 * {@code false} otherwise.
	 */
	public boolean isEmpty() {
		if (this.processors == null) {
			return true;
		}
		for (ProcessorPatch patch : this.processors.list) {
			if (!patch.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds a processor delta patch.
	 *
	 * @param patch the processor delta patch.
	 */
	public void add(ProcessorPatch patch) {
		this.processors.list.add(patch);
	}

	/**
	 * A list of processor delta patches.
	 */
	public static class ProcessorPatchList {

		/**
		 * The list of processors.
		 */
		@XmlElement(name = "processor-patch")
		public List<ProcessorPatch> list = new ArrayList<>();

		/**
		 * Creates a new processor list.
		 */
		public ProcessorPatchList() {
		}

	}

	/**
	 * A processor patch.
	 */
	@XmlRootElement
	public static class ProcessorPatch {

		/**
		 * ID of the processor.
		 */
		@XmlAttribute
		public int id;

		/**
		 * The patched parameters of the processor. These will overwrite the
		 * original parameters of the processor.
		 */
		@XmlElement
		public Map<String, Object> parameters;

		/**
		 * Empty constructor. Needed for JAXB.
		 */
		public ProcessorPatch() {
		}

		/**
		 * Processor delta patch constructor.
		 *
		 * @param id id of the processor.
		 */
		public ProcessorPatch(int id) {
			this.id = id;
			this.parameters = new HashMap<>();
		}

		/**
		 * Checks whether the processor patch is empty (or null).
		 *
		 * @return {@code true} if the patch is empty (and must be ommited),
		 * {@code false} otherwise.
		 */
		public boolean isEmpty() {
			if (this.parameters == null) {
				return true;
			}
			return this.parameters.isEmpty();
		}

		/**
		 * Puts a parameter to be patched.
		 *
		 * @param key the key of the parameter.
		 * @param value the patched value.
		 */
		public void put(String key, Object value) {
			this.parameters.put(key, value);
		}

	}

	/**
	 * Loads a pipeline patch. This method reads the file from an input stream,
	 * which is needed to read from a zip filesystem.
	 *
	 * @param file path to the file containing the patch.
	 * @return the pipeline patch.
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static PipelinePatch loadAsStream(Path file) throws IOException, JAXBException {
		try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
			return load(stream);
		}
	}

	/**
	 * Loads a pipeline patch.
	 *
	 * @param file path to the file containing the patch.
	 * @return the pipeline patch.
	 * @throws JAXBException
	 */
	public static PipelinePatch load(Path file) throws JAXBException {
		return XmlUtils.unmarshal(PipelinePatch.class, file);
	}

	/**
	 * Load a pipeline patch from an input stream.
	 *
	 * @param stream the input stream containing the patch.
	 * @return the pipeline patch.
	 * @throws JAXBException
	 */
	public static PipelinePatch load(InputStream stream) throws JAXBException {
		return XmlUtils.unmarshal(PipelinePatch.class, stream);
	}

	/**
	 * Writes the pipeline patch to an output stream.
	 *
	 * @param stream the output stream.
	 * @throws JAXBException
	 */
	public void save(OutputStream stream) throws JAXBException {
		XmlUtils.marshal(this, stream);
	}

	/**
	 * Writes the pipeline patch to a file.
	 *
	 * @param file path to the file.
	 * @throws JAXBException
	 */
	public void save(Path file) throws JAXBException {
		XmlUtils.marshal(this, file);
	}

	/**
	 * Creates a pipeline delta patch.
	 *
	 * @param base the base (or prototype) pipeline.
	 * @param revision the modified pipeline.
	 * @return a pipeline delta patch with including the difference between base
	 * and revision.
	 */
	public static PipelinePatch createPatch(Pipeline<ProcessorWrapper> base, RunnablePipeline revision) {
		final PipelinePatch patch = new PipelinePatch();

		final int n = base.processors().size();
		if (n != revision.processors().size()) {
			return patch;
		}

		for (int i = 0; i < n; i++) {
			final ProcessorWrapper a = base.processors().get(i);
			final ProcessorWrapper b = revision.processors().get(i);

			final ProcessorPatch p = new ProcessorPatch(a.id);

			for (Map.Entry<String, Object> e : a.getParameterValues().entrySet()) {
				final String key = e.getKey();
				if (b.getParameterValues().containsKey(key)) {
					final Object obj = b.getParameterValues().get(key);
					if (!e.getValue().equals(obj)) {
						p.put(key, obj);
					}
				}
			}

			patch.add(p);
		}

		return patch;
	}

}
