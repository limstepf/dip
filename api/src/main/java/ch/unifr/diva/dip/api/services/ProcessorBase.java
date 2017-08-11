package ch.unifr.diva.dip.api.services;

import ch.unifr.diva.dip.api.components.EditorLayerPane;
import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.utils.BufferedIO;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;
import org.slf4j.LoggerFactory;

/**
 * Processor base already implements some common bits of the {@code Processor}
 * interface, and offers some helper methods as well.
 */
public abstract class ProcessorBase implements Processor {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(ProcessorBase.class);

	/**
	 * Name of the processor.
	 */
	protected final String name;

	// we use LinkedHashMap's (as opposed to ordinary HashMap's) since we care
	// about the order of ports and parameters
	/**
	 * Published input ports.
	 */
	protected final Map<String, InputPort<?>> inputs = new LinkedHashMap<>();

	/**
	 * Published output ports.
	 */
	protected final Map<String, OutputPort<?>> outputs = new LinkedHashMap<>();

	/**
	 * Published parameters.
	 */
	protected final Map<String, Parameter<?>> parameters = new LinkedHashMap<>();

	/**
	 * The repaint property.
	 */
	protected final BooleanProperty repaintProperty = new SimpleBooleanProperty();

	/**
	 * Creates a new base processor.
	 *
	 * @param name name of the processor.
	 */
	public ProcessorBase(String name) {
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Map<String, Parameter<?>> parameters() {
		return parameters;
	}

	@Override
	public Map<String, InputPort<?>> inputs() {
		return inputs;
	}

	@Override
	public Map<String, OutputPort<?>> outputs() {
		return outputs;
	}

	@Override
	public BooleanProperty repaintProperty() {
		return repaintProperty;
	}

	/**
	 * Resets all (published) output ports. Make sure to override this method if
	 * the processor has a dynamic/changing set of output ports.
	 */
	protected void resetOutputs() {
		for (OutputPort<?> output : outputs.values()) {
			output.setOutput(null);
		}
	}

	/**
	 * Clears/empties the processor's object map.
	 *
	 * @param context the processor context.
	 */
	public static void resetObjects(ProcessorContext context) {
		context.getObjects().clear();
	}

	/**
	 * Clears/empties the processor layer.
	 *
	 * @param context the processor context.
	 */
	public static void resetLayer(ProcessorContext context) {
		context.getLayer().clear();
	}

	/**
	 * Provides and adds a simple {@code LayerPane} with the given {@code Node}
	 * to the processor layer.
	 *
	 * @param context the processor context.
	 * @param node the JavaFX node.
	 * @return the added {@code LayerPane}.
	 */
	public static EditorLayerPane provideLayer(ProcessorContext context, Node node) {
		return provideLayer(context, node, "");
	}

	/**
	 * Provides and adds a simple {@code LayerPane} with the given {@code Node}
	 * to the processor layer.
	 *
	 * @param context the processor context.
	 * @param node the JavaFX node.
	 * @param name the name of the layer.
	 * @return the added {@code LayerPane}.
	 */
	public static EditorLayerPane provideLayer(ProcessorContext context, Node node, String name) {
		final EditorLayerPane layer = context.getLayer().newLayerPane(name);
		layer.add(node);
		return layer;
	}

	/**
	 * Provides and adds a simple {@code LayerPane} with an {@code ImageView} to
	 * the processor layer.
	 *
	 * @param context the processor context.
	 * @param bufferedImage the image to be displayed in the {@code LayerPane}.
	 * @return the added {@code LayerPane}.
	 */
	public static EditorLayerPane provideImageLayer(ProcessorContext context, BufferedImage bufferedImage) {
		return provideImageLayer(context, bufferedImage, "");
	}

	/**
	 * Provides and adds a simple {@code LayerPane} with an {@code ImageView} to
	 * the processor layer.
	 *
	 * @param context the processor context.
	 * @param bufferedImage the image to be displayed in the {@code LayerPane}.
	 * @param name the name of the layer.
	 * @return the added {@code LayerPane}.
	 */
	public static EditorLayerPane provideImageLayer(ProcessorContext context, BufferedImage bufferedImage, String name) {
		final Image image = SwingFXUtils.toFXImage(bufferedImage, null);
		return provideImageLayer(context, image, name);
	}

	/**
	 * Provides and adds a simple {@code LayerPane} with an {@code ImageView} to
	 * the processor layer.
	 *
	 * @param context the processor context.
	 * @param image the image to be displayed in the {@code LayerPane}.
	 * @return the added {@code LayerPane}.
	 */
	public static EditorLayerPane provideImageLayer(ProcessorContext context, Image image) {
		return provideImageLayer(context, image, "");
	}

	/**
	 * Provides and adds a named {@code LayerPane} with an {@code ImageView} to
	 * the processor layer.
	 *
	 * @param context the processor context.
	 * @param image the image to be displayed in the {@code LayerPane}.
	 * @param name the name of the layer.
	 * @return the added {@code LayerPane}.
	 */
	public static EditorLayerPane provideImageLayer(ProcessorContext context, Image image, String name) {
		final EditorLayerPane layer = context.getLayer().newLayerPane(name);
		layer.add(new ImageView(image));
		return layer;
	}

	/**
	 * Writes an object to the savefile.
	 *
	 * @param <T> type of the object.
	 * @param context the processor context.
	 * @param obj the object. Must be marshallable with JAXB.
	 * @param filename the filename of the image.
	 */
	public static <T> void writeObject(ProcessorContext context, T obj, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			writeObject(obj, file);
		} catch (IOException | JAXBException ex) {
			log.warn("failed to write {} to file: {}", obj, file, ex);
		}
	}

