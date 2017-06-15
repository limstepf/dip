package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import ch.unifr.diva.dip.api.parameters.CheckboxParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.PipelineData;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getCancelButton;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getDefaultButton;
import ch.unifr.diva.dip.gui.layout.FormGridPane;
import ch.unifr.diva.dip.gui.layout.Listable;
import ch.unifr.diva.dip.utils.IOUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modified pipelines dialog. Used to resolve potential conflicts after
 * pipelines that are used by pages have been modified.
 */
public class ModifiedPipelinesDialog extends AbstractDialog {

	private static final Logger log = LoggerFactory.getLogger(ModifiedPipelinesDialog.class);
	private final static ColumnConstraints[] cc;

	static {
		final int n = 4;
		cc = new ColumnConstraints[n];
		for (int i = 0; i < n; i++) {
			cc[i] = new ColumnConstraints();
			cc[i].setHgrow(Priority.SOMETIMES);
			cc[i].setHalignment(HPos.LEFT);
		}
		cc[0].setHgrow(Priority.ALWAYS);
		cc[1].setHgrow(Priority.ALWAYS);
		cc[0].setPercentWidth(25.0);
		cc[1].setPercentWidth(35.0);
		cc[n - 2].setPercentWidth(20.0);
		cc[n - 1].setPercentWidth(20.0);
		cc[n - 1].setHalignment(HPos.RIGHT);
	}

	private final ApplicationHandler handler;
	private final Map<Integer, Set<Integer>> usedAndModified;
	private final Map<Integer, PipelineData.Pipeline> backupData;
	private final List<ModifiedPipelineSection> sections;
	private final ListView<AffectedPage> listView;
	private final VBox box;
	private final Button ok;
	private final Button cancel;

	/**
	 * Creates a new modified pipelines dialog.
	 *
	 * @param owner the owner of the dialog.
	 * @param handler the application handler.
	 * @param usedAndModified the map of used and modified pipelines.
	 * @param backupData backup data of all pipelines.
	 */
	public ModifiedPipelinesDialog(Window owner, ApplicationHandler handler, Map<Integer, Set<Integer>> usedAndModified, Map<Integer, PipelineData.Pipeline> backupData) {
		super(owner);

		this.handler = handler;
		this.usedAndModified = usedAndModified;
		this.backupData = backupData;
		this.ok = getDefaultButton(localize("apply"));
		this.cancel = getCancelButton(stage);

		setTitle(localize("pipelines.modified"));

		this.box = new VBox();
		box.setPrefWidth(720);

		final Label msgA = new Label(localize("pipelines.modified.msg"));
		final Label msgB = new Label(localize("pipelines.modified.msg.resolve"));
		msgB.setPadding(new Insets(0, 0, 10, 0));
		box.getChildren().add(msgA);
		box.getChildren().add(msgB);

		this.listView = Listable.newListView();

		final ContextMenu contextMenu = new ContextMenu();
		final MenuItem applyToAll = new MenuItem(localize("apply.all"));
		applyToAll.setOnAction((e) -> {
			final AffectedPage sel = listView.getSelectionModel().getSelectedItem();
			final int res = sel.getResolutionParameter().get().getSelectedIndex();
			final boolean patch = sel.getRemovePatchParameter().get();
			for (AffectedPage p : listView.getItems()) {
				if (!p.equals(sel)) {
					p.getResolutionParameter().setSelection(res);
					p.getRemovePatchParameter().set(patch);
				}
			}
		});
		contextMenu.getItems().add(applyToAll);
		listView.setContextMenu(contextMenu);

		final List<Label> headerLabels = Arrays.asList(
				new Label(localize("pipeline")),
				new Label(localize("page")),
				new Label(localize("resolution")),
				new Label(localize("pipeline.patch"))
		);
		for (Label label : headerLabels) {
			GridPane.setHgrow(label, Priority.ALWAYS);
			label.getStyleClass().add("dip-list-header");
		}
		final FormGridPane header = newRow(headerLabels);
		box.getChildren().add(header);
		this.sections = new ArrayList<>();
		for (Map.Entry<Integer, Set<Integer>> e : usedAndModified.entrySet()) {
			final ModifiedPipelineSection section = new ModifiedPipelineSection(
					e.getKey(),
					e.getValue()
			);
			sections.add(section);
			for (AffectedPage page : section.getAffectedPages()) {
				listView.getItems().add(page);
			}
		}
		VBox.setVgrow(listView, Priority.ALWAYS);
		box.getChildren().add(listView);

		final Label infoA = new Label(localize("pipelines.modified.msg.reset"));
		final Label infoB = new Label(localize("pipelines.modified.msg.cancel"));
		infoA.setPadding(new Insets(10, 0, 5, 0));
		infoA.setWrapText(true);
		infoB.setWrapText(true);
		box.getChildren().addAll(infoA, infoB);

		ok.setOnAction((e) -> {
			onCloseRequest(true);
			stage.hide();
		});
		cancel.setOnAction((e) -> {
			onCloseRequest();
			stage.hide();
		});
		buttons.add(ok);
		buttons.add(cancel);

		this.setOnCloseRequest((e) -> onCloseRequest());
		root.setCenter(box);
	}

