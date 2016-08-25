package ch.unifr.diva.dip.api.imaging.rescaling;

/**
 * Resampling filters used for rescaling.
 */
public enum ResamplingFilter implements FilterFunction {

	/**
	 * Box filter.
	 */
	BOX(0.5) {
				@Override
				public double filter(double x) {
					if ((x >= -0.5) && (x < 0.5)) {
						return 1.0;
					}

					return 0.0;
				}
			},
	/**
	 * Triangle filter.
	 */
	TRIANGLE(1.0) {
				@Override
				public double filter(double x) {
					if (x < 0.0) {
						x = -x;
					}

					if (x < 1.0) {
						return 1.0 - x;
					}

					return 0.0;
				}
			},
	/**
	 * Catmull-Rom filter.
	 */
	CATMULL_ROM(2.0) {
				@Override
				public double filter(double x) {
					if (x < 0) {
						x = -x;
					}

					if (x < 1.0) {
						return 0.5 * (2.0 + x * x * (-5.0 + x * 3.0));
					}

					if (x < 2.0) {
						return 0.5 * (4.0 + x * (-8.0 + x * (5.0 - x)));
					}

					return 0.0;
				}
			},
	/**
	 * Lanczos3 filter.
	 */
	LANCZOS3(3.0) {
				@Override
				public double filter(double x) {
					if (x < 0) {
						x = -x;
					}

					if (x < 3.0) {
						return sinc(x) * sinc(x / 3.0);
					}

					return 0.0;
				}

				private double sinc(double x) {
					x *= Math.PI;
					return (x == 0) ? 1.0 : Math.sin(x) / x;
				}
			},
	/**
	 * Hermite filter.
	 */
	HERMITE(1.0) {
				@Override
				public double filter(double x) {
					if (x < 0.0) {
						x = -x;
					}

					if (x < 1.0) {
						return (2.0 * x - 3.0) * x * x + 1.0;
					}

					return 0.0;
				}
			},
	/**
	 * Mitchell filter.
	 */
	MITCHELL(2.0) {
				private double B = 1.0 / 3.0;
				private double C = 1.0 / 3.0;

				@Override
				public double filter(double x) {
					if (x < 0.0) {
						x = -x;
					}

					final double xx = x * x;

					if (x < 1.0) {
						x = (((12.0 - 9.0 * B - 6.0 * C) * (x * xx))
						+ ((-18.0 + 12.0 * B + 6.0 * C) * xx)
						+ (6.0 - 2 * B));
						return x / 6.0;
					}

					if (x < 2.0) {
						x = (((-1.0 * B - 6.0 * C) * (x * xx))
						+ ((6.0 * B + 30.0 * C) * xx)
						+ ((-12.0 * B - 48.0 * C) * x)
						+ (8.0 * B + 24 * C));
						return x / 6.0;
					}

					return 0.0;
				}
			};

	private final double support;

	ResamplingFilter(double support) {
		this.support = support;
	}

	@Override
	public double getSupport() {
		return this.support;
	}

}
