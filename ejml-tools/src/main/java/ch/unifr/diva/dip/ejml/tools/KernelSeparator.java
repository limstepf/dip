package ch.unifr.diva.dip.ejml.tools;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.DoubleMatrix;
import ch.unifr.diva.dip.api.datastructures.FloatMatrix;
import ch.unifr.diva.dip.api.datastructures.Kernel;
import ch.unifr.diva.dip.api.datastructures.Matrix;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.Transmutable;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.MaterialDesignIcons;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ejml.ops.SingularOps;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

/**
 * Kernel Separator. Tries to separate a kernel and output a row- and a
 * column-vector. If unable to, the unseparable kernel is simply bypassed.
 *
 * <p>
 * This works nice together with the convolution filter if we connect kernel,
 * row- and column-vectors (so all of them). If the latter two are not set
 * (since unseparable), then the original kernel will be used in a single-pass
 * convolution. And if we actually got a row- and column-vector, then we can do
 * a (more efficient) double-pass convolution. <br />
 * Then again... if you're sure that your kernel is separable, maybe not connect
 * the separable kernel, but row- and column-vectors only, since the convolution
 * filter is ready to go with a separable filter.
 */
@Component
@Service
public class KernelSeparator extends ProcessableBase implements Transmutable {

	private final InputPort<FloatMatrix> input_float;
	private final InputPort<DoubleMatrix> input_double;

	private final OutputPort<FloatMatrix> output_float;
	private final OutputPort<FloatMatrix> output_float_row;
	private final OutputPort<FloatMatrix> output_float_column;

	private final OutputPort<DoubleMatrix> output_double;
	private final OutputPort<DoubleMatrix> output_double_row;
	private final OutputPort<DoubleMatrix> output_double_column;

	private final EnumParameter kernelPrecision;
	private final ExpParameter singularityThreshold;
	private final ExpParameter errorThreshold;

	public KernelSeparator() {
		super("Kernel Separator");

		this.kernelPrecision = new EnumParameter(
				"Precision", Kernel.Precision.class, Kernel.Precision.FLOAT.name()
		);
		this.parameters.put("precision", this.kernelPrecision);

		this.singularityThreshold = new ExpParameter("Singularity threshold", "0.0001");
		this.parameters.put("singularity-threshold", this.singularityThreshold);
		this.errorThreshold = new ExpParameter("Max. singularity error", "-1");
		this.errorThreshold.setTooltipFormat("Current value: %f. Error check is disabled with a value <= 0.");
		this.parameters.put("singularity-error", this.errorThreshold);

		this.input_float = new InputPort(new ch.unifr.diva.dip.api.datatypes.FloatMatrix(), false);
		this.input_double = new InputPort(new ch.unifr.diva.dip.api.datatypes.DoubleMatrix(), false);

		this.output_float = new OutputPort(new ch.unifr.diva.dip.api.datatypes.FloatMatrix());
		this.output_float_row = new OutputPort(new ch.unifr.diva.dip.api.datatypes.FloatMatrix());
		this.output_float_column = new OutputPort(new ch.unifr.diva.dip.api.datatypes.FloatMatrix());
		this.output_double = new OutputPort(new ch.unifr.diva.dip.api.datatypes.DoubleMatrix());
		this.output_double_row = new OutputPort(new ch.unifr.diva.dip.api.datatypes.DoubleMatrix());
		this.output_double_column = new OutputPort(new ch.unifr.diva.dip.api.datatypes.DoubleMatrix());

		enableAllInputs();
		enableAllOutputs();
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new KernelSeparator();
	}

	@Override
	public void init(ProcessorContext context) {
		configCallback();

		if (context != null) {
			restoreOutputs(context);
		}

		this.kernelPrecision.property().addListener(configListener);
	}

	private final InvalidationListener configListener = (c) -> configCallback();

	private void configCallback() {
		enableInputs();
		enableOutputs();

		transmute();
	}

	@Override
	public boolean isConnected() {
		return xorIsConnected(inputs().values());
	}

	private Kernel.Precision getKernelPrecision() {
		return EnumParameter.valueOf(
				this.kernelPrecision.get(),
				Kernel.Precision.class,
				Kernel.Precision.FLOAT
		);
	}

	private void enableInputs() {
		this.inputs.clear();

		if (getKernelPrecision().equals(Kernel.Precision.DOUBLE)) {
			this.inputs.put("matrix-double", input_double);
		} else {
			this.inputs.put("matrix-float", input_float);
		}
	}

	private void enableAllInputs() {
		this.inputs.clear();
		this.inputs.put("matrix-float", input_float);
		this.inputs.put("matrix-double", input_double);
	}

	private void enableOutputs() {
		this.outputs.clear();

		if (getKernelPrecision().equals(Kernel.Precision.DOUBLE)) {
			enableDoubleOutputs();
		} else {
			enableFloatOutputs();
		}
	}

	private void enableAllOutputs() {
		this.outputs.clear();

		enableFloatOutputs();
		enableDoubleOutputs();
	}

	private void enableFloatOutputs() {
		this.outputs.put("matrix-float", output_float);
		this.outputs.put("row-vector-float", output_float_row);
		this.outputs.put("column-vector-float", output_float_column);
	}

	private void enableDoubleOutputs() {
		this.outputs.put("matrix-double", output_double);
		this.outputs.put("row-vector-double", output_double_row);
		this.outputs.put("column-vector-double", output_double_column);
	}

	@Override
	public void resetOutputs() {
		this.output_float.setOutput(null);
		this.output_float_row.setOutput(null);
		this.output_float_column.setOutput(null);
		this.output_double.setOutput(null);
		this.output_double_row.setOutput(null);
		this.output_double_column.setOutput(null);

		bypassKernel();
	}

