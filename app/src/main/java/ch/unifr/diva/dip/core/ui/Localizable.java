
package ch.unifr.diva.dip.core.ui;

import ch.unifr.diva.dip.api.utils.L10n;

/**
 * Convencience interface that implements default helper methods for localization.
 */
public interface Localizable {

	default String localize(String key) {
		return L10n.getInstance().getString(key);
	}

	default String localize(String key, Object... args) {
		return L10n.getInstance().getString(key, args);
	}

//	/**
//	 * Returns a translated label.
//	 *
//	 * @param key the key of a property.
//	 * @return the translated label - or the key encapsulated by double
//	 * exclamation marks as fallback.
//	 */
//	default String label(String key) {
//		return L10n.LABELS.getString(key);
//	}
//
//	/**
//	 * Returns a translated and formatted label. Replaces the tokens
//	 * {@literal {0}} to {@literal {n}} with the given arguments.
//	 *
//	 * @param key the key of a property.
//	 * @param args the arguments to replace the property tokens with.
//	 * @return the translated and formatted label - or the key encapsulated by
//	 * double exclamation marks as fallback.
//	 */
//	default String label(String key, Object... args) {
//		return L10n.LABELS.getString(key, args);
//	}
//
//	/**
//	 * Returns a translated label with an extra colon as postfix.
//	 *
//	 * @param key the key of a property.
//	 * @return the translated label - or the key encapsulated by double
//	 * exclamation marks as fallback.
//	 */
//	default String labelc(String key) {
//		return L10n.LABELS.getString(key) + ":";
//	}
//
//	/**
//	 * Returns a translated message.
//	 *
//	 * @param key the key of a property.
//	 * @return the translated message - or the key encapsulated by double
//	 * exclamation marks as fallback.
//	 */
//	default String msg(String key) {
//		return L10n.MESSAGES.getString(key);
//	}
//
//	/**
//	 * Returns a translated and formatted message. Replaces the tokens
//	 * {@literal {0}} to {@literal {n}} with the given arguments.
//	 *
//	 * @param key the key of a property.
//	 * @param args the arguments to replace the property tokens with.
//	 * @return the translated and formatted message - or the key encapsulated by
//	 * double exclamation marks as fallback.
//	 */
//	default String msg(String key, Object... args) {
//		return L10n.MESSAGES.getString(key, args);
//	}

}
