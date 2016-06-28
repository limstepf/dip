package ch.unifr.diva.dip.api.parameters;

import java.util.Collection;
import java.util.List;
import javafx.beans.InvalidationListener;

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
 * @see ValueList
 * @see ValueMap
 */
public abstract class CompositeBase<T> extends PersistentParameterBase<T> {

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

	private final InvalidationListener invalidationListener = (c) -> {
		this.valueProperty.invalidate();
	};

	/**
	 * Listens to changes in persistent child parameters to invalidate the
	 * composite value. This method should be called in the constructor of
	 * classes that implement CompositeBase.
	 *
	 * @param children persistent child parameters.
	 */
	protected void addChildListeners(List<PersistentParameter> children) {
		for (PersistentParameter p : children) {
			p.property().addListener(invalidationListener);
		}
	}

	/**
	 * Removes invalidation listeners from persistent child parameters.
	 *
	 * @param children persistent child parameters.
	 */
	protected void removeChildListeners(List<PersistentParameter> children) {
		for (PersistentParameter p : children) {
			p.property().removeListener(invalidationListener);
		}
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

	// For composite T it might be wise to override:
	// # public void set(T value)
	// ...and do some validation before setting a composite directly. Children
	// might be outdated or missing (e.g. loading an old savefile) so it's
	// probably best to set children individually.
}
