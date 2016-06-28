package ch.unifr.diva.dip.api.imaging;

import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * I/O utilities for BufferedMatrix.
 */
public class BufferedIO {

	/**
	 * Official file extension for binary BufferedMatrix files.
	 */
	public final static String BUFFERED_MATRIX_FILE_EXTENSION = "bmat";

	/**
	 * The header id is written first, and is used to decide whether a file can
	 * be decoded or not.
	 */
	private final static String HEADER_ID = "BMAT";

	/**
	 * Encoding of strings if converted to bytes. US-ASCII is good enough, and
	 * get's us a nice one byte per char.
	 */
	private final static String STRING_ENCODING = "US-ASCII";

	private BufferedIO() {
		// nope.
	}

	/**
	 * Writes chars (in US-ASCII) to a DataOutputStream. Chars are prepended by
	 * an int encoding the length of the string (or how many chars to read back;
	 * namely one byte a char).
	 *
	 * @param out the DataOutputStream.
	 * @param string the ASCII string to write.
	 * @throws IOException
	 */
	private static void writeChars(DataOutputStream out, String string) throws IOException {
		out.writeInt(string.length());
		out.write(string.getBytes(STRING_ENCODING));
	}

	/**
	 * Reads chars (in US-ASCII) from a DataInputStream at the current position.
	 * Chars are prepended by an int encoding the length of the string. So first
	 * that int is read, to read as many bytes/chars as declared.
	 *
	 * @param in the DataInputStream.
	 * @return an ASCII string.
	 * @throws IOException
	 */
	private static String readChars(DataInputStream in) throws IOException {
		final int length = in.readInt();
		final byte[] bytes = new byte[length];
		in.readFully(bytes);
		return new String(bytes, STRING_ENCODING);
	}

	/**
	 * Writes a BufferedMatrix in a binary format.
	 *
	 * @param mat the BufferedMatrix to be saved/written to disk.
	 * @param path path to save/write the BufferedMatrix to.
	 * @throws IOException
	 */
	public static void writeMat(BufferedMatrix mat, Path path) throws IOException {
		if (Files.exists(path)) {
			Files.delete(path);
		}

		final OutputStream os = new FileOutputStream(path.toFile());
		writeMat(mat, new BufferedOutputStream(os));
	}

	/**
	 * Writes a BufferedMatrix in a binary format to an output stream.
	 *
	 * @param mat the BufferedMatrix to be saved/written to disk.
	 * @param os the output stream to write to.
	 * @throws IOException
	 */
	public static void writeMat(BufferedMatrix mat, OutputStream os) throws IOException {
		final int width = mat.getWidth();
		final int height = mat.getHeight();
		final int bands = mat.getRaster().getNumBands();
		final BufferedMatrix.DataType type = mat.getSampleDataType();
		final BufferedMatrix.Interleave interleave = mat.getInterleave();

		try (DataOutputStream out = new DataOutputStream(os)) {
			// write header
			out.write(HEADER_ID.getBytes(STRING_ENCODING));

			out.writeInt(width);
			out.writeInt(height);
			out.writeInt(bands);
			writeChars(out, type.name());
			writeChars(out, interleave.name());

			// write samples
			final int numPixels = width * height;
			final int numSamples = numPixels * bands;
			final int bytesPerSample = mat.getBytesPerSample();
			final ByteBuffer buffer;

			switch (interleave) {
				case BIP:
					buffer = ByteBuffer.allocate(numSamples * bytesPerSample);
					switch (type) {
						case INT:
							buffer.asIntBuffer().put(mat.getPixelsInt());
							break;

						case FLOAT:
							buffer.asFloatBuffer().put(mat.getPixelsFloat());
							break;

						case DOUBLE:
							buffer.asDoubleBuffer().put(mat.getPixelsDouble());

						default:
							throw new IllegalArgumentException(
									"Don't know how to write: " + type.name()
							);
					}
					out.write(buffer.array());
					break;

				case BSQ:
					buffer = ByteBuffer.allocate(numPixels * bytesPerSample);
					switch (type) {
						case INT:
							for (int i = 0; i < bands; i++) {
								buffer.asIntBuffer().put(mat.getSamplesInt(i));
								out.write(buffer.array());
								buffer.clear();
							}
							break;

						case FLOAT:
							for (int i = 0; i < bands; i++) {
								buffer.asFloatBuffer().put(mat.getSamplesFloat(i));
								out.write(buffer.array());
								buffer.clear();
							}
							break;

						case DOUBLE:
							for (int i = 0; i < bands; i++) {
								buffer.asDoubleBuffer().put(mat.getSamplesDouble(i));
								out.write(buffer.array());
								buffer.clear();
							}
							break;

						default:
							throw new IllegalArgumentException(
									"Don't know how to write: " + type.name()
							);
					}
					break;

				default:
					throw new IllegalArgumentException(
							"Don't know how to write: " + interleave.name()
					);
			}
		}
	}

