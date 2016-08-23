package ch.unifr.diva.dip.benchmarks;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Fx Image writer benchmark. Comparing sample/pixel access of JavaFX Image and
 * AWT BufferedImage.
 */
public class FxImageWriterBenchmark {

	@State(Scope.Benchmark)
	public static class Resources {

		@Param({"256", "512", "1024", "2048", "4096"})
		int size;

		// let's say we have them already allocated... (since we can not directly
		// access the Image's buffer as with BufferedImages.
		int[] intBuffer;
		byte[] byteBuffer;

		@Setup
		public void setup() {
			intBuffer = new int[size * size];
			byteBuffer = new byte[size * size * 4];
		}
	}

	// new WritableImages are: BYTE_BGRA_PRE, isPremultiplied==TRUE
	// 0..255
	public static int sample() {
		return (int) (Math.random() * 255);
	}

	public static byte sampleByte() {
		return (byte) (Math.random() * 255);
	}

	// 0..1
	public static double sampleN() {
		return Math.random();
	}

	// map 2d coords. to linear indexing
	public static int index(int row, int col, int width) {
		return col * width + row;
	}

	public static int index(int row, int col, int width, int band) {
		return col * width * 4 + row * 4 + band;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage awtSetSample(Resources resources) {
		final BufferedImage image = new BufferedImage(resources.size, resources.size, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		final WritableRaster raster = image.getRaster();

		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				for (int i = 0; i < 4; i++) {
					raster.setSample(x, y, i, sample());
				}
			}
		}

		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage awtSetPixel(Resources resources) {
		final BufferedImage image = new BufferedImage(resources.size, resources.size, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		final WritableRaster raster = image.getRaster();

		final int[] pixel = new int[4];
		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				for (int i = 0; i < 4; i++) {
					pixel[i] = sample();
				}
				raster.setPixel(x, y, pixel);
			}
		}

		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage awtSetDataBufferInt(Resources resources) {
		final BufferedImage image = new BufferedImage(resources.size, resources.size, BufferedImage.TYPE_INT_ARGB_PRE);

		final DataBufferInt buffer = (DataBufferInt) image.getRaster().getDataBuffer();
		final int pixels = resources.size * resources.size;

		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				final int r = sample();
				final int g = sample();
				final int b = sample();
				final int a = sample();
				final int pixel = b | (g << 8) | (r << 16) | (a << 24);
				buffer.setElem(index(x, y, resources.size), pixel);
			}
		}

		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage awtSetDataArrayInt(Resources resources) {
		final BufferedImage image = new BufferedImage(resources.size, resources.size, BufferedImage.TYPE_INT_ARGB_PRE);

		final int[] buffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				final int r = sample();
				final int g = sample();
				final int b = sample();
				final int a = sample();
				final int pixel = b | (g << 8) | (r << 16) | (a << 24);
				buffer[index(x, y, resources.size)] = pixel;
			}
		}

		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage awtSetDataBufferByte(Resources resources) {
		final BufferedImage image = new BufferedImage(resources.size, resources.size, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		final DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();

		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				for (int i = 0; i < 4; i++) {
					buffer.setElem(index(x, y, resources.size, i), sample());
				}
			}
		}

		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage fxArgb(Resources resources) {
		final WritableImage image = new WritableImage(resources.size, resources.size);
		final PixelWriter writer = image.getPixelWriter();

		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				final int r = sample();
				final int g = sample();
				final int b = sample();
				final int a = sample();
				final int pixel = b | (g << 8) | (r << 16) | (a << 24);
				writer.setArgb(x, y, pixel);
			}
		}

		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage fxColor(Resources resources) {
		final WritableImage image = new WritableImage(resources.size, resources.size);
		final PixelWriter writer = image.getPixelWriter();

		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				final Color color = new Color(
						sampleN(),
						sampleN(),
						sampleN(),
						sampleN()
				);
				writer.setColor(x, y, color);
			}
		}

		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage fxIntBuffer(Resources resources) {
		final WritableImage image = new WritableImage(resources.size, resources.size);
		final PixelWriter writer = image.getPixelWriter();
		final WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbPreInstance();
		final int[] buffer = new int[resources.size * resources.size];

		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				final int r = sample();
				final int g = sample();
				final int b = sample();
				final int a = sample();
				final int pixel = b | (g << 8) | (r << 16) | (a << 24);
				buffer[index(x, y, resources.size)] = pixel;
			}
		}

		writer.setPixels(0, 0, resources.size, resources.size, format, buffer, 0, resources.size);
		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage fxIntBufferCached(Resources resources) {
		final WritableImage image = new WritableImage(resources.size, resources.size);
		final PixelWriter writer = image.getPixelWriter();
		final WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbPreInstance();

		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				final int r = sample();
				final int g = sample();
				final int b = sample();
				final int a = sample();
				final int pixel = b | (g << 8) | (r << 16) | (a << 24);
				resources.intBuffer[index(x, y, resources.size)] = pixel;
			}
		}

		writer.setPixels(0, 0, resources.size, resources.size, format, resources.intBuffer, 0, resources.size);
		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage fxByteBuffer(Resources resources) {
		final WritableImage image = new WritableImage(resources.size, resources.size);
		final PixelWriter writer = image.getPixelWriter();
		final WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraPreInstance();
		final byte[] buffer = new byte[resources.size * resources.size * 4];

		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				for (int i = 0; i < 4; i++) {
					buffer[index(x, y, resources.size, i)] = sampleByte();
				}
			}
		}

		writer.setPixels(0, 0, resources.size, resources.size, format, buffer, 0, resources.size * 4);
		return image;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage fxByteBufferCached(Resources resources) {
		final WritableImage image = new WritableImage(resources.size, resources.size);
		final PixelWriter writer = image.getPixelWriter();
		final WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraPreInstance();

		for (int y = 0; y < resources.size; y++) {
			for (int x = 0; x < resources.size; x++) {
				for (int i = 0; i < 4; i++) {
					resources.byteBuffer[index(x, y, resources.size, i)] = sampleByte();
				}
			}
		}

		writer.setPixels(0, 0, resources.size, resources.size, format, resources.byteBuffer, 0, resources.size * 4);
		return image;
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(FxImageWriterBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.measurementIterations(5)
				.forks(1)
				.build();

		Collection<RunResult> results = new Runner(opt).run();

		BenchmarkUtils.printRunResults(
				results,
				FxImageWriterBenchmark.class.getSimpleName()
		);
	}

}
