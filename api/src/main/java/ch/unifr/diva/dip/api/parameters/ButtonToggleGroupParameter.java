package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.ui.PersistentToggleButtonGroup;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

/**
 * A button toggle group parameter.
 *
 * @param <T> class of the key (typically a {@code String}).
 */
public class ButtonToggleGroupParameter<T> extends PersistentParameterBase<T, ButtonToggleGroupParameter.ButtonToggleGroupView<T>> implements SingleRowParameter<T> {

	protected final List<ButtonToggleData<T>> toggles;

	/**
	 * Creates a new button toggle group parameter.
	 *
	 * @param label the label.
	 * @param valueClass the parameter's value class {@code T}.
	 * @param defaultValue the default value/key.
	 */
	public ButtonToggleGroupParameter(String label, Class<T> valueClass, T defaultValue) {
		super(label, valueClass, defaultValue);

		this.toggles = new ArrayList<>();
	}

	/**
	 * Returns a new button toggle group parameter with {@code String} values.
	 *
	 * @param label the label.
	 * @param defaultValue the default value/key.
	 * @return a new button toggle group parameter.
	 */
	public static ButtonToggleGroupParameter<String> newInstance(String label, String defaultValue) {
		return new ButtonToggleGroupParameter<>(label, String.class, defaultValue);
	}

	/**
	 * Returns a new button toggle group parameter with {@code Integer} values.
	 *
	 * @param label the label.
	 * @param defaultValue the default value/key.
	 * @return a new button toggle group parameter.
	 */
	public static ButtonToggleGroupParameter<Integer> newInstance(String label, Integer defaultValue) {
		return new ButtonToggleGroupParameter<>(label, Integer.class, defaultValue);
	}

	@Override
	protected ButtonToggleGroupView<T> newViewInstance() {
		return new ButtonToggleGroupView<>(this);
	}

	/**
	 * Adds a button to the button toggle group.
	 *
	 * @param label the label of the button.
	 * @param key the key of the button.
	 */
	public void add(String label, T key) {
		this.toggles.add(new ButtonToggleData<>(label, key));
	}

	protected final List<ViewHook<ToggleButton>> toggleButtonViewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize all toggle buttons.
	 *
	 * @param hook the view hook.
	 */
	public void addToggleButtonViewHook(ViewHook<ToggleButton> hook) {
		this.toggleButtonViewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook the view hook.
	 */
	public void removeToggleButtonViewHook(ViewHook<ToggleButton> hook) {
		this.toggleButtonViewHooks.remove(hook);
	}

	protected ViewHook<ToggleButton> singleRowViewHook = null;

	@Override
	public void initSingleRowView() {
		singleRowViewHook = (b) -> {
			b.getStyleClass().add("dip-small");
		};
	}

	/**
	 * Button toggle group view.
	 *
	 * @param <T> the type of the value.
	 */
	public static class ButtonToggleGroupView<T> extends PersistentParameterBase.ParameterViewBase<ButtonToggleGroupParameter<T>, T, HBox> {

		private final ToggleGroup group;
		private final List<ToggleButton> toggles;

		/**
		 * Creates a new button toggle group view.
		 *
		 * @param parameter the button toggle group parameter.
		 */
		public ButtonToggleGroupView(ButtonToggleGroupParameter<T> parameter) {
			super(parameter, new HBox());
			this.group = new PersistentToggleButtonGroup();
			this.toggles = new ArrayList<>();
			for (ButtonToggleData<T> b : parameter.toggles) {
				toggles.add(b.getToggleButton(group));
			}
			PersistentParameter.applyViewHooks(
					toggles,
					parameter.toggleButtonViewHooks,
					parameter.singleRowViewHook
			);
			root.setAlignment(Pos.CENTER_LEFT);
			root.getChildren().addAll(toggles);

			set(parameter.get());

			group.selectedToggleProperty().addListener((obs) -> {
				parameter.setLocal(get());
			});
		}

		@Override
		public final void set(T value) {
			for (int i = 0; i < parameter.toggles.size(); i++) {
				final ButtonToggleData<T> data = parameter.toggles.get(i);
				if (data.key.equals(value)) {
					final ToggleButton toggle = toggles.get(i);
					group.selectToggle(toggle);
				}
			}
		}

		@Override
		public final T get() {
			final Toggle toggle = group.getSelectedToggle();
			final int idx = toggles.indexOf(toggle);
			if (idx >= 0) {
				final ButtonToggleData<T> data = parameter.toggles.get(idx);
				if (data != null) {
					return data.key;
				}
			}
			return null;
		}

	}

	/**
	 * Button toggle data.
	 *
	 * @param <T> the type of the value.
	 */
	public static class ButtonToggleData<T> {

		protected final String label;
		protected final T key;

		/**
		 * Creates new button toggle data.
		 *
		 * @param label the label of the button.
		 * @param key the key of the button.
		 */
		public ButtonToggleData(String label, T key) {
			this.label = label;
			this.key = key;
		}

		protected ToggleButton getToggleButton(ToggleGroup group) {
			final ToggleButton button = new ToggleButton(label);
			button.setToggleGroup(group);
			return button;
		}

	}

}
