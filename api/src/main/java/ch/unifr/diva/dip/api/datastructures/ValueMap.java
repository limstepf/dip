package ch.unifr.diva.dip.api.datastructures;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holds a map of multiple values. Used to back/store parameter compositions.
 * Values must be un-/marshallable with JAXB.
 */
@XmlRootElement(name = "value-map")
@XmlAccessorType(XmlAccessType.NONE)
public class ValueMap extends AbstractMap<String, Object> {

	/**
	 * Creates a new, empty value map.
	 */
	public ValueMap() {
		this(new HashMap<>());
	}

	/**
	 * Creates a new value map containing the given objects.
	 *
	 * @param map the objects of the value map.
	 */
	public ValueMap(Map<String, Object> map) {
		super(map);
	}

	/**
	 * Returns a shallow copy of the {@code ValueMap}. The keys and values
	 * themselves are not cloned.
	 *
	 * @return a shallow copy.
	 */
	public ValueMap shallowCopy() {
		return new ValueMap(new HashMap<>(map));
	}

}
