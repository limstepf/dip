package ch.unifr.diva.dip.gui.layout;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * A padded region. Adds padding around a region while redispatching events with
 * the padded (or empty) area as target to some redispatch node.
 *
 * <h4>Padding</h4>
 * The child-region (or content node) of the padded region can be padded with
 * constant/fixed padding for each side, which applies in case the child-region
 * is larger than the padded region itself, which expands to take all available
 * space in order to catch all (mouse) events in the padded (or empty) area.
 *
 * <p>
 * With a smaller child-region, the distribution of the extra padded (or empty)
 * space can be controlled with the {@code alignmentProperty()}, which defaults
 * to center the child-region in the padded-region. There are read-only
 * properties to query the real padding (fixed + extra padding) of each side.
 *
 * <h4>Event redispatching</h4>
 * Events with the padded area (i.e. not the content region) as target are
 * filtered and redispatched to a redispatch node (usually the content the
 * child-region/node of the padded region, i.e. the {@code contentProperty()}).
 * The effect is that events triggered in the padded area of this pane also fire
 * on that redispatch node, and with adjusted (event/mouse) coordinates relative
 * to that redispatch node. Events originating from/inside the content region
 * aren't affected by this filter.
 *
 * <p>
 * For example an event to the top-left of the content region (assuming there is
 * some top-left padding) will have negative x and y-coordinates, and one to the
 * bottom-right x and y-coordinates will exceed the size of the content region.
 *
 * <p>
 * {@code MOUSE_ENTERED} and {@code MOUSE_EXITED} events for the padded region
 * are redispatched to the redispatch node, whose own entered and exited events
 * are suppressed/consumed. The effect of this is that entered and exited events
 * fire already upon entering/exiting the padded region, but not a second time
 * for entering/exiting the inner redispatch node.
 */
public class PaddedRegion extends Region {

	/**
	 * Creates a new padded region.
	 */
	public PaddedRegion() {
		this(null);
	}

	/**
	 * Creates a new padded region.
	 *
	 * @param content initial node/child of the padded region.
	 */
	public PaddedRegion(Node content) {
		super();

		// redispatch event filter
		addEventFilter(Event.ANY, (e) -> {
			final Node node = getRedispatchTarget();
			if (node == null) {
				return;
			}

			if (this.equals(e.getTarget())) {
				// redispatch events for the padded region (i.e. this) to the
				// redispatch target (node)
				final Event copy = e.copyFor(node, node);
				node.fireEvent(copy);
				e.consume();
			} else if (node.equals(e.getTarget())) {
				// swallow the entered-/exited target events, that is, the entered/
				// exited events for the redispatch target wont fire. If we wouldn't
				// do this, we'd end up with two sets of entered/exited events on
				// the redispatch target: one for entering/exiting the padded region
				// (redispatched), and a second time for entering/exiting the re-
				// dispatch target inside the padded region.
				//
				// N.B. the difference between the MOUSE_X and MOUSE_X_TARGET events
				// is that the latter are delivered to the parent node of the target
				// before the MOUSE_X is delivered to the child (i.e. the target).
				if (e.getEventType().equals(MouseEvent.MOUSE_ENTERED_TARGET)
						|| e.getEventType().equals(MouseEvent.MOUSE_EXITED_TARGET)) {
					e.consume();
				}
			}
		});

		setContent(content);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ "content=" + getContent()
				+ ", redispatch target=" + getRedispatchTarget()
				+ ", alignment=" + getAlignment()
				+ String.format(
						", padding={%.2f (%.2f), %.2f (%.2f), %.2f (%.2f), %.2f (%.2f)}",
						getTopPadding(), getTopPaddingReal(),
						getRightPadding(), getRightPaddingReal(),
						getBottomPadding(), getBottomPaddingReal(),
						getLeftPadding(), getLeftPaddingReal()
				)
				+ "}";
	}

	// object property that request(s)Layout on invalidation
	protected class LayoutObjectProperty<T> extends SimpleObjectProperty<T> {

		public LayoutObjectProperty() {
			super();
		}

		public LayoutObjectProperty(T obj) {
			super(obj);
		}

		@Override
		public void invalidated() {
			super.invalidated();
			requestLayout();
		}
	}

	// double property that request(s)Layout on invalidation
	protected class LayoutDoubleProperty extends SimpleDoubleProperty {

		@Override
		public void invalidated() {
			super.invalidated();
			requestLayout();
		}
	}

	protected DoubleProperty topPaddingProperty;

	/**
	 * The top padding property.
	 *
	 * @return top padding property.
	 */
	public DoubleProperty topPaddingProperty() {
		if (topPaddingProperty == null) {
			topPaddingProperty = new LayoutDoubleProperty();
		}
		return topPaddingProperty;
	}

