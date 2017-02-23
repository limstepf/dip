package ch.unifr.diva.dip.gui.layout;

import ch.unifr.diva.dip.api.ui.KeyEventHandler;
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
 * Zoom slider. Adjusts the zoom of a {@code Zoomable}.
 */
public class ZoomSlider {

	protected final Zoomable zoomable;

	protected final Lane lane;
	protected final TextField text;
	protected final Slider slider;

	protected final EventHandler<KeyEvent> zoomHandler;
	protected final ChangeListener<Number> sliderListener;
	protected final ChangeListener<Number> zoomListener;

	protected boolean changeIsLocal;

	/**
	 * Creates a new zoom slider.
	 *
	 * @param zoomable the {@code Zoomable} to control.
	 */
	public ZoomSlider(Zoomable zoomable) {
		this.zoomable = zoomable;

		this.zoomHandler = new KeyEventHandler(
				KeyCode.ENTER,
				(e) -> {
					zoomable.setZoom(getZoomValueFromTextField());
					return true;
				}
		);
		this.text = new TextField();
		text.setPrefWidth(62.5);
		text.setAlignment(Pos.CENTER_RIGHT);
		text.setOnKeyPressed(zoomHandler);

		this.sliderListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			if (!changeIsLocal) {
				zoomable.setZoom(sliderToZoomVal(newValue.doubleValue()));
			}
		};
		this.slider = new Slider();
		final double one = zoomValToSlider(1);
		slider.setMin(zoomValToSlider(zoomable.getZoomMin() * 0.01));
		slider.setMax(zoomValToSlider(zoomable.getZoomMax() * 0.01));
		slider.setValue(one);
		slider.setBlockIncrement(one * 0.05);
		slider.setMinorTickCount(4);
		slider.setMajorTickUnit(one);
		slider.valueProperty().addListener(sliderListener);
		sliderListener.changed(null, 0, zoomValToSlider(zoomable.getZoom()));

		this.zoomListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			setZoomValue(newValue.doubleValue());
		};
		zoomable.zoomProperty().addListener(zoomListener);
		zoomListener.changed(null, 0, zoomable.getZoom());

		this.lane = new Lane();
		lane.add(text);
		lane.add(slider, Priority.ALWAYS);
	}

	/**
	 * Returns the {@code TextField} of the zoom slider composite. This guy
	 * holds the zoom value in percent ({@code %} sign included).
	 *
	 * @return the {@code TextField}.
	 */
	public TextField getTextField() {
		return text;
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
		text.setText(String.format("%.1f%%", value * 100));
	}

	/**
	 * Updates the zoom value. This is *not* propagated to the controlled
	 * {@code ZoomPane}, but just updates the view (textfield and the slider).
	 *
	 * @param value the new zoom value.
	 */
	final public void updateZoomValue(double value) {
		changeIsLocal = true;
		slider.valueProperty().setValue(zoomValToSlider(value));
		text.setText(String.format("%.1f%%", value * 100));
		changeIsLocal = false;
	}

	protected final double getZoomValueFromTextField() {
		return 0.01 * Double.parseDouble(text.getText().replace("%", ""));
	}

	protected static double sliderToZoomVal(double value) {
		return Math.pow(10, value) - 1;
	}

	protected static double zoomValToSlider(double value) {
		return Math.log10(value + 1);
	}

}
