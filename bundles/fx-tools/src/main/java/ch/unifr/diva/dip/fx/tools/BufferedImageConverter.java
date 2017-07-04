package ch.unifr.diva.dip.fx.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.osgi.service.component.annotations.Component;

/**
 * Java AWT BufferedImage converter. Converts an AWT BufferedImage into a JavaFX
 * image.
 */
@Component(service = Processor.class)
public class BufferedImageConverter extends ProcessableBase {

	private final InputPort<BufferedImage> input;
	private final OutputPort<Image> output;
	private final static String STORAGE_FILE = "image.png";
	private final static String STORAGE_FORMAT = "PNG";

	public BufferedImageConverter() {
		super("BufferedImage Converter");

		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), true);
		inputs.put("buffered-image", input);

		this.output = new OutputPort(new ch.unifr.diva.dip.api.datatypes.FxImage());
		outputs.put("image", output);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new BufferedImageConverter();
	}

	@Override
	public void init(ProcessorContext context) {
		if (context != null) {
			final Image image = readImage(context, STORAGE_FILE);
			if (image != null) {
				this.output.setOutput(image);
			}
		}
	}

	@Override
	public void process(ProcessorContext context) {
		Image image = readImage(context, STORAGE_FILE);

		if (image == null) {
			final BufferedImage source = input.getValue();
			image = SwingFXUtils.toFXImage(source, null);

			writeImage(context, STORAGE_FILE, STORAGE_FORMAT, image);
		}

		this.output.setOutput(image);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_FILE);
		resetOutputs();
	}

}
