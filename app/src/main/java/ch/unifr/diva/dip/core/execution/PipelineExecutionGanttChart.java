package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.gui.chart.GanttChart;
import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Pipeline execution Gantt chart dialog.
 */
public class PipelineExecutionGanttChart extends AbstractDialog {

	protected final ApplicationHandler handler;
	protected final PipelineTiming pipelineTiming;
	protected final List<ProcessorTiming> processorTimings;
	protected final Button ok;
	protected final NumberAxis xAxis;
	protected final CategoryAxis yAxis;
	protected final GanttChart<Number, String> gantt;
	protected final ScrollPane ganttScrollPane;
	protected final FormGridPane dataGrid;
	protected final ScrollPane dataScrollPane;
	protected final Map<String, Label[]> dataLabels;
	private final PipelineExecutionExportFormat.ExportNode<PipelineExecutionExportFormat.SingleExportHandler> exportOptions;

	/**
	 * Creates a new pipeline execution Gantt chart dialog.
	 *
	 * @param owner the owner of the dialog.
	 * @param handler
	 * @param pipelineTiming the pipeline timing.
	 */
	public PipelineExecutionGanttChart(Window owner, ApplicationHandler handler, PipelineTiming pipelineTiming) {
		super(owner);
		setTitle(localize("pipeline.execution"));
		this.handler = handler;
		this.pipelineTiming = pipelineTiming;
		this.processorTimings = pipelineTiming.getProcessorTimings();
		Collections.reverse(processorTimings);

		final FormGridPane infoGrid = new FormGridPane();
		infoGrid.addRow(
				newLabel(localize("page") + ":"),
				newLabel(String.format(
								"%s (id: %d)",
								pipelineTiming.getPageName(),
								pipelineTiming.getPageId()
						))
		);
		infoGrid.addRow(
				newLabel(localize("pipeline") + ":"),
				newLabel(String.format(
								"%s (id: %d)",
								pipelineTiming.getPipelineName(),
								pipelineTiming.getPipelineId()
						))
		);
		infoGrid.addRow(
				newLabel(localize("pipeline.executor") + ":"),
				newLabel(pipelineTiming.getPipelineExecutor())
		);
		infoGrid.addRow(
				newLabel(localize("time.wall.clock") + ":"),
				newLabel(String.format(
								"%d %s",
								pipelineTiming.getElapsedMillis(),
								localize("time.millis.abbr")
						))
		);

		this.xAxis = new NumberAxis();
		xAxis.setLabel(
				localize("time.wall.clock")
				+ " (" + localize("time.format.in", localize("time.millis.abbr"))
				+ ")"
		);
		xAxis.setMinorTickCount(4);

		this.yAxis = new CategoryAxis();
		yAxis.setLabel(localize("processor.id"));
		yAxis.setTickLabelGap(10);
		yAxis.setCategories(getYLabels(pipelineTiming, processorTimings));

		this.gantt = new GanttChart<>(xAxis, yAxis, getSeries(pipelineTiming, processorTimings));
		gantt.setLegendVisible(false);
		gantt.setMaxWidth(Double.MAX_VALUE);
		final double prefHeight = pipelineTiming.getPipelineSize() * gantt.getRectHeight() * 3;
		gantt.setPrefHeight(prefHeight);
		gantt.setMaxHeight(Double.MAX_VALUE);

		this.ganttScrollPane = getScrollPane(gantt);
		ganttScrollPane.setMaxHeight(Double.MAX_VALUE);

		this.dataLabels = new HashMap<>();

		final FormGridPane dataGridHead = getDataGridHead();
		this.dataGrid = getDataGrid(processorTimings, dataLabels);
		this.dataScrollPane = getScrollPane(dataGrid);
		dataScrollPane.setPrefWidth(480);
		dataScrollPane.setMaxHeight(Double.MAX_VALUE);

		final double s = UIStrategyGUI.Stage.insets * 3;
		final VBox dataBox = new VBox();
		dataBox.setPadding(new Insets(s, 0, 0, 0));
		dataBox.getChildren().addAll(
				dataGridHead,
				dataScrollPane
		);

		this.exportOptions = PipelineExecutionExportFormat.getExportNode(
				owner,
				handler,
				new PipelineExecutionExportFormat.SingleExportHandler() {

					@Override
					public PipelineTiming getTiming() {
						return pipelineTiming;
					}

					@Override
					public String getFilename() {
						return String.format(
								"page-%d-timing",
								pipelineTiming.getPageId()
						);
					}
				}
		);
		final VBox exportBox = new VBox();
		exportBox.getChildren().setAll(
				exportOptions
		);

		final BorderPane infoBox = new BorderPane();
		infoBox.setCenter(infoGrid);
		infoBox.setRight(exportBox);

		final BorderPane sideBox = new BorderPane();
		sideBox.setPadding(new Insets(0, 0, 0, s));
		sideBox.setTop(infoBox);
		sideBox.setCenter(dataBox);

		final BorderPane vbox = new BorderPane();
		vbox.setCenter(ganttScrollPane);
		vbox.setRight(sideBox);

		this.root.setCenter(vbox);
		this.ok = getDefaultButton(localize("close"));
		ok.setOnAction((e) -> ok());
		buttons.add(ok);

		// make sure dialog isn't too large
		final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final double maxHeight = gd.getDisplayMode().getHeight() * .6;
		this.root.setPrefHeight(
				Math.min(prefHeight + 196, maxHeight)
		);
	}

