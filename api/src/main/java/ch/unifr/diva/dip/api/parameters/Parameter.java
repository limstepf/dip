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
 * @param <T> class of the parameter's value.
 */
public interface Parameter<T> {

	/**
	 * Returns the view (or graphical representation) of a parameter.
	 *
	 * @return the view.
	 */
	public View view();

	/**
	 * Un-/hides the parameter (and it's view).
	 *
	 * @param hide {@code true} to hide the parameter, {@code false} to
	 * unhide/show the parameter.
	 */
	public void setHide(boolean hide);

	/**
	 * A view of a transient parameter.
	 */
	public interface View {

		/**
		 * Returns the root node of the view.
		 *
		 * @return the root node of the view.
		 */
		public Node node();

		/**
		 * Un-/hides the parameter's view. Marks the parameter as invisible and
		 * also as unmanaged by its parent, thereby removing it from the
		 * flow/size calculations.
		 *
		 * @param hide {@code true} to hide the view, {@code false} to
		 * unhide/show the view.
		 */
		default void setHide(boolean hide) {
			node().setVisible(!hide);
			node().setManaged(!hide);
		}
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
	 * might still return {@code false} here in case all of it's child
	 * parameters are transient).
	 *
	 * @return {@code true} if this parameter is persistent, {@code false}
	 * otherwise.
	 */
	default boolean isPersistent() {
		return (this instanceof PersistentParameter);
	}

	/**
	 * Returns the parameter as a persistent parameter. Make sure this is
	 * actually a persistent parameter with {@code isPersistent()}.
	 *
	 * @return the parameter as a persistent parameter.
	 */
	default PersistentParameter<T> asPersitentParameter() {
		return (PersistentParameter<T>) this;
	}

	/**
	 * Filters a parameter map, only keeping persistent parameters.
	 *
	 * @param parameters map of parameters.
	 * @return map of non-transient parameters.
	 */
	public static Map<String, PersistentParameter<?>> filterPersistent(Map<String, Parameter<?>> parameters) {
		final Map<String, PersistentParameter<?>> filtered = new HashMap<>();
		for (Map.Entry<String, Parameter<?>> p : parameters.entrySet()) {
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
	public static List<PersistentParameter<?>> filterPersistent(List<Parameter<?>> parameters) {
		final List<PersistentParameter<?>> filtered = new ArrayList<>();
		for (Parameter<?> p : parameters) {
			if (p.isPersistent()) {
				filtered.add((PersistentParameter) p);
			}
		}
		return filtered;
	}
}
