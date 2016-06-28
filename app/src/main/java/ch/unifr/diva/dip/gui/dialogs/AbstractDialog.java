package ch.unifr.diva.dip.gui.dialogs;

import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.gui.layout.AbstractStage;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Abstract class to build custom dialogs.
 */
public abstract class AbstractDialog extends AbstractStage {

	protected final List<Button> buttons;

	/**
	 * Creates a new dialog without title.
	 *
	 * @param owner owner/parent window.
	 */
	public AbstractDialog(Window owner) {
		this(owner, "");
	}

	/**
	 * Creates a new dialog.
	 *
	 * @param owner owner/parent window.
	 * @param title title of the dialog.
	 */
	public AbstractDialog(Window owner, String title) {
		super(owner, title);

		stage.initStyle(StageStyle.UTILITY);
		stage.initModality(Modality.WINDOW_MODAL);
		root.getStyleClass().add("dip-dialog");

		buttons = new ArrayList<>();
	}

	private void prepareDialog() {
		if (buttons.size() > 0) {
			final HBox hbox = new HBox();
			hbox.getStyleClass().add("dip-button-bar");
			hbox.getChildren().addAll(buttons);
			root.setBottom(hbox);
		}
		stage.sizeToScene();
	}

	@Override
	public void show() {
		prepareDialog();
		super.show();
	}

	@Override
	public void showAndWait() {
		prepareDialog();
		super.showAndWait();
	}

	/**
	 * Creates a default button with the label "OK".
	 *
	 * @return A default button.
	 */
	public static Button getOkButton() {
		return getDefaultButton(L10n.getInstance().getString("ok"));
	}

	/**
	 * Creates a default button with a custom label.
	 *
	 * @param label Label of the button.
	 * @return A default button.
	 */
	public static Button getDefaultButton(String label) {
		final Button button = new Button(label);
		button.setDefaultButton(true);
		return button;
	}

	/**
	 * Creates a default cancel button.
	 *
	 * @return A default cancel button.
	 */
	public static Button getCancelButton() {
		return getCancelButton(null);
	}

	/**
	 * Create a default cancel button hooked to hide the given window.
	 *
	 * @param window Window to be closed on activating the button.
	 * @return A default cancel button.
	 */
	public static Button getCancelButton(Window window) {
		final Button button = new Button(L10n.getInstance().getString("cancel"));
		button.setCancelButton(true);
		if (window != null) {
			button.setOnAction((ActionEvent e) -> {
				window.hide();
			});
		}
		return button;
	}

}
