package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.PrototypePipeline;
import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.Region;

/**
 * Pipeline layout strategies. A pipeline layout strategy defines the layout of
 * processors in a pipeline, which not only concerns the visual representation,
 * but also in-stage order of processors. The pipeline layout strategy can be
 * set per pipeline, and a default strategy can be set in the user settings for
 * new pipelines created.
 *
 * <ol>
 * <li>The main axis defines how stages are arranged.</li>
 * <li>The secondary axis defines the order of processors in a single
 * stage.</li>
 * </ol>
 *
 * The LEFTRIGHT layout strategy arranges all stages along the x-axis, from left
 * to right, while the y-axis is used for in-stage order or processors
 * (top-down).
 *
 * <br />
 * Similarly, the TOPDOWN layout strategy arranges all stages along the y-axis,
 * from top to bottom, while the x-axis is used for in-stage order or processors
 * (left-right).
 */
public enum PipelineLayoutStrategy {

	/**
	 * Left-Right layout. Uses the vertical position for in stage order.
	 */
	LEFTRIGHT() {

				@Override
				public Comparator<PrototypeProcessor> comparator() {
					return (PrototypeProcessor a, PrototypeProcessor b)
					-> Double.compare(a.layoutYProperty().get(), b.layoutYProperty().get());
				}

				@Override
				public void arrange(PrototypePipeline pipeline, Map<PrototypeProcessor, ProcessorView> views) {
					final Pipeline.PipelineStages<PrototypeProcessor> stages = new Pipeline.PipelineStages<>(pipeline);
					double x = 0;
					double y;
					double width = 0;
					double w;

					for (Pipeline.Stage<PrototypeProcessor> stage : stages) {
						y = 0;

						final List<ProcessorView> processors = new ArrayList<>();
						for (PrototypeProcessor p : stage.processors) {
							processors.add(views.get(p));
						}

						for (ProcessorView view : processors) {
							view.setLayoutX(x);
							view.setLayoutY(y);

							doLayout(view);
							w = view.getWidth() - view.getHorizontalPadding();

							if (w > width) {
								width = w;
							}
							y += view.getHeight() - view.getVerticalPadding();
						}

						x += width;
						width = 0;
					}
				}

				@Override
				public double getLabelRotation() {
					return 0;
				}

				@Override
				public <T extends Port<?>> void setPortLabelPosition(Region node, PortView<T> port, boolean isInputPort) {
					if (isInputPort) {
						node.setLayoutX(clipPosition(
										port.centerXProperty().get() - 19 - node.getWidth()
								));
					} else {
						node.setLayoutX(clipPosition(
										port.centerXProperty().get() + 20
								));
					}
					node.setLayoutY(clipPosition(
									port.centerYProperty().get() - 10
							));
				}

				@Override
				public ProcessorView newProcessorView(PipelineEditor editor, PrototypeProcessor wrapper) {
					return new ProcessorViewLeftRight(editor, wrapper);
				}
			},
	/**
	 * Top-Down layout. Uses the horizontal position for in stage order.
	 */
	TOPDOWN() {

				@Override
				public Comparator<PrototypeProcessor> comparator() {
					return (PrototypeProcessor a, PrototypeProcessor b)
					-> Double.compare(a.layoutXProperty().get(), b.layoutXProperty().get());
				}

				@Override
				public void arrange(PrototypePipeline pipeline, Map<PrototypeProcessor, ProcessorView> views) {
					final Pipeline.PipelineStages<PrototypeProcessor> stages = new Pipeline.PipelineStages<>(pipeline);
					double x;
					double y = 0;
					double height = 0;
					double h;

					for (Pipeline.Stage<PrototypeProcessor> stage : stages) {
						x = 0;

						final List<ProcessorView> processors = new ArrayList<>();
						for (PrototypeProcessor p : stage.processors) {
							processors.add(views.get(p));
						}

						for (ProcessorView view : processors) {
							view.setLayoutX(x);
							view.setLayoutY(y);

							doLayout(view);
							h = view.getHeight() - view.getVerticalPadding();

							if (h > height) {
								height = h;
							}
							x += view.getWidth() - view.getHorizontalPadding();
						}

						y += height;
						height = 0;
					}

					// TODO: second pass to center, or offer additional TOPDOWNCENTER strategy?
				}

				@Override
				public double getLabelRotation() {
					return -90;
				}

				@Override
				public <T extends Port<?>> void setPortLabelPosition(Region node, PortView<T> port, boolean isInputPort) {
					final double w = node.getWidth() * .5;
					node.setLayoutX(clipPosition(
									port.centerXProperty().get() - w
							));
					if (isInputPort) {
						node.setLayoutY(clipPosition(
										port.centerYProperty().get() - w - 27
								));
					} else {
						node.setLayoutY(clipPosition(
										port.centerYProperty().get() + w + 11
								));
					}
				}

				@Override
				public ProcessorView newProcessorView(PipelineEditor editor, PrototypeProcessor wrapper) {
					return new ProcessorViewTopDown(editor, wrapper);
				}
			};

