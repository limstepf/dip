package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.utils.ui.NumberValidationTooltip;
import javafx.scene.control.TextField;

/**
 * Integer parameter.
 */
public class IntegerParameter extends PersistentParameterBase<Integer> {

	protected final int minValue;
	protected final int maxValue;

	/**
	 * Creates an integer parameter. Default range is {@code [Integer.MIN_VALUE,
	 * Integer.MAX_VALUE]}
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 */
	public IntegerParameter(String label, int defaultValue) {
		this(label, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Creates an integer parameter.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 * @param minValue minimum value (inclusive).
	 * @param maxValue maximum value (inclusive).
	 */
	public IntegerParameter(String label, int defaultValue, int minValue, int maxValue) {
		super(label, defaultValue);

		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public void set(Integer value) {
		this.valueProperty.set(validateInteger(value));
		if (view != null) {
			view.set(value);
		}
	}

	protected Integer validateInteger(Integer value) {
		if (value >= this.maxValue) {
			return this.maxValue;
		} else if (value <= this.minValue) {
			return this.minValue;
		}

		return value;
	}

	@Override
	protected PersistentParameter.View newViewInstance() {
		return new IntegerView(this);
	}

	/**
	 * Simple Integer view with a TextField.
	 */
	public static class IntegerView extends ParameterViewBase<IntegerParameter, Integer, TextField> {

		protected final NumberValidationTooltip<Integer> validator = new NumberValidationTooltip<>();

		public IntegerView(IntegerParameter parameter) {
			super(parameter, new TextField());
			set(parameter.get());

			root.getStyleClass().add("dip-text-input");
			root.setTooltip(validator);
			root.textProperty().addListener((obs) -> {
				final Integer number = get();
				validator.setOutOfRange(number, parameter.minValue, parameter.maxValue);

				if (!validator.isValid()) {
					root.pseudoClassStateChanged(PersistentParameter.ALERT, true);
					return;
				}

				root.pseudoClassStateChanged(PersistentParameter.ALERT, false);
				parameter.valueProperty.set(number);
			});
		}

		protected final Integer get() {
			try {
				return Integer.parseInt(root.getText());
			} catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		public final void set(Integer value) {
			root.setText(String.format("%d", value));
		}
	}

}
