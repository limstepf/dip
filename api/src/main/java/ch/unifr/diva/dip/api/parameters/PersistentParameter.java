package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.utils.ParentObjectProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.css.PseudoClass;

/**
 * Parameter interface (persistent). Persistent parameters have a value that
 * gets saved and restored. The view is expected to be lazily initialzed, s.t.
 * no UI controls are initialized if not needed. A ViewHook can be used to
 * customize UI controls (once initialized).
 *
 * @param <T> class of the parameter's value.
 */
public interface PersistentParameter<T> extends Parameter<T> {

	static final PseudoClass ALERT = PseudoClass.getPseudoClass("alert");

	/**
	 * Returns the label (or title) of the parameter.
	 *
	 * @return the label.
	 */
	public String label();

	/**
	 * Returns the default value of the parameter.
	 *
	 * @return the default value.
	 */
	public T defaultValue();

	/**
	 * Returns the value of the parameter.
	 *
	 * @return the value of the parameter.
	 */
	public T get();

	/**
	 * Sets the value of the parameter. Note that a call to this method might
	 * not have the expected side-effect of updating the view in case the value
	 * is equal to the current value of the parameter. This might happen with
	 * complex objects/values that were retrieved with {@code get()}, modified,
	 * followed by a call to this method. In such cases
	 *
	 * @param value the new value.
	 */
	public void set(T value);

	/**
	 * Invalidates the parameter's value property. This updates the view, and
	 * fires the {@code onValuePropertySet()} hook method. Should be called in
	 * case calling {@code set()} would fail to do so.
	 */
	public void invalidate();

	/**
	 * Sets the (raw/untyped) value of the parameter.
	 *
	 * @param value the raw/untyped value.
	 */
	@SuppressWarnings("unchecked")
	default void setRaw(Object value) {
		set((T) value);
	}

	/**
	 * Returns the class of the parameter's value.
	 *
	 * @return the class of the parameter's value.
	 */
	public Class<T> getValueClass();

	/**
	 * Checks whether the given value can be assigned to this parameter.
	 *
	 * @param value some (untyped) value.
	 * @return {@code true} if this value can be set on this parameter,
	 * {@code false} otherwise.
	 */
	default boolean isAssignable(Object value) {
		if (value == null) {
			return false;
		}
		return getValueClass().equals(value.getClass());
	}

	/**
	 * Returns the value property of the persistent parameter.
	 *
	 * @return the value property of the persistent parameter.
	 */
	public ParentObjectProperty<T> property();

	@Override
	public View<T> view();

	/**
	 * A view of a persistent parameter.
	 *
	 * <p>
	 * Note that the view must update the value of the parameter by a call to
	 * {@code parameter.setLocal(T v)} instead of using the
	 * {@code parameter.set(T v)} method, or the {@code valueProperty} directly
	 * (or face a stack overflow since the parameter will in turn update the
	 * view, which in turn will update the parameter, ...).
	 *
	 * @param <T> class of the parameter's value.
	 */
	public interface View<T> extends Parameter.View {

		/**
		 * Updates the parameter and its view with a new value.
		 *
		 * @param value the new value.
		 */
		public void set(T value);

		/**
		 * Returns the current value of the view. This method exists mostly for
		 * testing purposes (if implemented at all!). Usually you're going to
		 * retrieve the value on the parameter itself.
		 *
		 * @return the current value of the view.
		 * @throws UnsupportedOperationException
		 */
		public T get();

		/**
		 * Returns the underlying parameter.
		 *
		 * @return the parameter.
		 */
		public PersistentParameter<T> parameter();

	}

	/**
	 * Returns the default preset of a processor, that is a map of default
	 * values for a given set of published parameters.
	 *
	 * @param parameters the published parameters.
	 * @return a map of default values.
	 */
	public static Map<String, Object> getDefaultPreset(Map<String, Parameter<?>> parameters) {
		final Map<String, Object> preset = new HashMap<>();
		for (Map.Entry<String, Parameter<?>> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				final PersistentParameter<?> pp = (PersistentParameter<?>) p.getValue();
				preset.put(p.getKey(), pp.defaultValue());
			}
		}

