package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.datatypes.DataType;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.core.UserSettings;
import ch.unifr.diva.dip.core.services.api.HostService;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.gui.pe.DataItemListView;
import ch.unifr.diva.dip.osgi.OSGiVersionPolicy;
import ch.unifr.diva.dip.utils.IOUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * PipelineData.
 */
@XmlRootElement(name = "pipelines")
@XmlAccessorType(XmlAccessType.NONE)
public class PipelineData {

	/**
	 * List of pipelines.
	 */
	@XmlElement(name = "pipeline")
	public List<Pipeline> list = new ArrayList<>();

	/**
	 * Empty constructor (needed for JAXB).
	 */
	public PipelineData() {

	}

	/**
	 * Creates a PipelineData instance from a list of Pipelines.
	 *
	 * @param pipelines a list of pipelines.
	 */
	public PipelineData(List<ch.unifr.diva.dip.core.model.Pipeline> pipelines) {
		for (ch.unifr.diva.dip.core.model.Pipeline pipeline : pipelines) {
			this.list.add(new Pipeline(pipeline));
		}
	}

	/**
	 * Returns the data of a single pipeline.
	 *
	 * @param id the id of the pipeline.
	 * @return the pipeline data, or null.
	 */
	public Pipeline getPipeline(int id) {
		for (Pipeline p : list) {
			if (p.id == id) {
				return p;
			}
		}
		return null;
	}

	/**
	 * Adds a pipeline to the pipeline data.
	 *
	 * @param pipeline the pipeline.
	 */
	public void addPipeline(Pipeline pipeline) {
		this.list.add(pipeline);
	}

	/**
	 * Adds a pipeline to the pipeline data.
	 *
	 * @param item the pipeline item.
	 */
	public void addPipeline(PipelineItem item) {
		addPipeline(item.toPipelineData());
	}

	/**
	 * Sets all pipelines. This replaces(!) all existing pipelines with the new
	 * set of pipelines.
	 *
	 * @param pipelines the set of pipeline items.
	 */
	public void setPipelines(ObservableList<PipelineItem> pipelines) {
		this.list.clear();
		for (PipelineItem item : pipelines) {
			addPipeline(item);
		}
	}

	/**
	 * Returns a list of pipeline items.
	 *
	 * @return a list of pipeline items.
	 */
	public ObservableList<PipelineItem> getPipelineItems() {
		final ObservableList<PipelineItem> pipelines = FXCollections.observableArrayList();
		for (Pipeline p : this.list) {
			pipelines.add(new PipelineItem(p));
		}
		return pipelines;
	}

	/**
	 * Returns "the empty pipeline". The empty pipeline consists of a single
	 * processor supposed to be a generator processor/service. This method uses
	 * the default generator.
	 *
	 * @param settings the user settings.
	 * @return the empty pipeline.
	 */
	public static Pipeline emptyPipeline(UserSettings settings) {
		return emptyPipeline(settings, HostService.DEFAULT_GENERATOR, HostService.VERSION.toString());
	}

	/**
	 * Returns "the empty pipeline". The empty pipeline consists of a single
	 * processor supposed to be a generator processor/service (that provides the
	 * page/image).
	 *
	 * @param settings the user settings.
	 * @param pid PID of the generator/service.
	 * @param version version of the generator/service.
	 * @return the empty pipeline.
	 */
	public static Pipeline emptyPipeline(UserSettings settings, String pid, String version) {
		final Pipeline pipeline = new Pipeline(settings);
		final Processor processor = new Processor(-1, pid, version);
		pipeline.processors().add(processor);
		return pipeline;
	}

	/**
	 * Replaces processors and associated connections. Used to repair corrupt
	 * projects (replacing unavailable services). Connections remain intact, as
	 * long as a valid port mapping is given.
	 *
	 * @param swap the processor swap object/data.
	 */
	public void swapProcessor(ProcessorSwap swap) {
		for (Pipeline<ProcessorWrapper> pipeline : this.list) {
			// swap connections
			final List<Integer> ids = pipeline.getProcessorIds(swap.pid, swap.version);
			for (Integer id : ids) {
				final List<Connection> deprecatedConnections = new ArrayList<>();
				for (Connection c : pipeline.connections()) {
					if (c.input.id == id) {
						final String key = swap.inputs.get(c.input.port);
						if (key != null) {
							c.input.port = key;
						} else {
							deprecatedConnections.add(c);
						}
					}
					if (c.output.id == id) {
						final String key = swap.outputs.get(c.output.port);
						if (key != null) {
							c.output.port = key;
						} else {
							deprecatedConnections.add(c);
						}
					}
				}
				pipeline.connections.list.removeAll(deprecatedConnections);
			}

			// swap processors
			for (Processor p : pipeline.processors()) {
				if (p.pid.equals(swap.pid) && p.version.equals(swap.version)) {
					p.pid = swap.toPID;
					p.version = swap.toVersion;
				}
			}
		}
	}

