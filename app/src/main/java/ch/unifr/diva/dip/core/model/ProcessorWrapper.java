package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import ch.unifr.diva.dip.core.services.api.HostProcessor;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.utils.Modifiable;
import ch.unifr.diva.dip.utils.ModifiedProperty;
import ch.unifr.diva.dip.core.services.api.HostProcessorContext;
import ch.unifr.diva.dip.api.utils.ReflectionUtils;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.osgi.OSGiService;
import ch.unifr.diva.dip.utils.IOUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ProcessorWrapper wrapps a processor service. While the wrapper itself is
 * persistent, the wrapped processor service is a bit more elusive and might
 * disappear and come back on at any point in time, or might get replaced by a
 * completely different processor even.
 *
 * <p>
 * The ProcessorWrapper is just a prototype of a processor in a (prototypical)
 * pipeline so far. A RunnablePipeline will have to instantiate
 * RunnableProcessor's that extend from ProcessorWrapper.
 */
public class ProcessorWrapper implements Modifiable, Localizable {

	protected static final Logger log = LoggerFactory.getLogger(ProcessorWrapper.class);
	protected final ApplicationHandler handler;

	/**
	 * Unique id of the ProcessorWrapper. This id is unique with respect to its
	 * parent pipeline, that is among the set of all ProcessorWrappers in the
	 * particular pipeline. Not to be confused with the pid of the wrapped
	 * processor (service).
	 */
	public final int id;

	protected volatile String pid;
	protected final Version version;
	protected volatile Processor processor;
	protected volatile boolean isHostProcessor;
	protected final DoubleProperty layoutXProperty;
	protected final DoubleProperty layoutYProperty;
	protected final BooleanProperty availableProperty;
	protected final BooleanProperty editingProperty;
	protected final ModifiedProperty modifiedProcessorProperty;
	protected final InvalidationListener softEditListener;

	private Map<String, Object> parameters;

