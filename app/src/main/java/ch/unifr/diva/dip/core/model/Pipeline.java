package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.gui.pe.PipelineLayoutStrategy;
import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.execution.PipelineExecutor;
import ch.unifr.diva.dip.core.model.PrototypeProcessor.PortMapEntry;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.osgi.OSGiVersionPolicy;
import ch.unifr.diva.dip.utils.Modifiable;
import ch.unifr.diva.dip.utils.ModifiedProperty;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline base class. Note that {@code Pipeline<PrototypeProcessor>} is pretty
 * much equivalent to a {@code PrototypePipeline}. The latter is usually nicer
 * to write and read than using parametric polymorphism/generics (historically
 * this class wasn't always abstract, and PrototypePipeline didn't exist...).
 * Also it lines up nicely with the processor wrappers
 * {@code PrototypeProcessor} and {@code RunnableProcessor}. <br />
 *
 * Methods that need to take/work with both, {@code PrototypePipeline} and
 * {@code RunnablePipeline}, can still program against {@code Pipeline<T>} where
 * {@code <T extends PrototypeProcessor>} (granted, generics/type erasure with
 * collections can be a bit of a pain in the ass...).
 *
 * @param <T> type of the processor wrapper; {@code PrototypeProcessor} or
 * {@code RunnableProcessor}.
 */
public abstract class Pipeline<T extends PrototypeProcessor> implements Modifiable {

	protected static final Logger log = LoggerFactory.getLogger(Pipeline.class);
	protected final ApplicationHandler handler;

	/**
	 * Pipeline id. Unique key for all pipelines in a project.
	 */
	public final int id;

	protected int maxProcessorId = -1;
	protected final StringProperty name;
	protected final ObservableList<T> processors;
	protected final ObjectProperty<PipelineExecutor.Type> pipelineExecutorProperty;
	protected final ObjectProperty<PipelineLayoutStrategy> layoutStrategyProperty;
	protected final ObjectProperty<OSGiVersionPolicy> versionPolicyProperty;
	protected final ModifiedProperty modifiedPipelineProperty;

	/**
	 * Creates an empty pipeline.
	 *
	 * @param handler the application handler.
	 * @param id id of the pipeline. Needs to be unique among all pipelines in
	 * the project, and is usually assigned by the PipelineManager.
	 * @param name name of the pipeline.
	 */
	public Pipeline(ApplicationHandler handler, int id, String name) {
		this(
				handler,
				id,
				name,
				handler.settings.pipelineEditor.getDefaultPipelineExecutor(),
				handler.settings.pipelineEditor.getDefaultPipelineLayout(),
				handler.settings.osgi.versionPolicy
		);
	}

	/**
	 * Creates an empty pipeline.
	 *
	 * @param handler the application handler.
	 * @param id id of the pipeline. Needs to be unique among all pipelines in
	 * the project, and is usually assigned by the PipelineManager.
	 * @param name name of the pipeline.
	 * @param pipelineExecutor the pipeline executor.
	 * @param layoutStrategy the layout strategy.
	 * @param versionPolicy the version policy.
	 */
	public Pipeline(ApplicationHandler handler, int id, String name, PipelineExecutor.Type pipelineExecutor, PipelineLayoutStrategy layoutStrategy, OSGiVersionPolicy versionPolicy) {
		this.handler = handler;
		this.id = id;
		this.name = new SimpleStringProperty(name);
		this.processors = FXCollections.observableArrayList();
		this.pipelineExecutorProperty = new SimpleObjectProperty<>(pipelineExecutor);
		this.layoutStrategyProperty = new SimpleObjectProperty<>(layoutStrategy);
		this.versionPolicyProperty = new SimpleObjectProperty<>(versionPolicy);
		this.modifiedPipelineProperty = new ModifiedProperty();
		this.modifiedPipelineProperty.addObservedProperty(this.name);
		this.modifiedPipelineProperty.addObservedProperty(this.processors);
		this.modifiedPipelineProperty.addObservedProperty(this.pipelineExecutorProperty);
		this.modifiedPipelineProperty.addObservedProperty(this.layoutStrategyProperty);
		this.modifiedPipelineProperty.addObservedProperty(this.versionPolicyProperty);
	}