	private void bypassKernel() {
		this.output_float.setOutput(this.input_float.getValue());
		this.output_double.setOutput(this.input_double.getValue());
	}

	private boolean restoreOutputs(ProcessorContext context) {
		// bypass unseparable(?) kernel
		bypassKernel();

		// try to restore separable kernels
		final Kernel.Precision precision = getKernelPrecision();

		if (precision.equals(Kernel.Precision.DOUBLE)) {
			final DoubleMatrix rowVector = (DoubleMatrix) context.objects.get("row-vector-double");
			final DoubleMatrix columnVector = (DoubleMatrix) context.objects.get("column-vector-double");

			if (rowVector != null && columnVector != null) {
				this.output_double_row.setOutput(rowVector);
				this.output_double_column.setOutput(columnVector);
				return true;
			}
		} else {
			final FloatMatrix rowVector = (FloatMatrix) context.objects.get("row-vector-float");
			final FloatMatrix columnVector = (FloatMatrix) context.objects.get("column-vector-float");

			if (rowVector != null && columnVector != null) {
				this.output_float_row.setOutput(rowVector);
				this.output_float_column.setOutput(columnVector);
				return true;
			}
		}

		return false;
	}

	@Override
	public void process(ProcessorContext context) {
		if (!restoreOutputs(context)) {

			final Kernel.Precision precision = getKernelPrecision();
			final boolean isDouble = precision.equals(Kernel.Precision.DOUBLE);
			final SimpleMatrix matrix;

			if (isDouble) {
				final DoubleMatrix kernel = this.input_double.getValue();
				matrix = new SimpleMatrix(kernel.getArray2D());
			} else {
				final FloatMatrix kernel = this.input_float.getValue();
				matrix = new SimpleMatrix(
						kernel.rows,
						kernel.columns,
						kernel.layout.equals(Matrix.Layout.ROW_MAJOR_ORDER),
						floatToDouble(kernel.data)
				);
			}

			final SimpleSVD svd = matrix.svd();
			// svd.rank() uses a ridiculously small/conservative singularity
			// threshold, so we rather use our own here
			final double singThresh = this.singularityThreshold.getDouble();
			final int rank = SingularOps.rank(svd.getSVD(), singThresh);

			log.trace("input matrix: {}", matrix);
			log.trace("singularity threshold: {}", singThresh);
			log.trace("svd(matrix) = UWV: {}", svd.getSVD());
			log.trace("U: {}" + svd.getU());
			log.trace("W: {}" + svd.getW());
			log.trace("V: {}" + svd.getV());
			log.debug("singular values({}): {}",
					svd.getSVD().numberOfSingularValues(),
					Arrays.toString(svd.getSVD().getSingularValues())
			);
			log.debug("rank: {} (w. EJML's default threshold: {})", rank, svd.rank());

			// our kernel is only separable if rank == 1, meaning that all
			// rows are linearly dependent
			if (rank == 1) {
				final double s = Math.sqrt(svd.getW().get(0, 0));

				// get horizontal and vertical vectors from first columns of U and V,
				// scaled by the singular value
				final SimpleMatrix column = svd.getU().extractVector(false, 0).scale(s);
				final SimpleMatrix row = svd.getV().extractVector(false, 0).transpose().scale(s);

				log.debug("row vector: {}", row);
				log.debug("column vector: {}", column);

				// and now we should have that:		matrix - column*row ~= 0
				final double errThresh = this.errorThreshold.getDouble();
				if (errThresh > 0) {
					final SimpleMatrix diff = matrix.minus(column.mult(row));
					final double absErr = diff.elementMaxAbs();
					if (absErr > errThresh) {
						log.debug(
								"diff. error check failed. Max. abs. diff: {}, threshold: {}",
								absErr,
								errThresh
						);
						return; // abort, abort; not good enough!
					} else {
						log.debug("diff. error check passed. Max. abs. error: {}", absErr);
					}
				} else {
					log.debug("diff. error check skipped. Threshold: {}", errThresh);
				}

				if (isDouble) {
					final DoubleMatrix rowVector = new DoubleMatrix(1, row.getNumElements());
					copyMatrixData(row, rowVector);
					final DoubleMatrix columnVector = new DoubleMatrix(column.getNumElements(), 1);
					copyMatrixData(column, columnVector);

					context.objects.put("row-vector-double", rowVector);
					context.objects.put("column-vector-double", columnVector);
				} else {
					final FloatMatrix rowVector = new FloatMatrix(1, row.getNumElements());
					copyMatrixData(row, rowVector);
					final FloatMatrix columnVector = new FloatMatrix(column.getNumElements(), 1);
					copyMatrixData(column, columnVector);

					context.objects.put("row-vector-float", rowVector);
					context.objects.put("column-vector-float", columnVector);
				}

				restoreOutputs(context);
			}
		}
	}

	private void copyMatrixData(SimpleMatrix src, DoubleMatrix dst) {
		for (int i = 0; i < dst.data.length; i++) {
			dst.data[i] = src.get(i);
		}
	}

	private void copyMatrixData(SimpleMatrix src, FloatMatrix dst) {
		for (int i = 0; i < dst.data.length; i++) {
			dst.data[i] = (float) src.get(i);
		}
	}

	private double[] floatToDouble(float[] floats) {
		final double[] doubles = new double[floats.length];
		for (int i = 0; i < floats.length; i++) {
			doubles[i] = floats[i];
		}
		return doubles;
	}

	@Override
	public void reset(ProcessorContext context) {
		resetOutputs();
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.MATRIX;
	}

	private final BooleanProperty transmuteProperty = new SimpleBooleanProperty();

	@Override
	public BooleanProperty transmuteProperty() {
		return this.transmuteProperty;
	}
}
