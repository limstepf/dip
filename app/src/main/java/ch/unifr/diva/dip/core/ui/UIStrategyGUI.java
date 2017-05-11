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
	public void showError(String message, Throwable throwable) {
		final ErrorDialog dialog = new ErrorDialog(message, throwable);
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

		/**
		 * Inverted (or contrast) color to the highlighting/accenting color.
		 * Used as a backup accent color in cases where the background color is
		 * the accent color (e.g. for selected list cells).
		 */
		public static final Color accent_inverted = Color.WHITE;

		/**
		 * Ok status color.
		 */
		public static final Color ok = Color.FORESTGREEN;

		/**
		 * Warning/alert status color.
		 */
		public static final Color warning = Color.GOLD;

		/**
		 * Error status color. Also used for the processor states
		 * {@code UNAVAILABLE} and {@code UNCONNECTED}.
		 */
		public static final Color error = Color.ORANGERED;

		/**
		 * Waiting status color. Used to indicate the processor state
		 * {@code WAITING}.
		 */
		public static final Color waiting = Color.ORANGE;

		/**
		 * Processing status color. Used to indicate the processor state
		 * {@code PROCESSING}.
		 */
		public static final Color processing = accent;

		/**
		 * Manual processing status color. Used to indicate the processor state
		 * {@code PROCESSING} (manual).
		 */
		public static final Color processingEdit = warning;

		/**
		 * Ready status color. Used to indicate the processor state
		 * {@code READY}.
		 */
		public static final Color ready = ok;

		/**
		 * Ready, but editable, status color. Used to indicate the processor
		 * state {@code READY} (still manually editable).
		 */
		public static final Color readyEdit = Color.LIMEGREEN;

	}

	/**
	 * Application-wide animation/transition settings.
	 */
	public static class Animation {

		/**
		 * Time in milliseconds to delay popping in/up nodes. E.g. a context
		 * menu while holding the mouse button down.
		 */
		public static final int delayDuration = 320;

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

		/**
		 * Default ok glyph.
		 */
		public static final NamedGlyph ok = MaterialDesignIcons.CHECK_CIRCLE;

		/**
		 * Default warning/alert glyph.
		 */
		public static final NamedGlyph warning = MaterialDesignIcons.ALERT_CIRCLE;

		/**
		 * Default error glyph.
		 */
		public static final NamedGlyph error = MaterialDesignIcons.CLOSE_CIRCLE;

		/**
		 * Returns a new ok status glyph.
		 *
		 * @param size the size of the glyph.
		 * @return new ok status glyph.
		 */
		public static Glyph newOkGlyph(Glyph.Size size) {
			return newGlyph(ok, Glyph.Size.NORMAL, Colors.ok);
		}

		/**
		 * Returns a new warning/alert status glyph.
		 *
		 * @param size the size of the glyph.
		 * @return new warning status glyph.
		 */
		public static Glyph newWarningGlyph(Glyph.Size size) {
			return newGlyph(warning, Glyph.Size.NORMAL, Colors.warning);
		}

		/**
		 * Returns a new error status glyph.
		 *
		 * @param size the size of the glyph.
		 * @return new error status glyph.
		 */
		public static Glyph newErrorGlyph(Glyph.Size size) {
			return newGlyph(error, Glyph.Size.NORMAL, Colors.error);
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
		 * @param size the size of the glyph. Consider using one of the
		 * standardized sizes defined on the {@code Size} enum. The font size
		 * specified in points which are a real world measurement of
		 * approximately 1/72 inch.
		 * @return a new glyph.
		 */
		public static Glyph newGlyph(NamedGlyph glyph, Glyph.Size size) {
			return newGlyph(glyph, size, Colors.accent);
		}

		/**
		 * Creates a new glyph.
		 *
		 * @param glyph the named glyph (or glyph factory).
		 * @param size the size of the glyph. Consider using one of the
		 * standardized sizes defined on the {@code Size} enum. The font size
		 * specified in points which are a real world measurement of
		 * approximately 1/72 inch.
		 * @param color the color of the glyph.
		 * @return a new glyph.
		 */
		public static Glyph newGlyph(NamedGlyph glyph, Glyph.Size size, Color color) {
			final Glyph g = glyph.get(size.pt);
			g.setColor(color);
			return g;
		}
	}

}
