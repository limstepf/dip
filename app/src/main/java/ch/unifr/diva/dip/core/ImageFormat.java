package ch.unifr.diva.dip.core;

import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.utils.IOUtils;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/**
 * Supported image formats. ImageFormat defines a set of supported image formats
 * and implements necessary I/O methods.
 */
public enum ImageFormat {

	BMP("bmp") {
				@Override
				Image loadImage(InputStream stream) {
					return new Image(stream);
				}

				@Override
				Image loadImage(String url) {
					return new Image(url);
				}

				@Override
				BufferedImage loadBufferedImage(String url) throws IOException {
					return ImageIO.read(new File(url));
				}

				@Override
				BufferedImage loadBufferedImage(InputStream stream) throws IOException {
					return ImageIO.read(stream);
				}
			},
	GIF("gif") {
				@Override
				Image loadImage(InputStream stream) {
					return new Image(stream);
				}

				@Override
				Image loadImage(String url) {
					return new Image(url);
				}

				@Override
				BufferedImage loadBufferedImage(String url) throws IOException {
					return ImageIO.read(new File(url));
				}

				@Override
				BufferedImage loadBufferedImage(InputStream stream) throws IOException {
					return ImageIO.read(stream);
				}
			},
	JPEG("jpeg", "jpg") {
				@Override
				Image loadImage(InputStream stream) {
					return new Image(stream);
				}

				@Override
				Image loadImage(String url) {
					return new Image(url);
				}

				@Override
				BufferedImage loadBufferedImage(String url) throws IOException {
					return ImageIO.read(new File(url));
				}

				@Override
				BufferedImage loadBufferedImage(InputStream stream) throws IOException {
					return ImageIO.read(stream);
				}
			},
	PNG("png") {
				@Override
				Image loadImage(InputStream stream) {
					return new Image(stream);
				}

				@Override
				Image loadImage(String url) {
					return new Image(url);
				}

				@Override
				BufferedImage loadBufferedImage(String url) throws IOException {
					return ImageIO.read(new File(url));
				}

				@Override
				BufferedImage loadBufferedImage(InputStream stream) throws IOException {
					return ImageIO.read(stream);
				}
			};

	// state per enum/instance
	private final HashSet<String> extensions = new HashSet<>();

	// static state
	private static final HashSet<String> supported = new HashSet<>();
	private static final FileChooser.ExtensionFilter extensionFilter;

	static {
		for (ImageFormat format : ImageFormat.values()) {
			supported.addAll(format.extensions);
		}
		final List<String> filter = new ArrayList<>();
		for (String ext : supported) {
			filter.add("*." + ext);
		}
		Collections.sort(filter);
		extensionFilter = new FileChooser.ExtensionFilter(
				L10n.getInstance().getString("image.files"),
				filter
		);
	}

	/**
	 * Default constructor.
	 *
	 * @param extensions list of file extensions associated with this
	 * ImageFormat.
	 */
	ImageFormat(String... extensions) {
		this.extensions.addAll(Arrays.asList(extensions));
	}

	//==========================================================================
	// ImageFormat Enum/per instance methods
	//==========================================================================
	/**
	 * Constructs an Image with content loaded from the specified url.
	 *
	 * @param url the string representing the URL to use in fetching the pixel
	 * data.
	 * @return an Image.
	 */
	abstract Image loadImage(String url);

	/**
	 * Construct an Image with content loaded from the specified input stream.
	 *
	 * @param stream the stream from which to load the image.
	 * @return an Image.
	 */
	abstract Image loadImage(InputStream stream);

	/**
	 * Constructs a BufferedImage with content loaded from the specified url.
	 *
	 * @param url the string representing the URL to use in fetching the pixel
	 * data.
	 * @return a BufferedImage.
	 */
	abstract BufferedImage loadBufferedImage(String url) throws IOException;

	/**
	 * Construct a BufferedImage with content loaded from the specified input
	 * stream.
	 *
	 * @param stream the stream from which to load the image.
	 * @return a BufferedImage.
	 */
	abstract BufferedImage loadBufferedImage(InputStream stream) throws IOException;

	/**
	 * Checks whether a given file extension is supported by the ImageFile.
	 *
	 * @param extension a file extension (in lowercase!)
	 * @return {@code true} if the ImageFile supports the file extension,
	 * {@code false} otherwise.
	 */
	public boolean supportsExtension(String extension) {
		return extensions.contains(extension);
	}

	//==========================================================================
	// ImageFormat methods (static)
	//==========================================================================
	/**
	 * Loads an image from the specified file.
	 *
	 * @param file an image file.
	 * @return an Image.
	 * @throws IOException in case of an I/O error, or an unsupported image
	 * format.
	 */
	public static Image getImage(Path file) throws IOException {
		final ImageFormat format = getImageFormat(file);
		if (format == null) {
			throw (new IOException("Unsupported image format"));
		}

		try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
			return format.loadImage(stream);
		} catch (IOException ex) {
			throw (ex);
		}
	}

	public static BufferedImage getBufferedImage(Path file) throws IOException {
		final ImageFormat format = getImageFormat(file);
		if (format == null) {
			throw (new IOException("Unsupported image format"));
		}

		try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
			return format.loadBufferedImage(stream);
		} catch (IOException ex) {
			throw (ex);
		}
	}

	/**
	 * Returns the ImageFormat of a file, or null if no supported ImageFormat is
	 * available.
	 *
	 * @param file an image file.
	 * @return the ImageFormat that supports the file, or null.
	 */
	public static ImageFormat getImageFormat(Path file) {
		String ext = IOUtils.getFileExtension(file);
		for (ImageFormat format : ImageFormat.values()) {
			if (format.supportsExtension(ext)) {
				return format;
			}
		}
		return null;
	}

	/**
	 * Check whether a file extension looks like a supported image format.
	 *
	 * @param extension a file extension (usually 3 or 4 chars long).
	 * @return {@code true} if the file extension is supported, {@code false}
	 * otherwise.
	 */
	public static boolean isSupported(String extension) {
		return supported.contains(extension.toLowerCase());
	}

	/**
	 * Returns an ExtensionFilter to select (supported) images.
	 *
	 * @return an ExtensionFilter for supported images.
	 */
	public static FileChooser.ExtensionFilter getExtensionFilter() {
		return extensionFilter;
	}

	/**
	 * Sets an extension filter on a {@code FileChooser} to select (supported)
	 * images. This also registers an alternative extension filter to select
	 * from all/any files.
	 *
	 * @param chooser a FileChooser.
	 */
	public static void setExtensionFilter(FileChooser chooser) {
		chooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter(
						L10n.getInstance().getString("files.all"),
						"*.*"
				),
				getExtensionFilter()
		);
		chooser.setSelectedExtensionFilter(
				getExtensionFilter()
		);
	}
}
