package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.BooleanMatrix;
import ch.unifr.diva.dip.api.datastructures.Mask;
import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.ops.RankOp;
import ch.unifr.diva.dip.api.imaging.padders.ImagePadder;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.Transmutable;
import ch.unifr.diva.dip.awt.tools.MatrixEditor.MatrixShapeParameter;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * A rank filter.
 */
@Component
@Service
public class RankFilter extends ProcessableBase implements Transmutable {

	private final static String IMAGE_FORMAT = "PNG";
	private final static String IMAGE_FILE = "ranked.png";
	private final static String MATRIX_FILE = "ranked.bmat";

	private final InputPort<BufferedImage> input;
	private final InputPort<BooleanMatrix> mask;
	private final OutputPort<BufferedImage> output;

	private final EnumParameter rankOption;
	private final EnumParameter padderOption;
	private final XorParameter maskOption;
	private final MatrixShapeParameter shape;

	public RankFilter() {
		super("Rank Filter");

		this.rankOption = new EnumParameter(
				"ranking method", RankOp.Rank.class, RankOp.Rank.MEDIAN.name()
		);
		this.padderOption = new EnumParameter(
				"edge handling", ImagePadder.Type.class, ImagePadder.Type.REFLECTIVE.name()
		);
		this.shape = new MatrixShapeParameter();
		this.maskOption = new XorParameter(
				"mask",
				Arrays.asList(
						this.shape.composite,
						new TextParameter("Use BooleanMatrix")
				)
		);
		this.parameters.put("padder", this.padderOption);
		this.parameters.put("rank", this.rankOption);
		this.parameters.put("mask", this.maskOption);

		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), true);
		this.mask = new InputPort(new ch.unifr.diva.dip.api.datatypes.BooleanMatrix(), false);
		enableAllInputs();

		this.output = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.outputs.put("buffered-image", this.output);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new RankFilter();
	}

	@Override
	public void init(ProcessorContext context) {
		maskCallback();

		if (context != null) {
			restoreOutputs(context);
		}

		this.maskOption.property().addListener(maskListener);
	}

	private final InvalidationListener maskListener = (c) -> maskCallback();

	private void maskCallback() {
		enableBooleanMatrix(useBooleanMatrix());
		transmute();
	}

	private void enableBooleanMatrix(boolean enable) {
		this.inputs.clear();
		this.inputs.put("buffered-image", this.input);
		if (enable) {
			this.inputs.put("boolean-matrix", this.mask);
		}
	}

	private void enableAllInputs() {
		this.inputs.clear();
		this.inputs.put("buffered-image", this.input);
		this.inputs.put("boolean-matrix", this.mask);
	}

	private boolean useBooleanMatrix() {
		return this.maskOption.get().selection == 1;
	}

	private BooleanMatrix getBooleanMatrixAsSpecified() {
		return new BooleanMatrix(
				this.shape.rows.get(),
				this.shape.columns.get()
		).fill(true);
	}

	private Mask getMask() {
		final BooleanMatrix mat;
		if (useBooleanMatrix()) {
			mat = this.mask.getValue();
		} else {
			mat = getBooleanMatrixAsSpecified();
		}

		return new Mask(mat);
	}

	private boolean isBufferedMatrix() {
		if (!this.input.isConnected()) {
			return false;
		}
		final BufferedImage src = this.input.getValue();
		if (src == null) {
			return false;
		}
		return src instanceof BufferedMatrix;
	}

	private boolean restoreOutputs(ProcessorContext context) {
		return restoreOutputs(context, null);
	}

	private boolean restoreOutputs(ProcessorContext context, BufferedImage rankedImage) {
		final boolean isBufferedMatrix = isBufferedMatrix();

		final BufferedImage image;
		if (rankedImage == null) {
			if (isBufferedMatrix) {
				image = readBufferedMatrix(context, MATRIX_FILE);
			} else {
				image = readBufferedImage(context, IMAGE_FILE);
			}
		} else {
			image = rankedImage;
		}

		if (image == null) {
			return false;
		}

		this.output.setOutput(image);

		if (!isBufferedMatrix) {
			provideImageLayer(context, image);
		}

		return true;
	}

	@Override
	public boolean isConnected() {
		if (!this.input.isConnected()) {
			return false;
		}

		if (!useBooleanMatrix()) {
			return true;
		}

		return this.mask.isConnected();
	}

	@Override
	public void process(ProcessorContext context) {
		if (!restoreOutputs(context)) {
			final BufferedImage src = input.getValue();
			final RankOp.Rank rank = EnumParameter.valueOf(
					this.rankOption.get(),
					RankOp.Rank.class,
					RankOp.Rank.MEDIAN
			);
			final ImagePadder.Type padderType = EnumParameter.valueOf(
					this.padderOption.get(),
					ImagePadder.Type.class,
					ImagePadder.Type.REFLECTIVE
			);

			final RankOp op = new RankOp(rank, getMask(), padderType.getInstance());
			final BufferedImage image = filter(
					context, op, src, op.createCompatibleDestImage(src)
			);

			if (isBufferedMatrix()) {
				writeBufferedMatrix(context, MATRIX_FILE, (BufferedMatrix) image);
			} else {
				writeBufferedImage(context, IMAGE_FILE, IMAGE_FORMAT, image);
			}

			restoreOutputs(context, image);
		}
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, IMAGE_FILE);
		deleteFile(context, MATRIX_FILE);
		resetOutputs();
		resetLayer(context);
	}

	private final BooleanProperty transmuteProperty = new SimpleBooleanProperty();

	@Override
	public BooleanProperty transmuteProperty() {
		return this.transmuteProperty;
	}
}
