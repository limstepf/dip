package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.RadioChoiceBox;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PipelineManager;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.model.PrototypePipeline;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getCancelButton;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getDefaultButton;
import ch.unifr.diva.dip.gui.layout.Listable;
import java.util.Arrays;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Delete pipeline dialog. We can't delete pipelines that are still assigned
 * to/in use by some page. This dialog offers ways to fix the problem.
 */
public class DeletePipelineDialog extends AbstractDialog {

	protected final static Insets titleInsets = new Insets(0, 0, UIStrategyGUI.Stage.insets, 0);
	private final ApplicationHandler handler;
	private final Button ok;
	private final Button cancel;
	private final List<ProjectPage> pages;
	private final List<PrototypePipeline> deletedPipelines;
	private final List<Integer> deletedPipelineIds;
	private final ListView<PageItem> items;
	private final InvalidationListener validListener;
	private boolean done = false;
	private boolean isValid = false;

	/**
	 * Creates a new delete pipeline dialog.
	 *
	 * @param handler the application handler.
	 * @param pages list of pages still in use by some pipeline.
	 * @param deletedPipelines pipelines to be deleted, but still in use.
	 * @param deletedPipelineIds list of pipeline ids to be deleted.
	 */
	public DeletePipelineDialog(ApplicationHandler handler, List<ProjectPage> pages, List<PrototypePipeline> deletedPipelines, List<Integer> deletedPipelineIds) {
		super(
				handler.getProject().getPipelineEditor(handler.uiStrategy.getStage()).stage()
		);
		this.handler = handler;
		this.pages = pages;
		this.deletedPipelines = deletedPipelines;
		this.deletedPipelineIds = deletedPipelineIds;

		setTitle(localize(this.pages.size() > 1
				? "pipeline.delete.selected"
				: "pipeline.delete"
		));

		final Label conf = new Label(
				PipelineManager.formatDeletePipelineMessage(this.deletedPipelines)
		);
		VBox.setMargin(conf, titleInsets);
		conf.getStyleClass().add("dip-title");

		final VBox topbox = new VBox();
		BorderPane.setMargin(topbox, titleInsets);
		topbox.getChildren().setAll(
				conf,
				new Label(localize("pipeline.delete.inuse")),
				new Label(localize("howto.proceed"))
		);
		this.items = Listable.newListView();

		this.ok = getDefaultButton(localize("ok"));
		this.cancel = getCancelButton(stage);

		this.validListener = (c) -> {
			this.isValid = areAllValid();
			this.ok.setDisable(!this.isValid);
		};

		for (ProjectPage p : this.pages) {
			final PageItem item = new PageItem(this.handler, p, this.deletedPipelineIds);
			item.validProperty().addListener(validListener);
			this.items.getItems().add(item);
		}

		this.root.setTop(topbox);
		this.root.setCenter(items);

		ok.setDisable(true);
		ok.setOnAction((e) -> {
			stage.hide();
			done = true;
		});

		buttons.add(ok);
		buttons.add(cancel);

		root.requestFocus();
	}

	/**
	 * Checks whether the operation was a success (the ok button has been
	 * clicked), or not.
	 *
	 * @return {@code true} in case of success, {@code false} otherwise.
	 */
	public boolean isOk() {
		return done;
	}

