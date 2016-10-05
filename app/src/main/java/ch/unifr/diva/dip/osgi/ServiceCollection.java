package ch.unifr.diva.dip.osgi;

import java.util.List;
import org.osgi.framework.Version;

/**
 * A service collection.
 *
 * @param <T> type/interface of the service.
 */
public interface ServiceCollection<T> {

	/**
	 * Returns the PID of the OSGi services in the collection.
	 *
	 * @return the PID of the OSGi services in the collection.
	 */
	public String pid();

	/**
	 * Returns the number of available versions of the service.
	 *
	 * @return the number of available versions of the service.
	 */
	public int numVersions();

	/**
	 * Returns a list of all available versions of the service.
	 *
	 * @return a list of all available versions of the service.
	 */
	public List<OSGiService<T>> getVersions();

	/**
	 * Returns the latest available version of the service.
	 *
	 * @return the most recent version of the service, or null if none are
	 * available.
	 */
	public OSGiService<T> getService();

	/**
	 * Returns the latest version within the given range.
	 *
	 * @param range semantic version range string.
	 * @return the latest service within the given version range, or null if not
	 * available.
	 */
	public OSGiService<T> getService(String range);

	/**
	 * Returns the service with the given version.
	 *
	 * @param version semantic version.
	 * @return the service with the given version, or null if not available.
	 */
	public OSGiService<T> getService(Version version);

	/**
	 * Returns the latest {@code major} version.
	 *
	 * @param major the major version.
	 * @return the latest service with given major version, or null if not
	 * available.
	 */
	default OSGiService<T> getService(int major) {
		final String range = String.format(
				"[%d.0.0, %d.0.0)",
				major, major + 1
		);
		return getService(range);
	}

	/**
	 * Returns the latest {@code major.minor} version.
	 *
	 * @param major the major version.
	 * @param minor the minor version.
	 * @return the latest service with given major and minor version, or null if
	 * not available.
	 */
	default OSGiService<T> getService(int major, int minor) {
		final String range = String.format(
				"[%d.%d.0, %d.%d.0)",
				major, minor, major, minor + 1
		);
		return getService(range);
	}

	/**
	 * Returns the exact {@code major.minor.micro} version.
	 *
	 * @param major the major version.
	 * @param minor the minor version.
	 * @param micro the micro version.
	 * @return the service with given major, minor and micro version, or null if
	 * not available.
	 */
	default OSGiService<T> getService(int major, int minor, int micro) {
		final String range = String.format(
				"[%d.%d.%d, %d.%d.%d)",
				major, minor, micro, major, minor, micro + 1
		);
		return getService(range);
	}

}
