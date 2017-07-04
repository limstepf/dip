package ch.unifr.diva.dip.awt.imaging.padders;

import java.awt.image.BufferedImage;

/**
 * An image padder with circular indexing. Represents a read-only image that is
 * infinite in extend. The tiled padder extends the source image by tiling it
 * using circular indexing.
 */
public class TiledPadder implements ImagePadder {

	protected int column(BufferedImage src, int column) {
		column = column % src.getWidth();

		if (column < 0) {
			column += src.getWidth();
		}

		return column;
	}

	protected int row(BufferedImage src, int row) {
		row = row % src.getHeight();

		if (row < 0) {
			row += src.getHeight();
		}

		return row;
	}

	@Override
	public int[] getPixel(BufferedImage src, int column, int row, int[] iArray) {
		return src.getRaster().getPixel(column(src, column), row(src, row), iArray);
	}

	@Override
	public float[] getPixel(BufferedImage src, int column, int row, float[] iArray) {
		return src.getRaster().getPixel(column(src, column), row(src, row), iArray);
	}

	@Override
	public double[] getPixel(BufferedImage src, int column, int row, double[] iArray) {
		return src.getRaster().getPixel(column(src, column), row(src, row), iArray);
	}

	@Override
	public int getSample(BufferedImage src, int column, int row, int band) {
		return src.getRaster().getSample(column(src, column), row(src, row), band);
	}

	@Override
	public float getSampleFloat(BufferedImage src, int column, int row, int band) {
		return src.getRaster().getSampleFloat(column(src, column), row(src, row), band);
	}

	@Override
	public double getSampleDouble(BufferedImage src, int column, int row, int band) {
		return src.getRaster().getSampleDouble(column(src, column), row(src, row), band);
	}

}
