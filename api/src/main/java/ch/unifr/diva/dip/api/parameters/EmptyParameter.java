package ch.unifr.diva.dip.api.parameters;

import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * An empty parameter. Transient parameter displaying nothing. Might be usefull
 * to fill in some gaps in a composite view/parameter.
 */
public class EmptyParameter implements Parameter<Void> {

	private EmptyView view;
	protected boolean isHidden;
	protected double minWidth;
	protected double minHeight;

	/**
	 * Creates an empty parameter.
	 */
	public EmptyParameter() {
		this(-1, -1);
	}

	/**
	 * Creates an empty parameter.
	 *
	 * @param minWidth the minimum width of the empty parameter's view/region.
	 * @param minHeight the minimum height of the empty parameter's view/region.
	 */
	public EmptyParameter(double minWidth, double minHeight) {
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}

	/**
	 * Sets a minimum width. By default the view/region of the empty parameter
	 * doesn't occupy any space. This allows the empty parameter to act as a
	 * spacer.
	 *
	 * @param minWidth the minimum width of the empty parameter's view/region.
	 */
	public void setMinWidth(double minWidth) {
		this.minWidth = minWidth;
	}

	/**
	 * Sets a minimum height. By default the view/region of the empty parameter
	 * doesn't occupy any space. This allows the empty parameter to act as a
	 * spacer.
	 *
	 * @param minHeight the minimum height of the empty parameter's view/region.
	 */
	public void setMinHeight(double minHeight) {
		this.minHeight = minHeight;
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
			view = new EmptyView();
			if (minWidth > 0) {
				view.setMinWidth(minWidth);
			}
			view.setHide(this.isHidden);
		}
		return view;
	}

	/**
	 * An empty view.
	 */
	public static class EmptyView implements Parameter.View {

		private final Region region = new Region();

		/**
		 * Sets the minimum width.
		 *
		 * @param minWidth the minimum width.
		 */
		public void setMinWidth(double minWidth) {
			region.setMinWidth(minWidth);
		}

		@Override
		public Node node() {
			return region;
		}

	}

}
