package ch.unifr.diva.dip.core;

import ch.unifr.diva.dip.utils.DecoratedPath;
import ch.unifr.diva.dip.utils.FileFinder;
import ch.unifr.diva.dip.utils.FileFinderOption;
import static ch.unifr.diva.dip.utils.IOUtils.getRealDirectory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Data Manager. The Data Manager uses {@code DecoratedPath}s s.t.
 * directories can be extended by its files.
 */
public class ApplicationDataManager {

	private static final Logger log = LoggerFactory.getLogger(ApplicationDataManager.class);

	/**
	 * Current working directory. Directory from where the applicatino has been
	 * started.
	 */
	public final Path workingDir;

	/**
	 * User directory. Safe to write to independent of platform. There might be
	 * more appropriate locations to put app data though (e.g. %APPDATA% on a
	 * windows box). This is usually the user's home directory (or maybe some
	 * tmp. directory while running some tests...).
	 *
	 * This might not actually be the user's home directory (e.g. for
	 * integration tests), hence we don't expose it as such (i.e. private). Use
	 * {@code ApplicationDataManager.userDirectory()} instead.
	 */
	public final Path userDir;

	/**
	 * Application directory. The directory where the application executable is
	 * located. This is probably not safe to write to (e.g. on multi-user
	 * systems, or sandboxed, ...), so use {@code appDataDir} instead.
	 */
	public final ApplicationDirectory appDir;

	/**
	 * Application data directory. Root directory of all (global) application
	 * data. This directory is located in the user home directory and get's
	 * created in case this directory doesn't exist yet.
	 */
	public final ApplicationDataDirectory appDataDir;

	/**
	 * Application Data Manager constructor. The {@code appDataDirName}
	 * directory is created in the user's home directory.
	 *
	 * @param appDataDirName Name of the root directory for (global) application
	 * data. Should start with a dot (e.g. ".dip") to make this a hidden
	 * directory.
	 * @throws IOException Application Data is critical, hence the application
	 * has to shut down in case this constructor throws.
	 */
	public ApplicationDataManager(String appDataDirName) throws IOException {
		this(userDirectory(), appDataDirName);
	}

	/**
	 * Application Data Manager constructor.
	 *
	 * @param parent Parent directory of the {@code appDataDirName} directory.
	 * This is usually (or supposed to be) the user's home directory.
	 * @param appDataDirName Name of the root directory for (global) application
	 * data. Should start with a dot (e.g. ".dip") to make this a hidden
	 * directory.
	 * @throws IOException Application Data is critical, hence the application
	 * has to shut down in case this constructor throws.
	 */
	public ApplicationDataManager(Path parent, String appDataDirName) throws IOException {
		final String jar = ApplicationDataManager.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();

		workingDir = Paths.get(System.getProperty("user.dir")).toRealPath();
		appDir = new ApplicationDirectory(
				Paths.get(new File(jar).getParent()).toRealPath()
		);
		userDir = parent.toRealPath();
		appDataDir = new ApplicationDataDirectory(
				getRealDirectory(userDir.resolve(appDataDirName))
		);
	}

	/**
	 * Returns the user's home directory.
	 *
	 * @return The user's home directory.
	 */
	public static Path userDirectory() {
		return Paths.get(System.getProperty("user.home"));
	}

	/**
	 * Returns a path to an empty, temporary file. A temporary file doesn't need
	 * to be explicitly deleted, since the contents of the temporary directory
	 * will get cleaned upon start and proper shutdown of the application.
	 *
	 * @return path to a new temporary file that gets deleted once the JVM shuts
	 * down.
	 * @throws java.io.IOException
	 */
	public Path tmpFile() throws IOException {
		return tmpFile(false);
	}

	/**
	 * Returns a path to a new temporary file. A temporary file doesn't need to
	 * be explicitly deleted, since the contents of the temporary directory will
	 * get cleaned upon start and proper shutdown of the application.
	 *
	 * @param pathOnly if set to {@code true} no file will be created (or rather
	 * removed right away) and only a proper tmp. path will be returned. The
	 * file still get's deleted after being created and the JVM shuts down. If
	 * {@code false}, then an empty file will exist, where the returned path is
	 * pointing to.
	 * @return path to a new temporary file that gets deleted once the JVM shuts
	 * down.
	 * @throws java.io.IOException
	 */
	public Path tmpFile(boolean pathOnly) throws IOException {
		final Path tmp = Files.createTempFile(this.appDataDir.tmpDir, "", ".tmp");
		tmp.toFile().deleteOnExit();
		if (pathOnly) {
			Files.delete(tmp);
		}
		return tmp;
	}

	/**
	 * Returns a temporary copy of the given file. The copy will be deleted once
	 * the JVM properly shuts down.
	 *
	 * @param file file to copy
	 * @return path to the temporary copy of the given file.
	 * @throws IOException
	 */
	public Path tmpCopy(Path file) throws IOException {
		final Path copy = tmpFile();
		Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
		return copy;
	}

