
package ch.unifr.diva.dip.gui.dialogs;

import ch.unifr.diva.dip.api.utils.L10n;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * An information dialog just shows a message.
 */
public class InformationDialog extends AbstractAlert {

	public InformationDialog(String message) {
		super(new Alert(
				AlertType.INFORMATION,
				message,
				new ButtonType(L10n.getInstance().getString("ok"), ButtonBar.ButtonData.YES)
		));
		setTitle(L10n.getInstance().getString("information"));
	}

}
