package ch.unifr.diva.dip.awt.tools;

import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.StringParameter;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.awt.components.ColorPortsUnit;
import ch.unifr.diva.dip.awt.imaging.SimpleColorModel;
import ch.unifr.diva.dip.glyphs.mdi.MaterialDesignIcons;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.osgi.service.component.annotations.Component;

/**
 * A {@code BufferedImage} exporter. Writes {@code BufferedImage}s to the
 * project's export directory.
 */
@Component(service = Processor.class)
public class BufferedImageExporter extends ProcessableBase {

	/**
	 * Available image formats for writing/exporting.
	 */
	public enum ImageFormat {

		BMP,
		GIF,
		JPG,
		PNG;

		public String getFileExtension() {
			return this.name().toLowerCase();
		}

	}

	/**
	 * The default image format for writing/exporting.
	 */
	public static ImageFormat DEFAULT_IMAGE_FORMAT = ImageFormat.PNG;

	private final ColorPortsUnit<BufferedImageExporter> colorPortsUnit;
	private final StringParameter nameParam;
	private final EnumParameter formatParam;
	// we keep a path to the export directory around (updated each time we see a
	// context), s.t. we can determine if the file has already been exported.
	private Path exportDirectory;

	/**
	 * Creates a new {@code BufferedImage} exporter.
	 */
	public BufferedImageExporter() {
		super("BufferedImage Exporter");

		this.exportDirectory = null;
		this.colorPortsUnit = new ColorPortsUnit<>(
				this,
				"buffered-image-exporter",
				true, // bit
				true, // byte
				false, // float
				Arrays.asList(
						SimpleColorModel.RGB,
						SimpleColorModel.RGBA
				)
		);
		colorPortsUnit.disableOutputPorts(true);
		this.parameters.put("config", this.colorPortsUnit.getParameter());

		this.nameParam = new StringParameter("name");
		this.formatParam = new EnumParameter(
				"", ImageFormat.class, DEFAULT_IMAGE_FORMAT.name()
		);
		final CompositeGrid grid = new CompositeGrid(
				"File",
				nameParam,
				formatParam
		);
		grid.setHorizontalSpacing(5);
		this.parameters.put("file", grid);
	}

	@Override
	public NamedGlyph glyph() {
		return MaterialDesignIcons.FILE_IMAGE;
	}

	private String getName() {
		// we might wanna sanitize this to make sure we'll end up with a valid filename
		return this.nameParam.get().trim();
	}

	// some filename is required
	@Override
	public boolean isWaitingOnInputParams() {
		return getName().isEmpty();
	}

	// we consider the processor ready if the export file already exists/has
	// been written to (reset will delete it)
	@Override
	public boolean isReadyOutputParams() {
		final ImageFormat format = EnumParameter.valueOf(
				formatParam.get(),
				ImageFormat.class,
				DEFAULT_IMAGE_FORMAT
		);
		if (this.exportDirectory != null) {
			final Path out = exportDirectory.resolve(getExportFilename(format));
			return Files.exists(out);
		}
		return false;
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		final BufferedImageExporter p = new BufferedImageExporter();
		p.setExportPath(context);
		return p;
	}

	@Override
	public void init(ProcessorContext context) {
		this.colorPortsUnit.init(context);
		setExportPath(context);
	}

	private void setExportPath(ProcessorContext context) {
		if (context != null) {
			this.exportDirectory = context.getExportDirectoryPath();
		}
	}

	private ImageFormat getFormat() {
		return EnumParameter.valueOf(
				formatParam.get(),
				ImageFormat.class,
				DEFAULT_IMAGE_FORMAT
		);
	}

	private String getExportFilename(ImageFormat format) {
		return String.format(
				"%s.%s",
				getName(),
				format.getFileExtension()
		);
	}

	private Path getExportFile(ProcessorContext context, ImageFormat format) {
		try {
			final Path directory = context.getExportDirectory();
			return directory.resolve(getExportFilename(format));
		} catch (IOException ex) {
			log.error(
					"failed to access the project's export directory: {}",
					context.getExportDirectoryPath(), ex
			);
		}

		return null;
	}

	@Override
	public void process(ProcessorContext context) {
		setExportPath(context);
		final BufferedImage image = this.colorPortsUnit.getValue();
		final ImageFormat format = getFormat();
		final Path out = getExportFile(context, format);
		if (out != null) {
			ProcessorBase.deleteFile(out);
			try (OutputStream os = Files.newOutputStream(out)) {
				ImageIO.write(image, format.name(), os);
			} catch (IOException ex) {
				log.error("failed to write the file: {}", out, ex);
			}
		}
	}

	@Override
	public void reset(ProcessorContext context) {
		setExportPath(context);
		final Path out = getExportFile(context, getFormat());
		context.deleteExportFile(out);
	}

}
