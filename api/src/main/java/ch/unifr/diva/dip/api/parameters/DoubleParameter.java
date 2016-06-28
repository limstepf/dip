package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.utils.ui.NumberValidationTooltip;
import javafx.scene.control.TextField;

/**
 * A Double parameter.
 */
public class DoubleParameter extends PersistentParameterBase<Double> {

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
	protected PersistentParameter.View newViewInstance() {
		return new DoubleView(this);
	}

	/**
	 * Simple Double view with a TextField.
	 */
	public static class DoubleView extends ParameterViewBase<DoubleParameter, Double, TextField> {

		protected final NumberValidationTooltip<Double> validator = new NumberValidationTooltip<>();

		public DoubleView(DoubleParameter parameter) {
			super(parameter, new TextField());
			set(parameter.get());

			root.getStyleClass().add("dip-text-input");
			root.setTooltip(validator);
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
