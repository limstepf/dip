package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.BooleanMatrix;
import ch.unifr.diva.dip.api.datastructures.DoubleMatrix;
import ch.unifr.diva.dip.api.datastructures.FloatMatrix;
import ch.unifr.diva.dip.api.datastructures.StringMatrix;
import ch.unifr.diva.dip.api.datastructures.ValueList;
import ch.unifr.diva.dip.api.parameters.ButtonParameter;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EmptyParameter;
import ch.unifr.diva.dip.api.parameters.ExpMatrixParameter;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.IntegerParameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.TransientFacadeParameter;
import ch.unifr.diva.dip.api.components.Preset;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.InvalidationListener;
import org.osgi.service.component.annotations.Component;

/**
 * Matrix editor/generator. Manually configure and provide (small) matrices,
 * e.g. to be used as kernels for convolution filters.
 *
 * <p>
 * This plugin is not suited for larger matrices.
 */
@Component(service = Processor.class)
public class MatrixEditor extends ProcessorBase {

	public static final int MAX_ROWS = 64;
	public static final int MAX_COLUMNS = MAX_ROWS;

	private final OutputPort<BooleanMatrix> output_boolean;
	private final OutputPort<FloatMatrix> output_float;
	private final OutputPort<DoubleMatrix> output_double;

	private final MatrixShapeParameter shape;
	private final ExpMatrixParameter matrix;
	private final ExpParameter multiplier;

	private final InvalidationListener dimensionListener;

	/**
	 * A composite parameter to define the shape of a matrix.
	 */
	public static class MatrixShapeParameter {

		public final IntegerParameter columns;
		public final IntegerParameter rows;
		public final CompositeGrid composite;

		/**
		 * Creates a new matrix shape parameter with default 3x3 shape.
		 */
		public MatrixShapeParameter() {
			this(3, 3, "Shape");
		}

		/**
		 * Creates a new matrix shape parameter.
		 *
		 * @param defaultRows default number of rows.
		 * @param defaultColumns default number of columns.
		 * @param label label of the parameter.
		 */
		public MatrixShapeParameter(int defaultRows, int defaultColumns, String label) {
			final double dimWidth = 35;

			this.rows = new IntegerParameter("rows", defaultRows, 1, MAX_ROWS);
			this.rows.addTextFieldViewHook((tf) -> tf.setPrefWidth(dimWidth));

			this.columns = new IntegerParameter("columns", defaultColumns, 1, MAX_COLUMNS);
			this.columns.addTextFieldViewHook((tf) -> tf.setPrefWidth(dimWidth));

			this.composite = new CompositeGrid(
					label,
					this.rows,
					new TextParameter(" x "),
					this.columns
			);
		}
	}

