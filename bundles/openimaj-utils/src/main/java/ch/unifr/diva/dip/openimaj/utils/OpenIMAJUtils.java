package ch.unifr.diva.dip.openimaj.utils;

import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.datastructures.Floats2D;
import ch.unifr.diva.dip.api.datastructures.MultiFloats2D;
import ch.unifr.diva.dip.api.utils.BufferedIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.slf4j.LoggerFactory;

/**
 * OpenIMAJ utilities. {@code Floats2D} and {@code MultiFloats2D} are data
 * structures (and data types) from the DIP API that can use the exact same
 * underlying single-precision floating point arrays as OpenIMAJ's
 * {@code FImage} and {@code MBFImage} respectively. The conversion methods
 * offer the option to either reuse that array (for in-place manipulation), or
 * to make a copy. <br />
 *
 * Typically {@code Floats2D} and {@code MultiFloats2D} from input ports should
 * be copied to completely new {@code FImage} and {@code MBFImage} respectively,
 * since the values on input ports should not be manipulated, while
 * {@code FImage} and {@code MBFImage} can be turned into {@code Floats2D} and
 * {@code MultiFloats2D} without copying the underlying arrays for setting the
 * values on output ports.
 *
 * <p>
 * Conversion methods between {@code FImage} or {@code MBFImage}, and
 * {@code BufferedImage} or {@code BufferedMatrix} all copy the samples without
 * any checks or clamping. Also check out the available methods in
 * {@code org.openimaj.image.ImageUtilities}, but have a look at the source code
 * first to figure out what's going on (e.g. the method
 * {@code ImageUtilities.createFImage} does an NTSC colour conversion).
 *
 * <p>
 * The reading and writing methods internally convert to and from
 * {@code BufferedMatrix}, and use {@code BufferedIO} to read and write those
 * files in an efficient and documented data format. This means that you do not
 * have to worry about any color spaces or sample precision, as opposed to using
 * the methods offered by OpenIMAJ's {@code ImageUtilities}, which internally
 * convert to {@code BufferedImage} (using the
 * {@code createBufferedImageForDisplay} method), and use {@code ImageIO} for
 * reading and writing (which may easily fuck up your samples, or require you to
 * scale them before writing/after reading...).
 */
public class OpenIMAJUtils {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(OpenIMAJUtils.class);

	private OpenIMAJUtils() {
		// nope
	}

	/**
	 * Converts a {@code FImage} to {@code Floats2D}.
	 *
	 * @param fimage the image.
	 * @param copy {@code true} to make a copy of the underlying array,
	 * {@code false} to use the same underlying array, passing along just a
	 * pointer.
	 * @return a {@code Floats2D} wrapper.
	 */
	public static Floats2D toFloats2D(FImage fimage, boolean copy) {
		if (copy) {
			return new Floats2D(Floats2D.copy(fimage.pixels));
		}
		return new Floats2D(fimage.pixels);
	}

	/**
	 * Converts {@code Floats2D} to a {@code FImage}.
	 *
	 * @param floats the samples.
	 * @param copy {@code true} to make a copy of the underlying array,
	 * {@code false} to use the same underlying array, passing along just a
	 * pointer.
	 * @return a {@code FImage}.
	 */
	public static FImage toFImage(Floats2D floats, boolean copy) {
		if (copy) {
			return new FImage(floats.copyData());
		}
		return new FImage(floats.data);
	}

	/**
	 * Converts a {@code MBFImage} to {@code MultiFloats2D}.
	 *
	 * @param mbfimage the multiband image.
	 * @param copy {@code true} to make a copy of the underlying array,
	 * {@code false} to use the same underlying array, passing along just a
	 * pointer.
	 * @return a {@code MultiFloats2D}.
	 */
	public static MultiFloats2D toMultiFloats2D(MBFImage mbfimage, boolean copy) {
		final List<Floats2D> bands = new ArrayList<>();
		for (FImage fimage : mbfimage.bands) {
			bands.add(toFloats2D(fimage, copy));
		}
		return new MultiFloats2D(bands);
	}

