package ch.unifr.diva.dip.api.parameters;

import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * An empty parameter. Transient parameter displaying nothing. Might be usefull
 * to fill in some gaps in a composite view/parameter.
 */
public class EmptyParameter implements Parameter<Void> {

	private Parameter.View view;
	protected boolean isHidden;

	/**
	 * Creates an empty parameter.
	 */
	public EmptyParameter() {

	}

	@Override
	public void setHide(boolean hide) {
		this.isHidden = hide;
		if (view != null) {
			this.view.setHide(this.isHidden);
		}
	}

	@Override
	public View view() {
		if (view == null) {
			this.view = new EmptyView();
			this.view.setHide(this.isHidden);
		}
		return view;
	}

	/**
	 * An empty view.
	 */
	public static class EmptyView implements Parameter.View {

		private final Region region = new Region();

		@Override
		public Node node() {
			return region;
		}
	}

}