		return preset;
	}

	/**
	 * Returns the current preset of a processor.
	 *
	 * @param parameters the processor's parameters.
	 * @return a map of the current values.
	 */
	public static Map<String, Object> getPreset(Map<String, Parameter<?>> parameters) {
		final Map<String, Object> preset = new HashMap<>();
		for (Map.Entry<String, Parameter<?>> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				final PersistentParameter<?> pp = (PersistentParameter<?>) p.getValue();
				preset.put(p.getKey(), pp.get());
			}
		}

		return preset;
	}

	/**
	 * Validates presets against the set of published parameters (by the
	 * processor). Keys in the preset that also (or still) exist in the set of
	 * parameters are copied. Published keys not defined in the preset will be
	 * introduced with the default value.
	 *
	 * @param parameters the published parameters.
	 * @param preset the preset/a map of parameters.
	 * @return the preset/a map of parameters with all published keys.
	 */
	public static Map<String, Object> validatePreset(Map<String, Parameter<?>> parameters, Map<String, Object> preset) {
		final Map<String, Object> validated = new HashMap<>();

		for (Map.Entry<String, Parameter<?>> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				final String key = p.getKey();
				final PersistentParameter<?> pp = (PersistentParameter<?>) p.getValue();
				if (preset != null && preset.containsKey(key)) {
					validated.put(key, preset.get(key));
				} else {
					validated.put(key, pp.defaultValue());
				}
			}
		}

		return validated;
	}

	/**
	 * A view hook is used to customize the view of a parameter. To idea here is
	 * to not initialize a view of a parameter if we don't have to, meaning we
	 * should not request fx nodes directly (e.g. a slider or a combobox) since
	 * this initializes the view. So instead we offer to register a view hook
	 * that get's passed the ui item to be customized and get's called once the
	 * view is actually required.
	 *
	 * In other words: parameters shouldn't ever expose ui elements directly
	 * (e.g. {@code parameter.getComboBox().setThings(); }), except for the view
	 * itself of course. Implement/use a view hook instead (e.g.
	 * {@code parameter.addViewHook(c} -> { c.setThings(); }).
	 *
	 * @param <T> class of the node.
	 */
	public static interface ViewHook<T> {

		public void apply(T node);
	}

	/**
	 * Applies a view hook to the given fx node.
	 *
	 * @param <T> class of the node.
	 * @param <S> class of the view hook.
	 * @param node the node.
	 * @param hook the view hook to be applied to the node.
	 */
	public static <T, S extends ViewHook<T>> void applyViewHook(T node, S hook) {
		hook.apply(node);
	}

	/**
	 * Applies a list of view hooks to the given fx node.
	 *
	 * @param <T> class of the node.
	 * @param <S> class of the view hook.
	 * @param node the node.
	 * @param hooks the view hooks to be applied to the node.
	 */
	public static <T, S extends ViewHook<T>> void applyViewHooks(T node, List<S> hooks) {
		applyViewHooks(node, hooks, null);
	}

	/**
	 * Applies a list of view hooks to a list of fx nodes.
	 *
	 * @param <T> class of the node.
	 * @param <S> class of the view hook.
	 * @param nodes the nodes.
	 * @param hooks the view hooks to be applied to the node.
	 * @param lastHook a last view hook, or {@code null}.
	 */
	public static <T, S extends ViewHook<T>> void applyViewHooks(List<T> nodes, List<S> hooks, S lastHook) {
		for (T node : nodes) {
			applyViewHooks(node, hooks, lastHook);
		}
	}

	/**
	 * Applies a list of view hooks to the given fx node.
	 *
	 * @param <T> class of the node.
	 * @param <S> class of the view hook.
	 * @param node the node.
	 * @param hooks the view hooks to be applied to the node.
	 * @param lastHook a last view hook, or {@code null}.
	 */
	public static <T, S extends ViewHook<T>> void applyViewHooks(T node, List<S> hooks, S lastHook) {
		for (ViewHook<T> hook : hooks) {
			applyViewHook(node, hook);
		}
		if (lastHook != null) {
			applyViewHook(node, lastHook);
		}
	}

}
