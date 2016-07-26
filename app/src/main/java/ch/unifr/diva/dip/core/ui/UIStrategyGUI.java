package ch.unifr.diva.dip.core.ui;

import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.MaterialDesignIcons;
import ch.unifr.diva.dip.gui.dialogs.ConfirmationDialog;
import ch.unifr.diva.dip.gui.dialogs.ErrorDialog;
import ch.unifr.diva.dip.gui.dialogs.InformationDialog;
import ch.unifr.diva.dip.gui.dialogs.WarningDialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.paint.Color;

/**
 * UI strategy for the graphical user interface. In addition to the bare minimum
 * required to implement the {@code UIStrategy}, there are also all kind of GUI
 * related constants defined here.
 */
public class UIStrategyGUI implements UIStrategy {

	private javafx.stage.Stage stage;

	@Override
	public boolean hasStage() {
		return (stage != null);
	}

	@Override
	public void setStage(javafx.stage.Stage stage) {
		this.stage = stage;
	}

	@Override
	public javafx.stage.Stage getStage() {
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

	/**
	 * Default stage/window settings.
	 */
	public static class Stage {

		/**
		 * Minimum width of a stage/window.
		 */
		public static final int minWidth = 480;

		/**
		 * Minimum height of a stage/window.
		 */
		public static final int minHeight = 360;

		/**
		 * Default insets spacing (or padding). If in doubt, use this global
		 * value (e.g. for spacing between elements in a lane or what not)
		 * instead of introducing yet another magic variable.
		 */
		public static final int insets = 5;

	}

	/**
	 * Application wide colors. Because sometimes CSS just doesn't cut it, and
	 * we need to to things programmatically.
	 */
	public static class Colors {

		/**
		 * Color for highlighting/accenting objects. This is the
		 * {@code -fx-accent} color as defined by the Modena style sheet we're
		 * using as a base.
		 */
		public static final Color accent = Color.web("0x0096C9");

	}

	/**
	 * Application-wide animation/transition settings.
	 */
	public static class Animation {

		/**
		 * Time in milliseconds to display auto-disappearing nodes.
		 */
		public static final int displayDuration = 5500;

		/**
		 * Time in milliseconds to fade out nodes.
		 */
		public static final int fadeOutDuration = 3000;

		/**
		 * Time in milliseconds to fade in nodes.
		 */
		public static final int fadeInDuration = fadeOutDuration / 4;
	}

	/**
	 * Application-wide glyphs.
	 */
	public static class Glyphs {

		/**
		 * Default processor glyph. Used as default if a processor doesn't have
		 * a special glyph.
		 */
		public static final NamedGlyph defaultProcessor = MaterialDesignIcons.CHIP;
	}

	/**
	 * Creates a new glyph in normal size and default color.
	 *
	 * @param glyph the named glyph (or glyph factory).
	 * @return a new glyph.
	 */
	public static Glyph newGlyph(NamedGlyph glyph) {
		return newGlyph(glyph, Glyph.Size.NORMAL, Colors.accent);
	}

	/**
	 * Creates a new glyph in default color.
	 *
	 * @param glyph the named glyph (or glyph factory).
	 * @param size the size of the glyph. Consider using one of the standardized
	 * sizes defined on the {@code Size} enum. The font size specified in points
	 * which are a real world measurement of approximately 1/72 inch.
	 * @return a new glyph.
	 */
	public static Glyph newGlyph(NamedGlyph glyph, Glyph.Size size) {
		return newGlyph(glyph, size, Colors.accent);
	}

	/**
	 * Creates a new glyph.
	 *
	 * @param glyph the named glyph (or glyph factory).
	 * @param size the size of the glyph. Consider using one of the standardized
	 * sizes defined on the {@code Size} enum. The font size specified in points
	 * which are a real world measurement of approximately 1/72 inch.
	 * @param color the color of the glyph.
	 * @return a new glyph.
	 */
	public static Glyph newGlyph(NamedGlyph glyph, Glyph.Size size, Color color) {
		final Glyph g = glyph.get(size.pt);
		g.setColor(color);
		return g;
	}

}
