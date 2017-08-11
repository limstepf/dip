package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.gui.AbstractWidget;
import ch.unifr.diva.dip.gui.layout.Pannable;
import ch.unifr.diva.dip.gui.layout.ZoomSlider;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Scale;

/**
 * A navigator widget for pannable components.
 *
 * @param <T> class of the pannable.
 */
public class NavigatorWidget<T extends Pannable> extends AbstractWidget {

	protected final T pannable;
	protected final NavigatorWidgetView<T> view;
	protected final InvalidationListener contentChangedListener;
	protected final ChangeListener<Object> viewportListener;
	protected final EventHandler<MouseEvent> viewportDownEvent;
	protected final EventHandler<MouseEvent> viewportDragEvent;

	/**
	 * Creates a new navigator widget.
	 *
	 * @param pannable the pannable to control.
	 */
	public NavigatorWidget(T pannable) {
		super();
		this.pannable = pannable;
		this.view = new NavigatorWidgetView<>(this.pannable);

		this.contentChangedListener = (e) -> {
			view.setDisableViewport(pannable.isEmpty());
			view.updateSnapshot();
		};
		this.viewportListener = (ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
			view.updateViewport();
		};
		this.viewportDownEvent = (e) -> view.onViewportDown(e);
		this.viewportDragEvent = (e) -> view.onViewportDrag(e);

		setWidget(view);
		setTitle(localize("widget.navigator"));

		bind(pannable);
		view.updateSnapshot();
	}

	private void bind(T pannable) {
		this.pannable.contentChangedProperty().addListener(contentChangedListener);
		this.pannable.hvalueProperty().addListener(viewportListener);
		this.pannable.vvalueProperty().addListener(viewportListener);
		this.pannable.widthProperty().addListener(viewportListener);
		this.pannable.heightProperty().addListener(viewportListener);
		this.pannable.needsLayoutProperty().addListener(viewportListener);

		view.viewport.setOnMousePressed(viewportDownEvent);
		view.viewport.setOnMouseDragged(viewportDragEvent);

		contentChangedListener.invalidated(null);
	}

	/**
	 * The navigator widget view.
	 *
	 * @param <T> class of the pannable.
	 */
	public static class NavigatorWidgetView<T extends Pannable> extends VBox {

		protected final T pannable;
		protected final Pane pane;
		protected final ImageView snapshot;
		protected final Scale snapshotScaleTransform;
		protected final SnapshotParameters snapshotParams;
		protected final EventHandler<MouseEvent> snapshotClickEvent;
		protected final Rectangle viewport;
		protected final ZoomSlider zoomSlider;

		protected boolean mouseInViewport;
		protected double scale = 1;
		protected double x;
		protected double y;
		protected double h;
		protected double v;

		/**
		 * Creates a new navigator widget view.
		 *
		 * @param pannable the pannable to control.
		 */
		public NavigatorWidgetView(T pannable) {
			this.pannable = pannable;
			this.snapshotScaleTransform = new Scale(1.0, 1.0);
			this.snapshotParams = new SnapshotParameters();
			snapshotParams.setTransform(snapshotScaleTransform);
			this.snapshotClickEvent = (e) -> onSnapshotClicked(e);
			this.snapshot = new ImageView();
			snapshot.setOnMouseClicked(snapshotClickEvent);
			snapshot.setOnMouseEntered(e -> {
				snapshot.getScene().setCursor(Cursor.HAND);
			});
			snapshot.setOnMouseExited(e -> {
				snapshot.getScene().setCursor(Cursor.DEFAULT);
			});

			this.viewport = new Rectangle();
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

			this.pane = new Pane();
			pane.setPrefHeight(120);
			pane.getChildren().addAll(snapshot, viewport);

			this.zoomSlider = new ZoomSlider(pannable);

			this.getChildren().addAll(
					pane,
					new Separator(),
					zoomSlider.getNode()
			);
		}

		public void setDisableViewport(boolean disable) {
			viewport.setDisable(disable);
			viewport.setOpacity(disable ? 0 : 1);
		}

