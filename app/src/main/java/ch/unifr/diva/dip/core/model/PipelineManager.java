package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.utils.Modifiable;
import ch.unifr.diva.dip.utils.ModifiedProperty;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategy.Answer;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.core.services.api.HostService;
import ch.unifr.diva.dip.osgi.ServiceCollection;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PipelineManager manages the pipelines of a project.
 */
public class PipelineManager implements Modifiable, Localizable {

	private static final Logger log = LoggerFactory.getLogger(PipelineManager.class);

	private final ApplicationHandler handler;
	private int maxPipelineId = 0;
	private final ObservableList<Pipeline> pipelines;
	private final IntegerProperty defaultPipelineIdProperty = new SimpleIntegerProperty();
	private final ModifiedProperty modifiedPipelinesProperty;

	/**
	 * Creates a new pipeline manager.
	 *
	 * @param handler the application handler.
	 * @param pds list of pipeline specifications.
	 * @param defaultPipelineId id of the default pipeline.
	 */
	public PipelineManager(ApplicationHandler handler, List<PipelineData.Pipeline> pds, int defaultPipelineId) {
		this.handler = handler;
		this.pipelines = FXCollections.observableArrayList();
		this.defaultPipelineIdProperty.set(defaultPipelineId);
		this.modifiedPipelinesProperty = new ModifiedProperty();

		if (pds != null) {
			for (PipelineData.Pipeline pd : pds) {
				addPipeline(new Pipeline(handler, pd));
				levelMaxPipelineId(pd.id);
			}
		}

		this.modifiedPipelinesProperty.addObservedProperty(pipelines);
		this.modifiedPipelinesProperty.addObservedProperty(defaultPipelineIdProperty);

		// listen to the service monitor, and update created/running proc. instances
		// as proc./services are removed and re-added (OSGi is rather dynamic, remember?).
		// This is no killer-feature we absolutely need to have, still nice to have
		// in order to update services devs are working on, without having to shutdown
		// and start up DIP all over again...
		this.handler.osgi.getProcessors().getServiceCollectionList().addListener(processorListener);
	}

	private final ListChangeListener<? super ServiceCollection<Processor>> processorListener = (ListChangeListener.Change<? extends ServiceCollection<Processor>> c) -> {
		while (c.next()) {
			if (c.wasReplaced()) {
				for (ServiceCollection<Processor> collection : c.getRemoved()) {
					forAllPipelines(pipelines(), (wrappers, wrapper) -> {
						if (wrapper.pid().equals(collection.pid())) {
							wrapper.updateProcessor();
							ProcessorWrapperRunnable.notify(wrappers, wrapper);
						}
					});
				}
			} else if (c.wasRemoved()) {
				// nothing to do here. Removed services (or just some version) will
				// be removed from the processor list (pipeline editor) automatically,
				// and already created/instantiated processors are not a problem,
				// and will be replaced/updated should the removed version show
				// up again.
			} else if (c.wasAdded()) {
				for (ServiceCollection<Processor> collection : c.getAddedSubList()) {
					forAllPipelines(pipelines(), (wrappers, wrapper) -> {
						if (wrapper.pid().equals(collection.pid())) {
							wrapper.updateProcessor();
							ProcessorWrapperRunnable.notify(wrappers, wrapper);
						}
					});
				}
			}
		}
	};

	private int newPipelineId() {
		this.maxPipelineId++;
		return this.maxPipelineId;
	}

	private void levelMaxPipelineId(int id) {
		if (id > this.maxPipelineId) {
			this.maxPipelineId = id;
		}
	}

	@Override
	public ModifiedProperty modifiedProperty() {
		return modifiedPipelinesProperty;
	}

	/**
	 * Returns all pipelines managed by this pipeline manager.
	 *
	 * @return an observable list of all pipelines.
	 */
	public ObservableList<Pipeline> pipelines() {
		return pipelines;
	}

	/**
	 * Returns a pipeline by its id.
	 *
	 * @param id id of the pipeline.
	 * @return the pipeline with given id, or null if no such pipeline is found.
	 */
	public Pipeline getPipeline(int id) {
		for (Pipeline p : pipelines) {
			if (p.id == id) {
				return p;
			}
		}
		return null;
	}

