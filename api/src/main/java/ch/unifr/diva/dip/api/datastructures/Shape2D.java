package ch.unifr.diva.dip.api.datastructures;

/**
 * A 2D shape.
 */
public interface Shape2D {

	/**
	 * Returns a copy of the shape.
	 *
	 * @return a copy of the shape.
	 */
	public Shape2D copy();

	/**
	 * Returns the shape as a {@code Polygon2D}.
	 *
	 * @return the shape as a {@code Polygon2D}.
	 */
	public Polygon2D toPolygon2D();

}
