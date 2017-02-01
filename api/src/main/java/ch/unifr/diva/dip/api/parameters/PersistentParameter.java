package ch.unifr.diva.dip.api.parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.css.PseudoClass;

/**
 * Parameter interface (persistent). Persistent parameters have a value that
 * gets saved and restored. The view is expected to be lazily initialzed, s.t.
 * no UI controls are initialized if not needed. A ViewHook can be used to
 * customize UI controls (once initialized).
 *
 * @param <T> type of the parameter.
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
	 * Sets the value of the parameter.
	 *
	 * @param value the new value.
	 */
	public void set(T value);

	/**
	 * Returns a read-only property to listen for (manual) changes.
	 *
	 * @return a read-only property.
	 */
	public ReadOnlyObjectProperty<T> property();

	/**
	 * A view of a persistent parameter.
	 *
	 * @param <T>
	 */
	public interface View<T> extends Parameter.View<T> {

		/**
		 * Updates the parameter and its view with a new value.
		 *
		 * @param value the new value.
		 */
		public void set(T value);

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
	public static Map<String, Object> getDefaultPreset(Map<String, Parameter> parameters) {
		final Map<String, Object> preset = new HashMap<>();
		for (Map.Entry<String, Parameter> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				final PersistentParameter pp = (PersistentParameter) p.getValue();
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
	public static Map<String, Object> getPreset(Map<String, Parameter> parameters) {
		final Map<String, Object> preset = new HashMap<>();
		for (Map.Entry<String, Parameter> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				final PersistentParameter pp = (PersistentParameter) p.getValue();
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
	public static Map<String, Object> validatePreset(Map<String, Parameter> parameters, Map<String, Object> preset) {
		final Map<String, Object> validated = new HashMap<>();

		for (Map.Entry<String, Parameter> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				final String key = p.getKey();
				final PersistentParameter pp = (PersistentParameter) p.getValue();
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
	 * @param <T>
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
	 * @param lastHook a last view hook, or null.
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
	 * @param lastHook a last view hook, or null.
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
