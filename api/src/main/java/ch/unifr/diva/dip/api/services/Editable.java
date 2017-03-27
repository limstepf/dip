package ch.unifr.diva.dip.api.services;

import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.SingleRowParameter;
import ch.unifr.diva.dip.api.tools.Tool;
import java.util.List;
import java.util.Map;
import javafx.scene.shape.Shape;

/**
 * An editable processor offers tools to process its inputs manually.
 */
public interface Editable extends Resetable {

	/**
	 * The editing tools of the processor.
	 *
	 * @return a list of editing tools.
	 */
	public List<Tool> tools();

	/**
	 * The shared options of the processor. These options are shared by all
	 * tools of the processor.
	 *
	 * @return the shared options of the processor.
	 */
	public Map<String, SingleRowParameter> options();

	/**
	 * Checks whether this processor has shared options. These options are
	 * shared by all tools of the processor.
	 *
	 * @return True if this processor has at least one shared option, False
	 * otherwise.
	 */
	default boolean hasOptions() {
		return !options().isEmpty();
	}

	/**
	 * Callback method called before switching away from this processor's
	 * context. Editable processors usually do not store persistent data
	 * immediately after a tool has been used. Before switching context (e.g. by
	 * changing the page, or closing the project), this method will be called,
	 * allowing editable processors to store unsaved data.
	 *
	 * @param context the processor context.
	 * @param saveRequired if True the state of the processor needs to be saved
	 * (since we're about to close the page with the pipeline containing this
	 * processor), otherwise only the processor context needs to be updated
	 * (since references/paths might have changed; e.g. after saving the
	 * project), while saving the processor's state is not required.
	 */
	public void onContextSwitch(ProcessorContext context, boolean saveRequired);

	/**
	 * Callback method called if the global selection mask has changed. Only gets
	 * called for active/currently selected processors, and once as a new processor
	 * gets selected (no matter if the mask has actually changed).
	 *
	 * @param selectionMask the selection mask, or null if nothing is selected.
	 */
	default void onSelectionMaskChanged(Shape selectionMask) {

	}

}
