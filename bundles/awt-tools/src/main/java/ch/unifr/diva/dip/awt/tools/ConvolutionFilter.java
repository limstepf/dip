package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.datastructures.DoubleKernel;
import ch.unifr.diva.dip.api.datastructures.DoubleMatrix;
import ch.unifr.diva.dip.api.datastructures.FloatKernel;
import ch.unifr.diva.dip.api.datastructures.FloatMatrix;
import ch.unifr.diva.dip.api.datastructures.Kernel;
import ch.unifr.diva.dip.api.datastructures.Matrix;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.awt.imaging.Filter;
import ch.unifr.diva.dip.awt.imaging.ops.ConvolutionOp;
import ch.unifr.diva.dip.awt.imaging.padders.ImagePadder;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import org.osgi.service.component.annotations.Component;

/**
 * A convolution filter.
 */
@Component(service = Processor.class)
public class ConvolutionFilter extends ProcessableBase implements Previewable {

	private final static String IMAGE_FORMAT = "PNG";
	private final static String IMAGE_FILE = "convolved.png";
	private final static String MATRIX_FILE = "convolved.bmat";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedMatrix> input_float;

	private final KernelInput<FloatMatrix> kernelFloat;
	private final KernelInput<DoubleMatrix> kernelDouble;

	private final RescaleUnit rescaleUnit;
	private final CompositeGrid configGrid;
	private final EnumParameter kernelPrecision;
	private final EnumParameter padderOption;

	/**
	 * Kernel input set.
	 *
	 * @param <T> type of the matrix.
	 */
	private static class KernelInput<T extends Matrix<T>> {

		private final String postfix;
		public final InputPort<? extends Matrix<?>> kernel;
		public final InputPort<? extends Matrix<?>> rowVector;
		public final InputPort<? extends Matrix<?>> columnVector;

		public KernelInput(Class<T> clazz) {
			if (clazz.equals(DoubleMatrix.class)) {
				this.postfix = "d";
				this.kernel = new InputPort<>(
						"Kernel",
						new ch.unifr.diva.dip.api.datatypes.DoubleMatrix(),
						false
				);
				this.rowVector = new InputPort<>(
						"RowVector",
						new ch.unifr.diva.dip.api.datatypes.DoubleMatrix(),
						false
				);
				this.columnVector = new InputPort<>(
						"ColumnVector",
						new ch.unifr.diva.dip.api.datatypes.DoubleMatrix(),
						false
				);
			} else {
				this.postfix = "f";
				this.kernel = new InputPort<>(
						"Kernel",
						new ch.unifr.diva.dip.api.datatypes.FloatMatrix(),
						false
				);
				this.rowVector = new InputPort<>(
						"RowVector",
						new ch.unifr.diva.dip.api.datatypes.FloatMatrix(),
						false
				);
				this.columnVector = new InputPort<>(
						"ColumnVector",
						new ch.unifr.diva.dip.api.datatypes.FloatMatrix(),
						false
				);
			}
		}

		public void registerPorts(Map<String, InputPort<?>> map) {
			map.put("kernel-" + this.postfix, kernel);
			map.put("row-vector-" + this.postfix, rowVector);
			map.put("column-vector-" + this.postfix, columnVector);
		}

