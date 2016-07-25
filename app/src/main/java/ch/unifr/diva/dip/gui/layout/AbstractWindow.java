package ch.unifr.diva.dip.gui.layout;

import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * An abstract window. Blocks it's owner while open.
 */
public abstract class AbstractWindow extends AbstractStage {

	/**
	 * Create a window without title.
	 *
	 * @param owner owner/parent window.
	 */
	public AbstractWindow(Window owner) {
		this(owner, "");
	}

	/**
	 * Create a window.
	 *
	 * @param owner owner/parent window.
	 * @param title title of the window.
	 */
	public AbstractWindow(Window owner, String title) {
		super(owner, title);

		stage().initStyle(StageStyle.DECORATED);
		stage().initModality(Modality.WINDOW_MODAL);
		stage().setMinWidth(UIStrategyGUI.Stage.minWidth);
		stage().setMinHeight(UIStrategyGUI.Stage.minHeight);
		root.getStyleClass().add("dip-window");
	}

}
