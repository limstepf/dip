package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.Port;
import ch.unifr.diva.dip.api.datatypes.DataType;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.UserSettings;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.PipelineManager;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.layout.AbstractWindow;
import ch.unifr.diva.dip.gui.layout.RubberBandSelector;
import ch.unifr.diva.dip.gui.layout.ZoomPane;
import ch.unifr.diva.dip.gui.main.SideBarPresenter;
import ch.unifr.diva.dip.utils.IOUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The pipeline editor can be used as component or as dialog. Terminology: a
 * 'wire' is just the ConnectionView, but not a 'connection' yet.
 */
public class PipelineEditor extends AbstractWindow implements Presenter {

	private static final Logger log = LoggerFactory.getLogger(PipelineEditor.class);
	public static final double WIRE_RADIUS = 7.0;
	public static final double PORT_RATIO = 0.87;
	protected final ApplicationHandler handler;
	private final SplitPane splitPane = new SplitPane();
	private final List<Parent> splitPaneComponents = new ArrayList<>();
	private final ZoomPane zoomPane;
	private final EditorPane editorPane;
	private final SideBarPresenter sideBar;
	private final PipelineManager manager;
	private final ObjectProperty<Pipeline> selectedPipelineProperty = new SimpleObjectProperty();
	private final List<ProcessorsWidget> processorWidgets = new ArrayList<>();
	private final ListChangeListener<ProcessorWrapper> processorListener;
	private final RubberBandSelector rubberBandSelector;

	/**
	 * Creates a pipeline editor.
	 *
	 * @param owner owner of the pipeline editor's window.
	 * @param handler the application handler.
	 * @param manager the pipeline manager.
	 */
	public PipelineEditor(Window owner, ApplicationHandler handler, PipelineManager manager) {
		super(owner);
		setTitle(localize("pipeline.editor"));

		this.handler = handler;
		this.manager = manager;
		this.editorPane = new EditorPane(this);

		this.zoomPane = new ZoomPane(handler.discardingThreadPool, editorPane.getComponent());
		zoomPane.bindMinDimensions(editorPane.pane());

		this.rubberBandSelector = new RubberBandSelector<>(this.editorPane.pane(), ProcessorView.class);
		this.rubberBandSelector.enable(true);
		this.rubberBandSelector.suppressIfNotDefaultCursor(true);
		// don't turn this into a lambda expression. For some reason this won't work...
		this.rubberBandSelector.selection().addListener(new SetChangeListener<ProcessorView>() {
			@Override
			public void onChanged(SetChangeListener.Change<? extends ProcessorView> c) {
				if (c.wasAdded()) {
					final ProcessorView v = c.getElementAdded();
					v.selectedProperty().set(true);
				}
				// no else! wasReplaced fires wasAdded and(!) wasRemoved
				if (c.wasRemoved()) {
					final ProcessorView v = c.getElementRemoved();
					v.selectedProperty().set(false);
				}
			}
		});

		this.processorListener = (ListChangeListener.Change<? extends ProcessorWrapper> c) -> {
			while (c.next()) {
				if (c.wasReplaced()) {
					for (ProcessorWrapper w : c.getRemoved()) {
						editorPane().updateProcessor(w);
					}
				} else if (c.wasRemoved()) {
					for (ProcessorWrapper w : c.getRemoved()) {
						if (!selectedPipeline().processors().contains(w)) {
							// processor got explicitly/manually deleted
							editorPane().removeProcessor(w);
						} else {
							// only deprecated since service disappeared
							editorPane().deprecateProcessor(w);
						}
					}
				} else if (c.wasAdded()) {
					for (ProcessorWrapper w : c.getAddedSubList()) {
						editorPane().addProcessor(w);
					}
				}
			}
		};

		this.sideBar = new SideBarPresenter();
		// TODO: navigatorWidget needs to listen to dimension changes of the
		// zoomPane content and update its snapshot accordingly!
//		final NavigatorWidget navigator = new NavigatorWidget();
//		navigator.bind(zoomPane);
//		sideBar.addAuxiliaryWidget(navigator);
		final PipelinesWidget pipelines = new PipelinesWidget(handler, this);
		sideBar.addMainWidget(pipelines);

		final ProcessorsWidget hostProcessors = new ProcessorsWidget(
				localize("pipeline.services.host"),
				handler.osgi.hostProcessors
		);
		processorWidgets.add(hostProcessors);
		sideBar.addMainWidget(hostProcessors);

		final ProcessorsWidget processors = new ProcessorsWidget(
				localize("pipeline.services"),
				handler.osgi.processors
		);
		processorWidgets.add(processors);
		sideBar.addMainWidget(processors);

		final Pipeline firstPipeline = manager.pipelines().isEmpty()
				? null
				: manager.pipelines().get(0);
		selectPipeline(firstPipeline);

		root.setCenter(splitPane);

		splitPaneComponents.add(zoomPane);
		splitPaneComponents.add(sideBar.getComponent());
		updateSplitPaneComponents();

		// resize editor and not the sideBar upon resizing the scene/window
		SplitPane.setResizableWithParent(sideBar.getComponent(), false);

		// global key events for the pipeline editor
		this.scene.addEventFilter(KeyEvent.KEY_PRESSED, (e) -> {
			switch (e.getCode()) {
				case A: // select (a)ll
					if (e.isControlDown()) {
						selection().addAll(editorPane.processorViews());
					}
					break;

				case L: // rearrange/do (l)ayout
					if (e.isControlDown()) {
						editorPane.rearrangeProcessors();
					}
					break;

				case BACK_SPACE:
				case DELETE:
					for (Node node : selection()) {
						ProcessorView v = (ProcessorView) node;
						this.selectedPipeline().removeProcessor(v.wrapper(), false);
					}
					break;
			}
		});

		this.setOnCloseRequest((v) -> {
			UserSettings.saveDividerPositions(splitPane, handler.settings.pipelineStage);
			UserSettings.saveStage(this.stage, handler.settings.pipelineStage);

			//TODO: update affected project pages according to changed pipelines
			//      what if we only change some parameters?
//			for (Pipeline p : pipelineManager().pipelines()) {
//				System.out.println(p.getName()
//						+ ", modified: "
//						+ p.modifiedProperty().get());
//			}
		});
	}