	/**
	 * Creates/restores a pipeline according to the given pipeline data.
	 *
	 * @param handler the application handler.
	 * @param pipeline the pipeline data.
	 */
	public Pipeline(ApplicationHandler handler, PipelineData.Pipeline pipeline) {
		this(handler, pipeline, -1);
	}

	/**
	 * Creates/restores a pipeline according to the given pipeline data.
	 *
	 * @param handler the application handler.
	 * @param pipeline the pipeline data.
	 * @param id a new pipeline id, or {@code -1} to keep using the one defined
	 * in the pipeline data.
	 */
	public Pipeline(ApplicationHandler handler, PipelineData.Pipeline pipeline, int id) {
		this(
				handler,
				(id < 0) ? pipeline.id : id,
				pipeline.name
		);

		// don't count the following initialization as modifications
		this.modifiedPipelineProperty.removeObservedProperty(this.processors);

		this.pipelineExecutorProperty.set(
				PipelineExecutor.Type.get(pipeline.pipelineExecutor)
		);
		this.layoutStrategyProperty.set(
				PipelineLayoutStrategy.get(pipeline.layoutStrategy)
		);
		this.versionPolicyProperty.set(
				OSGiVersionPolicy.get(pipeline.versionPolicy)
		);

		for (PipelineData.Processor p : pipeline.processors.list) {
			@SuppressWarnings("unchecked")
			final T wrapper = (T) new PrototypeProcessor(p, handler);
			wrapper.init();
			addProcessor(wrapper);
			levelMaxProcessorId(p.id);
		}

		initConnections(pipeline, processors);

		// init processors now that they're connected
		for (PrototypeProcessor p : processors) {
			p.initProcessor();
		}

		this.modifiedPipelineProperty.addObservedProperty(this.processors);
	}

	/**
	 * Sets up all connections in a pipeline. This method is called after all
	 * processors have been created/registered and connects them according to
	 * the given pipeline data.
	 *
	 * @param pipeline the pipeline data.
	 * @param processors the list of all processors in the pipeline.
	 */
	protected final void initConnections(PipelineData.Pipeline pipeline, List<T> processors) {
		for (PipelineData.Connection c : pipeline.connections.list) {
			final T input = PrototypeProcessor.getProcessorWrapper(c.input.id, processors);
			final T output = PrototypeProcessor.getProcessorWrapper(c.output.id, processors);

			if (input == null) {
				log.warn("invalid connection. Input processor {} not found", c.input.id);
				continue;
			}
			if (input.serviceObject() == null) {
				log.warn("invalid connection. Input processor OSGi service {} is not available", input);
				continue;
			}

			if (output == null) {
				log.warn("invalid connection. Output processor {} not found", c.output.id);
				continue;
			}
			if (output.serviceObject() == null) {
				log.warn("invalid connection. Output processor OSGi service {} is not available", output);
				continue;
			}

			final InputPort<?> ip = input.serviceObject().input(c.input.port);
			final OutputPort<?> op = output.serviceObject().output(c.output.port);
			if (ip != null && op != null) {
				ip.connectTo(op);
			} else {
				log.warn("invalid ports from {} to {}",
						String.format("%s::%s", input, ip),
						String.format("%s::%s", output, op)
				);
			}
		}
	}

	/**
	 * Returns the name of the pipeline.
	 *
	 * @return the name of the pipeline.
	 */
	public String getName() {
		return name().get();
	}

	/**
	 * Sets the name of the pipeline.
	 *
	 * @param name the name of the pipeline.
	 */
	public void setName(String name) {
		name().set(name);
	}

	/**
	 * Returns the name of the pipeline.
	 *
	 * @return name of the pipeline.
	 */
	public StringProperty name() {
		return name;
	}

	/**
	 * Returns the pipeline executor property.
	 *
	 * @return the pipeline executor property.
	 */
	public ObjectProperty<PipelineExecutor.Type> pipelineExecutorProperty() {
		return this.pipelineExecutorProperty;
	}