	/**
	 * The application data directory (in userland).
	 */
	public static class ApplicationDataDirectory extends DecoratedPath {

		/**
		 * User bundle directory. Watched bundle directory (in user-land).
		 */
		public final Path userBundleDir;

		/**
		 * OSGi/Bundle cache directory.
		 */
		public final Path bundleCacheDir;

		/**
		 * Presets directory.
		 */
		public final Path presetsDir;

		/**
		 * Processor presets directory.
		 */
		public final Path processorPresetsDir;

		/**
		 * Directory for temporary files (scratch space).
		 */
		public final Path tmpDir;

		/**
		 * Directory for log files.
		 */
		public final Path logDir;

		/**
		 * Application settings file. This file doesn't necessarily exist (yet).
		 */
		public final Path settingsFile;

		/**
		 * File of the OSGi {@code Processor} service recollection.
		 */
		public final Path osgiProcessorRecollectionFile;

		/**
		 * Default constructor.
		 *
		 * @param path path to the application data directory (subdir in
		 * userland).
		 * @throws IOException
		 */
		private ApplicationDataDirectory(Path path) throws IOException {
			super(path);
			this.userBundleDir = getRealDirectory(path.resolve("bundles"));
			this.bundleCacheDir = getRealDirectory(path.resolve("felix-cache"));
			this.presetsDir = getRealDirectory(path.resolve("presets"));
			this.processorPresetsDir = getRealDirectory(this.presetsDir.resolve("processors"));
			this.tmpDir = getRealDirectory(path.resolve("tmp"));
			this.logDir = getRealDirectory(path.resolve("log"));

			this.settingsFile = path.resolve("settings.xml");
			this.osgiProcessorRecollectionFile = path.resolve("processor-recollection.xml");
		}

		/**
		 * Returns the path to the preset file of a plugin (service/processor).
		 * The file might not exist (yet).
		 *
		 * @param pid the pid of the service/processor.
		 * @return a Path to the preset file of the plugin.
		 */
		public Path getProcessorPresetPath(String pid) {
			return this.processorPresetsDir.resolve(pid + ".xml");
		}

		/**
		 * Returns the path to the pipelines preset file. The file might not
		 * exist (yet).
		 *
		 * @return a Path to the pipelines preset file.
		 */
		public Path getPipelinePresetPath() {
			return this.presetsDir.resolve("pipelines.xml");
		}

		/**
		 * Deletes all files in the temporary directory.
		 *
		 * @return {@code true} if successfull, {@code false} otherwise.
		 */
		public boolean deleteTemporaryFiles() {
			final FileFinder finder = new FileFinder("*.*");
			try {
				finder.walkFileTree(tmpDir, FileFinderOption.NONRECURSIVE);
				for (File file : finder.getFiles()) {
					if (file.isFile()) {
						file.delete();
					}
				}
				return true;
			} catch (IOException ex) {
				log.warn("failed to clean up temporary files in: {}", tmpDir, ex);
			}
			return false;
		}

		/**
		 * Deletes all files in the log directory that are older than a week.
		 *
		 * @return {@code true} if successfull, {@code false} otherwise.
		 */
		public boolean deleteLogFiles() {
			final FileFinder finder = new FileFinder("*.*");
			try {
				finder.walkFileTree(logDir, FileFinderOption.NONRECURSIVE);
				final List<File> files = finder.getFiles();
				if (files.size() > 0) {
					final LocalDateTime ts = LocalDateTime.now().minusWeeks(1);
					for (File file : finder.getFiles()) {
						final Instant inst = new Date(file.lastModified()).toInstant();
						if (ts.compareTo(LocalDateTime.ofInstant(inst, ZoneId.systemDefault())) > 0) {
							file.delete();
						}
					}
				}
				return true;
			} catch (IOException ex) {
				log.warn("failed to clean up log files in: {}", logDir, ex);
			}
			return false;
		}

	}

	/**
	 * The application directory. Most likely not safe to write to (missing
	 * permissions/privileges).
	 */
	public static class ApplicationDirectory extends DecoratedPath {

		/**
		 * Core bundle directory. Unwatched bundle directory for required system
		 * bundles (e.g. Felix File Install).
		 */
		public final Path coreBundleDir;

		/**
		 * Application/plugin bundle directory. Watched bundle directory.
		 */
		public final Path appBundleDir;

		/**
		 * Default constructor.
		 *
		 * @param path path to the application directory.
		 * @throws IOException
		 */
		private ApplicationDirectory(Path path) throws IOException {
			super(path);
			this.coreBundleDir = getRealDirectory(path.resolve("bundles-core"));
			this.appBundleDir = getRealDirectory(path.resolve("bundles"));
		}
	}

}