	/**
	 * Container holding data for a processor swap.
	 */
	public static class ProcessorSwap {

		/**
		 * PID of the old processor.
		 */
		final public String pid;

		/**
		 * Version of the old processor.
		 */
		final public String version;

		/**
		 * PID of the new processor.
		 */
		final public String toPID;

		/**
		 * Version of the new processor.
		 */
		final public String toVersion;

		/**
		 * Input port mapping of old keys to new keys.
		 */
		final public Map<String, String> inputs;

		/**
		 * Output port mapping of old keys to new keys.
		 */
		final public Map<String, String> outputs;

		/**
		 * Creates a new processor swap object.
		 *
		 * @param pid PID of the old processor.
		 * @param version version of the old processor.
		 * @param toPID PID of the new processor.
		 * @param toVersion version of the new processor.
		 * @param inputs Input port mapping of old keys to new keys.
		 * @param outputs Output port mapping of old keys to new keys.
		 */
		public ProcessorSwap(String pid, String version, String toPID, String toVersion, Map<String, String> inputs, Map<String, String> outputs) {
			this.pid = pid;
			this.version = version;
			this.toPID = toPID;
			this.toVersion = toVersion;
			this.inputs = inputs;
			this.outputs = outputs;
		}
	}

	/**
	 * Loads {@code PipelineData} from a file. This method opens an InputStream
	 * first to read the file (which might be necessary e.g. while reading from
	 * a {@code ZipFileSystem}).
	 *
	 * @param file path to the file to read from.
	 * @return unmarshalled {@code PipelineData}.
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static PipelineData loadAsStream(Path file) throws IOException, JAXBException {
		try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
			return load(stream);
		}
	}

	/**
	 * Loads {@code PipelineData} from a file.
	 *
	 * @param file path to the file to read from.
	 * @return unmarshalled {@code PipelineData}.
	 * @throws JAXBException
	 */
	public static PipelineData load(Path file) throws JAXBException {
		return XmlUtils.unmarshal(PipelineData.class, file);
	}

	/**
	 * Loads {@code PipelineData} from an {@code InputStream}.
	 *
	 * @param stream open {@code InputStream} to read from.
	 * @return unmarshalled {@code PipelineData}.
	 * @throws JAXBException
	 */
	public static PipelineData load(InputStream stream) throws JAXBException {
		return XmlUtils.unmarshal(PipelineData.class, stream);
	}

	/**
	 * A configuration of a pipeline.
	 *
	 * @param <T>
	 */
	public static class Pipeline<T extends ProcessorWrapper> {

		/**
		 * The ID of the pipeline.
		 */
		@XmlAttribute
		public int id;

		/**
		 * The name of the pipeline.
		 */
		@XmlAttribute
		public String name;

		/**
		 * The layout strategy of the pipeline.
		 */
		@XmlAttribute
		public String layoutStrategy;

		/**
		 * The version (upgrade) policy for all processors in the pipeline.
		 */
		@XmlAttribute
		public String versionPolicy;

		/**
		 * The processor list.
		 */
		public ProcessorList processors = new ProcessorList();

		/**
		 * The connection list.
		 */
		public ConnectionList connections = new ConnectionList();

		/**
		 * Empty constructor (needed for JAXB).
		 */
		public Pipeline() {
			this(null, -1, "empty pipeline");
		}

		/**
		 * Creates a new, empty pipeline.
		 *
		 * @param settings the user settings.
		 */
		public Pipeline(UserSettings settings) {
			this(settings, -1, "empty pipeline");
		}

		/**
		 * Creates a new, empty pipeline.
		 *
		 * @param settings the user settings.
		 * @param id the pipeline id.
		 * @param name the name of the pipeline.
		 */
		public Pipeline(UserSettings settings, int id, String name) {
			this.id = id;
			this.name = name;
			this.layoutStrategy = PipelineLayoutStrategy.getDefault().name();
			this.versionPolicy = (settings == null)
					? OSGiVersionPolicy.getDefault().name()
					: settings.osgi.versionPolicy.name();
		}

