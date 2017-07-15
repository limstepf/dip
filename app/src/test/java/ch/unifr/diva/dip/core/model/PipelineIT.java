package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.core.ApplicationContext;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ApplicationSettings;
import ch.unifr.diva.dip.core.services.api.HostService;
import ch.unifr.diva.dip.core.ui.UIStrategyCLI;
import ch.unifr.diva.dip.eventbus.EventBusGuava;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Processing pipeline integration tests.
 */
public class PipelineIT {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testEmptyPipeline() throws IOException, JAXBException {
		final String appDataDirName = ApplicationSettings.appDataDirName;
		final Path directory = parent.newFolder().toPath();
		final Path file = parent.newFile("pipeline.xml").toPath();
		final ApplicationContext context = new ApplicationContext(directory, appDataDirName);
		final ApplicationHandler handler = new ApplicationHandler(
				context,
				new UIStrategyCLI(),
				new EventBusGuava()
		);

		final Pipeline<ProcessorWrapper> theEmptyPipeline = PipelineManager.emptyPipeline(handler);
		final List<Pipeline<ProcessorWrapper>> pipelines = new ArrayList<>();
		pipelines.add(theEmptyPipeline);

		PipelineManager.exportPipelines(pipelines, file);

		final List<Pipeline<ProcessorWrapper>> theSamePipelines = PipelineManager.importPipelines(handler, file, -1);
		assertTrue("unserialized, previously serialized pipelines", theSamePipelines != null);
		assertTrue("exactly one pipeline again", theSamePipelines.size() == 1);

		final Pipeline<ProcessorWrapper> sameEmptyPipeline = theSamePipelines.get(0);
		assertEquals("equal id", theEmptyPipeline.id, sameEmptyPipeline.id);
		assertEquals(
				"equal name",
				theEmptyPipeline.getName(), sameEmptyPipeline.getName()
		);
		assertTrue(
				"has exactly one processor",
				sameEmptyPipeline.processors().size() == 1
		);
		assertTrue(
				"verify processor pid",
				sameEmptyPipeline.processors().get(0).pid().equals(
						HostService.DEFAULT_GENERATOR
				)
		);

		context.close();
		context.waitForStop();
	}

}
