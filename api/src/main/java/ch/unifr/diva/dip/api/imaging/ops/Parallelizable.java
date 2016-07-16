package ch.unifr.diva.dip.api.imaging.ops;

/**
 * The parallelizable interface marks a {@code BufferedImageOp} to be compatible
 * with being wrapped and run by {@code ConcurrentOp}. This interface is not
 * supposed to be directly implemented by a {@code BufferedImageOp}, since it
 * does not specify the actual parallelization method.
 *
 * @see TileParallelizable, PaddedTileParallelizable
 */
public interface Parallelizable {
	// nothing to implement
}
