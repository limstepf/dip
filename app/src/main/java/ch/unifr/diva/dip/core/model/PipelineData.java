package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.datatypes.DataType;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * PipelineData.
 */
@XmlRootElement(name = "pipelines")
public class PipelineData {

	@XmlElement(name = "pipeline")
	public List<Pipeline> list = new ArrayList<>();

	/**
	 * Empty constructor (needed for JAXB).
	 */
	public PipelineData() {
		//
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
	 * Returns "the empty pipeline". The empty pipeline consists of a single
	 * processor supposed to be the PageGenerator.
	 *
	 * @param pid pid of the PageGenerator.
	 * @return the empty pipeline.
	 */
	public static Pipeline emptyPipeline(String pid) {
		final Pipeline pipeline = new Pipeline();
		final Processor processor = new Processor(-1, pid);
		pipeline.processors().add(processor);
		return pipeline;
	}

	/**
	 * Checks whether there is exactly one pipeline encoded. This method is used
	 * for PipelineData that encodes exactly one pipeline (e.g. a
	 * RunnablePipeline).
	 *
	 * @return True if there is exactly one pipeline encoded, False otherwise.
	 */
	public boolean hasPrimaryPipeline() {
		return (this.list.size() == 1);
	}

	/**
	 * Returns the data of the first (or primary) pipeline. This method is used
	 * for PipelineData that encodes exactly one pipeline (e.g. a
	 * RunnablePipeline). Check with {@code hasOnePipeline()} first if there is
	 * actually a primary pipeline.
	 *
	 * @return the primary pipeline.
	 */
	public Pipeline primaryPipeline() {
		return this.list.get(0);
	}

	// replaces a processor and associated connections
	// used to repair corrupt projects (replacing missing services)
	public void swapProcessor(ProcessorSwap swap) {
		for (Pipeline<ProcessorWrapper> pipeline : this.list) {
			final int id = pipeline.getProcessorId(swap.pid);

			// swap connections
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

			// swap processor
			for (Processor p : pipeline.processors()) {
				if (p.pid.equals(swap.pid)) {
					p.pid = swap.toPID;
				}
			}
		}
	}

	/**
	 * Container holding data for a processor swap.
	 */
	public static class ProcessorSwap {

		public final String pid;
		public final String toPID;
		final Map<String, String> inputs; // key, new key
		final Map<String, String> outputs; // key, new key

		public ProcessorSwap(String pid, String toPID, Map<String, String> inputs, Map<String, String> outputs) {
			this.pid = pid;
			this.toPID = toPID;
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

		@XmlAttribute
		public int id;

		@XmlAttribute
		public String name;

		@XmlAttribute
		public String layoutStrategy;

		public ProcessorList processors = new ProcessorList();
		public ConnectionList connections = new ConnectionList();

		/**
		 * Empty constructor (needed for JAXB).
		 */
		public Pipeline() {
			this(-1, "empty pipeline");
		}

		public Pipeline(int id, String name) {
			this.id = id;
			this.name = name;
			this.layoutStrategy = PipelineLayoutStrategy.getDefault().name();
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

			final Map<OutputPort, ProcessorWrapper.PortMapEntry> outputPortMap;
			outputPortMap = ProcessorWrapper.getOutputPortMap(pipeline.processors());

			for (T wrapper : pipeline.processors()) {
				this.processors.list.add(new Processor(wrapper));

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

		public int getProcessorId(String pid) {
			for (Processor p : processors()) {
				if (p.pid.equals(pid)) {
					return p.id;
				}
			}
			return -1;
		}

		public List<Processor> processors() {
			return processors.list;
		}

		public List<Connection> connections() {
			return connections.list;
		}
	}

	public static class ProcessorList {

		@XmlElement(name = "processor")
		public List<Processor> list = new ArrayList<>();

		public ProcessorList() {
		}
	}

	public static class ConnectionList {

		@XmlElement(name = "connection")
		public List<Connection> list = new ArrayList<>();

		public ConnectionList() {
		}
	}

	@XmlRootElement
	public static class Processor {

		@XmlAttribute
		public int id;

		@XmlAttribute
		public String pid;

		@XmlAttribute
		public double x;

		@XmlAttribute
		public double y;

		@XmlAttribute
		public boolean editing;

		@XmlElement
		public Map<String, Object> parameters;

		public Processor() {
		}

		public Processor(int id, String pid) {
			this.id = id;
			this.pid = pid;
			this.x = 20;
			this.y = 20;
			this.editing = false;
			this.parameters = new HashMap<>();
		}

		public Processor(ProcessorWrapper wrapper) {
			this.id = wrapper.id;
			this.pid = wrapper.pid();
			this.x = wrapper.layoutXProperty().doubleValue();
			this.y = wrapper.layoutYProperty().doubleValue();
			this.editing = wrapper.isEditing();
			this.parameters = wrapper.parameters();
		}
	}

	@XmlRootElement
	public static class Connection {

		public Port output;
		public Port input;
		public String type;

		public Connection() {
		}

		public Connection(DataType dataType, int output, String outputPort, int input, String inputPort) {
			this.type = dataType.getClass().getName();
			this.output = new Port(output, outputPort);
			this.input = new Port(input, inputPort);
		}
	}

	@XmlRootElement
	public static class Port {

		@XmlAttribute
		public int id;
		@XmlAttribute
		public String port;

		public Port() {
		}

		public Port(int id, String port) {
			this.id = id;
			this.port = port;
		}
	}

}
