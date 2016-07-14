package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextField;

/**
 * String parameter.
 */
public class StringParameter extends PersistentParameterBase<String, StringParameter.StringView> {

	/**
	 * Creates a new, empty string parameter.
	 *
	 * @param label label of the parameter.
	 */
	public StringParameter(String label) {
		this(label, "");
	}

	/**
	 * Creates a new string parameter.
	 *
	 * @param label label of the parameter.
	 * @param defaultValue default value/string.
	 */
	public StringParameter(String label, String defaultValue) {
		super(label, defaultValue);
	}

	@Override
	protected StringView newViewInstance() {
		return new StringView(this);
	}

	protected final List<ViewHook> viewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the textfield. This method is only called
	 * if the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a label.
	 */
	public void addTextFieldViewHook(ViewHook<TextField> hook) {
		this.viewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeTextFieldViewHook(ViewHook<TextField> hook) {
		this.viewHooks.remove(hook);
	}

	/**
	 * Simple String view with a TextField.
	 */
	public static class StringView extends PersistentParameterBase.ParameterViewBase<StringParameter, String, TextField> {

		public StringView(StringParameter parameter) {
			super(parameter, new TextField());
			set(parameter.get());

			PersistentParameter.applyViewHooks(root, parameter.viewHooks);
			root.textProperty().addListener((obs) -> {
				parameter.valueProperty.set(get());
			});
		}

		protected final String get() {
			return root.getText();
		}

		@Override
		public final void set(String value) {
			root.setText(value);
		}
	}

}
