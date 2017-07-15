package ch.unifr.diva.dip.awt.imaging.features;

import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * An image sample histogram for a single band.
 */
public class Histogram {

	private final int[] counts;
	private final int maxValue;
	private final int totalSamples;
	private MinMax extrema; // lazily computed only if needed; not thread-safe

	/**
	 * Extracts a histogram from a band in an image.
	 *
	 * @param src image source.
	 * @param band the band to extract the histogram from.
	 */
	public Histogram(BufferedImage src, int band) {
		this.maxValue = ImagingUtils.maxSampleValue(src, band);
		this.counts = new int[this.maxValue + 1];
		this.totalSamples = src.getWidth() * src.getHeight();

		final WritableRaster raster = src.getRaster();
		for (Location pt : new RasterScanner(src, false)) {
			this.counts[raster.getSample(pt.col, pt.row, band)]++;
		}
	}

	private MinMax computeExtrema() {
		final MinMax mm = new MinMax(1);
		for (int i = 0; i < this.counts.length; i++) {
			mm.include(this.counts[i]);
		}
		return mm;
	}

	/**
	 * Returns the number of bins in the histogram.
	 *
	 * @return the number of bins in the histogram.
	 */
	public int getNumberOfBins() {
		return this.counts.length;
	}

	/**
	 * Returns the total number of samples in the histogram.
	 *
	 * @return the total number of samples.
	 */
	public int getTotalSamples() {
		return this.totalSamples;
	}

	/**
	 * Returns the value/count of a bin.
	 *
	 * @param index index of the bin.
	 * @return the value/count of a bin.
	 */
	public int getValue(int index) {
		return this.counts[index];
	}

	/**
	 * Returns the minimum value/count in the histogram.
	 *
	 * @return the minimum value/count in the histogram.
	 */
	public int getMinValue() {
		if (this.extrema == null) {
			this.extrema = computeExtrema();
		}

		return (int) this.extrema.getMin();
	}

	/**
	 * Returns the maximum value/count in the histogram.
	 *
	 * @return the maximum value/count in the histogram.
	 */
	public int getMaxValue() {
		if (this.extrema == null) {
			this.extrema = computeExtrema();
		}

		return (int) this.extrema.getMax();
	}

	/**
	 * Returns (a copy of) the histogram.
	 *
	 * @return (a copy of) the histogram.
	 */
	public int[] getCounts() {
		final int[] result = new int[this.counts.length];
		System.arraycopy(this.counts, 0, result, 0, this.counts.length);
		return result;
	}

	/**
	 * Returns the normalized histogram. In a normalized histogram the
	 * value/count is divided by the total number of samples.
	 *
	 * @return the normalized histogram.
	 */
	public double[] getNormalizedHistogram() {
		final double[] result = new double[this.counts.length];
		for (int i = 0; i < this.counts.length; i++) {
			result[i] = (this.counts[i] / (double) this.totalSamples);
		}
		return result;
	}

	/**
	 * Returns the (discrete) cummulative distribution function (CDF). The CDF
	 * essentially answers the question: "What percentage of the samples in an
	 * image are equal to or less than value J in the image?"
	 *
	 * @return the (discrete) cummulative distribution function (CDF).
	 */
	public double[] getCDF() {
		final double[] cdf = getNormalizedHistogram();
		for (int i = 1; i < this.counts.length; i++) {
			cdf[i] = cdf[i - 1] + cdf[i];
		}
		return cdf;
	}

	/**
	 * Returns the (discrete) cummulative distribution function (CDF) as a
	 * look-up table.
	 *
	 * @return the CDF look-up table.
	 */
	public byte[] toCDFArray() {
		return toCDFArray(getCDF());
	}

	/**
	 * Returns the (discrete) cummulative distribution function (CDF) as a
	 * look-up table.
	 *
	 * @param cdf the CDF (if already computed).
	 * @return the CDF look-up table.
	 */
	public byte[] toCDFArray(double[] cdf) {
		if (cdf == null) {
			cdf = getCDF();
		}
		final byte[] table = new byte[cdf.length];
		for (int i = 0; i < cdf.length; i++) {
			table[i] = (byte) Math.round(cdf[i] * this.maxValue);
		}
		return table;
	}
}
