package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.IntegerParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.PersistentParameter.ViewHook;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.openimaj.tools.patch.AdaptiveLocalThresholdBernsen;
import ch.unifr.diva.dip.openimaj.utils.OpenIMAJUtils;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javafx.beans.InvalidationListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdContrast;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdGaussian;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdMean;
import org.openimaj.image.processing.threshold.AdaptiveLocalThresholdMedian;
import org.osgi.service.component.annotations.Component;

/**
 * OpenIMAJ's adaptive local threshold processor(s).
 */
@Component(service = Processor.class)
public class AdaptiveLocalThreshold extends ProcessableBase implements Previewable {

	private final static String STORAGE_IMAGE = "threshold.png";
	private final static String STORAGE_IMAGE_FORMAT = "PNG";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_gray;
	private final XorInputPorts input_xor;
	private final OutputPort<BufferedImage> output_binary;
	private final OutputPort<BufferedImage> output;

	private final IntegerSliderParameter band;
	private final EnumParameter method;
	protected final IntegerParameter sizeX;
	protected final IntegerParameter sizeY;
	protected final CompositeGrid sizeGrid;
	protected final ExpParameter threshold;
	protected final ExpParameter sigma;
	protected final ExpParameter offset;

	private String runtimeTitle;

	/**
	 * Available adaptive local threshold methods.
	 */
	public enum LocalThresholdMethod {

		BERNSEN("Bernsen's Local Thresholding") {
					@Override
					public void putParameters(AdaptiveLocalThreshold processor) {
						processor.parameters().put("size", processor.sizeGrid);
						processor.parameters().remove("offset");
						processor.parameters().put("threshold", processor.threshold);
						processor.parameters().remove("sigma");
					}

					@Override
					public void process(AdaptiveLocalThreshold processor, FImage fimage) {
						/*
						 * AdaptiveLocalThresholdBernsen is (indirectly) broken in 1.3.5.
						 * Should be fixed in the next release of OpenIMAJ.
						 */
						final AdaptiveLocalThresholdBernsen bernsen = new AdaptiveLocalThresholdBernsen(
								processor.threshold.getFloat(),
								processor.sizeX.get(),
								processor.sizeY.get()
						);
						bernsen.processImage(fimage);
					}
				},
		CONTRAST("Local Contrast Thresholding") {
					@Override
					public void putParameters(AdaptiveLocalThreshold processor) {
						processor.parameters().put("size", processor.sizeGrid);
						processor.parameters().remove("offset");
						processor.parameters().remove("threshold");
						processor.parameters().remove("sigma");
					}

					@Override
					public void process(AdaptiveLocalThreshold processor, FImage fimage) {
						final AdaptiveLocalThresholdContrast contrast = new AdaptiveLocalThresholdContrast(
								processor.sizeX.get(),
								processor.sizeY.get()
						);
						contrast.processImage(fimage);
					}
				},
		GAUSSIAN("Local Gaussian Thresholding") {
					@Override
					public void putParameters(AdaptiveLocalThreshold processor) {
						processor.parameters().remove("size");
						processor.parameters().put("offset", processor.offset);
						processor.parameters().remove("threshold");
						processor.parameters().put("sigma", processor.sigma);
					}

					@Override
					public void process(AdaptiveLocalThreshold processor, FImage fimage) {
						final AdaptiveLocalThresholdGaussian gaussian = new AdaptiveLocalThresholdGaussian(
								processor.sigma.getFloat(),
								processor.offset.getFloat()
						);
						gaussian.processImage(fimage);
					}
				},
		MEAN("Local Mean Thresholding") {
					@Override
					public void putParameters(AdaptiveLocalThreshold processor) {
						processor.parameters().put("size", processor.sizeGrid);
						processor.parameters().put("offset", processor.offset);
						processor.parameters().remove("threshold");
						processor.parameters().remove("sigma");
					}

					@Override
					public void process(AdaptiveLocalThreshold processor, FImage fimage) {
						final AdaptiveLocalThresholdMean mean = new AdaptiveLocalThresholdMean(
								processor.sizeX.get(),
								processor.sizeY.get(),
								processor.offset.getFloat()
						);
						mean.processImage(fimage);
					}
				},
		MEDIAN("Local Median Thresholding") {
					@Override
					public void putParameters(AdaptiveLocalThreshold processor) {
						MEAN.putParameters(processor);
					}

					@Override
					public void process(AdaptiveLocalThreshold processor, FImage fimage) {
						final AdaptiveLocalThresholdMedian mean = new AdaptiveLocalThresholdMedian(
								processor.sizeX.get(),
								processor.sizeY.get(),
								processor.offset.getFloat()
						);
						mean.processImage(fimage);
					}
				};

		private final String methodName;

		private LocalThresholdMethod(String name) {
			this.methodName = name;
		}

		public String getMethodName() {
			return methodName;
		}

		public abstract void putParameters(AdaptiveLocalThreshold processor);

