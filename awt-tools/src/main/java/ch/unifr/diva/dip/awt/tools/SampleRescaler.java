package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.color.ColorPort;
import ch.unifr.diva.dip.api.components.color.OutputColorPort;
import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import ch.unifr.diva.dip.api.datatypes.BufferedImageBinary;
import ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat;
import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import ch.unifr.diva.dip.api.imaging.ops.RescaleOp;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.Transmutable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Sample rescaling filter. Scaling/gain and biasing (and clamping).
 */
@Component
@Service
public class SampleRescaler extends ProcessableBase implements Transmutable {

	private final static String STORAGE_IMAGE = "rescaled.png";
	private final static String STORAGE_IMAGE_FORMAT = "PNG";
	private final static String STORAGE_MAT = "rescaled.bmat";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_float;
	private final OutputPort<BufferedImage> output;
	private final OutputPort<BufferedImageBinary> output_binary;
	private final OutputPort<BufferedMatrixFloat> output_float;
	private final List<OutputColorPort> outputColors;

	private final CompositeGrid configGrid;
	private final XorParameter outputConfig;
	private final List<Band> bands;

	private static class Band {

		public final static List<Double> GRID_PERCENT_WIDTHS = Arrays.asList(
				0.33, 0.33, 0.17, 0.17
		);

		public final int num;
		public final String key;
		public final ExpParameter gain;
		public final ExpParameter bias;
		public final ExpParameter min;
		public final ExpParameter max;
		public final CompositeGrid composite;

		public Band(int num) {
			this.num = num;
			this.key = String.format("band%d", this.num);
			this.gain = new ExpParameter("gain", "1.0");
			this.bias = new ExpParameter("bias", "0.0");
			this.min = new ExpParameter("min", "0");
			this.max = new ExpParameter("max", "255");
			this.composite = new CompositeGrid(
					String.format("band %d", this.num),
					gain, bias, min, max
			);
			this.composite.setColumnWidthConstraints(GRID_PERCENT_WIDTHS);

			// 4 textfields on a row... better shorten their widths a bit
			final ExpParameter[] params = {gain, bias, min, max};
			for (ExpParameter p : params) {
				p.addViewHook((tf) -> initTextField(tf));
			}
		}

		private void initTextField(TextField tf) {
			tf.setPrefColumnCount(tf.getPrefColumnCount() / 2);
		}

	}

	private final RescaleConfig rescaleConfig;

	private static class RescaleConfig {

		private final SampleRescaler rescaler;
		private ValueListSelection vs;
		private final Map<String, OutputPort> ports = new HashMap<>();
		private int numBands = -1;
		private boolean isBufferedMatrix;
		private RescaleOp.Precision precision;

		public RescaleConfig(SampleRescaler rescaler) {
			this.rescaler = rescaler;
		}

		private OutputColorPort getPort(SimpleColorModel cm) {
			return getPort(cm.name());
		}

		private OutputColorPort getPort(Object obj) {
			return getPort((String) obj);
		}

		private OutputColorPort getPort(String name) {
			return ColorPort.getColorPort(name, this.rescaler.outputColors);
		}

		public void update(ValueListSelection vs) {
			this.vs = vs;
			this.ports.clear();

			switch (vs.selection) {
				case 0: // BIT
					this.numBands = 1;
					this.isBufferedMatrix = false;
					this.precision = RescaleOp.Precision.BIT;
					this.ports.put("buffered-image-binary", this.rescaler.output_binary);
					break;

				case 1: { // BYTE
					this.numBands = (int) vs.getSelectedValue();
					this.isBufferedMatrix = false;
					this.precision = RescaleOp.Precision.BYTE;
					final OutputColorPort out;
					switch (this.numBands) {
						case 1:
							out = getPort(SimpleColorModel.GRAY);
							break;
						case 4:
							out = getPort(SimpleColorModel.RGBA);
							break;
						default: // 3 (or 2)
							out = getPort(SimpleColorModel.RGB);
							break;
					}
					this.ports.put(out.key, out.port);
					break;
				}

				case 2: // FLOAT
					this.numBands = (int) vs.getSelectedValue();
					this.isBufferedMatrix = true;
					this.precision = RescaleOp.Precision.FLOAT;
					this.ports.put("buffered-matrix-float", this.rescaler.output_float);
					break;

				case 3: // SimpleColorModel
					final OutputColorPort out = getPort(vs.getSelectedValue());
					this.numBands = out.cm.numBands();
					this.isBufferedMatrix = out.cm.requiresBufferedMatrix();
					this.precision = this.isBufferedMatrix
							? RescaleOp.Precision.FLOAT
							: RescaleOp.Precision.BYTE;
					this.ports.put(out.key, out.port);
					break;
			}
		}

		public int numBands() {
			return this.numBands;
		}

		public boolean isBufferedMatrix() {
			return this.isBufferedMatrix;
		}

		public void enableBands() {
			this.rescaler.enableBands(this.numBands);
		}

