package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.DipData;
import ch.unifr.diva.dip.core.model.PresetData;
import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import java.nio.file.Path;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Export/save dialog for processor presets.
 */
public class ProcessorPresetExportDialog extends DataItemDialogBase<PresetData.PresetItem> {

	private DipData data;
	private final PrototypeProcessor wrapper;
	private final PresetData.PresetItem newPresetItem;
	private final TextField presetName;

	/**
	 * Creates a new export/save dialog for processor presets.
	 *
	 * @param handler the application handler.
	 * @param wrapper processor wrapper which parameters are to be
	 * exported/saved as preset.
	 */

	public ProcessorPresetExportDialog(ApplicationHandler handler, PrototypeProcessor wrapper) {
		super(
				handler,
				handler.dataManager.appDataDir.getProcessorPresetPath(wrapper.pid()),
				true
		);
		setTitle(localize("preset.save") + "...");
		setListLabel(localize("presets"));
		this.wrapper = wrapper;
		this.newPresetItem = new PresetData.PresetItem(wrapper);

		final Label nameLabel = new Label(localize("name") + ":");
		nameLabel.setPadding(new Insets(UIStrategyGUI.Stage.insets, 0, 0, 0));
		this.presetName = new TextField();
		this.presetName.textProperty().bindBidirectional(this.newPresetItem.nameProperty());
		this.presetName.textProperty().addListener(updateListener);
		vspane.getLeftChildren().add(nameLabel);
		vspane.getLeftChildren().add(this.presetName);

		initFile();
	}

	@Override
	public void onSelectFile(Path file) {
		this.data = loadDipData(file);
		final ObservableList<PresetData.PresetItem> list = this.data.getPresetData().getPresets(wrapper.pid(), wrapper.version());
		// append new preset
		list.add(this.newPresetItem);
		setItems(list);
	}

	@Override
	public boolean doAction() {
		this.data.setPresets(wrapper.pid(), wrapper.version(), getItems());
		return saveDipData(this.data, this.currentFile);
	}

	@Override
	public boolean isValid() {
		return isValidDestination(true) && isValidName();
	}

	private boolean isValidName() {
		final String name = this.presetName.getText();
		if (name == null) {
			return false;
		}
		return !name.trim().isEmpty();
	}

}
