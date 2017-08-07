package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.MathUtils;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * OSGi service recollection unit tests.
 */
public class OSGiServiceRecollectionTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testRecollection() throws IOException, JAXBException {
		OSGiServiceRecollection<Processor> recollection = new OSGiServiceRecollection<>();

		List<ServiceData> services = new ArrayList<>();
		String pid;
		String version;
		boolean isNewService;

		// generate and collect a bunch of services
		for (int i = 0; i < 10; i++) {
			pid = generatePID();
			for (int v = 0; v < 5; v++) {
				version = String.format("1.0.%d", v);
				isNewService = recollection.addService(pid, version);
				assertTrue(
						"new/unseen service",
						isNewService
				);
				services.add(new ServiceData(pid, version));
			}
		}

		ServiceData data;
		int idx;

		// make sure services are recollected
		for (int i = 0; i < 10; i++) {
			idx = MathUtils.randomInt(0, services.size());
			data = services.get(idx);
			isNewService = recollection.addService(data.pid, data.version);
			assertFalse(
					"service is not new/unseen",
					isNewService
			);
			assertTrue(
					"service is known/has seen before",
					recollection.knowsService(data.pid, data.version)
			);
		}

		// make sure new services aren't known yet without adding them
		for (int i = 0; i < 5; i++) {
			pid = generatePID();
			for (int v = 0; v < 3; v++) {
				version = String.format("2.0.%d", v);
				assertFalse(
						"service is new/unseen",
						recollection.knowsService(pid, version)
				);
			}
		}

		// test marshaller
		Path file = parent.newFile().toPath();
		XmlUtils.marshal(recollection, file);
		// ...unmarshal
		@SuppressWarnings("unchecked")
		OSGiServiceRecollection<Processor> re2 = XmlUtils.unmarshal(
				OSGiServiceRecollection.class,
				file
		);
//		print(re2);

		// ...and make sure services are (still) recollected
		for (int i = 0; i < 10; i++) {
			idx = MathUtils.randomInt(0, services.size());
			data = services.get(idx);
			isNewService = re2.addService(data.pid, data.version);
			assertFalse(
					"service is not new/unseen",
					isNewService
			);
			assertTrue(
					"service is known/has seen before",
					re2.knowsService(data.pid, data.version)
			);
		}

	}

	/**
	 * Test service data.
	 */
	public static class ServiceData {

		final public String pid;
		final public String version;

		/**
		 * Creates new test service data.
		 *
		 * @param pid the PID of the service.
		 * @param version the version of the service.
		 */
		public ServiceData(String pid, String version) {
			this.pid = pid;
			this.version = version;
		}
	}

	public static String generatePID() {
		return UUID.randomUUID().toString();
	}

	public static void print(OSGiServiceRecollection<Processor> recollection) throws JAXBException {
		XmlUtils.marshal(recollection, System.out);
	}

}
