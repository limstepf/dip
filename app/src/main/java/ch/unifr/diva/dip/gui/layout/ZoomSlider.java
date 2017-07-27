package ch.unifr.diva.dip.gui.layout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Priority;

/**
 * Zoom slider. Adjusts the zoom of a {@code Zoomable}.
 */
public class ZoomSlider {

	protected final Zoomable zoomable;
	protected final Lane lane;
	protected final TextField text;
	protected final Slider slider;
	protected final EventHandler<ScrollEvent> scrollHandler;
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

		this.scrollHandler = (e) -> {
			if (e.getDeltaY() > 0) {
				increment();
			} else if (e.getDeltaY() < 0) {
				decrement();
			}
		};

		this.text = new TextField();
		text.setPrefWidth(62.5);
		text.setAlignment(Pos.CENTER_RIGHT);
		text.addEventHandler(ScrollEvent.SCROLL, scrollHandler);
		text.addEventHandler(KeyEvent.KEY_PRESSED, (e) -> {
			final KeyCode k = e.getCode();
			if (k == KeyCode.ENTER) {
				zoomable.setZoom(getZoomValueFromTextField());
				e.consume();
			} else if (k == KeyCode.UP) {
				increment();
				e.consume();
			} else if (k == KeyCode.DOWN) {
				decrement();
				e.consume();
			}
		});

		this.sliderListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			if (!changeIsLocal) {
				this.zoomable.setZoom(sliderToZoomVal(newValue.doubleValue()));
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
		slider.addEventHandler(ScrollEvent.SCROLL, scrollHandler);
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
	 * Increase the slider value by a bit.
	 */
	protected final void increment() {
		nextVal(1);
	}

	/**
	 * Decrease the slider value a bit.
	 */
	protected final void decrement() {
		nextVal(-1);
	}

	protected final void nextVal(int sign) {
		final double zoom = sliderToZoomVal(slider.getValue());
		final double inc;
		if (zoom >= 2.5) {
			inc = 0.5;
		} else {
			inc = 0.05;
		}
		final double trunc = new BigDecimal(zoom).setScale(2, RoundingMode.HALF_UP).doubleValue();
		setZoomValue(trunc + inc * sign);
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
		try {
			return 0.01 * Double.parseDouble(text.getText().replace("%", ""));
		} catch (Exception ex) {
			return 1.0;
		}
	}

	protected static double sliderToZoomVal(double value) {
		return Math.pow(10, value) - 1;
	}

	protected static double zoomValToSlider(double value) {
		return Math.log10(value + 1);
	}

}
