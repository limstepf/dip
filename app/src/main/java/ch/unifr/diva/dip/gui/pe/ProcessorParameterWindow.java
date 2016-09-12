package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.ActiveInputPort;
import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.layout.AbstractWindow;
import ch.unifr.diva.dip.gui.layout.Lane;
import ch.unifr.diva.dip.gui.layout.ZoomPane;
import ch.unifr.diva.dip.gui.layout.ZoomSlider;
import java.awt.Rectangle;
import java.util.Collection;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * Processor parameter (and preview) window. Popup window to configure a
 * {@code RunnableProcessor} in a {@code RunnablePipeline}.
 *
 * <p>
 * While the pipeline editor is used to define project-wide/global pipelines,
 * this window allows to override the parameters of processors in such a
 * pipeline on a page-level to fine tune individual processing steps.
 */
public class ProcessorParameterWindow extends AbstractWindow implements Presenter {

	private final ApplicationHandler handler;
	private final RunnableProcessor runnable;
	private final ProcessorView.ProcessorHead head;
	private final ProcessorView.ParameterViewBase parameterView;
	private final PreviewWidget previewWidget;

	/**
	 * Creates a new processor parameter (and preview) window. The window can
	 * not be reopened once closed; create a new window instead.
	 *
	 * @param owner owner/parent window.
	 * @param handler the application handler.
	 * @param runnable the runnable processor.
	 */
	public ProcessorParameterWindow(Window owner, ApplicationHandler handler, RunnableProcessor runnable) {
		super(owner, runnable.processor().name());

		// set modality to none s.t. multiple such windows can be operated at
		// the same time
		this.stage.initModality(Modality.NONE);

		this.handler = handler;
		this.runnable = runnable;

		final double b = UIStrategyGUI.Stage.insets;
		final Insets insets = new Insets(b);

		this.head = new ProcessorView.ProcessorHead(this.stage, runnable);
		BorderPane.setMargin(head.getNode(), insets);
		this.root.setTop(head.getNode());

		this.root.setPadding(insets);
		this.parameterView = new ProcessorView.GridParameterView(runnable.processor());
		this.root.setCenter(this.parameterView.node());

		final VBox sideBox = new VBox();
		sideBox.setMinWidth(96);
		sideBox.setPadding(new Insets(b * 2, b * 2, b, b * 5));
		sideBox.setSpacing(b);
		final Button ok = newButton(localize("ok"));
		ok.setOnAction((e) -> {
			this.close();
		});
		final Button reset = newButton(localize("reset"));
		reset.setDisable(true);
		reset.setOnAction((e) -> {
			// TODO
		});
		sideBox.getChildren().addAll(ok, reset);
		this.root.setRight(sideBox);

		if (runnable.isPreviewable()) {
			this.previewWidget = new PreviewWidget(handler, runnable);
			this.root.setLeft(this.previewWidget.getNode());

			// listen to input changes
			final Collection<InputPort> inputs = runnable.processor().inputs().values();
			for (InputPort input : inputs) {
				if (input instanceof ActiveInputPort) {
					final ActiveInputPort a = (ActiveInputPort) input;
					a.valueChangedProperty().addListener((c) -> {
						previewWidget.onParamChanged();
					});
				}
			}
		} else {
			this.previewWidget = null;
			final Region spacer = new Region();
			spacer.setPrefWidth(b * 2);
			this.root.setLeft(spacer);
		}

		this.setOnCloseRequest((e) -> onClose(e));
	}

	@Override
	final public void close() {
		onClose(null);
		super.close();
	}

	private void onClose(WindowEvent e) {
		if (this.previewWidget != null) {
			this.previewWidget.close();
		}
	}

	private Button newButton(String label) {
		final Button b = new Button(label);
		b.setMaxWidth(Double.MAX_VALUE);
		return b;
	}

	@Override
	public Parent getComponent() {
		return null;
	}

