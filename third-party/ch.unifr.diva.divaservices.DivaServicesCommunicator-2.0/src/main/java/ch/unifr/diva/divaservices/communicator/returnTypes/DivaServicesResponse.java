package ch.unifr.diva.divaservices.communicator.returnTypes;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Class encapsulating an image and output information from DivaServices
 *
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 08.10.2015.
 */
public class DivaServicesResponse<H> {

    /**
     * extracted image
     */
    private List<BufferedImage> image;
    /**
     * extracted outputs
     */
    private List<List<Map>> outputs;
    /**
     * extracted highlighters
     */
    private List<AbstractHighlighter<H>> highlighters;

    /**
     * @param images        the result images
     * @param outputs       the contents of "output" for each of the processed images
     * @param highlighters  the extracted highlighter information for each of the processed images
     */
    public DivaServicesResponse(List<BufferedImage> images, List<List<Map>> outputs, List<AbstractHighlighter<H>> highlighters) {
        this.image = images;
        this.outputs = outputs;
        this.highlighters = highlighters;
    }

    public List<BufferedImage> getImages() {
        return image;
    }

    public List<List<Map>> getOutputs() {
        return outputs;
    }

    public List<AbstractHighlighter<H>> getHighlighters() {
        return highlighters;
    }
}