		public void enableOutputs() {
			this.rescaler.enableOutputs(this.ports);
		}

		public void setOutputs(BufferedImage image) {
			for (OutputPort port : this.ports.values()) {
				port.setOutput(image);
			}
		}

		public void setClamp() {
			switch (this.vs.selection) {
				case 0: // BIT
					this.rescaler.setClamp(1, "0", "1");
					break;

				case 1: // BYTE
					this.rescaler.setClamp(this.numBands, "0", "255");
					break;

				case 2: // FLOAT
					// don't know how to clamp (by default)
					break;

				case 3: // SimpleColorModel
					final OutputColorPort out = getPort(this.vs.getSelectedValue());
					this.rescaler.setClamp(out);
					break;
			}
		}

		public double[] getGain() {
			final double[] gain = new double[this.numBands];
			for (int i = 0; i < this.numBands; i++) {
				gain[i] = this.rescaler.bands.get(i).gain.getDouble();
			}
			return gain;
		}

		public double[] getBias() {
			final double[] bias = new double[this.numBands];
			for (int i = 0; i < this.numBands; i++) {
				bias[i] = this.rescaler.bands.get(i).bias.getDouble();
			}
			return bias;
		}

		public double[] getMin() {
			final double[] min = new double[this.numBands];
			for (int i = 0; i < this.numBands; i++) {
				min[i] = this.rescaler.bands.get(i).min.getDouble();
			}
			return min;
		}

		public double[] getMax() {
			final double[] max = new double[this.numBands];
			for (int i = 0; i < this.numBands; i++) {
				max[i] = this.rescaler.bands.get(i).max.getDouble();
			}
			return max;
		}

		public RescaleOp.Precision getPrecision() {
			return this.precision;
		}
	}

