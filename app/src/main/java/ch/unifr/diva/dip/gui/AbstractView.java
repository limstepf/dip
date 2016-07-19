package ch.unifr.diva.dip.gui;

import ch.unifr.diva.dip.core.ui.Localizable;
import javafx.scene.Parent;

/**
 * The AbstractView already implements the required interface and some
 * convenience methods.
 *
 * @param <P> The parent (or root) component of this view.
 */
public class AbstractView<P extends Parent> implements View, Localizable {

	protected final P root;

	/**
	 * AbstractView constructor.
	 *
	 * @param root the parent (or root) component of this view.
	 */
	public AbstractView(P root) {
		super();
		this.root = root;
	}

	@Override
	public Parent getComponent() {
		return root;
	}

}
