package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import ch.unifr.diva.dip.api.parameters.CheckboxParameter;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EmptyParameter;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.awt.components.ColorPort;
import ch.unifr.diva.dip.awt.components.OutputColorPort;
import ch.unifr.diva.dip.awt.imaging.SimpleColorModel;
import ch.unifr.diva.dip.awt.imaging.ops.NullOp.SamplePrecision;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * A rescaling unit, or base class. Not by itself a processor, but used by the
 * sample rescaler and the convolution filter, maybe more, and because we can
 * only extend once, eh.
 */
public class RescaleUnit {

	// GUI/config
	private final List<? extends Band> bands;
	private final XorParameter outputConfig;
	private final CompositeGrid bandConfig;

	// outputs
	private final OutputPort<BufferedImage> output;
	private final OutputPort<BufferedImage> output_binary;
	private final OutputPort<BufferedMatrix> output_float;
	private final List<OutputColorPort> outputColors;

	// state/config
	private ValueListSelection vs;
	private int numBands = -1;
	private boolean isBufferedMatrix;
	private SamplePrecision precision;
	private final Map<String, OutputPort<? extends BufferedImage>> outputPorts = new HashMap<>();
	private final Map<String, OutputPort<? extends BufferedImage>> enabledOutputPorts = new HashMap<>();

	/**
	 * Creates a new rescale unit with up to 4 bands.
	 */
	public RescaleUnit() {
		this(getDefaultBands(4), true, true, true, true);
	}

	public static List<Band> getDefaultBands(int num) {
		final List<Band> bands = new ArrayList<>();
		for (int i = 1; i <= num; i++) {
			bands.add(new Band(i));
		}
		return bands;
	}

	protected final TextParameter bitOption;
	protected final IntegerSliderParameter byteSlider;
	protected final IntegerSliderParameter floatSlider;
	protected final EnumParameter cmOption;
	protected final List<Parameter<?>> enabledOutputOptions;

