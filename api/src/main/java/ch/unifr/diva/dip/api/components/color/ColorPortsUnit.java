package ch.unifr.diva.dip.api.components.color;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.TextParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.services.Transmutable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.util.Callback;

/**
 * Color ports unit. The color ports unit sets up ports for
 * {@code BufferedImage}s, providing untyped and typed ports. Single-band (bit,
 * byte, float) and color-typed (RGB, Lab, ...) ports may be specified as needed
 * (i.e. whatever the processor is able to process).
 *
 * @param <T> class of a transmutable processor.
 */
public class ColorPortsUnit<T extends Processor & Transmutable> {

	protected final T processor;

	protected final static String PORT_KEY_UNTYPED = "buffered-image";
	protected final static String PORT_KEY_BINARY = "buffered-image-binary";
	protected final static String PORT_KEY_BYTE = "buffered-image-byte";
	protected final static String PORT_KEY_FLOAT = "buffered-image-float";

	protected final static String STORAGE_IMAGE_FORMAT = "PNG";
	protected final String STORAGE_IMAGE;
	protected final String STORAGE_MAT;

	// untyped ports
	protected final InputPort<BufferedImage> input;
	protected final OutputPort<BufferedImage> output;
	// single-band ports
	protected final InputPort<BufferedImage> input_binary;
	protected final OutputPort<BufferedImage> output_binary;
	protected final InputPort<BufferedImage> input_byte;
	protected final OutputPort<BufferedImage> output_byte;
	protected final InputPort<BufferedImage> input_float;
	protected final OutputPort<BufferedImage> output_float;
	// color-typed ports
	protected final List<InputColorPort> input_colors;
	protected final List<OutputColorPort> output_colors;

	protected final XorParameter config;
	protected final EnumParameter config_colors;
	protected final int index_binary;
	protected final int index_byte;
	protected final int index_float;
	protected final int index_colors;

	protected boolean portInsetPositionTop = true;
	protected boolean disableInputPorts = false;
	protected boolean disableOutputPorts = false;
	protected InputPort input_selected;
	protected OutputPort output_selected;
	protected String key_selected;
	protected ValueListSelection vs;

