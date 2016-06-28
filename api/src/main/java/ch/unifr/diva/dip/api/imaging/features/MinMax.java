package ch.unifr.diva.dip.api.imaging.features;

import java.util.Arrays;

/**
 * Keeps track of registered/included extrema of samples.
 */
public class MinMax {

	// extrema per band
	private final double[] minima;
	private final double[] maxima;

	// global extrema
	private double min;
	private double max;

	/**
	 * Creates a {@code MinMax} instance for the given number of bands. Sample
	 * values need to be added with the {@code include} method.
	 *
	 * @param numBands number of bands to look for extrema separately.
	 */
	public MinMax(int numBands) {
		this.minima = new double[numBands];
		this.maxima = new double[numBands];

		for (int b = 0; b < numBands; b++) {
			this.minima[b] = Double.POSITIVE_INFINITY;
			this.maxima[b] = Double.NEGATIVE_INFINITY;
		}

		this.min = Double.POSITIVE_INFINITY;
		this.max = Double.NEGATIVE_INFINITY;
	}

	/**
	 * Includes a value in the search for extrema (on a single/the first band).
	 *
	 * @param value the value of the sample.
	 */
	public void include(double value) {
		include(value, 0);
	}

	/**
	 * Includes a value in the search for extrema.
	 *
	 * @param value the value of the sample.
	 * @param band band of the sample.
	 */
	public void include(double value, int band) {
		this.minima[band] = Math.min(this.minima[band], value);
		this.maxima[band] = Math.max(this.maxima[band], value);
		this.min = Math.min(this.min, value);
		this.max = Math.max(this.max, value);
	}

	/**
	 * Returns the (global) maximum sample value over all bands.
	 *
	 * @return the (global) maximum sample value over all bands.
	 */
	public double getMax() {
		return this.max;
	}

	/**
	 * Returns the maximum sample value in the given band.
	 *
	 * @param band the band.
	 * @return the maximum sample value in the given band.
	 */
	public double getMax(int band) {
		return this.maxima[band];
	}

	/**
	 * Returns the (global) minimum sample value over all bands.
	 *
	 * @return the (global) minimum sample value over all bands.
	 */
	public double getMin() {
		return this.min;
	}

	/**
	 * Returns the minimum sample value in the given band.
	 *
	 * @param band the band.
	 * @return the minimum sample value in the given band.
	 */
	public double getMin(int band) {
		return this.minima[band];
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "min=" + Arrays.toString(this.minima)
				+ ", max=" + Arrays.toString(this.maxima)
				+ "}";
	}
}
