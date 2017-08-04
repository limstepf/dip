package ch.unifr.diva.dip.eventbus.events;

import javafx.scene.Cursor;

/**
 * Cursor notification.
 */
public class CursorNotification {

	/**
	 * Type of the cursor notification.
	 */
	public enum Type {

		/**
		 * Notification to lock to the given cursor.
		 */
		LOCK,
		/**
		 * Notification to release a lock, and return to default.
		 */
		RELEASE_LOCK;
	}

	/**
	 * Cursor notification type.
	 */
	public final Type type;

	/**
	 * A cursor.
	 */
	public final Cursor cursor;

	/**
	 * Creates a new cursor notification.
	 *
	 * @param type notification type.
	 */
	public CursorNotification(Type type) {
		this(type, null);
	}

	/**
	 * Creates a new cursor notification.
	 *
	 * @param type notification type.
	 * @param cursor a {@code Cursor}, or {@code null}.
	 */
	public CursorNotification(Type type, Cursor cursor) {
		this.type = type;
		this.cursor = cursor;
	}

}
