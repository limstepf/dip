package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.ValueList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Composite grid backed by a list of child parameters. Keep in mind that a list
 * is harder to maintain (while keeping backwards compatibility) in the long run
 * (if possible at all) than a map. So if this is composite parameter shall be
 * populated by persistent parameters that might possibly change or get extended
 * in the future, consider using a {@code CompositeGridMap} instead.
 *
 * @see CompositeGridMap
 */
public class CompositeGrid extends CompositeGridBase<ValueList> {

	protected final List<Parameter> children;
	protected final List<PersistentParameter> persistentChildren;

	/**
	 * Creates a composite grid (without label).
	 *
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGrid(Parameter... parameters) {
		this("", parameters);
	}

	/**
	 * Creates a composite grid.
	 *
	 * @param label label.
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGrid(String label, Parameter... parameters) {
		this(label, Arrays.asList(parameters));
	}

	/**
	 * Creates a composite grid (without label).
	 *
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGrid(Collection<Parameter> parameters) {
		this("", parameters);
	}

	/**
	 * Creates a composite grid.
	 *
	 * @param label label.
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGrid(String label, Collection<Parameter> parameters) {
		super(label, initValue(parameters), initValue(parameters));

		this.children = new ArrayList<>(parameters);
		this.persistentChildren = Parameter.filterPersistent(this.children);

		addChildListeners(this.persistentChildren);
	}

	// yes, we do this twice to not override the default later on...
	private static ValueList initValue(Collection<Parameter> parameters) {
		final List<Object> defaultValues = new ArrayList<>();
		for (Parameter p : parameters) {
			if (p.isPersistent()) {
				final PersistentParameter pp = (PersistentParameter) p;
				defaultValues.add(pp.defaultValue());
			}
		}
		return new ValueList(defaultValues);
	}

	@Override
	protected Collection<? extends Parameter> getChildren() {
		return this.children;
	}

	@Override
	protected boolean hasPersistentChildren() {
		return !this.persistentChildren.isEmpty();
	}

	@Override
	protected void updateValue(int index) {
		final ValueList v = get();
		v.list.set(index, this.persistentChildren.get(index).get());
		this.setLocal(v);
	}

	@Override
	protected void updateChildValues(ValueList value) {
		final int n = value.list.size();
		if (n == 0) {
			return;
		}

		final int m = Math.min(n, this.persistentChildren.size());
		for (int i = 0; i < m; i++) {
			this.persistentChildren.get(i).set(value.get(i));
		}
	}

	@Override
	protected void invalidateChildParameter(PersistentParameter p) {
		final ValueList vl = get();
		for (int i = 0; i < this.persistentChildren.size(); i++) {
			final PersistentParameter pi = this.persistentChildren.get(i);
			if (p.equals(pi)) {
				vl.list.set(i, p.get());
				break;
			}
		}
		this.valueProperty.invalidate();
	}

	@Override
	protected ValueList filterValueProperty(ValueList value) {
		enableChildListeners(false);

		final ValueList v = get();
		// read at most as many values as we have persistent children
		final int max = Math.min(
				this.persistentChildren.size(),
				value.list.size()
		);
		for (int i = 0; i < max; i++) {
			v.list.set(i, value.get(i));
			this.persistentChildren.get(i).set(value.get(i));
		}

		return v;
	}

}