	/**
	 * Checks whether a pipeline with the given id exists.
	 *
	 * @param id a pipeline id.
	 * @return True if there is a pipeline with given id, False otherwise.
	 */
	public boolean pipelineExists(int id) {
		final Pipeline p = getPipeline(id);
		return (p != null);
	}

	/**
	 * Returns the pipeline id of the default pipeline.
	 *
	 * @return pipeline id of the default pipeline.
	 */
	public int getDefaultPipelineId() {
		return this.defaultPipelineIdProperty().get();
	}

	/**
	 * Sets the default pipeline.
	 *
	 * @param id pipeline id of the new default pipeline.
	 */
	public void setDefaultPipelineId(int id) {
		this.defaultPipelineIdProperty().set(id);
	}

	/**
	 * Pipeline id property.
	 *
	 * @return pipeline id property.
	 */
	public IntegerProperty defaultPipelineIdProperty() {
		return this.defaultPipelineIdProperty;
	}

	/**
	 * Creates a new pipeline.
	 *
	 * @param name name of the new pipeline.
	 * @return the new pipeline.
	 */
	public Pipeline createPipeline(String name) {
		int id = newPipelineId();
		final Pipeline pipeline = new Pipeline(handler, id, name);
		addPipeline(pipeline);
		return pipeline;
	}

	private void addAllPipelines(List<Pipeline> pipelines) {
		for (Pipeline pipeline : pipelines) {
			addPipeline(pipeline);
		}
	}

	private void addPipeline(Pipeline pipeline) {
		this.modifiedPipelinesProperty.addManagedProperty(pipeline);
		this.pipelines.add(pipeline);
		if (this.pipelines.size() == 1) {
			this.setDefaultPipelineId(pipeline.id);
		}
	}

	/**
	 * Removes a pipeline from the pipeline manager.
	 *
	 * @param id pipeline id of the pipeline which is to be removed.
	 * @return True if the pipeline was deleted, False otherwise.
	 */
	public boolean deletePipeline(int id) {
		return deletePipeline(getPipeline(id));
	}

	/**
	 * Removes a pipeline from the pipeline manager.
	 *
	 * @param pipeline pipeline to be removed.
	 * @return True if the pipeline was deleted, False otherwise.
	 */
	public boolean deletePipeline(Pipeline pipeline) {
		final List<Pipeline> list = Arrays.asList(pipeline);
		return deletePipelines(list);
	}

	/**
	 * Removes a list of pipelines from the pipeline manager. User confirmation
	 * will be prompted for.
	 *
	 * @param selection list of pipelines to be removed.
	 * @return True if the pipelines were deleted, False otherwise.
	 */
	public boolean deletePipelines(List<Pipeline> selection) {
		return deletePipelines(selection, true);
	}

	/**
	 * Removes a list of pipelines form the pipeline manager.
	 *
	 * @param selection list of pipelines to be removed.
	 * @param confirm whether to prompt for user confirmation first.
	 * @return True if the pipelines were deleted, False otherwise.
	 */
	public boolean deletePipelines(List<Pipeline> selection, boolean confirm) {
		if (confirm) {
			final String msg = formatDeletePipelineMessage(selection);
			final Answer answer = handler.uiStrategy.getAnswer(msg);
			switch (answer) {
				case YES:
					break;
				case NO:
				case CANCEL:
					return false;
			}
		}

		for (Pipeline pipeline : selection) {
			if (!pipelines.contains(pipeline)) {
				continue;
			}
			log.info("deleting pipeline: {}", pipeline);
			this.modifiedPipelinesProperty.removeManagedProperty(pipeline);
			this.pipelines.remove(pipeline);
			if (this.pipelines.size() == 1) {
				this.setDefaultPipelineId(pipelines.get(0).id);
			}
		}

		return true;
	}

	/**
	 * Returns the delete pipelines confirmation message.
	 *
	 * @param selection the selection of pipelines to be deleted.
	 * @return the delete pipelines confirmation message.
	 */
	static public String formatDeletePipelineMessage(List<Pipeline> selection) {
		final List<String> names = new ArrayList<>();
		for (Pipeline pipeline : selection) {
			names.add(pipeline.getName());
		}
		return L10n.getInstance().getString(
				"delete.confirm", String.join(", ", names)
		);
	}

