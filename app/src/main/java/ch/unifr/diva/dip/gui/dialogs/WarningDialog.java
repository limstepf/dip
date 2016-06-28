
package ch.unifr.diva.dip.gui.dialogs;

import ch.unifr.diva.dip.api.utils.L10n;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * A warning dialog.
 */
public class WarningDialog extends AbstractAlert {

	public WarningDialog(String message) {
		super(new Alert(
				Alert.AlertType.WARNING,
				message,
				new ButtonType(L10n.getInstance().getString("ok"), ButtonBar.ButtonData.YES)
		));
		setTitle(L10n.getInstance().getString("warning"));
	}

}