	/**
	 * Converts {@code MultiFloats2D} to a {@code MBFImage}.
	 *
	 * @param mfloats the samples.
	 * @param copy {@code true} to make a copy of the underlying array,
	 * {@code false} to use the same underlying array, passing along just a
	 * pointer.
	 * @return a {@code MBFImage}.
	 */
	public static MBFImage toMBFImage(MultiFloats2D mfloats, boolean copy) {
		final FImage[] bands = new FImage[mfloats.getNumBands()];
		for (int i = 0; i < mfloats.getNumBands(); i++) {
			bands[i] = toFImage(mfloats.get(i), copy);
		}
		return new MBFImage(bands);
	}

	/**
	 * Converts a {@code FImage} to a {@code BufferedImage} of type
	 * {@code TYPE_BYTE_BINARY}.
	 *
	 * @param fimage the image.
	 * @return a {@code BufferedImage} of type {@code TYPE_BYTE_BINARY}.
	 */
	public static BufferedImage toBinaryBufferedImage(FImage fimage) {
		return toBufferedImage(fimage, BufferedImage.TYPE_BYTE_BINARY);
	}

	/**
	 * Converts a {@code FImage} to a {@code BufferedImage} of type
	 * {@code TYPE_BYTE_GRAY}.
	 *
	 * @param fimage the image.
	 * @return a {@code BufferedImage} of type {@code TYPE_BYTE_GRAY}.
	 */
	public static BufferedImage toBufferedImage(FImage fimage) {
		return toBufferedImage(fimage, BufferedImage.TYPE_BYTE_GRAY);
	}

	/**
	 * Converts a {@code FImage} to a {@code BufferedImage}.
	 *
	 * @param fimage the image.
	 * @param type type of the {@code BufferedImage}.
	 * @return a {@code BufferedImage}.
	 */
	public static BufferedImage toBufferedImage(FImage fimage, int type) {
		final BufferedImage image = new BufferedImage(
				fimage.getWidth(),
				fimage.getHeight(),
				type
		);
		final WritableRaster raster = image.getRaster();
		for (int y = 0; y < fimage.getHeight(); y++) {
			raster.setSamples(
					0,
					y,
					fimage.getWidth(),
					1,
					0,
					fimage.pixels[y]
			);
		}
		return image;
	}

	/**
	 * Converts a {@code MBFImage} to a {@code BufferedImage}. The type of the
	 * {@code BufferedImage} is {@code TYPE_INT_RGB} with 3 bands (or less), or
	 * {@code TYPE_INT_ARGB} with 4 bands.
	 *
	 * @param mbfimage the image.
	 * @return a {@code BufferedImage}.
	 */
	public static BufferedImage toBufferedImage(MBFImage mbfimage) {
		return toBufferedImage(
				mbfimage,
				(mbfimage.numBands() > 3) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB
		);
	}

	/**
	 * Converts a {@code MBFImage} to a {@code BufferedImage}
	 *
	 * @param mbfimage the image.
	 * @param type type of the {@code BufferedImage}.
	 * @return a {@code BufferedImage}.
	 */
	public static BufferedImage toBufferedImage(MBFImage mbfimage, int type) {
		final List<FImage> bands = mbfimage.bands;
		final BufferedImage image = new BufferedImage(
				mbfimage.getWidth(),
				mbfimage.getHeight(),
				type
		);
		final WritableRaster raster = image.getRaster();
		for (int b = 0; b < bands.size(); b++) {
			final FImage band = mbfimage.getBand(b);
			for (int y = 0; y < mbfimage.getHeight(); y++) {
				raster.setSamples(
						0,
						y,
						mbfimage.getWidth(),
						1,
						b,
						band.pixels[y]
				);
			}
		}
		return image;
	}

