package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.utils.EnumStringMapper;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ComboBox;

/**
 * Enumeration parameter with a DropDown-Menu view. This is similar to the
 * OptionParameter but backed by a String (to store the Enumeration value's
 * name) instead of an Integer.
 *
 * @see OptionParameter
 */
public class EnumParameter extends PersistentParameterBase<String, EnumParameter.EnumView> implements SingleRowParameter<String> {

	final List<String> options;
	final List<String> labels;

	/**
	 * Constructor to turn an Enum into an EnumParameter.
	 *
	 * @param <E> enumeration class.
	 * @param label label.
	 * @param e the enumeration to choose an option from. The toString() method
	 * is used for the label, and is the same as the name of the enumeration
	 * option unless the toString() method is overridden.
	 * @param defaultValue name of the default enumeration option.
	 */
	public <E extends Enum<E>> EnumParameter(String label, Class<E> e, String defaultValue) {
		this(label, getEnumNames(e), getEnumLabels(e), defaultValue);
	}

	/**
	 * Constructor to turn an Enum into an EnumParameter.
	 *
	 * @param <E> enumeration class.
	 * @param label label.
	 * @param e the enumeration to choose an option from. The toString() method
	 * is used for the label, and is the same as the name of the enumeration
	 * option unless the toString() method is overridden.
	 * @param mapper custom enumeration string mapper to format (or translate)
	 * the labels.
	 * @param defaultValue name of the default enumeration option.
	 */
	public <E extends Enum<E>> EnumParameter(String label, Class<E> e, EnumStringMapper<E> mapper, String defaultValue) {
		this(label, getEnumNames(e), getEnumLabels(e, mapper), defaultValue);
	}

	/**
	 * Constructor to turn a list of options into an EnumParameter.
	 *
	 * @param label label.
	 * @param enumeration a list of enumeration names (also used as labels).
	 * @param defaultValue name of the default enumeration option.
	 */
	public EnumParameter(String label, List<String> enumeration, String defaultValue) {
		this(label, enumeration, null, defaultValue);
	}

	/**
	 * Constructor to turn a map (name/key -> label) into an EnumParameter.
	 *
	 * @param label label.
	 * @param options list of option identifiers (or keys).
	 * @param labels list of option labels (optional, can be {@code null}).
	 * @param defaultValue name of the default enumeration option.
	 */
	public EnumParameter(String label, List<String> options, List<String> labels, String defaultValue) {
		super(label, String.class, defaultValue);

		this.options = options;
		this.labels = (labels == null) ? options : labels;
	}

	private static <E extends Enum<E>> List<String> getEnumNames(Class<E> enumeration) {
		final List<String> items = new ArrayList<>();
		for (E e : enumeration.getEnumConstants()) {
			items.add(e.name());
		}
		return items;
	}

	private static <E extends Enum<E>> List<String> getEnumLabels(Class<E> enumeration) {
		final List<String> items = new ArrayList<>();
		for (E e : enumeration.getEnumConstants()) {
			items.add(e.toString());
		}
		return items;
	}

	private static <E extends Enum<E>> List<String> getEnumLabels(Class<E> enumeration, EnumStringMapper<E> mapper) {
		return EnumStringMapper.map(enumeration, mapper);
	}

	/**
	 * Manually adds all options from an Enumeration (to the back).
	 *
	 * @param <E> enumeration class.
	 * @param e an enumeration whose options should all be added.
	 */
	public <E extends Enum<E>> void addOptions(Class<E> e) {
		final List<String> names = getEnumNames(e);
		final List<String> labels = getEnumLabels(e);

		for (int i = 0; i < names.size(); i++) {
			addOption(
					names.get(i),
					labels.get(i),
					-1
			);
		}
	}

