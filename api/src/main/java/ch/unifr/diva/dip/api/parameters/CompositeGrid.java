package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.ValueList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Composite grid backed by a list of child parameters. Keep in mind that a list
 * is harder to maintain (while keeping backwards compatibility) in the long run
 * (if possible at all) than a map. So if this composite parameter shall be
 * populated by persistent parameters that might possibly change or get extended
 * in the future, consider using a {@code CompositeGridMap} instead.
 *
 * @see CompositeGridMap
 */
public class CompositeGrid extends CompositeGridBase<ValueList> {

	protected final List<Parameter<?>> children;
	protected final List<PersistentParameter<?>> persistentChildren;

	/**
	 * Creates a composite grid (without label).
	 *
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGrid(Parameter<?>... parameters) {
		this("", parameters);
	}

	/**
	 * Creates a composite grid.
	 *
	 * @param label label.
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGrid(String label, Parameter<?>... parameters) {
		this(label, Arrays.asList(parameters));
	}

	/**
	 * Creates a composite grid (without label).
	 *
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGrid(Collection<Parameter<?>> parameters) {
		this("", parameters);
	}

	/**
	 * Creates a composite grid.
	 *
	 * @param label label.
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGrid(String label, Collection<Parameter<?>> parameters) {
		super(label, ValueList.class, initValue(parameters), initValue(parameters));

		this.children = new ArrayList<>(parameters);
		this.persistentChildren = Parameter.filterPersistent(this.children);

		addChildListeners(this.persistentChildren);
	}

	// yes, we do this twice to not override the default later on...
	private static ValueList initValue(Collection<Parameter<?>> parameters) {
		final List<Object> defaultValues = new ArrayList<>();
		for (Parameter<?> p : parameters) {
			if (p.isPersistent()) {
				defaultValues.add(p.asPersitentParameter().defaultValue());
			}
		}
		return new ValueList(defaultValues);
	}

	@Override
	protected Collection<? extends Parameter<?>> getChildren() {
		return this.children;
	}

	@Override
	protected boolean hasPersistentChildren() {
		return !this.persistentChildren.isEmpty();
	}

	@Override
	protected void updateValue(int index) {
		final ValueList v = get();
		v.set(index, this.persistentChildren.get(index).get());
		setLocal(v);
	}

	@Override
	protected void invalidateChildParameter(PersistentParameter<?> p) {
		final ValueList vl = get();
		for (int i = 0; i < this.persistentChildren.size(); i++) {
			final PersistentParameter<?> pi = this.persistentChildren.get(i);
			if (p.equals(pi)) {
				vl.set(i, p.get());
				break;
			}
		}
		this.valueProperty.invalidate();
	}

	public boolean isMatchingValueList(ValueList value) {
		if (value == null || value.size() != persistentChildren.size()) {
			return false;
		}
		for (int i = 0; i < persistentChildren.size(); i++) {
			final PersistentParameter<?> p = persistentChildren.get(i);
			if (!p.isAssignable(value.get(i))) {
				return false;
			}
		}
		return true;
	}

	protected String persistentChildrenToString() {
		final List<String> params = new ArrayList<>();
		for (PersistentParameter<?> p : persistentChildren) {
			params.add(p.getValueClass().getSimpleName());
		}
		return String.join(", ", params);
	}

	protected String valuesToString(ValueList value) {
		if (value == null) {
			return "null";
		}
		final List<String> values = new ArrayList<>();
		for (Object obj : value.elements) {
			values.add(obj.getClass().getSimpleName());
		}
		return String.join(", ", values);
	}

	@Override
	protected ValueList filterValueProperty(ValueList value) {
		if (!isMatchingValueList(value)) {
			log.warn(
					"ValueList mismatch in filterValueProperty: {}"
					+ ",\n expected: [{}]"
					+ ",\n given:    [{}]",
					this,
					persistentChildrenToString(),
					valuesToString(value)
			);
			return get(); // ignore given ValueList alltogether
		}

		return value;
	}

	@Override
	protected void onValuePropertySet(boolean changed) {
		if (changed) { // propagate changed values to child-parameters
			enableChildListeners(false);

			final ValueList value = get();
			for (int i = 0; i < persistentChildren.size(); i++) {
				persistentChildren.get(i).setRaw(value.get(i));
			}

			enableChildListeners(true);
		}
	}

	/**
	 * A grid view. Extended/overwritten for the sake of testability.
	 */
	public static class GridListView extends CompositeGridBase.GridView<CompositeGrid, ValueList> {

		/**
		 * Creates a new grid view.
		 *
		 * @param parameter the parameter.
		 */
		public GridListView(CompositeGrid parameter) {
			super(parameter);
		}

		@Override
		public ValueList get() {
			final ValueList list = new ValueList();
			PersistentParameter.View<?> view;
			for (PersistentParameter<?> p : parameter.persistentChildren) {
				view = (PersistentParameter.View<?>) p.view();
				list.add(view.get());
			}
			return list;
		}

	}

	@Override
	protected GridListView newViewInstance() {
		return new GridListView(this);
	}

}
