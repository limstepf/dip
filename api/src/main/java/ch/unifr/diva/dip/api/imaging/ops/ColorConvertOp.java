package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Color conversion from and to known SimpleColorModels.
 */
public class ColorConvertOp extends NullOp {

	private final SimpleColorModel srcCm;
	private final SimpleColorModel dstCm;

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

	public BufferedImage createCompatibleDestImage(BufferedImage src, SimpleColorModel cm) {
		if (cm.dataType().type().equals(BufferedMatrix.class)) {
			return new BufferedMatrix(src.getWidth(), src.getHeight(), cm.numBands());
		}
		return new BufferedImage(src.getWidth(), src.getHeight(), getCompatibleType(cm));
	}

	private int getCompatibleType(SimpleColorModel cm) {
		switch (cm.numBands()) {
			case 1:
				return BufferedImage.TYPE_BYTE_GRAY;

			case 4:
				return BufferedImage.TYPE_INT_ARGB;

			default:
				return BufferedImage.TYPE_INT_RGB;
		}
	}
}
