package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 * A button as parameter. This is a transient parameter (for display purposes
 * only). Nothing gets saved.
 */
public class ButtonParameter extends TransientParameterBase<ButtonParameter.ButtonView> implements SingleRowParameter {

	private final String label;
	private EventHandler<ActionEvent> onAction;

	/**
	 * Creates a new button parameter without onAction event handler. Use
	 * {@code setOnAction()}, or a view hook to make the button functional in
	 * one way or another.
	 *
	 * @param label label of the button.
	 */
	public ButtonParameter(String label) {
		this(label, null);
	}

	/**
	 * Creates a new button parameter.
	 *
	 * @param label label of the button.
	 * @param onAction the onAction event handler.
	 */
	public ButtonParameter(String label, EventHandler<ActionEvent> onAction) {
		this.label = label;
		this.onAction = onAction;
	}

	/**
	 * Sets the onAction event handler of the button.
	 *
	 * @param onAction the onAction event handler.
	 */
	public void setOnAction(EventHandler<ActionEvent> onAction) {
		this.onAction = onAction;

		if (this.view != null) {
			this.view.setOnAction(onAction);
		}
	}

	@Override
	protected ButtonView newViewInstance() {
		return new ButtonView(this);
	}

	protected final List<PersistentParameter.ViewHook<Button>> viewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the label. This method is only called if
	 * the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a label.
	 */
	public void addButtonViewHook(PersistentParameter.ViewHook<Button> hook) {
		this.viewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeButtonViewHook(PersistentParameter.ViewHook<Button> hook) {
		this.viewHooks.remove(hook);
	}

	protected PersistentParameter.ViewHook<Button> singleRowViewHook = null;

	@Override
	public void initSingleRowView() {
		singleRowViewHook = (b) -> {
			b.getStyleClass().add("dip-small");
		};
	}

	/**
	 * A button view.
	 */
	public static class ButtonView implements Parameter.View {

		private final Button button;

		/**
		 * Creates a new button view.
		 *
		 * @param parameter the button parameter.
		 */
		public ButtonView(ButtonParameter parameter) {
			this.button = new Button(parameter.label);
			if (parameter.onAction != null) {
				setOnAction(parameter.onAction);
			}
			PersistentParameter.applyViewHooks(
					this.button,
					parameter.viewHooks,
					parameter.singleRowViewHook
			);
		}

		/**
		 * Sets the on action event handler of the button.
		 *
		 * @param onAction the event handler.
		 */
		public final void setOnAction(EventHandler<ActionEvent> onAction) {
			this.button.setOnAction(onAction);
		}

		@Override
		public Node node() {
			return button;
		}

	}

}