	private void onCloseRequest() {
		onCloseRequest(false);
	}

	private void onCloseRequest(boolean resolve) {
		/*
		 * closeing/canceling means:
		 * 1) keep original pipelines assigned
		 * 2) revert/undo modifications by copying back original pipelines (no cloning)
		 */
		if (resolve) {
			for (ModifiedPipelineSection section : sections) {
				// first pass to check whether we're "cloning" the pipeline, by
				// restoring/-importing the original/unmodified one.
				boolean doClone = false;
				int newId = -1;
				for (AffectedPage a : section.getAffectedPages()) {
					final ValueListSelection val = a.getResolutionParameter().get();
					if (val.getSelectedIndex() == 2) {
						doClone = true;
					}
				}
				// clone/restore original pipeline
				if (doClone) {
					final PipelineData.Pipeline pipeline = backupData.get(section.pipeline.id);
					if (pipeline != null) {
						/*
						 * the original/unmodified pipeline we're about to restore/-import
						 * gets a new pipeline id, while the modified pipeline keeps the
						 * original one.
						 */
						newId = handler.getProject().pipelineManager().importPipeline(pipeline);
						section.pipeline.setName(section.getCloneName());
					} else {
						log.warn(
								"Can't clone/restore original pipeline. "
								+ "Pipeline not found. Modified pipeline: {}, "
								+ "missing(?) in backup data (key set): {}",
								section.pipeline,
								backupData.keySet()
						);
					}
				}
				// second pass
				for (AffectedPage a : section.getAffectedPages()) {
					final boolean removePatch = a.getRemovePatchParameter().get();
					/*
					 * update, and reset				-> -, reset page's pipeline
					 * update, no reset					-> -, -
					 * keep original pipeline			-> IMPOSSIBLE/doClone!
					 * update, and reset + doClone		-> -, reset page's pipeline
					 * update, no reset + doClone		-> -, -
					 * keep original pipeline + doClone -> update page's pipeline id, -
					 *
					 * + remove patch?
					 */
					final ValueListSelection val = a.getResolutionParameter().get();
					switch (val.getSelectedIndex()) {
						case 0: // update, and reset
							a.page.updatePipeline(true, removePatch);
							break;
						case 1: // update, no reset
							a.page.updatePipeline(false, removePatch);
							break;
						case 2: // keep original pipeline, which we just reimported,
							// so all we have to do is update the pipeline id
							a.page.setPipelineId(newId, false);
							break;
					}
				}
			}
		} else {
			// dialog got cancelled: revert all changed piplines (nothing to
			// reset, nothing to write back to projectData, all modifications are lost)
			for (ModifiedPipelineSection section : sections) {
				final PipelineData.Pipeline pipeline = backupData.get(section.pipeline.id);
				handler.getProject().pipelineManager().replacePipeline(pipeline);
			}
		}

		// unmark dirty flag for all pipelines (while the project keeps being dirty!)
		for (ModifiedPipelineSection section : sections) {
			section.pipeline.setModified(false);
		}
	}

	/**
	 * Modified pipeline section.
	 */
	private class ModifiedPipelineSection implements Localizable {

		private final List<AffectedPage> affectedPages;
		private final Pipeline pipeline;
		private final Set<Integer> pageids;
		private final InvalidationListener resolutionListener;
		private final Label cloneLabel;
		private final TextField cloneName;

		/**
		 * Creats a new, modified pipeline section.
		 *
		 * @param pipelineid the pipeline id.
		 * @param pageids the page id's of the affected pages.
		 */
		public ModifiedPipelineSection(int pipelineid, Set<Integer> pageids) {
			this.pageids = pageids;
			this.pipeline = handler.getProject().pipelineManager().getPipeline(pipelineid);
			this.resolutionListener = (c) -> updateResolution();
			this.cloneLabel = new Label(localize("clone.as") + ":");
			cloneLabel.getStyleClass().add("dip-small");
			this.cloneName = new TextField(IOUtils.nameSuffixIncrement(pipeline.getName()));
			cloneName.getStyleClass().add("dip-small");
			this.affectedPages = new ArrayList<>();
			for (int pageid : pageids) {
				final AffectedPage apage = new AffectedPage(
						handler.getProject().getPage(pageid)
				);
				apage.getResolutionParameter().property().addListener(resolutionListener);
				affectedPages.add(apage);
			}
			if (affectedPages.size() > 0) {
				final AffectedPage firstPage = affectedPages.get(0);
				firstPage.setPipeline(pipeline.getName(), pipeline.id, cloneLabel, cloneName);
			}
			updateResolution();
		}