	private void ok() {
		close();
	}

	private static ScrollPane getScrollPane(Node node) {
		final ScrollPane scrollPane = new ScrollPane(node);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		scrollPane.getStyleClass().add("edge-to-edge");
		return scrollPane;
	}

	private static ColumnConstraints[] cc = new ColumnConstraints[]{
		getColumnConstraints(7, HPos.RIGHT),
		getColumnConstraints(7, HPos.RIGHT),
		getColumnConstraints(56, HPos.LEFT),
		getColumnConstraints(20, HPos.RIGHT)
	};

	private static FormGridPane getDataGridHead() {
		final L10n l10n = L10n.getInstance();
		final FormGridPane grid = new FormGridPane(cc);
		for (ColumnConstraints c : grid.getColumnConstraints()) {
			c.setHalignment(HPos.LEFT);
		}
		grid.setMaxWidth(Double.MAX_VALUE);
		grid.addRow(
				newLabel(l10n.getString("pipeline.stage")),
				newLabel(l10n.getString("id")),
				newLabel(l10n.getString("processor")),
				newLabel(l10n.getString("time.wall.clock"))
		);
		return grid;
	}

	private static FormGridPane getDataGrid(List<ProcessorTiming> processorTimings, Map<String, Label[]> dataLabels) {
		final L10n l10n = L10n.getInstance();
		final FormGridPane grid = new FormGridPane(cc);
		grid.setMaxWidth(Double.MAX_VALUE);
		for (int i = processorTimings.size() - 1; i >= 0; i--) {
			final ProcessorTiming timing = processorTimings.get(i);
			final String categoryLabel = getCategoryLabel(timing);
			final Label[] labels = new Label[]{
				/* 0 */newLabel(String.format("%d", timing.getPipelineStage()), true),
				/* 1 */ newLabel(categoryLabel, true),
				/* 2 */ newLabel(timing.getProcessorName(), true),
				/* 3 */ newLabel(timing.getProcessorPID(), true),
				/* 4 */ newLabel(timing.getProcessorVersion(), true),
				/* 5 */ newLabel(String.format("%d %s",
				timing.getElapsedMillis(),
				l10n.getString("time.millis.abbr")
				),
				true
				)
			};
			dataLabels.put(categoryLabel, labels);

			final VBox vbox = new VBox();
			vbox.getChildren().addAll(
					labels[2],
					labels[3],
					labels[4]
			);
			grid.addRow(
					labels[0],
					labels[1],
					vbox,
					labels[5]
			);
		}
		return grid;
	}

	private static Label newLabel(String text) {
		return newLabel(text, false);
	}

	private static Label newLabel(String text, boolean small) {
		final Label label = new Label(text);
		if (small) {
			label.getStyleClass().add("dip-small");
		}
		return label;
	}

	private static ColumnConstraints getColumnConstraints(double percentWidth, HPos alignment) {
		final ColumnConstraints cc = new ColumnConstraints();
		cc.setHalignment(alignment);
		cc.setPercentWidth(percentWidth);
		return cc;
	}

