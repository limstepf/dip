package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.DipData;
import ch.unifr.diva.dip.core.model.PresetData;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import java.nio.file.Path;
import javafx.collections.ObservableList;

/**
 * Import/load dialog for processor presets.
 */
public class ProcessorPresetImportDialog extends DataItemDialogBase<PresetData.PresetItem> {

	private DipData data;
	private final ProcessorWrapper wrapper;

	/**
	 * Creates a new import/load dialog for processor presets.
	 *
	 * @param handler the application handler.
	 * @param wrapper processor wrapper supposed to load the preset.
	 */

	public ProcessorPresetImportDialog(ApplicationHandler handler, ProcessorWrapper wrapper) {
		super(
				handler,
				handler.dataManager.appDataDir.getProcessorPresetPath(wrapper.pid()),
				false
		);
		setTitle(localize("preset.load"));
		setListLabel(localize("presets"));
		this.wrapper = wrapper;

		initFile();
	}

	@Override
	public void onSelectFile(Path file) {
		this.data = loadDipData(file);
		final ObservableList<PresetData.PresetItem> list = this.data.getPresetData().getPresets(wrapper.pid(), wrapper.version());
		setItems(list);
	}

	@Override
	public boolean doAction() {
		final PresetData.PresetItem item = this.dataItemList.getSelectedItem();
		if (item == null) {
			return false;
		}
		final PresetData.Preset preset = item.toPresetData();
		this.wrapper.setParameters(preset.parameters);
		return true;
	}

}
