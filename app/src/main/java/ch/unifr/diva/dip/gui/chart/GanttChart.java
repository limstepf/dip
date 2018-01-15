package ch.unifr.diva.dip.gui.chart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * A simple Gantt chart.
 *
 * @param <X> the generic data value to be plotted on the X axis.
 * @param <Y> the generic data value to be plotted on the Y axis.
 */
public class GanttChart<X, Y> extends XYChart<X, Y> {

	/**
	 * Extra value of Gantt chart data points.
	 */
	public static interface ExtraData {

		/**
		 * The duration (or length) of this data point.
		 *
		 * @return the duration.
		 */
		public double getDuration();

		/**
		 * Hook method to initialize and style the data point's node.
		 *
		 * @param node the data point's node (a stack pane containing a
		 * rectangle).
		 * @param seriesIndex the series index.
		 * @param itemIndex the item index.
		 */
		default void initNode(Node node, int seriesIndex, int itemIndex) {
			node.getStyleClass().addAll("chart-bar", "series" + seriesIndex, "data" + itemIndex);
		}

	}

	/**
	 * Creates a new Gantt chart.
	 *
	 * @param xAxis The x axis to use
	 * @param yAxis The y axis to use
	 */
	public GanttChart(Axis<X> xAxis, Axis<Y> yAxis) {
		this(xAxis, yAxis, FXCollections.<Series<X, Y>>observableArrayList());
	}

	/**
	 * Creates a new Gantt chart.
	 *
	 * @param xAxis The x axis to use
	 * @param yAxis The y axis to use
	 * @param data The data to use, this is the actual list used so any changes
	 * to it will be reflected in the chart
	 */
	public GanttChart(Axis<X> xAxis, Axis<Y> yAxis, ObservableList<Series<X, Y>> data) {
		super(xAxis, yAxis);
		setData(data);
	}

	protected double rectHeight = 15.0;

	/**
	 * The height of the nodes/boxes of data points.
	 *
	 * @return the height of the nodes/boxes of data points.
	 */
	public double getRectHeight() {
		return rectHeight;
	}

	/**
	 * Sets the height of the nodes/boxes of data points.
	 *
	 * @param value the new height of the nodes/boxes of data points.
	 */
	public void setRectHeigth(double value) {
		rectHeight = value;
	}

	@Override
	protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
		Node node = createNode(series, getData().indexOf(series), item, itemIndex);
		getPlotChildren().add(node);
	}

	@Override
	protected void dataItemRemoved(Data<X, Y> item, Series<X, Y> series) {
		final Node node = item.getNode();
		getPlotChildren().remove(node);
		removeDataItemFromDisplay(series, item);
	}

	@Override
	protected void dataItemChanged(Data<X, Y> item) {
	}

	@Override
	protected void seriesAdded(Series<X, Y> series, int seriesIndex) {
		for (int j = 0; j < series.getData().size(); j++) {
			Data<X, Y> item = series.getData().get(j);
			Node node = createNode(series, seriesIndex, item, j);
			getPlotChildren().add(node);
		}
	}

	@Override
	protected void seriesRemoved(Series<X, Y> series) {
		for (XYChart.Data<X, Y> d : series.getData()) {
			final Node node = d.getNode();
			getPlotChildren().remove(node);
		}
		removeSeriesFromDisplay(series);
	}

	@Override
	protected void layoutPlotChildren() {
		for (int seriesIndex = 0; seriesIndex < getDataSize(); seriesIndex++) {
			Series<X, Y> series = getData().get(seriesIndex);
			Iterator<Data<X, Y>> iter = getDisplayedDataIterator(series);
			while (iter.hasNext()) {
				Data<X, Y> item = iter.next();
				double x = getXAxis().getDisplayPosition(item.getXValue());
				double y = getYAxis().getDisplayPosition(item.getYValue());
				Node node = item.getNode();
				Rectangle rect;
				if (node != null) {
					if (node instanceof StackPane) {
						StackPane region = (StackPane) item.getNode();
						if (region.getShape() == null) {
							rect = new Rectangle();
						} else if (region.getShape() instanceof Rectangle) {
							rect = (Rectangle) region.getShape();
						} else {
							return;
						}
						rect.setWidth(
								getDuration(item.getExtraValue())
								* ((getXAxis() instanceof NumberAxis)
										? Math.abs(((NumberAxis) getXAxis()).getScale())
										: 1)
						);
						rect.setHeight(
								getRectHeight()
								* ((getYAxis() instanceof NumberAxis)
										? Math.abs(((NumberAxis) getYAxis()).getScale())
										: 1)
						);
						y -= getRectHeight() / 2.0;
						// Note: workaround for RT-7689 - saw this in ProgressControlSkin
						// The region doesn't update itself when the shape is mutated in place, so we
						// null out and then restore the shape in order to force invalidation.
						region.setShape(null);
						region.setShape(rect);
						region.setScaleShape(false);
						region.setCenterShape(false);
						region.setCacheShape(false);
						// position the node
						node.setLayoutX(x);
						node.setLayoutY(y);
					}
				}
			}
		}
	}

	@Override
	protected void updateAxisRange() {
		final Axis<X> xa = getXAxis();
		final Axis<Y> ya = getYAxis();
		final List<X> xData = xa.isAutoRanging() ? new ArrayList<>() : null;
		final List<Y> yData = ya.isAutoRanging() ? new ArrayList<>() : null;
		if (xData != null || yData != null) {
			for (Series<X, Y> series : getData()) {
				for (Data<X, Y> data : series.getData()) {
					if (xData != null) {
						xData.add(data.getXValue());
						xData.add(xa.toRealValue(xa.toNumericValue(data.getXValue()) + getDuration(data.getExtraValue())));
					}
					if (yData != null) {
						yData.add(data.getYValue());
					}
				}
			}
			if (xData != null) {
				xa.invalidateRange(xData);
			}
			if (yData != null) {
				ya.invalidateRange(yData);
			}
		}
	}

	protected Node createNode(Series<X, Y> series, int seriesIndex, final Data<X, Y> item, int itemIndex) {
		Node node = item.getNode();
		// check if node has already been created
		if (node == null) {
			node = new StackPane();
			item.setNode(node);
		}

		initNode(node, seriesIndex, itemIndex, item.getExtraValue());
		return node;
	}

	protected void initNode(Node node, int seriesIndex, int itemIndex, Object extraValue) {
		((ExtraData) extraValue).initNode(node, seriesIndex, itemIndex);
	}

	protected double getDuration(Object obj) {
		return ((ExtraData) obj).getDuration();
	}

	protected int getDataSize() {
		final ObservableList<Series<X, Y>> data = getData();
		if (data == null) {
			return 0;
		}
		return data.size();
	}

}
