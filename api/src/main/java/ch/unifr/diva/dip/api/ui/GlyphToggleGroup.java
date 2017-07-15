package ch.unifr.diva.dip.api.ui;

import java.util.LinkedHashMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * A glyph toggle group. A toggle group using multiple glyphs.
 *
 * <p>
 * Note that glyph customization methods need to be called before adding the
 * first glyph to the toggle group.
 *
 * @param <T> class of the keys to refer to the glyphs (typically
 * {@code String}).
 */
public class GlyphToggleGroup<T> {

	protected final Glyph.Size glyphSize;
	protected final Orientation orientation;
	protected final LinkedHashMap<T, Glyph> toggles;
	protected final ObjectProperty<T> selectedProperty;

	protected boolean enableDeselection = false;
	protected double hPadding = 2;
	protected double vPadding = 0;
	protected Color hoverColor = Color.WHITE;
	protected Color selectedColor = Color.web("0x0096C9");

	protected final Pane root;

	/**
	 * Creates a new, horizontal glyph toggle group.
	 *
	 * @param glyphSize the size of the glyphs.
	 */
	public GlyphToggleGroup(Glyph.Size glyphSize) {
		this(glyphSize, Orientation.HORIZONTAL);
	}

	/**
	 * Creates a new glyph toggle group.
	 *
	 * @param glyphSize the size of the glyphs.
	 * @param orientation orientation of the toggle group.
	 */
	public GlyphToggleGroup(Glyph.Size glyphSize, Orientation orientation) {
		this.glyphSize = glyphSize;
		this.orientation = orientation;
		this.root = this.orientation.equals(Orientation.HORIZONTAL) ? newHBox() : newVBox();
		this.toggles = new LinkedHashMap<>();
		this.selectedProperty = new SimpleObjectProperty<T>(null) {
			@Override
			public void set(T value) {
				onSetGlyph(get(), value);
				super.set(value);
			}
		};
	}

	private HBox newHBox() {
		final HBox box = new HBox();
		box.setAlignment(Pos.CENTER_LEFT);
		return box;
	}

	private VBox newVBox() {
		final VBox box = new VBox();
		box.setAlignment(Pos.TOP_CENTER);
		return box;
	}

	protected void onSetGlyph(T oldKey, T newKey) {
		if (oldKey != null && this.toggles.containsKey(oldKey)) {
			final Glyph oldGlyph = this.toggles.get(oldKey);
			selectGlyph(oldGlyph, false);
		}
		if (newKey != null && this.toggles.containsKey(newKey)) {
			final Glyph newGlyph = this.toggles.get(newKey);
			selectGlyph(newGlyph, true);
		}
	}

	protected void selectGlyph(Glyph glyph, boolean select) {
		glyph.enableHoverEffect(!select);
		glyph.setColor(select ? selectedColor : hoverColor);
	}

	protected Glyph newGlyph(NamedGlyph glyph, String tooltip) {
		final Glyph g = glyph.get(glyphSize.pt);
		if (hPadding > 0 || vPadding > 0) {
			g.setPadding(new Insets(vPadding, hPadding, vPadding, hPadding));
		}
		g.setColor(hoverColor);
		g.enableHoverEffect(true);
		if (tooltip != null) {
			g.setTooltip(tooltip);
		}
		return g;
	}

	protected boolean isSelectedKey(T key) {
		return key.equals(selectedProperty.get());
	}

	/**
	 * Enables/disables the deselection of a selected glyph.
	 *
	 * @param enable If {@code true} a selected glyph can be deselected by
	 * clicking it again, s.t. no glyph will be selected any longer. Otherwise,
	 * once initialized, some glyph will always be selected.
	 */
	public void enableDeselection(boolean enable) {
		this.enableDeselection = enable;
	}

	/**
	 * Sets the hover (or onMouseOver) color.
	 *
	 * @param color the hover color.
	 */
	public void setHoverColor(Color color) {
		this.hoverColor = color;
	}

	/**
	 * Sets the selected color.
	 *
	 * @param color the selected color.
	 */
	public void setSelectedColor(Color color) {
		this.selectedColor = color;
	}

	/**
	 * Sets the horizontal and vertical padding around glyphs.
	 *
	 * @param padding the padding.
	 */
	public void setPadding(double padding) {
		setPadding(padding, padding);
	}

	/**
	 * Sets the padding around glyphs.
	 *
	 * @param horizontal the horizontal padding.
	 * @param vertical the vertical padding.
	 */
	public void setPadding(double horizontal, double vertical) {
		this.hPadding = horizontal;
		this.vPadding = vertical;
	}

	/**
	 * The selected property holds the key of the selected glyph, or null if no
	 * glyph is selected.
	 *
	 * @return the selected property.
	 */
	public ObjectProperty<T> selectedProperty() {
		return this.selectedProperty;
	}

	/**
	 * Selects a new glyph.
	 *
	 * @param key the key of the glyph, or null to deselect the currently
	 * selected glyph.
	 */
	public void setSelected(T key) {
		if (key == null) {
			selectedProperty.set(null);
		} else if (this.toggles.containsKey(key)) {
			selectedProperty.set(key);
		}
	}

	/**
	 * Returns the key of the selected glyph, or null if no glyph is selected.
	 *
	 * @return the key of the selected glyph, or null.
	 */
	public T getSelected() {
		return this.selectedProperty.get();
	}

	/**
	 * Adds a new glyph to the glyph toggle group.
	 *
	 * @param key the key of the glyph.
	 * @param glyph the named glyph.
	 * @param tooltip the tooltip, or null.
	 */
	public void add(T key, NamedGlyph glyph, String tooltip) {
		final Glyph g = newGlyph(glyph, tooltip);
		g.setOnMousePressed((e) -> selectedProperty().set(
				(enableDeselection && isSelectedKey(key)) ? null : key
		));
		this.toggles.put(key, g);
		this.root.getChildren().add(g);
	}

	/**
	 * Removes a glyph from the glyph toggle group.
	 *
	 * @param key the key of the glyph.
	 */
	public void remove(T key) {
		if (this.toggles.containsKey(key)) {
			final Glyph g = this.toggles.get(key);
			this.toggles.remove(key);
			this.root.getChildren().remove(g);
		}
	}

	/**
	 * Returns the instantiated glyph.
	 *
	 * @param key the key of the glyph.
	 * @return the glyph.
	 */
	public Glyph get(T key) {
		return this.toggles.get(key);
	}

	/**
	 * Returns the (root) node of the glyph toggle.
	 *
	 * @return the (root) node of the glyph toggle.
	 */
	public Node getNode() {
		return this.root;
	}

}
