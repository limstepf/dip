package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.openimaj.utils.OpenIMAJUtils;
import java.awt.image.BufferedImage;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.convolution.FSobel;
import org.osgi.service.component.annotations.Component;

/**
 * OpenIMAJ's sobel filter.
 */
@Component(service = Processor.class)
public class SobelFilter extends ProcessableBase {

	private final static String STORAGE_MAT_DX = "dx.bmat";
	private final static String STORAGE_MAT_DY = "dy.bmat";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedMatrix> input_float;
	private final XorInputPorts input_xor;
	private final OutputPort<BufferedMatrix> dx_float;
	private final OutputPort<BufferedMatrix> dy_float;
	private final IntegerSliderParameter band;
	private final ExpParameter sigma;

	/**
	 * Creates a new sobel filter.
	 */
	public SobelFilter() {
		super("Sobel Filter");

		this.band = new IntegerSliderParameter("Band", 1, 1, 4);
		parameters.put("band", band);
		this.sigma = new ExpParameter("Sigma", "1.0");
		parameters.put("sigma", sigma);

		this.input = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				false
		);
		this.input_float = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat(),
				false
		);
		this.input_xor = new XorInputPorts(this);
		input_xor.addPort("buffered-image", input);
		input_xor.addPort("buffered-matrix-float", input_float);
		input_xor.enableAllPorts();

		this.dx_float = new OutputPort<>(
				"dx",
				new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat()
		);
		this.dy_float = new OutputPort<>(
				"dy",
				new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat()
		);
		outputs.put("dx-float", dx_float);
		outputs.put("dy-float", dy_float);
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.BLUR_LINEAR;
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new SobelFilter();
	}

	@Override
	public void init(ProcessorContext context) {
		input_xor.init(context);
		if (context != null) {
			restoreOutputs(context);
		}
	}

	@Override
	public boolean isConnected() {
		return input_xor.isConnected();
	}

	@Override
	public void process(ProcessorContext context) {
		final InputPort<?> port = input_xor.getEnabledPort();
		if (port == null) {
			log.warn("no input port enabled");
			return;
		}

		final FImage fimage;
		if (port.equals(input_float)) {
			final BufferedMatrix mat = input_float.getValue();
			fimage = OpenIMAJUtils.toFImage(mat, getBand(mat));
		} else {
			final BufferedImage image = input.getValue();
			fimage = OpenIMAJUtils.toFImage(image, getBand(image));
		}

		float fsobel_sigma = sigma.getFloat();
		if (!Float.isFinite(fsobel_sigma) || fsobel_sigma < 0) {
			log.warn("invalid sigma: {}. Sigma is reset to 1.0f.", fsobel_sigma);
			fsobel_sigma = 1.0f;
		}
		final FSobel fsobel = new FSobel(fsobel_sigma);
		fsobel.analyseImage(fimage);

		final BufferedMatrix mat_dx = OpenIMAJUtils.toBufferedMatrix(fsobel.dx);
		final BufferedMatrix mat_dy = OpenIMAJUtils.toBufferedMatrix(fsobel.dy);
		ProcessorBase.writeBufferedMatrix(context, STORAGE_MAT_DX, mat_dx);
		ProcessorBase.writeBufferedMatrix(context, STORAGE_MAT_DY, mat_dy);
		setOutputs(context, mat_dx, mat_dy);
	}

	private <T extends BufferedImage> int getBand(T image) {
		final int n = image.getSampleModel().getNumBands();
		int b = band.get();
		if (b > n) {
			log.warn(
					"invalid band: {}. Image has only {} band(s). Selecting first band.",
					b,
					n
			);
			b = 0;
		}
		return b - 1;
	}

	protected void restoreOutputs(ProcessorContext context) {
		final BufferedMatrix dx = readBufferedMatrix(context, STORAGE_MAT_DX);
		final BufferedMatrix dy = readBufferedMatrix(context, STORAGE_MAT_DY);
		setOutputs(context, dx, dy);
	}

	protected void setOutputs(ProcessorContext context, BufferedMatrix dx, BufferedMatrix dy) {
		dx_float.setOutput(dx);
		dy_float.setOutput(dy);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_MAT_DX);
		deleteFile(context, STORAGE_MAT_DY);
		resetOutputs();
	}

}
