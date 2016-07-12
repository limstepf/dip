package ch.unifr.diva.dip.api.imaging.padders;

import java.awt.image.BufferedImage;

/**
 * An image padder with zero padding. Represents a read-only image that is
 * infinite in extend. The zero padder extends the source image with zero-valued
 * sampled, or a color border (black).
 */
public class ZeroPadder implements ImagePadder {

	protected boolean outOfBounds(BufferedImage src, int column, int row) {
		return (column < 0)
				|| (column >= src.getWidth())
				|| (row < 0)
				|| (row >= src.getHeight());
	}

	@Override
	public int[] getPixel(BufferedImage src, int column, int row, int[] iArray) {
		if (outOfBounds(src, column, row)) {
			return new int[src.getRaster().getNumBands()];
		}

		return src.getRaster().getPixel(column, row, iArray);
	}

	@Override
	public float[] getPixel(BufferedImage src, int column, int row, float[] iArray) {
		if (outOfBounds(src, column, row)) {
			return new float[src.getRaster().getNumBands()];
		}

		return src.getRaster().getPixel(column, row, iArray);
	}

	@Override
	public double[] getPixel(BufferedImage src, int column, int row, double[] iArray) {
		if (outOfBounds(src, column, row)) {
			return new double[src.getRaster().getNumBands()];
		}

		return src.getRaster().getPixel(column, row, iArray);
	}

	@Override
	public int getSample(BufferedImage src, int column, int row, int band) {
		if (outOfBounds(src, column, row)) {
			return 0;
		}

		return src.getRaster().getSample(column, row, band);
	}

	@Override
	public float getSampleFloat(BufferedImage src, int column, int row, int band) {
		if (outOfBounds(src, column, row)) {
			return 0;
		}

		return src.getRaster().getSampleFloat(column, row, band);
	}

	@Override
	public double getSampleDouble(BufferedImage src, int column, int row, int band) {
		if (outOfBounds(src, column, row)) {
			return 0;
		}

		return src.getRaster().getSampleDouble(column, row, band);
	}

}