	/**
	 * Returns the pipeline executor.
	 *
	 * @return the pipeline executor.
	 */
	public PipelineExecutor.Type getPipelineExecutor() {
		return this.pipelineExecutorProperty.get();
	}

	/**
	 * Sets/updates the pipeline executor.
	 *
	 * @param executor the new pipeline executor.
	 */
	public void setPipelineExecutor(PipelineExecutor.Type executor) {
		this.pipelineExecutorProperty.set(executor);
	}

	/**
	 * Returns the layout strategy property.
	 *
	 * @return the layout strategy property.
	 */
	public ObjectProperty<PipelineLayoutStrategy> layoutStrategyProperty() {
		return this.layoutStrategyProperty;
	}

	/**
	 * Returns the pipeline's layout strategy.
	 *
	 * @return the pipeline layout strategy in use.
	 */
	public PipelineLayoutStrategy getLayoutStrategy() {
		return this.layoutStrategyProperty.get();
	}

	/**
	 * Sets/updates the pipeline its layout strategy.
	 *
	 * @param strategy the new pipeline layout strategy.
	 */
	public void setLayoutStrategy(PipelineLayoutStrategy strategy) {
		this.layoutStrategyProperty.set(strategy);
	}

	/**
	 * Returns the pipeline's version policy property.
	 *
	 * @return the version policy property.
	 */
	public ObjectProperty<OSGiVersionPolicy> versionPolicyProperty() {
		return this.versionPolicyProperty;
	}

	/**
	 * Returns the pipeline's version policy.
	 *
	 * @return the version politcy.
	 */
	public OSGiVersionPolicy getVersionPolicy() {
		return this.versionPolicyProperty.get();
	}

	/**
	 * Sets the pipeline's version policy.
	 *
	 * @param policy the new version policy.
	 */
	public void setVersionPolicy(OSGiVersionPolicy policy) {
		this.versionPolicyProperty.set(policy);
	}

	private final Map<OutputPort<?>, PrototypeProcessor.PortMapEntry> outputPortMap = new HashMap<>();
	private final Map<InputPort<?>, PrototypeProcessor.PortMapEntry> inputPortMap = new HashMap<>();

	/**
	 * Returns the output port map. Ports are stupid on do not know anything
	 * about the processor they belong to. That's why we need a port map,
	 * mapping the port to a {@code PortMapEntry} which allows us to retrieve
	 * the processor id and the port-key of the port.
	 *
	 * @return the output port map.
	 */
	public Map<OutputPort<?>, PrototypeProcessor.PortMapEntry> outputPortMap() {
		return this.outputPortMap;
	}

	/**
	 * Returns the input port map. Ports are stupid on do not know anything
	 * about the processor they belong to. That's why we need a port map,
	 * mapping the port to a {@code PortMapEntry} which allows us to retrieve
	 * the processor id and the port-key of the port.
	 *
	 * @return the input port map.
	 */
	public Map<InputPort<?>, PrototypeProcessor.PortMapEntry> inputPortMap() {
		return this.inputPortMap;
	}

	/**
	 * Releases this object, making sure it can be garbage collected
	 * immediately. Should be called upon closing a project page.
	 */
	protected void release() {
		final ArrayList<T> copy = new ArrayList<>(this.processors);
		for (T p : copy) {
			removeProcessor(p);
		}
		repaintListeners.clear();
	}

	// processors might change ports later on (repaint), so we need to listen.
	// Removed ports are left in the map (technically they're just hidden/
	// deactivated anyways).
	protected final Map<Integer, InvalidationListener> repaintListeners = new HashMap<>();

	protected void addRepaintListener(T p) {
		if (p.serviceObject().hasRepaintProperty()) {
			final int weakId = p.id;
			final WeakReference<T> weakProcessor = new WeakReference<>(p);
			final InvalidationListener listener = (c) -> {
				final T wp = weakProcessor.get();
				if (wp == null) {
					repaintListeners.remove(weakId);
					return;
				}
				registerPorts(wp);
			};
			this.repaintListeners.put(p.id, listener);
			p.serviceObject().repaintProperty().addListener(listener);
		}
	}

