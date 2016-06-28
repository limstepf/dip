package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.services.Processable;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.Resetable;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.core.services.api.HostProcessorContext;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.eventbus.events.StatusMessageEvent;
import ch.unifr.diva.dip.gui.editor.LayerExtension;
import ch.unifr.diva.dip.gui.editor.LayerGroup;
import ch.unifr.diva.dip.gui.layout.Lane;
import ch.unifr.diva.dip.utils.BackgroundTask;
import ch.unifr.diva.dip.utils.FileFinder;
import ch.unifr.diva.dip.utils.FxUtils;
import ch.unifr.diva.dip.utils.IOUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javax.xml.bind.JAXBException;

/**
 * A runnable processor is a {@code ProcessorWrapper} that can be run/executed.
 */
public class RunnableProcessor extends ProcessorWrapper {

	private final Project project;
	private final ProjectPage page;
	private final RunnablePipeline pipeline;
	private final String PROCESSOR_DATA_DIR;
	private final String PROCESSOR_DATA_XML;
	private final ObjectMapData objectMap;
	private final LayerGroup layer;

	// needs to be updated manual after each interaction with the processor
	private final ObjectProperty<Processor.State> stateProperty;


	public RunnableProcessor(PipelineData.Processor processor, RunnablePipeline pipeline) {
		super(
				processor,
				pipeline.handler
		);
		this.pipeline = pipeline;
		this.page = pipeline.page;
		this.project = page.project();
		this.stateProperty = new SimpleObjectProperty();

		this.PROCESSOR_DATA_DIR = String.format(
				ProjectPage.PROCESSOR_DATA_DIR_FORMAT,
				page.id,
				processor.id
		);
		this.PROCESSOR_DATA_XML = String.format(
				ProjectPage.PROCESSOR_DATA_XML_FORMAT,
				page.id,
				processor.id
		);

		this.objectMap = initObjectMap();
		this.layer = new LayerGroup();
	}

	@Override
	public void init() {
		super.init();
		this.updateState();
	}

	@Override
	protected void initProcessor(Processor processor) {
		super.initProcessor(processor);

		// we overide initProcessor() rather than doing this in init() since a
		// transmutable processor might have changed its name once being fully
		// initialized
		this.layer.setName(this.processor().name());
		this.layer.setHideGroupMode(LayerGroup.HideGroupMode.AUTO);
		this.layer.layerExtensions().add(new ProcessorLayerExtension(this));
		this.updateState();
	}

	public static class ProcessorLayerExtension implements LayerExtension, Localizable {

		final RunnableProcessor runnable;
		final VBox vbox = new VBox();
		final Label status = new Label();
		final Lane lane = new Lane();
		final Button processButton = new Button();
		final Button resetButton = new Button();

		public ProcessorLayerExtension(RunnableProcessor runnable) {
			this.runnable = runnable;

			status.getStyleClass().add("dip-small");
			status.setText(runnable.getState().label);
			this.runnable.stateProperty().addListener((e) -> {
				status.setText(runnable.getState().label);

				processButton.setDisable(!runnable.getState().equals(Processor.State.PROCESSING));
				resetButton.setDisable(runnable.getState().equals(Processor.State.UNAVAILABLE));
			});
			if (runnable.processor().canProcess()) {
				processButton.setText(localize("process"));
				processButton.getStyleClass().add("dip-small");
				processButton.setOnAction((e) -> {
					this.runnable.processBackgroundTask();
				});
				lane.add(processButton);
			}
			if (runnable.processor().canReset()) {
				resetButton.setText(localize("reset"));
				resetButton.getStyleClass().add("dip-small");
				resetButton.setOnAction((e) -> {
					this.runnable.resetBackgroundTask();
				});
				lane.add(resetButton);
			}
			lane.setAlignment(Pos.CENTER_LEFT);
			lane.setPadding(new Insets(4, 4, 4, 4));
			vbox.getChildren().addAll(status, lane);
		}

		@Override
		public Node getComponent() {
			return vbox;
		}
	}

	public LayerGroup layer() {
		return this.layer;
	}

	private ObjectMapData initObjectMap() {
		if (Files.exists(processorDataXML())) {
			try (InputStream stream = new BufferedInputStream(Files.newInputStream(processorDataXML()))) {
				final ObjectMapData data = XmlUtils.unmarshal(ObjectMapData.class, stream);
				return data;
			} catch (JAXBException | IOException ex) {
				log.error("failed to load the processor's data map: {}", this, ex);
				handler.uiStrategy.showError(ex);
			}
		}

		return new ObjectMapData();
	}

	/**
	 * Saves the state of this {@code RunnableProcessor}.
	 */
	public void save() {
		if (!Files.exists(processorDataPath())) {
			return; // there is no pipeline anymore
		}

		saveObjectMap();
	}

