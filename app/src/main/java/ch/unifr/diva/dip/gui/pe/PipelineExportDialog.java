package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.DipData;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.PipelineData;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;

/**
 * Export/save dialog for pipelines.
 */
public class PipelineExportDialog extends DataItemDialogBase<PipelineData.PipelineItem> {

	private DipData data;
	private final List<Pipeline<ProcessorWrapper>> pipelines;
	private final List<PipelineData.PipelineItem> newPipelineItems;

	/**
	 * Create a new export/save dialog for pipelines.
	 *
	 * @param handler the application handler.
	 * @param pipelines the pipelines to be exported.
	 */
	public PipelineExportDialog(ApplicationHandler handler, List<Pipeline<ProcessorWrapper>> pipelines) {
		super(
				handler,
				handler.dataManager.appDataDir.getPipelinePresetPath(),
				true
		);
		setTitle(localize("pipeline.export"));
		setListLabel(localize("pipelines"));
		this.pipelines = pipelines;
		this.newPipelineItems = toPipelineItems(this.pipelines);
		initFile();
	}

	private List<PipelineData.PipelineItem> toPipelineItems(List<Pipeline<ProcessorWrapper>> pipelines) {
		final List<PipelineData.PipelineItem> items = new ArrayList<>();
		for (Pipeline<ProcessorWrapper> p : pipelines) {
			items.add(new PipelineData.PipelineItem(p));
		}
		return items;
	}

	@Override
	public void onSelectFile(Path file) {
		this.data = loadDipData(file);
		final ObservableList<PipelineData.PipelineItem> list = this.data.getPipelineData().getPipelineItems();
		// append new pipelines
		for (PipelineData.PipelineItem item : this.newPipelineItems) {
			list.add(item);
		}
		setItems(list);
	}

	@Override
	public boolean doAction() {
		this.data.setPipelines(getItems());
		return saveDipData(this.data, this.currentFile);
	}

	@Override
	public boolean isValid() {
		return isValidDestination(true);
	}

}
