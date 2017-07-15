package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Processor view with input ports at the top, and output ports at the bottom.
 * Suitable for a TOPDOWN pipeline layout strategy.
 */
public class ProcessorViewTopDown extends ProcessorView {

	protected final HBox inputPane = new HBox();
	protected final HBox outputPane = new HBox();
	protected final BorderPane infoPane = new BorderPane();

	/**
	 * Creates a new top-down processor view.
	 *
	 * @param editor the pipeline editor.
	 * @param wrapper the processor.
	 */
	public ProcessorViewTopDown(PipelineEditor editor, PrototypeProcessor wrapper) {
		super(editor, wrapper);

		this.setTop(inputPane);
		inputPane.setAlignment(Pos.CENTER);
		inputPane.getStyleClass().add("dip-processor-top");
		for (PortView<?> v : this.inputPorts) {
			inputPane.getChildren().add(v);
		}

		this.setCenter(infoPane);
		infoPane.getStyleClass().add("dip-processor-topdown");
		infoPane.setTop(head.getNode());
		setupDraggable(infoPane);

		this.setBottom(outputPane);
		outputPane.setAlignment(Pos.CENTER);
		outputPane.getStyleClass().add("dip-processor-bottom");
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
		return new GridParameterView(wrapper().processor());
	}

	@Override
	protected void showParameters(boolean show) {
		super.showParameters(show);

		final ClosedParameterView closedView = this.closedParameterView();
		if (show) {
			infoPane.setCenter(this.parameterView().node());
		} else {
			infoPane.setCenter(null);
			if (closedView != null) {
				infoPane.setCenter(closedView.node());
			}
		}
		repaint();
	}

}
