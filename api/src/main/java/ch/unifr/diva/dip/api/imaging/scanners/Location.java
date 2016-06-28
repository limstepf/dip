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

	public Location() {
		this(0, 0, 0, 0);
	}

	public Location(int index, int col, int row, int band) {
		this.index = index;
		this.col = col;
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
