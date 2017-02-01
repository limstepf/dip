package ch.unifr.diva.dip.api.ui;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 * A toggle group with a value property.
 *
 * @param <T> class of the value (set with {@code setUserData()}).
 */
public class ToggleGroupValue<T> extends ToggleGroup {

	private final boolean ignoreNullValues;

	/**
	 * Creates a new toggle group value. Null values are ignored.
	 */
	public ToggleGroupValue() {
		this(true);
	}

	/**
	 * Creates a new toggle group value.
	 *
	 * @param ignoreNullValues whether or not null values should be ignored. If
	 * null values are not ignored, the value property will fire twice upon
	 * selecting a different toggle (once to deselect with a null value, and a
	 * second time to select the new toggle). This does not happen if we ignore
	 * null values, which is nice for toggle groups where some toggle always has
	 * to be selected.
	 */
	public ToggleGroupValue(boolean ignoreNullValues) {
		this.ignoreNullValues = ignoreNullValues;

		selectedToggleProperty().addListener((Observable toggleProperty) -> {
			final Toggle selected = getSelectedToggle();
			if (selected == null) {
				if (!ignoreNullValues) {
					valueProperty.set(null);
				}
			} else {
				valueProperty.set((T) selected.getUserData());
			}
		});
	}

	/**
	 * Adds a toggle with a value (user data) to the toggle group.
	 *
	 * @param toggle the new toggle.
	 * @param value the toggle's value (user data).
	 */
	public void add(Toggle toggle, T value) {
		toggle.setToggleGroup(this);
		toggle.setUserData(value);
	}

	protected final ObjectProperty<T> valueProperty = new SimpleObjectProperty<T>(this, "value", null) {
		@Override
		public void set(T value) {
			super.set(value);

			// deselect toggle
			if (value == null) {
				if (getSelectedToggle() != null) {
					selectToggle(null);
				}
				return;
			}

			// select toggle with given value
			for (Toggle toggle : getToggles()) {
				if (toggle.getUserData() != null && toggle.getUserData().equals(value)) {
					if (getSelectedToggle() != toggle) {
						selectToggle(toggle);
					}
					return;
				}
			}
		}
	};

	/**
	 * The value property of the toggle group. This value is the user data set
	 * on the currently selected toggle in the toggle group.
	 *
	 * <p>
	 * This property always fires twice upon selecting a different toggle if
	 * null values are not ignored. Once to deselect the current toggle (which
	 * sets the value to null), and a second time to select the new toggle
	 * (which sets the value to the new value).
	 *
	 * @return the value property.
	 */
	public ObjectProperty<T> valueProperty() {
		return this.valueProperty;
	}

	/**
	 * Selects a toggle by its value (user data).
	 *
	 * @param value the value (user data) of the toggle.
	 */
	public void setValue(T value) {
		this.valueProperty.set(value);
	}

	/**
	 * Returns the value (user data) of the toggle group.
	 *
	 * @return the value (user data) of the toggle group.
	 */
	public T getValue() {
		return this.valueProperty.get();
	}

}
