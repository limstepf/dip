package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.ProcessorDocumentation;
import ch.unifr.diva.dip.api.components.SimpleProcessorDocumentation;
import ch.unifr.diva.dip.api.components.XorInputPortGroup;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.ui.StructuredText;
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
 * OpenIMAJ's canny edge detector.
 */
@Component(service = Processor.class)
public class CannyEdgeDetector extends ProcessableBase implements Previewable {

	private final static String STORAGE_MAT = "canny.bmat";
	private final static String STORAGE_IMAGE = "canny.png";
	private final static String STORAGE_IMAGE_FORMAT = "PNG";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_gray;
	private final InputPort<BufferedMatrix> input_float;
	private final InputPort<BufferedMatrix> input_dx;
	private final InputPort<BufferedMatrix> input_dy;
	private final XorInputPortGroup input_xor;
	private final XorInputPortGroup.PortGroup<InputPort<?>> pg_image;
	private final XorInputPortGroup.PortGroup<InputPort<?>> pg_sobel;
	private final OutputPort<BufferedImage> output;
	private final OutputPort<BufferedImage> output_binary;
	private final OutputPort<BufferedMatrix> output_float;
	private final IntegerSliderParameter band;
	private final CannyEdgeParameters cannyParameters;

	/**
	 * Creates a new canny edge detector.
	 */
	public CannyEdgeDetector() {
		super("Canny Edge Detector");

		this.band = new IntegerSliderParameter("Band", 1, 1, 4);
		band.addSliderViewHook((s) -> {
			s.disableProperty().bind(disableBandSelectionProperty);
		});
		parameters.put("band", band);

		this.cannyParameters = new CannyEdgeParameters();
		cannyParameters.put(parameters);

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
		this.input_dx = new InputPort<>(
				"dx",
				new ch.unifr.diva.dip.api.datatypes.BufferedImageFloat(),
				true
		);
		this.input_dy = new InputPort<>(
				"dy",
				new ch.unifr.diva.dip.api.datatypes.BufferedImageFloat(),
				true
		);
		this.input_xor = new XorInputPortGroup(this);

		this.pg_image = new XorInputPortGroup.PortGroup<>(true);
		pg_image.addPort("buffered-image", input);
		pg_image.addPort("buffered-image-gray", input_gray);
		pg_image.addPort("buffered-image-float", input_float);
		this.pg_sobel = new XorInputPortGroup.PortGroup<>();
		pg_sobel.addPort("dx-float", input_dx);
		pg_sobel.addPort("dy-float", input_dy);
		input_xor.addGroup(pg_image);
		input_xor.addGroup(pg_sobel);
		input_xor.enableAllGroups();

		this.output = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage()
		);
		this.output_binary = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary()
		);
		this.output_float = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageFloat()
		);
		outputs.put("buffered-image", output);
		outputs.put("buffered-image-binary", output_binary);
		outputs.put("buffered-matrix-float", output_float);
	}

	@Override
	public ProcessorDocumentation processorDocumentation() {
		final SimpleProcessorDocumentation doc = new SimpleProcessorDocumentation();
		doc.addTextFlow(
				"OpenIMAJ's Canny edge detector. Performs the following steps:"
		);
		doc.addStructuredText(StructuredText.orderedList(Arrays.asList(
				"Gaussian blur with std.dev. sigma",
				"Horizontal and vertical edge detection with Sobel operators",
				"Non-maximum suppression",
				"Hysteresis thresholding"
		)));
		doc.addTextFlow(
				"The upper and lower thresholds for the hysteresis thresholding "
				+ "can be specified manually or automatically chosen based on the "
				+ "histogram of the edge magnitudes."
		);
		return doc;
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.FINGERPRINT;
	}

	private final BooleanProperty disableBandSelectionProperty = new SimpleBooleanProperty();
	private final InvalidationListener grayPortListener = (e) -> onGrayPortChanged();

	private void onGrayPortChanged() {
		disableBandSelectionProperty.set(input_gray.isConnected());
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new CannyEdgeDetector();
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
	public void process(ProcessorContext context) {
		final XorInputPortGroup.PortGroup<InputPort<?>> group = input_xor.getEnabledGroup();
		if (group == null) {
			log.warn("no input port group enabled");
			return;
		}

		final org.openimaj.image.processing.edges.CannyEdgeDetector canny = cannyParameters.getCannyEdgeDetector();
		final FImage canny_out;

		if (group.equals(pg_sobel)) {
			final BufferedMatrix mat_dx = input_dx.getValue();
			final BufferedMatrix mat_dy = input_dy.getValue();
			final FImage fdx = OpenIMAJUtils.toFImage(mat_dx, 0);
			final FImage fdy = OpenIMAJUtils.toFImage(mat_dy, 0);
			canny_out = new FImage(fdx.width, fdx.height);
			canny.processImage(canny_out, fdx, fdy);
		} else {
			final InputPort<?> port = group.getConnection();
			final FImage fimage;
			if (port.equals(input_float)) {
				final BufferedMatrix mat = input_float.getValue();
				fimage = OpenIMAJUtils.toFImage(mat, getBand(mat));
			} else if (port.equals(input_gray)) {
				final BufferedImage image = input_gray.getValue();
				fimage = OpenIMAJUtils.toFImage(image, 0);
			} else {
				final BufferedImage image = input.getValue();
				if (image instanceof BufferedMatrix) {
					final BufferedMatrix mat = (BufferedMatrix) image;
					fimage = OpenIMAJUtils.toFImage(mat, getBand(mat));
				} else {
					fimage = OpenIMAJUtils.toFImage(image, getBand(image));
				}
			}
			canny.processImage(fimage);
			canny_out = fimage;
		}

		final BufferedImage canny_image = OpenIMAJUtils.toBinaryBufferedImage(canny_out);
		writeBufferedImage(context, canny_image, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT);
		final BufferedMatrix canny_mat;
		if (output_float.isConnected()) {
			canny_mat = OpenIMAJUtils.toBufferedMatrix(canny_out);
			writeBufferedMatrix(context, canny_mat, STORAGE_MAT);
		} else {
			canny_mat = null;
		}

		setOutputs(context, canny_image, canny_mat);
	}

	@Override
	public Image previewSource(ProcessorContext context) {
		final XorInputPortGroup.PortGroup<InputPort<?>> group = input_xor.getEnabledGroup();
		if (group == null) {
			log.warn("no input port group enabled");
			return null;
		}
		if (group.equals(pg_sobel)) {
			return null;
		}
		final InputPort<?> port = group.getConnection();
		if (port.equals(input_float) || (input.getValue() instanceof BufferedMatrix)) {
			return null;
		}
		if (port.equals(input_gray)) {
			final BufferedImage image = input_gray.getValue();
			return SwingFXUtils.toFXImage(image, null);
		}
		final BufferedImage image = input.getValue();
		return SwingFXUtils.toFXImage(image, null);
	}

	@Override
	public Image preview(ProcessorContext context, Rectangle bounds) {
		final XorInputPortGroup.PortGroup<InputPort<?>> group = input_xor.getEnabledGroup();
		if (group == null) {
			log.warn("no input port group enabled");
			return null;
		}

		final org.openimaj.image.processing.edges.CannyEdgeDetector canny = cannyParameters.getCannyEdgeDetector();
		final FImage canny_out;

		/*
		 * if thresholds are choosen automatically, the result will differ depending
		 * on the given bounds/subimage, so technically, we maybe should process
		 * the whole thing anyways in this case?!
		 */
		if (group.equals(pg_sobel)) {
			final BufferedImage mat_dx = getSubimage(input_dx.getValue(), bounds);
			final BufferedImage mat_dy = getSubimage(input_dy.getValue(), bounds);
			final FImage fdx = OpenIMAJUtils.toFImage(mat_dx, 0);
			final FImage fdy = OpenIMAJUtils.toFImage(mat_dy, 0);
			canny_out = new FImage(fdx.width, fdx.height);
			canny.processImage(canny_out, fdx, fdy);
		} else {
			final InputPort<?> port = group.getConnection();
			final FImage fimage;
			if (port.equals(input_float)) {
				final BufferedImage mat = getSubimage(input_float.getValue(), bounds);
				fimage = OpenIMAJUtils.toFImage(mat, getBand(mat));
			} else if (port.equals(input_gray)) {
				final BufferedImage image = getSubimage(input_gray.getValue(), bounds);
				fimage = OpenIMAJUtils.toFImage(image, 0);
			} else {
				final BufferedImage image = getSubimage(input.getValue(), bounds);
				if (image instanceof BufferedMatrix) {
					final BufferedImage mat = (BufferedMatrix) image;
					fimage = OpenIMAJUtils.toFImage(mat, getBand(mat));
				} else {
					fimage = OpenIMAJUtils.toFImage(image, getBand(image));
				}
			}
			canny.processImage(fimage);
			canny_out = fimage;
		}
		final BufferedImage canny_image = OpenIMAJUtils.toBinaryBufferedImage(canny_out);
		return SwingFXUtils.toFXImage(canny_image, null);
	}

	private <T extends BufferedImage> BufferedImage getSubimage(T image, Rectangle bounds) {
		return image.getSubimage(
				bounds.x,
				bounds.y,
				bounds.width,
				bounds.height
		);
	}

	private <T extends BufferedImage> int getBand(T image) {
		final XorInputPortGroup.PortGroup<InputPort<?>> group = input_xor.getEnabledGroup();
		if (group == null) {
			return 0;
		}
		final InputPort<?> port = group.getConnection();
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
		if (image != null) {
			provideImageLayer(context, image);
		}
		output.setOutput(image);
		output_binary.setOutput(image);
		output_float.setOutput(mat);
	}

	@Override
	public boolean isReady() {
		if (!output.getPortState().equals(Port.State.READY)) {
			return false;
		}
		if (!output_binary.getPortState().equals(Port.State.READY)) {
			return false;
		}
		// consider out_float only if connected
		if (output_float.isConnected() && !output_float.getPortState().equals(Port.State.READY)) {
			return false;
		}
		return true;
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_IMAGE);
		deleteFile(context, STORAGE_MAT);
		resetOutputs();
		resetLayer(context);
	}

}
