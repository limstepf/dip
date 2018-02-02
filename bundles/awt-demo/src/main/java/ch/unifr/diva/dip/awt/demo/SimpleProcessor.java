package ch.unifr.diva.dip.awt.demo;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import static ch.unifr.diva.dip.api.services.ProcessorBase.deleteFile;
import static ch.unifr.diva.dip.api.services.ProcessorBase.provideImageLayer;
import static ch.unifr.diva.dip.api.services.ProcessorBase.readBufferedImage;
import static ch.unifr.diva.dip.api.services.ProcessorBase.resetLayer;
import static ch.unifr.diva.dip.api.services.ProcessorBase.writeBufferedImage;
import ch.unifr.diva.dip.awt.imaging.Filter;
import ch.unifr.diva.dip.awt.imaging.ops.NullOp;
import ch.unifr.diva.dip.awt.imaging.ops.SimpleTileParallelizable;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.concurrent.ThreadLocalRandom;
import org.osgi.service.component.annotations.Component;

/**
 * A simple processor.
 */
@Component(service = Processor.class)
public class SimpleProcessor extends ProcessableBase {

	private final static String STORAGE_FILE = "simple.png";
	private final static String STORAGE_FORMAT = "PNG";
	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_gray;
	private final XorInputPorts input_xor;
	private final OutputPort<BufferedImage> output;
	private final OutputPort<BufferedImage> output_gray;
	private final IntegerSliderParameter band;

	/**
	 * Creates a new simple processor.
	 */
	public SimpleProcessor() {
		super("Simple Processor");

		this.band = new IntegerSliderParameter("Band", 1, 1, 4);
		parameters.put("band", band);

		this.input = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				false
		);
		this.input_gray = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageGray(),
				false
		);
		this.input_xor = new XorInputPorts(this);
		input_xor.addPort("buffered-image", input);
		input_xor.addPort("buffered-image-gray", input_gray);
		input_xor.enableAllPorts();

		this.output = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage()
		);
		this.output_gray = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageGray()
		);
		outputs.put("buffered-image", output);
		outputs.put("buffered-image-binary", output_gray);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new SimpleProcessor();
	}

	@Override
	public void init(ProcessorContext context) {
		input_xor.init(context);

		if (context != null) {
			provideResult(context, null);
		}
	}

	@Override
	public boolean isConnected() {
		return input_xor.isConnected();
	}

	@Override
	public void process(ProcessorContext context) {
		try {
			final InputPort<BufferedImage> port = getConnectedPort();
			cancelIfInterrupted(port);

			final BufferedImage src = port.getValue();
			cancelIfInterrupted(src);

			final SimpleOp op = new SimpleOp(band.get() - 1);
			final BufferedImage result = Filter.filter(context, op, src, op.createCompatibleDestImage(src));
			cancelIfInterrupted(result);

			writeBufferedImage(context, result, STORAGE_FILE, STORAGE_FORMAT);
			cancelIfInterrupted();

			provideResult(context, result);
			cancelIfInterrupted();
		} catch (InterruptedException ex) {
			reset(context);
		}
	}

	protected void provideResult(ProcessorContext context, BufferedImage result) {
		if (result == null) {
			result = readBufferedImage(context, STORAGE_FILE);
		}

		this.output.setOutput(result);
		this.output_gray.setOutput(result);

		if (result != null) {
			provideImageLayer(context, result);
		}
	}

	protected InputPort<BufferedImage> getConnectedPort() {
		if (input.equals(input_xor.getEnabledPort())) {
			return input;
		}
		return input_gray;
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_FILE);
		resetOutputs();
		resetLayer(context);
	}

	/**
	 * A simple operation.
	 */
//	public static class SimpleOp extends NullOp {
	public static class SimpleOp extends NullOp implements SimpleTileParallelizable {

		final int selectedBand;

		/**
		 * Creates a new simple operation.
		 */
		public SimpleOp() {
			this(0);
		}

		/**
		 * Creates a new simple operation.
		 *
		 * @param band the band to process in case of a multi-banded image.
		 */
		public SimpleOp(int band) {
			this.selectedBand = band;
		}

		@Override
		public BufferedImage filter(BufferedImage src, BufferedImage dst) {
			if (dst == null) {
				dst = createCompatibleDestImage(src);
			}

			final int numBands = src.getRaster().getNumBands();
			final int band = Math.min(numBands - 1, selectedBand);

			final WritableRaster srcRaster = src.getRaster();
			final WritableRaster dstRaster = dst.getRaster();

			int sample;
			for (Location pt : new RasterScanner(dst, false)) {
				sample = srcRaster.getSample(pt.col, pt.row, band);
				dstRaster.setSample(
						pt.col,
						pt.row,
						0,
						// Math.sqrt(sample / 255.0) * 255.0
						heavyComputation(sample)
				);
			}

			return dst;
		}

		protected int heavyComputation(int sample) {
			float sum = 0;
			for (int i = 0; i < sample; i++) {
				// sum += (i%2 == 0) ? 1.0 : Math.random();
				sum += (i % 2 == 0) ? 1.0 : ThreadLocalRandom.current().nextFloat();
			}
			return (int) sum;
		}

		@Override
		public BufferedImage createCompatibleDestImage(BufferedImage src) {
			return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		}

	}

}
