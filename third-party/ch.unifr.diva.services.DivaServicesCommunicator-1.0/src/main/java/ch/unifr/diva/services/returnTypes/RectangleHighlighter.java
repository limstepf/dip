package ch.unifr.diva.services.returnTypes;

import java.awt.*;
import java.util.List;

/**
 * DivaServices RectangleHighlighter.
 *
 * @author Marcel Würsch marcel.wuersch@unifr.ch
 * http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch Created on:
 * 08.10.2015.
 */
public class RectangleHighlighter implements IHighlighter {

	private List<Rectangle> rectangles;

	public RectangleHighlighter(List<Rectangle> rectangles) {
		this.rectangles = rectangles;
	}

	@Override
	public List getData() {
		return rectangles;
	}
}