	public RescaleUnit(List<? extends Band> bands, boolean enableBitOutput, boolean enableByteOutput, boolean enableFloatOutput, boolean enableColorOutput) {
		this.bands = bands;

		this.enabledOutputOptions = new ArrayList<>();
		if (enableBitOutput) {
			this.bitOption = new TextParameter("BIT");
			enabledOutputOptions.add(bitOption);
		} else {
			this.bitOption = null;
		}
		if (enableByteOutput) {
			this.byteSlider = new IntegerSliderParameter(1, 1, 4);
			byteSlider.setPrefix(new TextParameter("BYTE"));
			byteSlider.addSliderViewHook((s) -> initBandSlider(s));
			enabledOutputOptions.add(byteSlider);
		} else {
			this.byteSlider = null;
		}
		if (enableFloatOutput) {
			this.floatSlider = new IntegerSliderParameter(1, 1, 4);
			floatSlider.setPrefix(new TextParameter("FLOAT"));
			floatSlider.addSliderViewHook((s) -> initBandSlider(s));
			enabledOutputOptions.add(floatSlider);
		} else {
			this.floatSlider = null;
		}
		if (enableColorOutput) {
			this.cmOption = new EnumParameter(
					"", SimpleColorModel.class, SimpleColorModel.RGB.name()
			);
			enabledOutputOptions.add(cmOption);
		} else {
			this.cmOption = null;
		}
		this.outputConfig = new XorParameter(
				"output",
				enabledOutputOptions
		);

		final List<Parameter<?>> bandComponents = new ArrayList<>();
		bandComponents.addAll(this.bands.get(0).getHeaderRows());
		for (Band band : this.bands) {
			bandComponents.add(band.composite);
		}
		this.bandConfig = new CompositeGrid(bandComponents);
		this.bandConfig.setColumnConstraints(1);

		// outputs
		this.output = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.output_binary = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary());
		this.output_float = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImageFloat());

		this.outputPorts.put("buffered-image", this.output);
		this.outputPorts.put("buffered-image-binary", this.output_binary);
		this.outputPorts.put("buffered-matrix-float", this.output_float);

		this.outputColors = new ArrayList<>();
		for (SimpleColorModel cm : SimpleColorModel.values()) {
			final OutputColorPort out = new OutputColorPort(cm);
			this.outputColors.add(out);
			this.outputPorts.put(out.key, out.port);
		}
	}

	// the goal here is to have sliders of equal width, despite variable prefix
	// labels (BYTE, FLOAT). Not sure this is the best way to do this...
	private void initBandSlider(Slider s) {
		s.setMaxWidth(128);
		BorderPane.setAlignment(s, Pos.CENTER_RIGHT);
	}

	private OutputColorPort getPort(SimpleColorModel cm) {
		return getPort(cm.name());
	}

	private OutputColorPort getPort(Object obj) {
		return getPort((String) obj);
	}

	private OutputColorPort getPort(String name) {
		return ColorPort.getColorPort(name, this.outputColors);
	}

	private Parameter<?> getSelectedOutputOption(int index) {
		return enabledOutputOptions.get(index);
	}

	/**
	 * Updates the state/config.
	 */
	public void update() {
		this.vs = this.outputConfig.get();
		this.enabledOutputPorts.clear();
		this.enabledOutputPorts.put("buffered-image", this.output);

		final Object selectedValue = getSelectedOutputOption(vs.getSelectedIndex());
		if (selectedValue != null) {
			if (selectedValue.equals(this.bitOption)) {
				this.numBands = 1;
				this.isBufferedMatrix = false;
				this.precision = SamplePrecision.BIT;
				this.enabledOutputPorts.put("buffered-image-binary", this.output_binary);

			} else if (selectedValue.equals(this.byteSlider)) {
				this.numBands = (int) this.vs.getSelectedValue();
				this.isBufferedMatrix = false;
				this.precision = SamplePrecision.BYTE;
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
				this.enabledOutputPorts.put(out.key, out.port);

			} else if (selectedValue.equals(this.floatSlider)) {
				this.numBands = (int) this.vs.getSelectedValue();
				this.isBufferedMatrix = true;
				this.precision = SamplePrecision.FLOAT;
				this.enabledOutputPorts.put("buffered-matrix-float", this.output_float);

			} else if (selectedValue.equals(this.cmOption)) {
				final OutputColorPort out = getPort(this.vs.getSelectedValue());
				this.numBands = out.cm.numBands();
				this.isBufferedMatrix = out.cm.requiresBufferedMatrix();
				this.precision = this.isBufferedMatrix
						? SamplePrecision.FLOAT
						: SamplePrecision.BYTE;
				this.enabledOutputPorts.put(out.key, out.port);
			}
		}

		setClamp();
		enableBands(this.numBands);
	}

	/**
	 * Returns the output configuration parameter.
	 *
	 * @return the output configuration parameter.
	 */
	public XorParameter getOutputConfig() {
		return this.outputConfig;
	}

	/**
	 * Returns the band configuration parameter.
	 *
	 * @return the band configuration parameter.
	 */
	public CompositeGrid getBandConfig() {
		return this.bandConfig;
	}

	/**
	 * Returns the enabled output ports of the current configuration.
	 *
	 * @return the enabled output ports.
	 */
	public Map<String, OutputPort<? extends BufferedImage>> getEnabledOutputPorts() {
		return this.enabledOutputPorts;
	}

	/**
	 * Returns all output ports.
	 *
	 * @return all output ports.
	 */
	public Map<String, OutputPort<? extends BufferedImage>> getAllOutputPorts() {
		return this.outputPorts;
	}

	/**
	 * Returns the number of bands of the current configuration.
	 *
	 * @return the number of bands of the current configuration.
	 */
	public int numBands() {
		return this.numBands;
	}

	/**
	 * Checks whether the current configuration requires a
	 * {@code BufferedMatrix}, or if a {@code BufferedImage} will do.
	 *
	 * @return {@code true} if the current configuration requires a
	 * {@code BufferedMatrix}, {@code false} otherwise.
	 */
	public boolean isBufferedMatrix() {
		return this.isBufferedMatrix;
	}

	/**
	 * Returns indications of all bands of the current configuration as to
	 * whether or not take the absolute sample value.
	 *
	 * @return the abs. value indicator of all bands.
	 */
	public boolean[] getAbs() {
		final boolean[] abs = new boolean[this.numBands];
		for (int i = 0; i < this.numBands; i++) {
			abs[i] = this.bands.get(i).abs.get();
		}
		return abs;
	}

	/**
	 * Returns the gain of all bands of the current configuration.
	 *
	 * @return the gain of all bands.
	 */
	public double[] getGain() {
		final double[] gain = new double[this.numBands];
		for (int i = 0; i < this.numBands; i++) {
			gain[i] = this.bands.get(i).gain.getDouble();
		}
		return gain;
	}

	/**
	 * Returns the bias of all bands of the current configuration.
	 *
	 * @return the bias of all bands.
	 */
	public double[] getBias() {
		final double[] bias = new double[this.numBands];
		for (int i = 0; i < this.numBands; i++) {
			bias[i] = this.bands.get(i).bias.getDouble();
		}
		return bias;
	}

	/**
	 * Returns the minimum range of all bands of the current configuration.
	 *
	 * @return the minimum range of all bands.
	 */
	public double[] getMin() {
		final double[] min = new double[this.numBands];
		for (int i = 0; i < this.numBands; i++) {
			min[i] = this.bands.get(i).min.getDouble();
		}
		return min;
	}

	/**
	 * Returns the maximum range of all bands of the current configuration.
	 *
	 * @return the maximum range of all bands.
	 */
	public double[] getMax() {
		final double[] max = new double[this.numBands];
		for (int i = 0; i < this.numBands; i++) {
			max[i] = this.bands.get(i).max.getDouble();
		}
		return max;
	}

	/**
	 * Returns the sample precision.
	 *
	 * @return the sample precision.
	 */
	public SamplePrecision getPrecision() {
		return this.precision;
	}

	/**
	 * Sets the value on all ports of the current configuration.
	 *
	 * @param image the value to be set on all ports of the current
	 * configuration.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void setOutputs(BufferedImage image) {
		for (OutputPort port : this.enabledOutputPorts.values()) {
			port.setOutput(image);
		}
	}

	private void setClamp() {
		final Object selectedValue = getSelectedOutputOption(vs.getSelectedIndex());
		if (selectedValue != null) {
			if (selectedValue.equals(this.bitOption)) {
				this.setClamp(1, "0", "1");
			} else if (selectedValue.equals(this.byteSlider)) {
				this.setClamp(this.numBands, "0", "255");
			} else if (selectedValue.equals(this.floatSlider)) {
				this.setClamp(1, "0.0", "1.0"); // wild guess :)
			} else if (selectedValue.equals(this.cmOption)) {
				final OutputColorPort out = getPort(this.vs.getSelectedValue());
				this.setClamp(out);
			}
		}
	}

	private void setClamp(int n, String min, String max) {
		for (int i = 0; i < n; i++) {
			final Band band = this.bands.get(i);
			band.min.set(min);
			band.max.set(max);
		}
	}

	private void setClamp(OutputColorPort output) {
		final int n = output.cm.numBands();
		for (int i = 0; i < n; i++) {
			final Band band = this.bands.get(i);
			band.min.set(Float.toString(output.cm.minValue(i)));
			band.max.set(Float.toString(output.cm.maxValue(i)));
		}
	}

	private void enableBands(int num) {
		for (int i = 0; i < this.bands.size(); i++) {
			final Band band = this.bands.get(i);
			band.composite.setHide(band.num > num);
		}
	}

	/**
	 * Configuration of a single band.
	 */
	public static class Band {

		protected final int num;
		protected final List<Double> gridPercentWidths;
		protected final String key;
		protected final LabelParameter label;
		protected final CheckboxParameter abs;
		protected final ExpParameter gain;
		protected final ExpParameter bias;
		protected final ExpParameter min;
		protected final ExpParameter max;
		protected final CompositeGrid composite;

		/**
		 * Creates a new band.
		 *
		 * @param num the band number, starting with {@code 1} for the first
		 * band.
		 */
		public Band(int num) {
			this(
					num,
					null,
					Arrays.asList(
							0.03, 0.05, 0.3, 0.3, 0.16, 0.16
					)
			);
		}

		/**
		 * Creates a new band.
		 *
		 * @param num the band number, starting with {@code 1} for the first
		 * band.
		 * @param extraParameter an extra parameter, or null.
		 * @param gridPercentWidths the grid percent widths.
		 */
		public Band(int num, Parameter<?> extraParameter, List<Double> gridPercentWidths) {
			this.num = num;
			this.gridPercentWidths = gridPercentWidths;
			this.key = String.format("band%d", this.num);
			this.label = new LabelParameter(String.format("%d:", this.num));
			this.abs = new CheckboxParameter(false);
			this.gain = new ExpParameter("gain", "1.0");
			this.bias = new ExpParameter("bias", "0.0");
			this.min = new ExpParameter("min", "0");
			this.max = new ExpParameter("max", "255");
			if (extraParameter == null) {
				this.composite = new CompositeGrid(
						String.format("band %d", this.num),
						label, abs, gain, bias, min, max
				);
			} else {
				this.composite = new CompositeGrid(
						String.format("band %d", this.num),
						label, extraParameter, abs, gain, bias, min, max
				);
			}
			this.composite.setColumnWidthConstraints(gridPercentWidths);

			// 4 textfields on a row... better shorten their widths a bit
			final ExpParameter[] params = {gain, bias, min, max};
			for (ExpParameter p : params) {
				p.addTextFieldViewHook((tf) -> initTextField(tf));
			}
		}

		protected final void initTextField(TextField tf) {
			tf.setPrefColumnCount(tf.getPrefColumnCount() / 2);
		}

		public List<Parameter<?>> getHeaderRows() {
			return getHeaderRows(null, gridPercentWidths);
		}

		public static List<Parameter<?>> getHeaderRows(Parameter<?> extraLabel, List<Double> gridPercentWidths) {
			final List<Parameter<?>> header = new ArrayList<>();
			final Insets insets = new Insets(10, 0, 0, 0);
			final LabelParameter scalingLP = new LabelParameter("Scaling");
			final LabelParameter clampLP = new LabelParameter("Clamp");
			scalingLP.addLabelViewHook((t) -> {
				t.setPadding(insets);
			});
			clampLP.addLabelViewHook((t) -> {
				t.setPadding(insets);
			});

			final CompositeGrid top;
			final CompositeGrid bottom;
			if (extraLabel == null) {
				top = new CompositeGrid(
						new EmptyParameter(),
						new EmptyParameter(),
						scalingLP,
						new EmptyParameter(),
						clampLP,
						new EmptyParameter()
				);
				bottom = new CompositeGrid(
						new EmptyParameter(),
						new LabelParameter("abs."),
						new LabelParameter("Gain"),
						new LabelParameter("Bias"),
						new LabelParameter("Minimum"),
						new LabelParameter("Maximum")
				);
			} else {
				top = new CompositeGrid(
						new EmptyParameter(),
						new EmptyParameter(),
						new EmptyParameter(),
						scalingLP,
						new EmptyParameter(),
						clampLP,
						new EmptyParameter()
				);
				bottom = new CompositeGrid(
						new EmptyParameter(),
						extraLabel,
						new LabelParameter("abs."),
						new LabelParameter("Gain"),
						new LabelParameter("Bias"),
						new LabelParameter("Minimum"),
						new LabelParameter("Maximum")
				);
			}
			top.setColumnWidthConstraints(gridPercentWidths);
			bottom.setColumnWidthConstraints(gridPercentWidths);
			header.add(top);
			header.add(bottom);
			return header;
		}

	}

}
