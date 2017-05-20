package ch.unifr.diva.dip;

import ch.unifr.diva.dip.core.LogBackConfig;
import static ch.unifr.diva.dip.utils.IOUtils.NL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The DIP command line options.
 */
public enum CommandLineOption {

	HELP(
			"h", "help", false,
			"prints the help/usage"
	),
	FILE(
			"f", "file", true,
			"the DIP project to be loaded/processed"
	),
	LIST_ALL(
			null, "list-all", false,
			"lists the system information, the installed OSGi bundles, and the project pipelines and pages"
	),
	LIST_SYSTEM(
			null, "list-system", false,
			"lists the system information"
	),
	LIST_BUNDLES(
			null, "list-bundles", false,
			"lists the installed OSGi bundles"
	),
	LIST_PROJECT(
			null, "list-project", false,
			"lists the project pipelines and pages"
	),
	LIST_PIPELINES(
			null, "list-pipelines", false,
			"lists the project pipelines"
	),
	LIST_PAGES(
			null, "list-pages", false,
			"lists the project pages"
	),
	LOG(
			null, "log-config", true, "CFG",
			"set log config with CFG in (" + LogBackConfig.getOptionsString() + ")"
			+ ", default=" + LogBackConfig.getDefault().name()
	),
	PROCESS(
			"p", "process", false,
			"process the project (all pages)"
	),
	RESET(
			"r", "reset", false,
			"reset the project (all pages). Get's executed before processing (if set)"
	),
	DONT_SAVE(
			null, "dont-save", false,
			"prevents the project from being saved, after being processed in headless mode"
	);

	private final static Options options;

	static {
		options = new Options();
		for (CommandLineOption opt : values()) {
			options.addOption(opt.opt, opt.longOpt, opt.hasArg, opt.description);
			if (opt.argName != null) {
				options.getOption(opt.longOpt).setArgName(opt.argName);
			}
		}
	}

	private final static CommandLineParser parser = new DefaultParser();
	private static volatile CommandLine line;

	private final String opt;
	private final String longOpt;
	private final boolean hasArg;
	private final String argName;
	private final String description;

	private CommandLineOption(String opt, String longOpt, boolean hasArg, String description) {
		this(opt, longOpt, hasArg, null, description);
	}

	private CommandLineOption(String opt, String longOpt, boolean hasArg, String argName, String description) {
		this.opt = opt;
		this.longOpt = longOpt;
		this.hasArg = hasArg;
		this.argName = argName;
		this.description = description;
	}

	/**
	 * Parses the command line arguments. Should be called just once in the
	 * application's {@code Main} class, after which the command line options
	 * can be queried from any thread.
	 *
	 * @param arguments the command line arguments.
	 * @throws ParseException
	 */
	public static void parse(String[] arguments) throws ParseException {
		line = parser.parse(options, arguments);
	}

	/**
	 * Checks whether the command line arguments have been parsed successfully.
	 *
	 * @return {@code true} if the command line arguments have been parsed
	 * successfully, {@code false} otherwise.
	 */
	public static boolean hasLine() {
		return line != null;
	}

	/**
	 * Checks whether the command line option is set.
	 *
	 * @param opt the command line option.
	 * @return {@code true} if the command line option is set, {@code false}
	 * otherwise.
	 */
	public static boolean hasOption(CommandLineOption opt) {
		return opt.hasOption();
	}

	/**
	 * Checks whether at least one of the given command line options is set.
	 *
	 * @param options the command line options.
	 * @return {@code true} if any of the command line options is set,
	 * {@code false} otherwise.
	 */
	public static boolean hasAnyOption(CommandLineOption... options) {
		if (line == null) {
			return false;
		}
		for (CommandLineOption opt : options) {
			if (line.hasOption(opt.longOpt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the command line option is set.
	 *
	 * @return {@code true} if the command line option is set, {@code false}
	 * otherwise.
	 */
	public boolean hasOption() {
		if (line == null) {
			return false;
		}
		return line.hasOption(longOpt);
	}

	/**
	 * Returns the value of the command line option.
	 *
	 * @return the value of the command line option.
	 */
	public String getOptionValue() {
		if (line == null) {
			return null;
		}
		return line.getOptionValue(longOpt);
	}

	/**
	 * Prints the help/usage.
	 */
	public static void printHelp() {
		if (line == null) {
			return;
		}
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				"dip", // cmdLineSyntax
				"By using the options "
				+ "--help, --list-XXX, --process, or --reset "
				+ "the application is run in headless mode (no GUI)."
				+ NL + NL, // header
				options,
				"", // footer
				true // autoUsage
		);
		System.out.println();
	}

	/**
	 * Returns the log config, as defined by the command line option (if set).
	 *
	 * @return the requested log config, or the default.
	 */
	public static LogBackConfig getLogConfig() {
		if (line != null && LOG.hasOption()) {
			final String val = LOG.getOptionValue();
			if (val != null) {
				return LogBackConfig.get(val.toUpperCase());
			}
		}
		return LogBackConfig.getDefault();
	}

}
