package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.datatypes.DataType;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;

/**
 * Interface for the graphical representation of a connection (or a wire).
 */
public interface ConnectionView {

	/**
	 * Enumeration of available connection view (or wire)
	 * implementations/factories.
	 */
	public enum Type {

		/**
		 * Wire represented by a line.
		 */
		LINEAR() {
					@Override
					public ConnectionView newConnection(InputPort<?> input) {
						return new ConnectionViewLinear(input);
					}
				},
		/**
		 * Wire represented by a quadratic curve.
		 */
		QUAD() {
					@Override
					public ConnectionView newConnection(InputPort<?> input) {
						return new ConnectionViewQuad(input);
					}
				};
		// TODO: CUBIC; return new ConnectionViewCubic(input);

		/**
		 * Safely returns a valid connection view (or wire) type.
		 *
		 * @param name name of the connection view (or wire) type.
		 * @return implementation/factory of a connection view (or wire).
		 * Returns a QUAD wire in case the one specified by name isn't
		 * available.
		 */
		public static Type get(String name) {
			try {
				return Type.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return getDefault();
			}
		}

		/**
		 * Return the default connection view (or wire) type.
		 *
		 * @return implementation/factory of the default QUAD connection view
		 * (or wire).
		 */
		public static Type getDefault() {
			return Type.QUAD;
		}

		/**
		 * Connection view (or wire) factory.
		 *
		 * @param input already connected input port, or origin of the wire.
		 * @return a new connection view.
		 */
		public abstract ConnectionView newConnection(InputPort<?> input);
	}

	/**
	 * Returns the data type of the connection.
	 *
	 * @return the data type of the connection.
	 */
	DataType<?> dataType();

	/**
	 * Returns the node of the connection.
	 *
	 * @return a JavaFX node.
	 */
	Node node();

	/**
	 * Checks whether the wire is fully connected to in- and output ports.
	 *
	 * @return {@code true} if fully connected, {@code false} otherwise.
	 */
	boolean isConnected();

	/**
	 * Marks the connection (or wire) as loose. A loose wire is currently not
	 * fully connected (e.g. {@code setLoose(true)} gets called upon dragging a
	 * wire since only a single port is connected. A call to
	 * {@code setLoose(false)} follows once a full connection is feasible again,
	 * that is, upon sitting on a compatible target port).
	 *
	 * <p>
	 * In other words: being loose or not only has meaning while a wire is being
	 * dragged. Then a loose wire (visually) indicates that stopping the drag
	 * action will not lead to a connection (i.e. the wire is removed), while if
	 * the wire is not set to loose, then the connection can be made by dropping
	 * the wire onto the compatible port.
	 *
	 * @param loose marks the connection (or wire) as loose if {@code true}.
	 */
	void setLoose(boolean loose);

	/**
	 * Returns the input port of the connection.
	 *
	 * @return the input port of the connection.
	 */
	public InputPort<?> inputPort();

	/**
	 * Returns the output port of the connection.
	 *
	 * @return the output port of the connection.
	 */
	public OutputPort<?> outputPort();

	/**
	 * Binds the connection view (or wire) to an input and an output port view.
	 *
	 * @param output the port view of an output port.
	 * @param input the port view of an input port.
	 */
	default void bind(PortView<OutputPort<?>> output, PortView<InputPort<?>> input) {
		startXProperty().bind(input.centerXProperty());
		startYProperty().bind(input.centerYProperty());
		endXProperty().bind(output.centerXProperty());
		endYProperty().bind(output.centerYProperty());
		output.updateCenter();
		input.updateCenter();
	}

	/**
	 * Sets/updates the output port view of the connection.
	 *
	 * @param output the port view of an output port.
	 */
	void setOutput(PortView<OutputPort<?>> output);

	/**
	 * Unbinds a connection view (or wire) from its ports.
	 */
	default void unbind() {
		startXProperty().unbind();
		startYProperty().unbind();
		endXProperty().unbind();
		endYProperty().unbind();
	}

	/**
	 * Sets/updates the position of the end of the wire.
	 *
	 * @param p point to set the end of the wire.
	 */
	void setEnd(Point2D p);

	/**
	 * Sets/updates the position of the start of the wire.
	 *
	 * @param p point to set the start of the wire.
	 */
	void setStart(Point2D p);

	/**
	 * X-position property of the start of the wire.
	 *
	 * @return x-position property of the start of the wire.
	 */
	DoubleProperty startXProperty();

	/**
	 * Y-position property of the start of the wire.
	 *
	 * @return y-position property of the start of the wire.
	 */
	DoubleProperty startYProperty();

	/**
	 * X-position property of the end of the wire.
	 *
	 * @return x-position property of the end of the wire.
	 */
	DoubleProperty endXProperty();

	/**
	 * Y-position property of the end of the wire.
	 *
	 * @return y-position property of the end of the wire.
	 */
	DoubleProperty endYProperty();

}
