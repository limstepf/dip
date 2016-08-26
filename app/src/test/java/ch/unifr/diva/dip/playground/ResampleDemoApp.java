package ch.unifr.diva.dip.playground;

import ch.unifr.diva.dip.api.datastructures.FileReference;
import ch.unifr.diva.dip.api.imaging.rescaling.AwtFilteredRescaling;
import ch.unifr.diva.dip.api.imaging.rescaling.AwtRescaling;
import ch.unifr.diva.dip.api.imaging.rescaling.FxFilteredRescaling;
import ch.unifr.diva.dip.api.imaging.rescaling.FxRescaling;
import ch.unifr.diva.dip.api.imaging.rescaling.ResamplingFilter;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.EnumParameter;
import ch.unifr.diva.dip.api.parameters.ExpParameter;
import ch.unifr.diva.dip.api.parameters.FileParameter;
import ch.unifr.diva.dip.api.parameters.IntegerParameter;
import ch.unifr.diva.dip.api.parameters.LabelParameter;
import ch.unifr.diva.dip.api.parameters.XorParameter;
import ch.unifr.diva.dip.gui.layout.Lane;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javax.imageio.ImageIO;
import org.junit.Test;

/**
 * Resample demo.
 */
public class ResampleDemoApp {

	private static int bufferedImageType = BufferedImage.TYPE_INT_RGB;

	public static BufferedImage getRandomImage(int width, int height, int type) {
		final BufferedImage src = new BufferedImage(width, height, type);
		for (Location pt : new RasterScanner(src, true)) {
			src.getRaster().setSample(pt.col, pt.row, pt.band, Math.random() * 255);
		}
		return src;
	}

	public static BufferedImage getOnePixelImage(int width, int height, int type) {
		final BufferedImage src = new BufferedImage(width, height, type);
		int col = width / 2;
		int row = height / 2;
		for (int i = 0; i < src.getRaster().getNumBands(); i++) {
			src.getRaster().setSample(col, row, i, Math.random() * 255);
		}
		return src;
	}

	public static BufferedImage getGradientImage(int width, int height, int type) {
		final BufferedImage src = new BufferedImage(width, height, type);
		double v;
		for (Location pt : new RasterScanner(src, true)) {
			switch (pt.band) {
				case 2:
					v = (1 - (pt.col / width)) * 255;
					break;
				case 1:
					v = (double) pt.row / height * 255;
					break;
				default:
					v = (double) pt.col / width * 255;
					break;
			}
			src.getRaster().setSample(pt.col, pt.row, pt.band, v);
		}
		return src;
	}

	public static BufferedImage getCheckerBoardImage(int width, int height, int type) {
		final BufferedImage src = new BufferedImage(width, height, type);
		for (Location pt : new RasterScanner(src, false)) {
			for (int i = 0; i < src.getRaster().getNumBands(); i++) {
				int offset = (pt.col % 2 == 0) ? 0 : 1;
				int v = ((pt.row + offset) % 2 == 0) ? 0 : 255;
				src.getRaster().setSample(pt.col, pt.row, i, v);
			}
		}
		return src;
	}

	public static class Viewport {

		final public ImageView imageView;
		final public ScrollPane scrollPane;
		final public Label status;

		public BufferedImage dstImage;
		public WritableImage dstImageFx;

		public Viewport() {
			this.imageView = new ImageView();
			this.scrollPane = new ScrollPane();
			GridPane.setHalignment(this.scrollPane, HPos.CENTER);
			GridPane.setValignment(this.scrollPane, VPos.CENTER);
			GridPane.setHgrow(this.scrollPane, Priority.SOMETIMES);
			GridPane.setVgrow(this.scrollPane, Priority.SOMETIMES);
			this.scrollPane.setContent(this.imageView);
			this.status = new Label();
		}

		public ScrollPane getViewport() {
			return this.scrollPane;
		}

		public void setImage(Image image) {
			setImage(image, -1);
		}

		public void setImage(Image image, long td) {
			this.imageView.setImage(image);
			this.status.setText(viewPortInfo(image, td));
		}

		private String viewPortInfo(Image image, long td) {
			final int w = (int) image.getWidth();
			final int h = (int) image.getHeight();
			if (td > 0) {
				return String.format(
						"%dx%d = %d px | %d ms | %d µs",
						w, h, w * h,
						TimeUnit.NANOSECONDS.toMillis(td),
						TimeUnit.NANOSECONDS.toMicros(td)
				);
			}
			return String.format("%dx%d = %d px", w, h, w * h);
		}

		private AwtFilteredRescaling awtfr = new AwtFilteredRescaling();
		private FxFilteredRescaling fxfr = new FxFilteredRescaling();
		private long t0;
		private long td;

