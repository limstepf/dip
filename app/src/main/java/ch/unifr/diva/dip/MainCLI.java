package ch.unifr.diva.dip;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.core.ApplicationContext;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.UIStrategy;
import ch.unifr.diva.dip.core.ui.UIStrategyCLI;
import ch.unifr.diva.dip.eventbus.EventBus;
import ch.unifr.diva.dip.eventbus.EventBusGuava;
import ch.unifr.diva.dip.eventbus.events.StatusMessageEvent;
import ch.unifr.diva.dip.eventbus.events.StatusWorkerEvent;
import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.core.execution.PipelineExecutionLogger;
import ch.unifr.diva.dip.core.execution.PrintingPipelineExecutionLogger;
import ch.unifr.diva.dip.core.model.PipelineData;
import ch.unifr.diva.dip.core.model.Project;
import ch.unifr.diva.dip.core.model.ProjectData;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import ch.unifr.diva.dip.gui.main.RepairProjectUnit;
import ch.unifr.diva.dip.osgi.OSGiBundleTracker;
import ch.unifr.diva.dip.osgi.OSGiService;
import ch.unifr.diva.dip.osgi.OSGiVersionPolicy;
import ch.unifr.diva.dip.osgi.ServiceCollection;
import ch.unifr.diva.dip.utils.IOUtils;
import ch.unifr.diva.dip.utils.TreePrinter;
import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javax.xml.bind.JAXBException;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line interface (CLI) of the application.
 */
public class MainCLI {

	private static final Logger log = LoggerFactory.getLogger(MainCLI.class);
	private static final String INDENT = "   ";

	private final EventBus eventBus;
	private final UIStrategy uiStrategy;
	private final ApplicationHandler handler;

	/**
	 * Creates the headless/CLI application.
	 *
	 * @param context the application context.
	 */
	public MainCLI(ApplicationContext context) {
		// verify that we have a valid context - or shut down.
		if (!context.getErrors().isEmpty()) {
			printErrors(context.getErrors());
			this.uiStrategy = null;
			this.eventBus = null;
			this.handler = null;
			return;
		}

		this.uiStrategy = new UIStrategyCLI();
		this.eventBus = new EventBusGuava();
		this.handler = new ApplicationHandler(context, uiStrategy, eventBus);

		// manually initialize the JavaFX toolkit
		FxUtils.initToolkit();

		StatusListener status = new StatusListener();
		eventBus.register(status);
		System.out.println();

		// list system information
		if (CommandLineOption.hasAnyOption(
				CommandLineOption.LIST_ALL,
				CommandLineOption.LIST_SYSTEM
		)) {
			System.out.println();
			listSystemInformation(handler);
		}

		waitForOSGiBundles();

		final ProjectData data = readProjectData();
		final Project project = loadProject(data);
		pauseThread();

		// print DIP project information
		if (data != null) {
			if (CommandLineOption.hasAnyOption(
					CommandLineOption.LIST_ALL,
					CommandLineOption.LIST_PROJECT,
					CommandLineOption.LIST_PIPELINES
			)) {
				System.out.println();
				listProjectPipelines(data);
			}

			if (CommandLineOption.hasAnyOption(
					CommandLineOption.LIST_ALL,
					CommandLineOption.LIST_PROJECT,
					CommandLineOption.LIST_PAGES
			)) {
				System.out.println();
				listProjectPages(data);
			}
		}

		if (project != null) {
			// reset all pages
			if (CommandLineOption.RESET.hasOption()) {
				System.out.println();
				System.out.println("resetting project...");
				joinThread(project.resetAllPages().getThread());
			}

			// process all pages
			if (CommandLineOption.PROCESS.hasOption()) {
				System.out.println();
				System.out.println("processing project...");
				// TODO: option to select a different logger; or simple "verbose" toggle?
				final PipelineExecutionLogger logger = new PrintingPipelineExecutionLogger();
				joinThread(project.processAllPages(logger).getThread());
			}

			// save project, unless asked to not do so...
			System.out.println();
			if (!CommandLineOption.DONT_SAVE.hasOption()) {
				System.out.println("saving project...");
				joinThread(handler.saveProject().getThread());
				System.out.println();
				System.out.println("closing project...");
			} else {
				System.out.println("closing project... (without saving)");
			}

			// and close the project
			handler.closeProject();
			pauseThread();
		}

		System.out.println();

		/*
		 * keep alive: this is usefull if we have a shell (e.g. Felix Gogo)
		 * around which allows us to inspect the OSGi environment, and to debug
		 * bundles that just wont start, and what not...
		 */
		if (CommandLineOption.KEEP_ALIVE.hasOption()) {
			try {
				handler.osgi.waitForStop();
			} catch (InterruptedException ex) {
				// so long...
			}
		}
	}

