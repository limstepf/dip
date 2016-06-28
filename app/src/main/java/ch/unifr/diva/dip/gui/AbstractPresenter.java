package ch.unifr.diva.dip.gui;

import ch.unifr.diva.dip.core.ui.Localizable;
import javafx.scene.Parent;

/**
 * The AbstractPresenter already implements the required interface and some
 * convenience methods.
 *
 * @param <V> A view interface.
 */
public class AbstractPresenter<V extends View> implements Presenter, Localizable {

	protected final V view;

	public AbstractPresenter(V view) {
		super();
		this.view = view;
	}

	@Override
	public Parent getComponent() {
		return view.getComponent();
	}
}
