package ch.unifr.diva.dip.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * Snapshot of an OSGi service collection. Not thread-safe (and way cheaper than
 * a {@code OsgiServiceCollection}). Used for filtered collections or similar
 * "throw away" collections.
 *
 * @param <T> type/interface of the service.
 */
public class ServiceCollectionSnapshot<T> implements ServiceCollection<T> {

	private final String pid;
	private final List<OSGiService<T>> versions;

	/**
	 * Creates a new service collection snapshot.
	 *
	 * @param service the first service in the collection.
	 */
	public ServiceCollectionSnapshot(OSGiService<T> service) {
		this(service.pid);
		add(service);
	}

	/**
	 * Creates a new service collection snapshot.
	 *
	 * @param pid the PID of the OSGi service.
	 */
	public ServiceCollectionSnapshot(String pid) {
		this.pid = pid;
		this.versions = new ArrayList<>();
	}

	/**
	 * Adds a service to the collection.
	 *
	 * @param service the service.
	 */
	final public void add(OSGiService<T> service) {
		if (!pid.equals(service.pid)) {
			throw new RuntimeException(String.format(
					"Can't add `%s` service to `%s` collection",
					service.pid,
					pid
			));
		}

		// keep the list sorted in descending order, s.t. the most recent version
		// is at the first position, the oldest all way back...
		for (int i = 0, n = versions.size(); i <= n; i++) {
			// first version to insert, or oldest one
			if (i == n) {
				versions.add(service);
				break;
			}

			// insert as sonn as the service is newer than the next one
			final OSGiService<T> next = versions.get(i);
			if (service.version.compareTo(next.version) > 0) {
				versions.add(i, service);
				break;
			}
		}
	}

	@Override
	public String pid() {
		return pid;
	}

	@Override
	public int numVersions() {
		return versions.size();
	}

	@Override
	public List<OSGiService<T>> getVersions() {
		return versions;
	}

	@Override
	public OSGiService<T> getService() {
		if (versions.isEmpty()) {
			return null;
		}

		return versions.get(0);
	}

	@Override
	public OSGiService<T> getService(String range) {
		final VersionRange versionRange = new VersionRange(range);
		OSGiService<T> latest = null;

		for (OSGiService<T> s : versions) {
			if (!versionRange.includes(s.version)) {
				continue;
			}
			if (latest == null || s.version.compareTo(latest.version) > 0) {
				latest = s;
			}
		}

		return latest;
	}

	@Override
	public OSGiService<T> getService(Version version) {
		for (OSGiService<T> s : versions) {
			if (s.version.equals(version)) {
				return s;
			}
		}
		return null;
	}

}