	/**
	 * Returns the top padding.
	 *
	 * @return the top padding.
	 */
	public double getTopPadding() {
		return (topPaddingProperty == null) ? 0 : topPaddingProperty.get();
	}

	protected DoubleProperty leftPaddingProperty;

	/**
	 * The left padding property.
	 *
	 * @return left padding property.
	 */
	public DoubleProperty leftPaddingProperty() {
		if (leftPaddingProperty == null) {
			leftPaddingProperty = new LayoutDoubleProperty();
		}
		return leftPaddingProperty;
	}

	/**
	 * Returns the left padding.
	 *
	 * @return the left padding.
	 */
	public double getLeftPadding() {
		return (leftPaddingProperty == null) ? 0 : leftPaddingProperty.get();
	}

	protected DoubleProperty bottomPaddingProperty;

	/**
	 * The bottom padding property.
	 *
	 * @return bottom padding property.
	 */
	public DoubleProperty bottomPaddingProperty() {
		if (bottomPaddingProperty == null) {
			bottomPaddingProperty = new LayoutDoubleProperty();
		}
		return bottomPaddingProperty;
	}

	/**
	 * Returns the bottom padding.
	 *
	 * @return the bottom padding.
	 */
	public double getBottomPadding() {
		return (bottomPaddingProperty == null) ? 0 : bottomPaddingProperty.get();
	}

	protected DoubleProperty rightPaddingProperty;

	/**
	 * The top padding property.
	 *
	 * @return top padding property.
	 */
	public DoubleProperty rightPaddingProperty() {
		if (rightPaddingProperty == null) {
			rightPaddingProperty = new LayoutDoubleProperty();
		}
		return rightPaddingProperty;
	}

	/**
	 * Returns the right padding.
	 *
	 * @return the right padding.
	 */
	public double getRightPadding() {
		return (rightPaddingProperty == null) ? 0 : rightPaddingProperty.get();
	}

	/**
	 * Sets the same padding for all sides.
	 *
	 * @param padding the padding for all sides.
	 */
	public void setPadding(double padding) {
		setPadding(padding, padding, padding, padding);
	}

	/**
	 * Sets a vertical and horizontal padding.
	 *
	 * @param vertical the top and bottom padding.
	 * @param horizontal the left and right padding.
	 */
	public void setPadding(double vertical, double horizontal) {
		setPadding(vertical, horizontal, vertical, horizontal);
	}

	/**
	 * Sets individual padding.
	 *
	 * @param top the top padding.
	 * @param left the left padding.
	 * @param bottom the bottom padding.
	 * @param right the right padding.
	 */
	public void setPadding(double top, double left, double bottom, double right) {
		topPaddingProperty().set(top);
		leftPaddingProperty().set(left);
		bottomPaddingProperty().set(bottom);
		rightPaddingProperty().set(right);
	}

	// we can end up with extra padding in case the padded region is set/bound
	// to some minimum bounds.
	protected DoubleProperty topPaddingRealProperty = new SimpleDoubleProperty(0);
	protected DoubleProperty rightPaddingRealProperty = new SimpleDoubleProperty(0);
	protected DoubleProperty bottomPaddingRealProperty = new SimpleDoubleProperty(0);
	protected DoubleProperty leftPaddingRealProperty = new SimpleDoubleProperty(0);

	/**
	 * Real top padding property. This value might be larger than the set
	 * padding if the padded region is set/bound to some minimum bounds.
	 *
	 * @return the real top padding property.
	 */
	public ReadOnlyDoubleProperty topPaddingRealProperty() {
		return topPaddingRealProperty;
	}

	/**
	 * Returns the real top padding.
	 *
	 * @return the real top padding.
	 */
	public double getTopPaddingReal() {
		return topPaddingRealProperty().get();
	}

	/**
	 * Real right padding property. This value might be larger than the set
	 * padding if the padded region is set/bound to some minimum bounds.
	 *
	 * @return the real right padding property.
	 */
	public ReadOnlyDoubleProperty rightPaddingRealProperty() {
		return rightPaddingRealProperty;
	}

	/**
	 * Returns the real right padding.
	 *
	 * @return the real right padding.
	 */
	public double getRightPaddingReal() {
		return rightPaddingRealProperty().get();
	}

	/**
	 * Real bottom padding property. This value might be larger than the set
	 * padding if the padded region is set/bound to some minimum bounds.
	 *
	 * @return the real bottom padding property.
	 */
	public ReadOnlyDoubleProperty bottomPaddingRealProperty() {
		return bottomPaddingRealProperty;
	}

