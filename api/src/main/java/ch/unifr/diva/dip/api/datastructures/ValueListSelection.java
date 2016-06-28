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

	public ValueListSelection(List<Object> values, int selection) {
		super(values);
		this.selection = selection;
	}

	public Object getSelectedValue() {
		if (this.selection > this.list.size()) {
			return null;
		}

		return this.get(this.selection);
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