	private void saveObjectMap() {
		try {
			Files.deleteIfExists(processorDataXML());
		} catch (IOException ex) {
			log.error("failed to clear the processor's data map: {}", this, ex);
			handler.uiStrategy.showError(ex);
			return;
		}

		try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(processorDataXML()))) {
			XmlUtils.marshal(this.objectMap, stream);
		} catch (JAXBException | IOException ex) {
			log.error("failed to save the processor's data map: {}", this, ex);
			handler.uiStrategy.showError(ex);
		}
	}

	/**
	 * Returns a path to the processor's dedicated data directory.
	 *
	 * @return a path to the processor's data directory.
	 */
	private Path processorDataDirectory() {
		final Path path = processorDataPath();

		try {
			final Path directory = IOUtils.getRealDirectories(path);
			return directory;
		} catch (IOException ex) {
			log.error("failed to retrieve the data directory of the processor: {} in {}", this, path, ex);
			handler.uiStrategy.showError(ex);
		}

		return null;
	}

	private Path processorDataPath() {
		return project.zipFileSystem().getPath(PROCESSOR_DATA_DIR);
	}

	/**
	 * Returns a path to the processor's data XML file. This file stores a
	 * {@code Map<String, Object>}.
	 *
	 * @return a path to the processor's data XML file.
	 */
	private Path processorDataXML() {
		return project.zipFileSystem().getPath(PROCESSOR_DATA_XML);
	}

	/**
	 * Returns a read-only stateProperty of the processor.
	 *
	 * @return a read-only stateProperty.
	 */
	public ReadOnlyObjectProperty<Processor.State> stateProperty() {
		return this.stateProperty;
	}

	/**
	 * Returns the state of the processor.
	 *
	 * @return the state of the processor.
	 */
	public Processor.State getState() {
		updateState();
		return this.stateProperty.get();
	}

	/**
	 * Updates the state of the processor.
	 */
	protected void updateState() {
		updateState(false);
	}

	/**
	 * Updates the state of the processor.
	 *
	 * @param updateDependentProcessors Also updates the state of (directly)
	 * dependend processors (i.e. processors connected to some output of this
	 * processor) if set to True, does not otherwise.
	 */
	protected void updateState(boolean updateDependentProcessors) {
		if (this.processor() == null) {
			this.stateProperty.set(Processor.State.UNAVAILABLE);
			return;
		}

		this.stateProperty.set(this.processor().state());
		if (updateDependentProcessors) {
			final Map<InputPort, PortMapEntry> inputPortMap = this.pipeline.inputPortMap();

			for (Map.Entry<String, Set<InputPort>> e : this.processor().dependentInputs().entrySet()) {
				final Set<InputPort> inputs = e.getValue();
				for (InputPort input : inputs) {
					final PortMapEntry m = inputPortMap.get(input);
					if (m != null) {
						final RunnableProcessor runnable = this.pipeline.getProcessor(m.id);
						if (runnable != null) {
							runnable.updateState();
						}
					}
				}
			}
		}
	}

	@Override
	protected HostProcessorContext newHostProcessorContext() {
		return new HostProcessorContext(this.page, this.layer);
	}

	@Override
	protected ProcessorContext newProcessorContext() {
		return new ProcessorContext(processorDataDirectory(), objectMap.objects, this.layer);
	}

	public void processBackgroundTask() {
		final RunnableProcessor runnable = this;
		BackgroundTask<Void> task = new BackgroundTask<Void>(handler) {

			@Override
			protected Void call() throws Exception {
				updateTitle(localize("processing"));
				updateMessage(localize("processing.object", runnable.processor().name()));
				updateProgress(-1, Double.NaN);

				runnable.process();
				return null;
			}

			@Override
			protected void finished(BackgroundTask.Result result) {
				runLater(() -> {
					runnable.updateState(true);
					handler.eventBus.post(new StatusMessageEvent(
							localize("processing.object", runnable.processor().name())
							+ " "
							+ localize("done")
							+ "."
					));
				});
			}

		};
		task.start();
	}

	// you probably wanna run this on some background/worker thread or something...
	private void process() {
		if (!this.processor().canProcess()) {
			log.warn("Can't process. Processor doesn't implement Processable: {}", this.processor());
			return;
		}

		final Processable p = (Processable) this.processor();
		p.process(newProcessorContext());

		FxUtils.run(() -> {
			// the method calling process (e.g. to run on background task) has
			// to update states once done.
			this.setModified(true);
		});
	}

	public void resetBackgroundTask() {
		final RunnableProcessor runnable = this;
		BackgroundTask<Void> task = new BackgroundTask<Void>(handler) {

			@Override
			protected Void call() throws Exception {
				updateTitle(localize("resetting"));
				updateMessage(localize("resetting.object", runnable.processor().name()));
				updateProgress(-1, Double.NaN);

				runnable.reset();
				return null;
			}

			@Override
			protected void finished(BackgroundTask.Result result) {
				runLater(() -> {
					runnable.updateState(true);
					handler.eventBus.post(new StatusMessageEvent(
							localize("resetting.object", runnable.processor().name())
							+ " "
							+ localize("done")
							+ "."
					));
				});
			}

		};
		task.start();
	}

	// you probably wanna run this on some background/worker thread or something...
	private void reset() {
		try {
			Files.deleteIfExists(processorDataXML());
		} catch (IOException ex) {
			log.error("failed to clear the processor's `data.xml`: {}", this, ex);
			handler.uiStrategy.showError(ex);
		}

		if (Files.exists(processorDataPath())) {
			try {
				FileFinder.deleteDirectory(processorDataPath());
			} catch (IOException ex) {
				log.error("failed to clear the processors's data directory: {}", this, ex);
				handler.uiStrategy.showError(ex);
			}
		}

		if (!this.processor().canReset()) {
			log.warn("Can't process. Processor doesn't implement Processable or Editable: {}", this.processor());
			return;
		}

		final Resetable p = (Resetable) this.processor();
		p.reset(newProcessorContext());

		FxUtils.run(() -> {
			// actually the method calling process (e.g. to run on background task) has
			// to update states once done.
			//this.updateState(true);
			this.setModified(true);
		});
	}
}