		private void resample(String method, int dstWidth, int dstHeight, BufferedImage src) {
			this.dstImage = new BufferedImage(dstWidth, dstHeight, bufferedImageType);
			t0 = System.nanoTime();
			switch (method) {
				case "NN":
					AwtRescaling.zoom(src, this.dstImage, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
					break;
				case "BILINEAR":
					AwtRescaling.zoom(src, this.dstImage, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					break;
				case "BICUBIC":
					AwtRescaling.zoom(src, this.dstImage, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					break;
				case "BRESENHAM":
					AwtRescaling.bresenham(src, this.dstImage);
					break;
				default:
					ResamplingFilter filter = ResamplingFilter.valueOf(method);
					awtfr.setFiterFunction(filter);
					awtfr.zoom(src, this.dstImage);
					break;
			}
			td = System.nanoTime() - t0;
			this.dstImageFx = SwingFXUtils.toFXImage(this.dstImage, null);
			setImage(this.dstImageFx, td);
		}

		private void resample(String method, int dstWidth, int dstHeight, Image src) {
			this.dstImageFx = new WritableImage(dstWidth, dstHeight);
			t0 = System.nanoTime();
			switch (method) {
				case "NN":
					FxRescaling.zoom(src, this.dstImageFx, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
					break;
				case "BILINEAR":
					FxRescaling.zoom(src, this.dstImageFx, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					break;
				case "BICUBIC":
					FxRescaling.zoom(src, this.dstImageFx, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					break;
				case "BRESENHAM":
					FxRescaling.bresenham(src, this.dstImageFx);
					break;
				default:
					ResamplingFilter filter = ResamplingFilter.valueOf(method);
					fxfr.setFiterFunction(filter);
					fxfr.zoom(src, this.dstImageFx);
					break;

			}
			td = System.nanoTime() - t0;
			this.dstImage = null; // not needed/possibly discarded in the process...
			setImage(this.dstImageFx, td);
		}
	}

	public static class App extends JavaFxApplicationTests.StyledSimpleApplication<BorderPane> {

		final private XorParameter sourceSelection;
		final private FileParameter sourceFile;
		final private CompositeGrid sourceGenGrid;
		final private IntegerParameter sourceGenWidth;
		final private IntegerParameter sourceGenHeight;
		final private EnumParameter sourceGen;
		final private ExpParameter dstScale;

		final private EnumParameter dstLibA;
		final private EnumParameter dstMethodA;
		final private EnumParameter dstLibB;
		final private EnumParameter dstMethodB;

		final Viewport srcViewport;
		final Viewport dstViewportA;
		final Viewport dstViewportB;

		final private static List<String> resamplingLibs = Arrays.asList(
				"AWT",
				"FX"
		);
		final private static List<String> resamplingMethods;

		static {
			resamplingMethods = new ArrayList<>();
			resamplingMethods.addAll(Arrays.asList(
					"NN", "BILINEAR", "BICUBIC",
					"BRESENHAM"
			));
			for (ResamplingFilter f : ResamplingFilter.values()) {
				resamplingMethods.add(f.name());
			}
		}

		public App() {
			super(new BorderPane());

			// UI/parameters
			this.sourceFile = new FileParameter(
					"", //"load image",
					"load",
					FileParameter.Mode.OPEN
			);
			this.sourceGen = new EnumParameter(
					"generate image",
					Arrays.asList(
							"1PX",
							"CHECKERBOARD",
							"GRADIENT",
							"RANDOM"
					),
					"1PX"
			);
			this.sourceGenWidth = new IntegerParameter("width", 3);
			final LabelParameter labelW = new LabelParameter(" width: ");
			final LabelParameter labelH = new LabelParameter(" height: ");
			this.sourceGenHeight = new IntegerParameter("height", 3);
			this.sourceGenGrid = new CompositeGrid(
					sourceGen,
					labelW, sourceGenWidth,
					labelH, sourceGenHeight
			);
			this.sourceSelection = new XorParameter(
					"source selection",
					Arrays.asList(
							sourceGenGrid,
							sourceFile
					)
			);
			this.sourceSelection.property().addListener((c) -> updateSource());

			final Label labelS = new Label("Scale factor: ");
			labelS.setPadding(new Insets(5, 0, 0, 25));
			this.dstScale = new ExpParameter("scale", "100");

			final Button update = new Button("update");
			HBox.setMargin(update, new Insets(0, 0, 0, 25));
			update.setOnAction((e) -> update());

			final Lane lane = new Lane();
			lane.setPadding(new Insets(10));
			lane.setAlignment(Pos.TOP_LEFT);
			lane.getChildren().addAll(
					sourceSelection.view().node(),
					labelS,
					dstScale.view().node(),
					update
			);
			this.root.setTop(lane);

			// viewports
			this.dstLibA = new EnumParameter("resampling lib A", resamplingLibs, "AWT");
			this.dstMethodA = new EnumParameter("resampling method A", resamplingMethods, "NN");
			this.dstLibB = new EnumParameter("resampling lib B", resamplingLibs, "AWT");
			this.dstMethodB = new EnumParameter("resampling method B", resamplingMethods, "BRESENHAM");

			this.srcViewport = new Viewport();
			this.dstViewportA = new Viewport();
			this.dstViewportB = new Viewport();

			final GridPane viewports = new GridPane();
			final ColumnConstraints c = new ColumnConstraints();
			c.setHalignment(HPos.CENTER);
			for (int i = 0; i < 3; i++) {
				viewports.getColumnConstraints().add(c);
			}
			this.root.setCenter(viewports);
			addViewport(viewports, srcViewport, "source", null, null);
			addViewport(viewports, dstViewportA, "viewport A", dstLibA, dstMethodA);
			addViewport(viewports, dstViewportB, "viewport B", dstLibB, dstMethodB);

			this.root.widthProperty().addListener((e) -> {
				final double width = viewports.getWidth();
				srcViewport.getViewport().setPrefWidth(width / 3.0);
				dstViewportA.getViewport().setPrefWidth(width / 3.0);
				dstViewportB.getViewport().setPrefWidth(width / 3.0);
			});

			dstViewportA.scrollPane.hvalueProperty().bindBidirectional(dstViewportB.scrollPane.hvalueProperty());
			dstViewportA.scrollPane.vvalueProperty().bindBidirectional(dstViewportB.scrollPane.vvalueProperty());

			this.root.setMinHeight(360);
			this.root.setMinWidth(720);
		}

		private int vcol = 0;

		private void addViewport(GridPane viewports, Viewport viewport, String label, EnumParameter lib, EnumParameter method) {
			if (lib != null && method != null) {
				final GridPane g = new GridPane();
				g.add(lib.view().node(), 0, 0);
				g.add(method.view().node(), 1, 0);
				viewports.add(g, vcol, 0);
			} else {
				final Label l = new Label(" ");
				l.setMaxWidth(Double.MAX_VALUE);
				viewports.add(l, vcol, 0);
			}
			viewports.add(new Label(label), vcol, 1);
			viewports.add(viewport.getViewport(), vcol, 2);
			viewports.add(viewport.status, vcol, 3);
			vcol++;
		}

		private int paramSourceSelection;
		private String paramGen;
		private int paramGenWidth;
		private int paramGenHeight;
		private File paramFile;

		private void updateSource() {
			paramSourceSelection = sourceSelection.get().selection;

			if (paramSourceSelection == 0) {
				paramGen = sourceGen.get();
				paramGenWidth = sourceGenWidth.get();
				paramGenHeight = sourceGenHeight.get();

			} else {
				final FileReference ref = sourceFile.get();
				if (ref != null) {
					paramFile = ref.toFile();
				}
			}
		}

		private File lastFile;
		private BufferedImage sourceImage;
		private Image sourceImageFx;
		private double paramScale;
		private int dstWidth;
		private int dstHeight;

		private void update() {
			paramSourceSelection = sourceSelection.get().selection;

			// (re-)generate/load source image
			if (paramSourceSelection == 0) {
				switch (paramGen) {
					case "CHECKERBOARD":
						sourceImage = getCheckerBoardImage(paramGenWidth, paramGenHeight, bufferedImageType);
						break;
					case "GRADIENT":
						sourceImage = getGradientImage(paramGenWidth, paramGenHeight, bufferedImageType);
						break;
					case "RANDOM":
						sourceImage = getRandomImage(paramGenWidth, paramGenHeight, bufferedImageType);
						break;
					case "1PX":
					default:
						sourceImage = getOnePixelImage(paramGenWidth, paramGenHeight, bufferedImageType);
						break;
				}
				sourceImageFx = SwingFXUtils.toFXImage(sourceImage, null);
				this.srcViewport.setImage(sourceImageFx);
			} else {
				if (paramFile == null || !paramFile.exists()) {
					System.out.println("the file: `" + paramFile + "` does not exist!");
					return;
				}

				try {
					sourceImage = loadBufferedImage(paramFile);
					lastFile = paramFile;
				} catch (IOException ex) {
					System.out.println("failed to read the file: `" + paramFile + "`!");
					return;
				}

				sourceImageFx = SwingFXUtils.toFXImage(sourceImage, null);
				this.srcViewport.setImage(sourceImageFx);
			}

			// inb4 out of memory ;)
			paramScale = dstScale.getDouble();
			dstWidth = (int) (paramScale * sourceImage.getWidth());
			dstHeight = (int) (paramScale * sourceImage.getHeight());

			// resample for viewport A
			final String libA = this.dstLibA.get();
			final String methodA = this.dstMethodA.get();
			resample(dstViewportA, libA, methodA);

			// resample for viewport B
			final String libB = this.dstLibB.get();
			final String methodB = this.dstMethodB.get();
			resample(dstViewportB, libB, methodB);

		}

		private void resample(Viewport viewport, String lib, String method) {
			if (lib.equals("AWT")) {
				viewport.resample(method, dstWidth, dstHeight, sourceImage);
			} else {
				viewport.resample(method, dstWidth, dstHeight, sourceImageFx);
			}
		}

		private BufferedImage loadBufferedImage(File file) throws IOException {
			final BufferedImage image = ImageIO.read(file);
			if (image.getType() != bufferedImageType) {
				final BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), bufferedImageType);
				final Graphics g = rgb.getGraphics();
				g.drawImage(image, 0, 0, null);
				g.dispose();
				return rgb;
			}
			return image;
		}
	}

	@Test
	public void launchResampleApp() {
		Application.launch(App.class);
	}

}