	private ObservableList<XYChart.Series<Number, String>> getSeries(PipelineTiming pipelineTiming, List<ProcessorTiming> processorTimings) {
		final ObservableList<XYChart.Series<Number, String>> series = FXCollections.observableArrayList();
		final long t0 = pipelineTiming.getStartMillis();
		// TODO: adapt time unit depending on the pipeline's wall-clock time?
		for (ProcessorTiming timing : processorTimings) {
			if (timing.getElapsedNanos() == 0) {
				continue;
			}
			final XYChart.Series<Number, String> s = new XYChart.Series<>();
			s.getData().add(getSeriesData(
					getCategoryLabel(timing),
					getTooltipLabel(timing),
					timing.getStartMillis() - t0,
					timing.getElapsedMillis()
			));
			series.add(s);
		}
		return series;
	}

	private static String getTooltipLabel(ProcessorTiming timing) {
		return String.format(
				"%s\nPID: %s\n%s: %s",
				timing.getProcessorName(),
				timing.getProcessorPID(),
				L10n.getInstance().getString("version"),
				timing.getProcessorVersion()
		);
	}

	private static String getCategoryLabel(ProcessorTiming timing) {
		return String.format("%d", timing.getProcessorId());
	}

	private static long nanosToMillis(long nanos) {
		return TimeUnit.NANOSECONDS.toMillis(nanos);
	}

	private XYChart.Data<Number, String> getSeriesData(String categoryLabel, String tooltipLabel, double start, double duration) {
		return new XYChart.Data<>(
				start,
				categoryLabel,
				new GanttExtraData(categoryLabel, duration)
		);
	}

	private static ObservableList<String> getYLabels(PipelineTiming pipelineTiming, List<ProcessorTiming> processorTimings) {
		final int n = pipelineTiming.getPipelineSize();
		final ObservableList<String> labels = FXCollections.observableArrayList();
		for (ProcessorTiming timing : processorTimings) {
			labels.add(getCategoryLabel(timing));
		}
		return labels;
	}

	/**
	 * Gantt extra data.
	 */
	protected class GanttExtraData implements GanttChart.ExtraData {

		private final String categoryLabel;
		private final double duration;

		/**
		 * Creates new Gantt extra data.
		 *
		 * @param categoryLabel the category label.
		 * @param duration the duration (in ms).
		 */
		public GanttExtraData(String categoryLabel, double duration) {
			this.categoryLabel = categoryLabel;
			this.duration = duration;
		}

		@Override
		public double getDuration() {
			return duration;
		}

		@Override
		public void initNode(Node node, int seriesIndex, int itemIndex) {
			ganttNodes.add(node);

			node.setStyle(rectStyle);
			node.setOnMouseClicked((e) -> {
				if (isProcessorSelected(categoryLabel)) {
					deselectProcessor();
					deselectAll();
				} else {
					selectProcessor(categoryLabel);
					select(node);
				}
			});
			final Tooltip t = new Tooltip(String.format(
					"%d %s",
					(int) duration,
					L10n.getInstance().getString("time.millis.abbr")
			));
			Tooltip.install(node, t);
		}

		private void deselectAll() {
			for (Node node : ganttNodes) {
				node.setStyle(rectStyle);
			}
		}

		private void select(Node select) {
			for (Node node : ganttNodes) {
				node.setStyle(node.equals(select) ? rectStyle : deselectedRectStyle);
			}
		}

	}

	private static String rectStyle = String.format(
			"-fx-background-color:%s;",
			FxUtils.toHexString(UIStrategyGUI.Colors.accent)
	);
	private static String deselectedRectStyle = String.format(
			"-fx-background-color:%s;",
			FxUtils.toHexString(UIStrategyGUI.Colors.accent_light)
	);
	private final Set<Node> ganttNodes = new HashSet<>();
	private String selectedProcessor = "";

	protected boolean isProcessorSelected(String categoryLabel) {
		return selectedProcessor.equals(categoryLabel);
	}

	protected void deselectProcessor() {
		if (!selectedProcessor.isEmpty()) {
			for (Label label : dataLabels.get(selectedProcessor)) {
				label.getStyleClass().removeAll("dip-small-emphasis");
				label.getStyleClass().add("dip-small");
			}
		}
		selectedProcessor = "";
	}

	protected void selectProcessor(String categoryLabel) {
		deselectProcessor();
		if (dataLabels.containsKey(categoryLabel)) {
			for (Label label : dataLabels.get(categoryLabel)) {
				label.getStyleClass().removeAll("dip-small");
				label.getStyleClass().add("dip-small-emphasis");
			}
			selectedProcessor = categoryLabel;
		}
	}

}
