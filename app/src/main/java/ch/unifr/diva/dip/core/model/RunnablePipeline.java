package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.utils.FileFinder;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import javax.xml.bind.JAXBException;

/**
 * A RunnablePipeline is a Pipeline "in use".
 */
public class RunnablePipeline extends Pipeline<RunnableProcessor> {

	/**
	 * The project page to be processed by this pipeline.
	 */
	public final ProjectPage page;

	/**
	 * Creates a new runnable pipeline from pipeline data.
	 *
	 * @param handler the application handler.
	 * @param page the project page.
	 * @param pipeline the pipeline data/specification.
	 */
	public RunnablePipeline(ApplicationHandler handler, ProjectPage page, PipelineData.Pipeline pipeline) {
		super(handler, pipeline.id, pipeline.name);
		this.page = page;

		// don't count the following initialization as modifications
		this.modifiedPipelineProperty.removeObservedProperty(this.processors);

		for (PipelineData.Processor p : pipeline.processors.list) {
			final RunnableProcessor run = new RunnableProcessor(p, this);
			run.init();
			addProcessor(run);
			levelMaxProcessorId(p.id);
		}

		initConnections(pipeline, processors);

		// init and update processor states now that they're connected
		for (Stage<RunnableProcessor> stage : stages()) {
			for (RunnableProcessor p : stage.processors) {
				p.initProcessor();
				p.updateState();
			}
		}

		// reattach listener we temp. removed (see above)
		this.modifiedPipelineProperty.addObservedProperty(this.processors);
	}

	/**
	 * Clones/copies the runnable pipeline. This returns a deep-copy of the
	 * pipeline.
	 *
	 * @return a clone of the runnable pipeline.
	 */
	public RunnablePipeline cloneRunnablePipeline() {
		final PipelineData.Pipeline data = new PipelineData.Pipeline<>(this);
		return new RunnablePipeline(handler, page, data);
	}

	/**
	 * Returns the stages of the pipeline.
	 *
	 * @return the stages of the pipeline.
	 */
	public final PipelineStages<RunnableProcessor> stages() {
		if (stages == null || dirtyStages) {
			this.stages = new Pipeline.PipelineStages<>(this, outputPortMap());
		}
		return this.stages;
	}

	/**
	 * Saves the state of the pipeline including all its
	 * {@code RunnableProcessor}s.
	 */
	public void save() {
		for (RunnableProcessor p : processors) {
			p.save();
		}
		savePipelinePatch();
		this.modifiedProperty().set(false);
	}

	private boolean savePipelinePatch() {
		deletePipelinePatchIfExists();

		final Pipeline prototype = this.page.project().pipelineManager().getPipeline(id);
		final PipelinePatch patch = PipelinePatch.createPatch(prototype, this);

		if (!patch.isEmpty()) {
			try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(this.page.pipelinePatchXml()))) {
				patch.save(stream);
			} catch (JAXBException | IOException ex) {
				log.error("failed to save pipeline patch: {}", this, ex);
				handler.uiStrategy.showError(ex);
				return false;
			}
		}

		return true;
	}

	private boolean deletePipelinePatchIfExists() {
		try {
			return Files.deleteIfExists(this.page.pipelinePatchXml());
		} catch (IOException ex) {
			log.error("failed to clear the pipeline patch: {}", this, ex);
			handler.uiStrategy.showError(ex);
		}
		return false;
	}

	/**
	 * Resets the (runnable) pipeline. This method resets all parameters
	 * (possibly overwritten w.r.t. the pipeline's prototype) and deletes all
	 * persistent processor data.
	 */
	public synchronized void reset() {
		try {
			if (Files.exists(this.page.processorRootDirectory())) {
				FileFinder.deleteDirectory(this.page.processorRootDirectory());
			}
		} catch (IOException ex) {
			log.error("failed to clear the project page's processor data: {}", this, ex);
			handler.uiStrategy.showError(ex);
		}

		deletePipelinePatchIfExists();
	}

}