	/**
	 * Converts a {@code FImage} to a {@code BufferedMatrix} (single-band,
	 * single-precision float, BSQ).
	 *
	 * @param fimage the image.
	 * @return a {@code BufferedMatrix}.
	 */
	public static BufferedMatrix toBufferedMatrix(FImage fimage) {
		final BufferedMatrix mat = new BufferedMatrix(
				fimage.getWidth(),
				fimage.getHeight(),
				1,
				BufferedMatrix.DataType.FLOAT,
				BufferedMatrix.Interleave.BSQ
		);
		final WritableRaster raster = mat.getRaster();
		for (int y = 0; y < fimage.getHeight(); y++) {
			raster.setSamples(
					0,
					y,
					fimage.getWidth(),
					1,
					0,
					fimage.pixels[y]
			);
		}
		return mat;
	}

	/**
	 * Converts a band of a {@code MBFImage} to a {@code BufferedMatrix}
	 * (single-band, single-precision float, BSQ).
	 *
	 * @param mbfimage the image.
	 * @param band the band to extract.
	 * @return a {@code BufferedMatrix}.
	 */
	public static BufferedMatrix toBufferedMatrix(MBFImage mbfimage, int band) {
		return toBufferedMatrix(mbfimage.getBand(band));
	}

	/**
	 * Converts a {@code MBFImage} to a {@code BufferedMatrix} (multi-band,
	 * single-precision float, BSQ).
	 *
	 * @param mbfimage the image.
	 * @return a {@code BufferedMatrix}.
	 */
	public static BufferedMatrix toBufferedMatrix(MBFImage mbfimage) {
		final List<FImage> bands = mbfimage.bands;
		final BufferedMatrix mat = new BufferedMatrix(
				mbfimage.getWidth(),
				mbfimage.getHeight(),
				bands.size(),
				BufferedMatrix.DataType.FLOAT,
				BufferedMatrix.Interleave.BSQ
		);
		final WritableRaster raster = mat.getRaster();
		for (int b = 0; b < bands.size(); b++) {
			final FImage band = mbfimage.getBand(b);
			for (int y = 0; y < mbfimage.getHeight(); y++) {
				raster.setSamples(
						0,
						y,
						mbfimage.getWidth(),
						1,
						b,
						band.pixels[y]
				);
			}
		}
		return mat;
	}

	/**
	 * Converts a {@code BufferedImage} (or a {@code BufferedMatrix}) to a
	 * {@code FImage}.
	 *
	 * @param <T> (sub-)class of {@code BufferedImage}.
	 * @param image the image.
	 * @param band the band to extract.
	 * @return a {@code FImage}.
	 */
	public static <T extends BufferedImage> FImage toFImage(T image, int band) {
		final float[] data = new float[image.getWidth() * image.getHeight()];
		image.getRaster().getSamples(
				0,
				0,
				image.getWidth(),
				image.getHeight(),
				band,
				data
		);
		return new FImage(data, image.getWidth(), image.getHeight());
	}

	/**
	 * Converts a {@code BufferedImage} (or a {@code BufferedMatrix}) to a
	 * {@code MBFImage}.
	 *
	 * @param <T> (sub-)class of {@code BufferedImage}.
	 * @param image the image.
	 * @return a {@code MBFImage}.
	 */
	public static <T extends BufferedImage> MBFImage toMBFImage(T image) {
		final int numBands = image.getSampleModel().getNumBands();
		final double[] data = new double[image.getWidth() * image.getHeight() * numBands];
		image.getRaster().getPixels(
				0,
				0,
				image.getWidth(),
				image.getHeight(),
				data
		);
		return new MBFImage(
				data,
				image.getWidth(),
				image.getHeight(),
				numBands,
				true
		);
	}

