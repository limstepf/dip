package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.ui.NumberValidationTooltip;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextField;

/**
 * A Double parameter.
 */
public class DoubleParameter extends PersistentParameterBase<Double, DoubleParameter.DoubleView> {

	protected final double minValue;
	protected final double maxValue;

	/**
	 * Creates a double parameter. Default range is {@code [Double.MIN_VALUE,
	 * Double.MAX_VALUE]}.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 */
	public DoubleParameter(String label, double defaultValue) {
		this(label, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
	}

	/**
	 * Creates a double parameter.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 * @param minValue minimum value (inclusive).
	 * @param maxValue maximum value (inclusive).
	 */
	public DoubleParameter(String label, double defaultValue, double minValue, double maxValue) {
		super(label, defaultValue);

		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public void set(Double value) {
		this.valueProperty.set(validateDouble(value));
		if (view != null) {
			view.set(value);
		}
	}

	protected Double validateDouble(Double value) {
		if (value >= this.maxValue) {
			return this.maxValue;
		} else if (value <= this.minValue) {
			return this.minValue;
		}

		return value;
	}

	@Override
	protected DoubleView newViewInstance() {
		return new DoubleView(this);
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
	 * Simple Double view with a TextField.
	 */
	public static class DoubleView extends PersistentParameterBase.ParameterViewBase<DoubleParameter, Double, TextField> {

		protected final NumberValidationTooltip<Double> validator = new NumberValidationTooltip<>();

		public DoubleView(DoubleParameter parameter) {
			super(parameter, new TextField());
			set(parameter.get());

			root.getStyleClass().add("dip-text-input");
			root.setTooltip(validator);
			PersistentParameter.applyViewHooks(root, parameter.viewHooks);
			root.textProperty().addListener((obs) -> {
				final Double number = get();
				validator.setOutOfRange(number, parameter.minValue, parameter.maxValue);

				if (!validator.isValid()) {
					root.pseudoClassStateChanged(PersistentParameter.ALERT, true);
					return;
				}

				root.pseudoClassStateChanged(PersistentParameter.ALERT, false);
				parameter.valueProperty.set(number);
			});
		}

		protected final Double get() {
			try {
				return Double.parseDouble(root.getText());
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		public final void set(Double value) {
			root.setText(String.format("%f", value));
		}
	}

}
