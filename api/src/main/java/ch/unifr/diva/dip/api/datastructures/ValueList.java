package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds a list of multiple values. Used to back/store parameter compositions.
 * Values must be un-/marshallable with JAXB.
 */
@XmlRootElement(name = "value-list")
@XmlAccessorType(XmlAccessType.NONE)
public class ValueList {

	@XmlElement
	public final List<Object> list;

	@SuppressWarnings("unused")
	public ValueList() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new value list containing the given objects.
	 *
	 * @param values the objects of the value list.
	 */
	public ValueList(List<Object> values) {
		this.list = values;
	}

	/**
	 * Returns an object of the value list.
	 *
	 * @param index the index of the object in the value list.
	 * @return the object at the given index.
	 */
	public Object get(int index) {
		return this.list.get(index);
	}

	/**
	 * Sets/updates an object of the value list.
	 *
	 * @param index the index of the object in the value list.
	 * @param obj the new object.
	 */
	public void set(int index, Object obj) {
		this.list.set(index, obj);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ "list=" + this.list
				+ "}";
	}

}
