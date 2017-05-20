package ch.unifr.diva.dip.core;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.ILoggerFactory;

/**
 * LOGBack (the generic, reliable fast & flexible logging framework)
 * configurations.
 */
public enum LogBackConfig {

	/**
	 * The disabled logging configuration. Detaches and stops all appenders. No
	 * console output, no file appenders, nothing.
	 */
	DISABLED() {

				@Override
				public void config(LoggerContext loggerContext, ApplicationDataManager dataManager) {
					final Logger logback = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
					logback.detachAndStopAllAppenders();
				}

			},
	/**
	 * The default LOGBack logging configuration. This is a no-op, leaving you
	 * with LOGBack's default configuration consiting of console output only at
	 * DEBUG level.
	 */
	DEFAULT_LOGBACK() {

				@Override
				public void config(LoggerContext loggerContext, ApplicationDataManager dataManager) {
					// no-op
				}

			},
	/**
	 * The production logging configuration. Configures a file and a console
	 * appender.
	 */
	PRODUCTION() {

				@Override
				public void config(LoggerContext loggerContext, ApplicationDataManager dataManager) {
					// configure file appender
					final Path logFile = dataManager.appDataDir.logDir.resolve(
							String.format(
									"dip_%s.log",
									LocalDateTime.now().format(
											DateTimeFormatter.ofPattern("yyyy-MM-dd")
									)
							)
					);
					final FileAppender fileAppender = newFileAppender(
							loggerContext,
							"main",
							logFile.toString()
					);
					final PatternLayoutEncoder fileEncoder = newPatternLayoutEncoder(
							loggerContext,
							PATTERN_DEFAULT
					);
					fileEncoder.start();
					fileAppender.setEncoder(fileEncoder);
					final ThresholdFilter fileFilter = newThresholdFilter(
							"DEBUG"
					);
					fileFilter.start();
					fileAppender.addFilter(fileFilter);
					fileAppender.start();

					// configure console appender
					final ConsoleAppender consoleAppender = newConsoleAppender(
							loggerContext,
							"console"
					);
					final PatternLayoutEncoder consoleEncoder = newPatternLayoutEncoder(
							loggerContext,
							PATTERN_DEFAULT
					);
					consoleEncoder.start();
					consoleAppender.setEncoder(consoleEncoder);
					final ThresholdFilter consoleFilter = newThresholdFilter(
							"INFO"
					);
					consoleFilter.start();
					consoleAppender.addFilter(consoleFilter);
					consoleAppender.start();

					// reconfigure root logger
					final Logger logback = loggerContext.getLogger(
							Logger.ROOT_LOGGER_NAME
					);
					logback.detachAndStopAllAppenders();
					logback.addAppender(consoleAppender);
					logback.addAppender(fileAppender);
				}

			};

	/**
	 * Default pattern.
	 */
	private final static String PATTERN_DEFAULT = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n";

	private static ConsoleAppender newConsoleAppender(LoggerContext loggerContext, String name) {
		final ConsoleAppender appender = new ConsoleAppender();
		appender.setContext(loggerContext);
		appender.setName(name);
		return appender;
	}

	private static FileAppender newFileAppender(LoggerContext loggerContext, String name, String file) {
		final FileAppender appender = new FileAppender();
		appender.setContext(loggerContext);
		appender.setName(name);
		appender.setPrudent(true);
		appender.setFile(file);
		return appender;
	}

	private static PatternLayoutEncoder newPatternLayoutEncoder(LoggerContext loggerContext, String pattern) {
		final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(loggerContext);
		encoder.setPattern(pattern);
		return encoder;
	}

	private static ThresholdFilter newThresholdFilter(String level) {
		final ThresholdFilter filter = new ThresholdFilter();
		filter.setLevel(level);
		return filter;
	}

	/**
	 * Configures the logger.
	 *
	 * @param loggerContext LOGBack's LoggerContext.
	 * @param dataManager the application data manager.
	 */
	public abstract void config(LoggerContext loggerContext, ApplicationDataManager dataManager);

	/**
	 * Configures the logger.
	 *
	 * @param factory the ILoggerFactory from slf4j. Assumes SLF4J is bound to
	 * logback-classic in the current environment.
	 * @param dataManager the application data manager.
	 */
	public void config(ILoggerFactory factory, ApplicationDataManager dataManager) {
		config((LoggerContext) factory, dataManager);
	}

	/**
	 * Returns a comma separated list of available LOGBack configurations.
	 *
	 * @return a comma separated list of available LOGBack configurations.
	 */
	public static String getOptionsString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, n = values().length; i < n; i++) {
			final LogBackConfig cfg = values()[i];
			sb.append(cfg.name());
			if (i == (n - 2)) {
				sb.append(", or ");
			} else if ((i + 1) < n) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the default LOGBack configuration.
	 *
	 * @return the default LOGBack configuration.
	 */
	public static LogBackConfig getDefault() {
		return LogBackConfig.DEFAULT_LOGBACK;
	}

	/**
	 * Safely returns a valid LOGBack configuration.
	 *
	 * @param name the name of the LOGBack configuration.
	 * @return the requested LOGBack configuration, or the default one.
	 */
	public static LogBackConfig get(String name) {
		try {
			return LogBackConfig.valueOf(name);
		} catch (IllegalArgumentException ex) {
			return getDefault();
		}
	}

}