		/**
		 * Creates a PipelineData.Pipeline instance from a Pipeline.
		 *
		 * @param pipeline a pipeline.
		 */
		public Pipeline(ch.unifr.diva.dip.core.model.Pipeline<T> pipeline) {
			this.id = pipeline.id;
			this.name = pipeline.getName();
			this.layoutStrategy = pipeline.getLayoutStrategy().name();
			this.versionPolicy = pipeline.getVersionPolicy().name();

			final Map<OutputPort, ProcessorWrapper.PortMapEntry> outputPortMap;
			outputPortMap = ProcessorWrapper.getOutputPortMap(pipeline.processors());

			for (T wrapper : pipeline.processors()) {
				this.processors.list.add(new Processor(wrapper));

				if (wrapper.isAvailable()) {
					for (Map.Entry<String, InputPort> e : wrapper.processor().inputs().entrySet()) {
						final InputPort input = e.getValue();
						final ProcessorWrapper.PortMapEntry output = outputPortMap.get(input.connection());
						if (output != null) {
							this.connections.list.add(
									new Connection(
											input.getDataType(),
											output.id,
											output.port,
											wrapper.id,
											e.getKey()
									)
							);
						}
					}
				}
			}
		}

		/**
		 * Returns a list of processor ids that point to processors with the
		 * given PID and version.
		 *
		 * @param pid the PID of the processor.
		 * @param version the version of the processor.
		 * @return a list of processor ids.
		 */
		public List<Integer> getProcessorIds(String pid, String version) {
			final List<Integer> ids = new ArrayList<>();
			for (Processor p : processors()) {
				if (p.pid().equals(pid) && p.version().equals(version)) {
					ids.add(p.id);
				}
			}
			return ids;
		}

		/**
		 * Returns the processor list.
		 *
		 * @return the processor list.
		 */
		public List<Processor> processors() {
			return processors.list;
		}

		/**
		 * Returns the connection list.
		 *
		 * @return the connection list.
		 */
		public List<Connection> connections() {
			return connections.list;
		}

		/**
		 * Patches the pipeline.
		 *
		 * @param patch the delta patch.
		 */
		public void patch(PipelinePatch patch) {
			for (PipelinePatch.ProcessorPatch pp : patch.processors.list) {
				final Processor p = processors.get(pp.id);
				if (p != null) {
					for (Map.Entry<String, Object> e : pp.parameters.entrySet()) {
						p.parameters.put(e.getKey(), e.getValue());
					}
				}
			}
		}

	}

	/**
	 * A processor list of a pipeline.
	 */
	public static class ProcessorList {

		/**
		 * The list of processors.
		 */
		@XmlElement(name = "processor")
		public List<Processor> list = new ArrayList<>();

		/**
		 * Creates a new processor list.
		 */
		public ProcessorList() {
		}

		/**
		 * Returns the processor data by processor id.
		 *
		 * @param id the id of the processor.
		 * @return the processor data, or null if not found.
		 */
		public Processor get(int id) {
			for (Processor p : list) {
				if (p.id == id) {
					return p;
				}
			}
			return null;
		}

	}

	/**
	 * A connection list of a pipeline.
	 */
	public static class ConnectionList {

		/**
		 * The list of connections.
		 */
		@XmlElement(name = "connection")
		public List<Connection> list = new ArrayList<>();

		/**
		 * Creates a new connection list.
		 */
		public ConnectionList() {
		}
	}

	/**
	 * A processor object.
	 */
	@XmlRootElement
	public static class Processor {

		/**
		 * ID of the processor.
		 */
		@XmlAttribute
		public int id;

		/**
		 * PID of the processor/service.
		 */
		@XmlAttribute
		public String pid;

		/**
		 * Version of the processor/service.
		 */
		@XmlAttribute
		public String version;

		/**
		 * X position of the processor in the pipeline editor pane.
		 */
		@XmlAttribute
		public double x;

		/**
		 * Y position of the processor in the pipeline editor pane.
		 */
		@XmlAttribute
		public double y;

		/**
		 * Whether or not this processor is in editing mode (opened), or not
		 * (closed).
		 */
		@XmlAttribute
		public boolean editing;

		/**
		 * The parameters of the processor.
		 */
		@XmlElement
		public Map<String, Object> parameters;

		/**
		 * Creates a new processor object.
		 */
		public Processor() {
		}

		/**
		 * Creates a new processor object.
		 *
		 * @param id id (unique w.r.t. the pipeline) of the processor.
		 * @param pid the PID of the processor.
		 * @param version the version of the processor.
		 */
		public Processor(int id, String pid, String version) {
			this.id = id;
			this.pid = pid;
			this.version = version;
			this.x = 20;
			this.y = 20;
			this.editing = false;
			this.parameters = new HashMap<>();
		}

		/**
		 * Creates a new processor object.
		 *
		 * @param wrapper the processor wrapper.
		 */
		public Processor(ProcessorWrapper wrapper) {
			this.id = wrapper.id;
			this.pid = wrapper.pid();
			this.version = wrapper.version().toString();
			this.x = wrapper.layoutXProperty().doubleValue();
			this.y = wrapper.layoutYProperty().doubleValue();
			this.editing = wrapper.isEditing();

			// A deep copy of the parameters is necessary here s.t. we can easily
			// clone pipelines by going from a Pipeline to PipelineData, and back
			// to create a new pipeline.
			this.parameters = IOUtils.deepClone(wrapper.getParameterValues());
		}