	@Override
	public void show() {
		UserSettings.restoreStage(this.stage, handler.settings.pipelineStage);
		super.show();
	}

	@Override
	public void showAndWait() {
		UserSettings.restoreStage(this.stage, handler.settings.pipelineStage);
		super.showAndWait();
	}

	/**
	 * Return the application handler.
	 *
	 * @return the application handler.
	 */
	public ApplicationHandler applicationHandler() {
		return handler;
	}

	/**
	 * Returns the (main) editor pane.
	 *
	 * @return the editor pane.
	 */
	public final EditorPane editorPane() {
		return editorPane;
	}

	/**
	 * Returns the pipeline manager.
	 *
	 * @return the pipeline manager.
	 */
	public final PipelineManager pipelineManager() {
		return manager;
	}

	/**
	 * Returns the selected pipeline property.
	 *
	 * @return the selected pipeline property.
	 */
	public ObjectProperty<Pipeline> selectedPipelineProperty() {
		return selectedPipelineProperty;
	}

	/**
	 * Returns the selected pipeline.
	 *
	 * @return the selected pipeline.
	 */
	public final Pipeline selectedPipeline() {
		return selectedPipelineProperty.get();
	}

	/**
	 * Selects a pipeline.
	 *
	 * @param id the id of the pipeline to be selected.
	 */
	public final void selectPipeline(int id) {
		selectPipeline(manager.getPipeline(id));
	}

