package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.awt.components.ColorPort;
import ch.unifr.diva.dip.awt.components.InputColorPort;
import ch.unifr.diva.dip.awt.imaging.Filter;
import ch.unifr.diva.dip.awt.imaging.SimpleColorModel;
import ch.unifr.diva.dip.awt.imaging.ops.ColorConvertOp;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import org.osgi.service.component.annotations.Component;

/**
 * Color space conversion filter. Can convert from and to all color models
 * defined in {@code SimpleColorModel}.
 *
 * @see ch.unifr.diva.dip.api.imaging.SimpleColorModel
 */
@Component(service = Processor.class)
public class ColorConverter extends ProcessableBase {

	private final EnumParameter cmSrc;
	private final EnumParameter cmDst;
	private final InputColorPort input;
	private final List<InputColorPort> inputColors;
	private final OutputColorPort output;
	private final List<OutputColorPort> outputColors;

	// extended output color ports by storage file and format
	private static class OutputColorPort extends ch.unifr.diva.dip.awt.components.OutputColorPort {

		public final String STORAGE_FILE;
		public final String STORAGE_FORMAT;

		public OutputColorPort() {
			this(null);
		}

		public OutputColorPort(SimpleColorModel cm) {
			super(cm);

			if (cm == null) {
				this.STORAGE_FILE = this.key + ".png";
				this.STORAGE_FORMAT = "png";
			} else {
				if (cm.requiresBufferedMatrix()) {
					this.STORAGE_FILE = this.key + ".bmat";
					this.STORAGE_FORMAT = "bmat";
				} else {
					this.STORAGE_FILE = this.key + ".png";
					this.STORAGE_FORMAT = "png";
				}
			}
		}
	}

	// title is used to publish the processor (as a service), while runtimeTitle
	// will reflect the current from/to information.
	private String runtimeTitle;

	@Override
	public String name() {
		return runtimeTitle;
	}

	private void updateTitle() {
		final InputColorPort in = ColorPort.getColorPort(this.cmSrc.get(), this.inputColors);
		final OutputColorPort out = ColorPort.getColorPort(this.cmDst.get(), this.outputColors);
		this.runtimeTitle = String.format(
				"%s to %s converter",
				in.cm.name(),
				out.cm.name()
		);
	}

	public ColorConverter() {
		super("Color Converter");

		this.runtimeTitle = this.name;
		this.inputColors = new ArrayList<>();
		this.outputColors = new ArrayList<>();

		// color-untyped buffered-image ports
		this.input = new InputColorPort();
		this.output = new OutputColorPort();

		for (SimpleColorModel cm : SimpleColorModel.values()) {
			final InputColorPort ic = new InputColorPort(cm);
			this.inputColors.add(ic);
			this.outputColors.add(new OutputColorPort(cm));
		}

		this.cmSrc = new EnumParameter("from", SimpleColorModel.class, SimpleColorModel.RGB.name());
		this.parameters.put("source", this.cmSrc);

		this.cmDst = new EnumParameter("to", SimpleColorModel.class, SimpleColorModel.RGB.name());
		this.parameters.put("destination", this.cmDst);

		// announce all in/ouput ports
		enableAllInputs();
		enableAllOutputs();
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new ColorConverter();
	}

	@Override
	public void init(ProcessorContext context) {
		enableInputs();
		enableOutputs();

		if (context != null) {
			restoreOutputs(context);
		}

		attachListeners();
		updateTitle();
	}

	private void attachListeners() {
		this.input.port.portStateProperty().addListener(inputListener);
		for (InputColorPort ic : this.inputColors) {
			ic.port.portStateProperty().addListener(inputListener);
		}
		this.cmSrc.property().addListener(inputListener);
		this.cmDst.property().addListener(outputListener);
	}

	private boolean restoreOutputs(ProcessorContext context) {
		return restoreOutputs(context, null);
	}

	public boolean restoreOutputs(ProcessorContext context, BufferedImage convertedImage) {
		final InputColorPort in = ColorPort.getColorPort(this.cmSrc.get(), this.inputColors);
		final OutputColorPort out = ColorPort.getColorPort(this.cmDst.get(), this.outputColors);

		final BufferedImage image;
		if (convertedImage == null) {
			if (in.cm.equals(out.cm)) {
				image = getSource().port.getValue(); // just by-pass
			} else if (out.cm.requiresBufferedMatrix()) {
				image = readBufferedMatrix(context, out.STORAGE_FILE);
			} else {
				image = readBufferedImage(context, out.STORAGE_FILE);
			}
		} else {
			image = convertedImage;
		}

		if (image == null) {
			return false;
		}

		out.port.setOutput(image);
		this.output.port.setOutput(image);

		if (!out.cm.requiresBufferedMatrix()) {
			provideImageLayer(context, image);
		}

		return true;
	}