	/**
	 * Writes an object to a file.
	 *
	 * @param <T> type of the object.
	 * @param obj the object. Must be marshallable with JAXB.
	 * @param file the file to write to.
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static <T> void writeObject(T obj, Path file) throws IOException, JAXBException {
		deleteFile(file);
		try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(file))) {
			XmlUtils.marshal(obj, stream);
		}
	}

	/**
	 * Reads an object from the savefile.
	 *
	 * @param <T> type of the object.
	 * @param context the processor context.
	 * @param filename the filename of the image.
	 * @param clazz class of the object.
	 * @return the object, or {@code null}.
	 */
	public static <T> T readObject(ProcessorContext context, String filename, Class<T> clazz) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			return readObject(file, clazz);
		} catch (FileNotFoundException ex) {
			return null;
		} catch (IOException | JAXBException ex) {
			log.warn("failed to read file: {}", file, ex);
		}
		return null;
	}

	/**
	 * Reads an object from a file.
	 *
	 * @param <T> type of the object.
	 * @param file the file to read from.
	 * @param clazz class of the object.
	 * @return the object.
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static <T> T readObject(Path file, Class<T> clazz) throws IOException, JAXBException {
		if (Files.exists(file)) {
			try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
				return XmlUtils.unmarshal(clazz, stream);
			}
		}
		throw new FileNotFoundException();
	}

	/**
	 * Writes a {@code BufferedImage} to the savefile.
	 *
	 * @param context the processor context.
	 * @param image the image.
	 * @param filename the filename of the image.
	 * @param format the format (or extension) of the image (e.g. "PNG").
	 */
	public static void writeBufferedImage(ProcessorContext context, BufferedImage image, String filename, String format) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			writeBufferedImage(image, format, file);
		} catch (IOException ex) {
			log.warn("failed to write file: {}", file, ex);
		}
	}

	/**
	 * Writes a {@code BufferedImage} to a file.
	 *
	 * @param image the image.
	 * @param format the format (or extension) of the image (e.g. "PNG").
	 * @param file the file to write to.
	 * @throws IOException
	 */
	public static void writeBufferedImage(BufferedImage image, String format, Path file) throws IOException {
		deleteFile(file);
		try (OutputStream os = Files.newOutputStream(file)) {
			ImageIO.write(image, format, os);
		}
	}

	/**
	 * Reads a {@code BufferedImage} from the savefile.
	 *
	 * @param context the processor context.
	 * @param filename the filename of the image.
	 * @return the image, or {@code null}.
	 */
	public static BufferedImage readBufferedImage(ProcessorContext context, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			return readBufferedImage(file);
		} catch (FileNotFoundException ex) {
			return null;
		} catch (IOException ex) {
			log.warn("failed to read file: {}", file, ex);
		}
		return null;
	}

	/**
	 * Reads a {@code BufferedImage} from a file.
	 *
	 * @param file the file to read from.
	 * @return the image.
	 * @throws IOException
	 */
	public static BufferedImage readBufferedImage(Path file) throws IOException {
		if (Files.exists(file)) {
			try (InputStream is = Files.newInputStream(file)) {
				return ImageIO.read(is);
			}
		}
		throw new FileNotFoundException();
	}

	/**
	 * Writes a {@code BufferedMatrix} to the savefile.
	 *
	 * @param context the processor context.
	 * @param mat the image/matrix.
	 * @param filename the filename of the image/matrix.
	 */
	public static void writeBufferedMatrix(ProcessorContext context, BufferedMatrix mat, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			writeBufferedMatrix(mat, file);
		} catch (IOException ex) {
			log.warn("failed to write file: {}", file, ex);
		}
	}

	/**
	 * Writes a {@code BufferedMatrix} to a file.
	 *
	 * @param mat the image/matrix.
	 * @param file the file to write to.
	 * @throws IOException
	 */
	public static void writeBufferedMatrix(BufferedMatrix mat, Path file) throws IOException {
		deleteFile(file);
		/*
		 We can't close the output stream here! Neither explicitly, nor by means
		 of autoclosable/try-with-resource. Otherwise we end up with a bmat-file
		 of 0 bytes. See: https://bugs.openjdk.java.net/browse/JDK-8069211

		 "There is a bug in ZipFileSystem.EntryOutputStream.close(). The current
		 implementation does not prevent you from writing/closing into the stream
		 after you have closed it. The second close will basically reset the size
		 to wrong number... You test case close the stream twice, one is the
		 explicit close, on by the try/catch. Will file a bug. The temporary
		 workaround is to either remove the "out" out of the try/catch auto-close
		 one or don't close it explicitly."

		 ...so writeMat wrapps the output stream in a DataOutputStream to do his
		 thing and closes it, which gets propagated to the original output stream
		 as usual. If we now close the output stream here as well, that's the
		 second time we're closing it, and that's what triggers this bug and will
		 mess up the bmat-file (check with Files.exists(file), Files.size(file)).

		 Also see:
		 http://hg.openjdk.java.net/jdk9/dev/jdk/rev/b6ae6184b241
		 http://hg.openjdk.java.net/jdk9/jdk9/jdk/rev/b6ae6184b241

		 ...meaning this should be fixed with the next major release (Java9).
		 */
		OutputStream os = Files.newOutputStream(file); // DON'T os.close(); (also no autoclose/try-with-resource)
		BufferedIO.writeMat(mat, os);
	}

	/**
	 * Reads a {@code BufferedMatrix} from the savefile.
	 *
	 * @param context the processor context.
	 * @param filename the filename of the image/matrix.
	 * @return the image/matrix, or {@code null}.
	 */
	public static BufferedMatrix readBufferedMatrix(ProcessorContext context, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			return readBufferedMatrix(file);
		} catch (FileNotFoundException ex) {
			return null;
		} catch (IOException ex) {
			log.warn("failed to read file: {}", file, ex);
		}
		return null;
	}

	/**
	 * Reads a {@code BufferedMatrix} from a file.
	 *
	 * @param file the file to read.
	 * @return the image/matrix.
	 * @throws IOException
	 */
	public static BufferedMatrix readBufferedMatrix(Path file) throws IOException {
		if (Files.exists(file)) {
			try (InputStream is = Files.newInputStream(file)) {
				return BufferedIO.readMat(is);
			}
		}
		throw new FileNotFoundException();
	}

	/**
	 * Writes an {@code Image} to the savefile.
	 *
	 * @param context the processor context.
	 * @param image the image.
	 * @param filename the filename of the image.
	 * @param format the format (or extension) of the image (e.g. "PNG").
	 */
	public static void writeImage(ProcessorContext context, Image image, String filename, String format) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			writeImage(image, format, file);
		} catch (IOException ex) {
			log.warn("failed to write file: {}", file, ex);
		}
	}

	/**
	 * Writes an {@code Image} to a file.
	 *
	 * @param image the image.
	 * @param format the format (or extension) of the image (e.g. "PNG").
	 * @param file the file to write to.
	 * @throws IOException
	 */
	public static void writeImage(Image image, String format, Path file) throws IOException {
		deleteFile(file);
		try (OutputStream os = Files.newOutputStream(file)) {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), format, os);
		}
	}

	/**
	 * Reads an {@code Image} from the savefile.
	 *
	 * @param context the processor context.
	 * @param filename the filename of the image.
	 * @return the image, or {@code null}.
	 */
	public static Image readImage(ProcessorContext context, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			return readImage(file);
		} catch (FileNotFoundException ex) {
			return null;
		} catch (IOException ex) {
			log.warn("failed to read file: {}", file, ex);
		}
		return null;
	}

	/**
	 * Reads an {@code Image} from a file.
	 *
	 * @param file the file to read from.
	 * @return the image.
	 * @throws IOException
	 */
	public static Image readImage(Path file) throws IOException {
		if (Files.exists(file)) {
			try (InputStream is = Files.newInputStream(file)) {
				return new Image(is);
			}
		}
		throw new FileNotFoundException();
	}

	/**
	 * Removes a file from the savefile (if it exists).
	 *
	 * @param context the processor context.
	 * @param filename the filename of the file.
	 * @return {@code true} if the file was deleted by this method,
	 * {@code false} otherwise.
	 */
	public static boolean deleteFile(ProcessorContext context, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			return deleteFile(file);
		} catch (IOException ex) {
			log.warn("failed to remove processor file: {}", file, ex);
		}
		return false;
	}

	/**
	 * Removes a file (if it exists).
	 *
	 * @param file the file.
	 * @return {@code true} if the file was deleted by this method,
	 * {@code false} otherwise.
	 * @throws java.io.IOException
	 */
	public static boolean deleteFile(Path file) throws IOException {
		return Files.deleteIfExists(file);
	}

	/**
	 * XOR isConnected replacement. This method can be used to override the
	 * isConnected method, and is used on a set of optional ports where we
	 * require exactly a single connection.
	 *
	 * Usually used together with the xorCallback method which disables the rest
	 * of the ports, once the needed connection has been made.
	 *
	 * @param ports a collection of optional ports.
	 * @return {@code true} if the port is connected, {@code false} otherwise.
	 */
	public static boolean xorIsConnected(Collection<? extends Port<?>> ports) {
		for (Port<?> port : ports) {
			if (port.isConnected()) {
				return true; // no need to check that there aren't more connections...
			}
		}
		return false;
	}

}
