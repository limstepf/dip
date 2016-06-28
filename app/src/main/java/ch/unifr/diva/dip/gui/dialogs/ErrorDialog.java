package ch.unifr.diva.dip.gui.dialogs;

import ch.unifr.diva.dip.api.utils.L10n;
import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;

/**
 * Error dialog based on Alert. Can show the stacktrace of an exception.
 */
public class ErrorDialog extends AbstractAlert {

	/**
	 * ErrorDialog constructor.
	 *
	 * @param throwable An exception, an error or null.
	 */
	public ErrorDialog(Throwable throwable) {
		this(null, throwable);
	}

	/**
	 * ErrorDialog constructor.
	 *
	 * @param error Custom error message or null.
	 * @param throwable An exception, an error or null.
	 */
	public ErrorDialog(String error, Throwable throwable) {
		super(new Alert(AlertType.ERROR));
		setTitle(L10n.getInstance().getString("error"));

		if (throwable != null) {
			alert.setContentText(throwable.getMessage());
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			final String exText = sw.toString();
			final TextArea textArea = new TextArea(exText);
			textArea.setEditable(false);
			textArea.setWrapText(true);
			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			alert.getDialogPane().setExpandableContent(textArea);
		}
	}

}
