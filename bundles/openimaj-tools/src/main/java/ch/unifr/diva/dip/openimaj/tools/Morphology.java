package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.ProcessorDocumentation;
import ch.unifr.diva.dip.api.components.SimpleProcessorDocumentation;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.dip.openimaj.utils.OpenIMAJUtils;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.morphology.Close;
import org.openimaj.image.processing.morphology.Dilate;
import org.openimaj.image.processing.morphology.Erode;
import org.openimaj.image.processing.morphology.Open;
import org.openimaj.image.processing.morphology.StructuringElement;
import org.osgi.service.component.annotations.Component;

/**
 * Morphological image processing.
 */
@Component(service = Processor.class)
public class Morphology extends ProcessableBase implements Previewable {

	private final static String STORAGE_IMAGE = "morphology.png";
	private final static String STORAGE_IMAGE_FORMAT = "PNG";

	private final InputPort<BufferedImage> input;
	private final InputPort<BufferedImage> input_binary;
	private final XorInputPorts input_xor;
	private final OutputPort<BufferedImage> output;
	private final OutputPort<BufferedImage> output_binary;
	private final EnumParameter operator;
	private final EnumParameter structuringElement;
	private final IntegerSliderParameter repeat;

	private String runtimeTitle;

	/**
	 * Available morphological operators.
	 */
	public enum OP {

		EROSION("Erosion") {
					@Override
					public void process(FImage image, StructuringElement se, int n) {
						final Erode erode = new Erode(se);
						for (int i = 0; i < n; i++) {
							image.processInplace(erode);
						}
					}

					@Override
					public boolean canRepeat() {
						return true;
					}
				},
		DILATION("Dilation") {
					@Override
					public void process(FImage image, StructuringElement se, int n) {
						final Dilate dilate = new Dilate(se);
						for (int i = 0; i < n; i++) {
							image.processInplace(dilate);
						}
					}

					@Override
					public boolean canRepeat() {
						return true;
					}
				},
		OPENING("Opening") {
					@Override
					public void process(FImage image, StructuringElement se, int n) {
						final Open open = new Open(se);
						open.processImage(image);
					}
				},
		CLOSING("Closing") {
					@Override
					public void process(FImage image, StructuringElement se, int n) {
						final Close close = new Close(se);
						close.processImage(image);
					}
				};

		private final String opName;

		private OP(String name) {
			this.opName = name;
		}

		public String getOpName() {
			return opName;
		}

		public boolean canRepeat() {
			return false;
		}

		public abstract void process(FImage image, StructuringElement se, int n);

	}

	/**
	 * Available structuring elements.
	 */
	public enum SE {

		BOX() {
					@Override
					public StructuringElement get() {
						return StructuringElement.BOX;
					}
				},
		CROSS() {
					@Override
					public StructuringElement get() {
						return StructuringElement.CROSS;
					}
				},
		HPIT() {
					@Override
					public StructuringElement get() {
						return StructuringElement.HPIT;
					}
				};

		public abstract StructuringElement get();
	}

	/**
	 * Creates a new morphological operator.
	 */
	public Morphology() {
		super("Morphology");

		this.runtimeTitle = this.name;
		this.operator = new EnumParameter(
				"Operator",
				OP.class,
				OP.EROSION.name()
		);
		parameters.put("operator", operator);

		this.structuringElement = new EnumParameter(
				"Structuring Element",
				SE.class,
				SE.BOX.name()
		);
		parameters.put("structuring-element", structuringElement);

		this.repeat = new IntegerSliderParameter("Repeat", 1, 1, 9);
		repeat.addSliderViewHook((s) -> {
			s.disableProperty().bind(repeatDisableProperty);
		});
		parameters.put("repeat", repeat);

		this.input = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
				false
		);
		this.input_binary = new InputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary(),
				false
		);
		this.input_xor = new XorInputPorts(this);
		input_xor.addPort("buffered-image", input);
		input_xor.addPort("buffered-image-binary", input_binary);
		input_xor.enableAllPorts();

		this.output = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImage()
		);
		this.output_binary = new OutputPort<>(
				new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary()
		);
		outputs.put("buffered-image", output);
		outputs.put("buffered-image-binary", output_binary);

	}

	protected final BooleanProperty repeatDisableProperty = new SimpleBooleanProperty();
	protected final InvalidationListener opListener = (t) -> updateParameters();

	protected void updateParameters() {
		final OP op = operator.getEnumValue(OP.class);
		runtimeTitle = op.getOpName();
		repeatDisableProperty.set(!op.canRepeat());
		repaint();
	}

	@Override
	public String name() {
		return runtimeTitle;
	}

	@Override
	public ProcessorDocumentation processorDocumentation() {
		final SimpleProcessorDocumentation doc = new SimpleProcessorDocumentation();
		doc.addTextFlow(
				"OpenIMAJ's morphological operators for (assumed) binary images."
		);
		return doc;
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.QRCODE;
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new Morphology();
	}

	@Override
	public void init(ProcessorContext context) {
		input_xor.init(context);
		operator.property().addListener(opListener);
		updateParameters();

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
		final FImage fimage = getSourceFImage();
		if (fimage == null) {
			return;
		}

		final OP op = operator.getEnumValue(OP.class);
		final SE se = structuringElement.getEnumValue(SE.class);

		op.process(fimage, se.get(), repeat.get());

		final BufferedImage image = OpenIMAJUtils.toBinaryBufferedImage(fimage);
		writeBufferedImage(context, image, STORAGE_IMAGE, STORAGE_IMAGE_FORMAT);
		setOutputs(context, image);
	}

	@Override
	public Image previewSource(ProcessorContext context) {
		return SwingFXUtils.toFXImage(getSourceImage(), null);
	}

	@Override
	public Image preview(ProcessorContext context, Rectangle bounds) {
		final BufferedImage image = getSourceImage().getSubimage(
				bounds.x,
				bounds.y,
				bounds.width,
				bounds.height
		);
		final FImage fimage = OpenIMAJUtils.toFImage(image, 0);
		final OP op = operator.getEnumValue(OP.class);
		final SE se = structuringElement.getEnumValue(SE.class);

		op.process(fimage, se.get(), repeat.get());

		final BufferedImage preview = OpenIMAJUtils.toBinaryBufferedImage(fimage);
		return SwingFXUtils.toFXImage(preview, null);
	}

	protected BufferedImage getSourceImage() {
		final InputPort<?> port = input_xor.getEnabledPort();
		if (port == null) {
			log.warn("no input port enabled");
			return null;
		}

		if (port.equals(input_binary)) {
			return input_binary.getValue();
		}
		return input.getValue();
	}

	protected FImage getSourceFImage() {
		final BufferedImage image = getSourceImage();
		if (image == null) {
			return null;
		}
		return OpenIMAJUtils.toFImage(image, 0);
	}

	protected void restoreOutputs(ProcessorContext context) {
		final BufferedImage image = readBufferedImage(context, STORAGE_IMAGE);
		setOutputs(context, image);
	}

	protected void setOutputs(ProcessorContext context, BufferedImage image) {
		if (image != null) {
			provideImageLayer(context, image);
		}
		output.setOutput(image);
		output_binary.setOutput(image);
	}

	@Override
	public void reset(ProcessorContext context) {
		deleteFile(context, STORAGE_IMAGE);
		resetOutputs();
		resetLayer(context);
	}

}
