package ch.unifr.diva.dip.api.imaging.padders;

import java.awt.image.BufferedImage;

/**
 * An image padder with clamped indexing. Represents a read-only image that is
 * infinite in extend. The extended border padder extends the source image with
 * repeated border samples.
 */
public class ExtendedBorderPadder implements ImagePadder {

	protected int clamp(int value, int max) {
		if (value < 0) {
			return 0;
		}
		if (value >= max) {
			return max - 1;
		}
		return value;
	}

	@Override
	public int[] getPixel(BufferedImage src, int column, int row, int[] iArray) {
		return src.getRaster().getPixel(
				clamp(column, src.getWidth()),
				clamp(row, src.getHeight()),
				iArray
		);
	}

	@Override
	public float[] getPixel(BufferedImage src, int column, int row, float[] iArray) {
		return src.getRaster().getPixel(
				clamp(column, src.getWidth()),
				clamp(row, src.getHeight()),
				iArray
		);
	}

	@Override
	public double[] getPixel(BufferedImage src, int column, int row, double[] iArray) {
		return src.getRaster().getPixel(
				clamp(column, src.getWidth()),
				clamp(row, src.getHeight()),
				iArray
		);
	}

	@Override
	public int getSample(BufferedImage src, int column, int row, int band) {
		return src.getRaster().getSample(
				clamp(column, src.getWidth()),
				clamp(row, src.getHeight()),
				band
		);
	}

	@Override
	public float getSampleFloat(BufferedImage src, int column, int row, int band) {
		return src.getRaster().getSampleFloat(
				clamp(column, src.getWidth()),
				clamp(row, src.getHeight()),
				band
		);
	}

	@Override
	public double getSampleDouble(BufferedImage src, int column, int row, int band) {
		return src.getRaster().getSampleDouble(
				clamp(column, src.getWidth()),
				clamp(row, src.getHeight()),
				band
		);
	}

}