	/**
	 * Creates a new color ports unit.
	 *
	 * <ul>
	 * <li>The parent processor must implement the {@code Transmutable}
	 * interface.</li>
	 * <li>The unit's configuration parameter (retrieved with a call to
	 * {@code getParameter()}) needs to be added manually to the parameters of
	 * the parent processor.</li>
	 * <li>The unit's {@code init()} method needs to be called in the parent
	 * processor's {@code init()} method.</li>
	 * <li>The method {@code reset()} (or {@code resetStorage()} and
	 * {@code resetOutputs()}) can be called in the parent processor's
	 * {@code reset()} method. </li>
	 * <li>The method {@code restoreOutputs()} may be used in the parent
	 * processor's {@code process()} method to handle persistant storage of an
	 * image, and to offer it as a layer.</li>
	 * </ul>
	 *
	 * @param processor the parent processor owning this unit.
	 * @param name the name of this unit. Possibly used to store persistent
	 * data, in case the {@code restoreOutput} method is used.
	 * @param enableBinaryPorts whether or not to enable ports for
	 * single-channel binary images.
	 * @param enableBytePorts whether or not to enable ports for single-channel
	 * images with a byte sample precision.
	 * @param enableFloatPorts whether or not to enable ports for single-channel
	 * images with a float sample precision.
	 * @param enableColorModels a list of color models for which to enable
	 * ports.
	 */
	public ColorPortsUnit(T processor, String name, boolean enableBinaryPorts, boolean enableBytePorts, boolean enableFloatPorts, List<SimpleColorModel> enableColorModels) {
		this.processor = processor;
		this.STORAGE_IMAGE = name + ".png";
		this.STORAGE_MAT = name + ".bmat";

		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), false);
		this.output = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage());

		final List<Parameter> options = new ArrayList<>();
		int index = 0;

		if (enableBinaryPorts) {
			this.input_binary = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary(), false);
			this.output_binary = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImageBinary());
			this.index_binary = index++;
			options.add(new TextParameter("BIT"));
		} else {
			this.input_binary = null;
			this.output_binary = null;
			this.index_binary = -1;
		}

		if (enableBytePorts) {
			this.input_byte = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImageGray(), false);
			this.output_byte = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImageGray());
			this.index_byte = index++;
			options.add(new TextParameter("BYTE"));
		} else {
			this.input_byte = null;
			this.output_byte = null;
			this.index_byte = -1;
		}

		if (enableFloatPorts) {
			this.input_float = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat(), false);
			this.output_float = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedMatrixFloat());
			this.index_float = index++;
			options.add(new TextParameter("FLOAT"));
		} else {
			this.input_float = null;
			this.output_float = null;
			this.index_float = -1;
		}

		this.input_colors = new ArrayList<>();
		this.output_colors = new ArrayList<>();
		if (enableColorModels != null && !enableColorModels.isEmpty()) {
			final List<String> values = new ArrayList<>();
			for (SimpleColorModel cm : enableColorModels) {
				this.input_colors.add(new InputColorPort(cm));
				this.output_colors.add(new OutputColorPort(cm));
				values.add(cm.name());
			}
			this.config_colors = new EnumParameter("", values, values.get(0));
			this.index_colors = index++;
			options.add(this.config_colors);
		} else {
			this.config_colors = null;
			this.index_colors = -1;
		}

		this.config = new XorParameter("config", options);
		enableAllPorts();
	}

	/**
	 * Inits the color ports unit.
	 *
	 * @param context the processor context of the parent processor.
	 */
	public void init(ProcessorContext context) {
		configCallback();
		// init signal/value
		if (context != null) {
			valueCallback();
		}
		// attach config listener
		this.config.property().addListener(configListener);
		// attach input listeners
		final List<InputPort> ports = Arrays.asList(
				input,
				input_binary,
				input_byte,
				input_float
		);
		for (InputPort port : ports) {
			if (port != null) {
				port.portStateProperty().addListener(inputListener);
			}
		}
		for (InputColorPort icp : this.input_colors) {
			icp.port.portStateProperty().addListener(inputListener);
		}
	}

	// listen to added/removed connections on input ports
	protected final InvalidationListener inputListener = (c) -> inputCallback();

	protected void inputCallback() {
		updatePorts();
		this.processor.transmute();
	}

	protected InputPort<BufferedImage> currentInput;

	protected void setCurrentInput() {
		if (this.currentInput != null) {
			// detach listener
			this.currentInput.valueChangedProperty().removeListener(valueListener);
		}
		this.currentInput = getConnectedInput();
		// attach listener
		this.currentInput.valueChangedProperty().addListener(valueListener);
	}

	// listen to signal/value on connected input port
	protected final InvalidationListener valueListener = (c) -> valueCallback();

	protected void valueCallback() {
		if (this.onInputCallback != null) {
			this.onInputCallback.call(getConnectedInput());
		}
	}

	protected Callback<InputPort<BufferedImage>, Void> onInputCallback;

	/**
	 * Sets the onInput callback. This method is called whenever the output port
	 * the currently selected/used input port is connected to sets/updates the
	 * signal/value. The callback gets passed the currently selected/used input
	 * port, which carries the set/updated signal from the output port.
	 *
	 * @param onInput the onInput callback.
	 */
	public void setOnInput(Callback<InputPort<BufferedImage>, Void> onInput) {
		this.onInputCallback = onInput;
	}

	// listen to configuration changes
	protected final InvalidationListener configListener = (c) -> configCallback();

	protected void configCallback() {
		this.vs = this.config.get();

		updateSelectedPort(this.vs);
		updatePorts();
		this.processor.transmute();
	}

	protected void updateSelectedPort(ValueListSelection vs) {
		if (vs.selection == this.index_binary) {
			this.key_selected = PORT_KEY_BINARY;
			this.input_selected = this.input_binary;
			this.output_selected = this.output_binary;
		} else if (vs.selection == this.index_byte) {
			this.key_selected = PORT_KEY_BYTE;
			this.input_selected = this.input_byte;
			this.output_selected = this.output_byte;
		} else if (vs.selection == this.index_float) {
			this.key_selected = PORT_KEY_FLOAT;
			this.input_selected = this.input_float;
			this.output_selected = this.output_float;
		} else if (vs.selection == this.index_colors) {
			final String key = this.config_colors.get();
			final InputColorPort in = ColorPort.getColorPort(key, this.input_colors);
			this.key_selected = in.key;
			this.input_selected = in.port;
			this.output_selected = ColorPort.getColorPort(key, this.output_colors).port;
		}
	}

	/**
	 * Returns the connected input of the color ports unit.
	 *
	 * @return the connected input, or the untyped input if no input is
	 * connected.
	 */
	public InputPort<BufferedImage> getConnectedInput() {
		if (this.input_selected != null && this.input_selected.isConnected()) {
			return this.input_selected;
		}
		return this.input;
	}

	/**
	 * Checks whether the used input port of the color ports unit is connected,
	 * or not.
	 *
	 * @return True if the used input port is connected, False otherwise.
	 */
	public boolean isConnected() {
		return getConnectedInput().isConnected();
	}

	/**
	 * Returns the signal/value on the connected input port.
	 *
	 * @return the signal/value on the connected input port.
	 */
	public BufferedImage getValue() {
		return getConnectedInput().getValue();
	}

	/**
	 * Returns the configuration parameter of the color ports unit.
	 *
	 * @return the configuration parameter of the color ports unit.
	 */
	public Parameter getParameter() {
		return this.config;
	}

	/**
	 * Sets the port insert position. Under the assumption that ports are kept
	 * in a linked hash map, we may (re-)insert the ports of this unit either
	 * at the top (default), or at the bottom.
	 *
	 * @param top ports of the unit are (re-)inserted at the top if True, at the
	 * bottom otherwise.
	 */
	public void setPortInsertPosition(boolean top) {
		this.portInsetPositionTop = top;
	}

	/**
	 * Checks whether the color ports unit is set to process single-channel
	 * images with binary sample precision.
	 *
	 * @return True if the color ports unit is set to process single-channel
	 * images with binary sample precision, False otherwise.
	 */
	public boolean isBinaryColor() {
		return (this.vs.selection == this.index_binary);
	}

	/**
	 * Checks whether the color ports unit is set to process single-channel
	 * images with byte sample precision.
	 *
	 * @return True if the color ports unit is set to process single-channel
	 * images with byte sample precision, False otherwise.
	 */
	public boolean isByteColor() {
		return (this.vs.selection == this.index_byte);
	}

	/**
	 * Checks whether the color ports unit is set to process single-channel
	 * images with float sample precision.
	 *
	 * @return True if the color ports unit is set to process single-channel
	 * images with float sample precision, False otherwise.
	 */
	public boolean isFloatColor() {
		return (this.vs.selection == this.index_float);
	}

	/**
	 * Checks whether the color ports unit is set to process images with a
	 * specified color model. If True, the color model can be retrieved with a
	 * call to {@code getColorModel()}.
	 *
	 * @return True if the color ports unit is set to process images with a
	 * specified color model, False otherwise.
	 */
	public boolean hasColorModel() {
		return (this.vs.selection == this.index_colors);
	}

	/**
	 * Returns the selected color model if the color ports unit is set to
	 * process images with a specified color model. Use {@code hasColorModel()}
	 * to check first whether this is actually the case.
	 *
	 * @return the selected color model.
	 */
	public SimpleColorModel getColorModel() {
		final String key = this.config_colors.get();
		final InputColorPort in = ColorPort.getColorPort(key, this.input_colors);
		return in.cm;
	}

	/**
	 * Checks whether the color ports unit is set to process images that use
	 * {@code BufferedMatrix} for storage. This applies to all images with float
	 * sample precision.
	 *
	 * @return True if the color ports unit is set to process images in float
	 * sample precision, False otherwise.
	 */
	public boolean isBufferedMatrix() {
		if (this.vs.selection == this.index_float) {
			return true;
		}
		if (this.vs.selection == this.index_colors) {
			final String key = this.config_colors.get();
			final InputColorPort in = ColorPort.getColorPort(key, this.input_colors);
			return in.cm.requiresBufferedMatrix();
		}
		return false;
	}

	/**
	 * Disables all input ports. Needs to be called in the constructor of the
	 * parent processor, or a call to {@code updatePorts()} has to be made if
	 * called at some later point.
	 *
	 * @param disableInputPorts True to disable all input ports, False to
	 * (re-)enable them.
	 */
	public void disableInputPorts(boolean disableInputPorts) {
		this.disableInputPorts = disableInputPorts;
	}

	/**
	 * Disables all output ports. Needs to be called in the constructor of the
	 * parent processor, or a call to {@code updatePorts()} has to be made if
	 * called at some later point.
	 *
	 * @param disableOutputPorts True to disable all output ports, , False to
	 * (re-)enable them.
	 */
	public void disableOutputPorts(boolean disableOutputPorts) {
		this.disableOutputPorts = disableOutputPorts;
	}

	/**
	 * Updates the ports.
	 */
	public void updatePorts() {
		setCurrentInput();

		enableInputs(this.input_selected);
		enableOutputs(this.output_selected);
	}

	private void enableAllPorts() {
		enableAllInputs();
		enableAllOutputs();
	}

	protected void enableAllInputs() {
		enableInputs(null);
	}

	protected void enableInputs(InputPort port) {
		removeAllInputs();

		if (this.disableInputPorts) {
			return;
		}

		final Map<String, InputPort> reinsert;
		if (this.portInsetPositionTop) {
			reinsert = extractPorts(this.processor.inputs());
		} else {
			reinsert = null;
		}

		// only show single connected port
		final InputPort in = getConnectedInput();
		if (port != null && in.isConnected()) {
			this.processor.inputs().put(this.key_selected, in);
			return;
		}

		// otherwise show untyped + one specific extra port,
		// or all (if port == null)
		this.processor.inputs().put(PORT_KEY_UNTYPED, this.input);

		if (this.input_binary != null && (port == null || port.equals(this.input_binary))) {
			this.processor.inputs().put(PORT_KEY_BINARY, this.input_binary);
		}
		if (this.input_byte != null && (port == null || port.equals(this.input_byte))) {
			this.processor.inputs().put(PORT_KEY_BYTE, this.input_byte);
		}
		if (this.input_float != null && (port == null || port.equals(this.input_float))) {
			this.processor.inputs().put(PORT_KEY_FLOAT, this.input_float);
		}
		for (InputColorPort icp : this.input_colors) {
			if (port == null || port.equals(icp.port)) {
				this.processor.inputs().put(icp.key, icp.port);
			}
		}

		if (reinsert != null) {
			for (Map.Entry<String, InputPort> e : reinsert.entrySet()) {
				this.processor.inputs().put(e.getKey(), e.getValue());
			}
		}
	}

	protected void removeAllInputs() {
		if (this.input != null) {
			this.processor.inputs().remove(PORT_KEY_UNTYPED);
		}
		if (this.input_binary != null) {
			this.processor.inputs().remove(PORT_KEY_BINARY);
		}
		if (this.input_byte != null) {
			this.processor.inputs().remove(PORT_KEY_BYTE);
		}
		if (this.input_float != null) {
			this.processor.inputs().remove(PORT_KEY_FLOAT);
		}
		for (InputColorPort icp : this.input_colors) {
			this.processor.inputs().remove(icp.key);
		}
	}

	protected void enableAllOutputs() {
		enableOutputs(null);
	}

	protected void enableOutputs(OutputPort port) {
		removeAllOutputs();

		if (this.disableOutputPorts) {
			return;
		}

		final Map<String, OutputPort> reinsert;
		if (this.portInsetPositionTop) {
			reinsert = extractPorts(this.processor.outputs());
		} else {
			reinsert = null;
		}

		this.processor.outputs().put(PORT_KEY_UNTYPED, this.output);

		if (this.output_binary != null && (port == null || port.equals(this.output_binary))) {
			this.processor.outputs().put(PORT_KEY_BINARY, this.output_binary);
		}
		if (this.output_byte != null && (port == null || port.equals(this.output_byte))) {
			this.processor.outputs().put(PORT_KEY_BYTE, this.output_byte);
		}
		if (this.output_float != null && (port == null || port.equals(this.output_float))) {
			this.processor.outputs().put(PORT_KEY_FLOAT, this.output_float);
		}
		for (OutputColorPort ocp : this.output_colors) {
			if (port == null || port.equals(ocp.port)) {
				this.processor.outputs().put(ocp.key, ocp.port);
			}
		}

		if (reinsert != null) {
			for (Map.Entry<String, OutputPort> e : reinsert.entrySet()) {
				this.processor.outputs().put(e.getKey(), e.getValue());
			}
		}
	}

	protected void removeAllOutputs() {
		if (this.output != null) {
			this.processor.outputs().remove(PORT_KEY_UNTYPED);
		}
		if (this.output_binary != null) {
			this.processor.outputs().remove(PORT_KEY_BINARY);
		}
		if (this.output_byte != null) {
			this.processor.outputs().remove(PORT_KEY_BYTE);
		}
		if (this.output_float != null) {
			this.processor.outputs().remove(PORT_KEY_FLOAT);
		}
		for (OutputColorPort ocp : this.output_colors) {
			this.processor.outputs().remove(ocp.key);
		}
	}

	protected static <T> Map<String, T> extractPorts(Map<String, T> values) {
		final Map<String, T> m = new LinkedHashMap<>();
		for (Map.Entry<String, T> e : values.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}
		values.clear();
		return m;
	}

	/**
	 * Resets the color ports unit its storage and resets all output ports.
	 *
	 * @param context the processor context of the parent processor.
	 */
	public void reset(ProcessorContext context) {
		resetStorage(context);
		resetOutputs();
	}

	/**
	 * Resets the color ports unit its storage.
	 *
	 * @param context the processor context of the parent processor.
	 */
	public void resetStorage(ProcessorContext context) {
		ProcessorBase.deleteFile(context, STORAGE_IMAGE);
		ProcessorBase.deleteFile(context, STORAGE_MAT);
	}

	/**
	 * Resets all output ports.
	 */
	public void resetOutputs() {
		setOutputs(null);
	}

	/**
	 * Sets all output ports.
	 *
	 * @param value the signal/value for the output ports.
	 */
	public void setOutputs(BufferedImage value) {
		if (this.output != null) {
			this.output.setOutput(value);
		}
		if (this.output_binary != null) {
			this.output_binary.setOutput(value);
		}
		if (this.output_byte != null) {
			this.output_byte.setOutput(value);
		}
		if (this.output_float != null) {
			this.output_float.setOutput(value);
		}
		for (OutputColorPort out : this.output_colors) {
			out.port.setOutput(value);
		}
	}

	/**
	 * Restores the output ports. Does not provide an image layer.
	 *
	 * @param context the processor context of the parent processor.
	 * @return True if the outputs could be restored, False otherwise.
	 */
	public boolean restoreOutputs(ProcessorContext context) {
		return restoreOutputs(context, null, false);
	}

	/**
	 * Restores the output ports.
	 *
	 * @param context the processor context of the parent processor.
	 * @param provideLayer True to provide image layer (if possible/not float
	 * sample precision), False to not do that.
	 * @return True if the outputs could be restored, False otherwise.
	 */
	public boolean restoreOutputs(ProcessorContext context, boolean provideLayer) {
		return restoreOutputs(context, null, provideLayer);
	}

	/**
	 * Sets/restores the output ports.
	 *
	 * @param context the processor context of the parent processor.
	 * @param processedImage the processed image, or null. If no processed image
	 * is given then the method tries to load it from the persistant storage.
	 * @param provideLayer True to provide image layer (if possible/not float
	 * sample precision), False to not do that.
	 * @return True if the outputs could be set/restored (either if the
	 * processed image has been supplied as parameter, or if it could be loaded
	 * from the persistent storage), False otherwise (if no processed image has
	 * been supplied, and persistent storage was empty).
	 */
	public boolean restoreOutputs(ProcessorContext context, BufferedImage processedImage, boolean provideLayer) {
		final BufferedImage image;
		if (processedImage == null) {
			if (context == null) {
				return false;
			}
			if (isBufferedMatrix()) {
				image = ProcessorBase.readBufferedMatrix(context, STORAGE_MAT);
			} else {
				image = ProcessorBase.readBufferedImage(context, STORAGE_IMAGE);
			}
		} else {
			image = processedImage;
		}

		if (image == null) {
			return false;
		}

		this.output.setOutput(image);
		this.output_selected.setOutput(image);

		if (provideLayer) {
			if (!isBufferedMatrix()) {
				ProcessorBase.provideImageLayer(context, image);
			}
		}

		return true;
	}

}
