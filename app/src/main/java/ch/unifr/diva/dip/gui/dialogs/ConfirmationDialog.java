package ch.unifr.diva.dip.gui.dialogs;

import ch.unifr.diva.dip.api.utils.L10n;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

/**
 * A confirmation dialog.
 */
public class ConfirmationDialog extends AbstractAlert {

	public ConfirmationDialog(String message) {
		this(message,
				new ButtonType(L10n.getInstance().getString("ok"), ButtonData.YES),
				new ButtonType(L10n.getInstance().getString("no"), ButtonData.NO),
				new ButtonType(L10n.getInstance().getString("cancel"), ButtonData.CANCEL_CLOSE)
		);
	}

	public ConfirmationDialog(String message, ButtonType... buttons) {
		super(new Alert(
				AlertType.CONFIRMATION,
				message,
				buttons
		));
		setTitle(L10n.getInstance().getString("confirm"));
	}

}
