package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A multiband {@code Floats2D}. Wrapper (or intermediate data structure) for
 * data types backed by multiple {@code float[][]} such as OpenIMAJ's
 * {@code MBFImage}.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MultiFloats2D extends AbstractList<Floats2D> {

	/**
	 * Creates a new, empty multiband {@code Floats2D}.
	 */
	public MultiFloats2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new multiband {@code Floats2D}.
	 *
	 * @param bands the initial bands.
	 */
	public MultiFloats2D(List<Floats2D> bands) {
		super(bands);
	}

	/**
	 * Returns the number of bands.
	 *
	 * @return the number of bands.
	 */
	public int getNumBands() {
		return size();
	}

	/**
	 * Returns the number of rows (or height). This method assumes that all
	 * bands are of equal size. The number of rows of the first band is
	 * returned.
	 *
	 * @return the number of rows (or height).
	 */
	public int getNumRows() {
		if (size() == 0) {
			return 0;
		}
		return get(0).getNumRows();
	}

	/**
	 * Returns the number of columns (or width). This method assumes that all
	 * bands are of equal size. The number of rows of the first band is
	 * returned.
	 *
	 * @return the number of columns (or width).
	 */
	public int getNumColumns() {
		if (size() == 0) {
			return 0;
		}
		return get(0).getNumColumns();
	}

	/**
	 * Returns a copy.
	 *
	 * @return a copy.
	 */
	public MultiFloats2D copy() {
		final MultiFloats2D copy = new MultiFloats2D();
		for (Floats2D band : elements) {
			copy.add(band.copy());
		}
		return copy;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		for (Floats2D band : elements) {
			hash = 31 * hash + band.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MultiFloats2D other = (MultiFloats2D) obj;
		if (size() != other.size()) {
			return false;
		}
		for (int i = 0; i < size(); i++) {
			if (!get(i).equals(other.get(i))) {
				return false;
			}
		}
		return true;
	}

}
