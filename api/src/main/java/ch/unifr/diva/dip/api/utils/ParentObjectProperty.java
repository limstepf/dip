package ch.unifr.diva.dip.api.utils;

import javafx.beans.property.SimpleObjectProperty;

/**
 * A parent object property. Can (manually) fire invalidation/change
 * notifications for internal changes of the (same) wrapped object.
 *
 * @param <T> the type of the wrapped Object.
 */
public class ParentObjectProperty<T> extends SimpleObjectProperty<T> {

	private static final Object DEFAULT_BEAN = null;
	private static final String DEFAULT_NAME = "";

	public ParentObjectProperty() {
		this(DEFAULT_BEAN, DEFAULT_NAME);
	}

	public ParentObjectProperty(T initialValue) {
		this(DEFAULT_BEAN, DEFAULT_NAME, initialValue);
	}

	public ParentObjectProperty(Object bean, String name) {
		super(bean, name);
	}

	public ParentObjectProperty(Object bean, String name, T initialValue) {
		super(bean, name, initialValue);
	}

	/**
	 * Sends notifications to all attached InvalidationListeners and
	 * ChangeListeners. Manually fires a value changed event.
	 */
	public void invalidate() {
		this.fireValueChangedEvent();
	}
}