	protected void removeRepaintListener(T p) {
		if (p.serviceObject().hasRepaintProperty()) {
			final InvalidationListener listener = this.repaintListeners.get(p.id);
			if (listener != null) {
				p.serviceObject().repaintProperty().removeListener(listener);
				this.repaintListeners.remove(p.id);
			}
		}
	}

	protected void registerPorts(T p) {
		for (Map.Entry<String, InputPort<?>> e : p.serviceObject().inputs().entrySet()) {
			this.inputPortMap.put(e.getValue(), new PortMapEntry(p.id, e.getKey()));
		}
		for (Map.Entry<String, OutputPort<?>> e : p.serviceObject().outputs().entrySet()) {
			this.outputPortMap.put(e.getValue(), new PortMapEntry(p.id, e.getKey()));
		}
	}

	protected void unregisterPorts(T p) {
		for (InputPort<?> port : p.serviceObject().inputs().values()) {
			this.inputPortMap.remove(port);
		}
		for (OutputPort<?> port : p.serviceObject().outputs().values()) {
			this.outputPortMap.remove(port);
		}
	}

	/**
	 * An observable list of all processors in the pipeline. This is supposed to
	 * be a readonly(!) observable list, so do not modify it directly. Use
	 * {@code addProcessor} and {@code removeProcessor} instead.
	 *
	 * @return an observable list of processors.
	 */
	public ObservableList<T> processors() {
		/*
		 * Not sure if multiple listeners listening to the same property are
		 * fired in order, but if so, we could use a ListChangeListener and
		 * do all the bookkeeping/updating of the portmaps there (as first ones
		 * to execute, s.t. following have an uptodate portmap) and users
		 * could just modify the processors list directly.
		 */
		return processors;
	}

	/**
	 * Returns a processor by id.
	 *
	 * @param id unique id of the processor.
	 * @return the processor (if found), or {@code null}.
	 */
	public T getProcessor(int id) {
		for (T wrapper : processors()) {
			if (wrapper.id == id) {
				return wrapper;
			}
		}

		return null;
	}

	/**
	 * Returns the next id for a new processor.
	 *
	 * @return a new processor id.
	 */
	protected final int newProcessorId() {
		maxProcessorId++;
		return maxProcessorId;
	}

	/**
	 * Updates {@code maxProcessorId}. This is done to assure new processor ids
	 * are unique.
	 *
	 * @param id the largest processor id in the current/loaded pipeline.
	 */
	protected final void levelMaxProcessorId(int id) {
		if (id > maxProcessorId) {
			maxProcessorId = id;
		}
	}

	/**
	 * Adds a processor to the pipeline.
	 *
	 * @param pid pid of the processor.
	 * @param version version of the processor.
	 * @param x initial x position of the processor view.
	 * @param y initial y position of the processor view.
	 * @return the added processor wrapper.
	 */
	public T addProcessor(String pid, String version, double x, double y) {
		return addProcessor(pid, version, x, y, null, false);
	}

	/**
	 * Adds a processor to the pipeline.
	 *
	 * @param pid pid of the processor.
	 * @param version version of the processor.
	 * @param x initial x position of the processor view.
	 * @param y initial y position of the processor view.
	 * @param parameters parameters to initialize the processor with.
	 * @param editing {@code true} to have to processor view open (editing
	 * parameters), {@code false} for a closed processor view.
	 * @return the added processor wrapper.
	 */
	public abstract T addProcessor(String pid, String version, double x, double y, Map<String, Object> parameters, boolean editing);

	protected final void addProcessor(T wrapper) {
		if (!wrapper.isAvailable()) {
			return;
		}
		addRepaintListener(wrapper);
		registerPorts(wrapper);
		this.modifiedPipelineProperty.addManagedProperty(wrapper);
		processors.add(wrapper);
		registerProcessor(wrapper);
	}

	/**
	 * Hook method called after a processor has been added to the pipeline. Make
	 * sure to not already populate the pipline by calling the super
	 * constructor, or this hook, if overwritten, won't get called.
	 *
	 * @param wrapper the processor.
	 */
	protected void registerProcessor(T wrapper) {

	}

