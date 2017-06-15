package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.utils.ParentObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;

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
	 * @param defaultValue the default value.
	 */
	public PersistentParameterBase(String label, T defaultValue) {
		this(label, defaultValue, defaultValue);
	}

	/**
	 * Creates a new persistent parameter base.
	 *
	 * @param label the label of the parameter.
	 * @param defaultValue the default value.
	 * @param initialValue the initial value.
	 */
	public PersistentParameterBase(String label, T defaultValue, T initialValue) {
		this.label = label;
		this.defaultValue = defaultValue;
		this.valueProperty = new ParentObjectProperty<T>(initialValue) {
			@Override
			public void set(T value) {
				final T newVal = filterValueProperty(value);
				final T val = get();
				if ((val == null && newVal != null) || (val != null && !val.equals(newVal))) {
					super.set(newVal);
					if (!changeIsLocal && view != null) {
						view.set(get());
					}
				}
				onValuePropertySet();
			}
		};
	}

	/**
	 * Invalidates the parameter's value property. The value property doesn't
	 * get invalidated if the new value is the same as the old one. But with
	 * more complex value objects it's often convenient to retrieve the value
	 * (or rather data structure) and alter it, thus also altering the already
	 * set value (without invalidating the property). Trying to set this value
	 * will not invalidate the property, since it's the same as the current
	 * value, so... in such a situation a call to this method will do the trick,
	 * and also update the view (if present).
	 *
	 * <h4>Example:</h4>
	 * <pre>
	 * <code>
	 * ExpMatrixParameter matrix = new ExpMatrixParameter("mat", new StringMatrix(3,3));
	 * // ...
	 * // retrieve the value of the property, a StringMatrix in this case,
	 * // and alter it directly, followed by a call to invalidate() instead of
	 * // trying to set the value of the parameter.
	 * matrix.get().fill("e");
	 * matrix.invalidate();
	 * </code>
	 * </pre>
	 *
	 * <p>
	 * In the same situation, the following:
	 * <pre>
	 * <code>
	 * // ...
	 * matrix.set(matrix.get().fill("e"));
	 * </code>
	 * </pre> ...would not work. The call to set would be ignored/not have the
	 * desired effect, since that value is already set. Alternatively we could
	 * create a new StringMatrix from scratch, or (deep-)clone the current
	 * matrix before altering it, but that's rather stupid...
	 */
	public void invalidate() {
		this.valueProperty.invalidate();
		if (!changeIsLocal && view != null) {
			view.set(get());
		}
		onValuePropertySet();
	}

	/**
	 * A filter applied before setting the value property. As is, this is just a
	 * pass-through, but can be overwritten if needed (e.g. to validate the new
	 * value first).
	 *
	 * @param value the new value.
	 * @return the new, filtered value.
	 */
	protected T filterValueProperty(T value) {
		return value;
	}

	/**
	 * Hook method that gets called after the value property has been
	 * set/updated. In case the {@code filterValueProperty} method is used to
	 * disable some listeners first, this method can be overwritten to re-enable
	 * them (or whatever). As is this method does nothing.
	 */
	protected void onValuePropertySet() {

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
	public ObjectProperty<T> property() {
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