	/**
	 * Returns the real bottom padding.
	 *
	 * @return the real bottom padding.
	 */
	public double getBottomPaddingReal() {
		return bottomPaddingRealProperty().get();
	}

	/**
	 * Real left padding property. This value might be larger than the set
	 * padding if the padded region is set/bound to some minimum bounds.
	 *
	 * @return the real left padding property.
	 */
	public ReadOnlyDoubleProperty leftPaddingRealProperty() {
		return leftPaddingRealProperty;
	}

	/**
	 * Returns the real left padding.
	 *
	 * @return the real left padding.
	 */
	public double getLeftPaddingReal() {
		return leftPaddingRealProperty().get();
	}

	protected ObjectProperty<Pos> alignmentProperty;

	/**
	 * The alignment property. Used to distribute extra padding resulting from a
	 * larger viewport than the scaled content. Note that this only applies to
	 * the dynamic extra padding, not the fixed one.
	 *
	 * @return the alignment property.
	 */
	public ObjectProperty<Pos> alignmentProperty() {
		if (alignmentProperty == null) {
			alignmentProperty = new LayoutObjectProperty<>(Pos.CENTER);
		}
		return alignmentProperty;
	}

	/**
	 * Returns the alignment used to distribute extra padding.
	 *
	 * @return the alginment.
	 */
	public Pos getAlignment() {
		if (alignmentProperty == null) {
			return Pos.CENTER;
		}
		return alignmentProperty().get();
	}

	/**
	 * Sets the alignment used to distribute extra padding.
	 *
	 * @param value the alignment.
	 */
	public void setAlignment(Pos value) {
		alignmentProperty().set(value);
	}

	// distribute extra padding (due to min bounds) equally to both sides
	// (center region)
	protected void setExtraHorizontalPadding(double extra) {
		switch (getAlignment().getHpos()) {
			case LEFT:
				leftPaddingRealProperty.set(getLeftPadding());
				rightPaddingRealProperty.set(getRightPadding() + extra);
				break;
			default:
			case CENTER:
				final double v = extra / 2;
				leftPaddingRealProperty.set(getLeftPadding() + v);
				rightPaddingRealProperty.set(getRightPadding() + v);
				break;
			case RIGHT:
				leftPaddingRealProperty.set(getLeftPadding() + extra);
				rightPaddingRealProperty.set(getRightPadding());
				break;
		}
	}

	protected void setExtraVerticalPadding(double extra) {
		switch (getAlignment().getVpos()) {
			case TOP:
				topPaddingRealProperty.set(getTopPadding());
				bottomPaddingRealProperty.set(getBottomPadding() + extra);
				break;
			default:
			case BASELINE:
			case CENTER:
				final double v = extra / 2;
				topPaddingRealProperty.set(getTopPadding() + v);
				bottomPaddingRealProperty.set(getBottomPadding() + v);
				break;
			case BOTTOM:
				topPaddingRealProperty.set(getTopPadding() + extra);
				bottomPaddingRealProperty.set(getBottomPadding());
				break;
		}
	}

	protected ObjectProperty<Bounds> minBoundsProperty;

	/**
	 * The minimum bounds property. Intended to be bound to the viewport bounds
	 * property of a scrollpane, or similar. The effect of setting/binding this
	 * property is that the padding is allowed to grow as needed in order to
	 * fill the available space. Additional padding is distributed equally to
	 * top/bottom (or left/right) sides.
	 *
	 * @return the minimum bounds property.
	 */
	public final ObjectProperty<Bounds> minBoundsProperty() {
		if (minBoundsProperty == null) {
			minBoundsProperty = new LayoutObjectProperty<>();
		}
		return minBoundsProperty;
	}

	/**
	 * Sets the minimum bounds property.
	 *
	 * @param bounds the minimum bounds.
	 */
	public void setMinBounds(Bounds bounds) {
		minBoundsProperty().set(bounds);
	}

	/**
	 * Returns the minimum bounds.
	 *
	 * @return the minimum bounds.
	 */
	public Bounds getMinBounds() {
		return minBoundsProperty().get();
	}

	/**
	 * Checks whether there are minimum bounds.
	 *
	 * @return {@code true} if there are minimum bounds to be respected,
	 * {@code false} otherwise.
	 */
	public boolean hasMinBounds() {
		if (minBoundsProperty == null) {
			return false;
		}
		return getMinBounds() != null;
	}

	protected ObjectProperty<Node> contentProperty;

