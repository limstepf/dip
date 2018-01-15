package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Processor view with input ports to the left, and output ports to the right.
 * Suitable for a LEFTRIGHT pipeline layout strategy.
 */
public class ProcessorViewLeftRight extends ProcessorView {

	protected final VBox inputPane = new VBox();
	protected final VBox outputPane = new VBox();
	protected final BorderPane infoPane = new BorderPane();

	/**
	 * Creates a new left-right processor view.
	 *
	 * @param editor the pipeline editor.
	 * @param wrapper the processor.
	 */
	public ProcessorViewLeftRight(PipelineEditor editor, PrototypeProcessor wrapper) {
		super(editor, wrapper);

		this.setLeft(inputPane);
		inputPane.getStyleClass().add("dip-processor-left");
		for (PortView<?> v : this.inputPorts) {
			inputPane.getChildren().add(v);
		}

		this.setCenter(infoPane);
		infoPane.getStyleClass().add("dip-processor-leftright");
		infoPane.setTop(head.getNode());
		setupDraggable(infoPane);

		this.setRight(outputPane);
		outputPane.getStyleClass().add("dip-processor-right");
		for (PortView<?> v : this.outputPorts) {
			outputPane.getChildren().add(v);
		}

		setupPortViewListener();
		setupAdditionalPortViewListeners();
		setupEditingListeners();
	}

	private void setupAdditionalPortViewListeners() {
		// otherwise ports are wrong upon resizing ProcessorView (e.g. editing)
		infoPane.layoutBoundsProperty().addListener(portListener);
	}

	@Override
	protected ParameterView newParameterView() {
		return new GridParameterView(wrapper().serviceObject());
	}

	@Override
	protected void showParameters(boolean show) {
		super.showParameters(show);

		if (show) {
			infoPane.setCenter(this.parameterView().node());
		} else {
			infoPane.setCenter(null);
			final ParameterView closedView = this.closedParameterView();
			if (closedView != null) {
				infoPane.setCenter(closedView.node());
			}
		}
		repaint();
	}

}
