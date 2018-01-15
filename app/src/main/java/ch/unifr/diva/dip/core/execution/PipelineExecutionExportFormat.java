package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import org.slf4j.LoggerFactory;

/**
 * Pipeline execution (timing) export format.
 */
public enum PipelineExecutionExportFormat {

	/**
	 * Comma separated values (CSV).
	 */
	CSV() {
				@Override
				public ExtensionFilter getExtensionFilter() {
					return new ExtensionFilter(
							L10n.getInstance().getString("file.csv"),
							"*.csv"
					);
				}

				@Override
				public void exportTiming(PipelineTiming timing, Path file) {
					final Path procFile = PipelineTiming.getProcessorFile(file);
					try {
						timing.writePipelineCSV(file);
						timing.writeProcessorCSV(procFile);
					} catch (IOException ex) {
						log.warn("failed to write CSV files: {} and {}", file, procFile, ex);
					}
				}

				@Override
				public void exportTimings(List<PipelineTiming> timings, Path file) {
					final Path procFile = PipelineTiming.getProcessorFile(file);
					try {
						PipelineTiming.writePipelineCSV(timings, file);
						PipelineTiming.writeProcessorCSV(timings, procFile);
					} catch (IOException ex) {
						log.warn("Failed to write CSV files: {} and {}", file, procFile, ex);
					}
				}
			};

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(PipelineExecutionDialog.class);

	/**
	 * Creates a new pipeline execution (timing) export format.
	 */
	private PipelineExecutionExportFormat() {

	}

	/**
	 * Returns the file extension filter of the export format.
	 *
	 * @return the file extension filter of the export format.
	 */
	public abstract ExtensionFilter getExtensionFilter();

	/**
	 * Exports a single pipeline timing.
	 *
	 * @param timing the pipeline timing.
	 * @param file the (main) destination file (depending on the format multiple
	 * files may be written to/necessary).
	 */
	public abstract void exportTiming(PipelineTiming timing, Path file);

	/**
	 * Exports multiple pipeline timings.
	 *
	 * @param timings the pipeline timings.
	 * @param file the (main) destination file (depending on the format multiple
	 * files may be written to/necessary).
	 */
	public abstract void exportTimings(List<PipelineTiming> timings, Path file);

	/**
	 * Returns the default export format.
	 *
	 * @return the default export format.
	 */
	public static PipelineExecutionExportFormat getDefault() {
		return CSV;
	}

	/**
	 * Returns the export node/component.
	 *
	 * @param <T> class of the export handler.
	 * @param owner the owner of dialogs to be opened.
	 * @param handler the application handler.
	 * @param exportHandler the export handler.
	 * @return the export node/component.
	 */
	public static <T extends ExportHandler> ExportNode<T> getExportNode(Window owner, ApplicationHandler handler, T exportHandler) {
		return new ExportNode<>(owner, handler, exportHandler);
	}

	/**
	 * Export handler.
	 */
	protected interface ExportHandler {

		/**
		 * Returns the default filename.
		 *
		 * @return the default filename (without file extension).
		 */
		public String getFilename();

		/**
		 * Exports timing(s) according to the given export format.
		 *
		 * @param exportFormat the export format.
		 * @param file the (main) destination file.
		 */
		public void export(PipelineExecutionExportFormat exportFormat, Path file);

	}

	/**
	 * An export handler to export a single pipeline timing.
	 */
	public interface SingleExportHandler extends ExportHandler {

		/**
		 * Returns the pipeline timing to be exported.
		 *
		 * @return the pipeline timing.
		 */
		public PipelineTiming getTiming();

		@Override
		default void export(PipelineExecutionExportFormat exportFormat, Path file) {
			exportFormat.exportTiming(getTiming(), file);
		}
	}

	/**
	 * An export handler to export multiple pipeline timings.
	 */
	public interface MultiExportHandler extends ExportHandler {

		/**
		 * Returns the pipeline timings to be exported.
		 *
		 * @return the pipeline timings.
		 */
		public List<PipelineTiming> getTimings();

		@Override
		default void export(PipelineExecutionExportFormat exportFormat, Path file) {
			exportFormat.exportTimings(getTimings(), file);
		}
	}

	/**
	 * An export node/component.
	 *
	 * @param <T> class of the export handler.
	 */
	public static class ExportNode<T extends ExportHandler> extends HBox {

		protected final Window owner;
		protected final ApplicationHandler handler;
		protected final T exportHandler;
		protected final EnumParameter param;
		protected final Button export;

		/**
		 * Creates a new export node/component.
		 *
		 * @param owner the owner of dialogs to be opened.
		 * @param handler the application handler.
		 * @param exportHandler the export handler.
		 */
		public ExportNode(Window owner, ApplicationHandler handler, T exportHandler) {
			this.owner = owner;
			this.handler = handler;
			this.exportHandler = exportHandler;

			this.param = new EnumParameter(
					"",
					PipelineExecutionExportFormat.class,
					PipelineExecutionExportFormat.getDefault().name()
			);
			param.addComboBoxViewHook((c) -> {
				c.getStyleClass().add("dip-small");
			});

			this.export = new Button(L10n.getInstance().getString("export"));
			export.getStyleClass().add("dip-small");
			export.setOnAction((e) -> onExport());

			setSpacing(UIStrategyGUI.Stage.insets);
			getChildren().setAll(
					param.view().node(),
					export
			);
		}

		private void onExport() {
			final PipelineExecutionExportFormat exportFormat = param.getEnumValue(
					PipelineExecutionExportFormat.class
			);
			final ExtensionFilter extensionFilter = exportFormat.getExtensionFilter();

			final FileChooser chooser = new FileChooser();
			chooser.setTitle(L10n.getInstance().getString("export"));
			chooser.getExtensionFilters().add(extensionFilter);
			chooser.setSelectedExtensionFilter(extensionFilter);
			chooser.setInitialDirectory(handler.getRecentSaveDirectory().toFile());
			chooser.setInitialFileName(exportHandler.getFilename());

			final File file = chooser.showSaveDialog(owner);
			if (file != null) {
				exportHandler.export(exportFormat, file.toPath());
			}
		}

		/**
		 * Sets the label of the export button.
		 *
		 * @param label the new label.
		 */
		public void setExportLabel(String label) {
			export.setText(label);
		}

	}

}