	/**
	 * The content property. This property holds the only child (or
	 * {@code null}) of the padded region.
	 *
	 * <p>
	 * Note that you should put a {@code Group} here in case transformations on
	 * the actual content node (now as child of that group) should be respected.
	 *
	 * @return the content property.
	 */
	public final ObjectProperty<Node> contentProperty() {
		if (contentProperty == null) {
			contentProperty = new LayoutObjectProperty<Node>() {
				@Override
				public void invalidated() {
					final Node node = getContent();
					if (node == null) {
						getChildren().clear();
					} else {
						getChildren().setAll(node);
					}
					super.invalidated();
				}
			};
		}
		return contentProperty;
	}

	/**
	 * Sets the content (or only child) of the padded region.
	 *
	 * @param node the new content node.
	 */
	public final void setContent(Node node) {
		contentProperty().set(node);
	}

	/**
	 * Returns the content node.
	 *
	 * @return the content node, or {@code null}.
	 */
	public final Node getContent() {
		return contentProperty().get();
	}

	/**
	 * Checks whether the padded region is empty.
	 *
	 * @return {@code true} if there is no child node set as content,
	 * {@code false} otherwise.
	 */
	public final boolean isEmpty() {
		if (contentProperty == null) {
			return true;
		}
		return getContent() == null;
	}

	protected ObjectProperty<Node> redispatchTargetProperty;

	/**
	 * The redispatch target property. This property holds the node for which
	 * all events in the padded region are being redispatched to. This is the
	 * node on the content property by default, but can be any descendant node.
	 *
	 * <p>
	 * Note that the redispatched events are copied for the redispatch target
	 * and it's coordinate system. Event handlers should only be attached to the
	 * redispatch target, and not on a parent node where these redispatched
	 * events also pass through (on the way from the padded region to the
	 * dispatch target, and back up...), since these redispatched events weren't
	 * copied for them ({@code e.copyFor(target, target)}), and thus the
	 * reported positions will be off (using the coordinate system of the actual
	 * redispatch target).
	 *
	 * @return the redispatch target property. If set to {@code null}, the
	 * redispatch filter will be disabled.
	 */
	public ObjectProperty<Node> redispatchTargetProperty() {
		if (redispatchTargetProperty == null) {
			redispatchTargetProperty = new SimpleObjectProperty<>();
		}
		return redispatchTargetProperty;
	}

	/**
	 * Sets a new redispatch target.
	 *
	 * @param node the redispatch target. Has to be a descendant node of the
	 * padded region/the content node.
	 */
	public void setRedispatchTarget(Node node) {
		redispatchTargetProperty().set(node);
	}

	/**
	 * Returns the redispatch target.
	 *
	 * @return the redispatch target, or the node on the content property if not
	 * defined.
	 */
	public final Node getRedispatchTarget() {
		if (redispatchTargetProperty == null) {
			return getContent();
		}
		return redispatchTargetProperty.get();
	}

	protected double getContentWidth() {
		final Node content = getContent();
		if (content == null) {
			return 0;
		}
		return Math.max(content.minWidth(-1), content.prefWidth(-1));
	}

	protected double getContentHeight() {
		final Node content = getContent();
		if (content == null) {
			return 0;
		}
		return Math.max(content.minHeight(-1), content.prefHeight(-1));
	}

	@Override
	protected double computeMinWidth(double height) {
		return computePrefWidth(height);
	}

	@Override
	protected double computePrefWidth(double height) {
		final double width = getContentWidth();
		final double totalWidth = getLeftPadding() + width + getRightPadding();

		if (hasMinBounds()) {
			final Bounds minBounds = getMinBounds();
			final double resizeWidth;
			if (totalWidth < minBounds.getWidth()) {
				setExtraHorizontalPadding(minBounds.getWidth() - totalWidth);
				return minBounds.getWidth();
			}
		}

		setExtraHorizontalPadding(0);
		return totalWidth;
	}

	@Override
	protected double computeMinHeight(double width) {
		return computePrefHeight(width);
	}

	@Override
	protected double computePrefHeight(double width) {
		final double height = getContentHeight();
		final double totalHeight = getTopPadding() + height + getBottomPadding();

		if (hasMinBounds()) {
			final Bounds minBounds = getMinBounds();
			if (totalHeight < minBounds.getHeight()) {
				setExtraVerticalPadding(minBounds.getHeight() - totalHeight);
				return minBounds.getHeight();
			}
		}

		setExtraVerticalPadding(0);
		return totalHeight;
	}

	@Override
	protected void layoutChildren() {
		if (isEmpty()) {
			return;
		}

		final Node node = getContent();
		final double width = getContentWidth();
		final double height = getContentHeight();
		final double leftPadding = leftPaddingRealProperty.get();
		final double topPadding = topPaddingRealProperty.get();

		if (node.isResizable() && node.isManaged()) {
			node.resize(width, height);
		}

		node.relocate(
				snapPosition(leftPadding),
				snapPosition(topPadding)
		);
	}

}
