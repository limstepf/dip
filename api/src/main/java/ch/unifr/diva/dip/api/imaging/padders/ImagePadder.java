package ch.unifr.diva.dip.api.imaging.padders;

import java.awt.image.BufferedImage;

/**
 * An image padder represents a read-only image that is infinite in extend.
 */
public interface ImagePadder {

	/**
	 * Available image padder types.
	 */
	enum Type {

		/**
		 * Zero padding.
		 */
		ZERO() {
					@Override
					public ImagePadder getInstance() {
						if (this.padder == null) {
							this.padder = new ZeroPadder();
						}
						return this.padder;
					}
				},
		/**
		 * Tiled padding (circular indexing).
		 */
		TILED() {
					@Override
					public ImagePadder getInstance() {
						if (this.padder == null) {
							this.padder = new TiledPadder();
						}
						return this.padder;
					}
				},
		/**
		 * Reflective or mirrored padding (reflective indexing).
		 */
		REFLECTIVE() {
					@Override
					public ImagePadder getInstance() {
						if (this.padder == null) {
							this.padder = new ReflectivePadder();
						}
						return this.padder;
					}
				};

		// image padders are thread-safe, so...
		protected ImagePadder padder;

		/**
		 * Returns an image padder of a particular type.
		 *
		 * @return an image padder.
		 */
		public abstract ImagePadder getInstance();
	}

	/**
	 * Returns the samples in an array of int for the specified pixel.
	 *
	 * @param src the padded image.
	 * @param column the X coordinate of the pixel location.
	 * @param row the Y coordinate of the pixel location.
	 * @param iArray an optionally preallocated int array.
	 * @return the samples for the specified pixel.
	 */
	public int[] getPixel(BufferedImage src, int column, int row, int[] iArray);

	/**
	 * Returns the samples in an array of int for the specified pixel.
	 *
	 * @param src the padded image.
	 * @param column the X coordinate of the pixel location.
	 * @param row the Y coordinate of the pixel location.
	 * @param iArray an optionally preallocated int array.
	 * @return the samples for the specified pixel.
	 */
	public float[] getPixel(BufferedImage src, int column, int row, float[] iArray);

	/**
	 * Returns the samples in an array of int for the specified pixel.
	 *
	 * @param src the padded image.
	 * @param column the X coordinate of the pixel location.
	 * @param row the Y coordinate of the pixel location.
	 * @param iArray an optionally preallocated int array.
	 * @return the samples for the specified pixel.
	 */
	public double[] getPixel(BufferedImage src, int column, int row, double[] iArray);

	/**
	 * Returns the sample in a specified band for the pixel located at (x,y) as
	 * an int.
	 *
	 * @param src the padded image.
	 * @param column the X coordinate of the pixel location.
	 * @param row the Y coordinate of the pixel location.
	 * @param band the band to return.
	 * @return the sample in the specified band for the pixel at the specified
	 * coordinate.
	 */
	public int getSample(BufferedImage src, int column, int row, int band);

	/**
	 * Returns the sample in a specified band for the pixel located at (x,y) as
	 * a float.
	 *
	 * @param src the padded image.
	 * @param column the X coordinate of the pixel location.
	 * @param row the Y coordinate of the pixel location.
	 * @param band the band to return.
	 * @return the sample in the specified band for the pixel at the specified
	 * coordinate.
	 */
	public float getSampleFloat(BufferedImage src, int column, int row, int band);

	/**
	 * Returns the sample in a specified band for the pixel located at (x,y) as
	 * a double.
	 *
	 * @param src the padded image.
	 * @param column the X coordinate of the pixel location.
	 * @param row the Y coordinate of the pixel location.
	 * @param band the band to return.
	 * @return the sample in the specified band for the pixel at the specified
	 * coordinate.
	 */
	public double getSampleDouble(BufferedImage src, int column, int row, int band);

}
