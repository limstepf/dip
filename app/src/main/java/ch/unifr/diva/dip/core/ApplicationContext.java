package ch.unifr.diva.dip.core;

import ch.unifr.diva.dip.api.services.Preset;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.core.model.DipData;
import ch.unifr.diva.dip.osgi.OSGiServiceRecollection;
import ch.unifr.diva.dip.core.model.PresetData;
import ch.unifr.diva.dip.osgi.OSGiFramework;
import ch.unifr.diva.dip.osgi.ExtraSystemPackages;
import ch.unifr.diva.dip.osgi.OSGiService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ApplicationContext class encapsulates resources used by the application.
 * Components of the application context are (supposed to be) safely published
 * (visibility) and thread-safe.
 */
public class ApplicationContext {

	private static final Logger log = LoggerFactory.getLogger(ApplicationContext.class);
	private final List<ContextError> errors = new ArrayList<>();

	/**
	 * The data manager knows about the filesystem.
	 */
	public final ApplicationDataManager dataManager;

	/**
	 * The application wide thread pool/executor service.
	 */
	public final DipThreadPool threadPool;

	/**
	 * A discarding thread pool. Single worker, and a work queue size of 1.
	 */
	public final DipThreadPool discardingThreadPool;

	/**
	 * The plugin/service framework.
	 */
	public final OSGiFramework osgi;

	/**
	 * The OSGi {@code Processor} service recollection.
	 */
	public final OSGiServiceRecollection<Processor> osgiProcessorRecollection;

	/**
	 * The user settings.
	 */
	public final UserSettings settings;

	/**
	 * ApplicationContext constructor. While the constructor itself doesn't
	 * throw, some of its components might, in which case the exceptions are
	 * wrapped together with an error message in an {@code Error} instance. An
	 * {@code ApplicationContext} instance is *not* valid in case the list of
	 * errors isn't empty!
	 *
	 * @param appDataDirName Directory name for the application data (somewhere
	 * in user-land). Should start with a period to make it a hidden directory.
	 * @param logBackConfig a LOGBack configuration.
	 */
	public ApplicationContext(String appDataDirName, LogBackConfig logBackConfig) {
		this(ApplicationDataManager.userDirectory(), appDataDirName, logBackConfig);
	}

	/**
	 * ApplicationContext constructor. While the constructor itself doesn't
	 * throw, some of its components might, in which case the exceptions are
	 * wrapped together with an error message in an {@code Error} instance. An
	 * {@code ApplicationContext} instance is *not* valid in case the list of
	 * errors isn't empty!
	 *
	 * @param parent Parent directory of the {@code appDataDirName} directory.
	 * This is usually the user's home directory.
	 * @param appDataDirName Directory name for the application data (somewhere
	 * in user-land). Should start with a period to make it a hidden directory.
	 */
	public ApplicationContext(Path parent, String appDataDirName) {
		this(parent, appDataDirName, LogBackConfig.DEFAULT_LOGBACK);
	}

