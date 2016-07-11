package ch.unifr.diva.dip.core.services;

import ch.unifr.diva.dip.api.components.EditorLayerPane;
import ch.unifr.diva.dip.core.services.api.HostProcessorContext;
import ch.unifr.diva.dip.core.services.api.HostProcessor;
import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.core.model.ProjectPage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.image.ImageView;
import org.slf4j.LoggerFactory;

/**
 * The page generator (or source) provides the image of a page to the pipeline.
 */
public class PageGenerator implements HostProcessor {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ProjectPage.class);

	private static final String name = "Page Generator";
	private static final String OUT_FX = "image";
	private static final String OUT_AWT = "buffered-image";

	private final Map<String, InputPort> inputs = new HashMap();
	private final Map<String, OutputPort> outputs = new LinkedHashMap();
	private final OutputPort output_awt;
	private final OutputPort output_fx;

	public PageGenerator() {
		this(null);
	}

	public PageGenerator(HostProcessorContext context) {
		this.output_awt = new OutputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.outputs.put(OUT_AWT, this.output_awt);

		this.output_fx = new OutputPort(new ch.unifr.diva.dip.api.datatypes.FxImage());
		this.outputs.put(OUT_FX, this.output_fx);
	}

	@Override
	public HostProcessor newInstance(HostProcessorContext context) {
		return new PageGenerator(context);
	}

	@Override
	public void init(HostProcessorContext context) {
		if (context == null) {
			return;
		}

		if (!context.page.imageExists()) {
			log.warn("the page's image file does not exist: {}", context.page.file);
			return;
		}

		// load javafx image immediately, since we wanna display the image anyways...
		try {
			// provide/set output
			this.output_fx.setOutput(context.page.image());

			// provide/set visual layer
			final EditorLayerPane layer = context.layer.newLayerPane();
			layer.add(new ImageView(context.page.image()));
		} catch (IOException ex) {
			log.error("failed to load the page's image as a JavaFX Image: {}", context.page.file, ex);
		}

		// buffered image only on demand/if connected (no need for a port listener)
		provideBufferedImage(context);
	}

	private void provideBufferedImage(HostProcessorContext context) {
		if (this.output_awt.getPortState().equals(Port.State.WAITING)) {
			try {
				this.output(OUT_AWT).setOutput(context.page.bufferedImage());
			} catch (IOException ex) {
				log.error("failed to load the page's image as BufferedImage: {}", context.page.file, ex);
			}
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Map<String, InputPort> inputs() {
		return inputs;
	}

	@Override
	public Map<String, OutputPort> outputs() {
		return outputs;
	}

}
