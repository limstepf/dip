package ch.unifr.diva.dip.api.datastructures;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.SampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * A BufferedMatrix is a BufferedImage with abritrary number of bands,
 * user-defined sample precision, and an undefined color space.
 */
public class BufferedMatrix extends BufferedImage {

	// don't accelerate this image, won't be displayed (directly) anyways...
	protected float accelerationPriority = 0.0f;

	/**
	 * Raster (or SampleModel) interleave. This defines how samples are ordererd
	 * in the raster/data buffer (possibly with multiple banks, e.g. with BSQ).
	 */
	public enum Interleave {
		// TODO: this could need some micro-benchmarking. Figure out what works best
		// with typical image processing tasks, or just color conversions...
		// - BSQ seems to do better at I/O (which is rather critical in DIP).
		//   It might also help that we divide the buffer by 3, as opposed to BIP.

		/**
		 * Band-Interleaved-by-Pixel (BIP). Good for pixel access (w. all
		 * samples).
		 */
		BIP,
		/**
		 * Band-Sequential (BSQ) interleave. Good for accessing each band (in
		 * sequence).
		 */
		BSQ
	}

	/**
	 * Data type of a sample/single component.
	 */
	public enum DataType {
		// Currently supported (make sure BufferedIO can deal with added types)

		/**
		 * Integer. 4 bytes (or 32 bits) per sample.
		 */
		INT(3),
		/**
		 * Float. 4 bytes (or 32 bits) per sample.
		 */
		FLOAT(4),
		/**
		 * Double. 8 bytes (or 64 bits) per sample.
		 */
		DOUBLE(5);

		/**
		 * Data type ordinal as defined on java.awt.image.DataBuffer. 0=byte,
		 * 1=ushort, 2=short, 3=int, 4=float, 5=double, 32=undefined.
		 */
		public final int dataBufferOrdinal;

		DataType(int dataBufferOrdinal) {
			this.dataBufferOrdinal = dataBufferOrdinal;
		}
	}

	// alpha channel doesn't really make sense with undefined color space...
	private final static boolean HAS_ALPHA = false;
	private final static boolean IS_ALPHA_PREMULTIPLIED = false;

	protected final Interleave INTERLEAVE;
	protected final DataType DATA_TYPE;
	protected final int width;
	protected final int height;
	protected final int bands;

	/**
	 * Returns the number of bands (or matrices).
	 *
	 * @return number of bands (or matrices).
	 */
	public int getNumBands() {
		return bands;
	}

	/**
	 * Returns the interleave method.
	 *
	 * @return the interleave method.
	 */
	public Interleave getInterleave() {
		return this.INTERLEAVE;
	}

	/**
	 * Returns the data type of the samples. This is a constant defined on the
	 * class java.awt.image.DataBuffer (0=byte, 1=ushort, 2=short, 3=int,
	 * 4=float, 5=double, 32=undefined).
	 *
	 * @return the data type of the samples.
	 * @see java.awt.image.DataBuffer
	 */
	public DataType getSampleDataType() {
		return this.DATA_TYPE;
	}

	/**
	 * Returns the used sample precision in bytes.
	 *
	 * @return bytes per sample/component.
	 */
	public int getBytesPerSample() {
		return getBytesPerSample(DATA_TYPE);
	}

	/**
	 * Returns the sample precision in bytes of the given data type.
	 *
	 * @param dataType used data type.
	 * @return bytes per sample/component.
	 */
	public static int getBytesPerSample(DataType dataType) {
		switch (dataType.dataBufferOrdinal) {
			case 0: // byte
				return 1;

			case 1: // ushort
			case 2: // short
				return 2;

			case 3: // int
				return 4;

			case 4: // float
				return 4;

			case 5: // double
				return 8;

			case 32: // undefined
			default:
				return -1;
		}
	}

	/**
	 * Returns an array of all integer samples over all pixels.
	 *
	 * @return an array of all integer samples.
	 */
	public int[] getPixelsInt() {
		final int[] ints = new int[width * height * bands];
		this.getRaster().getPixels(0, 0, width, height, ints);
		return ints;
	}

	/**
	 * Returns an array of all float samples over all pixels.
	 *
	 * @return an array of all float samples.
	 */
	public float[] getPixelsFloat() {
		final float[] floats = new float[width * height * bands];
		this.getRaster().getPixels(0, 0, width, height, floats);
		return floats;
	}

	/**
	 * Returns an array of all double samples over all pixels.
	 *
	 * @return an array of all double samples.
	 */
	public double[] getPixelsDouble() {
		final double[] doubles = new double[width * height * bands];
		this.getRaster().getPixels(0, 0, width, height, doubles);
		return doubles;
	}

	/**
	 * Returns an array of all integer samples of a specific band.
	 *
	 * @param band the band to get the samples from.
	 * @return an array of all integer samples of a band.
	 */
	public int[] getSamplesInt(int band) {
		final int[] ints = new int[width * height];
		this.getRaster().getSamples(0, 0, width, height, band, ints);
		return ints;
	}

