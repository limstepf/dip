package ch.unifr.diva.dip.core.ui;

import javafx.stage.Stage;

/**
 * UIStrategy implements some generic user interaction.
 */
public interface UIStrategy {

	/**
	 * UI answers.
	 */
	public enum Answer {

		YES,
		NO,
		CANCEL
	}

	default boolean hasStage() {
		return false;
	}

	default void setStage(Stage stage) {
	}

	default Stage getStage() {
		return null;
	}

	/**
	 * Asks the user a yes/no/cancel question.
	 *
	 * @param message a yes/no/cancel question.
	 * @return the answer (yes, no, or cancel).
	 */
	public Answer getAnswer(String message);

	/**
	 * Shows an information to the user.
	 *
	 * @param message the message.
	 */
	public void showInformation(String message);

	/**
	 * Shows a warning to the user.
	 *
	 * @param message the message.
	 */
	public void showWarning(String message);

	/**
	 * Shows an exception or error to the user.
	 *
	 * @param throwable an exception or an error.
	 */
	public void showError(Throwable throwable);
}
