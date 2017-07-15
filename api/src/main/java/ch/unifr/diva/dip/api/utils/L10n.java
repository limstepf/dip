package ch.unifr.diva.dip.api.utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * L10n is a pimped up ResourceBundle for localization. It comes with a default
 * and static localization bundle used by the host application that either can
 * be used by extra osgi bundles as is, or it can be augmented with a
 * specialized localization bundle local to some extra osgi bundle. In the
 * latter case keys are search first in the additional, local localization
 * bundle, while the default one is used as a fallback.
 *
 * <p>
 * Call from anywhere using the default localizatin bundle:
 * <pre>
 * <code>
 *		L10n.getInstance().getString(key)
 * </code>
 * </pre>
 *
 * <p>
 * Augment with local localization bundle, using the default one as fallback:
 * <pre>
 * <code>
 *		L10n l10n = L10n.newInstance("locales/localization", this.class.getClassLoader());
 *		l10n.getString(key)
 * </code>
 * </pre>
 *
 * ...where locales is a resource directory of the extra bundle that includes
 * property files like `localization_en.properties` (or the global
 * `localization.properties` file).
 */
public class L10n {

	private static final Logger log = LoggerFactory.getLogger(L10n.class);
	private static final List<Locale> availableLocales;
	private static volatile Locale locale;
	private static final String baseName;
	private static final ResourceBundleInstance baseInstance;
	private static final L10n instance;

	static {
		availableLocales = Arrays.asList(
				Locale.ENGLISH
		);
		locale = defaultLocale();
		baseName = "locales/localization";
		baseInstance = new ResourceBundleInstance(baseName, L10n.class.getClassLoader());
		instance = new L10n();
	}

	private final List<ResourceBundleInstance> bundles = new ArrayList<>();

	private L10n() {
		this(baseInstance);
	}

	private L10n(ResourceBundleInstance bundleInstance) {
		initLocale(bundleInstance);
		this.bundles.add(bundleInstance);
	}

	/**
	 * Returns the default static localization bundle. The default static
	 * localization bundle can be augmented with a local localization bundle
	 * that uses the default one as fallback (see {@code newInstance()}).
	 *
	 * @return the default static localization bundle.
	 */
	public static L10n getInstance() {
		return instance;
	}

	/**
	 * Returns a new instance, augmenting the default static localization
	 * bundle. A key is search first in the new, specialised localization
	 * bundle. If not found there, the default static localization bundle is
	 * searched as a fallback. This mechanism allows plugins/osgi bundles to
	 * define and use their local, specific translations (if needed) and/or use
	 * the (globally) available localization bundle.
	 *
	 * @param baseName baseName of the bundle resource (e.g. "locales/file"
	 * where a particular localization file is "locales/file_en.properties").
	 * @param loader class loader to retrieve the bundle resource.
	 * @return an augmented instance.
	 */
	public static L10n newInstance(String baseName, ClassLoader loader) {
		final L10n obj = new L10n();
		obj.addBundleInstance(baseName, loader);
		return obj;
	}

	private void addBundleInstance(String baseName, ClassLoader loader) {
		final ResourceBundleInstance bundleInstance = new ResourceBundleInstance(baseName, loader);
		initLocale(bundleInstance);
		bundles.add(bundleInstance);
	}

	/**
	 * Sets the static and application-wide {@code Locale}. This should only be
	 * called once at start-up of the host application (which needs to restart
	 * to effectively change the language). Augmented instances already created
	 * wont be fully affected by a call to this.
	 *
	 * @param locale the application-wide {@code Locale} to be used.
	 * @return {@code true} in case of success, {@code false} if the locale is
	 * not supported.
	 */
	public static boolean setLocale(Locale locale) {
		if (!availableLocales.contains(locale)) {
			return false;
		}

		if (locale.equals(L10n.locale)) {
			return true;
		}

		L10n.locale = locale;
		initLocale(L10n.baseInstance);

		return true;
	}

	/**
	 * Inits/updates a localization bundle instance with the current locale.
	 *
	 * @param bundleInstance a localization bundle instance.
	 */
	private static void initLocale(ResourceBundleInstance bundleInstance) {
		bundleInstance.bundle = ResourceBundle.getBundle(
				bundleInstance.baseName,
				L10n.locale,
				bundleInstance.loader
		);
	}

	/**
	 * Returns the default {@code Locale}.
	 *
	 * @return the default {@code Locale}.
	 */
	public static Locale defaultLocale() {
		return availableLocales.get(0);
	}

	/**
	 * Returns a list of all available locales.
	 *
	 * @return a list of available locales.
	 */
	public static List<Locale> availableLocales() {
		return new ArrayList<>(availableLocales);
	}

	/**
	 * Returns a set of all languages with available locale.
	 *
	 * @return a set of all languages with available locale.
	 */
	public static Collection<String> availableLanguages() {
		final Set<String> languages = new HashSet<>();
		for (Locale loc : availableLocales) {
			languages.add(loc.getLanguage());
		}
		return languages;
	}

	/**
	 * Returns the current {@code Locale}. The local is set at start-up of the
	 * host application and is not really expected to be reset at runtime. That
	 * is the host application needs to restart in order to effectively change
	 * the language.
	 *
	 * @return the current {@code Locale}.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Returns a translated and formatted string. Replaces the expected tokens
	 * {@literal {0}} to {@literal {n-1}} with the given n arguments.
	 *
	 * @param key the key of a property.
	 * @param args the arguments to replace the property tokens with.
	 * @return the translated and formatted string - or the key encapsulated by
	 * double exclamation marks as fallback.
	 */
	public String getString(String key, Object... args) {
		return MessageFormat.format(getString(key), args);
	}

	/**
	 * Returns a translated string.
	 *
	 * @param key the key of a property.
	 * @return the translated string - or the key itself if not found.
	 */
	public String getString(String key) {
		for (int i = bundles.size() - 1; i >= 0; i--) {
			try {
				final ResourceBundleInstance bundleInstance = bundles.get(i);
				return bundleInstance.bundle.getString(key);
			} catch (MissingResourceException ex) {
				// keep searching...
			} catch (NullPointerException ex) {
				log.error(
						"invalid resource bundle: {} (locale: {})",
						bundles.get(i),
						locale
				);
			}
		}

		log.warn("undefined key: {} (locale: {})", key, locale);
		return key;
	}

	private static class ResourceBundleInstance {

		public final String baseName;
		public final ClassLoader loader;
		public volatile ResourceBundle bundle;

		public ResourceBundleInstance(String baseName, ClassLoader loader) {
			this.baseName = baseName;
			this.loader = loader;
		}
	}

}
