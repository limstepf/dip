
package ch.unifr.diva.dip.gui.dialogs;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Alert wrapper for standard dialogs.
 */
public abstract class AbstractAlert {
	protected final Alert alert;

	/**
	 * Constructs a wrapped alert.
	 * @param alert a JavaFX alert.
	 */
	public AbstractAlert(Alert alert) {
		this.alert = alert;
	}

	/**
	 * Sets the title only.
	 * @param title the title.
	 */
	public final void setTitle(String title) {
		setTitle(title, null);
	}

	/**
	 * Sets title and header text.
	 *
	 * @param title the title.
	 * @param headerText the header text.
	 */
	public final void setTitle(String title, String headerText) {
		alert.setTitle(title);
		alert.setHeaderText(title);
	}

	/**
	 * Shows the dialog.
	 */
	public void show() {
		alert.show();
	}

	/**
	 * Shows the dialog and waits for the user response.
	 * @return the user response.
	 */
	public Optional<ButtonType> showAndWait() {
		return alert.showAndWait();
	}

	/**
	 * Returns the user response.
	 * @return the user response.
	 */
	public ButtonType getResult() {
		return alert.getResult();
	}
}
