package ch.unifr.diva.dip.api.imaging.scanners;

/**
 * Location of a sample in an image raster.
 */
public class Location {

	/**
	 * Index of the sample in the raster.
	 */
	public int index;

	/**
	 * Column (or x-coordinate) of the sample in the raster.
	 */
	public int col;

	/**
	 * Row (or y-coordinate) of the sample in the raster.
	 */
	public int row;

	/**
	 * Band of the sample.
	 */
	public int band;

	/**
	 * Creates a new location with index, coordinates and band all set to zero.
	 */
	public Location() {
		this(0, 0, 0, 0);
	}

	/**
	 * Creates a new location with index set to zero.
	 *
	 * @param column the X coordinate of the location.
	 * @param row the Y coordinate of the location.
	 * @param band the index of the band of the location.
	 */
	public Location(int column, int row, int band) {
		this(0, column, row, band);
	}

	/**
	 * Creates a new location.
	 *
	 * @param index the index of the location.
	 * @param column the X coordinate of the location.
	 * @param row the Y coordinate of the location.
	 * @param band the index of the band of the location.
	 */
	public Location(int index, int column, int row, int band) {
		this.index = index;
		this.col = column;
		this.row = row;
		this.band = band;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Location) {
			final Location other = (Location) obj;
			if (this.index != other.index) {
				return false;
			}
			if (this.col != other.col) {
				return false;
			}
			if (this.row != other.row) {
				return false;
			}
			return (this.band == other.band);
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 43 * hash + this.index;
		hash = 43 * hash + this.col;
		hash = 43 * hash + this.row;
		hash = 43 * hash + this.band;
		return hash;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "index=" + index
				+ ", col=" + col
				+ ", row=" + row
				+ ", band=" + band
				+ "}";
	}

}
