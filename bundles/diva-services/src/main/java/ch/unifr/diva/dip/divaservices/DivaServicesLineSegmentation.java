package ch.unifr.diva.dip.divaservices;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.datastructures.NestedRectangle2D;
import ch.unifr.diva.dip.api.datastructures.NestedRectangles2D;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.services.HybridProcessorBase;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.services.DivaServicesCommunicator;
import ch.unifr.diva.services.returnTypes.DivaServicesResponse;
import javafx.geometry.Bounds;
import javafx.scene.shape.Shape;
import org.osgi.service.component.annotations.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * DivaServices line segmentation processor.
 */
@Component(service = Processor.class)
public class DivaServicesLineSegmentation extends HybridProcessorBase {

	/**
	 * Available segmentation methods. Note that OCROPY and SEAMCARVING seem to
	 * be broken, and throw JSONException or NullPointerException...
	 *
	 * <p>
	 * And just for the record: instead of this, we should rely on service
	 * discovery at runtime (needs a better DivaServices Java API) and also
	 * consider available parameters. A copy of the service definition should be
	 * stored/managed locally for the case things go unavailable.
	 */
	public enum SegMethod {

		HISTOGRAM(
				"Histogram Based Text Line Segmentation",
				"Simple Text Line Segmentation using Histograms"
		) {
					@Override
					public DivaServicesResponse run(DivaServicesCommunicator communicator, BufferedImage image, Rectangle rectangle) {
						return communicator.runHistogramTextLineExtraction(image, rectangle);
					}
				},
		OCROPY(
				"ocropy - page segmentation",
				"ocropy page segmentation method"
		) {
					@Override
					public DivaServicesResponse run(DivaServicesCommunicator communicator, BufferedImage image, Rectangle rectangle) {
						return communicator.runOcropyPageSegmentation(image, false);
					}
				},
		SEAMCARVING(
				"Seam Carving Based Text Line Segmentation",
				"Text line segmentation using seam carving approach"
		) {
					@Override
					public DivaServicesResponse run(DivaServicesCommunicator communicator, BufferedImage image, Rectangle rectangle) {
						return communicator.runSeamCarvingTextlineExtraction(image, rectangle, false);
					}
				};

		private final String name;
		private final String description;

		/**
		 * Creates a new segmentation method.
		 *
		 * @param name the name.
		 * @param description the description.
		 */
		private SegMethod(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		abstract public DivaServicesResponse run(DivaServicesCommunicator communicator, BufferedImage image, Rectangle rectangle);

	}

	private final static String DIVA_SERVICES_API = "http://divaservices.unifr.ch/api/v1/";
	private final static String STORAGE_LINES_XML = "lines.xml";
	private final static String STORAGE_IMAGE_LINES = "lines-layer.png";
	private final static String STORAGE_IMAGE_FORMAT = "png";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_binary;
	private final InputPort<BufferedImage> input_gray;
	private final XorInputPorts input_xor;
	private final EnumParameter segMethod;

	private final OutputPort<NestedRectangles2D> output_lines;

	// TODO: we really should offer a ButtonParameter as option to copy the
	// current selection, and then we should make that selection persistent.
	// Alternatively we could offer our own, dedicated RectangularSelectionTool
	// instead of relying on the global selection tool... thing is, we need the
	// possibility to keep accumulating new detected lines from different regions
	// which isn't possible right now...
	private Shape currentSelection;

