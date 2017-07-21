package ch.unifr.diva.dip.api.datastructures;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base class for (typed/non-generic) map data structures. The type of key
 * {@code K} and the type of values {@code V} must be marshallable.
 *
 * @param <K> the type of keys maintained by this map.
 * @param <V> the type of mapped values.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractMap<K, V> implements Map<K, V> {

	@XmlElement
	public final Map<K, V> map;

	/**
	 * Creates a new, empty map.
	 */
	public AbstractMap() {
		this(new HashMap<>());
	}

	/**
	 * Creates a new map.
	 *
	 * @param map the initial map.
	 */
	public AbstractMap(Map<K, V> map) {
		this.map = map;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{"
				+ mapToString()
				+ "}";
	}

	/**
	 * Returns a comma-separated list of the map entries.
	 *
	 * @return a comma-separated list of the map entries.
	 */
	protected String mapToString() {
		final StringBuilder sb = new StringBuilder();
		final Iterator<K> keys = this.map.keySet().iterator();
		while (keys.hasNext()) {
			final K key = keys.next();
			sb.append(key);
			sb.append('=');
			sb.append(this.map.get(key));
			if (keys.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return this.map.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AbstractMap<?, ?> other = (AbstractMap<?, ?>) obj;
		return this.map.equals(other.map);
	}

	/*
	 * Map<K, V>
	 */
	@Override
	public int size() {
		return this.map.size();
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public boolean containsKey(Object o) {
		return this.map.containsKey(o);
	}

	@Override
	public boolean containsValue(Object o) {
		return this.map.containsValue(o);
	}

	@Override
	public V get(Object o) {
		return this.map.get(o);
	}

	@Override
	public V put(K k, V v) {
		return this.map.put(k, v);
	}

	@Override
	public V remove(Object o) {
		return this.map.remove(o);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		this.map.putAll(map);
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public Set<K> keySet() {
		return this.map.keySet();
	}

	@Override
	public Collection<V> values() {
		return this.map.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return this.map.entrySet();
	}

}
