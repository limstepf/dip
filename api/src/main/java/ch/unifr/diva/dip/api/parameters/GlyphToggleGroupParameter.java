package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.GlyphToggleGroup;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.HBox;

/**
 * A glyph toggle group parameter.
 *
 * @param <T> class of the key (typically a {@code String}).
 */
public class GlyphToggleGroupParameter<T> extends PersistentParameterBase<T, GlyphToggleGroupParameter.GlyphToggleGroupParameterView<T>> implements SingleRowParameter<T> {

	protected final List<GlyphToggleData<T>> toggles;
	protected Glyph.Size glyphSize = Glyph.Size.MEDIUM;
	protected boolean enableDeselection = false;

	/**
	 * Creates a new glyph toggle group parameter.
	 *
	 * @param label the label.
	 * @param defaultValue the default value/key.
	 */
	public GlyphToggleGroupParameter(String label, T defaultValue) {
		super(label, defaultValue);

		this.toggles = new ArrayList<>();
	}

	@Override
	protected GlyphToggleGroupParameterView<T> newViewInstance() {
		return new GlyphToggleGroupParameterView<>(this);
	}

	/**
	 * Adds a glyph to the glyph toggle group.
	 *
	 * @param key the key of the glyph.
	 * @param glyph the named glyph.
	 * @param tooltip the tooltip.
	 */
	public void add(T key, NamedGlyph glyph, String tooltip) {
		this.toggles.add(new GlyphToggleData<>(key, glyph, tooltip));
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

	protected void getToggles(GlyphToggleGroup<T> group) {
		for (GlyphToggleData<T> data : this.toggles) {
			group.add(data.key, data.glyph, data.tooltip);
		}
	}

	@Override
	public void initSingleRowView() {
		glyphSize = Glyph.Size.MEDIUM;
	}

	/**
	 * The glyph toggle group parameter view.
	 *
	 * @param <T> class of the key (typically a {@code String}).
	 */
	public static class GlyphToggleGroupParameterView<T> extends PersistentParameterBase.ParameterViewBase<GlyphToggleGroupParameter<T>, T, HBox> {

		protected final GlyphToggleGroup<T> toggle;

		/**
		 * Creates a new glyph toggle group parameter view.
		 *
		 * @param parameter the glyph toggle group parameter.
		 */
		public GlyphToggleGroupParameterView(GlyphToggleGroupParameter<T> parameter) {
			super(parameter, new HBox());

			this.toggle = new GlyphToggleGroup<>(parameter.glyphSize);
			toggle.enableDeselection(parameter.enableDeselection);
			parameter.getToggles(toggle);
			root.getChildren().add(toggle.getNode());

			set(parameter.get());

			toggle.selectedProperty().addListener(
					(e) -> parameter.setLocal(get())
			);
		}

		protected final T get() {
			return toggle.getSelected();
		}

		@Override
		public final void set(T value) {
			toggle.setSelected(value);
		}

	}

	/**
	 * Glyph toggle data.
	 *
	 * @param <T> class of the key (typically a {@code String}).
	 */
	protected static class GlyphToggleData<T> {

		public final T key;
		public final NamedGlyph glyph;
		public final String tooltip;

		/**
		 * Creates new glyph toggle data.
		 *
		 * @param key the key of the glyph.
		 * @param glyph the named glyph.
		 * @param tooltip the tooltip.
		 */
		public GlyphToggleData(T key, NamedGlyph glyph, String tooltip) {
			this.key = key;
			this.glyph = glyph;
			this.tooltip = tooltip;
		}

	}

}
