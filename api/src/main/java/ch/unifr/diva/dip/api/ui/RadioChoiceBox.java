package ch.unifr.diva.dip.api.ui;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * A radio choice box. Takes arbitrary JavaFX nodes as choices (typically a
 * {@code Label} or {@code Text} node). Note, however, that all children are of
 * the type {@code RadioChoice} (which encapsulates such arbitrary nodes, as
 * well as the associated radio button).
 *
 */
public class RadioChoiceBox extends VBox {

	final static protected Insets insets = new Insets(0, 5, 0, 0);
	final protected ToggleGroup toggleGroup;

	/**
	 * Creates a new radio choice box.
	 *
	 * @param nodes the choices.
	 */
	@SuppressWarnings({"unchecked", "varargs"})
	public RadioChoiceBox(Node... nodes) {
		this(new ToggleGroup(), toRadioChoices(nodes));
	}

	@SuppressWarnings({"rawtypes", "unchecked", "varargs"})
	private static <T extends Node> RadioChoice<T>[] toRadioChoices(T... nodes) {
		final RadioChoice<T>[] choices = new RadioChoice[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			choices[i] = new RadioChoice<>(nodes[i]);
		}
		return choices;
	}

	/**
	 * Creates a new radio choice box.
	 *
	 * @param choices the choices.
	 */
	@SafeVarargs
	public RadioChoiceBox(RadioChoice<Node>... choices) {
		this(new ToggleGroup(), choices);
	}

	@SafeVarargs
	protected RadioChoiceBox(ToggleGroup toggleGroup, RadioChoice<Node>... choices) {
		this.toggleGroup = toggleGroup;
		for (RadioChoice<Node> c : choices) {
			add(c);
		}

		selectedToggleProperty().addListener((c) -> updateChoices());

		if (choices.length > 0) {
			this.toggleGroup.selectToggle(choices[0].radio);
		}

		this.setSpacing(5);
	}

	// visually disable unselected choices
	@SuppressWarnings("unchecked")
	private void updateChoices() {
		for (Node n : this.getChildren()) {
			final RadioChoice<Node> choice = (RadioChoice<Node>) n;
			if (choice.radio.isSelected()) {
				choice.node.getStyleClass().removeAll("dip-disabled");
			} else {
				choice.node.getStyleClass().add("dip-disabled");
			}
		}
	}

	/**
	 * Adds a radio choice.
	 *
	 * @param node the choice. Will be auto-wrapped by a {@code RadioChoice}
	 * object.
	 * @return the {@code RadioChoice} object wrapping the given choice.
	 */
	public RadioChoice<Node> add(Node node) {
		final RadioChoice<Node> choice = new RadioChoice<>(node);
		add(choice);
		return choice;
	}

	/**
	 * Removes a radio choice.
	 *
	 * @param node the node (or body) of the {@code RadioChoice}.
	 * @return the removed radio choice, or {@code null} if not found.
	 */
	public RadioChoice<Node> remove(Node node) {
		final RadioChoice<Node> choice = getRadioChoice(node);
		if (choice != null) {
			remove(choice);
		}
		return choice;
	}

	/**
	 * Adds a radio choice.
	 *
	 * @param choice the new radio choice.
	 */
	final public void add(RadioChoice<Node> choice) {
		choice.init(this.toggleGroup);
		this.getChildren().add(choice);
	}

	/**
	 * Removes a radio choice.
	 *
	 * @param choice the radio choice to be removed.
	 */
	final public void remove(RadioChoice<Node> choice) {
		this.getChildren().remove(choice);
		choice.deinit();
	}

	/**
	 * Returns a radio choice by index.
	 *
	 * @param index the index of the radio choice.
	 * @return the radio choice.
	 */
	@SuppressWarnings("unchecked")
	public RadioChoice<Node> get(int index) {
		return (RadioChoice<Node>) this.getChildren().get(index);
	}

	/**
	 * Returns the selected toggle property. The {@code RadioChoice} is set on
	 * the user data of this toggle.
	 *
	 * @return the selected toggle property.
	 */
	public final ReadOnlyObjectProperty<Toggle> selectedToggleProperty() {
		return toggleGroup.selectedToggleProperty();
	}

	/**
	 * Returns the selected radio choice/item.
	 *
	 * @return the selected radio choice/item, or {@code null} if none is
	 * selected.
	 */
	@SuppressWarnings("unchecked")
	public RadioChoice<Node> selectedRadioChoice() {
		final Toggle toggle = selectedToggleProperty().get();
		if (toggle == null) {
			return null;
		}
		return (RadioChoice<Node>) toggle.getUserData();
	}

	/**
	 * Selects a radio choice.
	 *
	 * @param choice the radio choice.
	 */
	public void selectRadioChoice(RadioChoice<Node> choice) {
		choice.radio.setSelected(true);
	}

	/**
	 * Selects a radio choice by it's node (or body).
	 *
	 * @param <T> type of the node used as body of the choice.
	 * @param node the node (or body) of the choice.
	 */
	public <T extends Node> void selectNode(T node) {
		final RadioChoice<T> choice = getRadioChoice(node);
		if (choice != null) {
			choice.radio.setSelected(true);
		}
	}

	/**
	 * Returns/finds the radio choice by it's node (or body).
	 *
	 * @param <T> type of the node used as body of the choice.
	 * @param node the node (or body) of the choice.
	 * @return the radio choice (wrapping the given node), or {@code null} if
	 * not found.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Node> RadioChoice<T> getRadioChoice(T node) {
		for (Node n : this.getChildren()) {
			final RadioChoice<T> choice = (RadioChoice<T>) n;
			if (choice.node.equals(node)) {
				return choice;
			}
		}
		return null;
	}

	/**
	 * A radio choice/item.
	 *
	 * @param <T> type of the node used as body of the choice (typically a
	 * {@code Label} or {@code Text} node).
	 */
	public static class RadioChoice<T extends Node> extends BorderPane {

		public final T node;
		public final RadioButton radio;
		protected final EventHandler<Event> clickHandler;

		/**
		 * Creates a new radio choice/item.
		 *
		 * @param node body of the choice.
		 */
		public RadioChoice(T node) {
			this.node = node;
			this.radio = new RadioButton();
			this.clickHandler = (e) -> {
				radio.setSelected(true);
			};

			this.setMaxWidth(Double.MAX_VALUE);
			this.setLeft(this.radio);
			BorderPane.setAlignment(this.node, Pos.CENTER_LEFT);
			this.setCenter(this.node);
			this.setPadding(RadioChoiceBox.insets);
		}

		/**
		 * Inits the radio choice/item. The RadioChoiceBox calls this method as
		 * a parent of this choice. This also means that a radio choice can't be
		 * shared and only be attached to a single RadioChoiceBox.
		 *
		 * @param toggleGroup the toggle group.
		 */
		protected void init(ToggleGroup toggleGroup) {
			this.radio.setUserData(this);
			this.radio.setToggleGroup(toggleGroup);
			this.node.addEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
		}

		/**
		 * Deinitializes the radio choice/item. This detaches the choice's radio
		 * button from the toggle group.
		 */
		protected void deinit() {
			this.node.removeEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
			final ToggleGroup t = this.radio.getToggleGroup();
			if (t != null) {
				t.getToggles().remove(this.radio);
			}
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName()
					+ "@" + Integer.toHexString(this.hashCode())
					+ "{"
					+ "node=" + this.node
					+ "}";
		}
	}

}
