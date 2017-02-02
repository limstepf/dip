package ch.unifr.diva.dip.api.parameters;

import java.util.Collection;
import java.util.List;

/**
 * Base class for composite parameters. Implementing classes should call
 * {@code listenToChildren()} in their constructor in order to reflect changes
 * in a child parameter in the composite parameter (e.g. for stuff listening to
 * the composite property). Similarly the {@code valueProperty} probably needs
 * to be invalidated manually by a call to {@code invalidate()}, given the
 * parameter's value is still the same, only its internals (e.g. some child
 * value) changed.
 *
 * <p>
 * Child parameters of a composite parameter do not need to be
 * published/exposed. The composite value is supposed to store all child
 * parameter values (e.g. in a {@code ValueList} or {@code ValueMap}).
 *
 * @param <T> class of the parameter's value. For composite parameters something
 * like a {@code ValueList} or a {@code ValueMap} will do.
 * @param <V> class of the parameter's view.
 * @see ValueList
 * @see ValueMap
 */
public abstract class CompositeBase<T, V extends PersistentParameter.View<T>> extends PersistentParameterBase<T, V> {

	protected boolean enableChildListeners;

	/**
	 * Composite base constructor.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 * @param initialValue initial value.
	 */
	public CompositeBase(String label, T defaultValue, T initialValue) {
		super(label, defaultValue, initialValue);
	}

	/**
	 * Listens to changes in persistent child parameters to invalidate the
	 * composite value. This method should be called in the constructor of
	 * classes that implement CompositeBase.
	 *
	 * @param children persistent child parameters.
	 */
	protected void addChildListeners(List<PersistentParameter> children) {
		for (final PersistentParameter p : children) {
			p.property().addListener((c) -> {
				if (enableChildListeners) {
					invalidateChildParameter(p);
				}
			});
		}

		enableChildListeners(true);
	}

	/**
	 * Disables/(re-)enables child parameter invalidation listeners.
	 *
	 * @param enable True to enable invalidation listeners, False to disable
	 * them.
	 */
	protected void enableChildListeners(boolean enable) {
		this.enableChildListeners = enable;
	}

	/**
	 * Invalidates a child parameter. This method gets called if a persistent
	 * child parameter has been changed. The composite parameter has to update
	 * its internal state/representation (often a list, or a map of child
	 * values) in the implementation of this method.
	 *
	 * @param p the persistent child parameter that has been changed.
	 */
	protected abstract void invalidateChildParameter(PersistentParameter p);

	/**
	 * A filter applied before setting the value property. Composite parameters
	 * need to propagate changes to persistent child parameters, and therefore
	 * can't simply set the new value and be done with it. It might be wise
	 * anyways to do some validation instead of setting the composite value
	 * directly (children might be outdated or missing...).
	 *
	 * <p>
	 * It is highly recommended to disable the invalidation listeners on child
	 * parameters first (in this method by calling
	 * {@code enableChildListeners(false);}), update the children, and re-enable
	 * the listeners at the end (by overwriting the hook method
	 * {@code onValuePropertySet()}), s.t. the composite parameter (parent)
	 * fires/invalidates the valueProperty just a single time.<br />
	 *
	 * In case the object of the valueProperty is still the same (i.e. has the
	 * same hash code), the valueProperty should be invalidated manually (e.g.
	 * in case of a list or map whose values changed).
	 *
	 * @param value the new value.
	 * @return the new, filtered value.
	 */
	@Override
	protected abstract T filterValueProperty(T value);

	@Override
	protected void onValuePropertySet() {
		enableChildListeners(true);
	}

	/**
	 * Returns a list of all child parameters.
	 *
	 * @return a list of all child parameters.
	 */
	protected abstract Collection<? extends Parameter> getChildren();

	/**
	 * Checks whether this composite has at least one persistent child
	 * parameter.
	 *
	 * @return True if there are persistent child parameters, False otherwise.
	 */
	protected abstract boolean hasPersistentChildren();

	@Override
	public boolean isPersistent() {
		return hasPersistentChildren();
	}

}
