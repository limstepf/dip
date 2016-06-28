package ch.unifr.diva.dip.core;

import java.io.IOException;
import java.nio.file.Path;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * ApplicationContext integration test.
 */
public class ApplicationContextIT {

	public ApplicationContextIT() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	/**
	 * Basic ApplicationContext test.
	 */
	@Test
	public void testAppContext() {
		final String appDataDirName = ApplicationSettings.appDataDirName;
		Path parentDir = null;
		try {
			parentDir = parent.newFolder().toPath();
		} catch (IOException ex) {
			fail("error creating tmp. directory: " + ex);
		}

		assertNotNull(parentDir);

		// new context with new settings
		ApplicationContext context = new ApplicationContext(parentDir, appDataDirName);

		assertEquals("no errors/exceptions", true, context.getErrors().isEmpty());

		assertNotNull("dataManager initialized", context.dataManager);
		assertNotNull("OSGi framework initialized", context.osgi);
		assertNotNull("application settings initialized", context.settings);

		// save settings
		try {
			context.settings.save(context.dataManager.appDataDir.settingsFile);
		} catch (JAXBException ex) {
			fail("error saving application settings: " + ex);
		}

		context.close();
		context.waitForStop();

		// another new context, reading back newly created settings
		context = new ApplicationContext(parentDir, appDataDirName);

		assertEquals("no errors/exceptions", true, context.getErrors().isEmpty());

		assertNotNull("dataManager initialized", context.dataManager);
		assertNotNull("OSGi framework initialized", context.osgi);
		assertNotNull("application settings initialized", context.settings);

		context.close();
		context.waitForStop();
	}

}
