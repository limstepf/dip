package ch.unifr.diva.dip.gui.layout;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;

/**
 * Zoom slider. Adjusts the zoom of a {@code ZoomPane}.
 */
public class ZoomSlider {

	private final ZoomPane zoomPane;
	private final Lane lane = new Lane();
	private final TextField zoom = new TextField();
	private final Slider slider = new Slider();

	private final EventHandler<KeyEvent> zoomHandler;
	private final ChangeListener<Number> sliderListener;
	private boolean propagate = true;

	/**
	 * Creates a new zoom slider.
	 *
	 * @param zoomPane the zoom pane to be controlled.
	 */
	public ZoomSlider(ZoomPane zoomPane) {
		this.zoomPane = zoomPane;

		zoom.setPrefWidth(62.5);
		zoom.setAlignment(Pos.CENTER_RIGHT);
		zoomHandler = (e) -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				zoomPane.setZoom(getZoomValueFromTextField());
			}
		};
		zoom.setOnKeyPressed(zoomHandler);

		final double one = zoomValToSlider(1);
		slider.setMin(zoomValToSlider(zoomPane.minZoomValue() * 0.01));
		slider.setMax(zoomValToSlider(zoomPane.maxZoomValue() * 0.01));
		slider.setValue(one);
		slider.setBlockIncrement(one * 0.05);
		slider.setMinorTickCount(4);
		slider.setMajorTickUnit(one);
		sliderListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			if (!propagate) {
				return;
			}
			zoomPane.setZoom(sliderToZoomVal(newValue.doubleValue()));
		};
		slider.valueProperty().addListener(sliderListener);
		sliderListener.changed(null, 0, zoomValToSlider(zoomPane.getZoom()));

		lane.add(zoom);
		lane.add(slider, Priority.ALWAYS);
	}

	/**
	 * Returns the {@code TextField} of the zoom slider composite. This guy
	 * holds the zoom value in percent ({@code %} sign included).
	 *
	 * @return the {@code TextField}.
	 */
	public TextField getTextField() {
		return zoom;
	}

	/**
	 * Returns the {@code Slider} of the zoom slider composite.
	 *
	 * @return the {@code Slider}.
	 */
	public Slider getSlider() {
		return slider;
	}

	/**
	 * Returns the zoom slider composite (or parent node).
	 *
	 * @return the zoom slider composite (or parent node).
	 */
	public Node getNode() {
		return lane;
	}

	/**
	 * Sets the zoom value. This is propagated to the controlled
	 * {@code ZoomPane}.
	 *
	 * @param value the new zoom value.
	 */
	final public void setZoomValue(double value) {
		slider.valueProperty().setValue(zoomValToSlider(value));
		zoom.setText(String.format("%.1f%%", value * 100));
	}

	/**
	 * Updates the zoom value. This is *not* propagated to the controlled
	 * {@code ZoomPane}, but just updates the view (textfield and the slider).
	 *
	 * @param value the new zoom value.
	 */
	final public void updateZoomValue(double value) {
		propagate = false;
		slider.valueProperty().setValue(zoomValToSlider(value));
		zoom.setText(String.format("%.1f%%", value * 100));
		propagate = true;
	}

	private double getZoomValueFromTextField() {
		return 0.01 * Double.parseDouble(zoom.getText().replace("%", ""));
	}

	private static double sliderToZoomVal(double value) {
		return Math.pow(10, value) - 1;
	}

	private static double zoomValToSlider(double value) {
		return Math.log10(value + 1);
	}

}