		private void updateResolution() {
			boolean doClone = false;
			for (AffectedPage p : affectedPages) {
				if (p.getResolutionParameter().get().selection == 2) {
					doClone = true;
					break;
				}
			}
			cloneLabel.setVisible(doClone);
			cloneLabel.setDisable(!doClone);
			cloneName.setVisible(doClone);
			cloneName.setDisable(!doClone);
		}

		/**
		 * Returns the affected pages of this modified pipeline section.
		 *
		 * @return the affected pages.
		 */
		protected List<AffectedPage> getAffectedPages() {
			return this.affectedPages;
		}

		/**
		 * Returns the clone name used for the modified pipeline, in case the
		 * original/unmodified pipeline will be restored.
		 *
		 * @return the clone name used for the modified pipeline.
		 */
		protected String getCloneName() {
			final String name = cloneName.getText().trim();
			return name.isEmpty() ? IOUtils.nameSuffixIncrement(pipeline.getName()) : name;
		}

	}

	/**
	 * Affected page view.
	 */
	private static class AffectedPage implements Listable, Localizable {

		private final FormGridPane root;
		private final ProjectPage page;
		private final CheckboxParameter removePatch;
		private final XorParameter resolution;
		private final Label pipelineTitle;
		private final Label pipelineId;
		private final VBox pipelineBox;
		private final VBox pageBox;

		/**
		 * Creates a new affected page list item.
		 *
		 * @param page the affected page.
		 */
		public AffectedPage(ProjectPage page) {
			this.page = page;
			this.pipelineTitle = new Label();
			pipelineTitle.setWrapText(true);
			this.pipelineId = new Label();
			pipelineId.getStyleClass().add("dip-small");
			this.pipelineBox = new VBox();
			pipelineBox.getChildren().addAll(pipelineTitle, pipelineId);

			final Label pageTitle = new Label(page.getName());
			pageTitle.setWrapText(true);
			final Label pageId = new Label(String.format("id: %d", page.id));
			pageId.getStyleClass().add("dip-small");
			this.pageBox = new VBox();
			pageBox.getChildren().addAll(pageTitle, pageId);

			final boolean isPatched = page.hasPipelinePatch();
			final Node patchNode;
			this.removePatch = newRemovePatchParameter();
			if (isPatched) {
				patchNode = removePatch.view().node();
			} else {
				patchNode = new Label(localize("pipeline.patched.not"));
			}

			this.resolution = newResolutionParameter();

			this.root = newRow(
					pipelineBox,
					pageBox,
					resolution.view().node(),
					patchNode
			);
		}

		/**
		 * Returns the resolution parameter.
		 *
		 * @return the resolution parameter.
		 */
		protected XorParameter getResolutionParameter() {
			return this.resolution;
		}

		/**
		 * Returns the remove (pipeline) patch parameter.
		 *
		 * @return the remove (pipeline) patch parameter.
		 */
		protected CheckboxParameter getRemovePatchParameter() {
			return this.removePatch;
		}

		/**
		 * Sets the pipeline. Should be called on the first affected page in a
		 * modified pipeline section.
		 *
		 * @param name the name of the pipeline.
		 * @param id the pipeline id.
		 * @param cloneLabel the clone label.
		 * @param cloneName the clone text field for the clone name.
		 */
		protected void setPipeline(String name, int id, Node cloneLabel, Node cloneName) {
			this.pipelineTitle.setText(name);
			this.pipelineId.setText(String.format("id: %d", id));
			this.pipelineBox.getChildren().setAll(pipelineTitle, pipelineId, cloneLabel, cloneName);
		}

		@Override
		public Parent node() {
			return root;
		}

	}

	private static <T extends Node> FormGridPane newRow(List<T> nodes) {
		final FormGridPane pane = new FormGridPane(cc);
		pane.addRow(nodes);
		return pane;
	}

	private static <T extends Node> FormGridPane newRow(T... nodes) {
		return newRow(Arrays.asList(nodes));
	}

	private static XorParameter newResolutionParameter() {
		return new XorParameter(
				"resolution",
				Arrays.asList(
						new LabelParameter(L10n.getInstance().getString("pipeline.resolution.update.andreset")),
						new LabelParameter(L10n.getInstance().getString("pipeline.resolution.update.noreset")),
						new LabelParameter(L10n.getInstance().getString("pipeline.resolution.keep.original"))
				),
				0
		);
	}

	private static CheckboxParameter newRemovePatchParameter() {
		return new CheckboxParameter(true, L10n.getInstance().getString("pipeline.patch.remove"));
	}

}
