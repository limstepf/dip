package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EmptyParameter;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.awt.imaging.Filter;
import ch.unifr.diva.dip.awt.imaging.ops.MergeOp;
import ch.unifr.diva.dip.awt.imaging.ops.NullOp.SamplePrecision;
import static ch.unifr.diva.dip.awt.tools.RescaleUnit.Band.getHeaderRows;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import org.osgi.service.component.annotations.Component;

/**
 * Band merger.
 */
@Component(service = Processor.class)
public class BandMerger extends ProcessableBase {

	/**
	 * Available input types.
	 */
	public enum InputType {

		UNTYPED,
		BINARY,
		GRAY,
		FLOAT;
	}

	/**
	 * M(erger) Band. Extended rescale unit band by input type.
	 */
	public static class MBand extends RescaleUnit.Band {

		protected EnumParameter inputType;
		protected final InputPort<BufferedImage> input;
		protected final InputPort<BufferedImage> input_binary;
		protected final InputPort<BufferedImage> input_gray;
		protected final InputPort<BufferedMatrix> input_float;
		protected final List<InputPort<? extends BufferedImage>> inputs;
		protected final List<String> input_keys;

		/**
		 * Creates a new band.
		 * @param num the band number (starting with 1).
		 * @param extraParameter the extra input type parameter.
		 */
		public MBand(int num, EnumParameter extraParameter) {
			super(
					num,
					extraParameter,
					Arrays.asList(
							0.05, 0.15, 0.05, 0.25, 0.25, 0.15, 0.15
					)
			);
			this.inputType = extraParameter;
			final String portLabel = "Band " + num;
			this.input = new InputPort<>(
					portLabel,
					new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
					false
			);
			this.input_binary = new InputPort<>(
					portLabel,
					new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary(),
					false
			);
			this.input_gray = new InputPort<>(
					portLabel,
					new ch.unifr.diva.dip.api.datatypes.BufferedImageGray(),
					false
			);
			this.input_float = new InputPort<>(
					portLabel,
					new ch.unifr.diva.dip.api.datatypes.BufferedImageFloat(),
					false
			);
			this.inputs = Arrays.asList(
					input,
					input_binary,
					input_gray,
					input_float
			);
			this.input_keys = Arrays.asList(
					"buffered-image-" + num,
					"buffered-image-binary-" + num,
					"buffered-image-gray-" + num,
					"buffered-matrix-float-" + num
			);
		}

		public InputType getSelectedType() {
			return inputType.getEnumValue(InputType.class);
		}

		public String getPortKey(InputPort<? extends BufferedImage> port) {
			final int idx = inputs.indexOf(port);
			return input_keys.get(idx);
		}

		public InputPort<? extends BufferedImage> getSelectedPort() {
			switch (getSelectedType()) {
				case BINARY:
					return input_binary;
				case GRAY:
					return input_gray;
				case FLOAT:
					return input_float;
				case UNTYPED:
				default:
					return input;
			}
		}

		public List<InputPort<? extends BufferedImage>> getPorts() {
			return inputs;
		}

		public List<String> getPortKeys() {
			return input_keys;
		}

		public boolean isConnected() {
			return getSelectedPort().isConnected();
		}

		public BufferedImage getValue() {
			return getSelectedPort().getValue();
		}

		@Override
		public List<Parameter<?>> getHeaderRows() {
			return getHeaderRows(
					new LabelParameter("input"),
					gridPercentWidths
			);
		}

		protected static EnumParameter newInputTypeParameter() {
			final EnumParameter p = new EnumParameter(
					"",
					InputType.class,
					InputType.GRAY.name()
			);
			p.addComboBoxViewHook((c) -> {
				c.getStyleClass().add("dip-small");
				GridPane.setMargin(c, new Insets(0, 10, 0, 0));
			});
			return p;
		}

	}

	private final static String STORAGE_IMAGE = "merged.png";
	private final static String STORAGE_IMAGE_FORMAT = "PNG";
	private final static String STORAGE_MAT = "merged.bmat";

	private final RescaleUnit rescaleUnit;
	private final CompositeGrid configGrid;
	private final List<MBand> mbands;

