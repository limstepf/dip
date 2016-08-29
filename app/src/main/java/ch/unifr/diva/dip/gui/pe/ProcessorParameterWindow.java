package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.services.Previewable;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.layout.AbstractWindow;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * Processor parameter (and preview) window. Popup window to configure a
 * {@code RunnableProcessor} in a {@code RunnablePipeline}.
 *
 * <p>
 * While the pipeline editor is used to define project-wide/global pipelines,
 * this window allows to override the parameters of processors in such a
 * pipeline on a page-level to fine tune individual processing steps.
 */
public class ProcessorParameterWindow extends AbstractWindow implements Presenter {

	private final ApplicationHandler handler;
	private final RunnableProcessor runnable;
	private final ProcessorView.ParameterViewBase parameterView;

	/**
	 * Creates a new processor parameter (and preview) window.
	 *
	 * @param owner owner/parent window.
	 * @param handler the application handler.
	 * @param runnable the runnable processor.
	 */
	public ProcessorParameterWindow(Window owner, ApplicationHandler handler, RunnableProcessor runnable) {
		super(owner, runnable.processor().name());

		this.handler = handler;
		this.runnable = runnable;

		final double b = UIStrategyGUI.Stage.insets;
		this.root.setPadding(new Insets(b));
		this.parameterView = new ProcessorView.GridParameterView(runnable.processor());
		this.root.setCenter(this.parameterView.node());

		final VBox sideBox = new VBox();
		sideBox.setPadding(new Insets(b * 2, b * 2, b, b * 5));
		final Button ok = new Button(localize("ok"));
		ok.setOnAction((e) -> {
			this.close();
		});
		sideBox.getChildren().addAll(ok);
		this.root.setRight(sideBox);

		if (runnable instanceof Previewable) {
			final Region todo = new Region();
			todo.setPrefWidth(128);
			todo.setPrefHeight(128);
			todo.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
			this.root.setLeft(todo);
		} else {
			final Region spacer = new Region();
			spacer.setPrefWidth(b * 2);
			this.root.setLeft(spacer);
		}
	}

	@Override
	public Parent getComponent() {
		return null;
	}

}