	@Override
	public boolean isConnected() {
		return xorIsConnected(inputs().values());
	}

	private final InvalidationListener inputListener = (c) -> inputPortCallback();

	private void inputPortCallback() {
		InputColorPort connectedPort = null;

		if (this.input.port.isConnected()) {
			connectedPort = this.input;
		} else {
			for (InputColorPort ic : this.inputColors) {
				if (ic.port.isConnected()) {
					connectedPort = ic;
					break;
				}
			}
		}

		enableInputs(connectedPort);
		updateTitle();
		repaint();
	}

	private final InvalidationListener outputListener = (c) -> outputOptionCallback();

	private void outputOptionCallback() {
		enableOutputs();
		updateTitle();
		repaint();
	}

	private void disconnectPort(Port<?> port) {
		if (port.isConnected()) {
			port.disconnect();
		}
	}

	private void enableInputs() {
		enableInputs(null);
	}

	private void enableInputs(InputColorPort connected) {
		this.inputs.clear();

		final InputColorPort icFrom = ColorPort.getColorPort(this.cmSrc.get(), this.inputColors);

		if (connected == null || this.input.equals(connected)) {
			this.inputs.put(this.input.key, this.input.port);
		} else {
			disconnectPort(this.input.port);
		}

		for (InputColorPort ic : this.inputColors) {
			if ((connected == null && ic.equals(icFrom)) || ic.equals(connected)) {
				this.inputs.put(ic.key, ic.port);
			} else {
				disconnectPort(ic.port);
			}
		}
	}

	private void enableAllInputs() {
		this.inputs.clear();
		this.inputs.put(this.input.key, this.input.port);
		for (InputColorPort ic : this.inputColors) {
			this.inputs.put(ic.key, ic.port);
		}
	}

	private void enableOutputs() {
		this.outputs.clear();

		final OutputColorPort icTo = ColorPort.getColorPort(this.cmDst.get(), this.outputColors);
		this.outputs.put(this.output.key, this.output.port);

		for (OutputColorPort oc : this.outputColors) {
			if (oc.equals(icTo)) {
				this.outputs.put(oc.key, oc.port);
			} else {
				disconnectPort(oc.port);
			}
		}
	}

	private void enableAllOutputs() {
		this.outputs.clear();
		this.outputs.put(this.output.key, this.output.port);
		for (OutputColorPort oc : this.outputColors) {
			this.outputs.put(oc.key, oc.port);
		}
	}

	@Override
	protected void resetOutputs() {
		this.output.port.setOutput(null);
		for (OutputColorPort oc : this.outputColors) {
			oc.port.setOutput(null);
		}
	}

	@Override
	public void process(ProcessorContext context) {
		if (!restoreOutputs(context)) {
			final InputColorPort in = ColorPort.getColorPort(this.cmSrc.get(), this.inputColors);
			final OutputColorPort out = ColorPort.getColorPort(this.cmDst.get(), this.outputColors);
			final InputColorPort source = getSource();
			final BufferedImage src = source.port.getValue();
			final ColorConvertOp op = new ColorConvertOp(in.cm, out.cm);
			final BufferedImage image = Filter.filter(context, op, src, op.createCompatibleDestImage(src, out.cm));

			if (out.cm.requiresBufferedMatrix()) {
				writeBufferedMatrix(context, (BufferedMatrix) image, out.STORAGE_FILE);
			} else {
				writeBufferedImage(context, image, out.STORAGE_FILE, out.STORAGE_FORMAT);
			}

			restoreOutputs(context, image);
		}
	}

	private InputColorPort getSource() {
		if (this.input.port.isConnected()) {
			return this.input;
		}
		for (InputColorPort ic : this.inputColors) {
			if (ic.port.isConnected()) {
				return ic;
			}
		}
		return null;
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, this.output.STORAGE_FILE);
		for (OutputColorPort oc : this.outputColors) {
			deleteFile(context, oc.STORAGE_FILE);
		}
		resetOutputs();
		resetLayer(context);
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.PALETTE;
	}

}
