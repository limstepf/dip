package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.utils.FileFinder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.StringJoiner;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGiFramework based on Apache Felix.
 */
public class OSGiFramework {

	private static final Logger log = LoggerFactory.getLogger(OSGiFramework.class);
	private final Framework framework;
	private final BundleContext context;
	private final Path bundleDir;
	private final int defaultBundleStartLevel;
	private final List<Path> bundleWatchDirs;
	private final int watchBundleStartLevel;
	private final Path bundleCacheDir;
//	The bundle tracker is not needed so far, but I'll leave it here outcommented
//  for quick activation/experimentation. Plus we might wanna use it to check if
//  required bundles are (already) loaded, later, for head-less/batch execution...
//	private final OSGiBundleTracker bundleTracker;
	private final OSGiServiceTracker<Processor> processorServiceTracker;
	private final HostServiceTracker<Processor> processorHostServiceTracker;

	/**
	 * {@code Processor} service monitor. Safe to be accessed from the JavaFX
	 * application thread.
	 */
	public final OSGiServiceMonitor<Processor> processors;

	/**
	 * {@code HostProcessor} service monitor. Safe to be accessed from the
	 * JavaFX application thread.
	 */
	public final HostServiceMonitor hostProcessors;

	/**
	 * OSGiFramework constructor.
	 *
	 * @param bundleDir Core/system bundle directory. Bundles are installed and
	 * started immediately and only once (i.e. this directory is unwatched).
	 * @param bundleWatchDirs Additional, and watched bundle directories (i.e.
	 * bundles will be automatically installed, updated, uninstalled, ...).
	 * @param bundleCacheDir The bundle cache directory.
	 * @param systemPackages List of extra system packages which the system
	 * bundle must export from the current execution environment
	 * @throws BundleException
	 * @throws IOException
	 */
	public OSGiFramework(
			Path bundleDir,
			List<Path> bundleWatchDirs,
			Path bundleCacheDir,
			List<ExtraSystemPackages.SystemPackage> systemPackages
	) throws BundleException, IOException {
		this.bundleDir = bundleDir;
		this.defaultBundleStartLevel = 1;
		this.bundleWatchDirs = bundleWatchDirs;
		this.watchBundleStartLevel = 3;
		this.bundleCacheDir = bundleCacheDir;
		this.framework = createFramework(systemPackages);
		this.context = framework.getBundleContext();
//		this.bundleTracker = new OSGiBundleTracker(this.context);
		this.processorServiceTracker = new OSGiServiceTracker(this.context, Processor.class);
		this.processorHostServiceTracker = new HostServiceTracker();
		this.processors = new OSGiServiceMonitor(processorServiceTracker);
		this.hostProcessors = new HostServiceMonitor(processorHostServiceTracker);
//		this.bundleTracker.open();
		this.processorServiceTracker.open();

		registerShutdownHook();

		final List<Bundle> bundles = installBundles(this.bundleDir);
		startBundles(bundles);
	}

	/**
	 * Stops this OSGi Framework.
	 *
	 * @throws BundleException
	 */
	public void stop() throws BundleException {
		framework.stop();
	}

	/**
	 * Wait until this OSGi Framework has completely stopped.
	 *
	 * @throws InterruptedException If an error occurs.
	 */
	public void waitForStop() throws InterruptedException {
		if (framework != null) {
			framework.waitForStop(0);
		}
	}

	/**
	 * Returns the bundle's execution context within the framework.
	 *
	 * @return the bundle context.
	 */
	public BundleContext getBundleContext() {
		return context;
	}

	/**
	 * Creates and starts an embedded OSGi Framework instance.
	 *
	 * @param systemPackages List of extra system packages which the system
	 * bundle must export from the current execution environment
	 * @return OSGi Framework instance.
	 * @throws BundleException If an error occurs.
	 * @see org.apache.felix.main.Main
	 */
	private Framework createFramework(List<ExtraSystemPackages.SystemPackage> systemPackages) throws BundleException {
		final Map config = new HashMap<>();

		// framework/bundle cache directory
		config.put(Constants.FRAMEWORK_STORAGE, bundleCacheDir.toString());

		// clear framework cache on startup
		config.put(Constants.FRAMEWORK_STORAGE_CLEAN,
				Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		// Specifies a comma-separated list of extra packages which the system
		// bundle must export from the current execution environment.
		config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
				toCommaSeparatedString(systemPackages));

		List activators = Arrays.asList(new OSGiHostActivator());
		config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators);

		// The name of the directory to watch. Several directories can be
		// specified by using a comma-separated list.
		config.put("felix.fileinstall.dir",
				toCommaSeparatedString(bundleWatchDirs));

