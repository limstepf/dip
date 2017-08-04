package ch.unifr.diva.dip.utils;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.eventbus.events.CursorNotification;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

/**
 * A (scene-wide) cursor lock. Listens to the cursor property while active, and
 * makes sure a certain cursor doesn't get overwritten. This is primarily used
 * to maintain the {@code Cursor.WAIT} cursor while busy (e.g. while waiting for
 * some background workers/threads), no matter what.
 *
 * <p>
 * A cursor lock sends {@code CursorNotification}s over the event bus, s.t.
 * other components (that do funny cursor stuff) may also respect the lock (upon
 * recieving a {@code LOCK} notification) until it's released again (by a
 * {@code RELEASE_LOCK} notification).
 *
 * <p>
 * Note that a cursor lock doesn't do anything if running headless, so there is
 * no need to only set it up conditionally.
 */
public class CursorLock {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(CursorLock.class);

	protected final ApplicationHandler handler;
	protected final Scene scene;
	protected final ObjectProperty<Cursor> cursorProperty;
	protected final Cursor cursor;
	protected final InvalidationListener cursorListener;
	protected boolean isLocal;

	/**
	 * Creates a new cursor lock.
	 *
	 * @param handler the application handler to get the cursor property from.
	 * @param cursor the desired/locked cursor.
	 */
	public CursorLock(ApplicationHandler handler, Cursor cursor) {
		this.handler = handler;
		this.scene = getScene(handler);
		this.cursorProperty = getCursorProperty(scene);
		this.cursor = cursor;
		this.cursorListener = (c) -> onCursorChanged();
		// cursor might be null, if running headless
		if (cursorProperty != null) {
			if (cursorProperty.isBound()) {
				log.warn(
						"trying to lock an already bound cursor property: {}. Unbinding...",
						cursorProperty
				);
				cursorProperty.unbind();
			}
			cursorProperty.set(cursor);
			handler.eventBus.post(new CursorNotification(
					CursorNotification.Type.LOCK,
					cursor
			));
			cursorProperty.addListener(cursorListener);
		}
	}

	private static Scene getScene(ApplicationHandler handler) {
		final Stage stage = handler.uiStrategy.getStage();
		if (stage == null) {
			return null;
		}
		return stage.getScene();
	}

	private static ObjectProperty<Cursor> getCursorProperty(Scene scene) {
		if (scene == null) {
			return null;
		}
		return scene.cursorProperty();
	}

	protected final void onCursorChanged() {
		if (isLocal) {
			return;
		}
		final Cursor current = cursorProperty.get();
		if (!current.equals(cursor)) {
			isLocal = true;
			cursorProperty.set(cursor);
			isLocal = false;
		}
	}

	/**
	 * Stops the cursor lock, and resets the cursor to {@code Cursor.DEFAULT}.
	 */
	public void stop() {
		stop(Cursor.DEFAULT);
	}

	/**
	 * Stops the cursor lock.
	 *
	 * @param cursor the cursor to reset to, or {@code null}.
	 */
	public void stop(Cursor cursor) {
		if (cursorProperty == null) {
			return;
		}
		handler.eventBus.post(new CursorNotification(
				CursorNotification.Type.RELEASE_LOCK,
				cursor
		));
		cursorProperty.removeListener(cursorListener);
		if (cursor != null) {
			cursorProperty.set(cursor);
		}
	}

}