	private void waitForOSGiBundles() {
		System.out.println("waiting for OSGi bundles...");
		boolean osgiTimeout = true;
		try {
			Thread.sleep(500);
			osgiTimeout = !handler.osgi.getProcessors().waitForBundles(3, 500, 10000);
		} catch (InterruptedException ex) {
			log.debug("interrupted while waiting for OSGi bundles to be installed", ex);
		}
		if (osgiTimeout) {
			System.out.println(INDENT + "...timed out.");
		} else {
			System.out.println(INDENT + "ready.");
		}
	}

	private ProjectData readProjectData() {
		ProjectData data;

		if (CommandLineOption.FILE.hasOption()) {
			final String projectFileVal = CommandLineOption.FILE.getOptionValue();
			final Path projectFile = ProjectData.toProjectFile(projectFileVal);
			if (projectFile == null) {
				log.warn("no file found at: {}", projectFileVal);
				return null;
			}

			System.out.println("reading project data from: " + projectFile + "...");
			try {
				data = handler.loadProjectData(projectFile);
			} catch (IOException | JAXBException ex) {
				log.error("failed to read project data from: {}", projectFile, ex);
				return null;
			}
		} else {
			return null;
		}

		// load project assets (the pipelines in particular)
		data.loadAssets();
		System.out.println(INDENT + "ok.");

		return data;
	}

	private Project loadProject(ProjectData data) {
		if (data == null) {
			return null;
		}
		System.out.println("validating project data...");
		final ProjectData.ValidationResult validation = handler.validateProjectData(data);
		if (!validation.isValid()) {
			final CountDownLatch latch = new CountDownLatch(1);
			final RepairProjectUnit repair = new RepairProjectUnit(handler);
			repair.setStopCallback(
					() -> {
						latch.countDown();
					},
					RepairProjectUnit.DEFAULT_TIMEOUT_IN_MS
			);
			repair.start();
			try {
				latch.await();
			} catch (InterruptedException ex) {

			}
			repair.stop();
			if (!repair.canFullyAutoRepair()) {
				log.error("invalid/corrupt project data: {}", validation);
				return null;
			}
			repair.applyRepairs();
		}

		System.out.println(INDENT + "ok.");
		System.out.println("opening project...");
		return handler.openProject(data);
	}

	// short sleep (in ms) after a thread has been joined to enforce correct
	// status message flow/order (i.e. we're waiting for the last status message
	// to be processed before continuing, which happens on the Java FX app. Thread)
	private static final int THREAD_DELAY = 100;

	private static boolean pauseThread() {
		return pauseThread(THREAD_DELAY);
	}

