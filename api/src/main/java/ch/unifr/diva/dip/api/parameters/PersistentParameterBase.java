package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.utils.ParentObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
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

	public PersistentParameterBase(String label, T defaultValue) {
		this(label, defaultValue, defaultValue);
	}

	public PersistentParameterBase(String label, T defaultValue, T initialValue) {
		this.label = label;
		this.defaultValue = defaultValue;
		this.valueProperty = new ParentObjectProperty(initialValue);
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
		if (view != null) {
			view.set(value);
		}
	}

	@Override
	public ReadOnlyObjectProperty<T> property() {
		return this.valueProperty;
	}

	@Override
	public void setHide(boolean hide) {
		isHidden = hide;
		if (view != null) {
			view.setHide(this.isHidden);
		}
	}

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

		protected final P parameter;
		protected final N root;

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
