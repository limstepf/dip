package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.model.PrototypePipeline;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import ch.unifr.diva.dip.gui.layout.VerticalSplitPane;
import ch.unifr.diva.dip.gui.pe.DataItemListView;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * The change pipeline dialog. Allows to change the pipeline of multiple pages.
 */
public class ChangePipelineDialog extends AbstractDialog {

	private final Window owner;
	private final Button ok;
	private final Button cancel;
	private final ApplicationHandler handler;
	private final List<ProjectPage> pages;
	private final VBox vbox;
	private final VerticalSplitPane vspane;
	private final DataItemListView<PageItem> pageView;
	private final DataItemListView<PipelineItem> pipelineView;

	/**
	 * Creates a new change pipeline dialog.
	 *
	 * @param handler the application handler.
	 * @param pages the pages whose pipeline we'd like to change.
	 */
	public ChangePipelineDialog(ApplicationHandler handler, List<ProjectPage> pages) {
		this(handler.uiStrategy.getStage(), handler, pages);
	}

	/**
	 * Creates a new change pipeline dialog.
	 *
	 * @param owner the owner of the dialog.
	 * @param handler the application handler.
	 * @param pages the pages whose pipeline we'd like to change.
	 */
	public ChangePipelineDialog(Window owner, ApplicationHandler handler, List<ProjectPage> pages) {
		super(owner);
		setTitle(localize("page.change.pipeline.selected"));
		this.owner = owner;
		this.handler = handler;
		this.pages = pages;

		this.pageView = DataItemListView.newEditableDataItemListView();
		pageView.setEditable(false);
		pageView.setSelectionMode(SelectionMode.MULTIPLE);
		for (ProjectPage page : pages) {
			pageView.getItems().add(new PageItem(page));
		}

		this.pipelineView = DataItemListView.newSelectableDataItemListView((c) -> {
			onAction();
			return null;
		});
		pipelineView.setEditable(false);
		pipelineView.setSelectionMode(SelectionMode.SINGLE);
		pipelineView.hasOneSelectedProperty().addListener((c) -> updateState());
		pipelineView.getItems().add(new PipelineItem());
		for (PrototypePipeline pipeline : handler.getProject().pipelineManager().pipelines()) {
			pipelineView.getItems().add(new PipelineItem(pipeline));
		}

		this.vspane = new VerticalSplitPane();
		this.vspane.getLeftChildren().setAll(
				new Label(localize("page.any") + ":"),
				this.pageView.getNode()
		);
		this.vspane.getRightChildren().setAll(
				new Label(localize("pipelines") + ":"),
				this.pipelineView.getNode()
		);

		final Label msg = new Label(localize("page.change.pipelines.msg"));
		msg.setWrapText(true);
		msg.setPadding(new Insets(0, 0, 10, 0));

		this.vbox = new VBox();
		vbox.getChildren().addAll(
				msg,
				vspane.getNode()
		);
		this.root.setCenter(vbox);

		this.ok = getDefaultButton(localize("page.change.pipelines"));
		this.cancel = getCancelButton(stage);
		ok.setDisable(true);
		ok.setOnAction((c) -> onAction());
		buttons.add(ok);
		buttons.add(cancel);

		updateState();
	}

	private void updateState() {
		final boolean sel = this.pipelineView.hasOneSelectedProperty().get();
		ok.setDisable(!sel);
	}

	private void onAction() {
		final PipelineItem item = this.pipelineView.getSelectedItem();
		if (item == null) {
			return;
		}
		final int pipelineId = (item.getPipeline() == null)
				? -1
				: item.getPipeline().id;
		for (ProjectPage page : this.pages) {
			page.setPipelineId(pipelineId);
		}
		stage.close();
	}

	/**
	 * A page item.
	 */
	private static class PageItem implements DataItemListView.DataItem {

		final private StringProperty nameProperty;
		final private StringProperty descriptionProperty;

		/**
		 * Creates a new page item.
		 *
		 * @param page the page.
		 */
		public PageItem(ProjectPage page) {
			this.nameProperty = new SimpleStringProperty(page.getName());
			this.descriptionProperty = new SimpleStringProperty(page.getPipelineName());
		}

		@Override
		public StringProperty nameProperty() {
			return nameProperty;
		}

		@Override
		public StringProperty descriptionProperty() {
			return descriptionProperty;
		}

	}

	/**
	 * A pipeline item.
	 */
	private static class PipelineItem implements DataItemListView.DataItem {

		final private StringProperty nameProperty;
		final private PrototypePipeline pipeline;

		/**
		 * Creates a pipeline item for no (or the empty) pipeline.
		 */
		public PipelineItem() {
			this.nameProperty = new SimpleStringProperty(
					L10n.getInstance().getString("none").toLowerCase()
			);
			this.pipeline = null;
		}

		/**
		 * Creates a new pipeline item.
		 *
		 * @param pipeline the pipeline.
		 */
		public PipelineItem(PrototypePipeline pipeline) {
			this.pipeline = pipeline;
			this.nameProperty = new SimpleStringProperty(pipeline.getName());
		}

		@Override
		public StringProperty nameProperty() {
			return nameProperty;
		}

		/**
		 * Returns the pipeline.
		 *
		 * @return the pipeline, or {@code null} in case of the empty pipeline.
		 */
		public PrototypePipeline getPipeline() {
			return this.pipeline;
		}

	}

}
