package ch.unifr.diva.dip.api.imaging.rescaling;

/**
 * A filter function of a resampling filter.
 */
public interface FilterFunction {

	/**
	 * The filter function.
	 *
	 * @param x the weight to filter.
	 * @return the filter value.
	 */
	public double filter(double x);

	/**
	 * Returns the support width for the filter function.
	 *
	 * @return the support width for the filter function.
	 */
	public double getSupport();

}
