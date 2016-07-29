package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.imaging.ops.GlobalThresholdOp;
import ch.unifr.diva.dip.api.services.Transmutable;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.MaterialDesignIcons;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Global threshold processor.
 */
@Component
@Service
public class GlobalThreshold extends ProcessableBase implements Transmutable {

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_gray;
	private final OutputPort<BufferedImage> output_binary;
	private final OutputPort<BufferedImage> output;
	private final IntegerSliderParameter bandParameter;
	private final XorParameter thresholdParameter;
	private final static String STORAGE_FILE = "binary.png";
	private final static String STORAGE_FORMAT = "PNG";

	enum AdaptiveMethod {

		MEAN {
					@Override
					int computeThreshold(BufferedImage source, int band) {
						return GlobalThresholdOp.computeMean(source, band, null);
					}
				},
		MOMENTS {
					@Override
					int computeThreshold(BufferedImage source, int band) {
						return GlobalThresholdOp.computeMoments(source, band, null);
					}
				},
		OTSU {
					@Override
					int computeThreshold(BufferedImage source, int band) {
						return GlobalThresholdOp.computeOtsu(source, band, null);
					}
				};

		abstract int computeThreshold(BufferedImage source, int band);
	}

	public GlobalThreshold() {
		super("Global Threshold");

		this.bandParameter = new IntegerSliderParameter("band", 1, 1, 4);
		this.parameters.put("band", this.bandParameter);

		final EnumParameter adaptiveMethods = new EnumParameter(
				"",
				AdaptiveMethod.class,
				AdaptiveMethod.OTSU.name()
		);
		final IntegerSliderParameter manualInput = new IntegerSliderParameter(
				"", // "manual",
				127, 0, 255
		);

		manualInput.addSliderViewHook((s) -> {
			s.setMajorTickUnit(64);
			s.setMinorTickCount(15);
		});

		this.thresholdParameter = new XorParameter("threshold", Arrays.asList(
				adaptiveMethods,
				manualInput
		));
		this.parameters.put("threshold", this.thresholdParameter);

		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), true);
		this.input_gray = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImageGray(), true);

		enableAllInputs();

		this.output = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.outputs.put("buffered-image", this.output);

		this.output_binary = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary());
		this.outputs.put("binary-image", this.output_binary);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new GlobalThreshold();
	}

	@Override
	public void init(ProcessorContext context) {
		inputCallback();

		this.input.portStateProperty().addListener(inputListener);
		this.input_gray.portStateProperty().addListener(inputListener);

		if (context != null) {
			final BufferedImage binaryImage = readBufferedImage(context, STORAGE_FILE);
			setOutputs(context, binaryImage);
		}
	}

	@Override
	public boolean isConnected() {
		return xorIsConnected(inputs().values());
	}

	private final InvalidationListener inputListener = (c) -> inputCallback();

	private void inputCallback() {
		if (this.input.isConnected()) {
			enableInputs(this.input);
		} else if (this.input_gray.isConnected()) {
			enableInputs(this.input_gray);
		} else {
			enableAllInputs();
		}

		transmute();
	}

	private void enableAllInputs() {
		enableInputs(null);
	}

	private void enableInputs(InputPort input) {
		inputs.clear();

		if (input == null || input.equals(this.input)) {
			this.inputs.put("buffered-image", this.input);
		}
		if (input == null || input.equals(this.input_gray)) {
			this.inputs.put("buffered-image-gray", this.input_gray);
		}
	}

	private InputPort<BufferedImage> getConnectedInput() {
		if (this.input_gray.isConnected()) {
			return this.input_gray;
		}
		return this.input;
	}

	private void setOutputs(ProcessorContext context, BufferedImage binaryImage) {
		if (binaryImage == null) {
			return;
		}
		this.output_binary.setOutput(binaryImage);
		this.output.setOutput(binaryImage);
		provideImageLayer(context, binaryImage);
	}

	@Override
	public void process(ProcessorContext context) {
		BufferedImage binaryImage = readBufferedImage(context, STORAGE_FILE);

		if (binaryImage == null) {
			final BufferedImage source = getConnectedInput().getValue();

			if (source instanceof BufferedMatrix) {
				// GloablThresholdOp can't handle BufferedMatrix; BITS and BYTES only
				// TODO: standard error handling!
				return;
			}

			final int band = this.bandParameter.get() - 1; // to index
			final GlobalThresholdOp op = new GlobalThresholdOp(band);

			final ValueListSelection vs = this.thresholdParameter.get();
			int th;
			switch (vs.selection) {
				case 1: // manual/fixed threshold
					th = (int) vs.list.get(vs.selection);
					log.info("global threshold filter (manual/fixed), threshold={}", th);
					op.setThreshold(th);
					break;

				case 0: // adaptive threshold
				default:
					final String mname = (String) vs.list.get(vs.selection);
					final AdaptiveMethod method = EnumParameter.valueOf(
							mname,
							AdaptiveMethod.class,
							AdaptiveMethod.OTSU
					);
					th = method.computeThreshold(source, band);
					log.info("global threshold filter ({}), threshold={}", method.name(), th);
					op.setThreshold(th);
					break;
			}

			binaryImage = filter(context, op, source, op.createBinaryDestImage(source));
			writeBufferedImage(context, STORAGE_FILE, STORAGE_FORMAT, binaryImage);
		}

		setOutputs(context, binaryImage);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_FILE);
		resetOutputs();
		resetLayer(context);
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.CONTRAST_BOX;
	}

	private final BooleanProperty transmuteProperty = new SimpleBooleanProperty();

	@Override
	public BooleanProperty transmuteProperty() {
		return this.transmuteProperty;
	}

}
