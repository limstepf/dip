package ch.unifr.diva.dip.api.parameters;

import javafx.scene.Node;

/**
 * A transient facade to wrap any kind of JavaFX node. Wrapping JavaFX nodes as
 * transient parameters might appear silly at first, but comes in handy every
 * once in a while, mostly to use them in composite parameters (we'd like to
 * reuse) such as an XorParameter.
 *
 * @param <T> class extending JavaFX node.
 */
public class TransientNodeParameter<T extends Node> extends TransientParameterBase {

	protected final T node;
	protected final View nodeView;

	/**
	 * Creates a new transient node parameter.
	 *
	 * @param node a JavaFX node.
	 */
	public TransientNodeParameter(T node) {
		this.node = node;
		this.nodeView = (View) () -> node;
	}

	/**
	 * Returns the wrapped JavaFX node.
	 *
	 * @return the JavaFX node.
	 */
	public T getNode() {
		return this.node;
	}

	@Override
	protected View newViewInstance() {
		return this.nodeView;
	}

}
