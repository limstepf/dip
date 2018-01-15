package ch.unifr.diva.dip.core.services;

import ch.unifr.diva.dip.api.components.EditorLayerPane;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.utils.L10n;
import ch.unifr.diva.dip.core.services.api.HostProcessor;
import ch.unifr.diva.dip.core.services.api.HostProcessorContext;
import ch.unifr.diva.dip.core.services.api.HostService;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The Gray page generator provides a {@code BufferedImage} of the type
 * {@code TYPE_BYTE_GRAY}.
 */
public class GrayPage extends HostService {

	private static final String BI_KEY = "buffered-image";
	private static final String GRAY_KEY = "buffered-image-gray";

	private final OutputPort<BufferedImage> bi_out;
	private final OutputPort<BufferedImage> gray_out;

	/**
	 * Empty Gray page constructor.
	 */
	public GrayPage() {
		this(null);
	}

	/**
	 * Creates a new Gray page.
	 *
	 * @param context the host processor context.
	 */
	public GrayPage(HostProcessorContext context) {
		super("Gray " + L10n.getInstance().getString("page"));

		this.bi_out = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.outputs.put(BI_KEY, this.bi_out);

		this.gray_out = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImageGray());
		this.outputs.put(GRAY_KEY, this.gray_out);
	}

	@Override
	public HostProcessor newInstance(HostProcessorContext context) {
		return new GrayPage(context);
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

		try {
			final BufferedImage image = assertBufferedImageType(context.page.bufferedImage());
			// provide outputs
			this.bi_out.setOutput(image);
			this.gray_out.setOutput(image);
			// provide visual layer
			final Image fx = SwingFXUtils.toFXImage(image, null);
			final EditorLayerPane layer = context.layer.newLayerPane();
			layer.add(new ImageView(fx));
		} catch (IOException ex) {
			log.error("failed to load the page's image: {}", context.page.file, ex);
		}
	}

	private final static List<Integer> supportedTypes = Arrays.asList(
			BufferedImage.TYPE_BYTE_GRAY
	);

	private BufferedImage assertBufferedImageType(BufferedImage src) {
		if (supportedTypes.contains(src.getType())) {
			return src;
		}

		// TODO: offer alternatives/a parameter to pick a conversion method
		/*
		 This approach results in a weighted conversion where:
		 (  r,   g,   b) -> gray,		ratio
		 -----------------------------------------
		 (  0,   0,   0) ->   0
		 (255,   0,   0) ->  77  +	 	(0.30196)
		 (  0, 255,   0) -> 149  +		(0.58431)
		 (  0,   0, 255) ->  29  +		(0.11373)
		 (255, 255, 255) -> 255  =
		 */
		final BufferedImage dst = new BufferedImage(
				src.getWidth(),
				src.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY
		);
		final Graphics2D g = dst.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		src.flush();
		return dst;
	}

}