		public abstract void process(AdaptiveLocalThreshold processor, FImage fimage);

	}

	/**
	 * Creates a new adaptive local threshold processor.
	 */
	public AdaptiveLocalThreshold() {
		super("Adaptive Local Threshold");

		this.runtimeTitle = this.name;
		this.band = new IntegerSliderParameter("band", 1, 1, 4);
		parameters.put("band", band);

		this.method = new EnumParameter("Method", LocalThresholdMethod.class, LocalThresholdMethod.BERNSEN.name());
		method.addComboBoxViewHook((c) -> {
			c.setMaxWidth(196);
		});
		parameters.put("method", method);

		final ViewHook<TextField> intViewHook = (t) -> {
			t.setMaxWidth(90);
		};
		this.sizeX = new IntegerParameter("Patch width", 7, 1, Integer.MAX_VALUE);
		sizeX.addTextFieldViewHook(intViewHook);
		this.sizeY = new IntegerParameter("Patch height", 7, 1, Integer.MAX_VALUE);
		sizeY.addTextFieldViewHook(intViewHook);
		final TextParameter sizeMul = new TextParameter(" x ");
		this.sizeGrid = new CompositeGrid(
				"Patch size",
				sizeX,
				sizeMul,
				sizeY
		);

		final ViewHook<TextField> expViewHook = (t) -> {
			t.setMaxWidth(196);
		};
		this.threshold = new ExpParameter("Contrast threshold", "15");
		threshold.addTextFieldViewHook(expViewHook);
		this.sigma = new ExpParameter("Sigma", "1.0");
		sigma.addTextFieldViewHook(expViewHook);
		this.offset = new ExpParameter("Patch mean offset", "0");
		offset.addTextFieldViewHook(expViewHook);

		parameters().put("size", sizeGrid);
		parameters().put("offset", offset);
		parameters().put("threshold", threshold);
		parameters().put("sigma", sigma);

		this.input = new InputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), true);
		this.input_gray = new InputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImageGray(), true);
		this.input_xor = new XorInputPorts(this);
		input_xor.addPort("buffered-image", input);
		input_xor.addPort("buffered-image-gray", input_gray);
		input_xor.enableAllPorts();

		this.output = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.output_binary = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary());
		outputs.put("buffered-image", this.output);
		outputs.put("binary-image", this.output_binary);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new AdaptiveLocalThreshold();
	}

	@Override
	public String name() {
		return runtimeTitle;
	}

	private final InvalidationListener methodListener = (c) -> onMethodSelection();

	protected void onMethodSelection() {
		final LocalThresholdMethod m = method.getEnumValue(LocalThresholdMethod.class);
		runtimeTitle = m.getMethodName();
		m.putParameters(this);
		repaint();
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.CONTRAST_BOX;
	}

	@Override
	public void init(ProcessorContext context) {
		input_xor.init(context);
		method.property().addListener(methodListener);
		onMethodSelection();

		if (context != null) {
			restoreOutputs(context);
		}
	}

	@Override
	public void process(ProcessorContext context) {
		final FImage fimage = getSourceFImage();
		if (fimage == null) {
			return;
		}

		final LocalThresholdMethod m = method.getEnumValue(LocalThresholdMethod.class);
		m.process(this, fimage);

		final BufferedImage image = OpenIMAJUtils.toBinaryBufferedImage(fimage);
		writeBufferedImage(context, image, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT);
		setOutputs(context, image);
	}

	@Override
	public Image previewSource(ProcessorContext context) {
		return SwingFXUtils.toFXImage(getSourceImage(), null);
	}

	@Override
	public Image preview(ProcessorContext context, Rectangle bounds) {
		final BufferedImage image = getSourceImage().getSubimage(
				bounds.x,
				bounds.y,
				bounds.width,
				bounds.height
		);
		final FImage fimage = OpenIMAJUtils.toFImage(image, getBand(image));
		final LocalThresholdMethod m = method.getEnumValue(LocalThresholdMethod.class);
		m.process(this, fimage);
		final BufferedImage preview = OpenIMAJUtils.toBinaryBufferedImage(fimage);
		return SwingFXUtils.toFXImage(preview, null);
	}

	protected BufferedImage getSourceImage() {
		final InputPort<?> port = input_xor.getEnabledPort();
		if (port == null) {
			log.warn("no input port enabled");
			return null;
		}

		if (port.equals(input_gray)) {
			return input_gray.getValue();
		}
		return input.getValue();
	}

	protected FImage getSourceFImage() {
		final BufferedImage image = getSourceImage();
		if (image == null) {
			return null;
		}
		return OpenIMAJUtils.toFImage(image, getBand(image));
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
		setOutputs(context, image);
	}

	protected void setOutputs(ProcessorContext context, BufferedImage image) {
		if (image != null) {
			provideImageLayer(context, image);
		}
		output.setOutput(image);
		output_binary.setOutput(image);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_IMAGE);
		resetOutputs();
		resetLayer(context);
	}

}
