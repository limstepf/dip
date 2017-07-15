package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.DipData;
import ch.unifr.diva.dip.core.model.PipelineData;
import java.nio.file.Path;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;

/**
 * Import/load dialog for pipelines.
 */
public class PipelineImportDialog extends DataItemDialogBase<PipelineData.PipelineItem> {

	private DipData data;

	/**
	 * Creates a new import/load dialog for pipelines.
	 *
	 * @param handler the application handler.
	 */
	public PipelineImportDialog(ApplicationHandler handler) {
		super(
				handler,
				handler.dataManager.appDataDir.getPipelinePresetPath(),
				false
		);
		setTitle(localize("pipeline.import"));
		setListLabel(localize("pipelines"));
		setSelectionMode(SelectionMode.MULTIPLE);
		initFile();

		dataItemList.hasSelectionProperty.addListener(updateListener);
	}

	@Override
	public boolean isValid() {
		return isValidDestination(false) && dataItemList.hasSelectionProperty.get();
	}

	@Override
	public void onSelectFile(Path file) {
		this.data = loadDipData(file);
		final ObservableList<PipelineData.PipelineItem> list = this.data.getPipelineData().getPipelineItems();
		setItems(list);
	}

	@Override
	public boolean doAction() {
		final List<PipelineData.PipelineItem> items = this.dataItemList.getSelectedItems();
		handler.getProject().pipelineManager().importPipelines(items);
		return true;
	}

}
