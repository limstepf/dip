package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.utils.ui.NumberValidationTooltip;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextField;

/**
 * Float parameter.
 */
public class FloatParameter extends PersistentParameterBase<Float, FloatParameter.FloatView> {

	protected final float minValue;
	protected final float maxValue;

	/**
	 * Creates a float parameter. Default range is {@code [Float.MIN_VALUE,
	 * Float.MAX_VALUE]}.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 */
	public FloatParameter(String label, float defaultValue) {
		this(label, defaultValue, Float.MIN_VALUE, Float.MAX_VALUE);
	}

	/**
	 * Creates a float parameter.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 * @param minValue minimum value (inclusive).
	 * @param maxValue maximum value (inclusive).
	 */
	public FloatParameter(String label, float defaultValue, float minValue, float maxValue) {
		super(label, defaultValue);

		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public void set(Float value) {
		this.valueProperty.set(validateFloat(value));
		if (view != null) {
			view.set(value);
		}
	}

	protected Float validateFloat(Float value) {
		if (value >= this.maxValue) {
			return this.maxValue;
		} else if (value <= this.minValue) {
			return this.minValue;
		}

		return value;
	}

	@Override
	protected FloatView newViewInstance() {
		return new FloatView(this);
	}

	protected final List<ViewHook> viewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the textfield. This method is only called
	 * if the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a label.
	 */
	public void addTextFieldViewHook(ViewHook<TextField> hook) {
		this.viewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeTextFieldViewHook(ViewHook<TextField> hook) {
		this.viewHooks.remove(hook);
	}

	/**
	 * Simple Float view with a TextField.
	 */
	public static class FloatView extends PersistentParameterBase.ParameterViewBase<FloatParameter, Float, TextField> {

		protected final NumberValidationTooltip<Float> validator = new NumberValidationTooltip<>();

		public FloatView(FloatParameter parameter) {
			super(parameter, new TextField());
			set(parameter.get());

			root.getStyleClass().add("dip-text-input");
			root.setTooltip(validator);
			PersistentParameter.applyViewHooks(root, parameter.viewHooks);
			root.textProperty().addListener((obs) -> {
				final Float number = get();
				validator.setOutOfRange(number, parameter.minValue, parameter.maxValue);

				if (!validator.isValid()) {
					root.pseudoClassStateChanged(PersistentParameter.ALERT, true);
					return;
				}

				root.pseudoClassStateChanged(PersistentParameter.ALERT, false);
				parameter.valueProperty.set(number);
			});
		}

		protected final Float get() {
			try {
				return Float.parseFloat(root.getText());
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		public final void set(Float value) {
			root.setText(String.format("%f", value));
		}
	}

}