	private static boolean pauseThread(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
			return false;
		}
		return true;
	}

	private static boolean joinThread(Thread thread) {
		try {
			thread.join();
			Thread.sleep(THREAD_DELAY);
		} catch (InterruptedException ex) {
			return false;
		}
		return true;
	}

	private static String validateChecksum(ProjectData.Page page) {
		if (!Files.exists(page.file)) {
			return "FILE NOT FOUND";
		}
		try {
			final String checksum = IOUtils.checksum(page.file);
			if (page.checksum.equals(checksum)) {
				return page.checksum + " (ok)";
			} else {
				return "FILE MODIFIED (checksum mismatch)";
			}
		} catch (IOException ex) {
			return ex.getMessage();
		}
	}

	private static String validateService(ApplicationHandler handler, OSGiVersionPolicy policy, PipelineData.Processor processor) {
		final ServiceCollection<Processor> collection = handler.osgi.getProcessorCollection(processor.pid);
		if (collection == null) {
			return Processor.State.UNAVAILABLE.toString();
		}
		final OSGiService<Processor> service = policy.getService(collection, new Version(processor.version()));
		if (service == null) {
			return Processor.State.UNAVAILABLE.toString();
		}
		final String versionByPolicy = service.version.toString();
		if (versionByPolicy.equals(processor.version())) {
			// exact version
			return processor.version();
		} else {
			// different version (auto up-/downgraded)
			return versionByPolicy + " (AUTO UP/DOWNGRADE by pipeline version policy)";
		}
	}

	private static void printErrors(List<ApplicationContext.ContextError> errors) {
		for (ApplicationContext.ContextError error : errors) {
			log.error(error.message, error.exception);
		}
	}

	private void listProjectPipelines(ProjectData data) {
		final PrintStream stream = TreePrinter.getUTF8PrintStream();
		final TreePrinter printer = new TreePrinter(stream);
		printer.print(
				"Project pipelines",
				data.pipelines(),
				new TreePrinter.Extractor() {
					@Override
					public Object[] getChildren(Object obj) {
						if (obj instanceof PipelineData.Pipeline) {
							final PipelineData.Pipeline pipeline = (PipelineData.Pipeline) obj;
							final OSGiVersionPolicy policy = OSGiVersionPolicy.get(pipeline.versionPolicy);
							return new Object[]{
								"id: " + pipeline.id,
								"version-policy: " + pipeline.versionPolicy,
								new TreePrinter.Node("processors", TreePrinter.toNodes(
												pipeline.processors(),
												(p) -> {
													return new TreePrinter.Node(p.pid(), new Object[]{
														"id: " + p.id,
														"requested-version: " + p.version(),
														"effective-version: " + validateService(handler, policy, p)
													});
												}
										))
							};
						}
						return new Object[]{};
					}

					@Override
					public String getName(Object obj) {
						if (obj instanceof PipelineData.Pipeline) {
							final PipelineData.Pipeline pipeline = (PipelineData.Pipeline) obj;
							return pipeline.name;
						}
						return obj.toString();
					}
				}
		);
	}

	private void listProjectPages(ProjectData data) {
		final PrintStream stream = TreePrinter.getUTF8PrintStream();
		final TreePrinter printer = new TreePrinter(stream);
		printer.print(
				"Project pages",
				data.getPages(),
				new TreePrinter.Extractor() {

					@Override
					public Object[] getChildren(Object obj) {
						if (obj instanceof ProjectData.Page) {
							final ProjectData.Page page = (ProjectData.Page) obj;
							return new Object[]{
								"id: " + page.id,
								"file: " + page.file,
								"checksum: " + validateChecksum(page),
								"pipeline id: " + page.pipelineId
							};
						}
						return new Object[]{};
					}

					@Override
					public String getName(Object obj) {
						if (obj instanceof ProjectData.Page) {
							final ProjectData.Page page = (ProjectData.Page) obj;
							return page.name;
						}
						return obj.toString();
					}
				}
		);
	}

	private void listOSGiBundles() {
		final PrintStream stream = TreePrinter.getUTF8PrintStream();
		final TreePrinter printer = new TreePrinter(stream);
		printer.print(
				"OSGi bundles",
				handler.osgi.getBundleTracker().getSortedBundles(),
				new TreePrinter.Extractor() {
					@Override
					public Object[] getChildren(Object obj) {
						if (obj instanceof Bundle) {
							final Bundle bundle = (Bundle) obj;
							return new Object[]{
								"id: " + bundle.getBundleId(),
								"version: " + bundle.getVersion(),
								"state: " + OSGiBundleTracker.getBundleState(bundle),
								"location: " + bundle.getLocation(),
								new TreePrinter.Node(
										"headers",
										TreePrinter.toChildren(
												bundle.getHeaders(),
												(key, val) -> {
													final String s = (String) key;
													switch (s.toLowerCase()) {
														case "embedded-artifacts":
														case "export-package":
														case "import-package":
														case "bundle-classpath":
															final String v = (String) val;
															return new TreePrinter.Node(
																	s,
																	splitExcludeQuotes(v, ',')
															);
													}
													return String.format("%s: %s", key, val);
												}
										)
								)
							};
						}
						return new Object[]{};
					}

					@Override
					public String getName(Object obj) {
						if (obj instanceof Bundle) {
							final Bundle bundle = (Bundle) obj;
							return bundle.getSymbolicName();
						}
						return obj.toString();
					}
				}
		);
	}

	// splits a string on some char unless inside quotes
	private static List<String> splitExcludeQuotes(String src, char split) {
		// hint: do not try to do this with a regular expression,
		// ...or maybe do, have some fun. :)
		final List<String> tokens = new ArrayList<>();
		boolean inQuotes = false;
		StringBuilder b = new StringBuilder();
		for (char c : src.toCharArray()) {
			switch (c) {
				case ',':
					if (inQuotes) {
						b.append(c);
					} else {
						tokens.add(b.toString());
						b = new StringBuilder();
					}
					break;
				case '"':
					inQuotes = !inQuotes;
					break;
				default:
					b.append(c);
					break;
			}
		}
		return tokens;
	}

	private static void listSystemInformation(ApplicationHandler handler) {
		final PrintStream stream = TreePrinter.getUTF8PrintStream();
		final TreePrinter printer = new TreePrinter(stream);
		printer.print(new TreePrinter.Node(
				"System information", new Object[]{
					new TreePrinter.Node("Operating System", new Object[]{
						"name: " + System.getProperty("os.name"),
						"version: " + System.getProperty("os.version"),
						"architecture: " + System.getProperty("os.arch")
					}),
					new TreePrinter.Node("Java", new Object[]{
						"version: " + System.getProperty("java.runtime.version"),
						"vendor: " + System.getProperty("java.specification.vendor")
					}),
					new TreePrinter.Node("JVM", new Object[]{
						"version: " + System.getProperty("java.vm.version"),
						"vendor: " + System.getProperty("java.vm.vendor"),
						"runtime: " + System.getProperty("java.vm.name")
					}),
					new TreePrinter.Node("Document Image Processor (DIP)", new Object[]{
						"current-working-directory: " + handler.dataManager.workingDir,
						"application-directory: " + handler.dataManager.appDir,
						"application-data-directory: " + handler.dataManager.appDataDir,
						"user-directory: " + handler.dataManager.userDir,
						new TreePrinter.Node("user-settings", new Object[]{
							"user-local: " + handler.settings.getLocale(),
							new TreePrinter.Node("primary-stage", handler.settings.primaryStage.getAttributes()),
							new TreePrinter.Node("pipeline-stage", handler.settings.pipelineStage.getAttributes()),
							new TreePrinter.Node("editor", handler.settings.editor.getAttributes()),
							new TreePrinter.Node("pipeline-editor", handler.settings.pipelineEditor.getAttributes()),
							new TreePrinter.Node("osgi", handler.settings.osgi.getAttributes()),
							new TreePrinter.Node("recent-files", handler.settings.recentFiles.getAttributes())
						}),
						new TreePrinter.Node("thread-pools", new Object[]{
							new TreePrinter.Node(
									handler.threadPool.toString(),
									handler.threadPool.getExecutorService()
							),
							new TreePrinter.Node(
									handler.discardingThreadPool.toString(),
									handler.discardingThreadPool.getExecutorService()
							)
						})
					})
				}
		));
	}

	/**
	 * CLI status listener.
	 */
	private class StatusListener {

		@Subscribe
		public void handleStatusEvent(StatusWorkerEvent<?> event) {
			final InvalidationListener progressListener = (e) -> {
				printWorkerProgress(event.worker);
			};

			final ChangeListener<Worker.State> stateListener = new ChangeListener<Worker.State>() {
				@Override
				public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
					if (isDone(newValue)) {
						event.worker.progressProperty().removeListener(progressListener);
						event.worker.stateProperty().removeListener(this);
						printWorkerProgress(event.worker);
					}
				}
			};

			event.worker.progressProperty().addListener(progressListener);
			event.worker.stateProperty().addListener(stateListener);
			// just in case the worker is already done by the time we started
			// listening to it...
			stateListener.changed(
					event.worker.stateProperty(),
					event.worker.stateProperty().get(),
					event.worker.stateProperty().get()
			);
		}

		private void printWorkerProgress(Worker<?> worker) {
			System.out.println(String.format(
					"%s%5.2f, %s: %s",
					INDENT,
					worker.getProgress(),
					worker.getState(),
					worker.getMessage()
			));
		}

		@Subscribe
		public void handleStatusEvent(StatusMessageEvent event) {
			System.out.println(INDENT + event.message);
		}

		@Subscribe
		public void handleProjectNotidifcation(ProjectNotification event) {
			if (event.page < 0) {
				System.out.println(String.format("%s%s", INDENT, event.type));
			} else {
				System.out.println(String.format("%s%s: page %d", INDENT, event.type, event.page));
			}
		}

		/**
		 * Checks whether the worker of a worker event is done already.
		 *
		 * @param event the worker event.
		 * @return {@code true} if done, {@code false} otherwise.
		 */
		private boolean isDone(StatusWorkerEvent<?> event) {
			return isDone(event.worker.getState());
		}

		/**
		 * Checks whether a worker is done already.
		 *
		 * @param state a worker's state.
		 * @return {@code true} if done, {@code false} otherwise.
		 */
		private boolean isDone(Worker.State state) {
			if (state == null) {
				return true;
			}
			switch (state) {
				case SUCCEEDED:
				case CANCELLED:
				case FAILED:
					return true;
			}
			return false;
		}

	}
}
