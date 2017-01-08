package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds a list of values and an index pointing to/selecting one. Values, just
 * typed as Objects, must be un-/marshallable with JAXB.
 */
@XmlRootElement(name = "value-list-selection")
@XmlAccessorType(XmlAccessType.NONE)
public class ValueListSelection extends ValueList {

	@XmlAttribute(name = "selected")
	public int selection;

	@SuppressWarnings("unused")
	public ValueListSelection() {
		this(new ArrayList<>(), -1);
	}

	/**
	 * Creates a new value list selection containing the given objects.
	 *
	 * @param values the objects of the value list selection.
	 * @param selection the index pointing to the currently selected object.
	 */
	public ValueListSelection(List<Object> values, int selection) {
		super(values);
		this.selection = selection;
	}

	/**
	 * Returns the currently selected object.
	 *
	 * @return the currently selected object.
	 */
	public Object getSelectedValue() {
		if (this.selection > this.list.size()) {
			return null;
		}

		return this.get(this.selection);
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = 31 * hash + this.selection;
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
		final ValueListSelection other = (ValueListSelection) obj;
		if (this.selection != other.selection) {
			return false;
		}
		if (!this.list.equals(other.list)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ "list=" + this.list
				+ ", selected=" + this.selection
				+ "}";
	}

}
