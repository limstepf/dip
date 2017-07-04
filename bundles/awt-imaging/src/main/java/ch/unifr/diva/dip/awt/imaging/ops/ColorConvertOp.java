package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
import ch.unifr.diva.dip.awt.imaging.SimpleColorModel;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Color conversion from and to known SimpleColorModels.
 */
public class ColorConvertOp extends NullOp implements TileParallelizable {

	private final SimpleColorModel srcCm;
	private final SimpleColorModel dstCm;

	/**
	 * Creates a new color conversion filter.
	 *
	 * @param srcCm the simple color model of the source image.
	 * @param dstCm the simple color model of the destination image.
	 */
	public ColorConvertOp(SimpleColorModel srcCm, SimpleColorModel dstCm) {
		this.srcCm = srcCm;
		this.dstCm = dstCm;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, dstCm);
		}

		final WritableRaster srcRaster = src.getRaster();
		final WritableRaster dstRaster = dst.getRaster();

		final float[] srcPx = new float[ImagingUtils.numBands(src)];
		final float[] dstPx = new float[ImagingUtils.numBands(dst)];

		for (Location pt : new RasterScanner(src, false)) {
			srcRaster.getPixel(pt.col, pt.row, srcPx);
			dstRaster.setPixel(pt.col, pt.row, srcCm.convertTo(dstCm, srcPx, dstPx));
		}

		return dst;
	}

	/**
	 * Creates a compatible destination image for the color conversion.
	 *
	 * @param src the source image.
	 * @param cm the simple color model of the destination image.
	 * @return a compatible destination image for the color conversion.
	 */
	public BufferedImage createCompatibleDestImage(BufferedImage src, SimpleColorModel cm) {
		if (cm.dataType().type().equals(BufferedMatrix.class)) {
			return new BufferedMatrix(src.getWidth(), src.getHeight(), cm.numBands());
		}
		return new BufferedImage(src.getWidth(), src.getHeight(), getCompatibleBufferdImageType(cm));
	}

}
