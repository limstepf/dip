package ch.unifr.diva.services.returnTypes;

import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class encapsulating an image and output information from DivaServices
 *
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 08.10.2015.
 */
public class DivaServicesResponse {

    /**
     * extracted image
     */
    private BufferedImage image;
    /**
     * extracted outputs
     */
    private Map<String, Object> output;
    /**
     * extracted highlighters
     */
    private IHighlighter highlighter;
    /**
     *
     * @param image the result image
     * @param output the contents of "output"
     * @param highlighter the extracted highlighter information
     */
    public DivaServicesResponse(BufferedImage image, Map<String, Object> output, IHighlighter highlighter){
        this.image = image;
        this.output = output;
        this.highlighter = highlighter;
    }

    public BufferedImage getImage(){
        return image;
    }

    public Map<String,Object> getOutput(){
        return output;
    }

    public IHighlighter getHighlighter(){
        return highlighter;
    }
}
