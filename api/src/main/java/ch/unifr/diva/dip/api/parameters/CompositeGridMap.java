package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.ValueMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.InvalidationListener;

/**
 * Composite grid backed by a map of child parameters. Use a
 * {@code LinkedHashMap} if you care about the order of child-parameters in the
 * grid (you probably do).
 *
 * @see CompositeGrid
 */
public class CompositeGridMap extends CompositeGridBase<ValueMap> {

	protected final List<Parameter> children;
	protected final List<PersistentParameter> persistentChildren;
	protected final List<String> persistentKeys;

	/**
	 * Creates a composite grid map (without label).
	 *
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGridMap(Map<String, Parameter> parameters) {
		this("", parameters);
	}

	/**
	 * Creates a composite grid map.
	 *
	 * @param label label.
	 * @param parameters child parameters to populate the grid with.
	 */
	public CompositeGridMap(String label, Map<String, Parameter> parameters) {
		super(label, initValue(parameters), initValue(parameters));

		this.children = new ArrayList<>(parameters.values());
		this.persistentChildren = new ArrayList<>();
		this.persistentKeys = new ArrayList<>();

		for (Map.Entry<String, Parameter> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				this.persistentKeys.add(p.getKey());
				this.persistentChildren.add((PersistentParameter) p.getValue());
			}
		}

		addChildListeners(this.persistentChildren);
	}

	// yes, we do this twice to not override the default later on...
	protected static ValueMap initValue(Map<String, Parameter> parameters) {
		final Map<String, Object> defaultValues = new HashMap<>();
		for (Map.Entry<String, Parameter> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				final PersistentParameter pp = (PersistentParameter) p.getValue();
				defaultValues.put(p.getKey(), pp.defaultValue());
			}
		}
		return new ValueMap(defaultValues);
	}

	// since we're storing the map in two lists (for reasons...)
	protected int getIndexFromKey(String key) {
		final int n = this.persistentKeys.size();
		for (int i = 0; i < n; i++) {
			if (this.persistentKeys.get(i).equals(key)) {
				return i;
			}
		}
		return -1;
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
		final String key = this.persistentKeys.get(index);
		final ValueMap v = get();
		v.map.put(key, this.persistentChildren.get(index).get());
		this.valueProperty.set(v);
	}

	@Override
	protected void updateChildValues(ValueMap value) {
		for (Map.Entry<String, Object> e : value.map.entrySet()) {
			final int index = getIndexFromKey(e.getKey());
			if (index >= 0) {
				this.persistentChildren.get(index).set(e.getValue());
			}
		}
	}

	@Override
	protected void invalidateChildParameter(PersistentParameter p) {
		final ValueMap vm = get();
		for (int i = 0; i < this.persistentChildren.size(); i++) {
			final PersistentParameter pi = this.persistentChildren.get(i);
			if (p.equals(pi)) {
				final String key = this.persistentKeys.get(i);
				vm.set(key, p.get());
				break;
			}
		}
		this.valueProperty.invalidate();
	}

	@Override
	public void set(ValueMap value) {
		enableChildListeners(false);

		// value can't be trusted (might have invalid/outdated, or worse: missing keys)
		// so we copy the entries individually
		final ValueMap v = get();
		for (Map.Entry<String, Object> e : value.map.entrySet()) {
			v.map.put(e.getKey(), e.getValue());
			final int index = getIndexFromKey(e.getKey());
			this.persistentChildren.get(index).set(e.getValue());
		}

		final boolean invalidate = this.valueProperty.get().equals(v);
		this.valueProperty.set(v);
		if (invalidate) {
			this.valueProperty.invalidate();
		}

		if (view != null) {
			view.set(v);
		}

		enableChildListeners(true);
	}

}
