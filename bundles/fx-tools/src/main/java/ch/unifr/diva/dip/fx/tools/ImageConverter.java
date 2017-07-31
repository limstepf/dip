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
 * JavaFX Image converter. Converts a JavaFX image into an AWT BufferedImage.
 */
@Component(service = Processor.class)
public class ImageConverter extends ProcessableBase {

	private final InputPort<Image> input;
	private final OutputPort<BufferedImage> output;
	private final static String STORAGE_FILE = "image.png";
	private final static String STORAGE_FORMAT = "PNG";

	public ImageConverter() {
		super("Image Converter");

		this.input = new InputPort<>(new ch.unifr.diva.dip.api.datatypes.FxImage(), true);
		inputs.put("image", input);

		this.output = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		outputs.put("buffered-image", output);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new ImageConverter();
	}

	@Override
	public void init(ProcessorContext context) {
		if (context != null) {
			final BufferedImage image = readBufferedImage(context, STORAGE_FILE);
			if (image != null) {
				this.output.setOutput(image);
			}
		}
	}

	@Override
	public void process(ProcessorContext context) {
		BufferedImage image = readBufferedImage(context, STORAGE_FILE);

		if (image == null) {
			final Image source = input.getValue();
			image = SwingFXUtils.fromFXImage(source, null);

			writeBufferedImage(context, image, STORAGE_FILE, STORAGE_FORMAT);
		}

		this.output.setOutput(image);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_FILE);
		resetOutputs();
	}
}