	/**
	 * Removes a processor from the pipeline. All inputs and outputs are
	 * disconnected before removal.
	 *
	 * @param wrapper processor to be removed.
	 */
	public void removeProcessor(T wrapper) {
		removeProcessor(wrapper, true);
	}

	/**
	 * Removes a processor from the pipeline.
	 *
	 * @param wrapper processor to be removed.
	 * @param disconnect disconnets all inputs and outputs if {@code true}
	 * (default), does not so otherwise - which can be useful if we still need
	 * the connections and manually disconnect afterwards (e.g. in the pipeline
	 * editor).
	 */
	public final void removeProcessor(T wrapper, boolean disconnect) {
		if (disconnect && wrapper.isAvailable()) {
			wrapper.serviceObject().disconnect();
		}

		this.modifiedPipelineProperty.removeManagedProperty(wrapper);
		removeRepaintListener(wrapper);
		unregisterPorts(wrapper);
		processors.remove(wrapper);
		unregisterProcessor(wrapper);
	}

	/**
	 * Hook method called after a processor has been removed from the pipeline.
	 *
	 * @param wrapper the processor.
	 */
	protected void unregisterProcessor(T wrapper) {

	}

	@Override
	public ModifiedProperty modifiedProperty() {
		return modifiedPipelineProperty;
	}

	/**
	 * Clones/copies the pipeline. This returns a deep-copy of the pipeline.
	 *
	 * @return a clone of the pipeline.
	 */
	public abstract Pipeline<T> clonePipeline();

	/**
	 * Clones/copies the pipeline as a {@code RunnablePipeline}. This returns a
	 * deep-copy of the pipeline.
	 *
	 * @param page the project page.
	 * @return a (runnable) clone of the pipeline.
	 */
	public RunnablePipeline cloneAsRunnablePipeline(ProjectPage page) {
		final PipelineData.Pipeline data = new PipelineData.Pipeline(this);
		return new RunnablePipeline(handler, page, data);
	}

	/**
	 * Clones/copies the pipeline as a {@code RunnablePipeline} and applies a
	 * patch. This returns a deep-copy of the pipeline.
	 *
	 * @param page the project page.
	 * @param patch the patch to be applied to the cloned pipeline.
	 * @return a (runnable) cloned and patched pipeline.
	 */
	public RunnablePipeline cloneAsRunnablePipeline(ProjectPage page, PipelinePatch patch) {
		final PipelineData.Pipeline data = new PipelineData.Pipeline(this);
		data.patch(patch);
		return new RunnablePipeline(handler, page, data);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ "id=" + id
				+ ", name=" + name.get()
				+ ", processors(" + processors.size() + ")=" + processors
				+ "}";
	}

	/**
	 * Stages of a pipeline. The processors of a pipeline can be naturally
	 * segmented into subsequent stages, s.t. all dependencies (or inputs) of a
	 * processor are made available at an earlier stage. Thus all processors in
	 * a given stage can be processed, given all preceding stages are done.
	 *
	 * @param <T> class that extends from {@code PrototypeProcessor}.
	 */
	public static class PipelineStages<T extends PrototypeProcessor> implements Iterable<Stage<T>> {

		/**
		 * The stages.
		 */
		public final List<Stage<T>> stages = new ArrayList<>();

		/**
		 * Creates new pipeline stages.
		 *
		 * @param pipeline the pipeline.
		 */
		public PipelineStages(Pipeline<T> pipeline) {
			this(pipeline, getPortMap(pipeline));
		}

		/**
		 * Creates new pipeline stages.
		 *
		 * @param pipeline the pipeline.
		 * @param portMap the port map of the pipeline.
		 */
		public PipelineStages(Pipeline<T> pipeline, Map<OutputPort<?>, PrototypeProcessor.PortMapEntry> portMap) {
			buildStages(pipeline, portMap);
		}

		private static Map<OutputPort<?>, PrototypeProcessor.PortMapEntry> getPortMap(Pipeline<? extends PrototypeProcessor> pipeline) {
			return PrototypeProcessor.getOutputPortMap(pipeline.processors());
		}

