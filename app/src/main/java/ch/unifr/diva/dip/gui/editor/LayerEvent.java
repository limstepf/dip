package ch.unifr.diva.dip.gui.editor;

/**
 * A Layer event. Light-weight events for the layer tree. Event paths can't be
 * influenced (e.g. consume()) and are determined by the event type (some bubble
 * up to the root layer, other go down to all layers below, ...).
 */
public class LayerEvent {

	/**
	 * Layer event types.
	 */
	public enum Type {

		/**
		 * A layer got modified. Bubbles up to the root layer. This event is
		 * also fired if the visibility of a layer changes.
		 */
		MODIFIED,
		/**
		 * A layer pane got modified. This event is manually triggered (from a
		 * layer user) after the content of a layer pane has been modified. As
		 * such, it acts like a {@code MODIFIED} event (to bubble up to the root
		 * layer and trigger the modified property).
		 */
		MODIFIED_PANE,
		/**
		 * The children of a layer group got modified, or the hide group status
		 * changed. Notifies the parent layer group to rebuild it's tree.
		 */
		MODIFIED_TREE,
		/**
		 * The empty property of a layer changed. Notifies the parent layer to
		 * reevaluate if it also can be considered empty now.
		 */
		MODIFIED_EMPTY,
		/**
		 * A parent layer turned invisible. Bubble down and make all layers
		 * below passively invisible (or "deactivated").
		 */
		DEACTIVATE,
		/**
		 * A passively invisible (or "deactivated") layer wants to be visible
		 * again. Bubble up until the subtree turns visible again, passing other
		 * passively invisible (or "deactivated") layers along the path.
		 */
		REACTIVATE_PARENT,
		/**
		 * Reactivate passively invisible layers. Bubbles down reactivating
		 * passively invisible (or "deactivated") layers, until a layer is
		 * (explicitly) invisible.
		 */
		REACTIVATE;
	}

	/**
	 * Type of the layer event.
	 */
	public final Type type;

	/**
	 * Creates a new layer event.
	 *
	 * @param type type of the layer event.
	 */
	public LayerEvent(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "type=" + type.name()
				+ "}";
	}

}
