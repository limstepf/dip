package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.core.model.ProjectData;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * A repair project dialog.
 */
public class RepairProjectDialog extends AbstractDialog {

	private final RepairProjectUnit repair;
	private final ProjectData projectData;
	private final ProjectData.ValidationResult validation;
	private final VBox box;
	private final Button ok;
	private final Button cancel;
	private boolean done = false;

	/**
	 * Creates a new repair project dialog.
	 *
	 * @param owner the owner of the dialog.
	 * @param repair the repair project unit (may already/still be running).
	 */
	public RepairProjectDialog(Window owner, RepairProjectUnit repair) {
		super(owner);
		this.repair = repair;
		this.projectData = this.repair.handler.getRepairData();
		this.validation = this.repair.handler.getRepairValidation();

		setTitle(localize("project.open.warning"));

		this.box = new VBox();
		box.setPrefWidth(owner.getWidth() * .78);
		box.setPrefHeight(owner.getHeight() * .42);
		box.setSpacing(10);

		for (RepairProjectUnit.RepairSection section : this.repair.sections) {
			VBox.setVgrow(section.getComponent(stage), Priority.ALWAYS);
			box.getChildren().add(section.getComponent(stage));
		}

		this.ok = getDefaultButton(localize("ok"));
		this.cancel = getCancelButton(stage);

		ok.setDisable(true);
		ok.disableProperty().bind(Bindings.not(this.repair.canFullyRepairProperty));
		ok.setOnAction((e) -> {
			this.repair.applyRepairs();
			this.done = true;
			stage.hide();
		});
		cancel.setOnAction((e) -> {
			this.repair.stop();
			stage.hide();
		});
		buttons.add(ok);
		buttons.add(cancel);

		this.setOnCloseRequest((e) -> this.repair.stop());
		root.setCenter(box);
		this.repair.updateCanFullyRepairProperty();
	}

	/**
	 * Checks whether the repairs have been applied.
	 *
	 * @return {@code true} if the repairs have been applied, {@code false} if
	 * the dialog got closed prematurely.
	 */
	public boolean isOk() {
		return this.done;
	}

}
