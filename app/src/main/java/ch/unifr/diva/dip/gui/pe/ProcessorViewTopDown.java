
package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Processor view with input ports at the top, and output ports at the bottom.
 * Suitable for a TOPDOWN pipeline layout strategy.
 */
public class ProcessorViewTopDown extends ProcessorView {

	protected final HBox inputPane = new HBox();
	protected final HBox outputPane = new HBox();
	protected final VBox infoPane = new VBox();

	public ProcessorViewTopDown(PipelineEditor editor, ProcessorWrapper wrapper) {
		super(editor, wrapper);

		this.setTop(inputPane);
		inputPane.setAlignment(Pos.CENTER);
		inputPane.getStyleClass().add("dip-processor-top");
		for (PortView v : this.inputPorts) {
			inputPane.getChildren().add(v);
		}

		this.setCenter(infoPane);
		infoPane.getStyleClass().add("dip-processor-topdown");
		infoPane.getChildren().addAll(title);
		setupDraggable(infoPane);

		this.setBottom(outputPane);
		outputPane.setAlignment(Pos.CENTER);
		outputPane.getStyleClass().add("dip-processor-bottom");
		for (PortView v : this.outputPorts) {
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
		if (show) {
			infoPane.getChildren().add(this.parameterView().node());
		} else {
			infoPane.getChildren().remove(this.parameterView().node());
		}
	}

}