	/**
	 * A preview widget with ZoomPane and ZoomSlider.
	 */
	public static class PreviewWidget implements Localizable {

		private final DipThreadPool threadPool;
		private final RunnableProcessor runnable;
		private final Previewable previewable;
		private final ProcessorContext context;
		private final VBox vbox;
		private final ZoomPane zoomPane;
		private final ZoomSlider zoomSlider;
		private final Slider opacitySlider;
		private final EnumParameter blendMode;

		private final Pane previewPane;
		private final ImageView previewSource;
		private final int previewSourceWidth;
		private final int previewSourceHeight;
		private final ImageView previewSubImage;

		private final ChangeListener<Object> scrollListener;

		/**
		 * Creates a new preview widget.
		 *
		 * @param handler the application handler.
		 * @param runnable the runnable processor.
		 */
		public PreviewWidget(ApplicationHandler handler, RunnableProcessor runnable) {
			/*
			 * Eh..., let's rather have our own (discarding) thread pool, since the
			 * other one is also used by the active zoom pane of the preview widget.
			 */
//			this.threadPool = handler.discardingThreadPool;
			this.threadPool = DipThreadPool.newDiscardingThreadPool("dip-preview-pool", 1, 1);
			this.runnable = runnable;
			this.previewable = runnable.getPreviewable();
			this.context = runnable.getProcessorContext();
			this.zoomPane = new ZoomPane(handler.discardingThreadPool);
			zoomPane.setInterpolation(
					ZoomPane.Interpolation.get(handler.settings.editor.interpolation)
			);
			zoomPane.setMinWidth(128);
			zoomPane.setMinHeight(128);
			zoomPane.setPrefWidth(256);
			zoomPane.setPrefHeight(256);
			zoomPane.setMaxWidth(512);
			zoomPane.setMaxHeight(512);
			zoomPane.setHvalue(.5);
			zoomPane.setVvalue(.5);

			this.scrollListener = (ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
				update();
			};
			zoomPane.hvalueProperty().addListener(scrollListener);
			zoomPane.vvalueProperty().addListener(scrollListener);
			zoomPane.widthProperty().addListener(scrollListener);
			zoomPane.heightProperty().addListener(scrollListener);
			zoomPane.needsLayoutProperty().addListener(scrollListener);

			this.previewSource = new ImageView();
			final Image source = previewable.previewSource(context);
			this.previewSourceWidth = (int) source.getWidth();
			this.previewSourceHeight = (int) source.getHeight();
			previewSource.setImage(source);

			this.previewSubImage = new ImageView();
			this.previewPane = new Pane();
			previewPane.getChildren().setAll(previewSource, previewSubImage);
			zoomPane.setZoomContent(previewPane);

			this.zoomSlider = new ZoomSlider(zoomPane);
			this.opacitySlider = new Slider(0, 1, 1);
			opacitySlider.setPrefWidth(96);
			opacitySlider.valueProperty().addListener((c) -> {
				previewSubImage.setOpacity(opacitySlider.getValue());
				zoomPane.fireContentChange();
			});

			this.blendMode = new EnumParameter("blending mode", BlendMode.class, BlendMode.SRC_OVER.name());
			blendMode.addComboBoxViewHook((c) -> {
				c.getStyleClass().add("dip-small");
			});
			blendMode.property().addListener((c) -> {
				final BlendMode mode = EnumParameter.valueOf(blendMode.get(), BlendMode.class, BlendMode.SRC_OVER);
				previewSubImage.setBlendMode(mode);
				zoomPane.fireContentChange();
			});
			final Lane blendLane = new Lane();
			blendLane.getChildren().setAll(blendMode.view().node(), opacitySlider);

			final Label zoomLabel = newLabel(localize("zoom") + ": ");
			final Label opLabel = newLabel(localize("blending") + ": ");

			final GridPane gridPane = new GridPane();
			gridPane.add(zoomLabel, 0, 0);
			gridPane.add(zoomSlider.getNode(), 1, 0);
			gridPane.add(opLabel, 0, 1);
			gridPane.add(blendLane, 1, 1);

			this.vbox = new VBox();
			final double b = UIStrategyGUI.Stage.insets;
			vbox.setPadding(new Insets(b, b * 5, b, b));
			vbox.setSpacing(b);
			vbox.getChildren().addAll(zoomPane, gridPane);

			// TODO/VERIFY: what about transmutable procs? Would they change params?
			// Should we re-listen? Or should transmutable procs that actually do
			// change params overwrite getCompositeProperty? Maybe easier...
			final ReadOnlyObjectProperty p = runnable.processor().getCompositeProperty();
			p.addListener((e) -> onParamChanged());
			onParamChanged();
		}

