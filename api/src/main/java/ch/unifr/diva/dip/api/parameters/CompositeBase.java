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
 */
public abstract class CompositeBase<T, V extends PersistentParameter.View<T>> extends PersistentParameterBase<T, V> {

	protected boolean enableChildListeners;

	/**
	 * Composite base constructor.
	 *
	 * @param label label.
	 * @param valueClass the parameter's value class {@code T}.
	 * @param defaultValue default value.
	 * @param initialValue initial value.
	 */
	public CompositeBase(String label, Class<T> valueClass, T defaultValue, T initialValue) {
		super(label, valueClass, defaultValue, initialValue);
	}

	/**
	 * Listens to changes in persistent child parameters to invalidate the
	 * composite value. This method should be called in the constructor of
	 * classes that implement CompositeBase.
	 *
	 * @param children persistent child parameters.
	 */
	protected void addChildListeners(List<PersistentParameter<?>> children) {
		for (final PersistentParameter<?> p : children) {
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
	 * @param enable {@code true} to enable invalidation listeners,
	 * {@code false} to disable them.
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
	protected abstract void invalidateChildParameter(PersistentParameter<?> p);

	/**
	 * Returns a list of all child parameters.
	 *
	 * @return a list of all child parameters.
	 */
	protected abstract Collection<? extends Parameter<?>> getChildren();

	/**
	 * Checks whether this composite has at least one persistent child
	 * parameter.
	 *
	 * @return {@code true} if there are persistent child parameters,
	 * {@code false} otherwise.
	 */
	protected abstract boolean hasPersistentChildren();

	@Override
	public boolean isPersistent() {
		return hasPersistentChildren();
	}

}
