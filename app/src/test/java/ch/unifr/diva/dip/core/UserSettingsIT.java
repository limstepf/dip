
package ch.unifr.diva.dip.core;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ApplicationSettings integration test.
 */
public class UserSettingsIT {

    public UserSettingsIT() {
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

	/**
	 * Test of load method, of class ApplicationSettings.
	 */
	@Test
	public void testSettingsIO() {
		File file = null;
		try {
			file = File.createTempFile("UserSettings", "xml");
			file.deleteOnExit();
		} catch (IOException ex) {
			fail("unable to create a temp. file: " + ex);
		}

		assertNotNull(file);

		UserSettings settings = new UserSettings();
		settings.primaryStage.x = 32;
		settings.primaryStage.y = 64;
		try {
			settings.save(file.toPath());
		} catch (JAXBException ex) {
			fail("error writing UserSettings: " + ex);
		}

		UserSettings ack = null;
		try {
			ack = UserSettings.load(file.toPath());
		} catch (JAXBException ex) {
			fail("error reading UserSettings: " + ex);
		}

		assertNotNull(ack);

		assertEquals(settings.primaryStage.x, ack.primaryStage.x);
		assertEquals(settings.primaryStage.y, ack.primaryStage.y);
		assertEquals(settings.primaryStage.width, ack.primaryStage.width);
		assertEquals(settings.primaryStage.height, ack.primaryStage.height);
		assertEquals(settings.primaryStage.maximized, ack.primaryStage.maximized);

		final Locale locale = ack.getLocale();
		assertNotNull(locale);
		assertEquals("default language is english",
				locale.getLanguage(), Locale.ENGLISH.getLanguage());
	}

}