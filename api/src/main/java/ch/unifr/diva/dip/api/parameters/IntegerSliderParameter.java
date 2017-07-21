package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;

/**
 * Integer slider parameter.
 */
public class IntegerSliderParameter extends PersistentParameterBase<Integer, IntegerSliderParameter.IntegerSliderView> implements SingleRowParameter<Integer> {

	protected final int minValue;
	protected final int maxValue;
	protected Parameter<?> prefixParameter;
	protected Parameter<?> postfixParameter;

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
		super(label, Integer.class, defaultValue);

		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * Prepends a (transient) parameter to the slider once the view is
	 * initialized. This is mostly intended for {@code TextParameters}.
	 *
	 * @param parameter a (transient) parameter. Does not get saved.
	 */
	public void setPrefix(Parameter<?> parameter) {
		this.prefixParameter = parameter;
	}

	/**
	 * Appends a (transient) parameter to the slider once the view is
	 * initialized. This is mostly intended for {@code TextParameters}.
	 *
	 * @param parameter a (transient) parameter. Does not get saved.
	 */
	public void setPostfix(Parameter<?> parameter) {
		this.postfixParameter = parameter;
	}

	@Override
	protected IntegerSliderView newViewInstance() {
		return new IntegerSliderView(this);
	}

	protected final List<ViewHook<Slider>> sliderViewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the slider. This method is only called if
	 * the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a slider.
	 */
	public void addSliderViewHook(ViewHook<Slider> hook) {
		this.sliderViewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeSliderViewHook(ViewHook<Slider> hook) {
		this.sliderViewHooks.remove(hook);
	}

	protected ViewHook<Slider> singleRowHook = null;

	@Override
	public void initSingleRowView() {
		singleRowHook = (s) -> {
			s.getStyleClass().add("dip-small");
			s.setShowTickLabels(false);
			s.setShowTickMarks(true);
		};
	}

	/**
	 * Integer view with a Slider.
	 */
	public static class IntegerSliderView extends PersistentParameterBase.ParameterViewBase<IntegerSliderParameter, Integer, BorderPane> {

		private final Slider slider = new Slider();
		private final Tooltip tooltip = new Tooltip();

		/**
		 * Creates a new integer slider view.
		 *
		 * @param parameter the integer slider parameter.
		 */
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

			PersistentParameter.applyViewHooks(
					slider,
					parameter.sliderViewHooks,
					parameter.singleRowHook
			);

			slider.valueProperty().addListener((obs) -> {
				final int v = get();
				tooltip.setText(String.format("%d", v));
				parameter.setLocal(v);
			});

			slider.addEventHandler(KeyEvent.KEY_PRESSED, (e) -> {
				final KeyCode k = e.getCode();
				if (k == KeyCode.UP || k == KeyCode.RIGHT) {
					increment();
					e.consume();
				} else if (k == KeyCode.DOWN || k == KeyCode.LEFT) {
					decrement();
					e.consume();
				}
			});
			slider.addEventHandler(ScrollEvent.SCROLL, (e) -> {
				if (e.getDeltaY() > 0) {
					increment();
				} else if (e.getDeltaY() < 0) {
					decrement();
				}
			});
		}

		protected final void increment() {
			parameter.set(parameter.get() + 1);
		}

		protected final void decrement() {
			parameter.set(parameter.get() - 1);
		}

		@Override
		public final Integer get() {
			return (int) slider.getValue();
		}

		@Override
		public final void set(Integer value) {
			slider.setValue(value);
		}

	}

}
