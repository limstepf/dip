package ch.unifr.diva.dip.api.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * A glyph. Glyphs are JavaFx {@code Label}s with a single character, backed by
 * dedicated glyph fonts. Keep in mind that a JavaFx node can only occur once in
 * a scene!
 *
 * <p>
 * A {@code Glyph} is rather low-level, and it's usually nicer to work with
 * {@code NamedGlyph}'s instead: they're typically implemented by glyph font
 * enums that also act as a {@code Glyph} factory. This is nice since we can
 * specify a certain glyph this way, but let the main/host application
 * instantiate and decorate them as needed (size, color, ...).
 */
public class Glyph extends Label {

	private final static String GLYPH_STYLE = "dip-glyph";
	private final static String HOVER_EFFECT_STYLE = "dip-hover-effect";
	private final static String HOVER_EFFECT_DISABLED_STYLE = "dip-hover-effect-disabled";
	private final String fontFamily;

	/**
	 * Standardized glyph sizes.
	 */
	public enum Size {

		// TODO: describe what size is typically used for what!
		SMALL(12),
		NORMAL(18),
		MEDIUM(24),
		LARGE(36),
		HUGE(48);

		/**
		 * The font size specified in points which are a real world measurement
		 * of approximately 1/72 inch.
		 */
		public final double pt;

		Size(double pt) {
			this.pt = pt;
		}

	}

	/**
	 * Creates a new glyph. Make sure the font is loaded first!
	 *
	 * @param fontFamily the font family of the glyph.
	 * @param fontSize the size of the glyph. Consider using one of the
	 * standardized sizes defined on the {@code Size} enum. The font size
	 * specified in points which are a real world measurement of approximately
	 * 1/72 inch.
	 * @param unicode the unicode character of the glyph.
	 */
	public Glyph(String fontFamily, double fontSize, char unicode) {
		this.fontFamily = fontFamily;
		this.getStyleClass().add(GLYPH_STYLE);
		setFont(this.fontFamily, fontSize);
		setCharacter(unicode);
	}

	/**
	 * Updates/sets the glyph.
	 *
	 * @param unicode the unicode character of the glyph.
	 * @return the modified glyph.
	 */
	public final Glyph setCharacter(char unicode) {
		this.setText(String.valueOf(unicode));
		return this;
	}

	/**
	 * Updates/sets the glyph color.
	 *
	 * @param color color to paint the glyph with.
	 * @return the modified glyph.
	 */
	public Glyph setColor(Color color) {
		this.setTextFill(color);
		return this;
	}

	private BooleanProperty disabledHoverEffectProperty;

	/**
	 * DisabledHoverEffect property. Enables/disables a constant hover effect on
	 * the glyph, no matter if the glyph is actually hovered over or not.
	 * Intended to be bound to a parent hoverProperty (e.g. of a {@code Button})
	 * or similar.
	 *
	 * <p>
	 * This property is to be set to true to disable hover, and to false to
	 * indicate active hover.
	 *
	 * @return the constantHoverEffect property.
	 */
	public BooleanProperty disabledHoverEffectProperty() {
		if (this.disabledHoverEffectProperty == null) {
			this.disabledHoverEffectProperty = new BooleanPropertyBase(false) {

				@Override
				protected void invalidated() {
					if (get()) {
						Glyph.this.getStyleClass().add(HOVER_EFFECT_DISABLED_STYLE);
					} else {
						Glyph.this.getStyleClass().remove(HOVER_EFFECT_DISABLED_STYLE);
					}
				}

				@Override
				public Object getBean() {
					return Glyph.this;
				}

				@Override
				public String getName() {
					return "constantHoverEffect";
				}

			};
		}
		return this.disabledHoverEffectProperty;
	}

	private BooleanProperty hoverEffectProperty;

	/**
	 * HoverEffect property. Enables/disables a hover effect on the glyph.
	 *
	 * @return the hoverEffect property.
	 */
	public BooleanProperty hoverEffectProperty() {
		if (this.hoverEffectProperty == null) {
			this.hoverEffectProperty = new BooleanPropertyBase(false) {

				@Override
				protected void invalidated() {
					if (get()) {
						Glyph.this.getStyleClass().add(HOVER_EFFECT_STYLE);
					} else {
						Glyph.this.getStyleClass().remove(HOVER_EFFECT_STYLE);
					}
				}

				@Override
				public Object getBean() {
					return Glyph.this;
				}

				@Override
				public String getName() {
					return "hoverEffect";
				}

			};
		}

		return this.hoverEffectProperty;
	}

	/**
	 * Enables/disables a hover effect on the glyph.
	 *
	 * @param enable True to enable the hover effect, False to disable.
	 * @return the modified glyph.
	 */
	public Glyph enableHoverEffect(boolean enable) {
		hoverEffectProperty().set(enable);
		return this;
	}

	/**
	 * Sets the value of the property background.
	 *
	 * @param color color to paint the background of the glyph with.
	 * @return the modified glyph.
	 */
	public Glyph setBackground(Color color) {
		return setBackground(color, -1);
	}

	/**
	 * Sets the value of the property background.
	 *
	 * @param color color to paint the background of the glyph with.
	 * @param corner the radii for each corner (from {@code 0.0} to {@code 1.0}
	 * interpreted as a percentage).
	 * @return the modified glyph.
	 */
	public Glyph setBackground(Color color, double corner) {
		this.setBackground(
				new Background(
						new BackgroundFill(
								color,
								corner > 0 ? new CornerRadii(corner, true) : null,
								null
						)
				)
		);
		return this;
	}

	/**
	 * Updates/sets the glyph font size.
	 *
	 * @param size the font size of the glyph in pt.
	 * @return the modified glyph.
	 */
	public Glyph setFontSize(double size) {
		this.setFont(Font.font(this.fontFamily, size));
		return this;
	}

	/**
	 * Sets the glyph font. Make sure the font is loaded first!
	 *
	 * @param fontFamily font family of the glyph.
	 * @param size the font size of the glyph in pt.
	 * @return the modified glyph.
	 */
	private Glyph setFont(String fontFamily, double size) {
		this.setFont(Font.font(fontFamily, size));
		return this;
	}

	/**
	 * Creates a tooltip on the glyph.
	 *
	 * @param tooltip the text of the tooltip.
	 */
	public void setTooltip(String tooltip) {
		final Tooltip t = new Tooltip(tooltip);
		this.setTooltip(t);
	}

}
