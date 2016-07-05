package ch.unifr.diva.dip.api.imaging.ops;

/**
 * The paralellizable interface marks a {@code BufferedImageOp} to be compatible
 * with being wrapped and run by {@code ConcurrentOp}. The assumption is that an
 * implementing {@code BufferedImageOp} can be safely paralellized over tiles of
 * an image. I.e. the {@code BufferedImageOp} should not rely on the order of
 * pixel scanning or on image geometry, or faulty results may be produced when
 * parallelized this way.
 */
public interface Parallelizable {
	// nothing to be implemented
}
