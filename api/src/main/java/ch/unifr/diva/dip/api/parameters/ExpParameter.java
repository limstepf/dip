package ch.unifr.diva.dip.api.parameters;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Mathematical expression parameter. While this is a StringParameter, the
 * String is supposed to be evaluated and return a double.
 *
 * This is backed by exp4j which comes with a standard set of built-in
 * functions, operators, and some defined numerical constants.
 *
 * Opretators:
 * <ul>
 * <li>+ (precedence addition and unary plus)</li>
 * <li>- (precendece substraction and unary minus)</li>
 * <li>* (multiplication)</li>
 * <li>/ (division)</li>
 * <li>^ (power)</li>
 * <li>% (modulo)</li>
 * </ul>
 *
 * Functions:
 * <ul>
 * <li>sin</li>
 * <li>cos</li>
 * <li>tan</li>
 * <li>log</li>
 * <li>log2</li>
 * <li>log10</li>
 * <li>log1p</li>
 * <li>abs</li>
 * <li>acos</li>
 * <li>asin</li>
 * <li>atan</li>
 * <li>cbrt</li>
 * <li>floor</li>
 * <li>sinh</li>
 * <li>sqrt</li>
 * <li>tanh</li>
 * <li>cosh</li>
 * <li>ceil</li>
 * <li>pow</li>
 * <li>exp</li>
 * <li>expm1</li>
 * <li>signum</li>
 * </ul>
 *
 * Numerical constants:
 * <ul>
 * <li>pi (or π)</li>
 * <li>e (Euler's number)</li>
 * <li>φ (the golden ratio 1.618...)</li>
 * </ul>
 *
 * @see
 * <a href="http://www.objecthunter.net/exp4j/">http://www.objecthunter.net/exp4j/</a>
 */
public class ExpParameter extends PersistentParameterBase<String, ExpParameter.ExpView> implements SingleRowParameter<String> {

	// TODO: this could be extended to support expressions with variables, e.g.
	// a function that gets x and y pixel coordinates, or another that takes
	// r, g, b, or what not...  -> adjust tooltip (validation only), and
	// processors would just retrieve the expression instead of a double.
	// see: http://www.objecthunter.net/exp4j/#Evaluating_an_expression
	private Double expressionValue;
	private String tooltipFormat;
	private DoubleValidator validator;

	/**
	 * Creates an expression parameter.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 */
	public ExpParameter(String label, String defaultValue) {
		super(label, String.class, defaultValue);
	}

	@Override
	protected ExpView newViewInstance() {
		return new ExpView(this);
	}

	/**
	 * Evaluates the value of the expression.
	 *
	 * @return the value of the expression, or {@code Float.NaN}.
	 */
	public float getFloat() {
		return (float) getDouble();
	}

	/**
	 * Evaluates the value of the expression.
	 *
	 * @return the value of the expression, or {@code Double.NaN}.
	 */
	public double getDouble() {
		if (this.expressionValue == null) {
			evalDouble(this.get());
		}

		return this.expressionValue;
	}

	protected boolean evalDouble(String expression) {
		try {
			Expression exp = new ExpressionBuilder(expression.toLowerCase()).build();
			if (exp.validate().isValid()) {
				if (validator != null) {
					expressionValue = validator.validate(exp.evaluate());
					if (expressionValue == null || Double.isNaN(expressionValue)) {
						expressionValue = Double.NaN;
						return false;
					}
				} else {
					expressionValue = exp.evaluate();
				}
				return true;
			} else {
				expressionValue = Double.NaN;
				return false;
			}
		} catch (Exception ex) {
			expressionValue = Double.NaN;
			return false;
		}
	}

	/**
	 * Sets a double validator. May reject input values as invalid.
	 *
	 * @param validator a double validator.
	 */
	public void setDoubleValidator(DoubleValidator validator) {
		this.validator = validator;
	}

	/**
	 * A double validator.
	 */
	public interface DoubleValidator {

		/**
		 * Validates a double. E.g. by imposing min and max values.
		 *
		 * @param value the value to be validated.
		 * @return the validated value, or {@code null} to reject the value
		 * (i.e. the current value will be marked as invalid).
		 */
		public Double validate(double value);

	}

	/**
	 * Sets a custom {@code Tooltip} format.
	 *
	 * @param format a {@code String} format that gets passed one double; the
	 * value of the current expression.
	 */
	public void setTooltipFormat(String format) {
		this.tooltipFormat = format;

		if (this.view != null) {
			this.view.updateTooltip();
		}
	}

	protected final List<ViewHook<TextField>> viewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the textfield. This method is only called
	 * if the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a label.
	 */
	public void addTextFieldViewHook(ViewHook<TextField> hook) {
		this.viewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeTextFieldViewHook(ViewHook<TextField> hook) {
		this.viewHooks.remove(hook);
	}

	protected ViewHook<TextField> singleRowViewHook = null;

	@Override
	public void initSingleRowView() {
		singleRowViewHook = (t) -> {
			t.getStyleClass().add("dip-small");
		};
	}

	/**
	 * Expression view.
	 */
	public static class ExpView extends PersistentParameterBase.ParameterViewBase<ExpParameter, String, TextField> {

		protected final Tooltip tooltip = new Tooltip();

		/**
		 * Creates a new expression view.
		 *
		 * @param parameter the expression parameter.
		 */
		public ExpView(ExpParameter parameter) {
			super(parameter, new TextField());
			set(parameter.get());

			root.getStyleClass().add("dip-text-input");
			root.setTooltip(tooltip);
			PersistentParameter.applyViewHooks(
					root,
					parameter.viewHooks,
					parameter.singleRowViewHook
			);
			root.textProperty().addListener((obs) -> {
				final String expression = get();

				if (!parameter.evalDouble(expression)) {
					root.pseudoClassStateChanged(PersistentParameter.ALERT, true);
				} else {
					root.pseudoClassStateChanged(PersistentParameter.ALERT, false);
				}

				updateTooltip();
				parameter.setLocal(expression);
			});

			updateTooltip();
		}

		/**
		 * Updates the tooltip displaying the expression's value.
		 */
		public final void updateTooltip() {
			final String msg;
			if (parameter.tooltipFormat != null) {
				msg = String.format(parameter.tooltipFormat, parameter.getDouble());
			} else {
				msg = Double.toString(parameter.getDouble());
			}
			tooltip.setText(msg);
		}

		@Override
		public final String get() {
			return root.getText();
		}

		@Override
		public final void set(String value) {
			root.setText(value);
		}

	}

}
