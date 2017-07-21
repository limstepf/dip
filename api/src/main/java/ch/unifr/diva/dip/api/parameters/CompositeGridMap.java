package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.ValueMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Composite grid backed by a map of child parameters. Use a
 * {@code LinkedHashMap} if you care about the order of child-parameters in the
 * grid (you probably do).
 *
 * @see CompositeGrid
 */
public class CompositeGridMap extends CompositeGridBase<ValueMap> {

	protected final List<Parameter<?>> children;
	protected final List<PersistentParameter<?>> persistentChildren;
	protected final List<String> persistentKeys;

	/**
	 * Creates a composite grid map (without label).
	 *
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGridMap(Map<String, Parameter<?>> parameters) {
		this("", parameters);
	}

	/**
	 * Creates a composite grid map.
	 *
	 * @param label label.
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGridMap(String label, Map<String, Parameter<?>> parameters) {
		super(label, ValueMap.class, initValue(parameters), initValue(parameters));

		this.children = new ArrayList<>(parameters.values());
		this.persistentChildren = new ArrayList<>();
		this.persistentKeys = new ArrayList<>();

		for (Map.Entry<String, Parameter<?>> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				this.persistentKeys.add(p.getKey());
				this.persistentChildren.add((PersistentParameter) p.getValue());
			}
		}

		addChildListeners(this.persistentChildren);
	}

	// yes, we do this twice to not override the default later on...
	protected static ValueMap initValue(Map<String, Parameter<?>> parameters) {
		final Map<String, Object> defaultValues = new HashMap<>();
		for (Map.Entry<String, Parameter<?>> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				final PersistentParameter<?> pp = (PersistentParameter<?>) p.getValue();
				defaultValues.put(p.getKey(), pp.defaultValue());
			}
		}
		return new ValueMap(defaultValues);
	}

	// since we're storing the map in two lists (for reasons...)
	protected int getIndexFromKey(String key) {
		return persistentKeys.indexOf(key);
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
		final String key = this.persistentKeys.get(index);
		final ValueMap v = get();
		v.map.put(key, this.persistentChildren.get(index).get());
		setLocal(v);
	}

	@Override
	protected void invalidateChildParameter(PersistentParameter<?> p) {
		final ValueMap vm = get();
		for (int i = 0; i < this.persistentChildren.size(); i++) {
			final PersistentParameter<?> pi = this.persistentChildren.get(i);
			if (p.equals(pi)) {
				final String key = this.persistentKeys.get(i);
				vm.put(key, p.get());
				break;
			}
		}
		this.valueProperty.invalidate();
	}

	@Override
	protected ValueMap filterValueProperty(ValueMap value) {
		// we need a shollow copy here, or set() will reject the value for
		// being the same as the current one...
		final ValueMap filtered = get().shallowCopy();
		PersistentParameter<?> p;
		int idx;
		for (Map.Entry<String, Object> e : value.map.entrySet()) {
			idx = getIndexFromKey(e.getKey());
			if (idx < 0) {
				log.warn(
						"ValueMap mismatch in filterValueProperty: {}"
						+ ",\n invalid key: {}",
						this,
						e.getKey()
				);
				continue; // ignore value
			}
			p = persistentChildren.get(idx);
			if (!p.isAssignable(e.getValue())) {
				log.warn(
						"ValueMap mismatch in filterValueProperty: {}"
						+ ",\n key: {}"
						+ ",\n unassignable value: {}"
						+ ",\n expected: {}",
						this,
						e.getKey(),
						e.getValue().getClass().getSimpleName(),
						p.getValueClass().getSimpleName()
				);
				continue; // ignore value
			}
			filtered.put(e.getKey(), e.getValue());
		}

		return filtered;
	}

	@Override
	protected void onValuePropertySet(boolean changed) {
		if (changed) { // propagate changed values to child-parameters
			enableChildListeners(false);

			final ValueMap value = get();
			int idx;
			for (Map.Entry<String, Object> e : value.map.entrySet()) {
				idx = getIndexFromKey(e.getKey());
				persistentChildren.get(idx).setRaw(e.getValue());
			}

			enableChildListeners(true);
		}
	}

	/**
	 * A grid view. Extended/overwritten for the sake of testability.
	 */
	public static class GridMapView extends CompositeGridBase.GridView<CompositeGridMap, ValueMap> {

		/**
		 * Creates a new grid view.
		 *
		 * @param parameter the parameter.
		 */
		public GridMapView(CompositeGridMap parameter) {
			super(parameter);
		}

		@Override
		public ValueMap get() {
			final ValueMap map = new ValueMap();
			PersistentParameter<?> p;
			String key;
			PersistentParameter.View<?> view;
			for (int i = 0; i < parameter.persistentKeys.size(); i++) {
				p = parameter.persistentChildren.get(i);
				key = parameter.persistentKeys.get(i);
				view = (PersistentParameter.View<?>) p.view();
				map.put(key, view.get());
			}
			return map;
		}

	}

	@Override
	protected GridMapView newViewInstance() {
		return new GridMapView(this);
	}

}