	/**
	 * Creates a new DivaServices line segmentation processor.
	 */
	public DivaServicesLineSegmentation() {
		super("DivaServices Line Segmentation");

		this.segMethod = new EnumParameter(
				"Method",
				SegMethod.class,
				SegMethod.HISTOGRAM.name()
		);
		// let's disable this for now, since other methods aren't working...
		segMethod.addComboBoxViewHook((c) -> {
			c.setDisable(true);
		});
		parameters.put("method", segMethod);

		this.input = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				false
		);
		this.input_binary = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary(),
				false
		);
		this.input_gray = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageGray(),
				false
		);
		this.input_xor = new XorInputPorts(this);
		input_xor.addPort("buffered-image", input);
		input_xor.addPort("buffered-image-binary", input_binary);
		input_xor.addPort("buffered-image-gray", input_gray);
		input_xor.enableAllPorts();

		this.output_lines = new OutputPort<>(
				"lines",
				new ch.unifr.diva.dip.api.datatypes.NestedRectangles2D()
		);
		outputs.put("lines", output_lines);
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.FORMAT_LINE_SPACING;
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new DivaServicesLineSegmentation();
	}

	@Override
	public void init(ProcessorContext context) {
		input_xor.init(context);

		if (context != null) {
			restoreOutputs(context);
		}
	}

	@Override
	public boolean isConnected() {
		return input_xor.isConnected();
	}

	@Override
	public void onSelectionMaskChanged(Shape selectionMask) {
		currentSelection = selectionMask;
	}

	@Override
	public void process(ProcessorContext context) {
		try {
			final BufferedImage src = getSourceImage();
			cancelIfInterrupted(src);
			final Rectangle rect = getRectangle(src);
			cancelIfInterrupted(rect);
			final SegMethod method = getSegMethod();
			cancelIfInterrupted(method);

			final DivaServicesCommunicator communicator = new DivaServicesCommunicator(
					DIVA_SERVICES_API
			);
			Thread.sleep(2000);

			final DivaServicesResponse response = method.run(communicator, src, rect);
			cancelIfInterrupted(response);

			@SuppressWarnings("unchecked")
			final List<Rectangle> rectangles = response.getHighlighter().getData();
			cancelIfInterrupted(rectangles);
			log.info(
					"DivaServices {}: {} line(s) detected.",
					method.getName(),
					rectangles.size()
			);

			final NestedRectangles2D lines = new NestedRectangles2D();
			for (Rectangle r : rectangles) {
				cancelIfInterrupted();
				final NestedRectangle2D lineRect = new NestedRectangle2D(
						r.getX(),
						r.getY(),
						r.getWidth(),
						r.getHeight()
				);
				lines.add(lineRect);
			}

			writeObject(context, lines, STORAGE_LINES_XML);
			cancelIfInterrupted();

			setOutputs(context, lines);
			cancelIfInterrupted();
		} catch (InterruptedException ex) {
			reset(context);
		}
	}

	protected void restoreOutputs(ProcessorContext context) {
		final NestedRectangles2D lines = ProcessableBase.readObject(
				context,
				STORAGE_LINES_XML,
				NestedRectangles2D.class
		);
		setOutputs(context, lines);
	}

	protected void setOutputs(ProcessorContext context, NestedRectangles2D lines) {
		output_lines.setOutput(lines);

		if (lines != null && !lines.isEmpty()) {
			final BufferedImage image = ProcessorBase.readBufferedImage(context, STORAGE_IMAGE_LINES);
			if (image != null) {
				provideImageLayer(context, image, "Lines");
			} else {
				final RenderLayer render = renderLayer(getSourceImage(), lines);
				if (render.image != null) {
					writeBufferedImage(context, render.image, STORAGE_IMAGE_LINES, STORAGE_IMAGE_FORMAT);
					provideImageLayer(context, render.image, "Lines");
				}
			}
		}
	}

	protected SegMethod getSegMethod() {
		return segMethod.getEnumValue(SegMethod.class);
	}

	protected Rectangle getRectangle(BufferedImage image) {
		if (currentSelection != null) {
			final Bounds bounds = currentSelection.getBoundsInLocal();
			return new Rectangle(
					(int) bounds.getMinX(),
					(int) bounds.getMinY(),
					(int) bounds.getWidth(),
					(int) bounds.getHeight()
			);
		}

		return new Rectangle(image.getWidth(), image.getHeight());
	}

	protected BufferedImage getSourceImage() {
		final InputPort<?> port = input_xor.getEnabledPort();
		if (port == null) {
			log.warn("no input port enabled");
			return null;
		}

		if (port.equals(input_binary)) {
			return input_binary.getValue();
		} else if (port.equals(input_gray)) {
			return input_gray.getValue();
		} else {
			return input.getValue();
		}
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_LINES_XML);
		deleteFile(context, STORAGE_IMAGE_LINES);

		resetOutputs();
		resetLayer(context);
	}

	protected RenderLayer renderLayer(BufferedImage source, NestedRectangles2D lines) {
		if (lines == null || lines.isEmpty()) {
			return new RenderLayer("Lines (empty)");
		}

		final RenderLayer linesLayer = new RenderLayer("Lines", source);
		linesLayer.g.setColor(Color.MAGENTA);
		for (NestedRectangle2D line : lines) {
			linesLayer.drawRect(line);
		}
		linesLayer.dispose();
		return linesLayer;
	}

	/**
	 * Render layer.
	 */
	protected static class RenderLayer {

		final public String name;
		final public BufferedImage image;
		final public Graphics2D g;

		public RenderLayer(String name, BufferedImage source) {
			this.name = name;
			this.image = new BufferedImage(
					source.getWidth(),
					source.getHeight(),
					BufferedImage.TYPE_INT_ARGB
			);
			this.g = image.createGraphics();
		}

		public RenderLayer(String name) {
			this.name = name;
			this.image = null;
			this.g = null;
		}

		public void drawRect(NestedRectangle2D rect) {
			g.drawRect(
					(int) rect.getX(),
					(int) rect.getY(),
					(int) Math.round(rect.getWidth()),
					(int) Math.round(rect.getHeight())
			);
		}

		public void dispose() {
			if (g == null) {
				return;
			}
			g.dispose();
		}

	}

}
