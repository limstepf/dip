package ch.unifr.diva.dip.fx.paint;

import ch.unifr.diva.dip.api.components.EditorLayerPane;
import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.color.ColorPortsUnit;
import ch.unifr.diva.dip.api.datastructures.FxColor;
import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import ch.unifr.diva.dip.api.parameters.ColorPickerParameter;
import ch.unifr.diva.dip.api.services.HybridProcessorBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.services.Transmutable;
import ch.unifr.diva.dip.api.tools.BrushTool;
import ch.unifr.diva.dip.api.tools.ClickGesture;
import ch.unifr.diva.dip.api.tools.SimpleTool;
import ch.unifr.diva.dip.api.tools.brush.CirclePaintBrush;
import ch.unifr.diva.dip.api.tools.brush.PaintBrush;
import ch.unifr.diva.dip.api.tools.brush.SquarePaintBrush;
import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.api.utils.ShapeUtils;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.glyphs.FontAwesome;
import ch.unifr.diva.dip.glyphs.MaterialDesignIcons;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.util.Callback;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * A simple JavaFX paint processor.
 */
@Component
@Service
public class FxPaint extends HybridProcessorBase implements Transmutable {

	protected final static String STORAGE_FORMAT = "PNG";
	protected final static String STORAGE_IMAGE = "fxpaint.png";

	private final Callback<InputPort<BufferedImage>, Void> onInputCallback;
	private final ColorPortsUnit colorPortsUnit;
	private final ColorPickerParameter primaryColorOption;
	private final PencilTool pencilTool;
	private final EyedropperTool eyedropperTool;
	private final ObjectProperty<GraphicsContext> gcProperty;
	private Canvas canvas;

	/**
	 * Creates a new FxPaint processor.
	 */
	public FxPaint() {
		super("FX Paint");

		this.gcProperty = new SimpleObjectProperty();
		this.onInputCallback = (in) -> initCanvas(in.getValue());
		this.colorPortsUnit = new ColorPortsUnit(
				this,
				"fx-paint",
				true, // bit
				true, // byte
				false, // float
				Arrays.asList(
						SimpleColorModel.RGB,
						SimpleColorModel.RGBA
				)
		);
		this.colorPortsUnit.setOnInput(this.onInputCallback);
		this.parameters.put("config", this.colorPortsUnit.getParameter());

		this.primaryColorOption = new ColorPickerParameter(
				L10n.getInstance().getString("color"),
				Color.BLUE
		);

		// processor options for all tools
		options().put("primary-color", primaryColorOption);

		this.pencilTool = new PencilTool();
		tools().add(pencilTool);

		this.eyedropperTool = new EyedropperTool();
		tools().add(eyedropperTool);

	}

	private final BooleanProperty transmuteProperty = new SimpleBooleanProperty();

	@Override
	public BooleanProperty transmuteProperty() {
		return this.transmuteProperty;
	}

	/**
	 * Any tool that modifies the canvas must set {@code isDirty} to
	 * {@code true}.
	 */
	private boolean isDirty;

	private GraphicsContext getGC() {
		return gcProperty.get();
	}

	private void setGC(GraphicsContext gc) {
		gcProperty.set(gc);
	}

	private Color getPrimaryColor() {
		return primaryColorOption.getColor();
	}

	private void setPrimaryColor(Color color) {
		primaryColorOption.set(new FxColor(color));
	}

	private void initCanvas() {
		initCanvas(this.colorPortsUnit.getValue());
	}

	private Void initCanvas(BufferedImage in) {
		if (!hasContext() || in == null) {
			return null;
		}

		// make sure we have a canvas with proper dimensions
		final int width = in.getWidth();
		final int height = in.getHeight();

		if (this.canvas != null) {
			if ((int) this.canvas.getHeight() != height || (int) this.canvas.getWidth() != width) {
				this.canvas = null;
			}
		}

		if (this.canvas == null) {
			this.canvas = new Canvas(width, height);
		}

		// try to restore modified image from disk
		BufferedImage image = ProcessorBase.readBufferedImage(getContext(), STORAGE_IMAGE);
		if (image == null) {
			image = in;
		}
		// and set output immediately in either case
		this.colorPortsUnit.setOutputs(image);

		setGC(canvas.getGraphicsContext2D());
		final Image fximage = SwingFXUtils.toFXImage(image, null);
		getGC().drawImage(fximage, 0, 0);
		isDirty = false;

		getContext().getLayer().clear();
		final EditorLayerPane layer = getContext().getLayer().newLayerPane();
		layer.add(canvas);

		return null;
	}

