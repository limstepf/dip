package ch.unifr.diva.dip.core;

import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.osgi.OSGiFramework;
import ch.unifr.diva.dip.osgi.ExtraSystemPackages;
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
		log.info("Current working directory: {}", dataManager.workingDir);
		log.info("Application directory: {}", dataManager.appDir);
		log.info("User directory: {}", dataManager.userDir);
		log.info("Application data directory: {}", dataManager.appDir);

		// init thread pools/executor services
		threadPool = new DipThreadPool();
		discardingThreadPool = DipThreadPool.newDiscardingThreadPool("dip-discarding-pool", 1, 1);

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
						ExtraSystemPackages.getSystemPackages()
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
					log.error("error reading application settings file: {}",
							dataManager.appDataDir.settingsFile, ex);
				}
			}
		}

		log.info("initializing new application settings file");
		return new UserSettings();
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
