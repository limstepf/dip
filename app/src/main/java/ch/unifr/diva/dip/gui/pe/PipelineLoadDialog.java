package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PipelineData;
import java.util.ArrayList;
import java.util.List;

/**
 * Loading dialog for pipelines. Used to create new projects, since we can't
 * import pipelines into the current project's pipeline manager.
 */
public class PipelineLoadDialog extends PipelineImportDialog {

	protected final List<PipelineData.PipelineItem> items;

	/**
	 * Creates a new loading dialog for pipelines.
	 *
	 * @param handler the application handler.
	 */
	public PipelineLoadDialog(ApplicationHandler handler) {
		super(handler);
		this.items = new ArrayList<>();
	}

	@Override
	public boolean doAction() {
		this.items.clear();
		this.items.addAll(this.dataItemList.getSelectedItems());
		return true;
	}

	/**
	 * Returns the loaded pipeline items. These can be later imported by a
	 * page's pipeline manager with a call to {@code importPipelines}.
	 *
	 * @return the pipeline items.
	 */
	public List<PipelineData.PipelineItem> getPipelineItems() {
		return this.items;
	}

}