		private Label newLabel(String text) {
			final Label label = new Label(text);
			final double p = UIStrategyGUI.Stage.insets * 2;
			label.setPadding(new Insets(p, p, p, 0));
			label.getStyleClass().add("dip-small");
			return label;
		}

		protected void onParamChanged() {
			this.previewable.previewSetup(context);
			update();
		}

		protected void update() {
			/*
			 * There are two cases to consider (for each axis individually):
			 *
			 * 1) scrollableWidth/Height is strictly positive, so the region covers
			 * the viewport scaled to the source image with according offsets, or
			 * 2) scrollableWidth/Height is negative/zero, so the region covers
			 * the full source image (with zero offset).
			 *
			 * And some extra care must be taken with "rounding" to nicely cover
			 * the whole viewport with the preview image, or we have some ugly
			 * 1-2px stripes where the preview source is still visible... hence
			 * the {@code + 1.5} below.
			 */
			final double invZoom = 1.0 / zoomPane.getZoom();
			final Bounds viewport = zoomPane.getViewportBounds();
			final int regionWidth;
			final int regionOffsetX;
			final int regionHeight;
			final int regionOffsetY;

			final double scrollableWidth = zoomPane.scrollableWidth();
			if (scrollableWidth > 0) {
				final double scrollOffsetX = zoomPane.scrollOffsetX();
				regionOffsetX = (int) (invZoom * scrollOffsetX);
				final int x = (int) (viewport.getWidth() * invZoom + 1.5);
				final int endX = regionOffsetX + x;
				if (endX > previewSourceWidth) {
					regionWidth = x - (endX - previewSourceWidth);
				} else {
					regionWidth = x;
				}
			} else {
				regionOffsetX = 0;
				regionWidth = previewSourceWidth;
			}

			final double scrollableHeight = zoomPane.scrollableHeight();
			if (scrollableHeight > 0) {
				final double scrollOffsetY = zoomPane.scrollOffsetY();
				regionOffsetY = (int) (invZoom * scrollOffsetY);
				final int y = (int) (viewport.getHeight() * invZoom + 1.5);
				final int endY = regionOffsetY + y;
				if (endY > previewSourceHeight) {
					regionHeight = y - (endY - previewSourceHeight);
				} else {
					regionHeight = y;
				}
			} else {
				regionOffsetY = 0;
				regionHeight = previewSourceHeight;
			}
			final Rectangle region = new Rectangle(
					regionOffsetX,
					regionOffsetY,
					regionWidth,
					regionHeight
			);

			if (region.width > 0 && region.height > 0) {
				final Runnable run = () -> {
					final Image preview = previewable.preview(context, region);
					Platform.runLater(() -> {
						previewSubImage.setImage(preview);
						previewSubImage.setLayoutX(regionOffsetX);
						previewSubImage.setLayoutY(regionOffsetY);
						zoomPane.fireContentChange();
					});
				};

				this.threadPool.getExecutorService().submit(run);
			}
		}

		public void close() {
			if (this.threadPool != null) {
				this.threadPool.shutdown();
			}
		}

		public Node getNode() {
			return vbox;
		}

	}

}