		protected void updateSnapshot() {
			final Bounds contentBounds = pannable.getContentBounds();
			scale = Math.min(
					pane.getWidth() / contentBounds.getWidth(),
					pane.getHeight() / contentBounds.getHeight()
			);
			snapshotScaleTransform.setX(scale);
			snapshotScaleTransform.setY(scale);
			final WritableImage image = pannable.getContentPane().snapshot(snapshotParams, null);
			snapshot.setImage(image);

			updateViewport();
		}

		protected void updateViewport() {
			final Pannable.VisibleRegion visibleRegion = pannable.getVisibleRegion();
			final double scaleFactor = scale / visibleRegion.zoom;

			if (visibleRegion.isEmpty()) {
				if (!viewport.isDisable()) {
					setDisableViewport(true);
				}
			} else {
				if (viewport.isDisable()) {
					setDisableViewport(false);
				}

				if (visibleRegion.scrollableWidth <= 0) {
					viewport.setWidth(pane.getWidth());
					viewport.setX(0);
				} else {
					viewport.setWidth(visibleRegion.getScaledWidth() * scaleFactor);
					viewport.setX(visibleRegion.getScaledOffsetX() * scaleFactor);
				}

				if (visibleRegion.scrollableHeight <= 0) {
					viewport.setHeight(pane.getHeight());
					viewport.setY(0);
				} else {
					viewport.setHeight(visibleRegion.getScaledHeight() * scaleFactor);
					viewport.setY(visibleRegion.getScaledOffsetY() * scaleFactor);
				}
			}
		}

		protected final void onSnapshotClicked(MouseEvent e) {
			final double invScale = 1 / scale;
			pannable.moveToPixel(
					e.getX() * invScale,
					e.getY() * invScale
			);
		}

		protected void onViewportDown(MouseEvent e) {
			viewport.getScene().setCursor(Cursor.CLOSED_HAND);
			x = e.getSceneX();
			y = e.getSceneY();
			h = pannable.getHvalue();
			v = pannable.getVvalue();
		}

		protected void onViewportDrag(MouseEvent e) {
			final double offsetX = e.getSceneX() - x;
			final double offsetY = e.getSceneY() - y;
			final Bounds viewportBounds = pannable.getViewportBounds();
			/*
			 * In a way, neither ScaledContentBounds, nor PaddedContentBounds are
			 * the correct thing to take here, since padding is only visible in
			 * the ScrollPane, but not in the NavigatorWidget. We take the latter,
			 * since working with ScaledContentBounds leads to hard control jumps/
			 * too quick movement with a low scale factor. The trade-off is that
			 * panning by the NavigatorWidget becomes a little bit more slow/sluggish,
			 * but that's much better.
			 */
			final Bounds scaledBounds = pannable.getPaddedContentBounds();
			final double scrollableWidth = scaledBounds.getWidth() - viewportBounds.getWidth();
			final double scrollableHeight = scaledBounds.getHeight() - viewportBounds.getHeight();
			final double invScale = 1 / scale;
			final double scaleFactor = invScale * pannable.getZoom();

			double hpos = pannable.getHmin();
			if (scrollableWidth > 0) {
				final double hmin = pannable.getHmin();
				final double hmax = pannable.getHmax();
				final double hrange = pannable.getHrangeReal();
				final double hppx = hrange / scrollableWidth;
				hpos = (offsetX * scaleFactor * hppx) + h;

				if (hpos < hmin || scrollableWidth < 0) {
					hpos = hmin;
				} else if (hpos > hmax) {
					hpos = hmax;
				}
			}

			double vpos = pannable.getVmin();
			if (scrollableHeight > 0) {
				final double vmin = pannable.getVmin();
				final double vmax = pannable.getVmax();
				final double vrange = pannable.getVrangeReal();
				final double vppx = vrange / scrollableHeight;
				vpos = (offsetY * scaleFactor * vppx) + v;

				if (vpos < vmin || scrollableHeight < 0) {
					vpos = vmin;
				} else if (vpos > vmax) {
					vpos = vmax;
				}
			}

			pannable.setViewportPosition(hpos, vpos);
		}

	}

}