		public boolean isSeparable() {
			if (!this.rowVector.isConnected() || !this.rowVector.getPortState().equals(Port.State.READY)) {
				return false;
			}
			if (!this.columnVector.isConnected() || !this.columnVector.getPortState().equals(Port.State.READY)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Creates a new convolution filter.
	 */
	public ConvolutionFilter() {
		super("Convolution Filter");

		// parameters/config
		this.kernelPrecision = new EnumParameter(
				"precision", Kernel.Precision.class, Kernel.Precision.FLOAT.name()
		);
		this.padderOption = new EnumParameter(
				"edge handling", ImagePadder.Type.class, ImagePadder.Type.REFLECTIVE.name()
		);
		this.rescaleUnit = new RescaleUnit();
		this.rescaleUnit.getBandConfig().addGridPaneViewHook((gp) -> {
			GridPane.setConstraints(gp, 0, 1, 2, 1);
		});

		final LabelParameter kpLabel = new LabelParameter("Precision:");
		final LabelParameter ehLabel = new LabelParameter("Edge handling:");
		ehLabel.addLabelViewHook((l) -> {
			GridPane.setMargin(l, new Insets(10, 0, 0, 0));
		});
		final CompositeGrid convolutionGrid = new CompositeGrid(
				kpLabel,
				this.kernelPrecision,
				ehLabel,
				this.padderOption
		);
		convolutionGrid.setColumnConstraints(1);
		this.configGrid = new CompositeGrid(
				convolutionGrid,
				this.rescaleUnit.getOutputConfig(),
				this.rescaleUnit.getBandConfig()
		);
		this.configGrid.setColumnWidthConstraints(0.5, 0.5);
		this.parameters.put("config", configGrid);

		// inputs
		this.kernelFloat = new KernelInput<>(FloatMatrix.class);
		this.kernelDouble = new KernelInput<>(DoubleMatrix.class);

		this.input = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				false
		);
		this.input_float = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageFloat(),
				false
		);

		enableAllInputs();
		enableAllOutputs();
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new ConvolutionFilter();
	}

	@Override
	public void init(ProcessorContext context) {
		this.rescaleUnit.update();
		enableOutputs();
		inputCallback();

		if (context != null) {
			restoreOutputs(context);
		}

		// add listeners
		this.input.portStateProperty().addListener(inputListener);
		this.input_float.portStateProperty().addListener(inputListener);
		this.kernelPrecision.property().addListener(inputListener);
		this.rescaleUnit.getOutputConfig().property().addListener(configListener);
	}

	private final InvalidationListener configListener = (c) -> configCallback();

	private void configCallback() {
		this.rescaleUnit.update();
		enableOutputs();
		repaint();
	}

	private final InvalidationListener inputListener = (c) -> inputCallback();

	private void inputCallback() {
		if (this.input.isConnected()) {
			enableInputs(this.input);
		} else if (this.input_float.isConnected()) {
			enableInputs(this.input_float);
		} else {
			enableInputs(null);
		}
		repaint();
	}

	private void enableInputs(InputPort<? extends BufferedImage> input) {
		this.inputs.clear();

		enableSourceInputs(input);

		final Kernel.Precision p = getKernelPrecision();
		switch (p) {
			case FLOAT:
				this.kernelFloat.registerPorts(this.inputs);
				break;
			case DOUBLE:
				this.kernelDouble.registerPorts(this.inputs);
				break;
		}
	}

	private void enableAllInputs() {
		this.inputs.clear();

		enableSourceInputs(null);

		this.kernelFloat.registerPorts(this.inputs);
		this.kernelDouble.registerPorts(this.inputs);
	}

	private void enableSourceInputs(InputPort<? extends BufferedImage> input) {
		if (input == null || input.equals(this.input)) {
			this.inputs.put("buffered-image", this.input);
		}
		if (input == null || input.equals(this.input_float)) {
			this.inputs.put("buffered-matrix-float", this.input_float);
		}
	}

	private InputPort<? extends BufferedImage> getConnectedInput() {
		if (this.input_float.isConnected()) {
			return this.input_float;
		}
		return this.input;
	}

	private Kernel.Precision getKernelPrecision() {
		return EnumParameter.valueOf(
				this.kernelPrecision.get(),
				Kernel.Precision.class,
				Kernel.Precision.FLOAT
		);
	}

	private void enableOutputs() {
		this.outputs.clear();
		this.outputs.putAll(this.rescaleUnit.getEnabledOutputPorts());
	}

	private void enableAllOutputs() {
		this.outputs.clear();
		this.outputs.putAll(this.rescaleUnit.getAllOutputPorts());
	}

