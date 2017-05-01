package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.services.Processable;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.Resetable;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.core.services.api.HostProcessorContext;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.eventbus.events.StatusMessageEvent;
import ch.unifr.diva.dip.gui.editor.LayerExtension;
import ch.unifr.diva.dip.gui.editor.LayerGroup;
import ch.unifr.diva.dip.gui.editor.LayerOverlay;
import ch.unifr.diva.dip.gui.layout.Lane;
import ch.unifr.diva.dip.gui.pe.ProcessorParameterWindow;
import ch.unifr.diva.dip.utils.BackgroundTask;
import ch.unifr.diva.dip.utils.FileFinder;
import ch.unifr.diva.dip.api.utils.FxUtils;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
	private final LayerGroup layerGroup;
	private final LayerOverlay layerOverlay;

	// needs to be updated manually after each interaction with the processor
	private final ObjectProperty<Processor.State> stateProperty;

	/**
	 * Creates a runnable processor. As opposed to {@code ProcessorWrapper}s,
	 * {@code RunnableProcessor}'s can be executed.
	 *
	 * @param processor the processor specification.
	 * @param pipeline the (parent) runnable pipeline.
	 */
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
		this.layerGroup = new LayerGroup(processor.id);
		this.layerOverlay = new LayerOverlay();
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
		this.layerGroup.setName(this.processor().name());
		this.layerGroup.setGlyph(RunnableProcessor.glyph(this.processor()));
		this.layerGroup.setHideGroupMode(LayerGroup.HideGroupMode.AUTO);
		this.layerGroup.layerExtensions().add(new ProcessorLayerExtension(this));
		this.updateState();
	}

	/**
	 * Checks whether the runnable processor is previewable, or not.
	 *
	 * @return True if a preview is offered, False otherwise.
	 */
	public boolean isPreviewable() {
		return this.processor() instanceof Previewable;
	}

	/**
	 * Returns a previewable instance of the processor service.
	 *
	 * @return a previewable instance of the processor service.
	 */
	public Previewable getPreviewable() {
		return (Previewable) this.processor();
	}

	/**
	 * Returns the project page this runnable processor is associated to (via
	 * runnable pipeline).
	 *
	 * @return the project page.
	 */
	public ProjectPage getPage() {
		return this.page;
	}

	/**
	 * Returns the runnable pipeline this runnable processor is associated to.
	 *
	 * @return the runnable pipeline.
	 */
	public RunnablePipeline getPipeline() {
		return this.pipeline;
	}

	/**
	 * Returns the layer group of the runnable processor.
	 *
	 * @return the layer group of the runnable processor.
	 */
	public LayerGroup layer() {
		return this.layerGroup;
	}

	/**
	 * Retuns the layer overlay of the runnable processor.
	 *
	 * @return the layer overlay of the runnable processor.
	 */
	public LayerOverlay layerOverlay() {
		return this.layerOverlay;
	}

	/**
	 * The processor layer extension. Integrates processor controls into a
	 * processors layer group.
	 */
	public static class ProcessorLayerExtension implements LayerExtension, Localizable {

		final private RunnableProcessor runnable;
		final private VBox vbox = new VBox();
		final private Label status = new Label();
		final private Lane lane = new Lane();
		private Button paramButton;
		private Button processButton;
		private Button resetButton;

		/**
		 * Creates a new processor layer extension.
		 *
		 * @param runnable the runnable processor.
		 */
		public ProcessorLayerExtension(RunnableProcessor runnable) {
			this.runnable = runnable;

			status.getStyleClass().add("dip-small");

			if (runnable.processor().hasParameters()) {
				paramButton = newButton(localize("parameters"));
				paramButton.setOnAction((e) -> {
					final ProcessorParameterWindow paramWindow = new ProcessorParameterWindow(
							runnable.handler.uiStrategy.getStage(),
							runnable.handler,
							runnable
					);
					paramWindow.show();
				});
				lane.add(paramButton);

				final Region spacer = new Region();
				spacer.setMaxWidth(Double.MAX_VALUE);
				HBox.setHgrow(spacer, Priority.ALWAYS);
				lane.add(spacer);
			}

			if (runnable.processor().canProcess()) {
				processButton = newButton(localize("process"));
				processButton.setOnAction((e) -> {
					this.runnable.processBackgroundTask();
				});
				lane.add(processButton);
			}

			if (runnable.processor().canReset()) {
				resetButton = newButton(localize("reset"));
				resetButton.setOnAction((e) -> {
					this.runnable.resetBackgroundTask();
				});
				lane.add(resetButton);
			}

			lane.setAlignment(Pos.CENTER_RIGHT);
			lane.setPadding(new Insets(4, 4, 4, 4));
			vbox.getChildren().addAll(status, lane);

			stateCallback();
			this.runnable.stateProperty().addListener((e) -> stateCallback());
		}

		private Button newButton(String label) {
			final Button b = new Button(label);
			b.getStyleClass().add("dip-small");
			return b;
		}

		private void stateCallback() {
			status.setText(runnable.getState().label);
			if (processButton != null) {
				processButton.setDisable(!runnable.getState().equals(Processor.State.PROCESSING));
			}
			if (resetButton != null) {
				resetButton.setDisable(runnable.getState().equals(Processor.State.UNAVAILABLE));
			}
		}

		@Override
		public Node getComponent() {
			return vbox;
		}

		/**
		 * Returns the parent processor of this layer extension.
		 *
		 * @return the parent processor.
		 */
		public RunnableProcessor getProcessor() {
			return runnable;
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName()
					+ "{"
					+ "processor=" + runnable
					+ ", status=" + status.getText()
					+ "}";
		}

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

		switchContext(true);
		saveObjectMap();
		this.modifiedProperty().set(false);
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
	 * Passes a new context to the processor. This is necessary for open pages
	 * <ul>
	 * <li>before we switch to another page, s.t. the processor can save its
	 * state, and</li>
	 * <li>after a project got saved in order to update references to the newly
	 * opened zip file system.</li>
	 * </ul>
	 *
	 * @param saveRequired if True the state of the processor needs to be saved
	 * (since we're about to close the page with the pipeline containing this
	 * processor), otherwise only the processor context needs to be updated
	 * (since references/paths might have changed; e.g. after saving the
	 * project), while saving the processor's state is not required.
	 */
	public void switchContext(boolean saveRequired) {
		if (processor.canEdit()) {
			processor.asEditableProcessor().onContextSwitch(getProcessorContext(), saveRequired);
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

	private Path processorExportDirectory() {
		final String sanitizedPageName = page.getName().replaceAll(
				"[^a-zA-Z_\\-0-9]", // simple whitelist should be good enough
				""
		).trim();
		final String name = sanitizedPageName.isEmpty()
				? String.valueOf(page.id)
				: String.format("%s (%d)", sanitizedPageName, page.id);
		return this.project.getExportDirectory().resolve("pages").resolve(
				name
		);
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
	 * Updates the state of the processor. Updates only the state of this
	 * processor, not however the states of depended processors (whose states
	 * might change once the state of this one does...).
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
		return new HostProcessorContext(
				this.handler.threadPool,
				this.page,
				this.layerGroup,
				this.layerOverlay
		);
	}

	@Override
	protected ProcessorContext newProcessorContext() {
		return new RunnableProcessorContext(
				this.handler.threadPool,
				this.page.id,
				processorDataDirectory(),
				processorExportDirectory(),
				this.project.getExportDirectory(),
				objectMap.objects,
				this.layerGroup,
				this.layerOverlay
		);
	}

	/**
	 * Returns the processor context.
	 *
	 * @return the processor context.
	 */
	public ProcessorContext getProcessorContext() {
		return newProcessorContext();
	}

	/**
	 * Starts processing on a background task.
	 */
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
			log.warn(
					"Can't process. Processor doesn't implement Processable: {}",
					this.processor()
			);
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

	/**
	 * Resets the processor on a background task.
	 */
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
				log.error(
						"failed to clear the processors's data directory: {}",
						this, ex
				);
				handler.uiStrategy.showError(ex);
			}
		}

		if (!this.processor().canReset()) {
			log.warn(
					"Can't process. Processor doesn't implement Processable or Editable: {}",
					this.processor()
			);
			return;
		}

		final Resetable p = (Resetable) this.processor();
		p.reset(newProcessorContext());

		FxUtils.run(() -> {
			// the method calling process (e.g. to run on background task) has
			// to update states once done.
			this.setModified(true);
		});
	}

}
