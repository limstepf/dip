package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * A label as parameter. This is a transient parameter (for display purposes
 * only). Nothing gets saved.
 */
public class LabelParameter extends TransientParameterBase implements SingleRowParameter {

	protected final String label;

	/**
	 * Creates a label parameter.
	 *
	 * @param label text of the label.
	 */
	public LabelParameter(String label) {
		this.label = label;
	}

	@Override
	protected Parameter.View newViewInstance() {
		return new LabelView(this);
	}

	protected final List<PersistentParameter.ViewHook<Label>> viewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the label. This method is only called if
	 * the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a label.
	 */
	public void addLabelViewHook(PersistentParameter.ViewHook<Label> hook) {
		this.viewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeLabelViewHook(PersistentParameter.ViewHook<Label> hook) {
		this.viewHooks.remove(hook);
	}

	@Override
	public void initSingleRowView() {
		// looking good...
	}

	/**
	 * A label view.
	 */
	public static class LabelView implements Parameter.View {

		private final Label label;

		/**
		 * Creates a new label view.
		 *
		 * @param parameter the label parameter.
		 */
		public LabelView(LabelParameter parameter) {
			this.label = new Label(parameter.label);
			this.label.getStyleClass().add("dip-small");
			PersistentParameter.applyViewHooks(this.label, parameter.viewHooks);
		}

		@Override
		public Node node() {
			return label;
		}

	}

}
