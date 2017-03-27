package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.Project;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import ch.unifr.diva.dip.eventbus.events.ProcessorNotification;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.layout.Pannable;
import ch.unifr.diva.dip.gui.layout.ZoomPaneBresenham;
import ch.unifr.diva.dip.gui.layout.Zoomable;
import com.google.common.eventbus.Subscribe;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The EditorPresenter.
 */
public class EditorPresenter implements Presenter {

	private static final Logger log = LoggerFactory.getLogger(EditorPresenter.class);
	private final ApplicationHandler handler;
	private final LayerGroup rootLayer;
	private final ZoomPaneBresenham zoomPane;
	private NavigatorWidget navigatorWidget;
	private LayersWidget layersWidget;
	private RunnableProcessor currentProcessor = null;
	// private overlay pane shared by global tools and processor overlays
	private final Pane overlayPane;
	private Node currentProcessorOverlayNode;

	/**
	 * Creates a new editor presenter.
	 *
	 * @param handler the application handler.
	 */
	public EditorPresenter(ApplicationHandler handler) {
		this.handler = handler;

		this.rootLayer = new LayerGroup();
		this.zoomPane = new ZoomPaneBresenham(handler.discardingThreadPool);
		zoomPane.setContent(this.rootLayer.getComponent());
		zoomPane.getStyleClass().add("dip-editor");
		zoomPane.setInterpolation(
				Zoomable.Interpolation.get(handler.settings.editor.interpolation)
		);
		zoomPane.setPaddingMethod(Pannable.Padding.VIEWPORT);
		this.overlayPane = new Pane();
		zoomPane.setOverlayContent(overlayPane);

		// repaint listener
		this.rootLayer.onModifiedProperty().addListener((e) -> this.zoomPane.fireContentChange());
		// content modification listener (not all repaints are due to content modifications)
		this.rootLayer.onModifiedContentProperty().addListener((e) -> {
			final Project project = this.handler.getProject();
			if (project != null) {
				this.handler.getProject().setModified(true);
			}
		});
	}

	/**
	 * Returns the zoom pane of the editor.
	 *
	 * @return the zoom pane.
	 */
	public ZoomPaneBresenham getZoomPane() {
		return this.zoomPane;
	}

	@Override
	public Parent getComponent() {
		return this.zoomPane.getNode();
	}

	@Subscribe
	public void projectNotification(ProjectNotification event) {
		switch (event.type) {
			case OPENED:
				onOpenProject();
				break;
			case SELECTED:
				onSelectPage(event.page);
				break;
			case MODIFIED:
				onPageModified();
				break;
			case PAGE_REMOVED:
				onPageRemoved(event.page);
				break;
			case CLOSING:
				onClosingProject();
				break;
			case CLOSED:
				break;
			default:
				log.warn("unhandled project notification: {}", event.type);
				break;
		}
	}

	@Subscribe
	public void processorNotification(ProcessorNotification event) {
		switch (event.type) {
			case SELECTED:
				onProcessorSelected(event.processorId);
				break;
		}
	}

	private void onOpenProject() {
		onSelectPage(handler.getProject().getSelectedPageId());
	}

	private void onSelectPage(int id) {
		// just clear and return on -1
		clear();

		if (id < 0) {
			return;
		}

		// build stage and processor layers
		buildStages();

		// TODO: save and restore zoom and position (centered pixel) for all pages
		zoomPane.moveToPos(Pos.TOP_LEFT, 50);
	}

	private void buildStages() {
		final ProjectPage page = handler.getProject().getSelectedPage();

		if (page.getPipeline() != null) {
			for (Pipeline.Stage<RunnableProcessor> stage : page.getPipeline().stages()) {
				final LayerGroup stageGroup = new LayerGroup(stage.title());

				for (RunnableProcessor p : stage.processors) {
					stageGroup.getChildren().add(p.layer());
				}
				this.rootLayer.getChildren().add(stageGroup);
			}
		}

		// since we're currently not saving the expandProperty of layers, we're
		// just going to expand all layers upon opening/building the stages.
		expandLayers();
	}

	private void onProcessorSelected(int processorId) {
		resetOverlay();

		final Project project = handler.getProject();
		if (project == null || processorId < 0) {
			currentProcessor = null;
			return;
		}
		final ProjectPage page = project.getSelectedPage();
		currentProcessor = page.getPipeline().getProcessor(processorId);

		currentProcessor.layerOverlay().setZoomable(zoomPane);
		currentProcessorOverlayNode = currentProcessor.layerOverlay().getNode();
		overlayPane.getChildren().add(0, currentProcessorOverlayNode);
	}

	private void clear() {
		this.rootLayer.clear();
		resetOverlay();
	}

	private void resetOverlay() {
		if (currentProcessorOverlayNode != null) {
			this.overlayPane.getChildren().remove(currentProcessorOverlayNode);
		}
	}

	private void onPageModified() {
		clear();
		buildStages();
		layersWidget().setRoot(this.rootLayer.getTreeItem());
	}

	private void onPageRemoved(int id) {
		if (handler.getProject().getSelectedPageId() == id) {
			clear();
		}
	}

	private void onClosingProject() {
		clear();
	}

	/**
	 * Sets the interpolation type used for zooming/resampling.
	 *
	 * @param type the interpolation type.
	 */
	public void setInterpolation(Zoomable.Interpolation type) {
		this.zoomPane.setInterpolation(type);
	}

	/**
	 * Returns the editor's navigator widget.
	 *
	 * @return a navigator widget.
	 */
	public NavigatorWidget navigatorWidget() {
		if (this.navigatorWidget == null) {
			this.navigatorWidget = new NavigatorWidget(this.zoomPane);
		}
		return this.navigatorWidget;
	}

	/**
	 * Returns the editor's layer (or stages) widget.
	 *
	 * @return a layer/stages widget.
	 */
	public LayersWidget layersWidget() {
		if (this.layersWidget == null) {
			this.layersWidget = new LayersWidget(handler);
			this.layersWidget.setRoot(this.rootLayer.getTreeItem());
		}
		return this.layersWidget;
	}

	private void expandLayers() {
		if (this.layersWidget != null) {
			this.layersWidget.expandAll();
		}
	}

}