	/**
	 * Returns an array of all float samples of a specific band.
	 *
	 * @param band the band to get the samples from.
	 * @return an array of all float samples of a band.
	 */
	public float[] getSamplesFloat(int band) {
		final float[] floats = new float[width * height];
		this.getRaster().getSamples(0, 0, width, height, band, floats);
		return floats;
	}

	/**
	 * Returns an array of all double samples of a specific band.
	 *
	 * @param band the band to get the samples from.
	 * @return an array of all double samples of a band.
	 */
	public double[] getSamplesDouble(int band) {
		final double[] doubles = new double[width * height];
		this.getRaster().getSamples(0, 0, width, height, band, doubles);
		return doubles;
	}

	/**
	 * Constructs a {@code BufferedMatrix}. Samples are of single-precision
	 * floating points (32-bit floats) using a Band-Sequential (BSQ) interleave
	 * raster/data buffer.
	 *
	 * @param width width of the created image.
	 * @param height height of the created image.
	 * @param bands number of bands of the created image.
	 */
	public BufferedMatrix(int width, int height, int bands) {
		this(
				width,
				height,
				bands,
				DataType.FLOAT,
				Interleave.BSQ
		);
	}

	/**
	 * Constructs a {@code BufferedMatrix}.
	 *
	 * @param width width of the created image.
	 * @param height height of the created image.
	 * @param bands number of bands of the created image.
	 * @param dataType sample precision (use {@code DataBuffer.*} constants
	 * (e.g. DataBuffer.TYPE_FLOAT, or DataBuffer.TYPE_DOUBLE).
	 * @param interleave Interleave mode.
	 */
	public BufferedMatrix(int width, int height, int bands, DataType dataType, Interleave interleave) {
		super(
				newMatrixColorModel(
						bands, dataType.dataBufferOrdinal,
						HAS_ALPHA, IS_ALPHA_PREMULTIPLIED
				),
				newMatrixRaster(
						width, height, bands,
						dataType.dataBufferOrdinal, interleave
				),
				IS_ALPHA_PREMULTIPLIED,
				null
		);

		this.INTERLEAVE = interleave;
		this.DATA_TYPE = dataType;
		this.width = width;
		this.height = height;
		this.bands = bands;
	}

	/**
	 * ColorModel.
	 */
	private static ColorModel newMatrixColorModel(int bands, int dataType, boolean hasAlpha, boolean isAlphaPremultiplied) {
		return new ComponentColorModel(
				newMatrixColorSpace(bands),
				hasAlpha,
				isAlphaPremultiplied,
				hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
				dataType
		);
	}

	/**
	 * ColorSpace. We don't care. BufferedMatrices aren't really supposed to be
	 * displayed.
	 */
	private static ColorSpace newMatrixColorSpace(int bands) {
		return new ColorSpace(ColorSpace.CS_sRGB, bands) {
			private static final long serialVersionUID = -3747231367879800411L;

			// just in case some jokester tries to directly display this matrix
			// we make sure that no java.lang.ArrayIndexOutOfBoundsException is
			// thrown in toRGB/toCIEXYZ.
			private final float[] invalid = new float[]{0.0f, 0.0f, 0.0f};

			@Override
			public float[] toRGB(float[] floats) {
				if (floats.length < 3) {
					return invalid;
				}
				return floats;
			}

			@Override
			public float[] fromRGB(float[] floats) {
				return floats;
			}

			@Override
			public float[] toCIEXYZ(float[] floats) {
				if (floats.length < 3) {
					return invalid;
				}
				return floats;
			}

			@Override
			public float[] fromCIEXYZ(float[] floats) {
				return floats;
			}
		};
	}

	/**
	 * WritableRaster.
	 */
	private static WritableRaster newMatrixRaster(int width, int height, int bands, int dataType, Interleave interleave) {
		return Raster.createWritableRaster(
				newMatrixSampleModel(width, height, bands, dataType, interleave),
				newMatrixDataBuffer(width, height, bands, interleave),
				null
		);
	}

	/**
	 * SampleModel.
	 */
	private static SampleModel newMatrixSampleModel(int width, int height, int bands, int dataType, Interleave interleave) {
		switch (interleave) {
			case BSQ:
				return new BandedSampleModel(
						dataType,
						width,
						height,
						bands
				);

			case BIP:
			default:
				return new PixelInterleavedSampleModel(
						dataType,
						width,
						height,
						bands, // pixel stride
						width * bands, // scanline stride
						newMatrixBandOffsetsBIP(bands)
				);
		}
	}

	/**
	 * BIP band offsets.
	 */
	private static int[] newMatrixBandOffsetsBIP(int bands) {
		final int[] offsets = new int[bands];
		for (int i = 0; i < bands; i++) {
			offsets[i] = i;
		}
		return offsets;
	}

	/**
	 * DataBuffer.
	 */
	private static DataBuffer newMatrixDataBuffer(int width, int height, int bands, Interleave interleave) {
		switch (interleave) {
			case BSQ:
				return new DataBufferFloat(width * height, bands);
			case BIP:
			default:
				return new DataBufferFloat(width * height * bands);
		}
	}
}
