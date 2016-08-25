package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.gui.AbstractWidget;
import ch.unifr.diva.dip.gui.layout.Lane;
import ch.unifr.diva.dip.gui.layout.ZoomPane;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Scale;

/**
 * The NavigatorWidget binds to the graphical editor.
 */
public class NavigatorWidget extends AbstractWidget {

	private static double MIN_ZOOM_VALUE = 0; // 1 := 100%
	private static double MAX_ZOOM_VALUE = 4800;
	private final View view = new View();
	private ZoomPane master;

	/**
	 * NavigatorWidget constructor.
	 */
	public NavigatorWidget() {
		this(null);
	}

	/**
	 * NavigatorWidget constructor.
	 *
	 * @param zoomPane the master ZoomPane to bind to.
	 */
	public NavigatorWidget(ZoomPane zoomPane) {
		super();

		setWidget(view);
		setTitle(localize("widget.navigator"));

		if (zoomPane != null) {
			bind(zoomPane);
		}
	}

	private static double sliderToZoomVal(double v) {
		return Math.pow(10, v) - 1;
	}

	private static double zoomValToSlider(double v) {
		return Math.log10(v + 1);
	}

	private final ChangeListener<Number> zoomListener = (
			ObservableValue<? extends Number> observable,
			Number oldValue,
			Number newValue) -> {
				final double v = newValue.doubleValue();
				view.slider.valueProperty().setValue(zoomValToSlider(v));
				view.zoom.setText(String.format("%.1f%%", v * 100));
				updateSnapshot();
			};

	private final InvalidationListener contentListener = (observable) -> {
		view.setDisableViewport(!master.hasZoomContent());
		updateSnapshot();
	};

	private final EventHandler<KeyEvent> textFieldEvent = (e) -> {
		if (e.getCode().equals(KeyCode.ENTER)) {
			final double v = 0.01 * Double.parseDouble(
					view.zoom.getText().replace("%", "")
			);
			master.setZoom(v);
			updateSnapshot();
		}
	};

	private final ChangeListener<Number> sliderListener = (
			ObservableValue<? extends Number> observable,
			Number oldValue,
			Number newValue) -> {
				master.setZoom(sliderToZoomVal(newValue.doubleValue()));
				updateSnapshot();
			};

	private final ChangeListener<Object> scrollListener = (
			ObservableValue<? extends Object> observable,
			Object oldValue,
			Object newValue) -> {
				updateViewport();
			};

	private final EventHandler<MouseEvent> viewportDownEvent = (e) -> {
		view.viewport.getScene().setCursor(Cursor.CLOSED_HAND);
		view.x = e.getSceneX();
		view.y = e.getSceneY();
		view.h = master.getHvalue();
		view.v = master.getVvalue();
	};

	private final EventHandler<MouseEvent> viewportDragEvent = (e) -> {
		final double offsetX = e.getSceneX() - view.x;
		final double offsetY = e.getSceneY() - view.y;
		final Bounds viewport = master.getViewportBounds();
		final Bounds zoomed = master.getContentBoundsZoomed();

		final double scrollableWidth = zoomed.getWidth() - viewport.getWidth();
		final double scrollableHeight = zoomed.getHeight() - viewport.getHeight();

		final double invScale = 1 / view.scale;
		final double scaleFactor = invScale * master.getZoom();

		final double hmin = master.getHmin();
		final double hmax = master.getHmax();
		final double hrange = hmax - hmin;
		final double hppx = hrange / scrollableWidth;
		double w = (offsetX * scaleFactor * hppx) + view.h;

		if (w < hmin) { // snap to bounds!
			w = hmin;
		} else if (w > hmax) {
			w = hmax;
		}
		master.hvalueProperty().setValue(w);

		final double vmin = master.getVmin();
		final double vmax = master.getVmax();
		final double vrange = vmax - vmin;
		final double vppx = vrange / scrollableHeight;
		double h = (offsetY * scaleFactor * vppx) + view.v;

		if (h < vmin) {
			h = vmin;
		} else if (h > vmax) {
			h = vmax;
		}
		master.vvalueProperty().setValue(h);
	};

	/**
	 * Binds the NavigatorWidget to a ZoomPane.
	 *
	 * @param zoomPane the master ZoomPane.
	 */
	public final void bind(ZoomPane zoomPane) {
		unbind();
		master = zoomPane;
		master.zoomContentProperty().addListener(contentListener);
		master.zoomProperty().addListener(zoomListener);
		master.hvalueProperty().addListener(scrollListener);
		master.vvalueProperty().addListener(scrollListener);
		master.widthProperty().addListener(scrollListener);
		master.heightProperty().addListener(scrollListener);
		master.needsLayoutProperty().addListener(scrollListener);
		view.zoom.setOnKeyPressed(textFieldEvent);
		view.slider.valueProperty().addListener(sliderListener);
		view.viewport.setOnMousePressed(viewportDownEvent);
		view.viewport.setOnMouseDragged(viewportDragEvent);
		sliderListener.changed(null, 0, zoomValToSlider(master.getZoom()));
		zoomListener.changed(null, 0, master.getZoom());
		contentListener.invalidated(null);
		updateSnapshot();
	}

