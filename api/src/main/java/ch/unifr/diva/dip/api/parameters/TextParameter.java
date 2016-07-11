package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.text.Text;

/**
 * Text as a parameter. This is a transient parameter (for display purposes
 * only). Nothing gets saved.
 *
 * <p>
 * TextParameter is backed by a StringProperty (bound to the view/actual Text
 * field, once initialized), so the text/message can be easily changed at
 * runtime, nevertheless.
 */
public class TextParameter implements Parameter {

	private final StringProperty textProperty;
	private Parameter.View view;

	/**
	 * Creates a text parameter.
	 *
	 * @param text text/message of the parameter.
	 */
	public TextParameter(String text) {
		this.textProperty = new SimpleStringProperty(text);
	}

	/**
	 * Updates/sets the text/message.
	 *
	 * @param text the text/message.
	 */
	public void setText(String text) {
		this.textProperty.set(text);
	}

	/**
	 * Returns the text/message.
	 *
	 * @return the text/message.
	 */
	public String getText() {
		return this.textProperty.get();
	}

	@Override
	public View view() {
		if (view == null) {
			this.view = new TextView(this);
		}
		return view;
	}

	protected final List<PersistentParameter.ViewHook<Text>> viewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the label. This method is only called if
	 * the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a label.
	 */
	public void addTextViewHook(PersistentParameter.ViewHook<Text> hook) {
		this.viewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeTextViewHook(PersistentParameter.ViewHook<Text> hook) {
		this.viewHooks.remove(hook);
	}

	/**
	 * Text view.
	 */
	public static class TextView implements Parameter.View {

		private final Text text;

		public TextView(TextParameter parameter) {
			this.text = new Text();
			this.text.textProperty().bind(parameter.textProperty);
			PersistentParameter.applyViewHooks(this.text, parameter.viewHooks);
		}

		@Override
		public Node node() {
			return text;
		}
	}

}