		// The algorithm described in the thesis, and that has been written on paper(!)
		// way after the following has been implemented (mea culpa!), is propably
		// more clear and even might be more efficient than this mess here.
		// Then again, I might have had my reasons to do it this way -- guess I just
		// had a different idea in mind, and focused on doing one stage after the other
		// -- so better double-check first before you get rid of it...
		private void buildStages(Pipeline<T> pipeline, Map<OutputPort<?>, PrototypeProcessor.PortMapEntry> portMap) {
			if (portMap == null) {
				portMap = getPortMap(pipeline);
			}

			final List<T> todo = new ArrayList<>(pipeline.processors());
			final List<Integer> ready = new ArrayList<>();
			int number = 1;

			while (!todo.isEmpty()) {
				final Stage<T> stage = new Stage<>(number);
				final List<Integer> readyNext = new ArrayList<>();

				for (Iterator<T> it = todo.iterator(); it.hasNext();) {
					final T wrapper = it.next();

					// check if all inputs are ready yet
					boolean isReady = true;

					if (wrapper.isAvailable()) {
						for (InputPort<?> input : wrapper.serviceObject().inputs().values()) {
							if (input.isConnected()) {
								final OutputPort<?> output = input.connection();
								final PrototypeProcessor.PortMapEntry source = portMap.get(output);
								if (!ready.contains(source.id)) {
									isReady = false;
									break;
								}
							}
						}
					}

					if (isReady) {
						readyNext.add(wrapper.id);
						stage.addProcessor(wrapper);
						it.remove();
					}
				}

				if (readyNext.isEmpty()) {
					log.warn("failed to build stages, invalid pipeline: {}", this);
					return;
				}

				ready.addAll(readyNext);
				pipeline.getLayoutStrategy().sort(stage.processors);
				this.stages.add(stage);
				number++;
			}
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(this.getClass().getSimpleName());
			sb.append("{");
			int n = stages.size();
			int i = 1;
			for (Stage<T> stage : stages) {
				sb.append(String.format("stage %d: [", i));
				int m = stage.processors.size();
				if (m == 0) {
					sb.append("-");
				} else {
					int j = 1;
					for (T obj : stage.processors) {
						sb.append(String.format("%s (%d)", obj.processor.name(), obj.id));
						if (j < m) {
							sb.append(",");
						}
						j++;
					}
				}
				if (i < n) {
					sb.append("], ");
				}
				i++;
			}
			sb.append("}");
			return sb.toString();
		}

		/**
		 * Returns a stage of the pipeline.
		 *
		 * @param index index of the stage.
		 * @return the stage.
		 */
		public Stage<T> get(int index) {
			return this.stages.get(index);
		}

		/**
		 * Returns the number of stages.
		 *
		 * @return the number of stages.
		 */
		public int size() {
			return this.stages.size();
		}

		@Override
		public Iterator<Stage<T>> iterator() {
			return this.stages.listIterator();
		}
	}

	/**
	 * A pipeline stage.
	 *
	 * @param <T> type of the processor wrapper.
	 */
	public static class Stage<T extends PrototypeProcessor> implements Localizable {

		public final int number;
		public final List<T> processors = new ArrayList<>();

		/**
		 * Creates a new pipeline stage.
		 *
		 * @param number number of the stage (starts with 1, unlike its index).
		 */
		public Stage(int number) {
			this.number = number;
		}

		/**
		 * Adds a processor to the stage.
		 *
		 * @param processor a processor.
		 */
		protected void addProcessor(T processor) {
			this.processors.add(processor);
		}

		/**
		 * Returns a formatted title of the stage.
		 *
		 * @return the title of the stage.
		 */
		public String title() {
			return String.format("%s %d", localize("pipeline.stage"), number);
		}

		/**
		 * Returns the implied state of the stage. The state of a stage is
		 * determined by the state of the processor that is processed/ready the
		 * least.
		 *
		 * @return the state of the stage.
		 */
		public Processor.State state() {
			int max = -1;
			for (PrototypeProcessor w : this.processors) {
				final int weight = w.getState().weight;
				if (weight > max) {
					max = weight;
				}
			}
			return Processor.State.getState(max);
		}
	}

}
