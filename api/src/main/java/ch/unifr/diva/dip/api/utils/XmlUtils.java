package ch.unifr.diva.dip.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * XML utilities. Basic utilities to un-/marshall Java objects to/from XML.
 */
public class XmlUtils {

	private XmlUtils() {
		/* nope :) */
	}

	private static final Logger log = LoggerFactory.getLogger(XmlUtils.class);
	private static final String DIP_API_DS_PACKAGE = "ch.unifr.diva.dip.api.datastructures";

	/**
	 * Additional classes for JAXB contexts. Add all data structures in the DIP
	 * API s.t. they can be easily marshalled to XML (e.g. in a HashMap). While
	 * we'll add them directly to JAXB contexts by passing these classes to the
	 * constructor of such a context, this has pretty much the same effect as
	 * using the Java annotation XmlSeeAlso (e.g. together with XmlRootElement).
	 */
	final static ArrayList<Class<?>> seeAlsoClasses = new ArrayList<>();

	static {
		try {
			List<String> classes = ReflectionUtils.findClasses(DIP_API_DS_PACKAGE);
			for (String cn : classes) {
				Class<?> clazz = ReflectionUtils.getClass(cn);
				// ignore interfaces or JAXB will complain about it
				if (clazz.isInterface()) {
					continue;
				}
				seeAlsoClasses.add(clazz);
			}
			seeAlsoClasses.add(ArrayList.class);
		} catch (IOException ex) {
			log.error("can't find package: {}", DIP_API_DS_PACKAGE);
		}
	}

	/**
	 * Returns an array of all classes default JAXB contexts should know about.
	 * This mixes the static seeAlsoClasses with the given additional class that
	 * is actually to be unmarshalled.
	 *
	 * @param clazz class of the object to be unmarshalled.
	 * @return an array of classes.
	 */
	private static Class<?>[] getClasses(Class<?> clazz) {
		final int n = seeAlsoClasses.size();
		final Class<?>[] classes = new Class<?>[n + 1];
		for (int i = 0; i < n; i++) {
			classes[i] = seeAlsoClasses.get(i);
		}
		classes[n] = clazz;
		return classes;
	}

	/**
	 * Creating JAXB contexts is considered quite expensive, so we store them
	 * statically. And yes, they're completely thread-safe.
	 */
	private static final HashMap<Class<?>, JAXBContext> jaxbContexts = new HashMap<>();

	private static JAXBContext getContext(Class<?> clazz) throws JAXBException {
		if (!jaxbContexts.containsKey(clazz)) {
			final JAXBContext context = JAXBContext.newInstance(getClasses(clazz));
			jaxbContexts.put(clazz, context);
		}
		return jaxbContexts.get(clazz);
	}

	/**
	 * Unmashalls an XML file into a Java object.
	 *
	 * @param <T> Type of clazz.
	 * @param clazz Class of the Java object to be unmarshalled.
	 * @param file XML to read from.
	 * @return The unmarshalled Java object of type T.
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unmarshal(Class<T> clazz, Path file) throws JAXBException {
		final Unmarshaller m = getUnmarshaller(clazz);
		return (T) m.unmarshal(file.toFile());
	}

	/**
	 * Unmashalls an XML file into a Java object.
	 *
	 * @param <T> Type of clazz.
	 * @param clazz Class of the Java object to be unmarshalled.
	 * @param stream input stream to read from.
	 * @return The unmarshalled Java object of type T.
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unmarshal(Class<T> clazz, InputStream stream) throws JAXBException {
		final Unmarshaller m = getUnmarshaller(clazz);
		return (T) m.unmarshal(stream);
	}

	/**
	 * Returns an unmarshaller for the specified class.
	 *
	 * @param clazz Class of the Java object to be unmarshalled.
	 * @return an unmarshaller for objects of type clazz.
	 * @throws JAXBException
	 */
	public static Unmarshaller getUnmarshaller(Class<?> clazz) throws JAXBException {
		final JAXBContext c = getContext(clazz);
		return c.createUnmarshaller();
	}

	/**
	 * Marshalls a Java object to an XML file.
	 *
	 * @param obj Java object to be marshalled to an XML file.
	 * @param file XML file to write to.
	 * @throws JAXBException
	 */
	public static void marshal(Object obj, Path file) throws JAXBException {
		final Marshaller m = getMarshaller(obj);
		m.marshal(obj, file.toFile());
	}

	/**
	 * Marshalls a Java object to an XML file.
	 *
	 * @param obj Java object to be marshalled to an XML file.
	 * @param stream an output stream to write to.
	 * @throws JAXBException
	 */
	public static void marshal(Object obj, OutputStream stream) throws JAXBException {
		final Marshaller m = getMarshaller(obj);
		m.marshal(obj, stream);
	}

	/**
	 * Returns a marshaller for the class of the given object.
	 *
	 * @param obj Java object to be marshalled to an XML file.
	 * @return a marshaller for the class of the given object.
	 * @throws JAXBException
	 */
	public static Marshaller getMarshaller(Object obj) throws JAXBException {
		final JAXBContext c = getContext(obj.getClass());
		final Marshaller m = c.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		return m;
	}

	/**
	 * Reads an XML document from a file and converts it to string.
	 * Alternatively maybe just {@code XmlUtils.marshal(obj, System.out)}.
	 *
	 * @param file an XML file
	 * @return pretty printed XML document
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public static String xmlToString(Path file) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		final DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file.toFile());
		return xmlToString(doc);
	}

	/**
	 * Converts an XML document to string.
	 *
	 * @param doc an XML document
	 * @return pretty printed XML document
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
	public static String xmlToString(Document doc) throws TransformerConfigurationException, TransformerException {
		final Transformer tf = TransformerFactory.newInstance().newTransformer();
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		final StreamResult result = new StreamResult(new StringWriter());
		final DOMSource source = new DOMSource(doc);
		tf.transform(source, result);
		return result.getWriter().toString();
	}
}
