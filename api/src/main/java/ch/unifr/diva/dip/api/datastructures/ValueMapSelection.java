package ch.unifr.diva.dip.api.datastructures;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds a map of values and a key pointing to/selecting one. Values, just typed
 * as Objects, must be un-/marshallable with JAXB.
 */
@XmlRootElement(name = "value-map-selection")
@XmlAccessorType(XmlAccessType.NONE)
public class ValueMapSelection extends ValueMap {

	@XmlAttribute(name = "selected")
	public String selection;

	@SuppressWarnings("unused")
	public ValueMapSelection() {
		this(new HashMap<>(), "");
	}

	/**
	 * Creates a new value map selection containing the given objects.
	 *
	 * @param map the objects of the value map.
	 * @param selection the key of the selected object.
	 */
	public ValueMapSelection(Map<String, Object> map, String selection) {
		super(map);
		this.selection = selection;
	}

	/**
	 * Returns the currently selected object.
	 *
	 * @return the currently selected object.
	 */
	public Object getSelectedValue() {
		return this.map.get(this.selection);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append('@');
		sb.append(Integer.toHexString(this.hashCode()));
		sb.append('{');

		printMap(sb);
		sb.append(", selected=");
		sb.append(this.selection);

		sb.append('}');
		return sb.toString();
	}

}
