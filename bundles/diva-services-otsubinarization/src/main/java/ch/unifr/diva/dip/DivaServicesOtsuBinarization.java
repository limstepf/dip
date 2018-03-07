package ch.unifr.diva.dip;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.services.HybridProcessorBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.services.DivaServicesCommunicator;
import ch.unifr.diva.services.returnTypes.DivaServicesResponse;
import org.osgi.service.component.annotations.Component;

import java.awt.image.BufferedImage;

/**
 * A processor plugin.
 */
@Component(service = Processor.class)
public class DivaServicesOtsuBinarization extends HybridProcessorBase {
    private final static String DIVA_SERVICES_API = "http://divaservices.unifr.ch/api/v1/";
    private final static String STORAGE_IMAGE = "binary.png";
    private final static String STORAGE_IMAGE_FORMAT = "PNG";

    private final InputPort<BufferedImage> input;
    private final InputPort<BufferedImage> input_binary;
    private final InputPort<BufferedImage> input_gray;
    private final XorInputPorts input_xor;
    private final OutputPort<BufferedImage> output_image;


    /**
     * Creates a new processor plugin.
     */
    public DivaServicesOtsuBinarization() {
        super("DivaServices Otsu Binarization");

        this.input = new InputPort<>(
                new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
                false
        );
        this.input_binary = new InputPort<>(
                new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary(),
                false
        );
        this.input_gray = new InputPort<>(
                new ch.unifr.diva.dip.api.datatypes.BufferedImageGray(),
                false
        );
        this.output_image = new OutputPort<>(
                new ch.unifr.diva.dip.api.datatypes.BufferedImage()
        );

        this.input_xor = new XorInputPorts(this);
        input_xor.addPort("buffered-image", input);
        input_xor.addPort("buffered-image-binary", input_binary);
        input_xor.addPort("buffered-image-gray", input_gray);
        input_xor.enableAllPorts();

        outputs.put("binary image", output_image);

    }

    @Override
    public NamedGlyph glyph() {
        // no need to overwrite this method, but that's how you customize the
        // glyph of the processor. Other glyph fonts are available too.
        return MaterialDesignIcons.CHIP;
    }

    @Override
    public Processor newInstance(ProcessorContext context) {
        return new DivaServicesOtsuBinarization();
    }

    @Override
    public void init(ProcessorContext context) {
        /*
         * If no context is passed, then this instance is used as a non-runnable
         * (or passive/latent) processor inside the pipeline editor.
         */
        input_xor.init(context);

        // TODO (optional): manage/deactivate ports, and repaint
        // TODO (optional): attach (port-)event listeners

        if (context != null) {
            restoreOutputs(context);
        }
    }

    @Override
    public void process(ProcessorContext context) {
        // TODO: process signal(s) from input ports
        // TODO: store result, and set output ports, layers, ...
        // TODO (optional): implement the Previewable interface if applicable
        try {
            final BufferedImage src = getSourceImage();
            cancelIfInterrupted(src);

            final DivaServicesCommunicator communicator = new DivaServicesCommunicator(
                    DIVA_SERVICES_API
            );
            Thread.sleep(2000);

            final DivaServicesResponse response = communicator.runOtsuBinarization(src, true);
            cancelIfInterrupted(response);
            BufferedImage outputImage = response.getImage();
            writeObject(context, outputImage, STORAGE_IMAGE);
            cancelIfInterrupted();

            setOutputs(context, outputImage);
            cancelIfInterrupted();
        } catch (InterruptedException ex) {
            reset(context);
        }
    }

    @Override
    public void reset(ProcessorContext context) {
        deleteFile(context, STORAGE_IMAGE);
        resetOutputs();
        resetLayer(context);
    }

    protected void restoreOutputs(ProcessorContext context) {
        final BufferedImage image = readBufferedImage(context, STORAGE_IMAGE);
        setOutputs(context, image);
    }

    protected void setOutputs(ProcessorContext context, BufferedImage image) {
        if (image != null) {
            provideImageLayer(context, image);
        }
        output_image.setOutput(image);
    }


    protected BufferedImage getSourceImage() {
        final InputPort<?> port = input_xor.getEnabledPort();
        if (port == null) {
            log.warn("no input port enabled");
            return null;
        }
        if (port.equals(input_binary)) {
            return input_binary.getValue();
        } else if (port.equals(input_gray)) {
            return input_gray.getValue();
        } else {
            return input.getValue();
        }
    }
}
