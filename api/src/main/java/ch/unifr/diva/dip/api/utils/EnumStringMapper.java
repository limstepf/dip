package ch.unifr.diva.dip.api.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * An enumeration string mapper. Takes an enumeration and returns a list of
 * formatted (e.g. localized) strings, one for each item.
 *
 * @param <E> type of the enumeration.
 */
public interface EnumStringMapper<E extends Enum> {

	/**
	 * Formats an enumeration item.
	 *
	 * @param e the enumeration item.
	 * @return the formatted string.
	 */
	public String map(E e);

	/**
	 * Returns a list of formatted enumeration strings.
	 *
	 * @param <E> type of the enumeration.
	 * @param enumeration the enumeration class.
	 * @param mapper the mapping function.
	 * @return a list of formatted enumeration strings.
	 */
	public static <E extends Enum<E>> List<String> map(Class<E> enumeration, EnumStringMapper mapper) {
		final List<String> mapped = new ArrayList<>();
		for (E e : enumeration.getEnumConstants()) {
			mapped.add(mapper.map(e));
		}
		return mapped;
	}

}
