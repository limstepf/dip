package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds a list of multiple values. Used to back/store parameter compositions.
 * Values must be un-/marshallable with JAXB.
 */
@XmlRootElement(name = "value-list")
@XmlAccessorType(XmlAccessType.NONE)
public class ValueList extends AbstractList<Object> {

	/**
	 * Creates a new, empty value list.
	 */
	public ValueList() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new value list containing the given objects.
	 *
	 * @param values the objects of the value list.
	 */
	public ValueList(List<Object> values) {
		super(values);
	}

}
