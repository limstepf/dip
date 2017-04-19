package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.utils.FxUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * A collection of all tracked versions of an OSGi service. With the exception
 * of the {@code FxContext} this class is thread safe, and objects may be
 * accessed from any thread.
 *
 * @param <T> type/interface of the service.
 */
public class OSGiServiceCollection<T> implements ServiceCollection<T> {

	private final String pid;
	private final LinkedList<OSGiService<T>> versions;

	/**
	 * Creates a new OSGi service collection.
	 *
	 * @param service first version in the collection.
	 */
	public OSGiServiceCollection(OSGiService<T> service) {
		this(service.pid);
		add(service);
	}

	/**
	 * Creates a new, empty OSGi service collection.
	 *
	 * @param pid the PID of the OSGi service.
	 */
	public OSGiServiceCollection(String pid) {
		this.pid = pid;
		this.versions = new LinkedList<>();
	}

	@Override
	public String pid() {
		return pid;
	}

	@Override
	public synchronized int numVersions() {
		return versions.size();
	}

	@Override
	public synchronized List<OSGiService<T>> getVersions() {
		return new ArrayList<>(versions);
	}

	@Override
	public synchronized OSGiService<T> getService() {
		if (versions.isEmpty()) {
			return null;
		}

		return versions.get(0);
	}

	@Override
	public synchronized OSGiService<T> getService(String range) {
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
	public synchronized OSGiService<T> getService(Version version) {
		for (OSGiService<T> s : versions) {
			if (s.version.equals(version)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Adds a service to the collection.
	 *
	 * @param service the service.
	 */
	final protected synchronized void add(OSGiService<T> service) {
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

		invalidateFxContext();
	}

	/**
	 * Updates a service in the collection.
	 *
	 * @param service the service.
	 */
	final protected synchronized void update(OSGiService<T> service) {
		final int index = indexOf(service.serviceReference);
		if (index < 0) {
			return;
		}

		versions.set(index, service);
		invalidateFxContext();
	}

	/**
	 * Removes a service from the collection.
	 *
	 * @param service the service.
	 */
	final protected synchronized void remove(OSGiService<T> service) {
		final int index = indexOf(service.serviceReference);
		if (index < 0) {
			return;
		}

		versions.remove(index);
		invalidateFxContext();
	}

	/**
	 * Returns the index of a service, given it's service reference.
	 *
	 * @param reference the service reference.
	 * @return the index of the service in the versions list.
	 */
	private int indexOf(ServiceReference reference) {
		for (int i = 0; i < versions.size(); i++) {
			final OSGiService<T> service = versions.get(i);
			if (service.serviceReference.equals(reference)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "pid=" + pid
				+ ", versions=[" + versionsToString()
				+ "]}";
	}

	private synchronized String versionsToString() {
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (OSGiService<T> service : versions) {
			if (i > 0) {
				sb.append(", ");
			}
			i++;

			sb.append(service.toString());
		}
		return sb.toString();
	}

	/*
	 * we have a lazily initialized FxContext to keep track of the currently
	 * selected version used in ListViews or what not...
	 */
	private FxContext<T> fxContext;

	/**
	 * Returns the FxContext of the collection. The FxContext needs to be
	 * manipulated on the JavaFx Application thread only!
	 *
	 * @return the FxContext of the collection.
	 */
	public FxContext<T> getFxContext() {
		if (fxContext == null) {
			fxContext = new FxContext(this);
		}

		return fxContext;
	}

	private void invalidateFxContext() {
		if (fxContext != null) {
			fxContext.invalidate();
		}
	}

	/**
	 * The JavaFx context of an OSGiServiceCollection. Should only be
	 * manipulated on the JavaFx application thread. The FxContext is used to
	 * keep a version in the collection selected, s.t. the user can choose with
	 * what version to work with (e.g. what version of a processor should be
	 * dragged into the pane of the pipeline editor).
	 *
	 * @param <T> class/interface of the service.
	 */
	public static class FxContext<T> {

		private final OSGiServiceCollection<T> collection;
		private OSGiService<T> selectedVersion;

		/**
		 * Creates a new JavaFx context of the OSGiServiceCollection.
		 *
		 * @param collection the OSGiServiceCollection.
		 */
		public FxContext(OSGiServiceCollection<T> collection) {
			this.collection = collection;
			this.selectedVersion = collection.getService();
		}

		protected void invalidate() {
			FxUtils.run(() -> {
				if (!collection.getVersions().contains(selectedVersion)) {
					selectedVersion = collection.getService();
				}
			});
		}

		/**
		 * Returns a list of the versions in the collection. While this returns
		 * an observable list, to play nice with some JavaFx components, but
		 * other than that, there is no use in listening for changes; this list
		 * is not keept up to date!
		 *
		 * @return a list of the versions.
		 */
		public ObservableList<String> getVersionList() {
			final ObservableList list = FXCollections.observableArrayList();
			for (OSGiService<T> service : collection.getVersions()) {
				list.add(service.version.toString());
			}
			return list;
		}

		/**
		 * Sets the selected version.
		 *
		 * @param index index of the version in the collection.
		 */
		public void setSelectedVersion(int index) {
			setSelectedVersion(collection.getVersions().get(index));
		}

		/**
		 * Sets the selected version.
		 *
		 * @param version the version of the service.
		 */
		public void setSelectedVersion(OSGiService<T> version) {
			if (!collection.getVersions().contains(version)) {
				return;
			}
			this.selectedVersion = version;
		}

		/**
		 * Returns the selected version of the service.
		 *
		 * @return the selected version.
		 */
		public OSGiService<T> getSelectedVersion() {
			return selectedVersion;
		}

		/**
		 * Returns the index of the selected version.
		 *
		 * @return the index of the selected version in the collection.
		 */
		public int getSelctedIndex() {
			return collection.getVersions().indexOf(selectedVersion);
		}

		/**
		 * Returns the OSGi service reference of the selected version. Use to
		 * identify the service dragged into the pane of the pipeline editor.
		 *
		 * @return the OSGi service reference of the selected version.
		 */
		public OSGiServiceReference getOSGiServiceReference() {
			final OSGiService<T> service = getSelectedVersion();
			return new OSGiServiceReference(service.pid, service.version);
		}

		/**
		 * Checks whether the selected version is the latest one available, or
		 * not.
		 *
		 * @return True if the selected version is the latest one available,
		 * False otherwise.
		 */
		public boolean isLatest() {
			return getSelectedVersion().equals(collection.getService());
		}
	}

}
