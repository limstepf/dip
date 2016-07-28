package ch.unifr.diva.dip.api.parameters;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

/**
 * A Boolean parameter.
 */
public class BooleanParameter extends PersistentParameterBase<Boolean, BooleanParameter.BooleanView> {

	protected String trueLabel;
	protected String falseLabel;

	/**
	 * Creates a boolean parameter. Uses "true" and "false" as option labels.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 */
	public BooleanParameter(String label, boolean defaultValue) {
		this(label, defaultValue, "true", "false");
	}

	/**
	 * Creates a boolean parameter.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 * @param trueLabel label of the true (or on) option.
	 * @param falseLabel label of the false (or off) option.
	 */
	public BooleanParameter(String label, boolean defaultValue, String trueLabel, String falseLabel) {
		super(label, defaultValue);

		this.trueLabel = trueLabel;
		this.falseLabel = falseLabel;
	}

	/**
	 * Sets the true option label.
	 *
	 * @param label label of the true (or on) option.
	 */
	public void setTrueLabel(String label) {
		this.trueLabel = label;
	}

	/**
	 * Sets the false option label.
	 *
	 * @param label label of the false (or off) option.
	 */
	public void setFalseLabel(String label) {
		this.falseLabel = label;
	}

	@Override
	protected BooleanView newViewInstance() {
		return new BooleanView(this);
	}

	/**
	 * Boolean view with a ToggleGroup of two ToggleButtons.
	 */
	public static class BooleanView extends PersistentParameterBase.ParameterViewBase<BooleanParameter, Boolean, HBox> {

		private final ToggleGroup group = new ToggleGroup();
		private final ToggleButton on;
		private final ToggleButton off;

		public BooleanView(BooleanParameter parameter) {
			super(parameter, new HBox());

			on = new ToggleButton(parameter.trueLabel);
			off = new ToggleButton(parameter.falseLabel);

			on.setToggleGroup(group);
			off.setToggleGroup(group);
			root.getChildren().addAll(off, on);

			set(parameter.get());

			group.selectedToggleProperty().addListener((obs) -> {
				parameter.valueProperty.set(get());
			});
		}

		protected final Boolean get() {
			return group.getSelectedToggle().equals(on);
		}

		@Override
		public final void set(Boolean value) {
			group.selectToggle(value ? on : off);
		}
	}

}
