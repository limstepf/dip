package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.datastructures.FileReference;
import ch.unifr.diva.dip.api.parameters.FileParameter;
import ch.unifr.diva.dip.api.parameters.StringParameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import ch.unifr.diva.dip.gui.dialogs.NewProjectDialog;
import java.util.Arrays;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export/save dialog for processor presets.
 */
public class ProcessorPresetExportDialog extends AbstractDialog {

	private static final Logger log = LoggerFactory.getLogger(NewProjectDialog.class);
	private final Button ok = getDefaultButton(localize("save"));
	private final Button cancel = getCancelButton(stage);
	private final StringParameter presetName;
	private final XorParameter dst;
	private final FileParameter dstFile;

	/**
	 * Creates a new export/save dialog for processor presets.
	 *
	 * @param owner owner of the dialog.
	 * @param wrapper processor wrapper which parameters are to be
	 * exported/saved as preset.
	 */
	public ProcessorPresetExportDialog(Window owner, ProcessorWrapper wrapper) {
		super(owner);
		setTitle(localize("preset.save"));

		final double b = UIStrategyGUI.Stage.insets;

		final Label nameLabel = new Label(localize("name") + ":");
		this.presetName = new StringParameter("", "");
		presetName.property().addListener((c) -> update());

		final Label dstLabel = new Label(localize("file") + ":");
		dstLabel.setPadding(new Insets(b, 0, 0, 0));
		this.dstFile = new FileParameter("", localize("locate"), FileParameter.Mode.SAVE);

		this.dst = new XorParameter("", Arrays.asList(
				new TextParameter(localize("directory.user")),
				dstFile
		));
		dst.property().addListener((c) -> update());

		final VBox pane = new VBox();
		pane.setSpacing(b);
		pane.getChildren().setAll(
				nameLabel,
				presetName.view().node(),
				dstLabel,
				dst.view().node()
		);

		this.root.setCenter(pane);

		ok.setDisable(true);
		ok.setOnAction((e) -> {
			stage.close();
		});
		buttons.add(ok);
		buttons.add(cancel);
	}

	private void update() {
		final boolean canExport = validName() && validDestination();
		ok.setDisable(!canExport);
	}

	private boolean validName() {
		final String name = presetName.get();
		return !name.trim().isEmpty();
	}

	private boolean validDestination() {
		final int sel = dst.get().selection;
		if (sel == 0) {
			return true;
		}

		final FileReference ref = dstFile.get();
		if (ref == null) {
			return false;
		}

		final String path = dstFile.get().path;
		// this can be a new file (not existing yet), or an existing file (to append to)
		// any idea for a better valid dstFile check?
		return !path.trim().isEmpty();
	}

}
