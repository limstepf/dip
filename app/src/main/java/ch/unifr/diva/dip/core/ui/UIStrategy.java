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

		/**
		 * Affirmative answer.
		 */
		YES,
		/**
		 * Negative answer.
		 */
		NO,
		/**
		 * Cancel.
		 */
		CANCEL
	}

	/**
	 * Checks whether the UI has a stage.
	 *
	 * @return true if there is a stage, false otherwise.
	 */
	default boolean hasStage() {
		return false;
	}

	/**
	 * Sets the stage.
	 *
	 * @param stage the stage.
	 */
	default void setStage(Stage stage) {
	}

	/**
	 * Returns the stage.
	 *
	 * @return the stage, or null.
	 */
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

	/**
	 * Shows an exception or error to the user.
	 *
	 * @param message custom error message.
	 * @param throwable an exception or an error.
	 */
	public void showError(String message, Throwable throwable);

}
