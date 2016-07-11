package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * A label as parameter. This is a transient parameter (for display purposes
 * only). Nothing gets saved.
 */
public class LabelParameter implements Parameter {

	private final String label;
	private Parameter.View view;

	/**
	 * Creates a label parameter.
	 *
	 * @param label text of the label.
	 */
	public LabelParameter(String label) {
		this.label = label;
	}

	@Override
	public View view() {
		if (view == null) {
			this.view = new LabelView(this);
		}
		return view;
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

	/**
	 * A label view.
	 */
	public static class LabelView implements Parameter.View {

		private final Label label;

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