	/**
	 * Unbinds the NavigatorWidget.
	 */
	public final void unbind() {
		if (master == null) {
			return;
		}

		view.viewport.setOnMousePressed(null);
		view.viewport.setOnMouseDragged(null);
		view.slider.valueProperty().removeListener(sliderListener);
		view.zoom.setOnKeyPressed(null);
		master.hvalueProperty().removeListener(scrollListener);
		master.vvalueProperty().removeListener(scrollListener);
		master.widthProperty().removeListener(scrollListener);
		master.heightProperty().removeListener(scrollListener);
		master.zoomProperty().removeListener(zoomListener);
		master.zoomContentProperty().removeListener(contentListener);

		master = null;
	}

	private void updateSnapshot() {
		final Bounds page = master.getContentBounds();
		view.scale = Math.min(
				view.pane.getWidth() / page.getWidth(),
				view.pane.getHeight() / page.getHeight()
		);
		view.snapshotScaleTransform.setX(view.scale);
		view.snapshotScaleTransform.setY(view.scale);
		final WritableImage snapshot = master.snapshot(view.snapshotParams);
		view.snapshot.setImage(snapshot);

		updateViewport();
	}

	private void updateViewport() {
		final Bounds viewport = master.getViewportBounds();
		final Bounds zoomed = master.getContentBoundsZoomed();
		final double invZoom = 1.0 / master.getZoom();
		final double scaleFactor = invZoom * view.scale;

		final double scrollableWidth = zoomed.getWidth() - viewport.getWidth();
		final double scrollableHeight = zoomed.getHeight() - viewport.getHeight();

		if (scrollableWidth <= 0) {
			view.viewport.setWidth(view.pane.getWidth());
			view.viewport.setX(0);
		} else {
			view.viewport.setWidth(viewport.getWidth() * scaleFactor);
			final double hScroll = master.getHvalue() * (master.getHmax() - master.getHmin());
			final double hOffset = hScroll * (zoomed.getWidth() - viewport.getWidth());
			view.viewport.setX(hOffset * scaleFactor);
		}

		if (scrollableHeight <= 0) {
			view.viewport.setHeight(view.pane.getHeight());
			view.viewport.setY(0);
		} else {
			view.viewport.setHeight(viewport.getHeight() * scaleFactor);
			final double vScroll = master.getVvalue() * (master.getVmax() - master.getVmin());
			final double vOffset = vScroll * (zoomed.getHeight() - viewport.getHeight());
			view.viewport.setY(vOffset * scaleFactor);
		}
	}

	public static class View extends VBox {

		private final ImageView snapshot = new ImageView();
		private final Pane pane = new Pane();
		private final Rectangle viewport = new Rectangle();

		private final Lane lane = new Lane();
		private final TextField zoom = new TextField();
		private final Slider slider = new Slider();

		private final SnapshotParameters snapshotParams = new SnapshotParameters();
		private final Scale snapshotScaleTransform = new Scale(1.0, 1.0);

		private boolean mouseInViewport;
		private double scale = 1;
		private double x;
		private double y;
		private double h;
		private double v;

		public View() {
			zoom.setPrefWidth(62.5);
			zoom.setAlignment(Pos.CENTER_RIGHT);

			final double one = zoomValToSlider(1);

			slider.setMin(zoomValToSlider(MIN_ZOOM_VALUE));
			slider.setMax(zoomValToSlider(MAX_ZOOM_VALUE * 0.01));
			slider.setValue(one);
			slider.setBlockIncrement(one * 0.05);
			slider.setMinorTickCount(4);
			slider.setMajorTickUnit(one);

			viewport.setFill(Color.TRANSPARENT);
			viewport.getStyleClass().add("dip-viewport");
			viewport.setStyle("-fx-stroke: -fx-accent;");
			viewport.setStrokeWidth(2);
			viewport.setStrokeType(StrokeType.INSIDE);

			viewport.setOnMouseEntered(e -> {
				mouseInViewport = true;
				viewport.getScene().setCursor(Cursor.OPEN_HAND);
			});
			viewport.setOnMouseExited(e -> {
				mouseInViewport = false;
				viewport.getScene().setCursor(Cursor.DEFAULT);
			});
			viewport.setOnMouseReleased(e -> {
				viewport.getScene().setCursor(mouseInViewport
						? Cursor.OPEN_HAND
						: Cursor.DEFAULT
				);
			});

			pane.setPrefHeight(120);
			pane.getChildren().addAll(snapshot, viewport);
			lane.add(zoom);
			lane.add(slider, Priority.ALWAYS);

			snapshotParams.setTransform(snapshotScaleTransform);

			this.getChildren().addAll(pane,
					new Separator(),
					lane
			);
		}

		public void setDisableViewport(boolean disable) {
			viewport.setDisable(disable);
			viewport.setOpacity(disable ? 0 : 1);
		}
	}

}
