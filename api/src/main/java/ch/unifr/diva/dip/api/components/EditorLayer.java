package ch.unifr.diva.dip.api.components;

import java.util.concurrent.Callable;

/**
 * Grants safe access to Layers. Layers follow the composite pattern where a
 * layer is either a group of layers (composite), or a pane (leaf) that can be
 * populated by arbitrary JavaFX nodes. Layers must be accessed/modified on the
 * JavaFX application thread.
 *
 * Access is considered fool-proof (i.e. safe), since all methods are executed
 * on the JavaFX application thread. Still, it might be a good idea to wrap
 * loops using these methods in a Callable to be explicitly {@code run()} on the
 * JavaFX application thread, or otherwise you'll end up with a gazillion calls
 * to implicit {@code run()}'s, which can be rather slow.
 */
public interface EditorLayer {

	/**
	 * Sets the name of the layer.
	 *
	 * @param name the name of the layer.
	 */
	public void setName(String name);

	/**
	 * Returns the name of the layer.
	 *
	 * @return the name of the layer.
	 */
	public String getName();

	/**
	 * Checks whether the layer is visible.
	 *
	 * @return {@code true} if the layer is visible, {@code false} otherwise.
	 */
	public boolean isVisible();

	/**
	 * Sets the visibility of the layer.
	 *
	 * @param visible {@code true} to make the layer visible, {@code false} to make it
	 * invisible.
	 */
	public void setVisible(boolean visible);

	/**
	 * Repaints the layer. This method should be called after tools have modified
	 * the content of a layer pane (e.g. after drawing a stroke on a canvas), to
	 * get the views (navigator widget, NN overlay) to update themselves. This
	 * also marks the project as dirty (s.t. the project needs to be saved).
	 */
	public void repaint();

	/**
	 * Reverses the order of children. Layers/TreeItems in the TreeView grow
	 * bottom-up, so either loops populating a parent need to be written
	 * backwards, or this helper method can be called afterwards to get the
	 * expected order of children.
	 */
	public void reverseChildren();

	/**
	 * Removes the layer from its parent layer group. This is a no-op in case
	 * the layer has no parent.
	 */
	public void remove();

	/**
	 * Runs custom code on the JavaFX application thread.
	 *
	 * @param runnable some custom code that needs to run on the JavaFX
	 * application thread.
	 */
	public void run(Runnable runnable);

	/**
	 * Executes a callable on the JavaFX application thread and returns the
	 * result. Make sure this is a quick one, or the JavaFX application thread
	 * might get clogged up.
	 *
	 * @param <T> type of the returned value.
	 * @param callable custom code that returns T that gets executed on the
	 * JavaFX application thread as a FutureTask.
	 * @return result of the callable.
	 */
	public <T> T runFutureTask(Callable<T> callable);
}
