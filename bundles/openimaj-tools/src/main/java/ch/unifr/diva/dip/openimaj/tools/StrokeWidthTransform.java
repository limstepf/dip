package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.ProcessorDocumentation;
import ch.unifr.diva.dip.api.components.SimpleProcessorDocumentation;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.parameters.BooleanParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.openimaj.utils.OpenIMAJUtils;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.openimaj.image.FImage;
import org.osgi.service.component.annotations.Component;

/**
 * OpenIMAJ's Stroke Width Transform.
 */
@Component(service = Processor.class)
public class StrokeWidthTransform extends ProcessableBase implements Previewable {

	private final static String STORAGE_MAT = "swt.bmat";
	private final static String STORAGE_IMAGE = "swt.png";
	private final static String STORAGE_IMAGE_FORMAT = "PNG";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_gray;
	private final InputPort<BufferedMatrix> input_float;
	private final XorInputPorts input_xor;

	private final OutputPort<BufferedImage> output;
	private final OutputPort<BufferedImage> output_gray;
	private final OutputPort<BufferedMatrix> output_float;

	private final IntegerSliderParameter band;
	private final CannyEdgeParameters cannyParameters;
	private final BooleanParameter direction;
	private final XorParameter normalization;

	/**
	 * Creates a new Stroke Width Transform.
	 */
	public StrokeWidthTransform() {
		super("Stroke Width Transform");

		this.band = new IntegerSliderParameter("Band", 1, 1, 4);
		band.addSliderViewHook((s) -> {
			s.disableProperty().bind(disableBandSelectionProperty);
		});
		parameters.put("band", band);

		this.cannyParameters = new CannyEdgeParameters();
		cannyParameters.putAsSub(parameters);

		this.direction = new BooleanParameter("Direction", true, "dark on light", "light on dark");
		parameters.put("direction", direction);

		final TextParameter nonormalize = new TextParameter("no normalization");
		final TextParameter normalize1 = new TextParameter("normalize to [0, 1]");
		final TextParameter normalize255 = new TextParameter("normalize to [0, 255]");

		this.normalization = new XorParameter(
				"Normalization",
				Arrays.asList(
						nonormalize,
						normalize1,
						normalize255
				),
				0
		);
		parameters.put("normalization", normalization);

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

		this.output = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage()
		);
		this.output_gray = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageGray()
		);
		this.output_float = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageFloat()
		);

		enableOutputs(false);
	}

	@Override
	public ProcessorDocumentation processorDocumentation() {
		final SimpleProcessorDocumentation doc = new SimpleProcessorDocumentation();
		doc.addTextFlow(
				"OpenIMAJ's implementation of the Stroke Width Transform."
				+ "The Stroke Width Transform detects strokes and their respective "
				+ "widths from an image. This implementation contains a number of "
				+ "enhancements to improve the quality of the detected strokes, "
				+ "based on ideas from LibCCV implementation:"
		);
		doc.addUnorderedList(Arrays.asList(
				"We search around the stroke in a small window for endpoints.",
				"We search around the endpoint in a small window for matching gradients.",
				"In addition to the stroke along the gradient, we also stroke at +/-45 degrees from this."
		));
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
		return new StrokeWidthTransform();
	}

	private final BooleanProperty disableBandSelectionProperty = new SimpleBooleanProperty();
	private final InvalidationListener grayPortListener = (e) -> onGrayPortChanged();

	private void onGrayPortChanged() {
		disableBandSelectionProperty.set(input_gray.isConnected());
	}

	private final InvalidationListener normalizationListener = (e) -> onNormalizationChanged();

	private void onNormalizationChanged() {
		enableOutputs(isForceFloat());
	}

	private boolean isForceFloat() {
		return normalization.getSelectedIndex() != 2;
	}

	private void enableOutputs(boolean forceFloat) {
		outputs.clear();
		outputs.put("buffered-image", output);
		if (!forceFloat) {
			outputs.put("buffered-image-gray", output_gray);
		}
		outputs.put("buffered-image-float", output_float);
		repaint();
	}

	@Override
	public void init(ProcessorContext context) {
		input_xor.init(context);
		input_gray.portStateProperty().addListener(grayPortListener);
		onGrayPortChanged();
		normalization.property().addListener(normalizationListener);
		onNormalizationChanged();

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
		FImage fimage = getSourceFImage(getSourceImage());
		if (fimage == null) {
			return;
		}

		fimage = doSWT(fimage);

		BufferedImage swt_image = null;
		BufferedMatrix swt_mat = null;

		final boolean isForceFloat = isForceFloat();
		if (isForceFloat || output_float.isConnected()) {
			swt_mat = OpenIMAJUtils.toBufferedMatrix(fimage);
			writeBufferedMatrix(context, swt_mat, STORAGE_MAT);
		}
		if (!isForceFloat) {
			swt_image = OpenIMAJUtils.toBufferedImage(fimage);
			writeBufferedImage(context, swt_image, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT);
		}

		setOutputs(context, swt_image, swt_mat);
	}

	protected FImage doSWT(FImage fimage) {
		final boolean swt_dir = direction.get();
		float canny_sigma = cannyParameters.getSigma();
		if (!Float.isFinite(canny_sigma) || canny_sigma < 0) {
			log.warn("invalid sigma: {}. Sigma is reset to 1.0f.", canny_sigma);
			canny_sigma = 1.0f;
		}
		boolean canny_auto = cannyParameters.isAutoThresholds();
		float canny_low = 0;
		float canny_high = 0;
		if (!canny_auto) {
			canny_low = cannyParameters.getThresholdLow();
			canny_high = cannyParameters.getThresholdHigh();
			if (canny_low < 0.0f || canny_low >= canny_high || canny_high > 1.0f) {
				log.warn(
						"invalid thresholds: low={}, hight={} must be in range [0, 1], and low < high."
						+ " Chosing thresholds automaticlly.",
						canny_low,
						canny_high
				);
				canny_auto = true;
			}
		}

		final org.openimaj.image.processing.edges.StrokeWidthTransform swt;
		if (canny_auto) {
			swt = new org.openimaj.image.processing.edges.StrokeWidthTransform(
					swt_dir, canny_sigma
			);
		} else {
			swt = new org.openimaj.image.processing.edges.StrokeWidthTransform(
					swt_dir, canny_low, canny_high, canny_sigma
			);
		}
		swt.processImage(fimage);

		switch (normalization.getSelectedIndex()) {
			case 0: // nonormalize
				return fimage; // no-op
			case 1: // normalize1
				return normalize(fimage, false);
			case 2: // normalize255
			default:
				return normalize(fimage, true);
		}
	}

	protected FImage normalize(FImage fimage, boolean to255) {
		final FImage normalized = org.openimaj.image.processing.edges.StrokeWidthTransform.normaliseImage(
				fimage
		);
		if (to255) {
			fimage.multiplyInplace(255.0f);
		}
		return fimage;
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
			b = 0;
		}
		return b - 1;
	}

	protected void restoreOutputs(ProcessorContext context) {
		final BufferedImage image = readBufferedImage(context, STORAGE_IMAGE);
		final BufferedMatrix mat = readBufferedMatrix(context, STORAGE_MAT);
		setOutputs(context, image, mat);
	}

	protected void setOutputs(ProcessorContext context, BufferedImage image, BufferedMatrix mat) {
		if (image == null) {
			output.setOutput(mat);
			output_gray.setOutput(null);
			output_float.setOutput(mat);
		} else {
			provideImageLayer(context, image);
			output.setOutput(image);
			output_gray.setOutput(image);
			output_float.setOutput(null);
		}
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_IMAGE);
		deleteFile(context, STORAGE_MAT);
		resetOutputs();
		resetLayer(context);
	}

	@Override
	public Image previewSource(ProcessorContext context) {
		if (isForceFloat()) {
			return null;
		}
		final BufferedImage source = getSourceImage();
		if (source instanceof BufferedMatrix) {
			return null;
		}
		return SwingFXUtils.toFXImage(getSourceImage(), null);
	}

	@Override
	public Image preview(ProcessorContext context, Rectangle bounds) {
		if (isForceFloat()) {
			return null;
		}
		final BufferedImage source = getSourceImage();
		if (source instanceof BufferedMatrix) {
			return null;
		}
		final BufferedImage image = source.getSubimage(
				bounds.x,
				bounds.y,
				bounds.width,
				bounds.height
		);
		FImage fimage = OpenIMAJUtils.toFImage(image, getBand(image));
		fimage = doSWT(fimage);
		final BufferedImage preview = OpenIMAJUtils.toBufferedImage(fimage);
		return SwingFXUtils.toFXImage(preview, null);
	}

}
