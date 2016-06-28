package ch.unifr.diva.dip;

import ch.unifr.diva.dip.core.ApplicationContext;
import ch.unifr.diva.dip.core.ApplicationSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document Image Processor (dip).
 */
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);
	private static ApplicationContext context;
	private static Map<String, List<String>> params;

	public static void main(String[] args) {
		welcome(args);

		context = new ApplicationContext(ApplicationSettings.appDataDirName);
		params = parseArgs(args);

		if (params.containsKey("b")) {
			// run with command-line interface (CLI) only
			// ...
			final MainCLI cli = new MainCLI(context, params);
		} else {
			// run with graphical user interface (GUI)
			Application.launch(MainGUI.class, args);
		}

		// close application resources
		context.close();

		// shutdown
		context.waitForStop();
		log.info("kthxbai.");
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
	 * Returns parsed command-line arguments, {@literal a.k.a} the parameters.
	 *
	 * @return A map of parameters where key is the option pointing to a list of
	 * values.
	 */
	public static Map<String, List<String>> getParams() {
		return params;
	}

	/**
	 * Log some basic system information.
	 *
	 * @param args Command-line arguments.
	 */
	public static void welcome(String[] args) {
		log.info("Command-Line Arguments: {}", Arrays.toString(args));
		log.info("Java Version: {}", System.getProperty("java.runtime.version"));
		log.info("Java Vendor: {}", System.getProperty("java.specification.vendor"));
		log.info("JVM Version: {}", System.getProperty("java.vm.version"));
		log.info("JVM Vendor: {}", System.getProperty("java.vm.vendor"));
		log.info("JVM Runtime: {}", System.getProperty("java.vm.name"));
		log.info("Operating System: {}, {}", System.getProperty("os.name"),
				System.getProperty("os.version"));
		log.info("Architecture: {}", System.getProperty("os.arch"));
	}

	/**
	 * Parse the command-line arguments.
	 *
	 * @param args Command-line arguments.
	 * @return A map of options (as keys) together with a list of values.
	 */
	public static Map<String, List<String>> parseArgs(String[] args) {
		HashMap<String, List<String>> p = new HashMap<>();
		List<String> values = null;

		for (String arg : args) {
			switch (arg.charAt(0)) {
				case '-':
					if (arg.length() > 1) {
						values = new ArrayList<>();
						p.put(arg.substring(1), values);
					}
					break;

				default:
					if (values == null) {
						values = new ArrayList<>();
						p.put("", values);
					}
					values.add(arg);
					break;
			}
		}

		return p;
	}

}
