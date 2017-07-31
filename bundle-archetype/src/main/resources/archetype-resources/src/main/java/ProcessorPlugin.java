package ${package};

import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import org.osgi.service.component.annotations.Component;

/**
 * A processor plugin.
 */
@Component(service = Processor.class)
public class ProcessorPlugin extends ProcessableBase {

	/**
	 * Creates a new processor plugin.
	 */
	public ProcessorPlugin() {
		super("Processor Plugin");
		// TODO: setup all parameters
		// TODO: setup all ports
	}

	@Override
	public NamedGlyph glyph() {
		// no need to overwrite this method, but that's how you customize the
		// glyph of the processor. Other glyph fonts are available too.
		return MaterialDesignIcons.CHIP;
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new ProcessorPlugin();
	}

	@Override
	public void init(ProcessorContext context) {
		/*
		 * If no context is passed, then this instance is used as a non-runnable
		 * (or passive/latent) processor inside the pipeline editor.
		 */

		// TODO (optional): manage/deactivate ports, and repaint
		// TODO (optional): attach (port-)event listeners

		if (context != null) {
			// TODO: restore state (outputs, layers, ...)
		}
	}

	@Override
	public void process(ProcessorContext context) {
		// TODO: process signal(s) from input ports
		// TODO: store result, and set output ports, layers, ...
		// TODO (optional): implement the Previewable interface if applicable

	}

	@Override
	public void reset(ProcessorContext context) {
		// TODO: reset state (outputs, layers, files, context/object map, ...)
	}

}