	/**
	 * Creates a new matrix editor.
	 */
	public MatrixEditor() {
		super("Matrix Editor");

		final double dimWidth = 35;
		final double singleWidth = dimWidth * 2 + 15;
		final int defaultSize = 3;

		this.matrix = new ExpMatrixParameter("matrix", new StringMatrix(defaultSize, defaultSize));
		this.matrix.setCellWidth(singleWidth + this.matrix.getCellSpacing() * 2);

		this.multiplier = new ExpParameter("multiplier", "1");
		this.multiplier.addTextFieldViewHook((tf) -> tf.setPrefWidth(singleWidth));

		this.shape = new MatrixShapeParameter();
		this.parameters.put("shape", this.shape.composite);

		final ExpParameter matModifier = new ExpParameter("matmod", "1");
		matModifier.addTextFieldViewHook((tf) -> tf.setPrefWidth(singleWidth));
		final ButtonParameter modFill = new ButtonParameter("fill", (c) -> {
			final String v = matModifier.get();
			this.matrix.get().fill(v);
			this.matrix.invalidate();
		});
		final CompositeGrid console = new CompositeGrid(
				matModifier,
				modFill
		);
		console.setHorizontalSpacing(3, 8);
		// the console should appear as transient parameter, s.t. it doesn't affect
		// equals/hashCode of the (top) parameter.
		final TransientFacadeParameter<?> consoleFacade = new TransientFacadeParameter<>(console);

		final CompositeGrid mat = new CompositeGrid(
				"Matrix",
				this.multiplier,
				new TextParameter(" x "),
				this.matrix,
				new EmptyParameter(),
				new EmptyParameter(),
				consoleFacade
		);
		mat.setColumnConstraints(3);
		mat.setVerticalSpacing(10);
		this.parameters.put("matrix", mat);

		this.dimensionListener = (obs) -> {
			final int m = this.shape.rows.get();
			final int n = this.shape.columns.get();
			final StringMatrix a = this.matrix.get();
			if (a.rows != m || a.columns != n) {
				this.matrix.set(new StringMatrix(m, n));
			}
		};

		this.output_boolean = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BooleanMatrix());
		this.output_float = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.FloatMatrix());
		this.output_double = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.DoubleMatrix());

		this.outputs.put("boolean-matrix", output_boolean);
		this.outputs.put("float-matrix", output_float);
		this.outputs.put("double-matrix", output_double);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new MatrixEditor();
	}

	@Override
	public void init(ProcessorContext context) {
		this.shape.rows.property().addListener(dimensionListener);
		this.shape.columns.property().addListener(dimensionListener);

		// only set/update outputs if this is for a runnable processor!
		if (context != null) {
			setOutputs();
			this.matrix.property().addListener(matrixListener);
			this.multiplier.property().addListener(matrixListener);
		}
	}

	private final InvalidationListener matrixListener = (c) -> {
		setOutputs();
	};

	private void setOutputs() {
		// use multiplier only if not equal to 1
		final double mul = this.multiplier.getDouble();
		final boolean doMul = new Double(1).compareTo(mul) != 0;

		if (this.output_float.isConnected()) {
			final FloatMatrix fmat = doMul
					? this.matrix.get().getFloatMatrix().multiply((float) mul)
					: this.matrix.get().getFloatMatrix();
			this.output_float.setOutput(fmat);
		}
		if (this.output_double.isConnected()) {
			final DoubleMatrix dmat = doMul
					? this.matrix.get().getDoubleMatrix().multiply(mul)
					: this.matrix.get().getDoubleMatrix();
			this.output_double.setOutput(dmat);
		}
		if (this.output_boolean.isConnected()) {
			final BooleanMatrix bmat = this.matrix.get().getBooleanMatrix();
			this.output_boolean.setOutput(bmat);
		}
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.MATRIX;
	}

	@Override
	public List<Preset> presets() {
		final List<Preset> presets = new ArrayList<>();

		presets.add(new MatrixPreset(
				"Box blur 3x3",
				3,
				3,
				"1/9",
				new String[][]{
					new String[]{"1", "1", "1"},
					new String[]{"1", "1", "1"},
					new String[]{"1", "1", "1"}
				}
		));

		presets.add(new MatrixPreset(
				"Gaussian blur 3x3",
				3,
				3,
				"1/16",
				new String[][]{
					new String[]{"1", "2", "1"},
					new String[]{"2", "4", "2"},
					new String[]{"1", "2", "1"}
				}
		));

		presets.add(new MatrixPreset(
				"Gaussian blur 5x5",
				5,
				5,
				"1/256",
				new String[][]{
					new String[]{"1", "4", "6", "4", "1"},
					new String[]{"4", "16", "24", "16", "4"},
					new String[]{"6", "24", "36", "24", "6"},
					new String[]{"4", "16", "24", "16", "4"},
					new String[]{"1", "4", "6", "4", "1"},
				}
		));

		presets.add(new MatrixPreset(
				"Prewitt operator X (vertical)",
				3,
				3,
				"1",
				new String[][]{
					new String[]{"-1", "0", "1"},
					new String[]{"-1", "0", "1"},
					new String[]{"-1", "0", "1"}
				}
		));

		presets.add(new MatrixPreset(
				"Prewitt operator Y (horizontal)",
				3,
				3,
				"1",
				new String[][]{
					new String[]{"1", "1", "1"},
					new String[]{"0", "0", "0"},
					new String[]{"-1", "-1", "-1"}
				}
		));

		presets.add(new MatrixPreset(
				"Sharpen 3x3",
				3,
				3,
				"1",
				new String[][]{
					new String[]{"0", "-1", "0"},
					new String[]{"-1", "5", "-1"},
					new String[]{"0", "-1", "0"}
				}
		));

		presets.add(new MatrixPreset(
				"Sobel operator X (vertical)",
				3,
				3,
				"1",
				new String[][]{
					new String[]{"-1", "0", "1"},
					new String[]{"-2", "0", "2"},
					new String[]{"-1", "0", "1"}
				}
		));

		presets.add(new MatrixPreset(
				"Sobel operator Y (horizontal)",
				3,
				3,
				"1",
				new String[][]{
					new String[]{"1", "2", "1"},
					new String[]{"0", "0", "0"},
					new String[]{"-1", "-2", "-1"}
				}
		));

		return presets;
	}

	/**
	 * A matrix preset. We use two {@code CompositeGrid} parameters backed by
	 * {@code ValueList}s (only persistent parameters are stored). The
	 * multiplier has to be given as a {@code String}, similarly we're using a
	 * {@code StringMatrix}, for all these strings are intented for
	 * {@code ExpParameter}s.
	 */
	public static class MatrixPreset extends Preset {

		/**
		 * Creates a new matrix preset.
		 *
		 * @param name name of the preset.
		 * @param m number of rows.
		 * @param n number of columns.
		 * @param multiplier the multiplier (applied to all values
		 * individually).
		 * @param matrix the m-by-n matrix.
		 */
		public MatrixPreset(String name, int m, int n, String multiplier, String[][] matrix) {
			super(name, getValues(m, n, multiplier, matrix));
		}

		protected static Map<String, Object> getValues(int m, int n, String multiplier, String[][] matrix) {
			final Map<String, Object> values = new HashMap<>();
			values.put("shape", new ValueList(Arrays.asList(
					m,
					n
			)));
			values.put("matrix", new ValueList(Arrays.asList(
					multiplier,
					new StringMatrix(matrix)
			)));
			return values;
		}

	}

}