	/**
	 * Selects a pipeline.
	 *
	 * @param pipeline the pipeline to be selected.
	 */
	public final void selectPipeline(Pipeline<ProcessorWrapper> pipeline) {
		if (selectedPipeline() != null && selectedPipeline().equals(pipeline)) {
			return;
		}
		editorPane().clear();

		if (selectedPipeline() != null) {
			selectedPipeline().processors().removeListener(processorListener);
		}

		if (manager.pipelines().contains(pipeline)) {
			selectedPipelineProperty.set(pipeline);

			// add processors first, s.t. all ports will be initialized
			for (ProcessorWrapper p : pipeline.processors()) {
				editorPane().addProcessor(p);
			}
			// ...then setup existing connections
			editorPane().setupConnections();

			pipeline.processors().addListener(processorListener);
			setDisableProcessorWidgets(false);
		} else {
			selectedPipelineProperty.set(null);
			setDisableProcessorWidgets(true);
		}
	}

	/**
	 * Disables/enables the processor widgets.
	 *
	 * @param disable True to disable, False to enable the processor widgets.
	 */
	public void setDisableProcessorWidgets(boolean disable) {
		for (ProcessorsWidget w : processorWidgets) {
			w.setDisable(disable);
		}
	}

	/**
	 * Creates a new pipeline.
	 *
	 * @param name name of the new pipeline.
	 */
	public void createPipeline(String name) {
		Pipeline pipeline = manager.createPipeline(name);
		selectPipeline(pipeline);
	}

	/**
	 * Changes the cursor in the (main) editor pane.
	 *
	 * @param cursor the new cursor.
	 */
	public void setCursor(Cursor cursor) {
		editorPane().pane().setCursor(cursor);
	}

	/**
	 * Observable set of selected nodes in the (main) editor pane.
	 *
	 * @return observable set of selected nodes.
	 */
	public final ObservableSet<Node> selection() {
		return rubberBandSelector.selection();
	}

	/**
	 * Shows/hides the side bar of the pipeline editor.
	 *
	 * @param show True to show, False to hide the side bar.
	 */
	public void showSideBar(boolean show) {
		if (show) {
			if (!splitPaneComponents.contains(sideBar.getComponent())) {
				splitPaneComponents.add(sideBar.getComponent());
			}
		} else {
			UserSettings.saveDividerPositions(splitPane, handler.settings.pipelineStage);
			splitPaneComponents.remove(sideBar.getComponent());
		}
		updateSplitPaneComponents();
	}

	private void updateSplitPaneComponents() {
		splitPane.getItems().setAll(splitPaneComponents);
		splitPane.setDividerPositions(handler.settings.pipelineStage.sideBarDivider);
	}

	@Override
	public Parent getComponent() {
		return root;
	}

	/**
	 * Computes a hash in form of a color for a given port based on the declared
	 * datatype.
	 *
	 * @param port an input or output port.
	 * @return a deterministic color.
	 */
	public static Color hashColor(Port port) {
		return hashColor(port.getDataType());
	}

	// hash color cache
	private static final Map<DataType, Color> hashColors = new HashMap<>();

	/**
	 * Computes a hash in form of a color for a given datatype (and associated
	 * class).
	 *
	 * @param dataType the datatype.
	 * @return a deterministic color.
	 */
	public static Color hashColor(DataType dataType) {
		if (hashColors.containsKey(dataType)) {
			return hashColors.get(dataType);
		}

		final String keyC = dataType.dataFormat().toString();
		final String keyS = dataType.type().getCanonicalName();

		final int k = keyC.length();
		final int j = (k > 4) ? k - 5 : 0;
		final String keyM = keyC.substring(j, k);

		int[] channels = {
			255 - IOUtils.hash(keyC, 233),
			IOUtils.hash(keyM, 251) + 5,
			IOUtils.hash(keyM + keyS, 251) + 5
		};

		int hashS = IOUtils.hash(keyS, 4049);
		IOUtils.shuffleArray(channels, hashS);

		final Color color = Color.rgb(channels[0], channels[1], channels[2]);
		hashColors.put(dataType, color);
		return color;
	}

}
