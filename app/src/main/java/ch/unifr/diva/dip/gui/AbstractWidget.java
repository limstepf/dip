package ch.unifr.diva.dip.gui;

import ch.unifr.diva.dip.core.ui.Localizable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * AbstractWidget.
 */
public abstract class AbstractWidget implements Widget, Localizable {

	private final BorderPane parent = new BorderPane();
	private String title;

	/**
	 * Default constructor.
	 */
	public AbstractWidget() {
		this("");
	}

	/**
	 * Default constructor.
	 *
	 * @param title title of the widget.
	 */
	public AbstractWidget(String title) {
		this.title = title;
		parent.getStyleClass().add("dip-widget");
		parent.setMinHeight(150);
		parent.setMaxHeight(Double.MAX_VALUE);
	}

	/**
	 * Sets the title of the widget.
	 *
	 * @param title the title.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets the main node/component.
	 *
	 * @param widget the main node/component.
	 */
	public void setWidget(Node widget) {
		parent.setCenter(widget);
	}

	@Override
	public void setHandle(Node handle) {
		parent.setBottom(handle);
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Parent getComponent() {
		return parent;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "label=" + this.getTitle()
				+ "}";
	}

}
