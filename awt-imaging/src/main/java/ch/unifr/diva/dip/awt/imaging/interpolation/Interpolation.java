package ch.unifr.diva.dip.awt.imaging.interpolation;

import ch.unifr.diva.dip.awt.imaging.padders.ImagePadder;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Interpolation algorithms used for image resampling.
 */
public enum Interpolation implements Interpolant {

	/**
	 * Nearest-neighbor interpolation. The nearest-neighbor algorithm selects
	 * the value of the nearest sample and does not consider the samples of
	 * neighboring points at all, yielding a piecewise-constant interpolant.
	 */
	NEAREST_NEIGHBOR() {

				@Override
				public double interpolate(BufferedImage src, ImagePadder padder, Point2D point, int band, Object support) {
					final int x = (int) Math.round(point.getX());
					final int y = (int) Math.round(point.getY());

					return padder.getSampleDouble(src, x, y, band);
				}

				@Override
				public Object getSupport() {
					return null;
				}
			},
	/**
	 * Bilinear interpolation. The bilinear algorithm performs linear
	 * interpolation first in one direction, and then again in the other
	 * direction. Although each step is linear in the sampled values and in the
	 * position, the interpolation as a whole is not linear but rather quadratic
	 * in the sample location.
	 */
	BILINEAR() {
				@Override
				public double interpolate(BufferedImage src, ImagePadder padder, Point2D point, int band, Object support) {
					final int x0 = (int) Math.floor(point.getX());
					final int y0 = (int) Math.floor(point.getY());
					final int x1 = x0 + 1;
					final int y1 = y0 + 1;
					final double dx = point.getX() - x0;
					final double dy = point.getY() - y0;

					final double f00 = padder.getSampleDouble(src, x0, y0, band);
					final double f01 = padder.getSampleDouble(src, x0, y1, band);
					final double f10 = padder.getSampleDouble(src, x1, y0, band);
					final double f11 = padder.getSampleDouble(src, x1, y1, band);

					final double t1 = f00 + dx * (f10 - f00);
					final double t2 = f01 + dx * (f11 - f01);

					return t1 + (t2 - t1) * dy;
				}

				@Override
				public Object getSupport() {
					return null;
				}
			},
	/**
	 * Bicubic interpolation. The bicubic algorithm considers the closest 4x4
	 * neighborhood of samples, where closer samples are given a higher
	 * weighting in the calculation.
	 *
	 * <p>
	 * Resulting signal needs to be clamped (due to ringing)!
	 */
	BICUBIC() {
				@Override
				public double interpolate(BufferedImage src, ImagePadder padder, Point2D point, int band, Object support) {
					final int x = (int) Math.floor(point.getX());
					final int y = (int) Math.floor(point.getY());
					final int x0 = x - 1;
					final int y0 = y - 1;
					final double dx = point.getX() - x;
					final double dy = point.getY() - y;
					final double[][] N = (double[][]) support;

					for (int j = 0; j < 4; j++) {
						for (int i = 0; i < 4; i++) {
							N[i][j] = padder.getSampleDouble(src, x0 + i, y0 + j, band);
						}
					}

					return bicubic(dx, dy, N);
				}

				@Override
				public Object getSupport() {
					return new double[4][4];
				}
			};

	/**
	 * Linear interpolation of {@code y} at {@code x}.
	 *
	 * @param x the {@code x} position.
	 * @param x0 the first {@code x} position.
	 * @param y0 the first {@code y} value.
	 * @param x1 the second {@code x} position.
	 * @param y1 the second {@code y} value.
	 * @return the interpolated {@code y} value at {@code x}.
	 */
	public static double lerp(double x, double x0, double y0, double x1, double y1) {
		if (x == x0) {
			return y0;
		}
		return y0 + (x - x0) * ((y1 - y0) / (x1 - x0));
	}

	/**
	 * Bilinear interpolation (or linear convolution) of the value at
	 * {@code (x,y)}.
	 *
	 * @param x the {@code x} position (in 0..1).
	 * @param y the {@code y} position (in 0..1).
	 * @param f00 the value at {@code (0,0)}.
	 * @param f01 the value at {@code (0,1)}.
	 * @param f10 the value at {@code (1,0)}.
	 * @param f11 the value at {@code (1,1)}.
	 * @return the interpolated value at {@code (x,y)}.
	 */
	public static double bilerp(double x, double y, double f00, double f01, double f10, double f11) {
		return f00 * (1.0 - x) * (1.0 - y) + f10 * x * (1.0 - y) + f01 * (1.0 - x) * y + f11 * x * y;
	}

	/**
	 * Cubic interpolation of {@code y} at {@code x}.
	 *
	 * @param x the {@code x} position (in 0..1).
	 * @param y0 the {@code y} value at {@code x=-1}.
	 * @param y1 the {@code y} value at {@code x=0}.
	 * @param y2 the {@code y} value at {@code x=1}.
	 * @param y3 the {@code y} value at {@code x=2}.
	 * @return the interpolated value at {@code x}.
	 */
	public static double cubic(double x, double y0, double y1, double y2, double y3) {
		return y1 + 0.5 * x * (y2 - y0 + x * (2.0 * y0 - 5.0 * y1 + 4.0 * y2 - y3 + x * (3.0 * (y1 - y2) + y3 - y0)));
	}

	/**
	 * Cubic interpolation {@code y} at {@code x}.
	 *
	 * @param x the {@code x} position (in 0..1).
	 * @param y the array of the 4 {@code y} values at {@code x=[-1, 0, 1, 2]}.
	 * @return the interpolated value at {@code x}.
	 */
	public static double cubic(double x, double[] y) {
		return y[1] + 0.5 * x * (y[2] - y[0] + x * (2.0 * y[0] - 5.0 * y[1] + 4.0 * y[2] - y[3] + x * (3.0 * (y[1] - y[2]) + y[3] - y[0])));
	}

	/**
	 * Bicubic interpolation (or cubic convolution) of the value at
	 * {@code (x,y)}.
	 *
	 * @param x the {@code x} position (in 0..1).
	 * @param y the {@code y} position (in 0..1).
	 * @param p the 4x4 array of values from {@code (-1,-1)} to {@code (2,2)}
	 * given in row-major order.
	 * @return the interpolated value at {@code (x,y)}.
	 */
	public static double bicubic(double x, double y, double[][] p) {
		final double y0 = cubic(y, p[0]);
		final double y1 = cubic(y, p[1]);
		final double y2 = cubic(y, p[2]);
		final double y3 = cubic(y, p[3]);
		return cubic(x, y0, y1, y2, y3);
	}

}
