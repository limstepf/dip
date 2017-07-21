package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.FxColor;
import ch.unifr.diva.dip.api.ui.ColorPicker;
import ch.unifr.diva.dip.api.ui.ColorPicker.ColorPickerControl;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * A color picker parameter.
 */
public class ColorPickerParameter extends PersistentParameterBase<FxColor, ColorPickerParameter.ColorPickerParameterView> implements SingleRowParameter<FxColor> {

	protected boolean enableOpacity = false;
	protected boolean enableWebColor = true;
	protected boolean enableHgrow = true;
	protected boolean enableFramedPickerRegion = false;

	/**
	 * Creates a new color picker parameter.
	 *
	 * @param label the label.
	 * @param defaultValue the default color.
	 */
	public ColorPickerParameter(String label, Color defaultValue) {
		this(label, new FxColor(defaultValue));
	}

	/**
	 * Creates a new color picker parameter.
	 *
	 * @param label the label.
	 * @param defaultValue the default color.
	 */
	public ColorPickerParameter(String label, FxColor defaultValue) {
		super(label, FxColor.class, defaultValue);
	}

	@Override
	protected ColorPickerParameterView newViewInstance() {
		return new ColorPickerParameterView(this);
	}

	/**
	 * Ënables/disables the opacity. If disabled, opacity is set to 100% and
	 * can't be changed. Needs to be called before the view is requested.
	 * Disabled by default.
	 *
	 * @param opacity {@code true} to enable opacity of colors, {@code false} to
	 * disable.
	 */
	public void enableOpacity(boolean opacity) {
		this.enableOpacity = opacity;
	}

	/**
	 * Enables/disables the web color field. Needs to be called before the view
	 * is requested. Enabled by default.
	 *
	 * @param webColor {@code true} to enable the web color field, {@code false}
	 * to disable.
	 */
	public void enableWebColor(boolean webColor) {
		this.enableWebColor = webColor;
	}

	/**
	 * Enables/disables horizontal grow of the color picker control region.
	 *
	 * @param hgrow horizontally grows to occupy available width if set to
	 * {@code true}, otherwise the region ends up as a square depending on the
	 * taken height. Needs to be called before the view is requested. Enabled by
	 * default.
	 */
	public void enableHgrow(boolean hgrow) {
		this.enableHgrow = hgrow;
	}

	/**
	 * Enables a framed color picker region.
	 *
	 * @param framedRegion {@code true} to have a black and white border around
	 * the color picker region, {@code false} to have a region without border.
	 */
	public void enableFramedPickerRegion(boolean framedRegion) {
		this.enableFramedPickerRegion = framedRegion;
	}

	/**
	 * Returns the JavaFX {@code Color}.
	 *
	 * @return the JavaFX {@code Color}.
	 */
	public Color getColor() {
		return (get() != null) ? get().toColor() : null;
	}

	// lazily initialized
	protected ObjectProperty<Color> colorProperty;

	/**
	 * The color property. The parameter's value as a JavaFx {@code Color}
	 * object instead of the {@code FxColor} wrapper of the parameter
	 * {@code property()}.
	 *
	 * @return the color property.
	 */
	public ReadOnlyObjectProperty<Color> colorProperty() {
		if (colorProperty == null) {
			colorProperty = new SimpleObjectProperty<>(get().toColor());
			property().addListener((c) -> {
				colorProperty.set(get().toColor());
			});
		}
		return colorProperty;
	}

	protected final List<ViewHook<Region>> regionViewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the color picker region. This method is
	 * only called if the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a region.
	 */
	public void addRegionViewHook(ViewHook<Region> hook) {
		this.regionViewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeRegionViewHook(ViewHook<Region> hook) {
		this.regionViewHooks.remove(hook);
	}

	@Override
	public void initSingleRowView() {
		enableHgrow(false);
		enableFramedPickerRegion(true);
	}

	/**
	 * Color picker parameter view.
	 */
	public static class ColorPickerParameterView extends PersistentParameterBase.ParameterViewBase<ColorPickerParameter, FxColor, StackPane> {

		private final ColorPickerControl picker;

		/**
		 * Creates a new color picker parameter view.
		 *
		 * @param parameter the color picker parameter.
		 */
		public ColorPickerParameterView(ColorPickerParameter parameter) {
			super(parameter, new StackPane());

			this.picker = parameter.enableFramedPickerRegion
					? ColorPicker.newFramedColorPickerRegion(
							null,
							parameter.enableOpacity,
							parameter.enableWebColor
					)
					: ColorPicker.newColorPickerRegion(
							null,
							parameter.enableOpacity,
							parameter.enableWebColor
					);
			final Region pickerRegion = picker.asRegion();
			PersistentParameter.applyViewHooks(pickerRegion, parameter.regionViewHooks);
			if (parameter.enableHgrow) {
				HBox.setHgrow(pickerRegion, Priority.ALWAYS);
			}
			final HBox hbox = new HBox();
			hbox.setAlignment(Pos.CENTER);
			hbox.getChildren().setAll(pickerRegion);

			root.setPadding(new Insets(3.5));
			root.getChildren().setAll(hbox);

			set(parameter.get());

			picker.colorProperty().addListener(
					(e) -> parameter.setLocal(get())
			);
		}

		@Override
		public final FxColor get() {
			return new FxColor(picker.colorProperty().get());
		}

		@Override
		public final void set(FxColor value) {
			picker.colorProperty().set(value.toColor());
		}

	}

}
