package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.imaging.ops.InvertOp;
import java.awt.image.BufferedImage;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Inverts all samples of a BufferedImage.
 */
@Component
@Service
public class SampleInverter extends ProcessableBase {

	private final InputPort<BufferedImage> input;
	private final OutputPort<BufferedImage> output;
	private final static String STORAGE_FILE = "inverted.png";
	private final static String STORAGE_FORMAT = "PNG";

	public SampleInverter() {
		super("Sample Inverter");

		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), true);
		this.inputs.put("buffered-image", input);

		this.output = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.outputs.put("buffered-image", output);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new SampleInverter();
	}

	@Override
	public void init(ProcessorContext context) {
		if (context != null) {
			final BufferedImage invertedImage = readBufferedImage(context, STORAGE_FILE);
			if (invertedImage != null) {
				this.output.setOutput(invertedImage);
				provideImageLayer(context, invertedImage);
			}
		}
	}

	@Override
	public void process(ProcessorContext context) {
		BufferedImage invertedImage = readBufferedImage(context, STORAGE_FILE);

		if (invertedImage == null) {
			final BufferedImage source = input.getValue();

			if (source instanceof BufferedMatrix) {
				// InvertOp can't handle BufferedMatrix; BITS and BYTES only
				// TODO: standard error handling!
				// ... or teach InvertOp how to invert floats (w.r.t. ranges on SimpleColorModel)
				return;
			}

			final InvertOp op = new InvertOp();
			invertedImage = op.filter(source, null);
			writeBufferedImage(context, STORAGE_FILE, STORAGE_FORMAT, invertedImage);
		}

		output.setOutput(invertedImage);
		provideImageLayer(context, invertedImage);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_FILE);
		resetOutputs();
		resetLayer(context);
	}

}