		// Determines if File Install waits felix.fileinstall.poll milliseconds
		// before doing an initial scan or not.
		config.put("felix.fileinstall.noInitialDelay", "true");

		// If set to a value different from 0, File Install will set the start
		// level for deployed bundles to that value. If set to 0, the default
		// framework bundle start level will be used.
		config.put("felix.fileinstall.start.level",
				String.valueOf(watchBundleStartLevel));

		final FrameworkFactory frameworkFactory = ServiceLoader.load(
				FrameworkFactory.class).iterator().next();
		final Framework fwk = frameworkFactory.newFramework(config);

		try {
			fwk.start();
		} catch (BundleException ex) {
			log.error("error starting OSGi framework: ", ex);
			throw ex;
		}

		return fwk;
	}

	/**
	 * Installs all bundles in the given directory (subdirectories inclusive).
	 *
	 * @param directory The bundle directory.
	 * @return List of installed - but not started yet - bundles.
	 * @throws IOException
	 * @see org.apache.felix.main.AutoProcessor
	 */
	private List<Bundle> installBundles(Path directory) throws IOException {
		final List<Bundle> bundles = new ArrayList<>();
		FileFinder finder = new FileFinder("*.jar");
		finder.walkFileTree(directory);
		for (Path file : finder.getMatches()) {
			try {
				final Bundle bundle = context.installBundle(file.toUri().toString());
				final BundleStartLevel bsl = bundle.adapt(BundleStartLevel.class);
				bsl.setStartLevel(defaultBundleStartLevel);
				log.info("installing OSGi bundle: {}", bundle.getSymbolicName());
				bundles.add(bundle);
			} catch (BundleException ex) {
				log.warn("error installing OSGi bundle: {}", file.toString(), ex);
			}
		}
		return bundles;
	}

	/**
	 * Starts a list of installed bundles.
	 *
	 * @param bundles A list of installed bundles.
	 */
	private void startBundles(List<Bundle> bundles) {
		for (Bundle bundle : bundles) {
			try {
				log.info("starting OSGi bundle: {}", bundle.getSymbolicName());
				bundle.start();
			} catch (BundleException ex) {
				log.warn("error starting OSGi bundle: {}", bundle.getSymbolicName(), ex);
			}
		}

		// raise start level so all bundles are started right away
		FrameworkStartLevel fsl = framework.adapt(FrameworkStartLevel.class);
		fsl.setStartLevel(Math.max(defaultBundleStartLevel, watchBundleStartLevel));
	}

	/**
	 * Registers a shutdown hook to make sure the framework is cleanly shut
	 * down.
	 */
	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {
			@Override
			public void run() {
				try {
					if (framework != null) {
						framework.stop();
						framework.waitForStop(0);
					}
				} catch (BundleException | InterruptedException ex) {
					log.warn("error stopping OSGi framework: ", ex);
				}
			}
		});
	}

	/**
	 * Turns a list of objects into a comma separated string using
	 * {@code toString()}.
	 *
	 * @param objects List of objects whose string representation can be
	 * retrieved with a call to {@code toString()}.
	 * @return Comma separated string.
	 */
	private static <T extends Object> String toCommaSeparatedString(List<T> objects) {
		switch (objects.size()) {
			case 0:
				return "";
			case 1:
				return objects.get(0).toString();
			default:
				final StringJoiner j = new StringJoiner(",");
				for (T obj : objects) {
					j.add(obj.toString());
				}
				return j.toString();
		}
	}

	/**
	 * Returns a copy of a list of all tracked processor collections. This does
	 * not include the host services/processors.
	 *
	 * @return a list of all processor collections.
	 */
	public List<ServiceCollection<Processor>> getProcessorCollectionList() {
		return processorServiceTracker.getServiceCollectionList();
	}

	/**
	 * Returns a copy of the map of all tracked processor collections. This does
	 * not include the host services/processors.
	 *
	 * @return a map of all processor collections, indexed by their PID.
	 */
	public Map<String, ServiceCollection<Processor>> getProcessorCollectionMap() {
		return processorServiceTracker.getServiceCollectionMap();
	}

	/**
	 * Returns the collection of a processor. A collection stores all available
	 * versions of the same processor. This may return a normal OSGi service, or
	 * a host service.
	 *
	 * @param pid PID of the processor.
	 * @return the OSGi service collection of the processor.
	 */
	public ServiceCollection<Processor> getProcessorCollection(String pid) {
		final ServiceCollection<Processor> collection = processorServiceTracker.getServiceCollection(pid);
		if (collection != null) {
			return collection;
		}

		return processorHostServiceTracker.getServiceCollection(pid);
	}

	/**
	 * Returns a version of a processor. May be a normal OSGi service, or a host
	 * service.
	 *
	 * @param pid PID of the processor.
	 * @param version version of the processor.
	 * @return the OSGiService of the processor with desired version, or null if
	 * not available.
	 */
	public OSGiService<Processor> getProcessor(String pid, Version version) {
		final ServiceCollection<Processor> collection = getProcessorCollection(pid);
		return collection.getService(version);
	}

	/**
	 * Compiles a list of collections with compatible processors. A processor is
	 * considered compatible if all input port and output port types occur at
	 * least once. Anything more specific (like having a some port type multiple
	 * times) isn't supported as of now.
	 *
	 * @param collections the list of collections to be filtered.
	 * @param inputTypes required input port types.
	 * @param outputTypes required output port types.
	 * @return a list of compatible processors.
	 */
	public static List<ServiceCollection<Processor>> getCompatibleProcessors(List<ServiceCollection<Processor>> collections, Collection<String> inputTypes, Collection<String> outputTypes) {
		return getCompatibleProcessors(collections, Collections.EMPTY_LIST, inputTypes, outputTypes);
	}

	/**
	 * Compiles a list of collections with compatible processors. A processor is
	 * considered compatible if all input port and output port types occur at
	 * least once. Anything more specific (like having a some port type multiple
	 * times) isn't supported as of now.
	 *
	 * @param collections the list of collections to be filtered.
	 * @param excludeCollections a list of PIDs of services/collections to skip.
	 * @param inputTypes required input port types.
	 * @param outputTypes required output port types.
	 * @return a list of compatible processors.
	 */
	public static List<ServiceCollection<Processor>> getCompatibleProcessors(List<ServiceCollection<Processor>> collections, List<String> excludeCollections, Collection<String> inputTypes, Collection<String> outputTypes) {
		final List<ServiceCollection<Processor>> compatible = new ArrayList<>();
		for (ServiceCollection<Processor> collection : collections) {
			if (excludeCollections.contains(collection.pid())) {
				continue;
			}
			final ServiceCollection<Processor> c = filterCollection(collection, inputTypes, outputTypes);
			if (c.numVersions() > 0) {
				compatible.add(c);
			}
		}
		return compatible;
	}

	/**
	 * Compiles a map of collections with compatible processors.
	 *
	 * @param collections the list of collections to be filtered.
	 * @param excludeCollections a list of PIDs of services/collections to skip.
	 * @param inputTypes required input port types.
	 * @param outputTypes required output port types.
	 * @return a map of compatible processors, indexed by PID.
	 */
	public static Map<String, ServiceCollection<Processor>> getCompatibleProcessorMap(List<ServiceCollection<Processor>> collections, List<String> excludeCollections, Collection<String> inputTypes, Collection<String> outputTypes) {
		final Map<String, ServiceCollection<Processor>> compatible = new HashMap<>();
		for (ServiceCollection<Processor> collection : collections) {
			if (excludeCollections.contains(collection.pid())) {
				continue;
			}
			final ServiceCollection<Processor> c = filterCollection(collection, inputTypes, outputTypes);
			if (c.numVersions() > 0) {
				compatible.put(c.pid(), c);
			}
		}
		return compatible;
	}

	/**
	 * Returns a filtered collection of compatible processors.
	 *
	 * @param collection the service collection.
	 * @param inputTypes required input port types.
	 * @param outputTypes required output port types.
	 * @return a filtered service collection of compatible processors.
	 */
	public static ServiceCollection<Processor> filterCollection(ServiceCollection<Processor> collection, Collection<String> inputTypes, Collection<String> outputTypes) {
		final ServiceCollectionSnapshot<Processor> filtered = new ServiceCollectionSnapshot(collection.pid());
		for (OSGiService<Processor> service : collection.getVersions()) {
			final Processor p = service.serviceObject;
			final List<String> inputs = p.portTypes(p.inputs());
			final List<String> outputs = p.portTypes(p.outputs());

			boolean compatible = true;
			for (String in : inputTypes) {
				if (!inputs.contains(in)) {
					compatible = false;
					break;
				}
				inputs.remove(in);
			}
			if (!compatible) {
				continue;
			}
			for (String out : outputTypes) {
				if (!outputs.contains(out)) {
					compatible = false;
					break;
				}
				outputs.remove(out);
			}
			if (compatible) {
				filtered.add(service);
			}
		}

		return filtered;
	}

}
