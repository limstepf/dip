package ch.unifr.diva.dip.eventbus;

import ch.unifr.diva.dip.eventbus.events.ApplicationRequest;
import ch.unifr.diva.dip.eventbus.events.ProjectRequest;
import com.google.common.eventbus.Subscribe;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * EventBusGuava "unit" tests.
 * Given this isn't really our code this runs as integration test.
 */
public class EventBusGuavaIT {

	public EventBusGuavaIT() {
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
	 * Test of register, post and unregister methods with single subscribers.
	 */
	@Test
	public void testSingleSubscribers() {
		final EventBus eventbus = new EventBusGuava();
		final TestHandler appHandler = new ApplicationTestHandler();
		final TestHandler projectHandler = new ProjectTestHandler();

		eventbus.register(appHandler);
		eventbus.register(projectHandler);

		assertEquals(0, appHandler.getEvents());
		assertEquals(0, appHandler.getOther());
		assertEquals(0, projectHandler.getEvents());
		assertEquals(0, projectHandler.getOther());

		eventbus.post(new ApplicationRequest(ApplicationRequest.Type.EXIT));
		eventbus.post(new ProjectRequest(ProjectRequest.Type.OPEN));
		eventbus.post(new ProjectRequest(ProjectRequest.Type.CLOSE));

		assertEquals(1, appHandler.getEvents());
		assertEquals(0, appHandler.getOther());
		assertEquals(1, projectHandler.getEvents());
		assertEquals(1, projectHandler.getOther());

		eventbus.unregister(appHandler);
		eventbus.unregister(projectHandler);

		eventbus.post(new ApplicationRequest(ApplicationRequest.Type.EXIT));
		eventbus.post(new ProjectRequest(ProjectRequest.Type.OPEN));

		assertEquals(1, appHandler.getEvents());
		assertEquals(1, projectHandler.getEvents());
	}

	/**
	 * Test of register, post and unregister methods with multiple subscribers.
	 */
	@Test
	public void testBroadcast() {
		final EventBus eventbus = new EventBusGuava();
		final List<TestHandler> handlers = Arrays.asList(
				new ProjectTestHandler(),
				new ProjectTestHandler(),
				new ProjectTestHandler()
		);

		for (TestHandler h : handlers) {
			eventbus.register(h);
		}

		for (TestHandler h : handlers) {
			assertEquals(0, h.getEvents());
			assertEquals(0, h.getOther());
		}

		eventbus.post(new ApplicationRequest(ApplicationRequest.Type.EXIT));
		eventbus.post(new ProjectRequest(ProjectRequest.Type.OPEN));
		eventbus.post(new ProjectRequest(ProjectRequest.Type.CLOSE));

		for (TestHandler h : handlers) {
			assertEquals(1, h.getEvents());
			assertEquals(1, h.getOther());
		}

		for (TestHandler h : handlers) {
			eventbus.unregister(h);
		}

		eventbus.post(new ApplicationRequest(ApplicationRequest.Type.EXIT));
		eventbus.post(new ProjectRequest(ProjectRequest.Type.OPEN));
		eventbus.post(new ProjectRequest(ProjectRequest.Type.CLOSE));

		for (TestHandler h : handlers) {
			assertEquals(1, h.getEvents());
			assertEquals(1, h.getOther());
		}
	}

	public static class TestHandler {
		protected int events = 0;
		protected int other = 0;

		public int getEvents() {
			return events;
		}

		public int getOther() {
			return other;
		}
	}

	public static class ApplicationTestHandler extends TestHandler {
		@Subscribe
		public void applicationEvent(ApplicationRequest event) {
			switch (event.type) {
				case EXIT:
					events++;
					break;
				default:
					other++;
					break;
			}
		}
	}

	public static class ProjectTestHandler extends TestHandler {
		@Subscribe
		public void projectEvent(ProjectRequest event) {
			switch (event.type) {
				case CLOSE:
					events++;
					break;
				default:
					other++;
					break;
			}
		}
	}
}
