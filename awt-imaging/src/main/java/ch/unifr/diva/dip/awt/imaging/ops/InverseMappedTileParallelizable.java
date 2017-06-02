package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.scanners.ImageTiler;
import ch.unifr.diva.dip.awt.imaging.scanners.SimpleImageTiler;
import java.awt.image.BufferedImage;

/**
 * The inverse mapped tile parallelizable interface marks a
 * {@code BufferedImageOp} to be compatible with being wrapped and run by
 * {@code ConcurrentOp}. While we still work with the assumption that an
 * implementing {@code BufferedImageOp} should not rely on the order of pixel
 * scanning (it's still a tiled approach afterall), this time it's fine to rely
 * on image geometry.
 *
 * <p>
 * This is achieved by tiling the destination image (which might have a
 * different size as the source image) only, while each worker get's the full
 * copy of the (read-only) source image.
 *
 * <p>
 * Implementing ops will have to check if the raster of the destination image
 * has a parent raster (if {@code dst.getRaster().getParent()} is not null),
 * meaning that the filter runs in parallel. In this case the tile offset can be
 * retrieved from the destination raster with a calls to
 * {@code raster.getSampleModelTranslateX()} and
 * {@code raster.getSampleModelTranslateY()}. Those guys need to be
 * substracted(!) from the destination location before doing the mapping
 * {@code inverseTransform(dstPt, srcPt)} onto the source image plane. <br />
 *
 * <p>
 * Also note that a {@code RasterScanner} should iterate over the destination
 * image (or raster), not the source image!
 */
public interface InverseMappedTileParallelizable extends TileParallelizable {

	@Override
	default ImageTiler getImageTiler(BufferedImage src, BufferedImage dst, int width, int height) {
		// tiles over the destination instead of the source image
		return new SimpleImageTiler(dst, width, height);
	}

}