	/**
	 * Reads a {@code FImage} from the savefile. Expects the file to be stored
	 * as {@code BufferedMatrix}.
	 *
	 * @param context the processor context.
	 * @param filename the filename of the image.
	 * @return the image, or {@code null}.
	 */
	public static FImage readFImage(ProcessorContext context, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			return readFImage(file);
		} catch (IOException ex) {
			log.warn("failed to read file: {}", file, ex);
		}
		return null;
	}

	/**
	 * Reads a {@code FImage} from a file. Expects the file to be stored as
	 * {@code BufferedMatrix}.
	 *
	 * @param file the file.
	 * @return the image.
	 * @throws IOException
	 */
	public static FImage readFImage(Path file) throws IOException {
		final BufferedMatrix mat = BufferedIO.readMat(file);
		return toFImage(mat, 0);
	}

	/**
	 * Writes an {@code FImage} to the savefile. The file is written as
	 * {@code BufferedMatrix}.
	 *
	 * @param context the processor context.
	 * @param fimage the image.
	 * @param filename the filename of the image.
	 */
	public static void writeFImage(ProcessorContext context, FImage fimage, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			writeFImage(fimage, file);
		} catch (IOException ex) {
			log.warn("failed to write file: {}", file, ex);
		}
	}

	/**
	 * Writes an {@code FImage} to a file. The file is written as
	 * {@code BufferedMatrix}.
	 *
	 * @param fimage the image.
	 * @param file the file.
	 * @throws IOException
	 */
	public static void writeFImage(FImage fimage, Path file) throws IOException {
		final BufferedMatrix mat = toBufferedMatrix(fimage);
		BufferedIO.writeMat(mat, file);
	}

	/**
	 * Reads a {@code MBFImage} from the savefile. Expects the file to be stored
	 * as {@code BufferedMatrix}.
	 *
	 * @param context the processor context.
	 * @param filename the filename of the image.
	 * @return the image, or {@code null}.
	 */
	public static MBFImage readMBFImage(ProcessorContext context, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			return readMBFImage(file);
		} catch (IOException ex) {
			log.warn("failed to read file: {}", file, ex);
		}
		return null;
	}

	/**
	 * Reads a {@code MBFImage} from a file. Expects the file to be stored as
	 * {@code BufferedMatrix}.
	 *
	 * @param file the file.
	 * @return the image.
	 * @throws IOException
	 */
	public static MBFImage readMBFImage(Path file) throws IOException {
		final BufferedMatrix mat = BufferedIO.readMat(file);
		return toMBFImage(mat);
	}

	/**
	 * Writes an {@code MBFImage} to the savefile. The file is written as
	 * {@code BufferedMatrix}.
	 *
	 * @param context the processor context.
	 * @param mbfimage the image.
	 * @param filename the filename of the image.
	 */
	public static void writeMBFImage(ProcessorContext context, MBFImage mbfimage, String filename) {
		final Path file = context.getDirectory().resolve(filename);
		try {
			writeMBFImage(mbfimage, file);
		} catch (IOException ex) {
			log.warn("failed to write file: {}", file, ex);
		}
	}

	/**
	 * Writes an {@code MBFImage} to a file. The file is written as
	 * {@code BufferedMatrix}.
	 *
	 * @param mbfimage the image.
	 * @param file the file.
	 * @throws IOException
	 */
	public static void writeMBFImage(MBFImage mbfimage, Path file) throws IOException {
		final BufferedMatrix mat = toBufferedMatrix(mbfimage);
		BufferedIO.writeMat(mat, file);
	}

	/**
	 * Removes a file (if it exists).
	 *
	 * @param file the file.
	 * @return {@code true} if the file was deleted by this method,
	 * {@code false} otherwise.
	 */
	public static boolean deleteFile(Path file) {
		try {
			return Files.deleteIfExists(file);
		} catch (IOException ex) {
			log.warn("failed to remove processor file: {}", file, ex);
			return false;
		}
	}

}
