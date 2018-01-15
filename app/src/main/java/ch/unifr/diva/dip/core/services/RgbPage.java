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
 * The RGB page generator provides a {@code BufferedImage} of the type
 * {@code TYPE_INT_RGB}.
 */
public class RgbPage extends HostService {

	private static final String BI_KEY = "buffered-image";
	private static final String RGB_KEY = "buffered-image-rgb";

	private final OutputPort<BufferedImage> bi_out;
	private final OutputPort<BufferedImage> rgb_out;

	/**
	 * Empty RGB page constructor.
	 */
	public RgbPage() {
		this(null);
	}

	/**
	 * Creates a new RGB page.
	 *
	 * @param context the host processor context.
	 */
	public RgbPage(HostProcessorContext context) {
		super("RGB " + L10n.getInstance().getString("page"));

		this.bi_out = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImage());
		this.outputs.put(BI_KEY, this.bi_out);

		this.rgb_out = new OutputPort<>(new ch.unifr.diva.dip.api.datatypes.BufferedImageRgb());
		this.outputs.put(RGB_KEY, this.rgb_out);
	}

	@Override
	public HostProcessor newInstance(HostProcessorContext context) {
		return new RgbPage(context);
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
			final BufferedImage cimage = context.page.bufferedImage();
			final BufferedImage image = assertBufferedImageType(cimage);

			// provide outputs
			this.bi_out.setOutput(image);
			this.rgb_out.setOutput(image);
			// provide visual layer
			final Image fx = SwingFXUtils.toFXImage(image, null);
			final EditorLayerPane layer = context.layer.newLayerPane();
			layer.add(new ImageView(fx));
		} catch (IOException ex) {
			log.error("failed to load the page's image: {}", context.page.file, ex);
		}
	}

	private final static List<Integer> supportedTypes = Arrays.asList(
			BufferedImage.TYPE_INT_RGB,
			BufferedImage.TYPE_INT_ARGB // shouldn't cause any problems (might wanna double check though...)
	);

	// this wouldn't solve all possible problems, but it let's us work nicely
	// with indexed images (i.e. images with a color palette) that otherwise
	// would crash every other filter.
	private BufferedImage assertBufferedImageType(BufferedImage src) {
		if (supportedTypes.contains(src.getType())) {
			return src;
		}

		final BufferedImage dst = new BufferedImage(
				src.getWidth(),
				src.getHeight(),
				BufferedImage.TYPE_INT_RGB
		);
		final Graphics2D g = dst.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		src.flush();
		return dst;
	}

}