	/**
	 * Imports the given pipeline items as new pipelines into the current
	 * project.
	 *
	 * @param items the pipeline items.
	 */
	public void importPipelines(List<PipelineData.PipelineItem> items) {
		for (PipelineData.PipelineItem item : items) {
			final PipelineData.Pipeline pipeline = item.toPipelineData();
			pipeline.id = newPipelineId();
			addPipeline(new Pipeline(handler, pipeline));
		}
	}

	/**
	 * Reads pipelines from a pipeline file.
	 *
	 * <p>
	 * Note that this method operates on serialized {@code PipelineData} (stored
	 * as such only internally), and not on {@code DipData}.
	 *
	 * @param handler application handler.
	 * @param file the pipeline file to read from.
	 * @param startId first new/free pipeline id to assign imported pipelines
	 * to.
	 * @return a list of pipelines.
	 * @throws JAXBException
	 */
	public static List<Pipeline> importPipelines(ApplicationHandler handler, Path file, int startId) throws JAXBException {
		final PipelineData data = PipelineData.load(file);
		final List<Pipeline> pipelines = new ArrayList<>();
		for (PipelineData.Pipeline pipeline : data.list) {
			if (startId > -1) {
				pipeline.id = startId++;
			}
			pipelines.add(new Pipeline(handler, pipeline));
		}
		return pipelines;
	}

	/**
	 * Writes all pipelines to an output stream.
	 *
	 * <p>
	 * Note that this method operates on serialized {@code PipelineData} (stored
	 * as such only internally), and not on {@code DipData}.
	 *
	 * @param stream output stream to write to.
	 * @throws JAXBException
	 */
	public void exportPipelines(OutputStream stream) throws JAXBException {
		exportPipelines(this.pipelines(), stream);
	}

	/**
	 * Writes a list of pipelines to an output stream.
	 *
	 * <p>
	 * Note that this method operates on serialized {@code PipelineData} (stored
	 * as such only internally), and not on {@code DipData}.
	 *
	 * @param pipelines list of pipelines.
	 * @param stream output stream to write to.
	 * @throws JAXBException
	 */
	public static void exportPipelines(List<Pipeline> pipelines, OutputStream stream) throws JAXBException {
		final PipelineData data = new PipelineData(pipelines);
		XmlUtils.marshal(data, stream);
	}

	/**
	 * Writes all pipelines to a pipeline file.
	 *
	 * <p>
	 * Note that this method operates on serialized {@code PipelineData} (stored
	 * as such only internally), and not on {@code DipData}.
	 *
	 * @param file pipeline file to write to.
	 * @throws JAXBException
	 */
	public void exportPipelines(Path file) throws JAXBException {
		exportPipelines(this.pipelines(), file);
	}

	/**
	 * Writes a list of pipelines to a pipeline file.
	 *
	 * <p>
	 * Note that this method operates on serialized {@code PipelineData} (stored
	 * as such only internally), and not on {@code DipData}.
	 *
	 * @param pipelines list of pipelines.
	 * @param file pipeline file to write to.
	 * @throws JAXBException
	 */
	public static void exportPipelines(List<Pipeline> pipelines, Path file) throws JAXBException {
		final PipelineData data = new PipelineData(pipelines);
		XmlUtils.marshal(data, file);
	}

	/**
	 * Applies code for all processors in the given pipelines.
	 *
	 * @param pipelines list of pipelines.
	 * @param x code to apply to the given pipelines.
	 */
	public static void forAllPipelines(List<Pipeline> pipelines, ProcessorWrapperRunnable x) {
		for (Pipeline pipeline : pipelines) {
			forAllProcessors(pipeline.processors(), x);
		}
	}

	/**
	 * Applies code for all processor wrappers in the given list.
	 *
	 * @param wrappers list of processor wrappers.
	 * @param x code to apply to the given processor wrappers.
	 */
	public static void forAllProcessors(List<ProcessorWrapper> wrappers, ProcessorWrapperRunnable x) {
		for (ProcessorWrapper wrapper : wrappers) {
			x.run(wrappers, wrapper);
		}
	}

	/**
	 * ProcessorWrapper runnable.
	 */
	public interface ProcessorWrapperRunnable {

