package ch.unifr.diva.dip.awt.imaging.padders;

import java.awt.image.BufferedImage;

/**
 * An image padder with reflective indexing. Represents a read-only image that
 * is infinite in extend. The reflective padder extends the source image by
 * mirroring it through the use of reflective indexing.
 */
public class ReflectivePadder extends TiledPadder {

	@Override
	protected int column(BufferedImage src, int column) {
		if (column < 0) {
			column = -1 - column;
		}

		if ((column / src.getWidth()) % 2 == 0) {
			column %= src.getWidth();
		} else {
			column = src.getWidth() - 1 - column % src.getWidth();
		}

		return column;
	}

	@Override
	protected int row(BufferedImage src, int row) {
		if (row < 0) {
			row = -1 - row;
		}

		if ((row / src.getHeight()) % 2 == 0) {
			row %= src.getHeight();
		} else {
			row = src.getHeight() - 1 - row % src.getHeight();
		}

		return row;
	}

}
