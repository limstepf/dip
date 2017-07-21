package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.ui.PersistentToggleButtonGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

/**
 * A Boolean parameter.
 */
public class BooleanParameter extends PersistentParameterBase<Boolean, BooleanParameter.BooleanView> implements SingleRowParameter<Boolean> {

	protected String trueLabel;
	protected String falseLabel;

	/**
	 * Creates a boolean parameter. Uses "true" and "false" as option labels.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 */
	public BooleanParameter(String label, boolean defaultValue) {
		this(label, defaultValue, "true", "false");
	}

	/**
	 * Creates a boolean parameter.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 * @param trueLabel label of the true (or on) option.
	 * @param falseLabel label of the false (or off) option.
	 */
	public BooleanParameter(String label, boolean defaultValue, String trueLabel, String falseLabel) {
		super(label, Boolean.class, defaultValue);

		this.trueLabel = trueLabel;
		this.falseLabel = falseLabel;
	}

	/**
	 * Sets the true option label.
	 *
	 * @param label label of the true (or on) option.
	 */
	public void setTrueLabel(String label) {
		this.trueLabel = label;
	}

	/**
	 * Sets the false option label.
	 *
	 * @param label label of the false (or off) option.
	 */
	public void setFalseLabel(String label) {
		this.falseLabel = label;
	}

	@Override
	protected BooleanView newViewInstance() {
		return new BooleanView(this);
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
	 * Boolean view with a ToggleGroup of two ToggleButtons.
	 */
	public static class BooleanView extends PersistentParameterBase.ParameterViewBase<BooleanParameter, Boolean, HBox> {

		private final ToggleGroup group = new PersistentToggleButtonGroup();
		private final ToggleButton on;
		private final ToggleButton off;

		/**
		 * Creates a new boolean view.
		 *
		 * @param parameter the boolean parameter.
		 */
		public BooleanView(BooleanParameter parameter) {
			super(parameter, new HBox());

			on = new ToggleButton(parameter.trueLabel);
			off = new ToggleButton(parameter.falseLabel);

			PersistentParameter.applyViewHooks(
					Arrays.asList(on, off),
					parameter.toggleButtonViewHooks,
					parameter.singleRowViewHook
			);

			on.setToggleGroup(group);
			off.setToggleGroup(group);
			root.setAlignment(Pos.CENTER);
			root.getChildren().addAll(off, on);

			set(parameter.get());

			group.selectedToggleProperty().addListener((obs) -> {
				parameter.setLocal(get());
			});
		}

		@Override
		public final Boolean get() {
			if (group.getSelectedToggle() == null) {
				return Boolean.FALSE;
			}
			return group.getSelectedToggle().equals(on);
		}

		@Override
		public final void set(Boolean value) {
			group.selectToggle(value ? on : off);
		}
	}

}
