package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.BooleanMatrix;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.datastructures.Mask;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.awt.imaging.Filter;
import ch.unifr.diva.dip.awt.imaging.ops.RankOp;
import ch.unifr.diva.dip.awt.imaging.padders.ImagePadder;
import ch.unifr.diva.dip.awt.tools.MatrixEditor.MatrixShapeParameter;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.osgi.service.component.annotations.Component;

/**
 * A rank filter.
 */
@Component(service = Processor.class)
public class RankFilter extends ProcessableBase implements Previewable {

	private final static String IMAGE_FORMAT = "PNG";
	private final static String IMAGE_FILE = "ranked.png";
	private final static String MATRIX_FILE = "ranked.bmat";

	private final InputPort<BufferedImage> input;
	private final InputPort<BooleanMatrix> mask;
	private final OutputPort<BufferedImage> output;

	private final EnumParameter padderOption;
	private final EnumParameter rankOption;
	private final XorParameter maskOption;
	private final MatrixShapeParameter shape;

	/**
	 * Creates a new rank filter.
	 */
	public RankFilter() {
		super("Rank Filter");

		this.padderOption = new EnumParameter(
				"Edge handling", ImagePadder.Type.class, ImagePadder.Type.REFLECTIVE.name()
		);
		this.rankOption = new EnumParameter(
				"Ranking method", RankOp.Rank.class, RankOp.Rank.MEDIAN.name()
		);
		this.shape = new MatrixShapeParameter(3, 3, "");
		this.maskOption = new XorParameter(
				"Mask",
				Arrays.asList(
						this.shape.composite,
						new TextParameter("Use BooleanMatrix")
				)
		);
		this.parameters.put("padder", this.padderOption);
		this.parameters.put("rank", this.rankOption);
		this.parameters.put("mask", this.maskOption);

		this.input = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				true
		);
		this.mask = new InputPort<>(
				"Mask",
				new ch.unifr.diva.dip.api.datatypes.BooleanMatrix(),
				false
		);
		enableAllInputs();

		this.output = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
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
		repaint();
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

	/**
	 * Processing configuration. Reads out and holds the rank filter's
	 * configuration parameters/objects.
	 */
	private static class ProcessConfig {

		final public RankOp.Rank rank;
		final public ImagePadder.Type padderType;
		final public Mask mask;

		/**
		 * Creates a new process configuration.
		 *
		 * @param filter the rank filter.
		 */
		public ProcessConfig(RankFilter filter) {
			rank = EnumParameter.valueOf(
					filter.rankOption.get(),
					RankOp.Rank.class,
					RankOp.Rank.MEDIAN
			);
			padderType = EnumParameter.valueOf(
					filter.padderOption.get(),
					ImagePadder.Type.class,
					ImagePadder.Type.REFLECTIVE
			);
			mask = filter.getMask();
		}

	}

	@Override
	public void process(ProcessorContext context) {
		if (!restoreOutputs(context)) {
			final BufferedImage src = input.getValue();
			final ProcessConfig cfg = new ProcessConfig(this);
			final BufferedImage image = doProcess(context, src, cfg);

			if (isBufferedMatrix()) {
				writeBufferedMatrix(context, (BufferedMatrix) image, MATRIX_FILE);
			} else {
				writeBufferedImage(context, image, IMAGE_FILE, IMAGE_FORMAT);
			}

			restoreOutputs(context, image);
		}
	}

	private BufferedImage doProcess(ProcessorContext context, BufferedImage src, ProcessConfig cfg) {
		final RankOp op = new RankOp(cfg.rank, cfg.mask, cfg.padderType.getInstance());
		return Filter.filter(context, op, src, op.createCompatibleDestImage(src));
	}

	private ProcessConfig previewCfg;

	@Override
	public void previewSetup(ProcessorContext context) {
		previewCfg = new ProcessConfig(this);
	}

	@Override
	public Image previewSource(ProcessorContext context) {
		final BufferedImage src = input.getValue();
		if (src instanceof BufferedMatrix) {
			return null;
		}
		return SwingFXUtils.toFXImage(src, null);
	}

	@Override
	public Image preview(ProcessorContext context, Rectangle bounds) {
		if (isBufferedMatrix()) {
			return null;
		}
		final BufferedImage src = input.getValue();
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
		return MaterialDesignIcons.SORT;
	}

}
