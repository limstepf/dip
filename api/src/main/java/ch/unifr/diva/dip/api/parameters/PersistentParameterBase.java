package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.utils.ParentObjectProperty;
import javafx.scene.Node;
import org.slf4j.LoggerFactory;

/**
 * Persistent parameter base class. Implements most boilerplate code of a
 * parameter leaving only/mostly the view to be implemented.
 *
 * <p>
 * While the view, extending from {@code PersistentParameter.View<T>} is
 * generic, keep in mind that once typed, subclasses have to share that view. So
 * it's often easier to not extend some basic integer parameter (with a simple
 * view), but rather extend directly from here again.
 *
 * @param <T> class of the parameter's value.
 * @param <V> class of the parameter's view.
 */
public abstract class PersistentParameterBase<T, V extends PersistentParameter.View<T>> implements PersistentParameter<T> {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(PersistentParameterBase.class);
	protected final Class<T> valueClass;
	protected V view;
	protected boolean isHidden;
	protected final String label;
	protected final T defaultValue;
	protected final ParentObjectProperty<T> valueProperty;
	protected boolean changeIsLocal;

	/**
	 * Creates a new persistent parameter base.
	 *
	 * @param label the label of the parameter.
	 * @param valueClass the parameter's value class {@code T}.
	 * @param defaultValue the default value.
	 */
	public PersistentParameterBase(String label, Class<T> valueClass, T defaultValue) {
		this(label, valueClass, defaultValue, defaultValue);
	}

	/**
	 * Creates a new persistent parameter base.
	 *
	 * @param label the label of the parameter.
	 * @param valueClass the parameter's value class {@code T}.
	 * @param defaultValue the default value.
	 * @param initialValue the initial value.
	 */
	public PersistentParameterBase(String label, Class<T> valueClass, T defaultValue, T initialValue) {
		this.label = label;
		this.defaultValue = defaultValue;
		this.valueClass = valueClass;
		this.valueProperty = new ParentObjectProperty<T>(initialValue) {
			@Override
			public void set(T value) {
				final T newVal = filterValueProperty(value);
				final T val = super.get();
				if ((val == null && newVal != null) || (val != null && !val.equals(newVal))) {
					super.set(newVal);
					if (!changeIsLocal && view != null) {
						view.set(super.get());
					}
					onValuePropertySet(true);
				} else {
					onValuePropertySet(false);
				}
			}
		};
	}

	@Override
	public void invalidate() {
		this.valueProperty.invalidate();
		if (!changeIsLocal && view != null) {
			view.set(get());
		}
		onValuePropertySet(true);
	}

	/**
	 * A filter applied before setting the value property. As is, this is just a
	 * pass-through, but can be overwritten if needed (e.g. to validate the new
	 * value first). The new value may be simply ignored by returning the
	 * current value by a call to {@code get()}.
	 *
	 * @param value the new value.
	 * @return the new, filtered value.
	 */
	protected T filterValueProperty(T value) {
		return value;
	}

	/**
	 * Hook method that gets called after the value property has been
	 * set/updated. As is, this method does nothing. Primarily used to propagate
	 * a changed value to child-parameters (if {@code changed} is {@code true}).
	 *
	 * @param changed {@code true} if the value actually changed ({@code set()}
	 * might ignore the value, or if explicitly {@code invalidate()}d,
	 * {@code false} otherwise, that is {@code set()} got called, but the value
	 * has been ignored (for being invalid or equal to the current value).
	 */
	protected void onValuePropertySet(boolean changed) {

	}

	@Override
	public String label() {
		return this.label;
	}

	@Override
	public T defaultValue() {
		return this.defaultValue;
	}

	@Override
	public T get() {
		return this.valueProperty.get();
	}

	@Override
	public void set(T value) {
		this.valueProperty.set(value);
	}

	@Override
	public Class<T> getValueClass() {
		return this.valueClass;
	}

	/**
	 * Set method to be used by the views to update the parameter.
	 *
	 * @param value the new value.
	 */
	protected void setLocal(T value) {
		this.changeIsLocal = true;
		this.valueProperty.set(value);
		this.changeIsLocal = false;
	}

	@Override
	public ParentObjectProperty<T> property() {
		return this.valueProperty;
	}

	@Override
	public void setHide(boolean hide) {
		isHidden = hide;
		if (view != null) {
			view.setHide(this.isHidden);
		}
	}

	/**
	 * Creates a new instance of a persistent parameter view.
	 *
	 * @return a new instance of a persistent parameter view.
	 */
	protected abstract V newViewInstance();

	@Override
	public V view() {
		if (view == null) {
			view = newViewInstance();
			view.setHide(isHidden);
		}
		return view;
	}

	/**
	 * Parameter view base class.
	 *
	 * @param <P> class of the parameter.
	 * @param <T> class of the parameter's value.
	 * @param <N> class of the view's root node, and subclass of Node.
	 */
	public static abstract class ParameterViewBase<P extends PersistentParameter<T>, T, N extends Node> implements PersistentParameter.View<T> {

		/**
		 * The parameter.
		 */
		protected final P parameter;

		/**
		 * The root node of the view.
		 */
		protected final N root;

		/**
		 * Creates a new parameter view base.
		 *
		 * @param parameter the parameter.
		 * @param root the root node of the view.
		 */
		public ParameterViewBase(P parameter, N root) {
			this.parameter = parameter;
			this.root = root;
		}

		@Override
		public PersistentParameter<T> parameter() {
			return this.parameter;
		}

		@Override
		public Node node() {
			return this.root;
		}

	}

}
