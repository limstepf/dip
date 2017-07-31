package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.XorInputPortGroup;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter.ViewHook;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.openimaj.utils.OpenIMAJUtils;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TextField;
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
	private final ExpParameter sigma;
	private final XorParameter thresholds;
	private final ExpParameter threshold_low;
	private final ExpParameter threshold_high;

	/**
	 * Creates a new canny edge detector.
	 */
	public CannyEdgeDetector() {
		super("Canny Edge Detector");

		this.band = new IntegerSliderParameter("Band", 1, 1, 4);
		parameters.put("band", band);

		this.sigma = new ExpParameter("Sigma", "1.0");
		parameters.put("sigma", sigma);

		final ExpParameter.DoubleValidator expValidator = (v) -> {
			if (v < 0 || v > 1.0) {
				return Double.NaN;
			}
			return v;
		};
		final ViewHook<TextField> expHook = (t) -> {
			t.setMaxWidth(64);
		};
		final TextParameter auto = new TextParameter("automatic");
		final TextParameter lowLabel = new TextParameter("low: ");
		this.threshold_low = new ExpParameter("low", "0.3");
		threshold_low.addTextFieldViewHook(expHook);
		threshold_low.setDoubleValidator(expValidator);
		final TextParameter highLabel = new TextParameter(" high: ");
		this.threshold_high = new ExpParameter("high", "0.7");
		threshold_high.addTextFieldViewHook(expHook);
		threshold_high.setDoubleValidator(expValidator);
		final CompositeGrid grid = new CompositeGrid(
				lowLabel,
				threshold_low,
				highLabel,
				threshold_high
		);
		this.thresholds = new XorParameter("Thresholds", Arrays.asList(auto, grid));
		parameters.put("thresholds", thresholds);

		this.input = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				false
		);
		this.input_float = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat(),
				false
		);
		this.input_dx = new InputPort<>(
				"dx",
				new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat(),
				true
		);
		this.input_dy = new InputPort<>(
				"dy",
				new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat(),
				true
		);
		this.input_xor = new XorInputPortGroup(this);

		this.pg_image = new XorInputPortGroup.PortGroup<>(true);
		pg_image.addPort("buffered-image", input);
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
				new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat()
		);
		outputs.put("buffered-image", output);
		outputs.put("buffered-image-binary", output_binary);
		outputs.put("buffered-matrix-float", output_float);
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.FINGERPRINT;
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new CannyEdgeDetector();
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
		final XorInputPortGroup.PortGroup<InputPort<?>> group = input_xor.getEnabledGroup();
		if (group == null) {
			log.warn("no input port group enabled");
			return;
		}

		final org.openimaj.image.processing.edges.CannyEdgeDetector canny = getCannyEdgeDetector();
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
			} else {
				final BufferedImage image = input.getValue();
				fimage = OpenIMAJUtils.toFImage(image, getBand(image));
			}
			canny.processImage(fimage);
			canny_out = fimage;
		}

		final BufferedImage canny_image = OpenIMAJUtils.toBinaryBufferedImage(canny_out);
		ProcessorBase.writeBufferedImage(context, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT, canny_image);
		final BufferedMatrix canny_mat;
		if (output_float.isConnected()) {
			canny_mat = OpenIMAJUtils.toBufferedMatrix(canny_out);
			ProcessorBase.writeBufferedMatrix(context, STORAGE_MAT, canny_mat);
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
		if (port.equals(input_float)) {
			return null;
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

		final org.openimaj.image.processing.edges.CannyEdgeDetector canny = getCannyEdgeDetector();
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
			} else {
				final BufferedImage image = getSubimage(input.getValue(), bounds);
				fimage = OpenIMAJUtils.toFImage(image, getBand(image));
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

	private org.openimaj.image.processing.edges.CannyEdgeDetector getCannyEdgeDetector() {
		float canny_sigma = sigma.getFloat();
		if (!Float.isFinite(canny_sigma) || canny_sigma < 0) {
			log.warn("invalid sigma: {}. Sigma is reset to 1.0f.", canny_sigma);
			canny_sigma = 1.0f;
		}

		boolean canny_auto = thresholds.get().selection == 0;
		float canny_low = 0;
		float canny_high = 0;
		if (!canny_auto) {
			canny_low = threshold_low.getFloat();
			canny_high = threshold_high.getFloat();
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
		if (canny_auto) {
			return new org.openimaj.image.processing.edges.CannyEdgeDetector(
					canny_sigma
			);
		}
		return new org.openimaj.image.processing.edges.CannyEdgeDetector(
				canny_low,
				canny_high,
				canny_sigma
		);
	}

	private <T extends BufferedImage> int getBand(T image) {
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