	/**
	 * ApplicationContext constructor. While the constructor itself doesn't
	 * throw, some of its components might, in which case the exceptions are
	 * wrapped together with an error message in an {@code Error} instance. An
	 * {@code ApplicationContext} instance is *not* valid in case the list of
	 * errors isn't empty!
	 *
	 * @param parent Parent directory of the {@code appDataDirName} directory.
	 * This is usually the user's home directory.
	 * @param appDataDirName Directory name for the application data (somewhere
	 * in user-land). Should start with a period to make it a hidden directory.
	 * @param logBackConfig a LOGBack configuration.
	 */
	public ApplicationContext(Path parent, String appDataDirName, LogBackConfig logBackConfig) {
		// init data manager/filesystem
		ApplicationDataManager tmpDataManager = null;
		try {
			tmpDataManager = new ApplicationDataManager(parent, appDataDirName);
		} catch (IOException ex) {
			errors.add(new ContextError(
					ex,
					"Error initializing the application data manager")
			);
		}
		dataManager = tmpDataManager;

		// logger configuration
		logBackConfig.config(
				LoggerFactory.getILoggerFactory(),
				dataManager
		);

		// log data manager main directories, now that the logger is configured.
		if (dataManager != null) {
			log.debug("Current working directory: {}", dataManager.workingDir);
			log.debug("Application directory: {}", dataManager.appDir);
			log.debug("Application data directory: {}", dataManager.appDataDir);
			log.debug("User directory: {}", dataManager.userDir);
		}

		// init thread pools/executor services
		threadPool = new DipThreadPool();
		discardingThreadPool = DipThreadPool.newDiscardingThreadPool("dip-discarding-pool", 1, 1);

		// init osgi service recollection
		osgiProcessorRecollection = getOSGiProcessorRecollection();

		// init OSGi framework
		OSGiFramework tmpOsgi = null;
		if (dataManager != null) {
			final List<Path> watchDirs = Arrays.asList(dataManager.appDir.appBundleDir,
					dataManager.appDataDir.userBundleDir
			);

			try {
				tmpOsgi = new OSGiFramework(
						dataManager.appDir.coreBundleDir,
						watchDirs,
						dataManager.appDataDir.bundleCacheDir,
						ExtraSystemPackages.getSystemPackages(),
						osgiProcessorRecollection
				);
			} catch (IOException | BundleException ex) {
				errors.add(new ContextError(
						ex,
						"Error initializing the OSGi framework")
				);
			}
		}
		osgi = tmpOsgi;

		// init application settings
		settings = getUserSettings();

		// init L10n
		ch.unifr.diva.dip.api.utils.L10n.setLocale(settings.getLocale());
	}

	/**
	 * Loads the OSGi {@code Processor} service recollection from disk, or
	 * initializes an empty recollection if no recollection exists yet.
	 *
	 * @return the OSGi {@code Processor} service recollection.
	 */
	private OSGiServiceRecollection<Processor> getOSGiProcessorRecollection() {
		if (dataManager != null) {
			if (Files.exists(dataManager.appDataDir.osgiProcessorRecollectionFile)) {
				try {
					final OSGiServiceRecollection<Processor> recollection = OSGiServiceRecollection.load(
							dataManager.appDataDir.osgiProcessorRecollectionFile
					);
					recollection.setNewServiceHandler(newOSGiProcessorRecollectionHandler);
					return recollection;
				} catch (JAXBException ex) {
					log.error(
							"error reading the OSGi processor service recollection file: {}",
							dataManager.appDataDir.osgiProcessorRecollectionFile,
							ex
					);
				}
			}
		}
		return new OSGiServiceRecollection<>(newOSGiProcessorRecollectionHandler);
	}

	private OSGiServiceRecollection.NewServiceHandler<Processor> newOSGiProcessorRecollectionHandler = (service) -> onNewProcessor(service);

	/**
	 * Handles the first time a version of a processor is seen on the user's
	 * system.
	 *
	 * @param service the service.
	 */
	private void onNewProcessor(OSGiService<Processor> service) {
		threadPool.getExecutorService().execute(() -> {
			log.debug(
					"OSGi Processor service recollection, new service: {}",
					service
			);

			if (service == null || service.serviceObject == null) {
				return;
			}

			final List<Preset> presets = service.serviceObject.presets();
			if (presets == null || presets.isEmpty()) {
				return;
			}

			// load preset data (or create a new file); currently presets of all
			// versions of a certain processor are stored in the same file
			final String pid = service.pid;
			final String version = PresetData.toPresetVersion(service.version);
			final Path file = dataManager.appDataDir.getProcessorPresetPath(pid);
			final DipData data;

			log.debug(
					"Installing {} default/shipped presets of processor: {}, version: {}",
					presets.size(),
					pid,
					version
			);

			try {
				data = DipData.load(file);
			} catch (IOException | JAXBException | ClassCastException ex) {
				// don't mess with user files in this case, he may still delete an
				// invalid file manually (or fix it)
				log.warn(
						"failed to load preset data from: {}. "
						+ "Aborting installation of default presets of processor: {}",
						file,
						service
				);
				return;
			}

			// append default/shipped presets
			final PresetData presetData = data.getPresetData();
			for (Preset preset : presets) {
				presetData.addPreset(
						new PresetData.Preset(
								pid,
								version,
								preset.getName(),
								preset.getParameters()
						)
				);
			}

			// and write it back to disk
			try {
				data.save(file);
			} catch (Exception ex) {
				log.warn(
						"failed to write preset file back to: {}. "
						+ "Aborting installation of default presets of processor: {}",
						file,
						service
				);
			}
		});
	}

