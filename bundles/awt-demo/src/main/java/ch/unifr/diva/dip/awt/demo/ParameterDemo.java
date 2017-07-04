package ch.unifr.diva.dip.awt.demo;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.BooleanParameter;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.CompositeGridMap;
import ch.unifr.diva.dip.api.parameters.DoubleParameter;
import ch.unifr.diva.dip.api.parameters.EmptyParameter;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.FileParameter;
import ch.unifr.diva.dip.api.parameters.FloatParameter;
import ch.unifr.diva.dip.api.parameters.IntegerParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.OptionParameter;
import ch.unifr.diva.dip.api.parameters.Parameter;
import ch.unifr.diva.dip.api.parameters.StringParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.osgi.service.component.annotations.Component;

/**
 * Parameter demo. Also demonstrates a simple pass-through, which can be neat to
 * wire up processors in a longer chain that all need the same input.
 */
@Component(service = Processor.class)
public class ParameterDemo extends ProcessableBase {

	private final InputPort<BufferedImage> input;
	private final OutputPort<BufferedImage> output;

	enum SomeOptions {
		A("Option A"),
		B("Option B"),
		C("Option C"),
		D("Option D");

		private final String label;

		SomeOptions(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public ParameterDemo() {
		super("Parameter Demo");

		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), true);
		this.inputs.put("buffered-image", this.input);

		this.output = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.outputs.put("buffered-image", this.output);

		// If we'd do something useful here, we'd wanna have a class variable for
		// each parameter in order to retrieve their values alter on (e.g. in process()).
		this.parameters.put("option-param",
				new OptionParameter("mode", Arrays.asList("avg.", "mean"), 0)
		);
		this.parameters.put("enum-param",
				new EnumParameter("enum", SomeOptions.class, SomeOptions.C.name())
		);
		// eh, let's put these into composite parameters, for the sake of demonstration
		/*
		this.parameters.put("boolean-param", new BooleanParameter("boolean", false));
		this.parameters.put("double-param", new DoubleParameter("double", 8.0, 1.0, 64.0));
		this.parameters.put("float-param", new FloatParameter("float", 0.67f, 0.0f, 1.0f));
		this.parameters.put("integer-param", new IntegerParameter("integer", 5));
		*/
		final CompositeGrid compositeGrid = new CompositeGrid(
				new LabelParameter("boolean"),
				new LabelParameter("double1"),
				new LabelParameter("double2"),
				new BooleanParameter("boolean", false),
				new DoubleParameter("double", 8.0, 1.0, 64.0),
				new DoubleParameter("double", 0.55, -1.0, 1.0)
		);
		compositeGrid.setColumnWidthConstraints(0.33, 0.33, 0.33);
		// For composite parameters we only need to "publish" (put on this.parameters)
		// the composite parameter itself, not the child parameters. However it might
		// be still nice to have a reference directly to child parameters...
		this.parameters.put("composite-list", compositeGrid);

		final Map<String, Parameter> compositeMap = new LinkedHashMap<>();
		compositeMap.put("empty-param", new EmptyParameter()); // skip first cell
		compositeMap.put("float-param", new FloatParameter("float", 0.67f, 0.0f, 1.0f));
		compositeMap.put("integer-param", new IntegerParameter("integer", 5));
		final CompositeGridMap compositeGridMap = new CompositeGridMap(compositeMap);
		compositeGridMap.setColumnWidthConstraints(0.33, 0.33, 0.33);
		this.parameters.put("composite-map", compositeGridMap);

		this.parameters.put("integer-slider", new IntegerSliderParameter("integer", 5, 0, 10));
		this.parameters.put("string-param", new StringParameter("string", "default"));
		this.parameters.put("exp-param", new ExpParameter("Math. expression", "sin(pi/2) + cos(pi/4)"));
		this.parameters.put("file-input",
				new FileParameter("Input", "Import...", FileParameter.Mode.OPEN)
		);
		this.parameters.put("file-output",
				new FileParameter("Output", "Export...", FileParameter.Mode.SAVE)
		);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		// at this point parameters are - obviously - not set/restored yet
		return new ParameterDemo();
	}

	@Override
	public void init(ProcessorContext context) {
		// init get's called for all spawned/instantiated processors in a
		// pipeline (pipeline editor) or in a runnable pipeline (fully functional
		// with data on inputs and all...).
		//
		// At this point all parameters have been set/restored and can be queried
		// in order to restore the configuration of the processor (needed for both:
		// ordinary pipelines in the editor, or runnable pipelines).
		//
		// This is also the preferred point to attach any kind of property listeners
		// such as InputPort listeners, or parameter property listeners; usually
		// needed for both, pipelines and runnable pipelines, if present.

		if (context != null) {
			// if context is not null here (meaning this is for a runnable pipeline),
			// we should restore the processors full state (as opposed to only it's
			// configuration), i.e. we'd wanna try to restore what we've computed
			// so far (and set all outputs if possible)
		}
	}

	@Override
	public void process(ProcessorContext context) {
		// retrive needed values from parameters here
		// ...

		// simple pass-through
		this.output.setOutput(this.input.getValue());
	}

	@Override
	public void reset(ProcessorContext context) {
		this.output.setOutput(null);
	}

}
