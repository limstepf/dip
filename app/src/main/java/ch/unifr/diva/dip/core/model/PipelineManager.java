package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.utils.Modifiable;
import ch.unifr.diva.dip.utils.ModifiedProperty;
import ch.unifr.diva.dip.core.services.PageGenerator;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategy.Answer;
import ch.unifr.diva.dip.osgi.ServiceMonitor.Service;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
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
	/**
	 * PID of the PageGenerator.
	 */
	public static final String GENERATION_PROCESSOR = PageGenerator.class.getCanonicalName();

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

		registerServiceListener();
	}

	private void registerServiceListener() {
		this.handler.osgi.services.services().addListener(processorListener);
	}

	private final ListChangeListener<? super Service<? extends Processor>> processorListener = (ListChangeListener.Change<? extends Service<? extends Processor>> c) -> {
		while (c.next()) {
			if (c.wasReplaced()) {
				for (Service<? extends Processor> s : c.getRemoved()) {
					forAllPipelines(pipelines(), (wrappers, wrapper) -> {
						if (wrapper.pid().equals(s.pid)) {
							wrapper.updateProcessor(true);
							ProcessorWrapperRunnable.notify(wrappers, wrapper);
						}
					});
				}
			} else if (c.wasRemoved()) {
				for (Service<? extends Processor> s : c.getRemoved()) {
					forAllPipelines(pipelines(), (wrappers, wrapper) -> {
						if (wrapper.pid().equals(s.pid)) {
							wrapper.deprecateProcessor();
							// no need to notify (availableProperty is used/listened to)
						}
					});
				}
			} else if (c.wasAdded()) {
				for (Service<? extends Processor> s : c.getAddedSubList()) {
					forAllPipelines(pipelines(), (wrappers, wrapper) -> {
						if (wrapper.pid().equals(s.pid)) {
							wrapper.updateProcessor(true);
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
	 */
	public void deletePipeline(int id) {
		deletePipeline(getPipeline(id));
	}

	/**
	 * Removes a pipeline from the pipeline manager.
	 *
	 * @param pipeline pipeline to be removed.
	 */
	public void deletePipeline(Pipeline pipeline) {
		final List<Pipeline> list = Arrays.asList(pipeline);
		deletePipelines(list);
	}

	/**
	 * Removes a list of pipelines from the pipeline manager. User confirmation
	 * will be prompted for.
	 *
	 * @param selection list of pipelines to be removed.
	 */
	public void deletePipelines(List<Pipeline> selection) {
		deletePipelines(selection, true);
	}

	/**
	 * Removes a list of pipelines form the pipeline manager.
	 *
	 * @param selection list of pipelines to be removed.
	 * @param confirm whether to prompt for user confirmation first.
	 */
	public void deletePipelines(List<Pipeline> selection, boolean confirm) {
		if (confirm) {
			final List<String> names = new ArrayList<>();
			for (Pipeline pipeline : selection) {
				names.add(pipeline.getName());
			}
			final String msg = localize("delete.confirm", String.join(", ", names));
			final Answer answer = handler.uiStrategy.getAnswer(msg);
			switch (answer) {
				case YES:
					break;
				case NO:
				case CANCEL:
					return;
			}
		}

		for (Pipeline pipeline : selection) {
			if (!pipelines.contains(pipeline)) {
				continue;
			}
			// TODO: what about pages that used a deleted pipeline?
			// TODO: do we need to refresh/layout the pages widget? -> event bus?
			log.info("deleting pipeline: {}", pipeline);
			this.modifiedPipelinesProperty.removeManagedProperty(pipeline);
			this.pipelines.remove(pipeline);
			if (this.pipelines.size() == 1) {
				this.setDefaultPipelineId(pipelines.get(0).id);
			}
		}
	}

	/**
	 * Imports pipelines from a file.
	 *
	 * @param file pipeline file to read from.
	 * @param indices null to import all/any pipelines, or a list of indices of
	 * pipelines to import only.
	 * @throws JAXBException
	 */
	public void importPipelines(Path file, List<Integer> indices) throws JAXBException {
		final List<Pipeline> imported = importPipelines(handler, file, newPipelineId());

		if (indices == null) {
			addAllPipelines(imported);
			for (Pipeline pipeline : imported) {
				levelMaxPipelineId(pipeline.id);
			}
		} else {
			for (Integer i : indices) {
				final Pipeline pipeline = imported.get(i);
				addPipeline(pipeline);
				levelMaxPipelineId(pipeline.id);
			}
		}
	}

	/**
	 * Reads pipelines from a pipeline file.
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
	 * @param stream output stream to write to.
	 * @throws JAXBException
	 */
	public void exportPipelines(OutputStream stream) throws JAXBException {
		exportPipelines(this.pipelines(), stream);
	}

	/**
	 * Writes a list of pipelines to an output stream.
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
	 * @param file pipeline file to write to.
	 * @throws JAXBException
	 */
	public void exportPipelines(Path file) throws JAXBException {
		exportPipelines(this.pipelines(), file);
	}

	/**
	 * Writes a list of pipelines to a pipeline file.
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

		public void run(List<ProcessorWrapper> wrappers, ProcessorWrapper wrapper);

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
		pipeline.addProcessor(GENERATION_PROCESSOR, 20, 20);
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

		box.getItems().add(new PipelineItem(-1, " "));
		for (Pipeline pipeline : pipelines) {
			box.getItems().add(new PipelineItem(pipeline));
		}

		return box;
	}

	/**
	 * A simple pipeline list cell.
	 */
	public static class SimplePipelineCell extends ListCell<PipelineItem> {

		@Override
		protected void updateItem(PipelineItem item, boolean empty) {
			super.updateItem(item, empty);

			setText(null);
			setGraphic(null);

			if (!empty) {
				setText(item.label);
			}
		}
	}

	/**
	 * Dummy pipeline object to populate comboboxes and the like. These dummies
	 * are needed since we also need to represent the "no pipeline" somehow.
	 */
	public static class PipelineItem {

		public final int id;
		public final String name;
		public final String label;

		public PipelineItem(Pipeline pipeline) {
			this(pipeline.id, pipeline.getName());
		}

		public PipelineItem(int id, String name) {
			this.id = id;
			this.name = name;
			this.label = (id > -1) ? name : " ";
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
