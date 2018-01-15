package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * {@code MultiImageOp} is an abstract base class implementation for arithmetic
 * (or logic) operations with multiple source images.
 */
public abstract class MultiImageOp extends NullOp implements InverseMappedTileParallelizable {

	protected final BufferedImage[] left;
	protected final SamplePrecision outputPrecision;
	protected final int outputNumBands;

	/**
	 * Creates a new multi image op.
	 *
	 * @param sources the (left) source images.
	 * @param outputNumBands the number of bands of the destination image.
	 */
	public MultiImageOp(BufferedImage[] sources, int outputNumBands) {
		this(sources, SamplePrecision.BYTE, outputNumBands);
	}

	/**
	 * Creates a new multi image op.
	 *
	 * @param sources the (left) source images.
	 * @param outputPrecision the sample precision of the destination image.
	 * @param outputNumBands the number of bands of the destination image.
	 */
	public MultiImageOp(BufferedImage[] sources, SamplePrecision outputPrecision, int outputNumBands) {
		this.left = sources;
		this.outputPrecision = outputPrecision;
		this.outputNumBands = outputNumBands;
	}

	/**
	 * Combines the source samples to the destination sample(s).
	 *
	 * @param samples_in the samples from the source images.
	 * @param samples_out the sample(s) of the destination image.
	 */
	public abstract void combine(float[] samples_in, float[] samples_out);

	@Override
	public BufferedImage filter(BufferedImage right, BufferedImage dst) {
		final BufferedImage[] sources = getSources(right);

		if (dst == null) {
			final Rectangle bounds = getIntersectionBounds(sources);
			dst = createCompatibleDestImage(
					bounds.width,
					bounds.height,
					outputPrecision,
					outputNumBands
			);
		}

		final WritableRaster[] srcRaster = getSourceRaster(sources);
		final WritableRaster dstRaster = dst.getRaster();

		// possible tile parallelizable offset (true if dstRaster.getParent() != null)
		final int offsetX = dstRaster.getSampleModelTranslateX();
		final int offsetY = dstRaster.getSampleModelTranslateY();

		final float[] samples_in = new float[left.length + 1];
		final float[] samples_out = new float[outputNumBands];

		for (Location pt : new RasterScanner(dstRaster, 1)) {
			readSamples(
					pt.col - offsetX,
					pt.row - offsetY,
					srcRaster,
					samples_in
			);

			combine(samples_in, samples_out);
			for (int band = 0; band < outputNumBands; band++) {
				dstRaster.setSample(
						pt.col,
						pt.row,
						band,
						samples_out[band]
				);
			}
		}

		return dst;
	}

	/**
	 * Returns the intersection of all images.
	 *
	 * @param sources the images.
	 * @return the intersection.
	 */
	public static Rectangle getIntersectionBounds(BufferedImage[] sources) {
		if (sources.length == 1) {
			return sources[0].getRaster().getBounds();
		}
		Rectangle bounds = sources[0].getRaster().getBounds();
		for (int i = 1; i < sources.length; i++) {
			bounds = bounds.intersection(sources[i].getRaster().getBounds());
		}
		return bounds;
	}

	protected void readSamples(int col, int row, WritableRaster[] srcRaster, float[] samples) {
		for (int i = 0; i < srcRaster.length; i++) {
			samples[i] = srcRaster[i].getSampleFloat(col, row, 0);
		}
	}

	protected BufferedImage[] getSources(BufferedImage right) {
		if (right == null) {
			return left;
		}
		final BufferedImage[] sources = new BufferedImage[left.length + 1];
		for (int i = 0; i < left.length; i++) {
			sources[i] = left[i];
		}
		sources[left.length] = right;
		return sources;
	}

	protected WritableRaster[] getSourceRaster(BufferedImage[] sources) {
		final WritableRaster[] raster = new WritableRaster[sources.length];
		for (int i = 0; i < sources.length; i++) {
			raster[i] = sources[i].getRaster();
		}
		return raster;
	}

}
