package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.Widget;
import ch.unifr.diva.dip.utils.FxUtils;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Simple side bar. Offers an auxiliary {@code TabPane} at the top with constant
 * height (optional), and a main {@code TabPane} that takes the remaining height
 * of the side bar.
 */
public class SideBarPresenter implements Presenter {

	private final BorderPane root = new BorderPane();
	private TabPane auxTabPane;
	private TabPane mainTabPane;

	/**
	 * Creates a new side bar.
	 */
	public SideBarPresenter() {
		FxUtils.expandInRegion(root);
		root.getStyleClass().add("dip-side-bar");
	}

	@Override
	public Parent getComponent() {
		return root;
	}

	private TabPane auxTabPane() {
		if (this.auxTabPane == null) {
			this.auxTabPane = getNewTabPane();
			root.setTop(this.auxTabPane);
		}
		return this.auxTabPane;
	}

	private TabPane mainTabPane() {
		if (this.mainTabPane == null) {
			this.mainTabPane = getNewTabPane();
			root.setCenter(this.mainTabPane);
		}
		return this.mainTabPane;
	}

	private TabPane getNewTabPane() {
		final TabPane tp = new TabPane();
		tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		return tp;
	}

	/**
	 * Adds a widget to the main {@code TabPabe} of the side bar.
	 *
	 * @param widget a widget.
	 */
	public void addMainWidget(Widget widget) {
		addWidget(widget, this.mainTabPane());
	}

	/**
	 * Adds a widget to the auxiliary {@code TabPabe} of the side bar.
	 *
	 * @param widget a widget.
	 */
	public void addAuxiliaryWidget(Widget widget) {
		addWidget(widget, this.auxTabPane());
	}

	private Tab addWidget(Widget widget, TabPane dst) {
		final Tab tab = new Tab(widget.getTitle(), widget.getComponent());
		dst.getTabs().add(tab);
		return tab;
	}
}