	/**
	 * Does/fixes the layout of a processor view. This is needed s.t. we can
	 * reliably calculate with the position and bounds of already placed
	 * processor views.
	 *
	 * @param view the placed processor view.
	 */
	private static void doLayout(ProcessorView view) {
		view.applyCss();
		view.layout();
	}

	/**
	 * Safely returns a valid pipeline layout strategy.
	 *
	 * @param name name of the pipeline layout strategy.
	 * @return implementation of a pipeline layout strategy. Returns a LEFTRIGHT
	 * pipeline layout strategy in case the one specified by name isn't
	 * available.
	 */
	public static PipelineLayoutStrategy get(String name) {
		try {
			return PipelineLayoutStrategy.valueOf(name);
		} catch (IllegalArgumentException ex) {
			return getDefault();
		}
	}

	/**
	 * Returns the default pipeline layout strategy.
	 *
	 * @return implementation of the default LEFTRIGHT pipeline layout strategy.
	 */
	public static PipelineLayoutStrategy getDefault() {
		return PipelineLayoutStrategy.LEFTRIGHT;
	}

	/**
	 * Returns a comparator to sort all processors of a stage.
	 *
	 * @return sorted processors of a stage.
	 */
	public abstract Comparator<PrototypeProcessor> comparator();

	/**
	 * Sorts processors of a stage.
	 *
	 * @param processors all processors of a single stage.
	 */
	public void sort(List<? extends PrototypeProcessor> processors) {
		Collections.sort(processors, comparator());
	}

	/**
	 * Arranges processor of a pipeline in the editor pane.
	 *
	 * @param pipeline the pipeline.
	 * @param views processor views.
	 */
	public abstract void arrange(PrototypePipeline pipeline, Map<PrototypeProcessor, ProcessorView> views);

	/**
	 * Processor view factory. Creates a new processor view suitable for the
	 * pipeline layout strategy.
	 *
	 * @param editor the pipeline editor.
	 * @param wrapper the processor wrapper for which to create a view.
	 * @return a processor view for the given processor wrapper.
	 */
	public abstract ProcessorView newProcessorView(PipelineEditor editor, PrototypeProcessor wrapper);

	/**
	 * Returns the rotation that needs to be applied to all port labels. Note
	 * that rotation is applied after all other transforms, with the center of
	 * the node's untransformed layoutBounds as pivot point.
	 *
	 * @return the port label rotation.
	 */
	public abstract double getLabelRotation();

	/**
	 * Sets/updates the port label position ({@literal w.r.t.} the port).
	 *
	 * @param <T> type of the port.
	 * @param node the port label (pane; not the actual label).
	 * @param port the port.
	 * @param isInputPort {@code true} if the port is an input port, {
	 * @false} if the port is an output port.
	 */
	public abstract <T extends Port<?>> void setPortLabelPosition(Region node, PortView<T> port, boolean isInputPort);

	/**
	 * Clips the (port label) position. Prevents bumping of the processor view.
	 *
	 * @param value the value.
	 * @return the clipped value.
	 */
	private static double clipPosition(double value) {
		if (value < 0) {
			value = 0;
		}
		return value;
	}

}
