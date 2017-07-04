/**
 * Image rescaling algorithms.
 *
 * <ol>
 * <li>Native scaling with {@code AwtRescaling} is clearly fastest, especially
 * with NN filtering. Even Bilinear filtering still beats the NN/BOX methods of
 * {@code FilteredRescaling} and {@code ResampleOp}.</li>
 *
 * <li>Using {@code FilteredRescaling} is way slower (~17 times), so use of this
 * only makes sense if we're using some filter other than {@code BOX},
 * {@code TRIANGLE}, or {@code CATMULL_ROM}.</li>
 *
 * <li>And in comparison, resizing an image with the {@code ResampleOp} is the
 * slowest approach (~25 times slower) of them all.</li>
 * </ol>
 *
 * (see: {@code UpscalingBenchmark.java})
 */
package ch.unifr.diva.dip.awt.imaging.rescaling;
