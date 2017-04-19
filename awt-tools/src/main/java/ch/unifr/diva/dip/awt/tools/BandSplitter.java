package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.color.ColorPort;
import ch.unifr.diva.dip.api.parameters.BooleanParameter;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.ui.SelectionListCellFactory;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.Transmutable;
import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import ch.unifr.diva.dip.api.imaging.ops.BandExtractOp;
import ch.unifr.diva.dip.api.imaging.ops.ColorBandVisualizationOp;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.MaterialDesignIcons;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Band/channel splitter.
 */
@Component
@Service
public class BandSplitter extends ProcessableBase implements Transmutable {

	private enum LayerOption {

		/**
		 * Provides no layers/visualization.
		 */
		NONE,
		/**
		 * Shows band as is in grayscale. Only works for BufferedImages up to
		 * byte sample precision (no floats/BufferedMatrix).
		 */
		GRAYSCALE,
		/**
		 * Provides special band visualization defined by the SimpleColorModel.
		 */
		VISUALIZATION
	}

	private final EnumParameter cmSrc;
	private final EnumParameter provideLayers;
	private final SelectionListCellFactory provideLayersCellFactory;
	private final BooleanParameter processAllBands;
	private final static String STORAGE_FORMAT = "PNG";

	private final static String ANY_OPTION = "ANY";
	private final InputColorPort input;
	private final List<InputColorPort> inputColors;

	// extended input color ports by band labels
	private static class InputColorPort extends ch.unifr.diva.dip.api.components.color.InputColorPort {

		public InputColorPort() {
			super();
		}

		public InputColorPort(SimpleColorModel cm) {
			super(cm);
		}

		public String bandLabel(int band) {
			if (this.cm == null) {
				return String.format("Band %d", band);
			}
			return String.format("Band %d: %s", band, cm.bandDescription(band));
		}
	}

	private final List<OutputBand> outputBands;

	private static class OutputBand {

		public final int num;
		public final String key_bi;
		public final String key_gray;
		public final String key_float;
		public final OutputPort<BufferedImage> output_bi;
		public final OutputPort<BufferedImage> output_gray;
		public final OutputPort<BufferedMatrix> output_float;
		public final String GRAY_FILE;
		public final String FLOAT_FILE;
		public final String VIS_FILE;