	/**
	 * Creates a new band merger.
	 */
	public BandMerger() {
		super("Band Merger");

		this.mbands = Arrays.asList(
				new MBand(1, MBand.newInputTypeParameter()),
				new MBand(2, MBand.newInputTypeParameter()),
				new MBand(3, MBand.newInputTypeParameter()),
				new MBand(4, MBand.newInputTypeParameter())
		);
		this.rescaleUnit = new RescaleUnit(
				mbands,
				false,
				true,
				true,
				true
		);
		rescaleUnit.getBandConfig().addGridPaneViewHook((gp) -> {
			GridPane.setConstraints(gp, 0, 1, 2, 1);
		});

		this.configGrid = new CompositeGrid(
				new EmptyParameter(),
				this.rescaleUnit.getOutputConfig(),
				this.rescaleUnit.getBandConfig()
		);
		configGrid.setColumnWidthConstraints(0.5, 0.5);
		this.parameters.put("config", configGrid);

		enableAllInputs();
		enableAllOutputs();
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new BandMerger();
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.BUFFER;
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
		for (MBand b : mbands) {
			b.inputType.property().addListener(inputListener);
		}
		this.rescaleUnit.getOutputConfig().property().addListener(configListener);
	}

	private final InvalidationListener configListener = (c) -> configCallback();

	private void configCallback() {
		this.rescaleUnit.update();
		enableInputs();
		enableOutputs();
		repaint();
	}

	private final InvalidationListener inputListener = (c) -> inputCallback();

	private void inputCallback() {
		enableInputs();
		repaint();
	}

	private void enableAllInputs() {
		this.inputs.clear();
		for (MBand b : mbands) {
			for (int i = 0, n = b.input_keys.size(); i < n; i++) {
				this.inputs.put(
						b.input_keys.get(i),
						b.inputs.get(i)
				);
			}
		}
	}

	private void enableInputs() {
		this.inputs.clear();
		for (int i = 0, n = rescaleUnit.numBands(); i < n; i++) {
			final MBand b = mbands.get(i);
			final InputPort<? extends BufferedImage> port = b.getSelectedPort();
			this.inputs.put(
					b.getPortKey(port),
					port
			);
		}
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
		for (OutputPort<?> output : this.rescaleUnit.getAllOutputPorts().values()) {
			output.setOutput(null);
		}
	}

	@Override
	public boolean isConnected() {
		for (int i = 0, n = rescaleUnit.numBands(); i < n; i++) {
			if (!mbands.get(i).isConnected()) {
				return false;
			}
		}
		return true;
	}

	private boolean restoreOutputs(ProcessorContext context) {
		return restoreOutputs(context, null);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private boolean restoreOutputs(ProcessorContext context, BufferedImage mergedImage) {
		final BufferedImage image;
		if (mergedImage == null) {
			if (context == null) {
				return false;
			}
			if (this.rescaleUnit.isBufferedMatrix()) {
				image = readBufferedMatrix(context, STORAGE_MAT);
			} else {
				image = readBufferedImage(context, STORAGE_IMAGE);
			}
		} else {
			image = mergedImage;
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
		try {
			if (!restoreOutputs(context)) {
				final int n = rescaleUnit.numBands();
				final int m = n - 1;
				final BufferedImage source_right = mbands.get(m).getValue();
				final BufferedImage[] sources = new BufferedImage[m];
				for (int i = 0; i < m; i++) {
					sources[i] = mbands.get(i).getValue();
					cancelIfInterrupted(sources[i]);
				}
				final boolean isMat = this.rescaleUnit.isBufferedMatrix();
				final BufferedImage mergedImage = doMerge(context, sources, source_right, n, isMat);
				cancelIfInterrupted(mergedImage);

				if (isMat) {
					writeBufferedMatrix(context, (BufferedMatrix) mergedImage, STORAGE_MAT);
				} else {
					writeBufferedImage(context, mergedImage, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT);
				}
				cancelIfInterrupted();

				restoreOutputs(context, mergedImage);
				cancelIfInterrupted();
			}
		} catch (InterruptedException ex) {
			reset(context);
		}
	}

	private BufferedImage doMerge(ProcessorContext context, BufferedImage[] sources, BufferedImage source_right, int numBands, boolean isMat) {
		final SamplePrecision precision = isMat ? SamplePrecision.FLOAT : SamplePrecision.BYTE;
		final MergeOp op = new MergeOp(
				sources,
				precision,
				numBands,
				this.rescaleUnit.getAbs(),
				this.rescaleUnit.getGain(),
				this.rescaleUnit.getBias(),
				this.rescaleUnit.getMin(),
				this.rescaleUnit.getMax()
		);
		final Rectangle bounds = source_right.getRaster().getBounds().intersection(
				MergeOp.getIntersectionBounds(sources)
		);
		return Filter.filter(
				context,
				op,
				source_right,
				op.createCompatibleDestImage(
						(int) bounds.getWidth(),
						(int) bounds.getHeight(),
						precision,
						numBands
				)
		);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_IMAGE);
		deleteFile(context, STORAGE_MAT);
		resetOutputs();
		resetLayer(context);
	}

}