		/**
		 * Safely returns the PID.
		 *
		 * @return the PID, or "-" as default if not specified.
		 */
		public String pid() {
			if (this.pid == null) {
				this.pid = "-";
			}
			return this.pid;
		}

		/**
		 * Safely returns the version string.
		 *
		 * @return the version string, or "0.0.0" as default if not specified.
		 */
		public String version() {
			if (this.version == null) {
				this.version = "0.0.0";
			}
			return this.version;
		}
	}

	/**
	 * A connection object.
	 */
	@XmlRootElement
	public static class Connection {

		/**
		 * The output port of the connection.
		 */
		public Port output;

		/**
		 * The input port of the connection.
		 */
		public Port input;

		/**
		 * The type of the connection.
		 */
		public String type;

		/**
		 * Creates a new connection object.
		 */
		public Connection() {
		}

		/**
		 * Creates a new connection object.
		 *
		 * @param dataType dataType of the connection.
		 * @param output id of the processor on the output port.
		 * @param outputPort key of the output port.
		 * @param input id of the processor on the input port.
		 * @param inputPort key of the input port.
		 */
		public Connection(DataType dataType, int output, String outputPort, int input, String inputPort) {
			this.type = dataType.getClass().getName();
			this.output = new Port(output, outputPort);
			this.input = new Port(input, inputPort);
		}
	}

	/**
	 * A port object.
	 */
	@XmlRootElement
	public static class Port {

		/**
		 * The processor id on the port.
		 */
		@XmlAttribute
		public int id;

		/**
		 * The key of the port.
		 */
		@XmlAttribute
		public String port;

		/**
		 * Creates a new port object.
		 */
		public Port() {
		}

		/**
		 * Creates a new port object.
		 *
		 * @param id the processor id on the port.
		 * @param port the key of the port.
		 */
		public Port(int id, String port) {
			this.id = id;
			this.port = port;
		}
	}

	/**
	 * A pipeline item. A list item pointing to a pipeline data object, or to an
	 * instantiated pipeline.
	 */
	public static class PipelineItem implements Localizable, DataItemListView.DataItem {

		final private ch.unifr.diva.dip.core.model.Pipeline pipeline;
		final private PipelineData.Pipeline data;
		final private StringProperty nameProperty;
		final private ObjectProperty<NamedGlyph> glyphProperty;

		/**
		 * Creates a new pipeline item for a pipeline.
		 *
		 * @param pipeline the pipeline.
		 */
		public PipelineItem(ch.unifr.diva.dip.core.model.Pipeline pipeline) {
			this(pipeline, null);
		}

		/**
		 * Creates a new pipeline item for a pipeline data object.
		 *
		 * @param data the pipeline data.
		 */
		public PipelineItem(PipelineData.Pipeline data) {
			this(null, data);
		}

		/**
		 * Creates a new pipeline item.
		 *
		 * @param pipeline the pipeline, or null.
		 * @param data the pipeline data, or null.
		 */
		private PipelineItem(ch.unifr.diva.dip.core.model.Pipeline pipeline, PipelineData.Pipeline data) {
			this.pipeline = pipeline;
			this.data = data;
			this.nameProperty = new SimpleStringProperty();
			this.glyphProperty = new SimpleObjectProperty();
			if (isNewItem()) {
				this.nameProperty.setValue(this.pipeline.getName());
				this.glyphProperty.set(MaterialDesignIcons.FLOPPY);
			} else {
				this.nameProperty.setValue(this.data.name);
			}
		}

		/**
		 * Returns the name property of the pipeline item.
		 *
		 * @return the name property of the pipeline item.
		 */
		@Override
		public StringProperty nameProperty() {
			return nameProperty;
		}

		/**
		 * Checks whether this pipeline item refers to a new pipeline (to be
		 * exported/saved), or to an existing one.
		 *
		 * @return true if this item refers to a new pipeline, false otherwise.
		 */
		final public boolean isNewItem() {
			return this.pipeline != null;
		}

		@Override
		public ObjectProperty<NamedGlyph> glyphProperty() {
			return this.glyphProperty;
		}

		/**
		 * Returns the pipeline data object.
		 *
		 * @return the pipeline data object.
		 */
		public PipelineData.Pipeline toPipelineData() {
			if (this.data != null) {
				this.data.name = this.nameProperty.get();
				return this.data;
			}
			if (this.pipeline != null) {
				this.pipeline.setName(this.nameProperty.get());
				return new PipelineData.Pipeline<>(this.pipeline);
			}
			return null;
		}

	}

}