	/**
	 * Reads a BufferdMatrix in binary format.
	 *
	 * @param path path to the binary file.
	 * @return a BufferedMatrix.
	 * @throws IOException
	 */
	public static BufferedMatrix readMat(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new IOException("Can't read input file (does not exist)!");
		}

		final InputStream is = new FileInputStream(path.toFile());
		return readMat(new BufferedInputStream(is));
	}

	/**
	 * Reads a BufferedMatrix in a binary format from an input stream.
	 *
	 * @param is the input stream to read from.
	 * @return a BufferedMatrix.
	 * @throws IOException
	 */
	public static BufferedMatrix readMat(InputStream is) throws IOException {
		BufferedMatrix mat;

		try (DataInputStream in = new DataInputStream(is)) {
			// read header
			final byte[] id_bytes = HEADER_ID.getBytes(STRING_ENCODING);
			final byte[] mat_bytes = new byte[HEADER_ID.length()];
			in.readFully(mat_bytes);
			for (int i = 0; i < mat_bytes.length; i++) {
				if (id_bytes[i] != mat_bytes[i]) {
					throw new InvalidObjectException("Invalid header (unknown format)");
				}
			}

			final int width = in.readInt();
			final int height = in.readInt();
			final int bands = in.readInt();

			BufferedMatrix.DataType type;
			try {
				type = BufferedMatrix.DataType.valueOf(readChars(in));
			} catch (IllegalArgumentException ex) {
				throw new InvalidObjectException("Invalid header (unknown type)");
			}

			BufferedMatrix.Interleave interleave;
			try {
				interleave = BufferedMatrix.Interleave.valueOf(readChars(in));
			} catch (IllegalArgumentException ex) {
				throw new InvalidObjectException("Invalid header (unknown interleave)");
			}

			// read samples
			mat = new BufferedMatrix(width, height, bands, type, interleave);
			WritableRaster raster = mat.getRaster();

			final int numPixels = width * height;
			final int numSamples = numPixels * bands;
			final int bytesPerSample = mat.getBytesPerSample();
			final byte[] bytes;
			ByteBuffer buffer;

			switch (interleave) {
				case BIP:
					bytes = new byte[numSamples * bytesPerSample];
					in.readFully(bytes);
					buffer = ByteBuffer.wrap(bytes);

					switch (type) {
						case INT:
							final int[] ints = new int[numSamples];
							buffer.asIntBuffer().get(ints);
							raster.setPixels(0, 0, width, height, ints);
							break;

						case FLOAT:
							final float[] floats = new float[numSamples];
							buffer.asFloatBuffer().get(floats);
							raster.setPixels(0, 0, width, height, floats);
							break;

						case DOUBLE:
							final double[] doubles = new double[numSamples];
							buffer.asDoubleBuffer().get(doubles);
							raster.setPixels(0, 0, width, height, doubles);
							break;

						default:
							throw new IllegalArgumentException(
									"Don't know how to read: " + type.name()
							);
					}
					break;

				case BSQ:
					bytes = new byte[numPixels * bytesPerSample];

					switch (type) {
						case INT:
							final int[] ints = new int[numPixels];
							for (int i = 0; i < bands; i++) {
								in.readFully(bytes);
								buffer = ByteBuffer.wrap(bytes);
								buffer.asIntBuffer().get(ints);
								raster.setSamples(0, 0, width, height, i, ints);
							}
							break;

						case FLOAT:
							final float[] floats = new float[numPixels];
							for (int i = 0; i < bands; i++) {
								in.readFully(bytes);
								buffer = ByteBuffer.wrap(bytes);
								buffer.asFloatBuffer().get(floats);
								raster.setSamples(0, 0, width, height, i, floats);
							}
							break;

						case DOUBLE:
							final double[] doubles = new double[numPixels];
							for (int i = 0; i < bands; i++) {
								in.readFully(bytes);
								buffer = ByteBuffer.wrap(bytes);
								buffer.asDoubleBuffer().get(doubles);
								raster.setSamples(0, 0, width, height, i, doubles);
							}
							break;

						default:
							throw new IllegalArgumentException(
									"Don't know how to read: " + type.name()
							);
					}
					break;

				default:
					throw new IllegalArgumentException(
							"Don't know how to read: " + interleave.name()
					);
			}
		}

		return mat;
	}

}
