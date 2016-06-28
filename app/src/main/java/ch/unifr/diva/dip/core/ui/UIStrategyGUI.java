package ch.unifr.diva.dip.core.ui;

import ch.unifr.diva.dip.gui.dialogs.ConfirmationDialog;
import ch.unifr.diva.dip.gui.dialogs.ErrorDialog;
import ch.unifr.diva.dip.gui.dialogs.InformationDialog;
import ch.unifr.diva.dip.gui.dialogs.WarningDialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Stage;

/**
 * Error handler for the graphical user interface.
 */
public class UIStrategyGUI implements UIStrategy {

	private volatile Stage stage;

	@Override
	public boolean hasStage() {
		return (stage != null);
	}

	@Override
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@Override
	public Stage getStage() {
		return this.stage;
	}

	@Override
	public void showError(Throwable throwable) {
		final ErrorDialog dialog = new ErrorDialog(throwable);
		dialog.showAndWait();
	}

	@Override
	public void showInformation(String message) {
		final InformationDialog dialog = new InformationDialog(message);
		dialog.showAndWait();
	}

	@Override
	public void showWarning(String message) {
		final WarningDialog dialog = new WarningDialog(message);
		dialog.showAndWait();
	}

	@Override
	public Answer getAnswer(String message) {
		final ConfirmationDialog dialog = new ConfirmationDialog(message);
		dialog.showAndWait();
		final ButtonData result = dialog.getResult().getButtonData();

		if (result.equals(ButtonData.YES)
				|| result.equals(ButtonData.OK_DONE)
				|| result.equals(ButtonData.APPLY)) {
			return Answer.YES;
		}

		if (result.equals(ButtonData.CANCEL_CLOSE)) {
			return Answer.CANCEL;
		}

		return Answer.NO;
	}

}
