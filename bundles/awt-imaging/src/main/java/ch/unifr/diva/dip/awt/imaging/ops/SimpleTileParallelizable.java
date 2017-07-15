package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.scanners.SimpleImageTiler;
import java.awt.image.BufferedImage;

/**
 * The simple tile parallelizable interface marks a {@code BufferedImageOp} to
 * be compatible with being wrapped and run by {@code ConcurrentOp}.
 */
public interface SimpleTileParallelizable extends TileParallelizable<SimpleImageTiler> {

	@Override
	default void process(SimpleImageTiler tiler, BufferedImage src, BufferedImage dst) {
		ConcurrentTileOp.processTiles(
				this,
				tiler,
				src,
				dst
		);
	}

	@Override
	default SimpleImageTiler getImageTiler(BufferedImage src, BufferedImage dst, int width, int height) {
		return new SimpleImageTiler(src, width, height);
	}

}
