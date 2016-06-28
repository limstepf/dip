package ch.unifr.diva.dip.utils;

/**
 * A modifiable object has a "dirty-bit" in form of a {@code ModifiedProperty}.
 * Modifiable objects listen to their own properties for changes and manages
 * other child objects that are modifiable. That is: a dirty child object will
 * also make its parent object dirty, and the other way around: a "cleaned" (or
 * saved) parent object will clean its child objects too.
 */
public interface Modifiable {

	/**
	 * Checks whether the object has been modified (since being opened or
	 * saved).
	 *
	 * @return True if the object has been modified and needs to be saved, False
	 * otherwise.
	 */
	default boolean isModified() {
		return modifiedProperty().get();
	}

	/**
	 * Changes the modification state of the modifiable object.
	 *
	 * @param modified True to mark this object as modified, False otherwise.
	 */
	default void setModified(boolean modified) {
		modifiedProperty().set(modified);
	}

	/**
	 * Property of the object's modification state.
	 *
	 * @return the modifiedProperty.
	 */
	ModifiedProperty modifiedProperty();
}
