package ch.unifr.diva.dip.awt.demo;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Simple image processing demo. Colors every nth pixel red.
 */
@Component
@Service
public class RedPixler extends ProcessableBase {

	private final InputPort<BufferedImage> input;
	private final OutputPort<BufferedImage> output;
	private final IntegerSliderParameter nthParameter;
	private final static String STORAGE_FILE = "red.png";
	private final static String STORAGE_FORMAT = "PNG";

	public RedPixler() {
		super("Red Pixler");

		this.nthParameter = new IntegerSliderParameter("band", 2, 1, 8);
		this.parameters.put("nth pixel", this.nthParameter);

		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), true);
		this.inputs.put("buffered-image", this.input);

		this.output = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.outputs.put("buffered-image", this.output);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new RedPixler();
	}
	
	@Override
	public void init(ProcessorContext context) {
		if (context != null) {
			final BufferedImage redImage = readBufferedImage(context, STORAGE_FILE);
			if (redImage != null) {
				this.output.setOutput(redImage);
				provideImageLayer(context, redImage);
			}
		}
	}

	@Override
	public void process(ProcessorContext context) {
		BufferedImage redImage = readBufferedImage(context, STORAGE_FILE);

		if (redImage == null) {
			final BufferedImage src = input.getValue();
			final int nth = this.nthParameter.get();

			// consider implementing a BufferedImageOp instead of twiddling
			// with samples/pixels in here...
			// @see package ch.unifr.diva.dip.api.utils.imaging.ops
			final BufferedImage dst = createBinaryDestImage(src);

			final WritableRaster srcRaster = src.getRaster();
			final WritableRaster dstRaster = dst.getRaster();

			// while we assume RGB, there might be a (useless) alpha channel there
			// (e.g. introduced by JavaFX Image to AWT BufferedImage converter)
			final int numBands = ImagingUtils.maxBands(src, dst);

			int i = 0;
			int[] pixel = new int[numBands];
			for (Location pt : new RasterScanner(src, false)) {
				pixel = srcRaster.getPixel(pt.col, pt.row, pixel);
				if ((i % nth) == 0) {
					pixel[0] = 255;
					pixel[1] = 0;
					pixel[2] = 0;
				}
				i++;
				dstRaster.setPixel(pt.col, pt.row, pixel);
			}

			redImage = dst;
			writeBufferedImage(context, STORAGE_FILE, STORAGE_FORMAT, redImage);
		}

		output.setOutput(redImage);
		provideImageLayer(context, redImage);
	}

	private BufferedImage createBinaryDestImage(BufferedImage src) {
		return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_FILE);
		resetOutputs();
		resetLayer(context);
	}
}