	@Override
	protected void resetOutputs() {
		for (OutputPort<? extends BufferedImage> output : this.rescaleUnit.getAllOutputPorts().values()) {
			output.setOutput(null);
		}
	}

	private boolean restoreOutputs(ProcessorContext context) {
		return restoreOutputs(context, null);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private boolean restoreOutputs(ProcessorContext context, BufferedImage convolvedImage) {
		final BufferedImage image;
		if (convolvedImage == null) {
			if (this.rescaleUnit.isBufferedMatrix()) {
				image = readBufferedMatrix(context, MATRIX_FILE);
			} else {
				image = readBufferedImage(context, IMAGE_FILE);
			}
		} else {
			image = convolvedImage;
		}

		if (image == null) {
			return false;
		}

		for (OutputPort output : this.rescaleUnit.getEnabledOutputPorts().values()) {
			output.setOutput(image);
		}

		if (!this.rescaleUnit.isBufferedMatrix()) {
			provideImageLayer(context, image);
		}

		return true;
	}

	@Override
	public boolean isConnected() {
		// check image source
		if (!getConnectedInput().isConnected()) {
			return false;
		}

		// check kernel (non-sep. kernel, or column and row vectors
		final Kernel.Precision p = getKernelPrecision();
		switch (p) {
			case FLOAT:
				if (!(this.kernelFloat.kernel.isConnected()
						|| (this.kernelFloat.columnVector.isConnected()
						&& this.kernelFloat.rowVector.isConnected()))) {
					return false;
				}
				break;

			case DOUBLE:
				if (!(this.kernelDouble.kernel.isConnected()
						|| (this.kernelDouble.columnVector.isConnected()
						&& this.kernelDouble.rowVector.isConnected()))) {
					return false;
				}
				break;
		}

		return true;
	}

	@Override
	public boolean isWaiting() {
		if (isReady()) {
			return false;
		}

		if (isPortWaiting(getConnectedInput())) {
			return true;
		}

		final Kernel.Precision p = getKernelPrecision();
		switch (p) {
			case FLOAT:
				if (isPortWaiting(this.kernelFloat.kernel)
						&& (isPortWaiting(this.kernelFloat.rowVector)
						|| isPortWaiting(this.kernelFloat.columnVector))) {
					return true;
				}
				break;

			case DOUBLE:
				if (isPortWaiting(this.kernelDouble.kernel)
						&& (isPortWaiting(this.kernelDouble.rowVector)
						|| isPortWaiting(this.kernelDouble.columnVector))) {
					return true;
				}
				break;
		}

		return isWaitingOnInputParams();
	}

	private boolean isPortWaiting(InputPort<?> input) {
		return !input.isConnected() || !input.getPortState().equals(Port.State.READY);
	}

	/**
	 * Processing configuration. Reads out and holds the convolution
	 * configuration parameters/objects.
	 */
	private static class ProcessConfig {

		final public Kernel.Precision precision;
		final public Kernel<?> kernel;
		final public Kernel<?> columnVector;
		final public ImagePadder.Type padderType;

		/**
		 * Creates a new process configuration.
		 *
		 * @param filter the convolution filter.
		 */
		public ProcessConfig(ConvolutionFilter filter) {
			this.precision = filter.getKernelPrecision();

			if (precision.equals(Kernel.Precision.DOUBLE)) {
				if (filter.kernelDouble.isSeparable()) {
					kernel = new DoubleKernel((DoubleMatrix) filter.kernelDouble.rowVector.getValue());
					columnVector = new DoubleKernel((DoubleMatrix) filter.kernelDouble.columnVector.getValue());
				} else {
					kernel = new DoubleKernel((DoubleMatrix) filter.kernelDouble.kernel.getValue());
					columnVector = null;
				}
			} else {
				if (filter.kernelFloat.isSeparable()) {
					kernel = new FloatKernel((FloatMatrix) filter.kernelFloat.rowVector.getValue());
					columnVector = new FloatKernel((FloatMatrix) filter.kernelFloat.columnVector.getValue());
				} else {
					kernel = new FloatKernel((FloatMatrix) filter.kernelFloat.kernel.getValue());
					columnVector = null;
				}
			}

			padderType = EnumParameter.valueOf(
					filter.padderOption.get(),
					ImagePadder.Type.class,
					ImagePadder.Type.REFLECTIVE
			);
		}
	}

	@Override
	public void process(ProcessorContext context) {
		try {
			if (!restoreOutputs(context)) {
				final InputPort<? extends BufferedImage> source = getConnectedInput();
				final BufferedImage src = source.getValue();
				cancelIfInterrupted(src);

				final ProcessConfig cfg = new ProcessConfig(this);
				final BufferedImage image = doProcess(context, src, cfg);
				cancelIfInterrupted(image);

				if (this.rescaleUnit.isBufferedMatrix()) {
					writeBufferedMatrix(context, (BufferedMatrix) image, MATRIX_FILE);
				} else {
					writeBufferedImage(context, image, IMAGE_FILE, IMAGE_FORMAT);
				}
				cancelIfInterrupted();

				restoreOutputs(context, image);
				cancelIfInterrupted();
			}
		} catch (InterruptedException ex) {
			reset(context);
		}
	}

	private BufferedImage doProcess(ProcessorContext context, BufferedImage src, ProcessConfig cfg) {
		final BufferedImage image;

		// We need to manually do the two passes if run in parallel, or
		// errors will be produced (see ConvolutionOp)
		if (cfg.columnVector != null) {
			// 2-pass convolution
			ConvolutionOp<?> op = getConvolutionOp(cfg.columnVector, cfg.padderType);
			final BufferedImage tmp = Filter.filter(
					context, op, src,
					getCompatibleDestImage(op, src)
			);

			op = getConvolutionOp(cfg.kernel, cfg.padderType);
			image = Filter.filter(
					context, op, tmp,
					getCompatibleDestImage(op, src)
			);
		} else {
			// 1-pass convolution
			final ConvolutionOp<?> op = getConvolutionOp(cfg.kernel, cfg.padderType);

			image = Filter.filter(
					context, op, src,
					getCompatibleDestImage(op, src)
			);
		}

		return image;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private ConvolutionOp<?> getConvolutionOp(Kernel kernel, ImagePadder.Type padderType) {
		return new ConvolutionOp<>(
				kernel,
				null,
				padderType.getInstance(),
				this.rescaleUnit.getAbs(),
				this.rescaleUnit.getGain(),
				this.rescaleUnit.getBias(),
				this.rescaleUnit.getMin(),
				this.rescaleUnit.getMax(),
				this.rescaleUnit.getPrecision()
		);
	}

	private BufferedImage getCompatibleDestImage(ConvolutionOp<?> op, BufferedImage src) {
		return op.createCompatibleDestImage(
				src.getWidth(),
				src.getHeight(),
				this.rescaleUnit.getPrecision(),
				this.rescaleUnit.numBands()
		);
	}

	private ProcessConfig previewCfg;

	@Override
	public void previewSetup(ProcessorContext context) {
		previewCfg = new ProcessConfig(this);
	}

	@Override
	public Image previewSource(ProcessorContext context) {
		final BufferedImage src = getConnectedInput().getValue();
		if (src instanceof BufferedMatrix) {
			return null;
		}
		return SwingFXUtils.toFXImage(src, null);
	}

	@Override
	public Image preview(ProcessorContext context, Rectangle bounds) {
		if (this.rescaleUnit.isBufferedMatrix()) {
			return null;
		}
		final BufferedImage src = getConnectedInput().getValue();
		final BufferedImage region = src.getSubimage(
				bounds.x,
				bounds.y,
				bounds.width,
				bounds.height
		);
		final BufferedImage preview = doProcess(context, region, previewCfg);
		return SwingFXUtils.toFXImage(preview, null);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, IMAGE_FILE);
		deleteFile(context, MATRIX_FILE);
		resetOutputs();
		resetLayer(context);
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.BLUR;
	}

}