	private boolean areAllValid() {
		for (PageItem item : this.items.getItems()) {
			if (!item.isValid()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether all conflicts can be resolved, or not.
	 *
	 * @return {@code true} if conflicts can be resolved, {@code false}
	 * otherwise.
	 */
	public boolean isValid() {
		return this.isValid;
	}

	/**
	 * Executes the repairs/resolves the conflicts.
	 *
	 * @return {@code true} if all conflicts have been successfully resolved,
	 * {@code false} otherwise.
	 */
	public boolean repair() {
		if (!isValid()) {
			return false;
		}

		for (PageItem item : this.items.getItems()) {
			item.repair();
		}
		return true;
	}

	/**
	 * A page item of a page assigned to a pipeline we wanna delete.
	 */
	public static class PageItem implements Listable, Localizable {

		private final static Insets glyphInsets = new Insets(0, UIStrategyGUI.Stage.insets, 0, 0);
		private final ApplicationHandler handler;
		private final ProjectPage page;
		private final List<Integer> deletedPipelines;
		private final BorderPane pane;
		private final HBox hbox;
		private final ComboBox<PipelineManager.PipelineItem> pipelines;
		private final RadioChoiceBox choice;
		private final Glyph okGlyph;
		private final Glyph errorGlyph;

		/**
		 * Creates a new page item.
		 *
		 * @param handler the application handler.
		 * @param page the offending project pages that use pipelines we wanna
		 * delete.
		 * @param deletedPipelines the ids of the pipelines we wanna delete.
		 */
		public PageItem(ApplicationHandler handler, ProjectPage page, List<Integer> deletedPipelines) {
			this.handler = handler;
			this.page = page;
			this.deletedPipelines = deletedPipelines;
			this.pipelines = this.handler.getProject().pipelineManager().getComboBox();
			final int id = this.page.getPipelineId(); // item's hashCode is the pipeline id!
			final PipelineManager.PipelineItem item = new PipelineManager.PipelineItem(id, "");

			this.pipelines.getSelectionModel().select(item);
			this.pipelines.getStyleClass().add("dip-small");
			for (PipelineManager.PipelineItem p : pipelines.getItems()) {
				if (deletedPipelines.contains(p.id)) {
					p.disabledProperty().set(true);
				}
			}
			this.hbox = new HBox();
			hbox.getChildren().setAll(
					new Label(localize("pipeline.reassign") + ": "),
					pipelines
			);

			final Label title = new Label(this.page.getName());
			BorderPane.setMargin(title, titleInsets);
			title.getStyleClass().add("dip-title");

			this.choice = new RadioChoiceBox(
					hbox,
					new Label(localize("page.delete"))
			);

			this.okGlyph = UIStrategyGUI.Glyphs.newOkGlyph(Glyph.Size.NORMAL);
			this.errorGlyph = UIStrategyGUI.Glyphs.newErrorGlyph(Glyph.Size.NORMAL);
			BorderPane.setMargin(this.okGlyph, glyphInsets);
			BorderPane.setMargin(this.errorGlyph, glyphInsets);

			this.pipelines.setOnAction((c) -> updateValidProperty());
			this.choice.selectedToggleProperty().addListener((c) -> updateValidProperty());

			this.pane = new BorderPane();
			this.pane.setTop(title);
			this.pane.setCenter(this.choice);

			updateValidProperty();
		}

		private BooleanProperty validProperty = new SimpleBooleanProperty(false);

		private void updateValidProperty() {
			boolean isValid = true;
			final RadioChoiceBox.RadioChoice<?> c = this.choice.selectedRadioChoice();
			if (c.node.equals(this.hbox)) {
				final PipelineManager.PipelineItem selected = pipelines.getSelectionModel().getSelectedItem();
				if (deletedPipelines.contains(selected.id)) {
					isValid = false;
				}
			}
			validProperty.set(isValid);
			this.pane.setLeft(isValid ? this.okGlyph : this.errorGlyph);
		}

		/**
		 * The valid property of the page item.
		 *
		 * @return the valid property of the page item.
		 */
		public ReadOnlyBooleanProperty validProperty() {
			return this.validProperty;
		}

		/**
		 * Checks whether the conflict can be resolved, or not.
		 *
		 * @return {@code true} if the conflict has been resolved, {@code false}
		 * otherwise.
		 */
		public boolean isValid() {
			return validProperty().get();
		}

		/**
		 * Repairs/resolves the conflict with the page.
		 */
		public void repair() {
			final RadioChoiceBox.RadioChoice<?> c = this.choice.selectedRadioChoice();
			if (c.node.equals(this.hbox)) {
				final PipelineManager.PipelineItem selected = pipelines.getSelectionModel().getSelectedItem();
				this.page.setPipelineId(selected.id);
			} else {
				// delete page; no confirmation needed
				this.handler.getProject().deletePages(Arrays.asList(this.page), false);
			}
		}

		@Override
		public Parent node() {
			return pane;
		}

	}

}
