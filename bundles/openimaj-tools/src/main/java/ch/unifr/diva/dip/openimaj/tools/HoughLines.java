package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.components.EditorLayerPane;
import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.datastructures.Line2D;
import ch.unifr.diva.dip.api.datastructures.Lines2D;
import ch.unifr.diva.dip.api.parameters.BooleanParameter;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.IntegerParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter.ViewHook;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.openimaj.utils.OpenIMAJUtils;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import org.openimaj.image.FImage;
import org.osgi.service.component.annotations.Component;

/**
 * OpenIMAJ's hough lines detector. A hough transform for lines.
 */
@Component(service = Processor.class)
public class HoughLines extends ProcessableBase {

	private final static String STORAGE_LINES_XML = "lines.xml";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_binary;
	private final InputPort<BufferedMatrix> input_float;
	private final XorInputPorts input_xor;
	private final OutputPort<Lines2D> output_lines;
	private final IntegerParameter maxLines;
	private final IntegerParameter numSegments;
	private final XorParameter angularRange;
	private final ExpParameter minTheta;
	private final ExpParameter maxTheta;
	private final BooleanParameter invertSamples;

	/**
	 * Creates a new hough lines detector.
	 */
	public HoughLines() {
		super("Hough Lines");

		this.maxLines = new IntegerParameter("Max. lines", 20, 1, Integer.MAX_VALUE);
		parameters.put("max-lines", maxLines);

		final TextParameter fullRange = new TextParameter("full range");
		final ViewHook<TextField> expHook = (t) -> {
			t.setMaxWidth(64);
		};
		final TextParameter minThetaLabel = new TextParameter("min: ");
		this.minTheta = new ExpParameter("Min. angle", "0");
		minTheta.addTextFieldViewHook(expHook);
		final TextParameter maxThetaLabel = new TextParameter(" max: ");
		this.maxTheta = new ExpParameter("Max. angle", "359");
		maxTheta.addTextFieldViewHook(expHook);
		final CompositeGrid angleGrid = new CompositeGrid(
				minThetaLabel,
				minTheta,
				maxThetaLabel,
				maxTheta
		);
		this.angularRange = new XorParameter(
				"Angular range",
				Arrays.asList(
						fullRange,
						angleGrid
				),
				0
		);
		parameters.put("angular-range", angularRange);

		/*
		 * the number of segments used in the accumulator space. By default this
		 * value is 360 (one accumulator bin per degree). However, if you require
		 * greater accuracy then this can be changed. It is suggested that it is
		 * a round multiple of 360.
		 */
		this.numSegments = new IntegerParameter("Num. segments", 360, 1, Integer.MAX_VALUE);
		parameters.put("num-segments", numSegments);

		this.invertSamples = new BooleanParameter("Invert", true);
		parameters.put("invert", invertSamples);

		final LabelParameter label = new LabelParameter(
				"The input image should have the lines to detect zeroed in the image "
				+ "(black). All other values will be ignored. That means you usually "
				+ "need to invert images created with edge detectors."
		);
		parameters.put("label", label);
		label.addLabelViewHook((c) -> {
			c.setMaxWidth(256);
			c.setWrapText(true);
		});

		this.input = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				false
		);
		this.input_binary = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary(),
				false
		);
		this.input_float = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageFloat(),
				false
		);
		this.input_xor = new XorInputPorts(this);
		input_xor.addPort("buffered-image", input);
		input_xor.addPort("buffered-image-binary", input_binary);
		input_xor.addPort("buffered-matrix-float", input_float);
		input_xor.enableAllPorts();

		this.output_lines = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.Lines2D()
		);
		this.outputs.put("hough-lines", output_lines);
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.REORDER_HORIZONTAL;
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new HoughLines();
	}

	@Override
	public void init(ProcessorContext context) {
		input_xor.init(context);

		if (context != null) {
			restoreOutputs(context);
		}
	}

	@Override
	public void process(ProcessorContext context) {
		final InputPort<?> port = input_xor.getEnabledPort();
		if (port == null) {
			log.warn("no input port enabled");
			return;
		}

		final FImage fimage;
		if (port.equals(input_float)) {
			final BufferedMatrix mat = input_float.getValue();
			fimage = OpenIMAJUtils.toFImage(mat, 0);
		} else if (port.equals(input_binary)) {
			final BufferedImage image = input_binary.getValue();
			fimage = OpenIMAJUtils.toFImage(image, 0);
		} else {
			final BufferedImage image = input.getValue();
			fimage = OpenIMAJUtils.toFImage(image, 0);
		}

		final int max = maxLines.get();
		final boolean invert = invertSamples.get();

		if (invert) {
			fimage.inverse();
		}

		final org.openimaj.image.analysis.algorithm.HoughLines hough = getHoughLines();
		fimage.analyseWith(hough);

		final Lines2D lines = new Lines2D();
		for (org.openimaj.math.geometry.line.Line2d line : getBestLines(hough, max)) {
			lines.add(new Line2D(
					line.begin.getX(),
					line.begin.getY(),
					line.end.getX(),
					line.end.getY()
			));
		}

		writeObject(context, lines, STORAGE_LINES_XML);
		context.getObjects().put("width", fimage.width);
		context.getObjects().put("height", fimage.height);
		setOutputs(context, lines);
	}

	protected List<org.openimaj.math.geometry.line.Line2d> getBestLines(org.openimaj.image.analysis.algorithm.HoughLines hough, int max) {
		final int ar = angularRange.get().selection;
		if (ar == 1) {
			// specified/limited range
			final float minT = minTheta.getFloat();
			final float maxT = maxTheta.getFloat();
			return hough.getBestLines(max, minT, maxT);
		}

		// full range
		return hough.getBestLines(max);
	}

	protected org.openimaj.image.analysis.algorithm.HoughLines getHoughLines() {
		final int n = numSegments.get();
		if ((n % 360) != 0) {
			log.warn(
					"The number of segments used in the accumulator space of the Hough Transform "
					+ "should be a round multiple of 360. Given: {}",
					n
			);
		}
		return new org.openimaj.image.analysis.algorithm.HoughLines(n, 0.0f);
	}

	protected void restoreOutputs(ProcessorContext context) {
		final Lines2D lines = readObject(
				context,
				STORAGE_LINES_XML,
				Lines2D.class
		);
		setOutputs(context, lines);
	}

	protected void setOutputs(ProcessorContext context, Lines2D lines) {
		output_lines.setOutput(lines);
		if (lines != null) {
			int i = 1;
			for (Line2D line : lines) {
				final EditorLayerPane layer = context.getLayer().newLayerPane(
						String.format("line %d", i++)
				);
				final double maxX = getBound(context, "width") - 0.5;
				final double maxY = getBound(context, "height") - 0.5;
				final Line fxline = clippedLine(line, maxX, maxY);
				fxline.setStroke(Color.ORANGE);
				fxline.setStrokeLineCap(StrokeLineCap.BUTT);
				layer.add(fxline);
			}
			context.getLayer().reverseChildren();
		}
	}

	protected int getBound(ProcessorContext context, String key) {
		if (!context.getObjects().containsKey(key)) {
			return Integer.MAX_VALUE;
		}
		return (int) context.getObjects().get(key);
	}

	protected Line clippedLine(Line2D line, double maxX, double maxY) {
		return new Line(
				clipValue(line.start.x, maxX),
				clipValue(line.start.y, maxY),
				clipValue(line.end.x, maxX),
				clipValue(line.end.y, maxY)
		);
	}

	protected double clipValue(double value, double max) {
		if (value < 0.5) {
			return 0.5;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_LINES_XML);
		resetOutputs();
		resetLayer(context);
		resetObjects(context);
	}

}
