package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.parameters.FileParameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getCancelButton;
import static ch.unifr.diva.dip.gui.dialogs.AbstractDialog.getDefaultButton;
import ch.unifr.diva.dip.gui.dialogs.NewProjectDialog;
import java.util.Arrays;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import/load dialog for processor presets.
 */
public class ProcessorPresetImportDialog extends AbstractDialog {

	private static final Logger log = LoggerFactory.getLogger(NewProjectDialog.class);
	private final Button ok = getDefaultButton(localize("load"));
	private final Button cancel = getCancelButton(stage);

	private final XorParameter source;
	private final FileParameter sourceFile;
	private final ListView presetList;

	/**
	 * Creates a new import/load dialog for processor presets.
	 *
	 * @param owner owner of the dialog.
	 * @param wrapper processor wrapper supposed to load the preset.
	 */
	public ProcessorPresetImportDialog(Window owner, ProcessorWrapper wrapper) {
		super(owner);
		setTitle(localize("preset.load"));

		final double b = UIStrategyGUI.Stage.insets;

		final Label sourceLabel = new Label(localize("source") + ":");
		this.sourceFile = new FileParameter("", localize("locate"), FileParameter.Mode.OPEN);
		this.source = new XorParameter("source", Arrays.asList(
				new TextParameter(localize("directory.user")),
				sourceFile
		));
		final Label presetLabel = new Label(localize("preset") + ":");
		presetLabel.setPadding(new Insets(b, 0, 0, 0));
		this.presetList = new ListView();
		presetList.setPrefHeight(256);
		presetList.getSelectionModel().selectedIndexProperty().addListener((e) -> {
			final int index = presetList.getSelectionModel().getSelectedIndex();
			ok.setDisable(index < 0);
		});

		final VBox pane = new VBox();
		pane.setSpacing(b);
		pane.getChildren().setAll(
				sourceLabel,
				source.view().node(),
				presetLabel,
				presetList
		);

		this.root.setCenter(pane);

		ok.setDisable(true);
		ok.setOnAction((e) -> {
			stage.close();
		});
		buttons.add(ok);
		buttons.add(cancel);
	}

}
