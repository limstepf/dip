package ch.unifr.diva.dip.api.datastructures;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds a map of multiple values. Used to back/store parameter compositions.
 * Values must be un-/marshallable with JAXB.
 */
@XmlRootElement(name = "value-map")
@XmlAccessorType(XmlAccessType.NONE)
public class ValueMap {

	@XmlElement
	public final Map<String, Object> map;

	@SuppressWarnings("unused")
	public ValueMap() {
		this(new HashMap<>());
	}

	/**
	 * Creates a new value map containing the given objects.
	 *
	 * @param map the objects of the value map.
	 */
	public ValueMap(Map<String, Object> map) {
		this.map = map;
	}

	/**
	 * Returns an object of the value map.
	 *
	 * @param key the key of the object.
	 * @return the object, or null if not found.
	 */
	public Object get(String key) {
		return map.get(key);
	}

	/**
	 * Sets/updates an object of the value map.
	 *
	 * @param key the key of the object.
	 * @param obj the new object.
	 */
	public void set(String key, Object obj) {
		this.map.put(key, obj);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append('@');
		sb.append(Integer.toHexString(this.hashCode()));
		sb.append('{');

		printMap(sb);

		sb.append('}');
		return sb.toString();
	}

	protected void printMap(StringBuilder sb) {
		sb.append("map=[");
		final Iterator<String> keys = this.map.keySet().iterator();
		while (keys.hasNext()) {
			final String key = keys.next();
			sb.append(key);
			sb.append('=');
			sb.append(this.map.get(key));
			if (keys.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(']');
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ValueMap other = (ValueMap) obj;
		if (!this.map.equals(other.map)) {
			return false;
		}
		return true;
	}

}
