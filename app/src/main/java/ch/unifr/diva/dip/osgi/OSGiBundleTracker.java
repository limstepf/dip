package ch.unifr.diva.dip.osgi;

import java.util.Arrays;
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
	private final BundleTracker tracker;

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

	public OSGiBundleTracker(BundleContext context, int stateMask) {
		this.context = context;
		this.customizer = new TrackerCustomizer();
		this.tracker = new BundleTracker(
				context,
				stateMask,
				this.customizer
		);
	}

	public final void open() {
		tracker.open();
	}

	public final void close() {
		tracker.close();
	}

	public List<Bundle> getBundles() {
		return Arrays.asList(tracker.getBundles());
	}

	public void addListener(TrackerListener listener) {
		customizer.addListener(listener);
	}

	public void removeListener(TrackerListener listener) {
		customizer.removeListener(listener);
	}

	public interface TrackerListener<T> {
		public void onAdded(Bundle bundle, BundleEvent event);
		public void onModified(Bundle bundle, BundleEvent event);
		public void onRemoved(Bundle bundle, BundleEvent event);
	}

	private static class TrackerCustomizer implements BundleTrackerCustomizer {

		private final CopyOnWriteArrayList<TrackerListener> listeners;

		public TrackerCustomizer() {
			listeners = new CopyOnWriteArrayList<>();
		}

		public void addListener(TrackerListener listener) {
			listeners.add(listener);
		}

		public void removeListener(TrackerListener listener) {
			listeners.remove(listener);
		}

		@Override
		public Object addingBundle(Bundle bundle, BundleEvent event) {
			for (TrackerListener listener : listeners) {
				listener.onAdded(bundle, event);
			}
			return bundle;
		}

		@Override
		public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
			for (TrackerListener listener : listeners) {
				listener.onModified(bundle, event);
			}
		}

		@Override
		public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
			for (TrackerListener listener : listeners) {
				listener.onRemoved(bundle, event);
			}
		}

	}
}