	private BufferedImage saveCanvas(ProcessorContext context) {
		if (context == null || this.canvas == null) {
			return null;
		}

		try {
			final WritableImage rendered = FxUtils.runFutureTask(() -> {
				final WritableImage img = new WritableImage(
						(int) this.canvas.getWidth(),
						(int) this.canvas.getHeight()
				);
				canvas.snapshot(null, img);
				return img;
			});
			final BufferedImage out = SwingFXUtils.fromFXImage(rendered, null);
			ProcessorBase.writeBufferedImage(context, STORAGE_IMAGE, STORAGE_FORMAT, out);
			isDirty = false;
			return out;
		} catch (Exception ex) {
			log.error("Failed to save the canvas in: {}", context, ex);
			return null;
		}
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		final FxPaint p = new FxPaint();
		p.setContext(context);
		return p;
	}

	@Override
	public void init(ProcessorContext context) {
		this.colorPortsUnit.init(context);
		if (context != null) {
			this.pencilTool.setEditorLayerOverlay(context.getOverlay());
		}
	}

	@Override
	public void reset(ProcessorContext context) {
		this.colorPortsUnit.resetOutputs();
		deleteFile(context, STORAGE_IMAGE);
		// setup a new canvas (this also resets the layer)
		initCanvas();
	}

	@Override
	public void process(ProcessorContext context) {
		BufferedImage image;
		if (isDirty) {
			image = saveCanvas(context);
		} else {
			image = ProcessorBase.readBufferedImage(context, STORAGE_IMAGE);
			if (image == null) {
				image = this.colorPortsUnit.getValue();
			}
		}

		this.colorPortsUnit.setOutputs(image);
	}

	@Override
	public void onContextSwitch(ProcessorContext context, boolean saveRequired) {
		super.onContextSwitch(context, saveRequired);

		// only safe canvas if we made it dirty
		if (saveRequired && isDirty) {
			saveCanvas(context);
		}
	}

	@Override
	public void onSelectionMaskChanged(Shape selectionMask) {
		getGC().restore();
		if (selectionMask != null) {
			final GraphicsContext gc = getGC();
			gc.save();
			ShapeUtils.drawShapeToGraphicsContext(gc, selectionMask);
			gc.clip();
		}
	}

	/**
	 * The pencil tool.
	 */
	private class PencilTool extends BrushTool<PaintBrush> {

		/**
		 * Creates a new pencil tool.
		 */
		public PencilTool() {
			super(
					// brush,
					"Pencil tool",
					MaterialDesignIcons.PENCIL,
					new SquarePaintBrush(
							"Square brush",
							FontAwesome.SQUARE
					),
					new CirclePaintBrush(
							"Circle brush",
							FontAwesome.CIRCLE
					)
			);
		}

		@Override
		protected void onPressed(MouseEvent e) {
			final GraphicsContext gc = getGC();
			gc.save();
			gc.setFill(getPrimaryColor());
			currentBrush.paint(gc, e);
			getContext().getLayer().repaint();
		}

		@Override
		protected void onDragged(double lastX, double lastY, MouseEvent e) {
			currentBrush.paintStroke(getGC(), lastX, lastY, e.getX(), e.getY(), 4);
			getContext().getLayer().repaint();

		}

		@Override
		protected void onReleased(MouseEvent e) {
			getGC().restore();
			isDirty = true;
		}

	}

	/**
	 * The eyedropper (or color picker) tool.
	 */
	private class EyedropperTool extends SimpleTool {

		/**
		 * Creates a new eyedropper tool.
		 */
		public EyedropperTool() {
			super(
					"Eyedropper tool",
					MaterialDesignIcons.EYEDROPPER
			);
			setGesture(new ClickGesture(onClick));
		}

		protected final EventHandler<MouseEvent> onClick = (e) -> {
			if (!e.isPrimaryButtonDown()) {
				return;
			}
			final SnapshotParameters p = new SnapshotParameters();
			final Rectangle2D viewport = new Rectangle2D(
					(int) e.getX(),
					(int) e.getY(),
					1,
					1
			);
			p.setViewport(viewport);
			final WritableImage pixel = new WritableImage(1, 1);
			canvas.snapshot(p, pixel);
			final Color color = pixel.getPixelReader().getColor(0, 0);
			setPrimaryColor(color);
		};

	}

}
