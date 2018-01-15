package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.slf4j.LoggerFactory;

/**
 * Pipeline execution dialog.
 */
public class PipelineExecutionDialog extends AbstractDialog {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(PipelineExecutionDialog.class);
	protected static final double[] gridPercentWidths = new double[]{40.0, 20.0, 40.0};
	protected final PipelineExecutionController controller;
	protected final PipelineExecutionLogger logger;
	private final VBox vbox;
	private final ListView<ProjectPage> listView;
	private final FormGridPane totalGrid;
	private final Label pageLabel;
	private final ProgressBar progressBar;
	private final HBox optionBox;
	private final PipelineExecutionExportFormat.ExportNode<PipelineExecutionExportFormat.MultiExportHandler> exportOptions;
	private final Button ok;
	private final Button cancel;

	private static ColumnConstraints[] getColumnConstraints() {
		final ColumnConstraints[] cc = new ColumnConstraints[3];
		for (int i = 0; i < 3; i++) {
			cc[i] = new ColumnConstraints();
			cc[i].setPercentWidth(PipelineExecutionDialog.gridPercentWidths[i]);
		}
		cc[0].setHgrow(Priority.ALWAYS);
		return cc;
	}

	/**
	 * Creates a new pipeline execution dialog.
	 *
	 * @param owner the owner of the dialog.
	 * @param controller the pipeline execution controller.
	 */
	public PipelineExecutionDialog(Window owner, PipelineExecutionController controller) {
		super(owner);
		setTitle(localize("processing.object", localize("pages")) + "...");
		this.controller = controller;
		this.logger = controller.getLogger();
		this.pageLabel = new Label(localize("progress.total") + ":");
		this.progressBar = new ProgressBar();
		Platform.runLater(() -> {
			progressBar.progressProperty().bind(controller.progressProperty());
		});

		this.exportOptions = PipelineExecutionExportFormat.getExportNode(
				owner,
				controller.handler,
				new PipelineExecutionExportFormat.MultiExportHandler() {

					@Override
					public List<PipelineTiming> getTimings() {
						// don't get timings from logger, since some timings might be
						// old from pages/pipelines already processed
						final List<PipelineTiming> timings = new ArrayList<>();
						for (ProjectPage page : controller.pages) {
							timings.add(page.getPipelineTiming());
						}
						return timings;
					}

					@Override
					public String getFilename() {
						return "project-timing";
					}
				}
		);
		exportOptions.setExportLabel(localize("export.all"));
		exportOptions.setDisable(true);

		this.optionBox = new HBox();
		optionBox.setSpacing(UIStrategyGUI.Stage.insets);
		optionBox.getChildren().addAll(exportOptions);

		this.totalGrid = new FormGridPane(getColumnConstraints());
		totalGrid.setPadding(new Insets(UIStrategyGUI.Stage.insets, 0, 0, 0));
		totalGrid.addRow(pageLabel, progressBar, optionBox);
		totalGrid.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);

		this.listView = new ListView<>();
		VBox.setVgrow(listView, Priority.ALWAYS);
		listView.setMaxHeight(Double.MAX_VALUE);
		listView.setPrefWidth(720);
		listView.setCellFactory(
				(ListView<ProjectPage> param) -> new PipelineExecutionListCell(owner, this)
		);
		listView.getItems().addAll(this.controller.pages);

		this.vbox = new VBox();
		vbox.getChildren().setAll(listView, totalGrid);
		this.root.setCenter(vbox);

