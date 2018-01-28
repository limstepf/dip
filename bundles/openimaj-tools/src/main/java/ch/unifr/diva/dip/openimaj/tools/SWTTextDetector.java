package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.ProcessorDocumentation;
import ch.unifr.diva.dip.api.components.SimpleProcessorDocumentation;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.datastructures.NestedRectangle2D;
import ch.unifr.diva.dip.api.datastructures.NestedRectangles2D;
import ch.unifr.diva.dip.api.parameters.BooleanParameter;
import ch.unifr.diva.dip.api.parameters.ButtonToggleGroupParameter;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.IntegerParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.openimaj.utils.OpenIMAJUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import org.openimaj.image.FImage;
import org.openimaj.image.text.extraction.swt.LetterCandidate;
import org.openimaj.image.text.extraction.swt.LineCandidate;
import org.openimaj.image.text.extraction.swt.WordCandidate;
import org.osgi.service.component.annotations.Component;

/**
 * OpenIMAJ's Stroke Width Transform Text Detector. Implementation of the Stroke
 * Width Transform text detection algorithm by Epshtein et al.
 */
@Component(service = Processor.class)
public class SWTTextDetector extends ProcessableBase {

	private final static String STORAGE_LINES_XML = "lines.xml";
	private final static String STORAGE_LETTERS_XML = "letters.xml";
	private final static String STORAGE_IMAGE_FORMAT = "png";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_gray;
	private final InputPort<BufferedMatrix> input_float;
	private final XorInputPorts input_xor;

	private final OutputPort<NestedRectangles2D> output_lines;
	private final OutputPort<NestedRectangles2D> output_letters;

	private final IntegerSliderParameter band;
	private final CannyEdgeParameters cannyParameters;
	private final ButtonToggleGroupParameter<String> direction;
	private final BooleanParameter doubleSize;

	private final ExpParameter strokeWidthRatio;
	private final ExpParameter letterVarianceMean;
	private final ExpParameter maxAspectRatio;
	private final ExpParameter maxDiameterStrokeRatio;
	private final IntegerParameter minArea;
	private final ExpParameter minHeight;
	private final ExpParameter maxHeight;
	private final IntegerParameter maxNumOverlappingBoxes;
	private final IntegerParameter maxStrokeWidth;
	private final ExpParameter medianStrokeWidthRatio;
	private final ExpParameter letterHeightRatio;
	private final ExpParameter intensityThreshold;
	private final ExpParameter widthMultiplier;
	private final IntegerParameter minLettersPerLine;
	private final ExpParameter intersectRatio;
	private final ExpParameter wordBreakdownRatio;

