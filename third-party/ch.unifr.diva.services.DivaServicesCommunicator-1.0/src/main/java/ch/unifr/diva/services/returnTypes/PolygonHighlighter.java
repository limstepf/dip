package ch.unifr.diva.services.returnTypes;

import java.awt.*;
import java.util.List;

/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 08.10.2015.
 */
public class PolygonHighlighter implements IHighlighter {
    private List<Polygon> polygons;

    public PolygonHighlighter(List<Polygon> polygons){
        this.polygons = polygons;
    }

    @Override
    public List getData() {
        return polygons;
    }
}
