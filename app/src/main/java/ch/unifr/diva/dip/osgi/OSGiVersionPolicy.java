package ch.unifr.diva.dip.osgi;

import org.osgi.framework.Version;

/**
 * OSGi version (auto-update) policy. Semantic versioning allows for a sensible
 * approach to (auto) maintaining versions of used services.
 *
 * <p>
 * Recall: there are four parts in a version:
 * {@code major.minor.micro.qualifier}, where a major bump breaks backwards
 * compatibility, a minor bump quarantees backwards compatibility but is
 * otherwise free to add more functionality, a micro bump is a bugfix only, and
 * the qualifier (a string, e.g. "SNAPSHOT") doesn't matter too much.
 *
 * <p>
 * Thus, it can be convenient to require the same major and minor version, while
 * micro bumps will be used/updated automatically (if available). Then again, it
 * still might be safer to insist on the exact same version (minus the
 * qualifier, which we do not really consider as meanigful). Or, if "safety" (or
 * reproducability) is not so much of a concern, just require the same major
 * version, always using the latest minor (and micro) available.
 */
public enum OSGiVersionPolicy {

	/**
	 * Requires the version {@code major.minor.micro}, or considers the service
	 * as unavailable. Never change a running system. Or something...
	 */
	MICRO_EXACT,
	/**
	 * Checks whether the version {@code major.minor.micro} is available,
	 * otherwise falls back to the latest {@code major.minor}.
	 */
	MICRO_LATEST_FALLBACK,
	/**
	 * Always requests the latest {@code major.minor} version. Micro bumped
	 * services just recieved a bugfix or two, so it makes sense to always use
	 * the latest micro version with fixed major, and minor part.
	 */
	MICRO_LATEST,
	/**
	 * Always request the latest {@code major} version. Minor bumped services
	 * might be newer, offering more functionality but should remain backward
	 * compatible.
	 */
	MINOR_LATEST;

	/**
	 * Returns the appropriate version of a service from an OSGi service
	 * collection.
	 *
	 * @param <T> type of the service.
	 * @param collection OSGi service collection of all versions of the service.
	 * @param version desired version to retrieve.
	 * @return a version of the service according to the given version policy,
	 * or null if no version meeting the requirements is available.
	 */
	public <T> OSGiService<T> getService(ServiceCollection<T> collection, Version version) {
		if (collection == null) {
			return null;
		}

		switch (this) {
			case MICRO_LATEST_FALLBACK:
				OSGiService<T> p = collection.getService(
						version.getMajor(),
						version.getMinor(),
						version.getMicro()
				);
				if (p != null) {
					return p;
				}
			// fall-through to MICRO_LATEST!

			case MICRO_LATEST:
				return collection.getService(
						version.getMajor(),
						version.getMinor()
				);

			case MINOR_LATEST:
				return collection.getService(
						version.getMajor()
				);

			case MICRO_EXACT:
			default:
				return collection.getService(
						version.getMajor(),
						version.getMinor(),
						version.getMicro()
				);
		}
	}

	/**
	 * Safely returns a valid version policy.
	 *
	 * @param name name of the version policy.
	 * @return the version policy, or a default (MICRO_LATEST).
	 */
	public static OSGiVersionPolicy get(String name) {
		try {
			return OSGiVersionPolicy.valueOf(name);
		} catch (IllegalArgumentException ex) {
			return getDefault();
		}
	}

	/**
	 * Returns the default version policy.
	 *
	 * @return the default version policy MICRO_LATEST.
	 */
	public static OSGiVersionPolicy getDefault() {
		return MICRO_LATEST;
	}

}
