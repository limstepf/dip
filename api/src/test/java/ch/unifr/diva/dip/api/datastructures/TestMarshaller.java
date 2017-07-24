package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.utils.XmlUtils;
import java.io.IOException;
import java.nio.file.Path;
import javax.xml.bind.JAXBException;
import org.junit.rules.TemporaryFolder;

/**
 * Test marshaller. Marshalls objects to a temporary file, unmarshalls it back,
 * and asserts that the original object equals the unmarshalled one.
 *
 * <ul>
 * <li>Make sure to put marshallable classes into the data structures package of
 * the API, since these classes get added automatically to {@code JAXBContext}s
 * (see {@code getClasses} and {@code getContext} in {@code XmlUtils}).</li>
 *
 * <li>Getting {@code IllegalAnnotationExceptions}? Try to clean(!) and rebuild
 * the project. Erroneous JAXB annotations/configurations tend to stick
 * around.</li>
 * </ul>
 *
 * @param <T> class of the object to be marshalled/unmarshalled.
 */
public abstract class TestMarshaller<T> {

	public final Class<T> clazz;
	public final Path file;
	private T obj;
	private T obj_unmarshalled;

	/**
	 * Creates a new test marshaller.
	 *
	 * @param clazz the class to be marshalled/unmarshalled.
	 * @param parent a temporary folder.
	 * @throws IOException
	 */
	public TestMarshaller(Class<T> clazz, TemporaryFolder parent) throws IOException {
		this.clazz = clazz;
		this.file = parent.newFile().toPath();
	}

	/**
	 * Marshal, unmarshal, and assert equality.
	 *
	 * @throws JAXBException
	 */
	public void test() throws JAXBException {
		marshal();
		unmarshal();
		assertEquals();
	}

	/**
	 * Marshal.
	 *
	 * @throws JAXBException
	 */
	public void marshal() throws JAXBException {
		if (obj == null) {
			obj = newInstance();
		}
		XmlUtils.marshal(obj, file);
	}

	/**
	 * Marshal to {@code System.out}.
	 *
	 * @throws JAXBException
	 */
	public void print() throws JAXBException {
		if (obj == null) {
			obj = newInstance();
		}
		XmlUtils.marshal(obj, System.out);
	}

	/**
	 * Unmarshal.
	 *
	 * @throws JAXBException
	 */
	public void unmarshal() throws JAXBException {
		obj_unmarshalled = XmlUtils.unmarshal(clazz, file);
	}

	/**
	 * Assert equality of original and unmarshalled objects.
	 */
	public void assertEquals() {
		org.junit.Assert.assertEquals(
				"original object equals unmarshalled object",
				obj,
				obj_unmarshalled
		);
	}

	/**
	 * Creates a new test object.
	 *
	 * @return a new test object.
	 */
	abstract public T newInstance();

}
