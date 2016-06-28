package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;

/**
 * Parameter interface (transient). This isn't so much a Parameter, but rather a
 * display (the idea here is to be able to easily mix custom UI controls with
 * actual parameters to create nicer/more elaborate parameter views). No value
 * will be saved or restored. Still, the view is expected to be lazily
 * initialized, s.t. no UI controls are initialized if not needed. A ViewHook
 * can be used to customize UI controls (once initialized).
 *
 * @param <T> type of the parameter.
 */
public interface Parameter<T> {

	/**
	 * Returns the view (or graphical representation) of a parameter.
	 *
	 * @param <U> subclass of the parameter view
	 * @return the view.
	 */
	public <U extends View<T>> U view();

	/**
	 * A view of a transient parameter.
	 *
	 * @param <T>
	 */
	public interface View<T> {

		/**
		 * Returns the root node of the view.
		 *
		 * @return the root node of the view.
		 */
		public Node node();
	}

	/**
	 * Checks whether this parameter is persitent. Persistent parameters - as
	 * opposed to transient ones - get saved and restored.
	 *
	 * <p>
	 * By default this simply checks if the parameter is an instance of
	 * {@code PersistentParameter}. Yet parent-, or composite parameters might
	 * override this method s.t. its child parameters are considered as well
	 * (e.g. a composite parameter, that on its own is a persitent parameter,
	 * might still return false here in case all of it's child parameters are
	 * transient).
	 *
	 * @return True if this parameter is persistent, False otherwise.
	 */
	default boolean isPersistent() {
		return (this instanceof PersistentParameter);
	}

	/**
	 * Filters a parameter map, only keeping persistent parameters.
	 *
	 * @param parameters map of parameters.
	 * @return map of non-transient parameters.
	 */
	public static Map<String, PersistentParameter> filterPersistent(Map<String, Parameter> parameters) {
		final Map<String, PersistentParameter> filtered = new HashMap<>();
		for (Map.Entry<String, Parameter> p : parameters.entrySet()) {
			if (p.getValue().isPersistent()) {
				filtered.put(p.getKey(), (PersistentParameter) p.getValue());
			}
		}

		return filtered;
	}

	/**
	 * Filters a parameter list, only keeping persistent parameters.
	 *
	 * @param parameters list of parameters.
	 * @return list of non-transient parameters.
	 */
	public static List<PersistentParameter> filterPersistent(List<Parameter> parameters) {
		final List<PersistentParameter> filtered = new ArrayList<>();
		for (Parameter p : parameters) {
			if (p.isPersistent()) {
				filtered.add((PersistentParameter) p);
			}
		}
		return filtered;
	}
}
