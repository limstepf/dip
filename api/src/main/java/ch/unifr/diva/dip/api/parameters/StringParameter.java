package ch.unifr.diva.dip.api.parameters;

import javafx.scene.control.TextField;

/**
 * String parameter.
 */
public class StringParameter extends PersistentParameterBase<String> {

	public StringParameter(String label, String defaultValue) {
		super(label, defaultValue);
	}

	@Override
	protected PersistentParameter.View newViewInstance() {
		return new StringView(this);
	}

	/**
	 * Simple String view with a TextField.
	 */
	public static class StringView extends ParameterViewBase<StringParameter, String, TextField> {

		public StringView(StringParameter parameter) {
			super(parameter, new TextField());
			set(parameter.get());

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
