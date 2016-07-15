package ch.unifr.diva.dip.api.parameters;

import javafx.scene.control.CheckBox;

/**
 * A boolean parameter in form of a checkbox.
 */
public class CheckboxParameter extends PersistentParameterBase<Boolean, CheckboxParameter.CheckboxView> {

	protected final String checkboxLabel;

	/**
	 * Creates a new (child-)checkbox parameter without label/postfix. This is a
	 * "child-"checkbox and as such intended to be attached to a composite
	 * parameter, rather than directly, hence the parameter's label (not a
	 * possible postfix of the checkbox) is empty.
	 *
	 * @param defaultValue default value of the checkbox.
	 */
	public CheckboxParameter(boolean defaultValue) {
		this("", defaultValue, "");
	}

	/**
	 * Creates a new (child-)checkbox parameter with label/postfix. This is a
	 * "child-"checkbox and as such intended to be attached to a composite
	 * parameter, rather than directly, hence the parameter's label (not a
	 * possible postfix of the checkbox) is empty.
	 *
	 * @param defaultValue
	 * @param checkboxLabel
	 */
	public CheckboxParameter(boolean defaultValue, String checkboxLabel) {
		this("", defaultValue, checkboxLabel);
	}

	/**
	 * Creates a new checkbox parameter. Checkboxes are probably best used in
	 * some composite parameter rather than directly as "top-level" parameter,
	 * and hence the {@code label} probably doesn't matter too much. Use the
	 * {@code checkboxLabel} instead.
	 *
	 * @param label label of the parameter (not the checkbox itself).
	 * @param defaultValue default value.
	 * @param checkboxLabel label of the checkbox (postfix label).
	 */
	public CheckboxParameter(String label, boolean defaultValue, String checkboxLabel) {
		super(label, defaultValue);

		this.checkboxLabel = checkboxLabel;
	}

	@Override
	protected CheckboxView newViewInstance() {
		return new CheckboxView(this);
	}

	/**
	 * Checkbox view.
	 */
	public static class CheckboxView extends PersistentParameterBase.ParameterViewBase<CheckboxParameter, Boolean, CheckBox> {

		public CheckboxView(CheckboxParameter parameter) {
			super(parameter, newCheckBox(parameter.checkboxLabel));

			set(parameter.get());

			this.root.selectedProperty().addListener((obs) -> {
				parameter.valueProperty.set(get());
			});
		}

		protected static CheckBox newCheckBox(String label) {
			if ("".equals(label)) {
				return new CheckBox();
			}
			return new CheckBox(label);
		}

		protected final Boolean get() {
			return this.root.isSelected();
		}

		@Override
		public final void set(Boolean value) {
			this.root.setSelected(value);
		}
	}

}