		this.ok = getDefaultButton(localize("ok"));
		this.cancel = getDefaultButton(localize("cancel"));
		ok.setDisable(true);
		ok.setOnAction((e) -> ok());
		cancel.setOnAction((e) -> cancel());
		buttons.add(ok);
		buttons.add(cancel);
		this.controller.executionStateProperty().addListener((javafx.beans.Observable e) -> {
			switch (this.controller.getExecutionState()) {
				case READY:
				case RUNNING:
					break;
				case CANCELLED:
					break;
				case SUCCEEDED:
					exportOptions.setDisable(false);
					ok.setDisable(false);
					cancel.setDisable(true);
					break;
			}
		});
	}

	private void ok() {
		close();
	}

	private void cancel() {
		cancel.setDisable(true);
		progressBar.getStyleClass().add("dip-cancelled-progress");
		controller.cancel();

		// wait for execution thread to actually stop...
		final Thread executionThread = controller.getThread();
		if (executionThread != null) {
			final Thread t = new Thread(() -> {
				try {
					executionThread.join();
				} catch (InterruptedException ex) {
					//
				}
				FxUtils.run(() -> {
					ok.setDisable(false);
					progressBar.setDisable(true);
				});
			});
			t.start();
		} else {
			ok.setDisable(false);
			progressBar.setDisable(true);
		}
	}

	/**
	 * Pipeline execution list cell.
	 */
	public static class PipelineExecutionListCell extends ListCell<ProjectPage> implements Localizable {

		private final static ColumnConstraints[] cc;

		static {
			cc = PipelineExecutionDialog.getColumnConstraints();
		}

		private final Window owner;
		private final PipelineExecutionDialog dialog;
		private final FormGridPane grid;
		private final VBox pageBox;
		private final Label pageLabel;
		private final Label pipelineLabel;
		private final ProgressBar progressBar;
		private final HBox optionBox;
		private final Button showGantt;
		private final PipelineExecutionExportFormat.ExportNode<PipelineExecutionExportFormat.SingleExportHandler> exportOptions;
		private final InvalidationListener currentPageIndexListener;
		private ProjectPage currentPage;

		/**
		 * Creates a new pipeline execution list ceel.
		 *
		 * @param owner the owner of the dialog.
		 * @param dialog the pipeline execution dialog.
		 */
		public PipelineExecutionListCell(Window owner, PipelineExecutionDialog dialog) {
			this.owner = owner;
			this.dialog = dialog;
			this.pageLabel = newLabel();
			this.pipelineLabel = newLabel();
			this.pageBox = new VBox();
			pageBox.getChildren().addAll(
					pageLabel,
					pipelineLabel
			);
			this.progressBar = new ProgressBar(0);
			this.showGantt = new Button("Gantt chart");
			showGantt.getStyleClass().add("dip-small");
			showGantt.setOnAction((e) -> showGantt());
			this.exportOptions = PipelineExecutionExportFormat.getExportNode(
					owner,
					dialog.controller.handler,
					new PipelineExecutionExportFormat.SingleExportHandler() {

						@Override
						public PipelineTiming getTiming() {
							return getPipelineTiming();
						}

						@Override
						public String getFilename() {
							return String.format(
									"page-%d-timing",
									currentPage.id
							);
						}
					}
			);

			final Label showLabel = new Label(localize("show") + ":");
			this.optionBox = new HBox();
			optionBox.setSpacing(UIStrategyGUI.Stage.insets);
			optionBox.getChildren().addAll(
					showLabel,
					showGantt
			);
			optionBox.getChildren().addAll(exportOptions);
			this.grid = new FormGridPane(cc);
			grid.setMinWidth(0);
			grid.addRow(
					pageBox,
					progressBar,
					optionBox
			);
			this.currentPageIndexListener = (c) -> onCurrentPageIndex();
			dialog.controller.currentPageIndexProperty().addListener(currentPageIndexListener);
			dialog.controller.executionStateProperty().addListener(currentPageIndexListener);
		}

		private void showGantt() {
			if (currentPage == null) {
				return;
			}
			final PipelineTiming timing = getPipelineTiming();
			if (timing == null) {
				showGantt.setDisable(true);
				return;
			}
			final PipelineExecutionGanttChart gantt = new PipelineExecutionGanttChart(
					owner,
					dialog.controller.handler,
					timing
			);
			gantt.show();
		}

		private void exportCSV(Path file) {
			if (currentPage == null) {
				log.warn("failed to write CSV file: no current page.");
				return;
			}
			final PipelineTiming timing = getPipelineTiming();
			final Path procFile = PipelineTiming.getProcessorFile(file);
			try {
				timing.writePipelineCSV(file);
				timing.writeProcessorCSV(procFile);
			} catch (IOException ex) {
				log.warn("failed to write CSV files: {} and {}", file, procFile, ex);
			}
		}

		private PipelineTiming getPipelineTiming() {
			PipelineTiming timing = dialog.logger.getTiming(currentPage.id);
			if (timing != null) {
				return timing;
			}
			// might be null from logger if processed/timed earlier already
			return currentPage.getPipelineTiming();
		}

		private static Label newLabel() {
			return newLabel("");
		}

		private static Label newLabel(String text) {
			final Label label = new Label(text);
			label.setTextOverrun(OverrunStyle.ELLIPSIS);
			label.setWrapText(true);
			return label;
		}

		@Override
		public final void updateItem(ProjectPage page, boolean empty) {
			super.updateItem(page, empty);
			setText(null);
			setGraphic(null);

			currentPage = page;

			if (!empty) {
				pageLabel.setText(page.getName());
				pipelineLabel.setText(page.getPipelineName());
				updateProgress();
				setGraphic(grid);
			}
		}

		private void onCurrentPageIndex() {
			if (currentPage == null) {
				return;
			}
			if (Platform.isFxApplicationThread()) {
				updateProgress();
			} else {
				Platform.runLater(() -> {
					updateProgress();
				});
			}
		}

		private void updateProgress() {
			if (currentPage == null) {
				setProgress(0);
				progressBar.setDisable(true);
				return;
			}
			progressBar.setDisable(false);

			final int currentIdx = dialog.controller.getCurrentPageIndex();
			final int thisIdx = dialog.controller.pages.indexOf(currentPage);
			if (thisIdx == currentIdx) {
				if (isCancelled()) {
					setProgress(0);
					progressBar.setDisable(true);
				} else {
					setProgress(-1.0);
				}
			} else {
				setProgress(currentPage.getProgress());
			}
		}

		private boolean isCancelled() {
			return PipelineExecutionController.ExecutionState.CANCELLED.equals(
					dialog.controller.getExecutionState()
			);
		}

		private void setProgress(double value) {
			final boolean disable = value <= 0.0; // consider anything > 0 (partially) done
			showGantt.setDisable(disable);
			exportOptions.setDisable(disable);
			progressBar.setProgress(value);
		}

	}

}