	/**
	 * Constructs an instance from stored PipelineData with custom context
	 * factories. These factories are used by RunnableProcessor's.
	 *
	 * @param processor a PipelineData definition of a processor.
	 * @param handler the application handler.
	 */
	protected ProcessorWrapper(PipelineData.Processor processor, ApplicationHandler handler) {
		this(
				processor.id,
				processor.pid,
				processor.version,
				processor.x,
				processor.y,
				processor.editing,
				processor.parameters,
				handler
		);
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param id new, unique id of the ProcessorWrapper, usually assigned by the
	 * parent pipeline.
	 * @param pid pid of the wrapped processor.
	 * @param version version of the wrapped processor.
	 * @param x x position of the processor view.
	 * @param y y position of the processor view.
	 * @param handler the application handler.
	 */
	public ProcessorWrapper(int id, String pid, String version, double x, double y, ApplicationHandler handler) {
		this(
				id,
				pid,
				version,
				x,
				y,
				false,
				null,
				handler
		);
	}

	private ProcessorWrapper(int id, String pid, String version, double x, double y, boolean editing, Map<String, Object> parameters, ApplicationHandler handler) {
		this.id = id;
		this.pid = pid;
		this.version = new Version(version);
		this.handler = handler;
		this.layoutXProperty = new SimpleDoubleProperty(x);
		this.layoutYProperty = new SimpleDoubleProperty(y);
		this.availableProperty = new SimpleBooleanProperty(false);
		this.editingProperty = new SimpleBooleanProperty(editing);
		this.parameters = parameters;
		this.modifiedProcessorProperty = new ModifiedProperty();
		this.softEditListener = (c) -> {
			final Project project = handler.getProject();
			if (project != null) {
				project.setModified(true);
			}
		};
	}

	/**
	 * Initializes the wrapped processor. Must be called before doing anything
	 * else (and can't be moved to/called from the constructor, since we need to
	 * override some methods in RunnableProcessor...).
	 */
	public void init() {
		setProcessor(pid);

		/*
		 * These are "soft" edits that don't really change the pipeline. For now,
		 * we don't make/mark the pipeline itself dirty, since we'd be forced to
		 * handle the case of modified pipelines that are in use (by some page)
		 * with no need to.
		 */
//		this.modifiedProcessorProperty.addObservedProperty(this.layoutXProperty);
//		this.modifiedProcessorProperty.addObservedProperty(this.layoutYProperty);
//		this.modifiedProcessorProperty.addObservedProperty(this.editingProperty);
		/*
		 * ...so instead we just mark the project as modified, s.t. we still can
		 * save such modifications.
		 */
		this.layoutXProperty.addListener(softEditListener);
		this.layoutYProperty.addListener(softEditListener);
		this.editingProperty.addListener(softEditListener);

	}

	/**
	 * Returns the layoutXProperty.
	 *
	 * @return the layoutXProperty.
	 */
	public DoubleProperty layoutXProperty() {
		return layoutXProperty;
	}

	/**
	 * Returns the layoutYProperty.
	 *
	 * @return the layoutYProperty.
	 */
	public DoubleProperty layoutYProperty() {
		return layoutYProperty;
	}

	/**
	 * Returns the availableProperty.
	 *
	 * @return the availableProperty.
	 */
	public final ReadOnlyBooleanProperty availableProperty() {
		return availableProperty;
	}

	/**
	 * Checks whether the wrapped processor is available or not.
	 *
	 * @return True if the processor is available, False otherwise.
	 */
	public final boolean isAvailable() {
		return availableProperty().get();
	}

	/**
	 * Returns the editingProperty.
	 *
	 * @return the editingProperty.
	 */
	public final BooleanProperty editingProperty() {
		return editingProperty;
	}

	/**
	 * Checks whether the wrapped processor is in editing mode or not. This only
	 * conserns the graphical representation of the wrapped processor in the
	 * pipeline. If in editing mode, the node is expanded to show parameters (to
	 * be edited), otherwise the node is more compact and only shows the
	 * minimum.
	 *
	 * @return True if the processor is in editing mode, False otherwise.
	 */
	public final boolean isEditing() {
		return editingProperty().get();
	}

	/**
	 * Returns the values of all parameters of the processor.
	 *
	 * @return the values of the parameters.
	 */
	public Map<String, Object> getParameterValues() {
		saveParameters(this.processor);
		return this.parameters;
	}

	@Override
	public ModifiedProperty modifiedProperty() {
		return modifiedProcessorProperty;
	}

	/**
	 * Returns the state of the wrapped processor.
	 *
	 * @return the state of the processor.
	 */
	public Processor.State state() {
		return (isAvailable() ? processor.state() : Processor.State.UNAVAILABLE);
	}

	/**
	 * Savely returns the processor's glyph, or the default one.
	 *
	 * @return the glyph (factory) of the processor.
	 */
	public NamedGlyph glyph() {
		return glyph(this.processor());
	}

	/**
	 * Savely returns a processor's glyph, or the default one.
	 *
	 * @param processor a processor.
	 * @return the glyph (factory) of the processor.
	 */
	public static NamedGlyph glyph(Processor processor) {
		if (processor == null || processor.glyph() == null) {
			return UIStrategyGUI.Glyphs.defaultProcessor;
		}

		return processor.glyph();
	}

	/**
	 * Returns the pid of the wrapped processor.
	 *
	 * @return the pid of the processor.
	 */
	public String pid() {
		return pid;
	}

	/**
	 * Returns the version of the wrapped processor.
	 *
	 * @return the version of the wrapped processor.
	 */
	public Version version() {
		return version;
	}

	/**
	 * Returns the wrapped processor.
	 *
	 * @return the wrapped processor instance.
	 */
	public final Processor processor() {
		return processor;
	}

	/**
	 * Sets the wrapped processor.
	 *
	 * @param pid pid of the desired processor service.
	 */
	public final void setProcessor(String pid) {
		this.pid = pid;
		updateProcessor();
	}

	/**
	 * Updates/hot-swaps the wrapped processor by creating a new instance of it.
	 * This method is also called the first time (by setProcessor) the wrapped
	 * processor is initialized.
	 */
	public void updateProcessor() {
		updateProcessor(false);
	}

	/**
	 * Updates/hot-swaps the wrapped processor by creating a new instance of it.
	 * This method is also called the first time (by setProcessor) the wrapped
	 * processor is initialized.
	 *
	 * @param forceInit forces initialization of the wrapped processor if set to
	 * True. This is usually set to False, and only needed for hot-swapping of
	 * processor services.
	 */
	public void updateProcessor(boolean forceInit) {
		final Processor newProcessor = getProcessor(pid, version);

		if (newProcessor == null) {
			availableProperty.set(false);
			return;
		}

		// disconnect old processor, wire up/connect new instance as was, unless
		// ports have disappeared or something...
		if (processor != null) {

			removeParameterListener(processor);
			saveParameters(processor);

			// disconnecting the old processor might change its ports (transmutable)
			// so we can't do that while iterating over the inputs, which is most
			// likely a LinkedHashMap, or we'll face a ConcurrentModificationException
			final List<InputPort> disconnectMe = new ArrayList<>();
			// similarly connecting dependent inputs to the new one likely removes
			// the wire to the old proc, same problem, so...
			final Map<InputPort, OutputPort> connectMe = new HashMap<>();

			for (Map.Entry<String, InputPort> e : processor.inputs().entrySet()) {
				final String key = e.getKey();
				final InputPort input = e.getValue();
				if (input.isConnected()) {
					final OutputPort output = input.connection();
					disconnectMe.add(input);
					final InputPort newInput = newProcessor.input(key);
					if (newInput != null) {
						connectMe.put(newInput, output);
					}
				}
			}

			for (Map.Entry<String, Set<InputPort>> e : processor.dependentInputs().entrySet()) {
				final String key = e.getKey();
				for (InputPort input : e.getValue()) {
					final OutputPort output = input.connection();
					disconnectMe.add(input);
					final OutputPort newOutput = newProcessor.output(key);
					if (newOutput != null) {
						connectMe.put(input, newOutput);
					}
				}
			}

			// disconnect old processor
			for (InputPort port : disconnectMe) {
				port.disconnect();
			}

			// and hook up new processor
			for (Map.Entry<InputPort, OutputPort> e : connectMe.entrySet()) {
				e.getKey().connectTo(e.getValue());
			}
		}

		// init new processor
		this.parameters = PersistentParameter.validatePreset(newProcessor.parameters(), this.parameters);
		initParameters(newProcessor);
		addParameterListener(newProcessor);

		// let the processor know it's ready again now. If processor was null,
		// this proc just got created and init will be called in RunnablePipeline
		// once all connections have been set up.
		if (processor != null || forceInit) {
			initProcessor(newProcessor);
		}

		processor = newProcessor;
		availableProperty.set(true);
	}

	/**
	 * Inits the wrapped processor. This signals the wrapped processor service
	 * that it's ready to be fully initialized, meaning that all parameters have
	 * been set/restored, and that ports are connected.
	 */
	protected void initProcessor() {
		if (processor() == null) {
			availableProperty.set(false);
			return;
		}
		initProcessor(processor());
	}

	/**
	 * Inits the wrapped processor. This signals the wrapped processor service
	 * that it's ready to be fully initialized, meaning that all parameters have
	 * been set/restored, and that ports are connected.
	 *
	 * @param processor the processor to be initialized.
	 */
	protected void initProcessor(Processor processor) {
		try {
			if (isHostProcessor) {
				final HostProcessor hp = (HostProcessor) processor;
				hp.init(getHostProcessorContext());
			} else {
				processor.init(getProcessorContext());
			}
		} catch (Exception ex) {
			log.warn("failed to initialize the processor: {}", processor, ex);
		}
	}

	private void initParameters(Processor processor) {
		for (Map.Entry<String, Object> e : this.parameters.entrySet()) {
			final String key = e.getKey();
			final Object value = e.getValue();
			final Parameter p = processor.parameters().get(key);
			if (p.isPersistent()) {
				final PersistentParameter pp = (PersistentParameter) p;
				try {
					pp.set(value);
				} catch (ClassCastException ex) {
					// this cast might go wrong upon having updated or replaced
					// a processor - which is okay. The parameter is lost.
					log.warn("Invalid processor parameter: {}={}, in: {}", key, value, this);
				}
			}
		}
	}

	private void saveParameters(Processor processor) {
		if (processor == null) {
			return;
		}
		this.parameters = PersistentParameter.getPreset(processor.parameters());
	}

	private void addParameterListener(Processor processor) {
		for (Parameter p : processor.parameters().values()) {
			if (p.isPersistent()) {
				final PersistentParameter pp = (PersistentParameter) p;
				this.modifiedProcessorProperty.addObservedProperty(pp.property());
			}
		}
	}

	private void removeParameterListener(Processor processor) {
		for (Parameter p : processor.parameters().values()) {
			if (p.isPersistent()) {
				final PersistentParameter pp = (PersistentParameter) p;
				this.modifiedProcessorProperty.removeObservedProperty(pp.property());
			}
		}
	}

	/**
	 * Marks the wrapped processor as deprecated. This happens when the OSGi
	 * service of the processor is removed from the OSGi framework, rendering
	 * the wrapped processor unavailable until the service shows up again.
	 */
	public void deprecateProcessor() {
		availableProperty.set(false);
	}

	/**
	 * Creates new instance of the processor (identified by its pid).
	 *
	 * @param pid id of the processor service.
	 * @return a new instance of the processor.
	 */
	private Processor getProcessor(String pid, Version version) {
		// check if pid is a host processor
		final Class<?> clazz = getHostProcessorClass(pid);
		if (clazz != null) {
			this.isHostProcessor = true;
			return newHostProcessor(clazz);
		}

		// return osgi service processor otherwise
		this.isHostProcessor = false;
		return newProcessor(pid, version);
	}

	/**
	 * Returns a new instance of a host processor.
	 *
	 * @param clazz class of the host processor.
	 * @return new instance of a host processor, or null.
	 */
	private Processor newHostProcessor(Class<?> clazz) {
		try {
			Constructor<?> c = clazz.getConstructor(HostProcessorContext.class);
			final HostProcessorContext context = getHostProcessorContext();
			// make sure to use constructur without args unless we actually
			// have a real context (will be provided by RunnableProcessor)
			if (context == null) {
				return (HostProcessor) clazz.newInstance();
			}
			return (HostProcessor) c.newInstance(context);
		} catch (InstantiationException | IllegalAccessException ex) {
			log.error("failed to instantiate new host processor: {}", pid, ex);
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
			log.error("failed to retrieve constructor for host processor: {}", pid, ex);
		}
		return null;
	}

	/**
	 * Returns a new instance of a processor.
	 *
	 * @param pid pid of the processor.
	 * @return new instance of a processor, or null.
	 */
	private Processor newProcessor(String pid, Version version) {
		final OSGiService<Processor> service = handler.osgi.getProcessor(
				pid,
				version
		);
		if (service != null) {
			final Processor proc = service.serviceObject;
			try {
				final ProcessorContext context = getProcessorContext();
				// make sure to use constructur without args unless we actually
				// have a real context (will be provided by RunnableProcessor)
				if (context == null) {
					return proc.getClass().newInstance();
				}
				return proc.newInstance(context);
			} catch (InstantiationException | IllegalAccessException t) {
				log.error("failed to instantitate new processor: {}", pid, t);
			}
		}

		log.error("invalid pid. There is no such processor: {}", pid);
		return null;
	}

	private volatile HostProcessorContext hostProcessorContext;

	private HostProcessorContext getHostProcessorContext() {
		if (hostProcessorContext == null) {
			hostProcessorContext = newHostProcessorContext();
		}
		return hostProcessorContext;
	}

	// overridden by RunnableProcessor
	protected HostProcessorContext newHostProcessorContext() {
		return null;
	}

	private volatile ProcessorContext processorContext;

	private ProcessorContext getProcessorContext() {
		if (processorContext == null) {
			processorContext = newProcessorContext();
		}
		return processorContext;
	}

	// overridden by RunnableProcessor
	protected ProcessorContext newProcessorContext() {
		return null;
	}

	/**
	 * Return the class of the host processor - if it exists. This method is
	 * also used to see if a processor (given by its pid) is a host processor or
	 * not.
	 *
	 * @param pid the pid of the host processor.
	 * @return the class of the host processor, or null if not found.
	 */
	protected final Class<?> getHostProcessorClass(String pid) {
		Class<?> clazz = ReflectionUtils.getClass(pid);
		if (clazz != null) {
			if (HostProcessor.class.isAssignableFrom(clazz)) {
				return clazz;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ "id=" + id
				+ ", pid=" + pid
				+ "}";
	}

	/**
	 * Copies the parameters of another processor. This deep-copies all
	 * parameters from the given processor.
	 *
	 * @param <T> processor class.
	 * @param p the processor to copy the parameters from.
	 */
	public <T extends ProcessorWrapper> void copyParameters(T p) {
		copyParameters(p.processor().parameters());
	}

	/**
	 * Sets/copies the parameters. This method deep-copies all given parameters
	 * to this processor.
	 *
	 * @param p the parameter map to be copied.
	 */
	public void copyParameters(Map<String, Parameter> p) {
		final Map<String, Parameter> q = this.processor().parameters();
		final List<String> secondPass = new ArrayList<>();

		for (Map.Entry<String, Parameter> e : p.entrySet()) {
			final String key = e.getKey();
			if (e.getValue().isPersistent()) {
				if (q.containsKey(key) && q.get(key).isPersistent()) {
					copyParameterValue(e.getValue(), q.get(key));
				} else {
					secondPass.add(key);
				}
			}
		}
		// do a second pass for parameters that couldn't be set the first time.
		// this might be needed for parameters with side-effects w.r.t. the
		// current set of parameters (e.g. setting a such a "master"-parameter
		// could enable dependent parameters that weren't present during the
		// first pass)
		for (String key : secondPass) {
			final Parameter pp = p.get(key);
			if (pp.isPersistent() && q.containsKey(key) && q.get(key).isPersistent()) {
				copyParameterValue(pp, q.get(key));
			}
		}
		// ...arguably this could be nested s.t. any number of passes might be
		// needed. But screw that; parameters with dependent child-parameters
		// simply need to be always exposed/present BY DESIGN.
	}

	/**
	 * Sets/copies the parameters. This method deep-copies all given parameters
	 * to this processor.
	 *
	 * @param parameters the new parameters.
	 */
	public void setParameters(Map<String, Object> parameters) {
		final Map<String, Parameter> q = this.processor().parameters();
		for (Map.Entry<String, Object> e : parameters.entrySet()) {
			final String key = e.getKey();
			if (q.containsKey(key)) {
				final Parameter p = q.get(key);
				if (p.isPersistent()) {
					final PersistentParameter pp = (PersistentParameter) p;
					pp.set(IOUtils.deepClone(e.getValue()));
				}
			}
		}
	}

	private void copyParameterValue(Parameter from, Parameter to) {
		final PersistentParameter p = (PersistentParameter) to;
		final PersistentParameter q = (PersistentParameter) from;
		p.set(IOUtils.deepClone(q.get()));
	}

	/**
	 * Checks whether the parameters of a processor are all equal to the
	 * parameters of this one.
	 *
	 * @param <T> class of the processor.
	 * @param p a processor.
	 * @return true if the parameters are equal, false otherwise.
	 */
	public <T extends ProcessorWrapper> boolean equalParameters(T p) {
		return equalParameters(this, p);
	}

	/**
	 * Checks whether two processors have equal parameters.
	 *
	 * @param <T> class of the processor.
	 * @param p a processor.
	 * @param q another processor.
	 * @return true if both sets of parameters are equal, false otherwise.
	 */
	public static <T extends ProcessorWrapper> boolean equalParameters(T p, T q) {
		return equalParameters(p.processor().parameters(), q.processor().parameters());
	}

	/**
	 * Checks whether two parameter maps are equal.
	 *
	 * @param p a parameter map.
	 * @param q another parameter map.
	 * @return true if both parameter maps are equal, false otherwise.
	 */
	public static boolean equalParameters(Map<String, Parameter> p, Map<String, Parameter> q) {
		if (p.size() != q.size()) {
			return false;
		}

		for (Map.Entry<String, Parameter> e : p.entrySet()) {
			final String key = e.getKey();
			// we don't really care about non-persistent parameters (i.e. labels and such...)
			if (e.getValue().isPersistent()) {
				if (!q.containsKey(key)) {
					return false;
				}
				final PersistentParameter pp = (PersistentParameter) e.getValue();
				final PersistentParameter pq = (PersistentParameter) q.get(key);
				if (!pp.get().equals(pq.get())) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Searches a list of {@code ProcessorWrapper} by id.
	 *
	 * @param <T>
	 * @param id id of the {@code ProcessorWrapper}.
	 * @param wrappers a list of {@code ProcessorWrapper}s.
	 * @return the {@code ProcessorWrapper} with the given id, or null if not
	 * found.
	 */
	public static <T extends ProcessorWrapper> T getProcessorWrapper(int id, List<T> wrappers) {
		for (T wrapper : wrappers) {
			if (wrapper.id == id) {
				return wrapper;
			}
		}
		return null;
	}

	/**
	 * Builds a map from OutputPort objects to PortMapEntries which specify the
	 * port. Since ports are directly linked but don't know about their parent
	 * processors, this mapping is needed to retrieve processor id and key of an
	 * output port.
	 *
	 * @param <T>
	 * @param wrappers list of wrapped processors.
	 * @return map of PortMapEntry indexed by OutputPort.
	 */
	public static <T extends ProcessorWrapper> Map<OutputPort, PortMapEntry> getOutputPortMap(List<T> wrappers) {
		final Map<OutputPort, PortMapEntry> map = new HashMap<>();

		for (ProcessorWrapper wrapper : wrappers) {
			if (!wrapper.isAvailable()) {
				continue;
			}
			for (Map.Entry<String, OutputPort> e : wrapper.processor().outputs().entrySet()) {
				map.put(e.getValue(), new PortMapEntry(wrapper.id, e.getKey()));
			}
		}

		return map;
	}

	/**
	 * Builds a map from InputPort objects to PortMapEntries which specify the
	 * port. Since ports are directly linked but don't know about their parent
	 * processors, this mapping is needed to retrieve processor id and key of an
	 * input port.
	 *
	 * @param <T>
	 * @param wrappers list of wrapped processors.
	 * @return map of PortMapEntry indexed by InputPort.
	 */
	public static <T extends ProcessorWrapper> Map<InputPort, PortMapEntry> getInputPortMap(List<T> wrappers) {
		final Map<InputPort, PortMapEntry> map = new HashMap<>();

		for (ProcessorWrapper wrapper : wrappers) {
			for (Map.Entry<String, InputPort> e : wrapper.processor().inputs().entrySet()) {
				map.put(e.getValue(), new PortMapEntry(wrapper.id, e.getKey()));
			}
		}

		return map;
	}

	/**
	 * An entry specifying a port (by key) of a processor (by id).
	 */
	public static class PortMapEntry {

		/**
		 * Unique id of the ProcessorWrapper/RunnableProcessor.
		 */
		public final int id;

		/**
		 * Unique key of the port.
		 */
		public final String port;

		/**
		 * Default constructor.
		 *
		 * @param id id of the processor.
		 * @param port key of the port.
		 */
		public PortMapEntry(int id, String port) {
			this.id = id;
			this.port = port;
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName()
					+ "{"
					+ "id=" + this.id
					+ ",port=" + this.port
					+ "}";
		}

		@Override
		public int hashCode() {
			int hash = 17;
			hash = 31 * hash + id;
			hash = 31 * hash + port.hashCode();
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final PortMapEntry other = (PortMapEntry) obj;
			if (this.id != other.id) {
				return false;
			}
			return Objects.equals(this.port, other.port);
		}
	}

}
