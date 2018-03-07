package ch.unifr.diva.divaservices.communicator.returnTypes;

import java.awt.*;
import java.util.List;

/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 08.10.2015.
 */
public class PolygonHighlighter extends AbstractHighlighter<Polygon> {

    public PolygonHighlighter(List<Polygon> data){
        super(data);
    }

}
