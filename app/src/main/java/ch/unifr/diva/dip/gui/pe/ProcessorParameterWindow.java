package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.layout.AbstractWindow;
import ch.unifr.diva.dip.gui.layout.Lane;
import ch.unifr.diva.dip.gui.layout.Pannable;
import ch.unifr.diva.dip.gui.layout.ZoomPaneBresenham;
import ch.unifr.diva.dip.gui.layout.ZoomSlider;
import ch.unifr.diva.dip.gui.layout.Zoomable;
import java.awt.Rectangle;
import java.util.Collection;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
	private final PrototypeProcessor prototype;
	private final ProcessorView.ProcessorHead head;
	private final ProcessorView.ParameterViewBase<? extends Parent> parameterView;
	private final PreviewWidget previewWidget;
	private final InvalidationListener valueListener;
	private final BooleanProperty patchedProperty;

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

		final Pipeline<PrototypeProcessor> pipelinePrototype = handler.getProject().getSelectedPage().getPipelinePrototype();
		this.prototype = pipelinePrototype.getProcessor(runnable.id);

		this.patchedProperty = new SimpleBooleanProperty();
		this.runnable.processor().getCompositeProperty().addListener((c) -> updatePatchedProperty());
		updatePatchedProperty();

		final double b = UIStrategyGUI.Stage.insets;
		final Insets insets = new Insets(b);

		this.head = new ProcessorView.ProcessorHead(handler, runnable);
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
		reset.disableProperty().bind(Bindings.not(this.patchedProperty));
		reset.setOnAction((e) -> resetParameters());
		sideBox.getChildren().addAll(ok, reset);
		this.root.setRight(sideBox);

		// kill off preview if the processor isn't in PROCESSING state
		final boolean canPreview = runnable.getState().equals(Processor.State.PROCESSING);
		// and make sure we have a valid previewContext
		final PreviewContext previewContext = (runnable.isPreviewable() && canPreview)
				? new PreviewContext(handler, runnable)
				: new PreviewContext();

		if (previewContext.isValid()) {
			this.previewWidget = new PreviewWidget(handler, previewContext);
			this.valueListener = (c) -> previewWidget.onParamChanged();
			this.root.setLeft(this.previewWidget.getNode());

			// listen to input changes
			final Collection<InputPort<?>> inputs = runnable.processor().inputs().values();
			for (InputPort<?> input : inputs) {
				input.valueChangedProperty().addListener(valueListener);
			}
		} else {
			this.previewWidget = null;
			this.valueListener = null;
			final Region spacer = new Region();
			spacer.setPrefWidth(b * 2);
			this.root.setLeft(spacer);
		}

		this.setOnCloseRequest((e) -> onClose(e));
	}

	private void updatePatchedProperty() {
		this.patchedProperty.set(!this.prototype.equalParameters(this.runnable));
	}

	private void resetParameters() {
		this.runnable.copyParameters(this.prototype);
		this.patchedProperty.set(false);
	}

	@Override
	final public void close() {
		onClose(null);
		super.close();
	}

	private void onClose(WindowEvent e) {
		if (this.valueListener != null) {
			final Collection<InputPort<?>> inputs = runnable.processor().inputs().values();
			for (InputPort<?> input : inputs) {
				input.valueChangedProperty().removeListener(valueListener);
			}
		}
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
	 * Preview context. A preview context is only valid if we manage to retireve
	 * the source image (to be previewed). Even if a processor implements
	 * previewable it is still free to return null here to abort the preview
	 * (for whatever reason).
	 */
	public static class PreviewContext {

		public final RunnableProcessor runnable;
		public final Previewable previewable;
		public final ProcessorContext context;
		public final Image source;

		/**
		 * Creates a new preview context.
		 *
		 * @param handler the application handler.
		 * @param runnable the runnable (and previewable) processor.
		 */
		public PreviewContext(ApplicationHandler handler, RunnableProcessor runnable) {
			this.runnable = runnable;
			this.previewable = runnable.getPreviewable();
			this.context = runnable.getProcessorContext();
			this.source = previewable.previewSource(context);
		}

		/**
		 * Creates an invalid preview context.
		 */
		public PreviewContext() {
			this.runnable = null;
			this.previewable = null;
			this.context = null;
			this.source = null;
		}

		/**
		 * Checks whether this is a valid preview context or not.
		 *
		 * @return {@code true} if the preview context is valid, {@code false}
		 * otherwise.
		 */
		public boolean isValid() {
			return source != null;
		}
	}

	/**
	 * A preview widget with ZoomPane and ZoomSlider.
	 */
	public static class PreviewWidget implements Localizable {

		private final DipThreadPool threadPool;
		private final PreviewContext previewContext;
		private final VBox vbox;
		private final ZoomPaneBresenham zoomPane;
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
		 * @param previewContext the preview context.
		 */
		public PreviewWidget(ApplicationHandler handler, PreviewContext previewContext) {
			/*
			 * Eh..., let's rather have our own (discarding) thread pool, since the
			 * other one is also used by the active zoom pane of the preview widget.
			 */
//			this.threadPool = handler.discardingThreadPool;
			this.threadPool = DipThreadPool.newDiscardingThreadPool("dip-preview-pool", 1, 1);
			this.previewContext = previewContext;
			this.zoomPane = new ZoomPaneBresenham(handler.discardingThreadPool);
			zoomPane.setInterpolation(
					Zoomable.Interpolation.get(handler.settings.editor.interpolation)
			);
			zoomPane.getNode().setMinWidth(128);
			zoomPane.getNode().setMinHeight(128);
			zoomPane.getNode().setPrefWidth(256);
			zoomPane.getNode().setPrefHeight(256);
			zoomPane.getNode().setMaxWidth(512);
			zoomPane.getNode().setMaxHeight(512);

			this.scrollListener = (ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
				update();
			};
			zoomPane.hvalueProperty().addListener(scrollListener);
			zoomPane.vvalueProperty().addListener(scrollListener);
			zoomPane.widthProperty().addListener(scrollListener);
			zoomPane.heightProperty().addListener(scrollListener);
			zoomPane.needsLayoutProperty().addListener(scrollListener);

			this.previewSource = new ImageView();
			if (previewContext.isValid()) {
				this.previewSourceWidth = (int) previewContext.source.getWidth();
				this.previewSourceHeight = (int) previewContext.source.getHeight();
				previewSource.setImage(previewContext.source);
			} else {
				this.previewSourceWidth = 0;
				this.previewSourceHeight = 0;
			}

			this.previewSubImage = new ImageView();
			this.previewPane = new Pane();
			previewPane.getChildren().setAll(previewSource, previewSubImage);
			zoomPane.setContent(previewPane);

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
			vbox.getChildren().addAll(zoomPane.getNode(), gridPane);

			// TODO/VERIFY: what about repainting procs? Would they change params?
			// Should we re-listen? Or should repainting procs that actually do
			// change params overwrite getCompositeProperty? Maybe easier...
			final ReadOnlyObjectProperty<?> p = previewContext.runnable.processor().getCompositeProperty();
			p.addListener((e) -> onParamChanged());
			onParamChanged();

			// Center the preview clipping assuming a larger image, but dont set
			// slider position if we can see all the image at once (or the zoompane
			// will go bonkers...).
			// zoomPane.getViewportBounds() aren't set/ready yet, so we take the
			// bounds of the vbox (call to layout is required to update bounds of
			// the zoompane).
			vbox.layout();
			final Bounds content = zoomPane.getScaledContentBounds();

			if (content.getWidth() > zoomPane.getNode().getWidth()) {
				zoomPane.setHvalue(.5);
			}
			if (content.getHeight() > zoomPane.getNode().getHeight()) {
				zoomPane.setVvalue(.5);
			}
		}

		private Label newLabel(String text) {
			final Label label = new Label(text);
			final double p = UIStrategyGUI.Stage.insets * 2;
			label.setPadding(new Insets(p, p, p, 0));
			label.getStyleClass().add("dip-small");
			return label;
		}

		final protected void onParamChanged() {
			previewContext.previewable.previewSetup(previewContext.context);
			update();
		}

		final protected void update() {
			final Pannable.VisibleRegion visibleRegion = zoomPane.getVisibleRegion();
			if (!visibleRegion.isEmpty()) {
				final Pannable.SubpixelRectangle2D sourceRegion = visibleRegion.getUnscaledVisibleSubpixelRegion();
				final Rectangle region = sourceRegion.getRectangle();
				final Runnable run = () -> {
					final Image preview = previewContext.previewable.preview(
							previewContext.context,
							region
					);
					if (preview != null) {
						Platform.runLater(() -> {
							previewSubImage.setImage(preview);
							previewSubImage.setLayoutX(region.x);
							previewSubImage.setLayoutY(region.y);
							zoomPane.fireContentChange();
						});
					}
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
