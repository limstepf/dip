package ch.unifr.diva.dip.awt.demo;

import ch.unifr.diva.dip.api.components.EditorLayerGroup;
import ch.unifr.diva.dip.api.components.EditorLayerPane;
import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.datastructures.JaxbList;
import ch.unifr.diva.dip.api.datastructures.Line2D;
import ch.unifr.diva.dip.api.datastructures.Point2D;
import ch.unifr.diva.dip.api.services.ProcessableBase;
import ch.unifr.diva.dip.api.services.Processor;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.osgi.service.component.annotations.Component;

/**
 * Layer demo.
 */
@Component(service = Processor.class)
public class LayerDemo extends ProcessableBase {

	private final InputPort<BufferedImage> input;
	private final OutputPort<List<Point2D>> points_out;
	private final OutputPort<List<Line2D>> lines_out;

	public LayerDemo() {
		super("Layer Demo");

		this.input = new InputPort(new ch.unifr.diva.dip.api.datatypes.BufferedImage(), true);
		this.inputs.put("buffered-image", this.input);

		this.points_out = new OutputPort(new ch.unifr.diva.dip.api.datatypes.Points2D());
		this.outputs.put("points", this.points_out);

		this.lines_out = new OutputPort(new ch.unifr.diva.dip.api.datatypes.Lines2D());
		this.outputs.put("lines", this.lines_out);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new LayerDemo();
	}

	@Override
	public void init(ProcessorContext context) {
		if (context != null) {
			if (context.hasKeys("points", "lines", "width", "height")) {
				final int width = (int) context.getObjects().get("width");
				final int height = (int) context.getObjects().get("height");
				// custom classes (with their own @XmlRootElement annotation) need
				// to be wrapped in a JaxbList to be marshalled to xml!
				final JaxbList<Point2D> pointSet = (JaxbList<Point2D>) context.getObjects().get("points");
				final JaxbList<Line2D> lineSet = (JaxbList<Line2D>) context.getObjects().get("lines");

				this.points = null; // make sure to get new layers
				this.lines = null;
				drawPoints(getPointsLayer(context), pointSet.getList(), width, height);
				drawLines(getLinesLayer(context), lineSet.getList(), width, height);
			}
		}
	}

	// radius of circles representing points
	private static final double radius = 2.0;

	private EditorLayerGroup points;

	private EditorLayerGroup getPointsLayer(ProcessorContext context) {
		if (this.points == null) {
			this.points = context.getLayer().newLayerGroup("points");
		}
		return this.points;
	}

	private EditorLayerGroup lines;

	private EditorLayerGroup getLinesLayer(ProcessorContext context) {
		if (this.lines == null) {
			this.lines = context.getLayer().newLayerGroup("lines");
		}
		return this.lines;
	}

	@Override
	public void process(ProcessorContext context) {
		try {
			final List<Point2D> pointSet = new ArrayList<>();
			final List<Line2D> lineSet = new ArrayList<>();

			final EditorLayerGroup root = context.getLayer();

			final int height = input.getValue().getHeight();
			final int width = input.getValue().getWidth();
			context.getObjects().put("width", width);
			context.getObjects().put("height", height);
			final int dx = 100;
			final double r = 2;

			for (int y = dx; y < height; y = y + dx) {
				for (int x = dx; x < width; x = x + dx) {
					pointSet.add(new Point2D(x, y));
				}
			}
			cancelIfInterrupted();
			drawPoints(getPointsLayer(context), pointSet, width, height);
			// custom classes (with their own @XmlRootElement annotation) need
			// to be wrapped in a JaxbList to be marshalled to xml!
			context.getObjects().put("points", new JaxbList(pointSet));

			for (int y = dx; y < height; y = y + dx) {
				lineSet.add(new Line2D(0, y, width, y));
			}
			cancelIfInterrupted();
			drawLines(getLinesLayer(context), lineSet, width, height);
			context.getObjects().put("lines", new JaxbList(lineSet));

			this.points_out.setOutput(pointSet);
			this.lines_out.setOutput(lineSet);
			cancelIfInterrupted();
		} catch (InterruptedException ex) {
			reset(context);
		}
	}

	private void drawPoints(final EditorLayerGroup layer, final List<Point2D> pointSet, int width, int height) {
		final ArrayList<Circle> circles = new ArrayList<>();
		for (Point2D point : pointSet) {
			final Circle c = new Circle(point.x, point.y, radius, getColor((int) point.x, width, (int) point.y, height));
			circles.add(c);
		}
		layer.run(() -> {
			for (Circle c : circles) {
				final EditorLayerPane pane = layer.newLayerPane(
						String.format("point (%d, %d)", c.centerXProperty().intValue(), c.centerYProperty().intValue())
				);
				pane.add(c);
			}
			layer.reverseChildren();
		});
	}

	private void drawLines(final EditorLayerGroup layer, final List<Line2D> lineSet, int width, int height) {
		final ArrayList<Line> lines = new ArrayList<>();
		for (Line2D line : lineSet) {
			final Line l = new Line(line.start.x + 1, line.start.y, line.end.x - 1, line.end.y);
			l.setStroke(getColor(0, width, (int) line.start.y, height));
			lines.add(l);
		}
		layer.run(() -> {
			for (Line l : lines) {
				final EditorLayerPane pane = layer.newLayerPane(
						String.format("line (0, %d)", l.startYProperty().intValue())
				);
				pane.add(l);
			}
			layer.reverseChildren();
		});
	}

	@Override
	public void reset(ProcessorContext context) {
		if (this.points != null) {
			this.points.clear();
		}
		if (this.lines != null) {
			this.lines.clear();
		}
		this.points_out.setOutput(null);
		this.lines_out.setOutput(null);

		context.getObjects().clear();
		context.getLayer().clear();
	}

	private static Color getColor(int x, double maxX, int y, double maxY) {
		final Color c = Color.color(1.0 - x / maxX, 1.0 - y / maxY, 0.0);
		return c;
	}
}
