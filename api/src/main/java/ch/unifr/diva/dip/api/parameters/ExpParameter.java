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
 * @see net.objecthunter.exp4j
 */
public class ExpParameter extends StringParameter {

	// TODO: this could be extended to support expressions with variables, e.g.
	// a function that gets x and y pixel coordinates, or another that takes
	// r, g, b, or what not...  -> adjust tooltip (validation only), and
	// processors would just retrieve the expression instead of a double.
	// see: http://www.objecthunter.net/exp4j/#Evaluating_an_expression
	
	/**
	 * Creates an expression parameter.
	 *
	 * @param label label.
	 * @param defaultValue default value.
	 */
	public ExpParameter(String label, String defaultValue) {
		super(label, defaultValue);
	}

	@Override
	protected PersistentParameter.View newViewInstance() {
		return new ExpView(this);
	}

	private Double expressionValue = null;

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
				this.expressionValue = exp.evaluate();
				return true;
			} else {
				this.expressionValue = Double.NaN;
				return false;
			}
		} catch (IllegalArgumentException ex) {
			// UnknownFunctionOrVariableException is a IllegalArgumentException
			this.expressionValue = Double.NaN;
			return false;
		}
	}

	private final List<ViewHook<TextField>> viewHooks = new ArrayList<>();

	/**
	 * Adds a view hook to customize the textfield. This method is only called
	 * if the view of the parameter is actually requested.
	 *
	 * @param hook hook method for a textfield.
	 */
	public void addViewHook(ViewHook<TextField> hook) {
		this.viewHooks.add(hook);
	}

	/**
	 * Removes a view hook.
	 *
	 * @param hook hook method to be removed.
	 */
	public void removeViewHook(ViewHook<TextField> hook) {
		this.viewHooks.remove(hook);
	}

	/**
	 * Expression view.
	 */
	public static class ExpView extends ParameterViewBase<ExpParameter, String, TextField> {

		protected final Tooltip tooltip = new Tooltip();

		public ExpView(ExpParameter parameter) {
			super(parameter, new TextField());
			set(parameter.get());

			root.getStyleClass().add("dip-text-input");
			root.setTooltip(tooltip);
			PersistentParameter.applyViewHooks(root, parameter.viewHooks);
			root.textProperty().addListener((obs) -> {
				final String expression = get();

				if (!parameter.evalDouble(expression)) {
					root.pseudoClassStateChanged(PersistentParameter.ALERT, true);
				} else {
					root.pseudoClassStateChanged(PersistentParameter.ALERT, false);
				}

				updateTooltip();
				parameter.valueProperty.set(expression);
			});

			updateTooltip();
		}

		protected final void updateTooltip() {
			tooltip.setText(Double.toString(parameter.getDouble()));
		}

		protected final String get() {
			return root.getText();
		}

		@Override
		public final void set(String value) {
			root.setText(value);
		}

	}

}
