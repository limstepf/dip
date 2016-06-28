package ch.unifr.diva.dip.api.datastructures;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

	public ValueMap(Map<String, Object> map) {
		this.map = map;
	}

	public Object get(String key) {
		return map.get(key);
	}

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

}
