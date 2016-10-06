package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.Pipeline;
import ch.unifr.diva.dip.core.model.ProjectPage;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import ch.unifr.diva.dip.eventbus.events.ProjectNotification;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.layout.ZoomPane;
import com.google.common.eventbus.Subscribe;
import javafx.beans.InvalidationListener;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The EditorPresenter.
 */
public class EditorPresenter implements Presenter {

	private static final Logger log = LoggerFactory.getLogger(EditorPresenter.class);

	private final ApplicationHandler handler;

	private final LayerGroup rootLayer;
	private final ZoomPane zoomPane;
	private final InvalidationListener onModifiedListener;
	private NavigatorWidget navigatorWidget;
	private LayersWidget layersWidget;

	/**
	 * Creates a new editor presenter.
	 *
	 * @param handler the application handler.
	 */
	public EditorPresenter(ApplicationHandler handler) {
		this.handler = handler;

		this.rootLayer = new LayerGroup();
		this.zoomPane = new ZoomPane(handler.discardingThreadPool, this.rootLayer.getComponent());
		this.zoomPane.getStyleClass().add("dip-editor");
		this.zoomPane.setInterpolation(
				ZoomPane.Interpolation.get(handler.settings.editor.interpolation)
		);
		this.onModifiedListener = (c) -> {
			this.zoomPane.fireContentChange();
		};
		this.rootLayer.onModifiedProperty().addListener(this.onModifiedListener);
	}

	@Override
	public Parent getComponent() {
		return this.zoomPane;
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

	private void clear() {
		this.rootLayer.clear();
	}

// TODO: monitor processors in runnable pipelines too!
// -> listen to page.getPipeline().processors()
	private void onPageModified() {
		clear();
		buildStages();
		layersWidget().setRoot(this.rootLayer.getTreeItem());
	}

	private void onPageRemoved(int id) {
		clear();
	}

	private void onClosingProject() {
		clear();
	}

	/**
	 * Sets the interpolation type used for zooming/resampling.
	 *
	 * @param type the interpolation type.
	 */
	public void setInterpolation(ZoomPane.Interpolation type) {
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
			this.layersWidget = new LayersWidget();
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
