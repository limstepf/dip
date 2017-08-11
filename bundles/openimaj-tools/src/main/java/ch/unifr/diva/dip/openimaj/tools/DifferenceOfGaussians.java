package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.ProcessorDocumentation;
import ch.unifr.diva.dip.api.components.SimpleProcessorDocumentation;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter.ViewHook;
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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.DifferenceOfGaussian;
import org.osgi.service.component.annotations.Component;

/**
 * Difference of Gaussians filter.
 */
@Component(service = Processor.class)
public class DifferenceOfGaussians extends ProcessableBase implements Previewable {

	private final static String STORAGE_MAT = "dog.bmat";
	private final static String STORAGE_IMAGE = "dog.png";
	private final static String STORAGE_IMAGE_FORMAT = "PNG";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_gray;
	private final InputPort<BufferedMatrix> input_float;
	private final XorInputPorts input_xor;

	private final OutputPort<BufferedImage> output;
	private final OutputPort<BufferedImage> output_gray;
	private final OutputPort<BufferedMatrix> output_float;

	private final IntegerSliderParameter band;
	private final ExpParameter sigma1;
	private final ExpParameter sigma2;
	private final XorParameter normalization;

	/**
	 * Creates a new difference of gaussians filter.
	 */
	public DifferenceOfGaussians() {
		super("Difference Of Gaussians");

		this.band = new IntegerSliderParameter("Band", 1, 1, 4);
		band.addSliderViewHook((s) -> {
			s.disableProperty().bind(disableBandSelectionProperty);
		});
		parameters.put("band", band);

		this.sigma1 = new ExpParameter("Sigma1", "1.0");
		this.sigma2 = new ExpParameter("Sigma2", "2.0");
		final ViewHook<TextField> shook = (t) -> {
			t.setMaxWidth(64);
		};
		sigma1.addTextFieldViewHook(shook);
		sigma2.addTextFieldViewHook(shook);
		final CompositeGrid grid = new CompositeGrid(
				"Gaussians",
				new LabelParameter("Sigma1"),
				new LabelParameter("Sigma2"),
				sigma1,
				sigma2
		);
		grid.setColumnConstraints(2);
		grid.setHorizontalSpacing(10);
		parameters.put("gaussians", grid);

		final TextParameter nonormalize = new TextParameter("no normalization");
		final TextParameter clamp = new TextParameter("clamp to [0, 255]");
		final TextParameter offset = new TextParameter("offset by 128, and clamp to [0, 255]");
		final TextParameter normalize = new TextParameter("normalize to [0, 255]");

		this.normalization = new XorParameter(
				"Normalization",
				Arrays.asList(
						nonormalize,
						clamp,
						offset,
						normalize
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
				"OpenIMAJ's implementation of a difference of gaussians filter."
		);
		return doc;
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.COMPARE;
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new DifferenceOfGaussians();
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
		return normalization.getSelectedIndex() == 0;
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
		final FImage fimage = getSourceFImage(getSourceImage());
		if (fimage == null) {
			return;
		}

		dogInplace(fimage);

		BufferedImage dog_image = null;
		BufferedMatrix dog_mat = null;

		final boolean isForceFloat = isForceFloat();
		if (isForceFloat || output_float.isConnected()) {
			dog_mat = OpenIMAJUtils.toBufferedMatrix(fimage);
			writeBufferedMatrix(context, dog_mat, STORAGE_MAT);
		}
		if (!isForceFloat) {
			dog_image = OpenIMAJUtils.toBufferedImage(fimage);
			writeBufferedImage(context, dog_image, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT);
		}

		setOutputs(context, dog_image, dog_mat);
	}

	private void dogInplace(FImage fimage) {
		float s1 = sigma1.getFloat();
		if (!Float.isFinite(s1) || s1 < 0) {
			log.warn("invalid sigma1: {}. Sigma1 is reset to 1.0f.", s1);
			s1 = 1.0f;
		}
		float s2 = sigma2.getFloat();
		if (!Float.isFinite(s2) || s2 < 0) {
			log.warn("invalid sigma2: {}. Sigma2 is reset to 2.0f.", s2);
			s2 = 2.0f;
		}

		final DifferenceOfGaussian dog = new DifferenceOfGaussian(s1, s2);
		dog.processImage(fimage);

		switch (normalization.getSelectedIndex()) {
			case 0: // nonormalize
				// no-op
				break;
			case 1: // clamp
				fimage.clip(0.0f, 255.0f);
				break;
			case 2: // offset and clamp
				fimage.addInplace(128.0f);
				fimage.clip(0.0f, 255.0f);
				break;
			case 3: // normalize
			default:
				final float min = Math.abs(fimage.min());
				fimage.addInplace(min);
				final float max = fimage.max();
				final float mul = 255.0f / max;
				fimage.multiplyInplace(mul);
				break;
		}
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
	public boolean isReady() {
		if (!output.getPortState().equals(Port.State.READY)) {
			return false;
		}
		if (isForceFloat()) {
			if (!output_float.getPortState().equals(Port.State.READY)) {
				return false;
			}
		} else {
			if (!output_gray.getPortState().equals(Port.State.READY)) {
				return false;
			}
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
		final FImage fimage = OpenIMAJUtils.toFImage(image, getBand(image));
		dogInplace(fimage);
		final BufferedImage preview = OpenIMAJUtils.toBufferedImage(fimage);
		return SwingFXUtils.toFXImage(preview, null);
	}

}
