package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.awt.components.ColorPort;
import ch.unifr.diva.dip.awt.components.InputColorPort;
import ch.unifr.diva.dip.awt.components.OutputColorPort;
import ch.unifr.diva.dip.awt.imaging.Filter;
import ch.unifr.diva.dip.awt.imaging.SimpleColorModel;
import ch.unifr.diva.dip.awt.imaging.ops.InvertOp;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.VPos;
import javafx.scene.layout.RowConstraints;
import org.osgi.service.component.annotations.Component;

/**
 * Inverts all samples of an image, either {@literal w.r.t.} the sample domain
 * or the range of a {@code SimpleColorModel}.
 */
@Component(service = Processor.class)
public class SampleInverter extends ProcessableBase {

	private final static String STORAGE_IMAGE = "inverted.png";
	private final static String STORAGE_IMAGE_FORMAT = "PNG";
	private final static String STORAGE_MAT = "inverted.bmat";

	// untyped ports
	private final InputPort<BufferedImage> input;
	private final OutputPort<BufferedImage> output;
	// single-band ports
	private final InputPort<BufferedImage> input_binary;
	private final OutputPort<BufferedImage> output_binary;
	private final InputPort<BufferedMatrix> input_float;
	private final OutputPort<BufferedMatrix> output_float;
	// color-typed ports
	private final List<InputColorPort> inputColors;
	private final List<OutputColorPort> outputColors;

	private final XorParameter configParam;
	private final EnumParameter cmOption;
	private final IntegerSliderParameter singleBandOption;
	private final XorParameter cmBandOption;
	private final DoubleProperty selectedBandProperty;
	private final BooleanProperty selectBandEnableProperty;

	private final InverterConfig config;

	private static class InverterConfig {

		private final SampleInverter inverter;
		private ValueListSelection vs;
		private InputColorPort icp;
		private InputPort<? extends BufferedImage> ip;
		@SuppressWarnings("rawtypes")
		private OutputPort op;
		private int selectedBand = -1;

		public InverterConfig(SampleInverter inverter) {
			this.inverter = inverter;
		}

		public void update(ValueListSelection vs) {
			this.vs = vs;

			final String cmKey = inverter.cmOption.get();
			icp = ColorPort.getColorPort(cmKey, inverter.inputColors);

			switch (vs.selection) {
				case 0: // BIT
					ip = inverter.input_binary;
					op = inverter.output_binary;
					break;

				default:
				case 1: // BYTE
					ip = ColorPort.getColorPort(SimpleColorModel.GRAY.name(), inverter.inputColors).port;
					op = ColorPort.getColorPort(SimpleColorModel.GRAY.name(), inverter.outputColors).port;
					break;

				case 2: // SimpleColorModel
					final ValueListSelection cmvs = inverter.cmBandOption.get();
					switch (cmvs.selection) {
						case 1: // single-band
							if (icp.cm.requiresBufferedMatrix()) {
								ip = inverter.input_float;
								op = inverter.output_float;
							} else {
								ip = ColorPort.getColorPort(SimpleColorModel.GRAY.name(), inverter.inputColors).port;
								op = ColorPort.getColorPort(SimpleColorModel.GRAY.name(), inverter.outputColors).port;
							}
							selectedBand = inverter.singleBandOption.get();
							break;

						default:
						case 0: // all bands
							ip = icp.port;
							op = ColorPort.getColorPort(cmKey, inverter.outputColors).port;
							selectedBand = -1;
							break;
					}
					break;
			}
		}

		public InputPort<? extends BufferedImage> getExtraInput() {
			return this.ip;
		}

		@SuppressWarnings("unchecked")
		public void setExtraOutput(BufferedImage image) {
			this.op.setOutput(image);
		}

		public boolean isBufferedMatrix() {
			return ((vs.selection == 2) && icp.cm.requiresBufferedMatrix());
		}

		public double[] getMinRange() {
			final int n = selectedBand > 0 ? 1 : icp.cm.numBands();
			final double[] min = new double[n];
			if (n == 1) {
				min[0] = icp.cm.minValue(selectedBand - 1);
			} else {
				for (int i = 0; i < icp.cm.numBands(); i++) {
					min[i] = icp.cm.minValue(i);
				}
			}
			return min;
		}

