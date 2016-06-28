
package ch.unifr.diva.dip.api.parameters;

import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * An empty parameter. Transient parameter displaying nothing. Might be usefull
 * to fill in some gaps in a composite view/parameter.
 */
public class EmptyParameter implements Parameter {

	private Parameter.View view;

	/**
	 * Creates an empty parameter.
	 */
	public EmptyParameter() {

	}

	@Override
	public View view() {
		if (view == null) {
			this.view = new EmptyView();
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
