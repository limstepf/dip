package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.datatypes.DataType;
import static ch.unifr.diva.dip.gui.pe.PipelineEditor.hashColor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * Connection view (or wire) base class.
 *
 * @param <T> type of the wire shape.
 */
abstract public class ConnectionViewBase<T extends Shape> implements ConnectionView {

	protected final InputPort<?> input;
	protected PortView<OutputPort<?>> output;
	protected final Color color;
	protected final T wire;

	/**
	 * Creates a new connection view (or wire).
	 *
	 * @param input input port of the connection.
	 * @param wire a JavaFX Shape that represents the connection view (or is the
	 * wire).
	 */
	public ConnectionViewBase(InputPort<?> input, T wire) {
		this.input = input;
		this.color = hashColor(input);
		this.wire = wire;

		this.wire.setFill(Color.TRANSPARENT);
		this.wire.setStroke(color);
	}

	@Override
	public DataType<?> dataType() {
		return input.getDataType();
	}

	@Override
	public Node node() {
		return wire;
	}

	@Override
	public boolean isConnected() {
		return input.isConnected();
	}

	@Override
	public void setLoose(boolean loose) {
		wire.setOpacity(loose ? .67 : 1.0);
	}

	@Override
	public void setOutput(PortView<OutputPort<?>> output) {
		this.output = output;
	}

	@Override
	public InputPort<?> inputPort() {
		return input;
	}

	@Override
	public OutputPort<?> outputPort() {
		if (output == null) {
			return null;
		}
		return output.port;
	}

}
