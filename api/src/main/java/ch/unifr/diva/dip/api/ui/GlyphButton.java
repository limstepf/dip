package ch.unifr.diva.dip.api.ui;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;

/**
 * A glyph button.
 */
public class GlyphButton extends Button {

	/**
	 * Creates a glyph button.
	 *
	 * @param glyph the glyph of the button.
	 */
	public GlyphButton(Glyph glyph) {
		this(glyph, null, ContentDisplay.CENTER);
	}

	/**
	 * Creates a glyph button with a label. This places the glyph to the left,
	 * and the label to the rigth.
	 *
	 * @param glyph the glyph of the button.
	 * @param label the label of the button.
	 */
	public GlyphButton(Glyph glyph, String label) {
		this(glyph, label, ContentDisplay.LEFT);
	}

	/**
	 * Creates a glyph button.
	 *
	 * @param glyph the glyph of the button.
	 * @param label the label of the button, or null.
	 * @param contentDisplay the position to place the glyph within the button.
	 */
	public GlyphButton(Glyph glyph, String label, ContentDisplay contentDisplay) {
		this.setGraphic(glyph);
		if (label != null) {
			this.setText(label);
		}
		this.setContentDisplay(contentDisplay);
	}

}