		public double[] getMaxRange() {
			final int n = selectedBand > 0 ? 1 : icp.cm.numBands();
			final double[] max = new double[n];
			if (n == 1) {
				max[0] = icp.cm.maxValue(selectedBand - 1);
			} else {
				for (int i = 0; i < icp.cm.numBands(); i++) {
					max[i] = icp.cm.maxValue(i);
				}
			}
			return max;
		}

		public void adjustBandSelectionSlider() {
			if (icp.cm.numBands() < 2) {
				inverter.selectBandEnableProperty.set(false);
			} else {
				inverter.selectBandEnableProperty.set(true);

				final int currentBandSelection = inverter.singleBandOption.get();
				if (currentBandSelection > icp.cm.numBands()) {
					inverter.singleBandOption.set(1);
				}
				inverter.selectedBandProperty.set(icp.cm.numBands());
			}
		}

		@SuppressWarnings("unchecked")
		public void enablePorts() {
			inverter.enableInputs(ip);
			inverter.enableOutputs(op);
		}
	}

	public SampleInverter() {
		super("Sample Inverter");

		final TextParameter bitOption = new TextParameter("BIT");
		final TextParameter byteOption = new TextParameter("BYTE");
		this.cmOption = new EnumParameter(
				"", SimpleColorModel.class, SimpleColorModel.RGB.name()
		);
		final TextParameter allBandsOption = new TextParameter("all bands");
		this.singleBandOption = new IntegerSliderParameter(1, 1, 4);
		this.singleBandOption.setPrefix(new TextParameter("single band"));
		this.selectedBandProperty = new SimpleDoubleProperty(4);
		this.selectBandEnableProperty = new SimpleBooleanProperty(true);
		this.singleBandOption.addSliderViewHook((s) -> {
			s.maxProperty().bind(selectedBandProperty);
			s.visibleProperty().bind(this.selectBandEnableProperty);
		});
		this.cmBandOption = new XorParameter(
				"color model",
				Arrays.asList(
						allBandsOption,
						singleBandOption
				)
		);
		final CompositeGrid cmGrid = new CompositeGrid(
				cmOption,
				cmBandOption
		);
		cmGrid.setColumnWidthConstraints(0.33, 0.67);
		cmGrid.setHorizontalSpacing(10);
		final RowConstraints rc = new RowConstraints();
		rc.setValignment(VPos.TOP);
		cmGrid.setRowConstraints(rc);
		this.configParam = new XorParameter(
				"Input",
				Arrays.asList(
						bitOption,
						byteOption,
						cmGrid
				)
		);
		this.parameters.put("config", this.configParam);

		this.input = new InputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), false);
		this.input_binary = new InputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary(), false);
		this.input_float = new InputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat(), false);
		this.output = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.output_binary = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary());
		this.output_float = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat());

		this.inputColors = new ArrayList<>();
		this.outputColors = new ArrayList<>();

		for (SimpleColorModel cm : SimpleColorModel.values()) {
			this.inputColors.add(new InputColorPort(cm));
			this.outputColors.add(new OutputColorPort(cm));
		}

		this.config = new InverterConfig(this);

		enableAllInputs();
		enableAllOutputs();
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new SampleInverter();
	}

	@Override
	public void init(ProcessorContext context) {
		configCallback();

		this.configParam.property().addListener(configListener);

		if (context != null) {
			restoreOutputs(context);
		}

		attachListeners();
	}

	private void attachListeners() {
		this.input.portStateProperty().addListener(inputListener);
		this.input_binary.portStateProperty().addListener(inputListener);
		this.input_float.portStateProperty().addListener(inputListener);
		for (InputColorPort icp : this.inputColors) {
			icp.port.portStateProperty().addListener(inputListener);
		}
	}

	private final InvalidationListener inputListener = (c) -> inputCallback();

	private void inputCallback() {
		config.enablePorts();
		repaint();
	}

	private final InvalidationListener configListener = (c) -> configCallback();

	private void configCallback() {
		final ValueListSelection vs = this.configParam.get();

		config.update(vs);
		config.adjustBandSelectionSlider();
		config.enablePorts();

		repaint();
	}

	@Override
	public boolean isConnected() {
		return xorIsConnected(inputs().values());
	}

	private void enableAllInputs() {
		enableInputs(null);
	}

	private void enableInputs(InputPort<? extends BufferedImage> port) {
		inputs.clear();

		// only show single connected port
		final InputPort<? extends BufferedImage> c = getConnectedInput();
		if (port != null && c.isConnected()) {
			if (this.input.equals(c)) {
				this.inputs.put("buffered-image", this.input);
				return;
			} else if (this.input_binary.equals(c)) {
				this.inputs.put("buffered-image-binary", this.input_binary);
				return;
			} else if (this.input_float.equals(c)) {
				this.inputs.put("buffered-matrix-float", this.input_float);
				return;
			} else {
				for (InputColorPort icp : this.inputColors) {
					if (icp.port.equals(c)) {
						this.inputs.put(icp.key, icp.port);
						return;
					}
				}
			}
		}

		// otherwise show untyped + one specific extra port,
		// or all (if port == null)
		this.inputs.put("buffered-image", this.input);

		if (port == null || port.equals(this.input_binary)) {
			this.inputs.put("buffered-image-binary", this.input_binary);
		}
		if (port == null || port.equals(this.input_float)) {
			this.inputs.put("buffered-matrix-float", this.input_float);
		}
		for (InputColorPort icp : this.inputColors) {
			if (port == null || port.equals(icp.port)) {
				this.inputs.put(icp.key, icp.port);
			}
		}
	}

	private InputPort<? extends BufferedImage> getConnectedInput() {
		final InputPort<? extends BufferedImage> extra = this.config.getExtraInput();
		if (extra != null && extra.isConnected()) {
			return extra;
		}

		return this.input;
	}

	private void enableAllOutputs() {
		enableOutputs(null);
	}

	// untyped + one specific extra port, or all
	private void enableOutputs(OutputPort<? extends BufferedImage> port) {
		this.outputs.clear();
		this.outputs.put("buffered-image", this.output);

		if (port == null || port.equals(this.output_binary)) {
			this.outputs.put("buffered-image-binary", this.output_binary);
		}
		if (port == null || port.equals(this.output_float)) {
			this.outputs.put("buffered-matrix-float", this.output_float);
		}
		for (OutputColorPort ocp : this.outputColors) {
			if (port == null || port.equals(ocp.port)) {
				this.outputs.put(ocp.key, ocp.port);
			}
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

	private boolean restoreOutputs(ProcessorContext context) {
		return restoreOutputs(context, null);
	}

	private boolean restoreOutputs(ProcessorContext context, BufferedImage invertedImage) {
		final BufferedImage image;
		if (invertedImage == null) {
			if (context == null) {
				return false;
			}
			if (this.config.isBufferedMatrix()) {
				image = readBufferedMatrix(context, STORAGE_MAT);
			} else {
				image = readBufferedImage(context, STORAGE_IMAGE);
			}
		} else {
			image = invertedImage;
		}

		if (image == null) {
			return false;
		}

		this.output.setOutput(image);
		this.config.setExtraOutput(image);

		if (!this.config.isBufferedMatrix()) {
			provideImageLayer(context, image);
		}

		return true;
	}

	@Override
	public void process(ProcessorContext context) {
		if (!restoreOutputs(context)) {
			final BufferedImage source = getConnectedInput().getValue();
			final InvertOp op;

			if (this.config.isBufferedMatrix()) {
				op = new InvertOp(this.config.getMinRange(), this.config.getMaxRange());
			} else {
				if (source instanceof BufferedMatrix) {
					// TODO: error handling; can't process bmat without range/color model
					return;
				}
				op = new InvertOp();
			}

			final BufferedImage invertedImage = Filter.filter(context, op, source);
			if (this.config.isBufferedMatrix()) {
				writeBufferedMatrix(context, STORAGE_MAT, (BufferedMatrix) invertedImage);
			} else {
				writeBufferedImage(context, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT, invertedImage);
			}

			restoreOutputs(context, invertedImage);
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
	public NamedGlyph glyph() {
		return MaterialDesignIcons.INVERT_COLORS;
	}

}