		public OutputBand(int num) {
			this.num = num;
			this.key_bi = "band" + num;
			this.key_gray = this.key_bi + "-gray";
			this.key_float = this.key_bi + "-float";
			this.GRAY_FILE = this.key_bi + ".png";
			this.FLOAT_FILE = this.key_bi + ".bmat";
			this.VIS_FILE = this.key_bi + "-vis.png";
			this.output_bi = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
			this.output_gray = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImageGray());
			this.output_float = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat());
		}

		public boolean isConnected() {
			return output_bi.isConnected() || output_gray.isConnected() || output_float.isConnected();
		}
	}

	public BandSplitter() {
		super("Band Splitter");

		this.cmSrc = new EnumParameter("source", SimpleColorModel.class, ANY_OPTION);
		this.cmSrc.addOption(ANY_OPTION, "Any/unknown");
		this.cmSrc.addComboBoxViewHook((combo) -> {
			final SelectionListCellFactory cf = new SelectionListCellFactory();
			cf.addSeparator(0);
			combo.setCellFactory(cf);
		});

		this.parameters.put("source", cmSrc);

		this.provideLayers = new EnumParameter("Provide layers", LayerOption.class, LayerOption.NONE.name());
		this.provideLayersCellFactory = new SelectionListCellFactory<>();
		this.provideLayers.addComboBoxViewHook((combo) -> {
			combo.setCellFactory(this.provideLayersCellFactory);
		});
		this.parameters.put("provide-layers", this.provideLayers);
		this.processAllBands = new BooleanParameter("Process all bands", false);
		this.parameters.put("process-all-bands", this.processAllBands);

		this.input = new InputColorPort();
		this.inputColors = new ArrayList<>();
		for (SimpleColorModel cm : SimpleColorModel.values()) {
			final InputColorPort ic = new InputColorPort(cm);
			this.inputColors.add(ic);
		}
		enableAllInputs();

		this.outputBands = new ArrayList<>(4);
		for (int i = 1; i < 5; i++) {
			this.outputBands.add(new OutputBand(i));
		}
		enableAllOutputs();
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new BandSplitter();
	}

	@Override
	public void init(ProcessorContext context) {
		if (context == null || context.getObjects().get("isBufferedMatrix") == null) {
			inputCallback();
			attachInputListeners();
			return;
		}

		final boolean isBufferedMatrix = (boolean) context.getObjects().get("isBufferedMatrix");

		// restore bands
		for (OutputBand band : this.outputBands) {
			if (isBufferedMatrix) {
				final BufferedMatrix mat = readBufferedMatrix(context, band.FLOAT_FILE);
				if (mat != null) {
					band.output_bi.setOutput(mat);
					band.output_float.setOutput(mat);
				}
			} else {
				final BufferedImage image = readBufferedImage(context, band.GRAY_FILE);
				if (image != null) {
					band.output_bi.setOutput(image);
					band.output_gray.setOutput(image);
				}
			}
		}

		// restore visualization
		final InputColorPort source = getSource();
		final LayerOption opt = EnumParameter.valueOf(
				this.provideLayers.get(),
				LayerOption.class,
				LayerOption.NONE
		);
		final int n = this.outputBands.size();
		final boolean provideAll = this.processAllBands.get();
		final boolean[] processBand = new boolean[n];

		for (int i = 0; i < n; i++) {
			final OutputBand band = this.outputBands.get(i);

			if (provideAll || band.isConnected()) {
				processBand[i] = true;
			}
		}

		final BufferedImage[] images;

		switch (opt) {
			case GRAYSCALE:
				if (!isBufferedMatrix) {
					images = new BufferedImage[n];
					for (int i = 0; i < n; i++) {
						if (processBand[i]) {
							final OutputBand band = this.outputBands.get(i);
							images[i] = band.output_bi.getOutput();
						}
					}
				} else {
					images = null;
				}
				break;

			case VISUALIZATION:
				images = new BufferedImage[n];
				for (int i = 0; i < n; i++) {
					if (processBand[i]) {
						final OutputBand band = this.outputBands.get(i);
						images[i] = readBufferedImage(context, band.VIS_FILE);
					}
				}
				break;

			case NONE:
			default:
				images = null;
				break;
		}

		if (images != null) {
			provideBandLayers(context, source, images);
		}

		inputCallback();
		attachInputListeners();
	}

	private void provideBandLayers(ProcessorContext context, InputColorPort source, BufferedImage[] images) {
		for (int i = images.length - 1; i >= 0; i--) {
			if (images[i] != null) {
				provideImageLayer(context, images[i], source.bandLabel(i));
			}
		}
	}

	private void attachInputListeners() {
		this.cmSrc.property().addListener(this.inputListener);
		this.input.port.portStateProperty().addListener(this.inputListener);
		for (InputColorPort ic : this.inputColors) {
			ic.port.portStateProperty().addListener(this.inputListener);
		}
	}

	private final InvalidationListener inputListener = (c) -> inputCallback();

	private void inputCallback() {
		final String opt = this.cmSrc.get();
		final SimpleColorModel cm;

		if (opt.equals(ANY_OPTION)) {
			cm = null;
			// grayscale is possible unless this is a BufferedMatrix
			this.provideLayersCellFactory.setDisable(
					this.provideLayers.getIndex(LayerOption.GRAYSCALE.name()),
					false
			);
			// but visu. requires a color model
			this.provideLayersCellFactory.setDisable(
					this.provideLayers.getIndex(LayerOption.VISUALIZATION.name()),
					true
			);
			if (this.provideLayers.get().equals(LayerOption.VISUALIZATION.name())) {
				this.provideLayers.set(LayerOption.NONE.name());
			}
		} else {
			cm = EnumParameter.valueOf(
					opt,
					SimpleColorModel.class,
					SimpleColorModel.RGB
			);
			// BufferedMatrix (or its bands) can't be directly displayed
			this.provideLayersCellFactory.setDisable(
					this.provideLayers.getIndex(LayerOption.GRAYSCALE.name()),
					cm.requiresBufferedMatrix()
			);
			if (cm.requiresBufferedMatrix() && this.provideLayers.get().equals(LayerOption.GRAYSCALE.name())) {
				this.provideLayers.set(LayerOption.NONE.name());
			}
			this.provideLayersCellFactory.setDisable(
					this.provideLayers.getIndex(LayerOption.VISUALIZATION.name()),
					false
			);
		}

		enableInputs(opt, cm);
		enableOutputs(cm);
		transmute();
	}

	private void enableInputs(String opt, SimpleColorModel cm) {
		inputs.clear();

		InputColorPort connected = this.input.port.isConnected() ? this.input : null;

		if (opt.equals(ANY_OPTION)) {
			inputs.put(this.input.key, this.input.port);
		} else {
			if (connected == null) {
				InputColorPort ic = ColorPort.getColorPort(cm.name(), this.inputColors);
				if (ic.port.isConnected()) {
					connected = ic;
				} else {
					inputs.put(this.input.key, this.input.port);
					inputs.put(ic.key, ic.port);
				}
			}
			if (connected != null) {
				inputs.put(connected.key, connected.port);
			}
		}
	}

	private void enableAllInputs() {
		inputs.clear();
		inputs.put(this.input.key, this.input.port);

		for (InputColorPort ic : this.inputColors) {
			inputs.put(ic.key, ic.port);
		}
	}

	private void enableAllOutputs() {
		outputs.clear();
		for (int i = 0, n = this.outputBands.size(); i < n; i++) {
			final OutputBand band = this.outputBands.get(i);
			outputs.put(band.key_bi, band.output_bi);
			outputs.put(band.key_float, band.output_float);
			outputs.put(band.key_gray, band.output_gray);
		}
	}

	@Override
	protected void resetOutputs() {
		for (int i = 0, n = this.outputBands.size(); i < n; i++) {
			final OutputBand band = this.outputBands.get(i);
			band.output_bi.setOutput(null);
			band.output_float.setOutput(null);
			band.output_gray.setOutput(null);
		}
	}

	private void enableOutputs(SimpleColorModel cm) {
		outputs.clear();

		final int bands = (cm == null) ? 4 : cm.numBands();

		for (int i = 0, n = this.outputBands.size(); i < n; i++) {
			final OutputBand band = this.outputBands.get(i);
			if (bands >= band.num) {
				outputs.put(band.key_bi, band.output_bi);

				if (cm != null) {
					if (cm.requiresBufferedMatrix()) {
						outputs.put(band.key_float, band.output_float);
					} else {
						outputs.put(band.key_gray, band.output_gray);
					}
				}
			}
		}
	}

	@Override
	public boolean isConnected() {
		return xorIsConnected(inputs().values());
	}

	@Override
	public void process(ProcessorContext context) {
		final InputColorPort source = getSource(); // untyped or color typed input port
		final BufferedImage src = source.port.getValue();
		final int cmBands = (source.cm == null) ? 4 : source.cm.numBands();
		final int srcBands = src.getColorModel().getNumComponents();
		final int numBands = Math.min(cmBands, srcBands);
		final WritableRaster srcRaster = src.getRaster();
		// source.cm is null if untyped port is used!
		final LayerOption opt = EnumParameter.valueOf(
				this.provideLayers.get(),
				LayerOption.class,
				LayerOption.NONE
		);
		final boolean provideAll = this.processAllBands.get();
		final boolean isBufferedMatrix = src instanceof BufferedMatrix;
		// remember type s.t. we can restore the saved file more easily
		context.getObjects().put("isBufferedMatrix", isBufferedMatrix);

		final boolean[] processBand = new boolean[numBands];
		final BufferedImage[] images = new BufferedImage[numBands];

		// check which bands to extract...
		for (int i = 0; i < numBands; i++) {
			final OutputBand band = this.outputBands.get(i);
			if (provideAll || band.isConnected()) {
				processBand[i] = true;
			}
		}

		// extract, save and set outputs
		final BandExtractOp op = new BandExtractOp();

		for (int i = 0; i < numBands; i++) {
			if (processBand[i]) {
				op.setBand(i);
				images[i] = filter(context, op, src);

				final OutputBand band = this.outputBands.get(i);

				if (source.cm == null) {
					if (isBufferedMatrix) {
						writeBufferedMatrix(context, band.FLOAT_FILE, (BufferedMatrix) images[i]);
					} else {
						writeBufferedImage(context, band.GRAY_FILE, STORAGE_FORMAT, images[i]);
					}
					band.output_bi.setOutput(images[i]);
				} else if (!source.cm.requiresBufferedMatrix()) {
					writeBufferedImage(context, band.GRAY_FILE, STORAGE_FORMAT, images[i]);
					band.output_bi.setOutput(images[i]);
					band.output_gray.setOutput(images[i]);
				} else {
					writeBufferedMatrix(context, band.FLOAT_FILE, (BufferedMatrix) images[i]);
					band.output_bi.setOutput(images[i]);
					band.output_float.setOutput((BufferedMatrix) images[i]);
				}
			}
		}

		// optionally provide layers (and extra band visualizations)
		final BufferedImage[] layerImages;
		switch (opt) {
			case GRAYSCALE:
				if (!isBufferedMatrix) {
					layerImages = images;
				} else {
					layerImages = null;
				}
				break;

			case VISUALIZATION:
				layerImages = new BufferedImage[numBands];
				final ColorBandVisualizationOp visop = new ColorBandVisualizationOp(source.cm);
				for (int i = 0; i < numBands; i++) {
					if (processBand[i]) {
						visop.setBand(i);
						layerImages[i] = filter(
								context,
								visop,
								src,
								visop.createCompatibleDestImage(src, source.cm)
						);

						final OutputBand band = this.outputBands.get(i);
						writeBufferedImage(context, band.VIS_FILE, STORAGE_FORMAT, layerImages[i]);
					}
				}
				break;

			case NONE:
			default:
				layerImages = null;
				break;
		}

		if (layerImages != null) {
			provideBandLayers(context, source, layerImages);
		}
	}

	private InputColorPort getSource() {
		for (InputColorPort ic : this.inputColors) {
			if (ic.port.isConnected()) {
				return ic;
			}
		}

		return this.input;
	}

	private BufferedImage createCompatibleDestImage(BufferedImage src) {
		if (src instanceof BufferedMatrix) {
			final BufferedMatrix mat = (BufferedMatrix) src;
			return new BufferedMatrix(
					mat.getWidth(),
					mat.getHeight(),
					1,
					mat.getSampleDataType(),
					mat.getInterleave()
			);
		}

		return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	}

	@Override
	public void reset(ProcessorContext context) {
		for (OutputBand band : this.outputBands) {
			deleteFile(context, band.GRAY_FILE);
			deleteFile(context, band.FLOAT_FILE);
			deleteFile(context, band.VIS_FILE);
		}
		resetOutputs();
		resetLayer(context);
		context.getObjects().clear();
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.BUFFER;
	}

	private final BooleanProperty transmuteProperty = new SimpleBooleanProperty();

	@Override
	public BooleanProperty transmuteProperty() {
		return this.transmuteProperty;
	}
}