	/**
	 * Writes the OSGi {@code Processor} service recollection back to disk.
	 *
	 * @return {@code true} in case of success, {@code false} otherwise.
	 */
	public boolean saveOSGiProcessorRecollection() {
		try {
			osgiProcessorRecollection.save(
					dataManager.appDataDir.osgiProcessorRecollectionFile
			);
			return true;
		} catch (JAXBException ex) {
			log.error(
					"error writing OSGi processor service recollection back to disk: {}",
					dataManager.appDataDir.osgiProcessorRecollectionFile,
					ex
			);
			return false;
		}
	}

	/**
	 * Loads the user settings from disk, or initializes new default user
	 * settings if no settings file is found.
	 *
	 * @return The application settings.
	 */
	private UserSettings getUserSettings() {
		if (dataManager != null) {
			if (Files.exists(dataManager.appDataDir.settingsFile)) {
				try {
					return UserSettings.load(dataManager.appDataDir.settingsFile);
				} catch (JAXBException ex) {
					log.error(
							"error reading application settings file: {}",
							dataManager.appDataDir.settingsFile,
							ex
					);
				}
			}
		}

		log.info("initializing new application settings file");
		return new UserSettings();
	}

	/**
	 * Performs cleanup before closing the application. Removes temporary files
	 * or outdated logs, and what not. A call to this method is optional and can
	 * be ommitted in tests and similar scenarios.
	 */
	public void cleanup() {
		dataManager.appDataDir.deleteTemporaryFiles();
		dataManager.appDataDir.deleteLogFiles();
	}

	/**
	 * Close open application resources ({@literal e.g.} the OSGi framework).
	 */
	public void close() {
		if (threadPool != null) {
			threadPool.stop();
		}
		if (discardingThreadPool != null) {
			discardingThreadPool.stop();
		}

		if (osgi != null) {
			try {
				osgi.stop();
			} catch (BundleException ex) {
				log.warn("stopping the OSGi framework could not be initiated", ex);
			}
		}
	}

	/**
	 * Wait until all application resources ({@literal e.g.} the OSGi framework)
	 * have been terminated.
	 */
	public void waitForStop() {
		// gracefully shutdown the thread pools
		if (threadPool != null) {
			threadPool.waitForStop();
		}
		if (discardingThreadPool != null) {
			discardingThreadPool.waitForStop();
		}

		// gracefully shutdown OSGi framework (and all installed bundles...)
		if (osgi != null) {
			try {
				osgi.waitForStop();
			} catch (InterruptedException ex) {
				log.error("interrupted shutdown of the OSGi framework", ex);
			}
		}
	}

	/**
	 * Get a list of errors (wrapped exception that have been thrown). An
	 * {@code ApplicationContext} instance is only valid iff the list of errors
	 * is empty.
	 *
	 * @return An empty list - or a list of errors in which case the application
	 * has to be shut down.
	 */
	public List<ContextError> getErrors() {
		return errors;
	}

	/**
	 * The ContextError class wrapps an exception together with an error
	 * message.
	 */
	public static class ContextError {

		public final Exception exception;
		public final String message;

		public ContextError(Exception ex, String msg) {
			exception = ex;
			message = msg;
		}
	}

}
