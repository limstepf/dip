package ch.unifr.diva.dip.osgi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi bundle tracker.
 */
public class OSGiBundleTracker {

	private static final Logger log = LoggerFactory.getLogger(OSGiBundleTracker.class);
	private final BundleContext context;
	private final TrackerCustomizer customizer;
	private final BundleTracker<Bundle> tracker;

	/**
	 * Creates a new OSGi bundle tracker.
	 *
	 * @param context the bundle context.
	 */
	public OSGiBundleTracker(BundleContext context) {
		this(
				context,
				BundleEvent.INSTALLED
				| BundleEvent.STARTED
				| BundleEvent.STOPPED
				| BundleEvent.UPDATED
				| BundleEvent.UNINSTALLED
				| BundleEvent.RESOLVED
				| BundleEvent.UNRESOLVED
				| BundleEvent.STARTING
				| BundleEvent.STOPPING
				| BundleEvent.LAZY_ACTIVATION
		);
	}

	/**
	 * Creates a new OSGi bundle tracker.
	 *
	 * @param context the bundle context.
	 * @param stateMask the state mask of the bundle tracker.
	 */
	public OSGiBundleTracker(BundleContext context, int stateMask) {
		this.context = context;
		this.customizer = new TrackerCustomizer();
		this.tracker = new BundleTracker<>(
				context,
				stateMask,
				this.customizer
		);
	}

	/**
	 * Opens the bundle tracker.
	 */
	public final void open() {
		tracker.open();
	}

	/**
	 * Closes the bundle tracker.
	 */
	public final void close() {
		tracker.close();
	}

	/**
	 * Returns the bundles tracked by the bundle tracker.
	 *
	 * @return a list of tracked bundles.
	 */
	public List<Bundle> getBundles() {
		return Arrays.asList(tracker.getBundles());
	}

	/**
	 * Returns a sorted list fo bundles tracked by the bundle tracker.
	 *
	 * @return a sorted list (by bundle id) of tracked bundles.
	 */
	public List<Bundle> getSortedBundles() {
		final List<Bundle> list = getBundles();
		Collections.sort(list, (Bundle b, Bundle b1) -> {
			return Long.compare(b.getBundleId(), b1.getBundleId());
		});
		return list;
	}

	/**
	 * Returns the bundle state (as {@code String}).
	 *
	 * @param bundle the bundle.
	 * @return the bundle state.
	 */
	public static String getBundleState(Bundle bundle) {
		switch (bundle.getState()) {
			case 1:
				return "UNINSTALLED";
			case 2:
				return "INSTALLED";
			case 4:
				return "RESOLVED";
			case 8:
				return "STARTING";
			case 16:
				return "STOPPING";
			case 32:
				return "ACTIVE";
			default:
				return "-";
		}
	}

	/**
	 * Adds a tracker listener.
	 *
	 * @param listener the tracker listener.
	 */
	public void addListener(TrackerListener listener) {
		customizer.addListener(listener);
	}

	/**
	 * Removes a tracker listener.
	 *
	 * @param listener the tracker listener.
	 */
	public void removeListener(TrackerListener listener) {
		customizer.removeListener(listener);
	}

	/**
	 * Bundle tracker listener.
	 */
	public interface TrackerListener {

		/**
		 * Fires if a bundle is detected and tracked by the bundle tracker.
		 *
		 * @param bundle the bundle.
		 * @param event the bundle event.
		 */
		public void onAdded(Bundle bundle, BundleEvent event);

		/**
		 * Fires if a bundle has been modified.
		 *
		 * @param bundle the bundle.
		 * @param event the bundle event.
		 */
		public void onModified(Bundle bundle, BundleEvent event);

		/**
		 * Fires if a bundle got removed.
		 *
		 * @param bundle the bundle.
		 * @param event the bundle event.
		 */
		public void onRemoved(Bundle bundle, BundleEvent event);

	}

	/**
	 * Bundle tracker customizer.
	 */
	private static class TrackerCustomizer implements BundleTrackerCustomizer<Bundle> {

		private final CopyOnWriteArrayList<TrackerListener> listeners;

		/**
		 * Creates a new bundle tracker customizer.
		 */
		public TrackerCustomizer() {
			listeners = new CopyOnWriteArrayList<>();
		}

		/**
		 * Adds a tracker listener.
		 *
		 * @param listener the tracker listener.
		 */
		public void addListener(TrackerListener listener) {
			listeners.add(listener);
		}

		/**
		 * Removes a tracker listener.
		 *
		 * @param listener the tracker listener.
		 */
		public void removeListener(TrackerListener listener) {
			listeners.remove(listener);
		}

		@Override
		public Bundle addingBundle(Bundle bundle, BundleEvent event) {
			for (TrackerListener listener : listeners) {
				listener.onAdded(bundle, event);
			}
			return bundle;
		}

		@Override
		public void modifiedBundle(Bundle bundle, BundleEvent event, Bundle object) {
			for (TrackerListener listener : listeners) {
				listener.onModified(bundle, event);
			}
		}

		@Override
		public void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
			for (TrackerListener listener : listeners) {
				listener.onRemoved(bundle, event);
			}
		}
	}

}
