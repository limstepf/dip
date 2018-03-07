package ch.unifr.diva.dip;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.components.XorInputPorts;
import ch.unifr.diva.dip.api.services.HybridProcessorBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import ch.unifr.diva.divaservices.communicator.DivaServicesAdmin;
import ch.unifr.diva.divaservices.communicator.DivaServicesConnection;
import ch.unifr.diva.divaservices.communicator.exceptions.*;
import ch.unifr.diva.divaservices.communicator.request.DivaCollection;
import javafx.util.Pair;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A processor plugin.
 */
@Component(service = Processor.class)
public class DivaServicesSauvolaBinarization extends HybridProcessorBase {
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
    public DivaServicesSauvolaBinarization() {
        super("DivaServices Sauvola Binarization");

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
        return new DivaServicesSauvolaBinarization();
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
            DivaServicesConnection connection = new DivaServicesConnection("http://divaservices.unifr.ch/api/v2", 5);
            List<Pair<String, BufferedImage>> images = new LinkedList<>();
            images.add(new Pair<>("inputImage.jpg", src));
            Thread.sleep(2000);
            Map<String, Object> parameters = new HashMap<>();
            DivaCollection collection = DivaCollection.createCollectionWithImages(images, connection, log);
            Thread.sleep(2000);
            parameters.put("inputImage", collection.getName() + "/inputImage.jpg");
            parameters.put("radius",15);
            parameters.put("thres_tune",0.3);
            List<JSONObject> binarizationResult = DivaServicesAdmin.runMethod("http://divaservices.unifr.ch/api/v2/binarization/sauvolabinarization/1", parameters);
            log.info(binarizationResult.get(0).toString(1));
            cancelIfInterrupted();

            setOutputs(context, parseResult(binarizationResult));
            cancelIfInterrupted();
        } catch (InterruptedException | FileTypeConfusionException | UserParametersOverloadException | IncompatibleValueException | UserValueRequiredException | ForgotKeyValueObjectException | IOException | MethodNotAvailableException ex) {
            log.error(ex.getMessage());
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

    private BufferedImage parseResult(List<JSONObject> result) throws IOException {

        for (int i = 0; i < result.get(0).getJSONArray("output").length(); i++) {
            JSONObject resultFile = result.get(0).getJSONArray("output").getJSONObject(i);
            if (resultFile.keySet().toArray()[0].equals("file")) {
                JSONObject file = resultFile.getJSONObject("file");
                if (file.getJSONObject("options").getBoolean("visualization")) {
                    return ImageIO.read(new URL(file.getString("url")));

                }
            }
        }
        return null;
    }

}
