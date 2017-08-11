package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EmptyParameter;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import java.util.Arrays;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.VPos;
import javafx.scene.layout.ColumnConstraints;
import org.slf4j.LoggerFactory;

/**
 * Canny Edge parameters.
 */
public class CannyEdgeParameters {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(CannyEdgeParameters.class);
	protected final BooleanProperty disableProperty;

	protected final ExpParameter sigma;
	protected final XorParameter thresholds;
	protected final ExpParameter threshold_low;
	protected final ExpParameter threshold_high;
	protected CompositeGrid subGrid;

	/**
	 * Creates new Canny Edge parameters.
	 */
	public CannyEdgeParameters() {
		this.disableProperty = new SimpleBooleanProperty(false);

		final ExpParameter.DoubleValidator expValidator = (v) -> {
			if (v < 0 || v > 1.0) {
				return Double.NaN;
			}
			return v;
		};
		this.sigma = new ExpParameter("Sigma", "1.0");
		sigma.addTextFieldViewHook((t) -> {
			t.disableProperty().bind(disableProperty);
		});
		final TextParameter auto = new TextParameter("automatic");
		final TextParameter lowLabel = new TextParameter("Low: ");
		this.threshold_low = new ExpParameter("low", "0.3");
		threshold_low.addTextFieldViewHook((t) -> {
			t.setMaxWidth(64);
		});
		threshold_low.setDoubleValidator(expValidator);
		final TextParameter highLabel = new TextParameter("High: ");
		this.threshold_high = new ExpParameter("high", "0.7");
		threshold_high.addTextFieldViewHook((t) -> {
			t.setMaxWidth(64);
		});
		threshold_high.setDoubleValidator(expValidator);
		final EmptyParameter spacer = new EmptyParameter();
		spacer.setMinWidth(10);
		final CompositeGrid grid = new CompositeGrid(
				lowLabel,
				threshold_low,
				spacer,
				highLabel,
				threshold_high
		);
		this.thresholds = new XorParameter("Thresholds", Arrays.asList(auto, grid));
		thresholds.addVBoxViewHook((b) -> {
			b.disableProperty().bind(disableProperty);
		});
	}

	/**
	 * Puts/registers the parameters.
	 *
	 * @param parameters the processor's parameters.
	 */
	public void put(Map<String, Parameter<?>> parameters) {
		parameters.put("canny-sigma", sigma);
		parameters.put("canny-thresholds", thresholds);
	}

	/**
	 * Puts/registers the parameters (as sub-group).
	 *
	 * @param parameters the processor's parameters.
	 */
	public void putAsSub(Map<String, Parameter<?>> parameters) {
		if (subGrid == null) {
			subGrid = new CompositeGrid(
					"Canny",
					new LabelParameter("Sigma"),
					new LabelParameter("Thresholds"),
					sigma,
					thresholds
			);
			subGrid.setColumnConstraints(2);
			subGrid.setHorizontalSpacing(10);
			for (ColumnConstraints cc : subGrid.getColumnConstraints()) {
				cc.setPercentWidth(50.0);
			}
			subGrid.setRowConstraints(2);
			subGrid.getRowConstraints().get(1).setValignment(VPos.TOP);
		}
		parameters.put("canny-options", subGrid);
	}

	/**
	 * The disable property.
	 *
	 * @return the disable property.
	 */
	public BooleanProperty disableProperty() {
		return disableProperty;
	}

	/**
	 * Enables the parameters. Only affects the view.
	 *
	 * @param enable {@code true} to enable, {@code false} to disable.
	 */
	public void enableParameters(boolean enable) {
		disableProperty.set(!enable);
	}

	/**
	 * Returns sigma.
	 *
	 * @return sigma.
	 */
	public float getSigma() {
		return sigma.getFloat();
	}

	/**
	 * Checks whether thresholds should be computed automatically.
	 *
	 * @return {@code true} if thresholds should be computed automatically,
	 * {@code false} to specify them manually.
	 */
	public boolean isAutoThresholds() {
		return thresholds.get().selection == 0;
	}

	/**
	 * Returns the low threshold.
	 *
	 * @return the low threshold.
	 */
	public float getThresholdLow() {
		return threshold_low.getFloat();
	}

	/**
	 * Returns the high threshold.
	 *
	 * @return the high threshold.
	 */
	public float getThresholdHigh() {
		return threshold_high.getFloat();
	}

	/**
	 * Returns a canny edge detector as specified by the parameters.
	 *
	 * @return a canny edge detector.
	 */
	public org.openimaj.image.processing.edges.CannyEdgeDetector getCannyEdgeDetector() {
		float canny_sigma = getSigma();
		if (!Float.isFinite(canny_sigma) || canny_sigma < 0) {
			log.warn("invalid sigma: {}. Sigma is reset to 1.0f.", canny_sigma);
			canny_sigma = 1.0f;
		}

		boolean canny_auto = isAutoThresholds();
		float canny_low = 0;
		float canny_high = 0;
		if (!canny_auto) {
			canny_low = getThresholdLow();
			canny_high = getThresholdHigh();
			if (canny_low < 0.0f || canny_low >= canny_high || canny_high > 1.0f) {
				log.warn(
						"invalid thresholds: low={}, hight={} must be in range [0, 1], and low < high."
						+ " Chosing thresholds automaticlly.",
						canny_low,
						canny_high
				);
				canny_auto = true;
			}
		}
		if (canny_auto) {
			return new org.openimaj.image.processing.edges.CannyEdgeDetector(
					canny_sigma
			);
		}
		return new org.openimaj.image.processing.edges.CannyEdgeDetector(
				canny_low,
				canny_high,
				canny_sigma
		);
	}

}