	/**
	 * Manually adds an additional option (in front). This can be used to add
	 * (general) options (e.g. `none`) that are not defined on the enum used to
	 * feed this parameter. Just take care to handle these options before trying
	 * to get back to an Enum item.
	 *
	 * @param option identifier (or key) of the option
	 * @param label label of the option
	 */
	public void addOption(String option, String label) {
		EnumParameter.this.addOption(option, label, 0);
	}

	/**
	 * Manually adds an additional option. This can be used to add (general)
	 * options (e.g. `none`) that are not defined on the enum used to feed this
	 * parameter. Just take care to handle these options before trying to get
	 * back to an Enum item.
	 *
	 * @param option identifier (or key) of the option
	 * @param label label of the option
	 * @param index insertion position of the new option. Items currently at
	 * this position, or following, simply will be shifted back one position. If
	 * negative the item will be appended to the list.
	 */
	public void addOption(String option, String label, int index) {
		if (index < 0) {
			index = this.options.size();
		}
		this.options.add(index, option);
		this.labels.add(index, label);
	}

	/**
	 * Returns the index of an option.
	 *
	 * @param option name of the option.
	 * @return index of the option.
	 */
	public int getIndex(String option) {
		return this.options.indexOf(option);
	}

	/**
	 * Returns the current value as en enumeration item.
	 *
	 * @param <E> type of the enumeration.
	 * @param clazz class of the enumeration.
	 * @return the current value.
	 */
	public <E extends Enum<E>> E getEnumValue(Class<E> clazz) {
		return valueOf(get(), clazz, clazz.getEnumConstants()[0]);
	}

	@Override
	protected EnumView newViewInstance() {
		return new EnumView(this);
	}

	protected final List<ViewHook<ComboBox<String>>> comboBoxViewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the combo box. This method is only called
	 * if the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a combo box.
	 */
	public void addComboBoxViewHook(ViewHook<ComboBox<String>> hook) {
		this.comboBoxViewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeComboBoxViewHook(ViewHook<ComboBox<String>> hook) {
		this.comboBoxViewHooks.remove(hook);
	}

	protected ViewHook<ComboBox<String>> singleRowViewHook = null;

	@Override
	public void initSingleRowView() {
		singleRowViewHook = (c) -> {
			c.getStyleClass().add("dip-small");
		};
	}

	/**
	 * Enum view with a ComboBox.
	 */
	public static class EnumView extends PersistentParameterBase.ParameterViewBase<EnumParameter, String, ComboBox<String>> {

		/**
		 * Creates a new enum view.
		 *
		 * @param parameter the enum parameter.
		 */
		public EnumView(EnumParameter parameter) {
			super(parameter, new ComboBox<>());

			root.setMaxWidth(Double.MAX_VALUE);
			root.getItems().addAll(parameter.labels);
			set(parameter.get());
			PersistentParameter.applyViewHooks(
					root,
					parameter.comboBoxViewHooks,
					parameter.singleRowViewHook
			);
			root.valueProperty().addListener((obs) -> {
				parameter.setLocal(get());
			});

			// this fixes some weird layout bug, where the combobox get's sized
			// wrong/too small initially, and snaps to the correct size once hit
			// by the mouse...
			root.layout();
		}

		@Override
		public final String get() {
			final int index = root.getSelectionModel().getSelectedIndex();
			return parameter.options.get(index);
		}

		@Override
		public final void set(String value) {
			final int index = parameter.options.indexOf(value);
			root.getSelectionModel().select(index);
		}

	}

	/**
	 * Safely returns the Enum item associated to the given name, or a default
	 * one if not defined.
	 *
	 * @param <E> class of the enumeration.
	 * @param name name of the element in the enumeration.
	 * @param e class of the enumeration.
	 * @param defaultValue default enumeration element returned in case no
	 * element in the enumeration is associated with the given name.
	 * @return an element in the enumeration.
	 */
	public static <E extends Enum<E>> E valueOf(String name, Class<E> e, E defaultValue) {
		try {
			return Enum.valueOf(e, name);
		} catch (IllegalArgumentException ex) {
			return defaultValue;
		}
	}

}
