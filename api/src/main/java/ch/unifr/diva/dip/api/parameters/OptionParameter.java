package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ComboBox;

/**
 * Option (or DropDown-Menu) parameter. Note that the choosen option is stored
 * as an Integer, so for an Enumeration the ordinal get's stored - which might
 * not be the best thing to do (what if the Enumeration changes, so ordinals
 * might too...?). So maybe better use an EnumParameter (which saves the name as
 * a String) instead.
 *
 * @see EnumParameter
 */
public class OptionParameter extends PersistentParameterBase<Integer, OptionParameter.OptionView> implements SingleRowParameter<Integer> {

	protected final List<String> options;

	/**
	 * Default constructor.
	 *
	 * @param label label.
	 * @param options labels of the available options.
	 * @param defaultValue index of the default option.
	 */
	public OptionParameter(String label, List<String> options, int defaultValue) {
		super(label, defaultValue);

		this.options = options;
	}

	@Override
	protected OptionView newViewInstance() {
		return new OptionView(this);
	}

	protected final List<ViewHook<ComboBox>> comboBoxViewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the combo box. This method is only called
	 * if the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a combo box.
	 */
	public void addComboBoxViewHook(ViewHook<ComboBox> hook) {
		this.comboBoxViewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeComboBoxViewHook(ViewHook<ComboBox> hook) {
		this.comboBoxViewHooks.remove(hook);
	}

	protected ViewHook<ComboBox> singleRowViewHook = null;

	@Override
	public void initSingleRowView() {
		singleRowViewHook = (c) -> {
			c.getStyleClass().add("dip-small");
		};
	}

	/**
	 * Option view with a ComboBox.
	 */
	public static class OptionView extends PersistentParameterBase.ParameterViewBase<OptionParameter, Integer, ComboBox> {

		/**
		 * Creates a new option view.
		 *
		 * @param parameter the option parameter.
		 */
		public OptionView(OptionParameter parameter) {
			super(parameter, new ComboBox());
			root.setMaxWidth(Double.MAX_VALUE);
			root.getItems().addAll(parameter.options);
			set(parameter.get());
			PersistentParameter.applyViewHooks(
					root,
					parameter.comboBoxViewHooks,
					parameter.singleRowViewHook
			);
			root.valueProperty().addListener((obs) -> {
				parameter.setLocal(get());
			});
		}

		protected final Integer get() {
			return root.getSelectionModel().getSelectedIndex();
		}

		@Override
		public final void set(Integer value) {
			root.getSelectionModel().select((int) value);
		}

	}

}
