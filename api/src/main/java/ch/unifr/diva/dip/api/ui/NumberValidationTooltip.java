package ch.unifr.diva.dip.api.ui;

import ch.unifr.diva.dip.api.utils.L10n;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Tooltip;

/**
 * Tooltip for Number validation and feedback.
 *
 * @param <T> subclass of Number.
 */
public class NumberValidationTooltip<T extends Number & Comparable<T>> extends Tooltip {

	private BooleanProperty validProperty = new SimpleBooleanProperty(true);

	public NumberValidationTooltip() {
		super();
	}

	public ReadOnlyBooleanProperty validProperty() {
		return this.validProperty;
	}

	public boolean isValid() {
		return this.validProperty.get();
	}

	public void reset(T value) {
		this.setText((value == null) ? "" : value.toString());
		this.validProperty.set(true);
	}

	public void setOutOfRange(T value, T minimum, T maximum) {
		if (value == null) {
			setParseError(value);
			return;
		}

		if (value.compareTo(minimum) < 0) {
			// too small
			this.setText(L10n.getInstance().getString(
					"error.number.minimum",
					value, minimum, maximum
			));
			this.validProperty.set(false);
		} else if (value.compareTo(maximum) > 0) {
			// too large
			this.setText(L10n.getInstance().getString(
					"error.number.maximum",
					value, minimum, maximum
			));
			this.validProperty.set(false);
		} else {
			// not out of range
			reset(value);
		}
	}

	public void setParseError(T value) {
		if (value == null) {
			// parse error
			this.setText(L10n.getInstance().getString(
					"error.number.parse"
			));
			this.validProperty.set(false);
		} else {
			// no parse error
			reset(value);
		}
	}
}