		/**
		 * Executes code on a processor.
		 *
		 * @param wrappers the list of processors (including {@code wrapper}.
		 * @param wrapper the processor.
		 */
		public void run(List<ProcessorWrapper> wrappers, ProcessorWrapper wrapper);

		/**
		 * Updates the list of processors by resetting the object. Used on
		 * observable lists when an object changed internally, and the list
		 * should fire a changed event.
		 *
		 * @param wrappers the list of processors (including {@code wrapper}.
		 * @param wrapper the processor.
		 */
		public static void notify(List<ProcessorWrapper> wrappers, ProcessorWrapper wrapper) {
			final int index = wrappers.indexOf(wrapper);
			wrappers.set(index, wrapper);
		}
	}

	/**
	 * Creates an empty pipeline.
	 *
	 * @param handler application handler.
	 * @return a new, empty pipeline.
	 */
	public static Pipeline emptyPipeline(ApplicationHandler handler) {
		final Pipeline pipeline = new Pipeline(
				handler,
				0,
				L10n.getInstance().getString("pipeline.new")
		);
		pipeline.addProcessor(HostService.DEFAULT_GENERATOR, HostService.VERSION.toString(), 20, 20);
		return pipeline;
	}

	/**
	 * Counts the number of times a pipeline is in use by a page.
	 *
	 * @param pages pages to consider.
	 * @return a map of pipeline ids (keys) pointing to their usage/count.
	 */
	public static Map<Integer, Integer> pipelineUsage(List<ProjectPage> pages) {
		final Map<Integer, Integer> usage = new HashMap<>();
		for (ProjectPage page : pages) {
			final int id = page.getPipelineId();
			if (usage.containsKey(id)) {
				usage.put(id, usage.get(id) + 1);
			} else {
				usage.put(id, 1);
			}
		}
		return usage;
	}

	/**
	 * Returns a new combo box with all pipelines managed by the pipeline
	 * manager.
	 *
	 * @return a combo box with all pipelines.
	 */
	public ComboBox<PipelineItem> getComboBox() {
		return getComboBox(pipelines());
	}

	/**
	 * Returns a new combo box with the given pipelines.
	 *
	 * @param pipelines pipelines to populate the combo box with.
	 * @return a combo box with the given pipelines.
	 */
	public static ComboBox<PipelineItem> getComboBox(List<Pipeline> pipelines) {
		final ComboBox<PipelineItem> box = new ComboBox();
		box.setCellFactory((ListView<PipelineItem> p) -> new SimplePipelineCell());
		box.setButtonCell(new SimplePipelineCell());

		box.getItems().add(new PipelineItem(-1, ""));
		for (Pipeline pipeline : pipelines) {
			box.getItems().add(new PipelineItem(pipeline));
		}

		return box;
	}

	/**
	 * A simple pipeline list cell.
	 */
	public static class SimplePipelineCell extends ListCell<PipelineItem> {

		private PipelineItem currentItem;

		@Override
		protected void updateItem(PipelineItem item, boolean empty) {
			super.updateItem(item, empty);

			setText(null);
			setGraphic(null);

			this.disableProperty().unbind();

			if (!empty) {
				this.disableProperty().bind(item.disabledProperty());
				setText(
						item.label.isEmpty()
								? PipelineItem.emptyPipeline
								: item.label
				);
			}
		}
	}

	/**
	 * Dummy pipeline object to populate comboboxes and the like. These dummies
	 * are needed since we also need to represent the "no pipeline" somehow.
	 */
	public static class PipelineItem {

		/**
		 * Name of the empty pipeline.
		 */
		public static String emptyPipeline = L10n.getInstance().getString("none").toLowerCase();

		/**
		 * The pipeline id.
		 */
		public final int id;

		/**
		 * The name/label of the pipeline.
		 */
		public final String label;

		private final BooleanProperty disabledProperty;

		public PipelineItem(Pipeline pipeline) {
			this(pipeline.id, pipeline.getName());
		}

		public PipelineItem(int id, String label) {
			this.id = id;
			this.label = (id > -1) ? label : "";
			this.disabledProperty = new SimpleBooleanProperty(false);
		}

		public BooleanProperty disabledProperty() {
			return this.disabledProperty;
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final PipelineItem other = (PipelineItem) obj;
			return this.id == other.id;
		}
	}

}
