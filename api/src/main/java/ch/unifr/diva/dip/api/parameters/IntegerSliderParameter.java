package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

/**
 * Integer slider parameter.
 */
public class IntegerSliderParameter extends IntegerParameter {

	private Parameter prefixParameter;
	private Parameter postfixParameter;

	/**
	 * Creates an integer slider parameter (without label). Use a view hook to
	 * further customize the slider (like setting ticks, marks, and what not).
	 *
	 * @param defaultValue default value.
	 * @param minValue minimum value (inclusive).
	 * @param maxValue maximum value (inclusive).
	 */
	public IntegerSliderParameter(int defaultValue, int minValue, int maxValue) {
		this("", defaultValue, minValue, maxValue);
	}

	/**
	 * Creates an integer slider parameter. Use a view hook to further customize
	 * the slider (like setting ticks, marks, and what not).
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 * @param minValue minimum value (inclusive).
	 * @param maxValue maximum value (inclusive).
	 */
	public IntegerSliderParameter(String label, int defaultValue, int minValue, int maxValue) {
		super(label, defaultValue, minValue, maxValue);
	}

	/**
	 * Prepends a (transient) parameter to the slider once the view is
	 * initialized. This is mostly intended for {@code TextParameters}.
	 *
	 * @param parameter a (transient) parameter. Does not get saved.
	 */
	public void setPrefix(Parameter parameter) {
		this.prefixParameter = parameter;
	}

	/**
	 * Appends a (transient) parameter to the slider once the view is
	 * initialized. This is mostly intended for {@code TextParameters}.
	 *
	 * @param parameter a (transient) parameter. Does not get saved.
	 */
	public void setPostfix(Parameter parameter) {
		this.postfixParameter = parameter;
	}

	@Override
	protected PersistentParameter.View newViewInstance() {
		return new IntegerSliderView(this);
	}

	private final List<ViewHook<Slider>> viewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the slider. This method is only called if
	 * the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a slider.
	 */
	public void addViewHook(ViewHook<Slider> hook) {
		this.viewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeViewHook(ViewHook<Slider> hook) {
		this.viewHooks.remove(hook);
	}

	/**
	 * Integer view with a Slider.
	 */
	public static class IntegerSliderView extends ParameterViewBase<IntegerSliderParameter, Integer, BorderPane> {

		private final Slider slider = new Slider();
		private final Tooltip tooltip = new Tooltip();

		public IntegerSliderView(IntegerSliderParameter parameter) {
			super(parameter, new BorderPane());
			set(parameter.get());

			slider.setMin(parameter.minValue);
			slider.setMax(parameter.maxValue);
			slider.setMajorTickUnit(1);
			slider.setMinorTickCount(0);
			slider.setShowTickLabels(true);
			slider.setShowTickMarks(true);
			slider.setSnapToTicks(true);
			slider.setBlockIncrement(1);
			slider.setTooltip(tooltip);
			tooltip.setText(String.format("%d", parameter.get()));

			root.setCenter(slider);
			if (parameter.prefixParameter != null) {
				final Node node = parameter.prefixParameter.view().node();
				BorderPane.setMargin(node, new Insets(0, 10, 0, 0));
				root.setLeft(node);
			}
			if (parameter.postfixParameter != null) {
				final Node node = parameter.postfixParameter.view().node();
				BorderPane.setMargin(node, new Insets(0, 0, 0, 10));
				root.setRight(node);
			}

			PersistentParameter.applyViewHooks(slider, parameter.viewHooks);

			slider.valueProperty().addListener((obs) -> {
				final int v = get();
				tooltip.setText(String.format("%d", v));
				parameter.valueProperty.set(v);
			});
		}

		protected final Integer get() {
			return (int) slider.getValue();
		}

		@Override
		public final void set(Integer value) {
			slider.setValue(value);
		}
	}

}