	public SWTTextDetector() {
		super("SWT Text Detector");

		this.band = new IntegerSliderParameter("Band", 1, 1, 4);
		band.addSliderViewHook((s) -> {
			s.disableProperty().bind(disableBandSelectionProperty);
		});
		parameters.put("band", band);

		this.cannyParameters = new CannyEdgeParameters();
		cannyParameters.putAsSub(parameters);

		this.direction = ButtonToggleGroupParameter.newInstance("Direction", "dol");
		direction.add("dark on light", "dol");
		direction.add("light on dark", "lod");
		direction.add("both", "both");
		parameters.put("swt-direction", direction);

		this.doubleSize = new BooleanParameter("Double Size", false);
		doubleSize.addToggleButtonViewHook((t) -> {
			t.setTooltip(new Tooltip(
					"Upscale the image to double size before applying the SWT."
			));
		});
		parameters.put("swt-double-size", doubleSize);

		this.strokeWidthRatio = newExpParameter(
				"Stroke Width Ratio",
				"Maximum allowed ratio of a pair of stroke widths for them to "
				+ "be considered part of the same connected component.",
				"3.0"
		);
		this.letterVarianceMean = newExpParameter(
				"Letter Variance Mean",
				"Maximum allowed variance of stroke width in a single "
				+ "character as a percentage of the mean.",
				"0.93"
		);
		this.maxAspectRatio = newExpParameter(
				"Max. Aspect Ratio",
				"Maximum allowed aspect ratio for a single letter.",
				"10"
		);
		this.maxDiameterStrokeRatio = newExpParameter(
				"Max. Diameter Stroke Ratio",
				"Maximum allowed ratio of diameter to stroke width for a "
				+ "single character.",
				"10"
		);
		this.minArea = newIntegerParameter(
				"Min. Area",
				"Minimum allowed component size; used to quickly filter "
				+ "out small components.",
				38
		);
		this.minHeight = newExpParameter(
				"Min. Height",
				"Minimum character height.",
				"10"
		);
		this.maxHeight = newExpParameter(
				"Max. Height",
				"Maximum character height.",
				"300"
		);
		this.maxNumOverlappingBoxes = newIntegerParameter(
				"Max. Num. Overlapping Boxes",
				"Maximum allowed number of overlapping characters.",
				10
		);
		this.maxStrokeWidth = newIntegerParameter(
				"Max. Stroke Width",
				"Maximum allowed stroke width",
				70
		);
		this.medianStrokeWidthRatio = newExpParameter(
				"Median Stroke Width Ratio",
				"Maximum ratio of stroke width for two letters to be "
				+ "considered to be related.",
				"2"
		);
		this.letterHeightRatio = newExpParameter(
				"Letter Height Ratio",
				"Maximum ratio of height for two letters to be considered "
				+ "to be related.",
				"2"
		);
		this.intensityThreshold = newExpParameter(
				"Intensity Threshold",
				"Maximum difference in intensity for two letters to be "
				+ "considered to be related.",
				"5.0" // was 0.12, but that's way too low from what I can tell...
		);
		this.widthMultiplier = newExpParameter(
				"Width Multiplier",
				"The width multiplier for two letters to be considered to "
				+ "be related. Distance between centroids must be less than "
				+ "widthMultiplier * maxLetterWidth.",
				"3"
		);
		this.minLettersPerLine = newIntegerParameter(
				"Min. Letters Per Line",
				"Minimum number of allowed letters on a line.",
				3
		);
		this.intersectRatio = newExpParameter(
				"Intersect Ratio",
				"Ratio of vertical intersection for character pairing. "
				+ "This helps ensure that the characters are horizontal.",
				"1.3"
		);
		this.wordBreakdownRatio = newExpParameter(
				"Word Breakdown Ratio",
				"Ratio of the interclass std dev of the letter spacings to "
				+ "the mean to suggest a word break.",
				"1"
		);

		final List<PersistentParameter<?>> options = Arrays.asList(
				strokeWidthRatio,
				letterVarianceMean,
				maxAspectRatio,
				maxDiameterStrokeRatio,
				minArea,
				minHeight,
				maxHeight,
				maxNumOverlappingBoxes,
				maxStrokeWidth,
				medianStrokeWidthRatio,
				letterHeightRatio,
				intensityThreshold,
				widthMultiplier,
				minLettersPerLine,
				intersectRatio,
				wordBreakdownRatio
		);

		final List<Parameter<?>> labelsAndOptions = new ArrayList<>();
		final int numCols = 4;
		final double colPercentWidth = 100.0 / (double) numCols;

		for (int i = 0; i < options.size(); i = i + numCols) {
			for (int j = 0; j < numCols; j++) {
				final PersistentParameter<?> p = options.get(i + j);
				labelsAndOptions.add(new LabelParameter(p.label()));
			}
			for (int j = 0; j < numCols; j++) {
				final PersistentParameter<?> p = options.get(i + j);
				labelsAndOptions.add(p);
			}
		}

		final CompositeGrid optionsGrid = new CompositeGrid(
				"Options",
				labelsAndOptions
		);
		optionsGrid.setColumnConstraints(numCols);
		for (ColumnConstraints cc : optionsGrid.getColumnConstraints()) {
			cc.setPercentWidth(colPercentWidth);
		}
		parameters.put("text-detector-options", optionsGrid);

		this.input = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				false
		);
		this.input_gray = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageGray(),
				false
		);
		this.input_float = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageFloat(),
				false
		);
		this.input_xor = new XorInputPorts(this);
		input_xor.addPort("buffered-image", input);
		input_xor.addPort("buffered-image-gray", input_gray);
		input_xor.addPort("buffered-image-float", input_float);
		input_xor.enableAllPorts();

		this.output_lines = new OutputPort<>(
				"lines",
				new ch.unifr.diva.dip.api.datatypes.NestedRectangles2D()
		);
		this.output_letters = new OutputPort<>(
				"letters",
				new ch.unifr.diva.dip.api.datatypes.NestedRectangles2D()
		);
		outputs.put("lines", output_lines);
		outputs.put("letters", output_letters);
	}

	private static IntegerParameter newIntegerParameter(String label, String tooltip, int value) {
		final IntegerParameter parameter = new IntegerParameter(label, value, 0, Integer.MAX_VALUE);
		parameter.addTextFieldViewHook((t) -> {
			t.setMaxWidth(96);
			t.setTooltip(new Tooltip(tooltip));
		});
		return parameter;
	}

	private static ExpParameter newExpParameter(String label, String tooltip, String value) {
		final ExpParameter parameter = new ExpParameter(label, value);
		parameter.addTextFieldViewHook((t) -> {
			t.setMaxWidth(96);
			t.setTooltip(new Tooltip(tooltip));
		});
		return parameter;
	}

	@Override
	public ProcessorDocumentation processorDocumentation() {
		final SimpleProcessorDocumentation doc = new SimpleProcessorDocumentation();
		doc.addTextFlow(
				"OpenIMAJ's implementation of the Stroke Width Transform text "
				+ "detection algorithm by Epshtein et al. This is a (relatively) "
				+ "high-performance text detection technique that does not require "
				+ "training (except for parameter setting) and is language "
				+ "independent. The algorithm automatically identifies individual "
				+ "characters (\"letters\"), as well as performing word grouping "
				+ "and line segmentation.\n"
				+ "There is an implicit assumption in this implementation that "
				+ "the text is *almost* horizontal. This implementation cannot "
				+ "be considered to be rotation invariant. It also has "
				+ "difficulties with curved text."
		);
		doc.addTextFlow(
				"See: B. Epshtein, E. Ofek and Y. Wexler. Detecting text in natural "
				+ "scenes with stroke width transform. Computer Vision and Pattern "
				+ "Recognition (CVPR), 2010 IEEE Conference on. pp2963-2970. 2010."
		);
		return doc;
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.NOTE_TEXT;
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new SWTTextDetector();
	}

	private final BooleanProperty disableBandSelectionProperty = new SimpleBooleanProperty();
	private final InvalidationListener grayPortListener = (e) -> onGrayPortChanged();

	private void onGrayPortChanged() {
		disableBandSelectionProperty.set(input_gray.isConnected());
	}

	@Override
	public void init(ProcessorContext context) {
		input_xor.init(context);
		input_gray.portStateProperty().addListener(grayPortListener);
		onGrayPortChanged();

		if (context != null) {
			restoreOutputs(context);
		}
	}

	@Override
	public boolean isConnected() {
		return input_xor.isConnected();
	}

	@Override
	public void process(ProcessorContext context) {
		try {
			final FImage fimage = getSourceFImage(getSourceImage());
			if (fimage == null) {
				return;
			}

			final org.openimaj.image.text.extraction.swt.SWTTextDetector detector = doTextDetection(fimage);
			cancelIfInterrupted();
			final NestedRectangles2D lines = new NestedRectangles2D();

			log.info(
					"SWTTextDetector detected {} line(s) and {} letter(s).",
					detector.getLines().size(),
					detector.getLetters().size()
			);

			for (LineCandidate line : detector.getLines()) {
				cancelIfInterrupted();
				final NestedRectangle2D lineRect = new NestedRectangle2D(
						line.getRegularBoundingBox().x,
						line.getRegularBoundingBox().y,
						line.getRegularBoundingBox().width,
						line.getRegularBoundingBox().height
				);
				for (WordCandidate word : line.getWords()) {
					cancelIfInterrupted();
					final NestedRectangle2D wordRect = new NestedRectangle2D(
							word.getRegularBoundingBox().x,
							word.getRegularBoundingBox().y,
							word.getRegularBoundingBox().width,
							word.getRegularBoundingBox().height
					);
					for (LetterCandidate letter : word.getLetters()) {
						final NestedRectangle2D letterRect = new NestedRectangle2D(
								letter.getRegularBoundingBox().x,
								letter.getRegularBoundingBox().y,
								letter.getRegularBoundingBox().width,
								letter.getRegularBoundingBox().height
						);
						wordRect.getChildren().add(letterRect);
					}
					lineRect.getChildren().add(wordRect);
				}
				lines.add(lineRect);
			}
			cancelIfInterrupted();

			final NestedRectangles2D letters = new NestedRectangles2D();
			for (LetterCandidate letter : detector.getLetters()) {
				final NestedRectangle2D letterRect = new NestedRectangle2D(
						letter.getRegularBoundingBox().x,
						letter.getRegularBoundingBox().y,
						letter.getRegularBoundingBox().width,
						letter.getRegularBoundingBox().height
				);
				letters.add(letterRect);
			}
			cancelIfInterrupted();

			writeObject(context, lines, STORAGE_LINES_XML);
			writeObject(context, letters, STORAGE_LETTERS_XML);

			setOutputs(context, lines, letters);
			cancelIfInterrupted();
		} catch (InterruptedException ex) {
			reset(context);
		}
	}

	protected org.openimaj.image.text.extraction.swt.SWTTextDetector doTextDetection(FImage fimage) {
		final org.openimaj.image.text.extraction.swt.SWTTextDetector detector = new org.openimaj.image.text.extraction.swt.SWTTextDetector();
		final org.openimaj.image.text.extraction.swt.SWTTextDetector.Options options = detector.getOptions();

		options.canny = cannyParameters.getCannyEdgeDetector();
		options.direction = getDirection();
		options.doubleSize = doubleSize.get();
		options.strokeWidthRatio = strokeWidthRatio.getFloat();
		options.letterVarianceMean = letterVarianceMean.getDouble();
		options.maxAspectRatio = maxAspectRatio.getDouble();
		options.maxDiameterStrokeRatio = maxDiameterStrokeRatio.getDouble();
		options.minArea = minArea.get();
		options.minHeight = minHeight.getFloat();
		options.maxHeight = maxHeight.getFloat();
		options.maxNumOverlappingBoxes = maxNumOverlappingBoxes.get();
		options.maxStrokeWidth = maxStrokeWidth.get();
		options.medianStrokeWidthRatio = medianStrokeWidthRatio.getFloat();
		options.letterHeightRatio = letterHeightRatio.getFloat();
		options.intensityThreshold = intensityThreshold.getFloat();
		options.widthMultiplier = widthMultiplier.getFloat();
		options.minLettersPerLine = minLettersPerLine.get();
		options.intersectRatio = intersectRatio.getFloat();
		options.wordBreakdownRatio = wordBreakdownRatio.getFloat();

		detector.analyseImage(fimage);
		return detector;
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

		public void drawRect(NestedRectangle2D rect, boolean doubledSize) {
			if (doubledSize) {
				g.drawRect(
						(int) (rect.getX() * 0.5),
						(int) (rect.getY() * 0.5),
						(int) Math.round(rect.getWidth() * 0.5),
						(int) Math.round(rect.getHeight() * 0.5)
				);
			} else {
				g.drawRect(
						(int) rect.getX(),
						(int) rect.getY(),
						(int) Math.round(rect.getWidth()),
						(int) Math.round(rect.getHeight())
				);
			}
		}

		public void dispose() {
			if (g == null) {
				return;
			}
			g.dispose();
		}

	}

	protected List<RenderLayer> renderLayers(BufferedImage source, NestedRectangles2D lines, NestedRectangles2D letters) {
		final boolean doubledSize = this.doubleSize.get();
		final RenderLayer linesLayer;
		final RenderLayer wordsLayer;
		if (lines == null || lines.isEmpty()) {
			linesLayer = new RenderLayer("Lines (empty)");
			wordsLayer = new RenderLayer("Words (empty)");
		} else {
			linesLayer = new RenderLayer("Lines", source);
			linesLayer.g.setColor(Color.MAGENTA);
			wordsLayer = new RenderLayer("Words", source);
			wordsLayer.g.setColor(Color.YELLOW);
			for (NestedRectangle2D line : lines) {
				linesLayer.drawRect(line, doubledSize);
				for (NestedRectangle2D word : line.getChildren()) {
					wordsLayer.drawRect(word, doubledSize);
				}
			}
			linesLayer.dispose();
			wordsLayer.dispose();
		}

		final RenderLayer lettersLayer;
		if (letters == null || letters.isEmpty()) {
			lettersLayer = new RenderLayer("Letters (empty)");
		} else {
			lettersLayer = new RenderLayer("Letters", source);
			lettersLayer.g.setColor(Color.GREEN);
			for (NestedRectangle2D letter : letters) {
				lettersLayer.drawRect(letter, doubledSize);
			}
			lettersLayer.dispose();
		}

		return Arrays.asList(
				lettersLayer,
				wordsLayer,
				linesLayer
		);
	}

	protected org.openimaj.image.text.extraction.swt.SWTTextDetector.Direction getDirection() {
		final String dir = direction.get();
		if ("dol".equals(dir)) {
			return org.openimaj.image.text.extraction.swt.SWTTextDetector.Direction.DarkOnLight;
		} else if ("lod".equals(dir)) {
			return org.openimaj.image.text.extraction.swt.SWTTextDetector.Direction.LightOnDark;
		}
		return org.openimaj.image.text.extraction.swt.SWTTextDetector.Direction.Both;
	}

	protected BufferedImage getSourceImage() {
		final InputPort<?> port = input_xor.getEnabledPort();
		if (port == null) {
			log.warn("no input port enabled");
			return null;
		}

		if (port.equals(input_float)) {
			return input_float.getValue();
		} else if (port.equals(input_gray)) {
			return input_gray.getValue();
		} else {
			return input.getValue();
		}
	}

	protected FImage getSourceFImage(BufferedImage image) {
		if (image == null) {
			return null;
		}
		if (image instanceof BufferedMatrix) {
			final BufferedMatrix mat = (BufferedMatrix) image;
			return OpenIMAJUtils.toFImage(
					mat,
					Math.min(mat.getSampleModel().getNumBands(), getBand(mat))
			);
		}
		return OpenIMAJUtils.toFImage(
				image,
				Math.min(image.getSampleModel().getNumBands(), getBand(image))
		);
	}

	private <T extends BufferedImage> int getBand(T image) {
		final InputPort<?> port = input_xor.getEnabledPort();
		if (input_gray.equals(port)) {
			return 0;
		}
		final int n = image.getSampleModel().getNumBands();
		int b = band.get();
		if (b > n) {
			log.warn(
					"invalid band: {}. Image has only {} band(s). Selecting first band.",
					b,
					n
			);
			return 0;
		}
		return b - 1;
	}

	protected void restoreOutputs(ProcessorContext context) {
		final NestedRectangles2D lines = ProcessableBase.readObject(
				context,
				STORAGE_LINES_XML,
				NestedRectangles2D.class
		);
		final NestedRectangles2D letters = ProcessableBase.readObject(
				context,
				STORAGE_LETTERS_XML,
				NestedRectangles2D.class
		);
		setOutputs(context, lines, letters);
	}

	protected void setOutputs(ProcessorContext context, NestedRectangles2D lines, NestedRectangles2D letters) {
		output_lines.setOutput(lines);
		output_letters.setOutput(letters);

		if (lines != null && !lines.isEmpty()) {
			// try to read layers from disk first
			boolean fromDisk = false;
			String name;
			BufferedImage image;
			String key = "layer-0";
			int idx = 0;
			while (context.getObjects().containsKey(key)) {
				name = (String) context.getObjects().get(key);
				image = ProcessorBase.readBufferedImage(context, key + ".png");
				if (name != null) { // image may be null!
					if (image != null) {
						provideImageLayer(context, image, name);
					} else {
						context.getLayer().newLayerPane(name);
					}
					fromDisk = true;
				}
				key = String.format("layer-%d", ++idx);
			}

			// render them otherwise, and write to disk
			if (!fromDisk) {
				// TODO: this can be easily split up and run on the thread pool
				final List<RenderLayer> layers = renderLayers(getSourceImage(), lines, letters);
				for (int i = 0; i < layers.size(); i++) {
					final RenderLayer layer = layers.get(i);
					if (layer.image != null) {
						key = String.format("layer-%d", i);
						context.getObjects().put(key, layer.name);
						writeBufferedImage(context, layer.image, key + ".png", STORAGE_IMAGE_FORMAT);
						provideImageLayer(context, layer.image, layer.name);
					} else {
						context.getLayer().newLayerPane(layer.name);
					}
				}
			}
		}
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_LETTERS_XML);
		deleteFile(context, STORAGE_LINES_XML);

		String key = "layer-0";
		int idx = 0;
		while (context.getObjects().containsKey(key)) {
			deleteFile(context, key + ".png");
			key = String.format("layer-%d", ++idx);
		}

		resetOutputs();
		resetObjects(context);
		resetLayer(context);
	}

}