	/**
	 * Default constructor.
	 */
	public SampleRescaler() {
		super("Sample Rescaler");

		final TextParameter rescaleText = new TextParameter(
				"I'(x,y) = clamp(gain * I(x,y) + bias)"
		);
		rescaleText.addViewHook((n) -> {
			GridPane.setValignment(n, VPos.BASELINE);
			n.getStyleClass().add("dip-disabled");
		});
		final TextParameter bitOption = new TextParameter("BIT");
		final IntegerSliderParameter byteSlider = new IntegerSliderParameter(1, 1, 4);
		byteSlider.setPrefix(new TextParameter("BYTE"));
		byteSlider.addViewHook((s) -> initBandSlider(s));
		final IntegerSliderParameter floatSlider = new IntegerSliderParameter(1, 1, 4);
		floatSlider.setPrefix(new TextParameter("FLOAT"));
		floatSlider.addViewHook((s) -> initBandSlider(s));
		final EnumParameter cmOption = new EnumParameter(
				"", SimpleColorModel.class, SimpleColorModel.RGB.name()
		);
		this.outputConfig = new XorParameter(
				"output",
				Arrays.asList(
						bitOption,
						byteSlider,
						floatSlider,
						cmOption
				)
		);
		this.configGrid = new CompositeGrid(
				rescaleText,
				this.outputConfig
		);
		this.configGrid.setColumnWidthConstraints(0.5, 0.5);
		this.configGrid.setHorizontalSpacing(20);
		this.parameters.put("config", this.configGrid);

		// band rescale spec. labels
		final Insets insets = new Insets(10, 0, 0, 0);
		final LabelParameter scalingLP = new LabelParameter("Scaling");
		final LabelParameter clampLP = new LabelParameter("Clamp");
		scalingLP.addViewHook((label) -> {
			label.setPadding(insets);
		});
		clampLP.addViewHook((label) -> {
			label.setPadding(insets);
		});

		final CompositeGrid labelsTop = new CompositeGrid(
				scalingLP, new LabelParameter(""), clampLP, new LabelParameter("")
		);
		labelsTop.setColumnWidthConstraints(Band.GRID_PERCENT_WIDTHS);
		this.parameters.put("labels1", labelsTop);

		final CompositeGrid labelsBottom = new CompositeGrid(
				new LabelParameter("Gain"),
				new LabelParameter("Bias"),
				new LabelParameter("Minimum"),
				new LabelParameter("Maximum")
		);
		labelsBottom.setColumnWidthConstraints(Band.GRID_PERCENT_WIDTHS);
		this.parameters.put("labels2", labelsBottom);

		this.bands = Arrays.asList(
				new Band(1),
				new Band(2),
				new Band(3),
				new Band(4)
		);

		// enable bands
		enableBands(4);

		// inputs
		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), false);
		this.input_float = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat(), false);

		enableAllInputs();

		// enable outputs
		this.output = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.output_binary = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary());
		this.output_float = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat());

		this.outputColors = new ArrayList<>();
		for (SimpleColorModel cm : SimpleColorModel.values()) {
			final OutputColorPort out = new OutputColorPort(cm);
			this.outputColors.add(out);
		}

		this.rescaleConfig = new RescaleConfig(this);

		enableAllOutputs();
	}

	// the goal here is to have sliders of equal width, despite variable prefix
	// labels (BYTE, FLOAT). Not sure this is the best way to do this...
	private void initBandSlider(Slider s) {
		s.setMaxWidth(128);
		BorderPane.setAlignment(s, Pos.CENTER_RIGHT);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new SampleRescaler();
	}

	@Override
	public void init(ProcessorContext context) {
		inputCallback();
		configCallback();

		this.input.portStateProperty().addListener(inputListener);
		this.input_float.portStateProperty().addListener(inputListener);
		this.configGrid.property().addListener(configListener);

		if (context != null) {
			restoreOutputs(context, null);
		}
	}

	private final InvalidationListener configListener = (c) -> configCallback();

	private void configCallback() {
		final ValueListSelection vs = this.outputConfig.get();
		this.rescaleConfig.update(vs);

		this.rescaleConfig.setClamp();
		this.rescaleConfig.enableBands();
		this.rescaleConfig.enableOutputs();

		transmute();
	}

	private void setClamp(OutputColorPort output) {
		final int n = output.cm.numBands();
		for (int i = 0; i < n; i++) {
			final Band band = this.bands.get(i);
			band.min.set(Float.toString(output.cm.minValue(i)));
			band.max.set(Float.toString(output.cm.maxValue(i)));
		}
	}

	private void setClamp(int n, String min, String max) {
		for (int i = 0; i < n; i++) {
			final Band band = this.bands.get(i);
			band.min.set(min);
			band.max.set(max);
		}
	}

	private void enableBands(int num) {
		for (int i = 0; i < this.bands.size(); i++) {
			final Band band = this.bands.get(i);
			if (band.num <= num) {
				this.parameters.put(band.key, band.composite);
			} else {
				this.parameters.remove(band.key);
			}
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
		} else if (this.input_float.isConnected()) {
			enableInputs(this.input_float);
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
		if (input == null || input.equals(this.input_float)) {
			this.inputs.put("buffered-matrix-float", this.input_float);
		}
	}

	private InputPort<BufferedImage> getConnectedInput() {
		if (this.input_float.isConnected()) {
			return this.input_float;
		}
		return this.input;
	}

	private void enableAllOutputs() {
		this.outputs.clear();
		this.outputs.put("buffered-image", this.output);
		this.outputs.put("buffered-image-binary", this.output_binary);
		this.outputs.put("buffered-matrix-float", this.output_float);
		for (OutputColorPort out : this.outputColors) {
			this.outputs.put(out.key, out.port);
		}
	}

	@Override
	protected void resetOutputs() {
		this.output.setOutput(null);
		this.output_binary.setOutput(null);
		this.output_float.setOutput(null);
		for (OutputColorPort out : this.outputColors) {
			out.port.setOutput(null);
		}
	}

	private void enableOutputs() {
		this.outputs.clear();
		this.outputs.put("buffered-image", this.output);
	}

	private void enableOutputs(Map<String, OutputPort> ports) {
		enableOutputs();
		for (Map.Entry<String, OutputPort> port : ports.entrySet()) {
			this.outputs.put(port.getKey(), port.getValue());
		}
	}

	@Override
	public void process(ProcessorContext context) {
		if (!restoreOutputs(context, null)) {
			final BufferedImage source = getConnectedInput().getValue();
			final RescaleOp op = new RescaleOp(
					this.rescaleConfig.getGain(),
					this.rescaleConfig.getBias(),
					this.rescaleConfig.getMin(),
					this.rescaleConfig.getMax(),
					this.rescaleConfig.getPrecision()
			);
			final BufferedImage rescaledImage = op.filter(source, null);
			if (this.rescaleConfig.isBufferedMatrix()) {
				writeBufferedMatrix(context, STORAGE_MAT, (BufferedMatrix) rescaledImage);
			} else {
				writeBufferedImage(context, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT, rescaledImage);
			}
			restoreOutputs(context, rescaledImage);
		}
	}

	private boolean restoreOutputs(ProcessorContext context) {
		return restoreOutputs(context, null);
	}

	private boolean restoreOutputs(ProcessorContext context, BufferedImage rescaledImage) {
		final BufferedImage image;
		if (rescaledImage == null) {
			if (context == null) {
				return false;
			}
			if (this.rescaleConfig.isBufferedMatrix()) {
				image = readBufferedMatrix(context, STORAGE_MAT);
			} else {
				image = readBufferedImage(context, STORAGE_IMAGE);
			}
		} else {
			image = rescaledImage;
		}

		if (image == null) {
			return false;
		}

		this.output.setOutput(image);
		this.rescaleConfig.setOutputs(image);

		if (!this.rescaleConfig.isBufferedMatrix) {
			provideImageLayer(context, image);
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

	private final BooleanProperty transmuteProperty = new SimpleBooleanProperty();

	@Override
	public BooleanProperty transmuteProperty() {
		return this.transmuteProperty;
	}

}
