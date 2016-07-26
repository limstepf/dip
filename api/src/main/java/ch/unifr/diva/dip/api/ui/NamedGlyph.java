package ch.unifr.diva.dip.api.ui;

/**
 * A named glyph. Named glyphs are implemented by glyph font enums, and act as
 * convenient {@code Glyph} factories.
 */
public interface NamedGlyph {

	/**
	 * Returns the name of the glyph.
	 *
	 * @return the name of the glyph.
	 */
	public String getName();

	/**
	 * Returns the unicode character of the glyph.
	 *
	 * @return the unicode character of the glyph.
	 */
	public char getCharacter();

	/**
	 * Returns a new glyph node. Keep in mind that a JavaFx node can only occur
	 * once in a scene!
	 *
	 * @param size size of the glyph in pt.
	 * @return a new glyph node.
	 */
	public Glyph get(double size);

	/**
	 * Returns a new glyph node in default size. Keep in mind that a JavaFx node
	 * can only occur once in a scene!
	 *
	 * @return a new glyph node.
	 */
	default Glyph get() {
		return get(-1);
	}

}
