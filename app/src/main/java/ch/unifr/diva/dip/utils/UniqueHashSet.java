package ch.unifr.diva.dip.utils;

import java.util.HashSet;

/**
 * A unique hash set. This is an extended {@code HashSet} with the methods
 * {@code equals(obj)} and {@code hashCode()} overwritten, s.t. this set wont
 * ever equal another, distinct set, even if it contains the same elements.
 *
 * @param <T> the type of the elements maintained by this set.
 */
public class UniqueHashSet<T> extends HashSet<T> {

	private static final long serialVersionUID = -8845785820290750961L;

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof UniqueHashSet)) {
			return false;
		}
		return hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

}
