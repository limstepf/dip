package ch.unifr.diva.dip;

import ch.unifr.diva.dip.core.ApplicationContext;
import ch.unifr.diva.dip.core.ApplicationSettings;
import java.util.Arrays;
import javafx.application.Application;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document Image Processor (dip).
 */
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);
	private static ApplicationContext context;

	/**
	 * The main method of the application.
	 *
	 * @param args the command line arguments.
	 */
	public static void main(String[] args) {
		try {
			CommandLineOption.parse(args);
			if (CommandLineOption.HELP.hasOption()) {
				CommandLineOption.printHelp();
			} else {
				welcome(args);
				context = new ApplicationContext(
						ApplicationSettings.appDataDirName,
						CommandLineOption.getLogConfig()
				);

				if (CommandLineOption.hasAnyOption(
						CommandLineOption.PROCESS,
						CommandLineOption.RESET,
						CommandLineOption.KEEP_ALIVE,
						CommandLineOption.LIST_ALL,
						CommandLineOption.LIST_BUNDLES,
						CommandLineOption.LIST_PROJECT,
						CommandLineOption.LIST_PIPELINES,
						CommandLineOption.LIST_PAGES
				)) {
					// ... headless/with command-line interface (CLI) only
					final MainCLI cli = new MainCLI(context);
				} else {
					// ... with graphical user interface (GUI)
					Application.launch(MainGUI.class, args);
				}
			}
		} catch (ParseException ex) {
			System.out.println(ex);
		}

		kthxbai();
	}

	/**
	 * Shuts down the application.
	 */
	public static void kthxbai() {
		// close application resources
		if (context != null) {
			context.saveOSGiProcessorRecollection();
			context.cleanup();
			context.close();
			// shutdown
			context.waitForStop();
		}
		log.info("kthxbai.");
		System.out.println();
		System.exit(0);
	}

	/**
	 * Returns the application context.
	 *
	 * @return The application context.
	 */
	public static ApplicationContext getApplicationContext() {
		return context;
	}

	/**
	 * Log some basic system information.
	 *
	 * @param args Command-line arguments.
	 */
	public static void welcome(String[] args) {
		log.debug("Command-Line Arguments: {}", Arrays.toString(args));
		log.debug("Java Version: {}", System.getProperty("java.runtime.version"));
		log.debug("Java Vendor: {}", System.getProperty("java.specification.vendor"));
		log.debug("JVM Version: {}", System.getProperty("java.vm.version"));
		log.debug("JVM Vendor: {}", System.getProperty("java.vm.vendor"));
		log.debug("JVM Runtime: {}", System.getProperty("java.vm.name"));
		log.debug("Operating System: {}, {}", System.getProperty("os.name"),
				System.getProperty("os.version"));
		log.debug("Architecture: {}", System.getProperty("os.arch"));
	}

}
