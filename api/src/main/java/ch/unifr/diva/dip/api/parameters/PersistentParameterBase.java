package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.utils.ParentObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;

/**
 * Persistent parameter base class. Implements most boilerplate code of a
 * parameter leaving only/mostly the view to be implemented.
 *
 * @param <T> class of the parameter's value.
 */
public abstract class PersistentParameterBase<T> implements PersistentParameter<T> {

	protected PersistentParameter.View view = null;
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

	protected abstract PersistentParameter.View newViewInstance();

	@Override
	public Parameter.View<T> view() {
		if (view == null) {
			view = newViewInstance();
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
