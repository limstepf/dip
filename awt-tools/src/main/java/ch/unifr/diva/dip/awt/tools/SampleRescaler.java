package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.ops.RescaleOp;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EmptyParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.Transmutable;
import java.awt.image.BufferedImage;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

	private final RescaleUnit rescaleUnit;
	private final CompositeGrid configGrid;

	public SampleRescaler() {
		super("Sample Rescaler");

		this.rescaleUnit = new RescaleUnit();
		this.rescaleUnit.getBandConfig().addGridPaneViewHook((gp) -> {
			GridPane.setConstraints(gp, 0, 1, 2, 1);
		});

		this.configGrid = new CompositeGrid(
				new EmptyParameter(),
				this.rescaleUnit.getOutputConfig(),
				this.rescaleUnit.getBandConfig()
		);
		this.configGrid.setColumnWidthConstraints(0.5, 0.5);
		this.parameters.put("config", configGrid);

		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), false);
		this.input_float = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat(), false);

		enableAllInputs();
		enableAllOutputs();
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new SampleRescaler();
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
		this.rescaleUnit.getOutputConfig().property().addListener(configListener);
	}

	private final InvalidationListener configListener = (c) -> configCallback();

	private void configCallback() {
		this.rescaleUnit.update();
		enableOutputs();
		transmute();
	}

	private final InvalidationListener inputListener = (c) -> inputCallback();

	private void inputCallback() {
		if (this.input.isConnected()) {
			enableInput(this.input);
		} else if (this.input_float.isConnected()) {
			enableInput(this.input_float);
		} else {
			enableInput(null);
		}
		transmute();
	}

	private void enableAllInputs() {
		this.inputs.clear();

		enableInput(null);
	}

	private void enableInput(InputPort input) {
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
		for (OutputPort output : this.rescaleUnit.getAllOutputPorts().values()) {
			output.setOutput(null);
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
			if (this.rescaleUnit.isBufferedMatrix()) {
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

		for (OutputPort output : this.rescaleUnit.getEnabledOutputPorts().values()) {
			output.setOutput(image);
		}

		if (!this.rescaleUnit.isBufferedMatrix()) {
			provideImageLayer(context, image);
		}

		return true;
	}

	@Override
	public void process(ProcessorContext context) {
		if (!restoreOutputs(context)) {
			final InputPort<BufferedImage> source = getConnectedInput();
			final BufferedImage src = source.getValue();
			final RescaleOp op = new RescaleOp(
					this.rescaleUnit.getAbs(),
					this.rescaleUnit.getGain(),
					this.rescaleUnit.getBias(),
					this.rescaleUnit.getMin(),
					this.rescaleUnit.getMax(),
					this.rescaleUnit.getPrecision()
			);
			final BufferedImage rescaledImage = filter(
					context, op, src,
					op.createCompatibleDestImage(
							src.getWidth(),
							src.getHeight(),
							this.rescaleUnit.getPrecision(),
							this.rescaleUnit.numBands()
					)
			);
			if (this.rescaleUnit.isBufferedMatrix()) {
				writeBufferedMatrix(context, STORAGE_MAT, (BufferedMatrix) rescaledImage);
			} else {
				writeBufferedImage(context, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT, rescaledImage);
			}
			restoreOutputs(context, rescaledImage);

		}
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
