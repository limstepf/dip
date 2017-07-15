package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * An XOR parameter. Offers different parameters to choose one from to define
 * the actual parameter. For example a parameter to select some automatic option
 * and alternatively a slider for manual input could be setup in this way.
 *
 * All (sub-)parameters are (re-)stored too, even if not selected, and referred
 * to by index (w.r.t. the given list of parameters).
 */
public class XorParameter extends CompositeBase<ValueListSelection, XorParameter.XorView> {

	protected final List<Parameter<?>> children;

	/**
	 * Creates an XOR parameter. Default selection is 0 (the first option).
	 *
	 * @param label label.
	 * @param parameters list of parameters to choose one from.
	 */
	public XorParameter(String label, List<Parameter<?>> parameters) {
		this(label, parameters, 0);
	}

	/**
	 * Creates an XOR parameter.
	 *
	 * @param label label.
	 * @param parameters list of parameters to choose one from.
	 * @param defaultSelection index of the default parameter to be
	 * selected/used.
	 */
	public XorParameter(String label, List<Parameter<?>> parameters, int defaultSelection) {
		super(label, initValue(parameters, defaultSelection), initValue(parameters, defaultSelection));

		this.children = parameters;

		addChildListeners(Parameter.filterPersistent(this.children));
	}

	// yes, we do this twice to not override the default later on...
	protected static ValueListSelection initValue(List<Parameter<?>> parameters, int defaultSelection) {
		final List<Object> defaultValues = new ArrayList<>();
		for (Parameter<?> p : parameters) {
			if (p.isPersistent()) {
				final PersistentParameter<?> pp = (PersistentParameter<?>) p;
				defaultValues.add(pp.defaultValue());
			} else {
				defaultValues.add(true);
			}
		}
		return new ValueListSelection(defaultValues, defaultSelection);
	}

	/**
	 * Selects a parameter by index.
	 *
	 * @param index the index of the parameter.
	 */
	public void setSelection(int index) {
		final ValueListSelection val = get();
		if (val.getSelectedIndex() == index) { // already selected
			return;
		}
		val.setSelection(index);
		if (val.getSelectedIndex() == index) { // might have failed if out of bounds
			this.valueProperty.invalidate();
			if (!changeIsLocal && view != null) {
				view.set(get());
			}
		}
	}

	@Override
	protected Collection<? extends Parameter<?>> getChildren() {
		return this.children;
	}

	@Override
	protected boolean hasPersistentChildren() {
		return true;
	}

	@Override
	protected void invalidateChildParameter(PersistentParameter<?> p) {
		final ValueListSelection vs = get();
		for (int i = 0; i < this.children.size(); i++) {
			final Parameter<?> pi = this.children.get(i);
			if (p.equals(pi)) {
				vs.list.set(i, p.get());
				break;
			}
		}
		this.valueProperty.invalidate();
	}

	@Override
	protected ValueListSelection filterValueProperty(ValueListSelection value) {
		enableChildListeners(false);

		for (int i = 0; i < this.children.size(); i++) {
			final Parameter<?> p = this.children.get(i);
			if (p.isPersistent()) {
				p.asPersitentParameter().setRaw(value.get(i));
			}
		}

		return value;
	}

	@Override
	protected XorView newViewInstance() {
		return new XorView(this);
	}

	/**
	 * XOR item/option view.
	 *
	 * @param <T> class of the item/parameter view.
	 */
	public static class XorViewItem<T extends Parameter.View> {

		/**
		 * The view of the item.
		 */
		public final T view;

		/**
		 * The radio button of the item.
		 */
		public final RadioButton radio;

		/**
		 * The parent node.
		 */
		public final Parent parent;

		/**
		 * Creates a new XOR view item/option.
		 *
		 * @param index the index in the toggle group.
		 * @param parameter the parameter.
		 * @param parent the parent node.
		 * @param toggleGroup the toggle group.
		 */
		@SuppressWarnings("unchecked")
		public XorViewItem(int index, Parameter<?> parameter, Parent parent, ToggleGroup toggleGroup) {
			this.view = (T) parameter.view();
			this.radio = new RadioButton();
			this.radio.setToggleGroup(toggleGroup);
			this.radio.setUserData(index);
			this.parent = parent;
		}
	}

	/**
	 * The XOR view.
	 */
	public static class XorView extends PersistentParameterBase.ParameterViewBase<XorParameter, ValueListSelection, VBox> {

		private final ToggleGroup toggleGroup = new ToggleGroup();
		private final List<XorViewItem<?>> items;

		/**
		 * Creates a new XOR view.
		 *
		 * @param parameter the XOR parameter.
		 */
		public XorView(XorParameter parameter) {
			super(parameter, new VBox());

			this.items = new ArrayList<>();
			initItems();
			set(parameter.get());
			updateItems();

			// listen to xor selection
			toggleGroup.selectedToggleProperty().addListener((obs) -> {
				final ValueListSelection v = parameter.get();
				v.selection = (int) toggleGroup.getSelectedToggle().getUserData();
				parameter.valueProperty.invalidate();
				updateItems();
			});
		}

		private void updateItems() {
			for (XorViewItem<?> item : this.items) {
				if (item.radio.isSelected()) {
					item.parent.getStyleClass().removeAll("dip-disabled");
				} else {
					item.parent.getStyleClass().add("dip-disabled");
				}
			}
		}

		private void initItems() {
			int i = 0;
			for (Parameter<?> p : this.parameter.children) {
				final int index = i++;
				final BorderPane pane = new BorderPane();
				final VBox box = new VBox();
				pane.setPadding(new Insets(0, 0, 5, 0));
				box.setPadding(new Insets(0, 0, 5, 5));

				if (p.isPersistent()) {
					final XorViewItem<PersistentParameter.View<?>> item = new XorViewItem<>(index, p, box, this.toggleGroup);
					item.view.parameter().property().addListener((obs) -> {
						parameter.get().list.set(index, item.view.parameter().get());
						if (item.radio.isSelected()) {
							parameter.valueProperty.invalidate();
						} else {
							item.radio.setSelected(true);
						}
					});
					final PersistentParameter<?> pp = p.asPersitentParameter();
					if (!pp.label().equals("")) {
						box.getChildren().add(new Label(pp.label()));
					}
					addItem(pane, box, item);
				} else {
					final XorViewItem<Parameter.View> item = new XorViewItem<>(index, p, box, this.toggleGroup);
					this.items.add(item);
					p.view().node().setOnMouseClicked((e) -> {
						item.radio.setSelected(true);
					});
					addItem(pane, box, item);
				}
			}
		}

		private void addItem(BorderPane pane, VBox box, XorViewItem<?> item) {
			this.items.add(item);

			box.getChildren().add(item.view.node());
			pane.setLeft(item.radio);
			pane.setCenter(box);
			root.getChildren().add(pane);
		}

		@Override
		@SuppressWarnings({"rawtypes", "unchecked"})
		public final void set(ValueListSelection value) {
			for (int i = 0; i < value.list.size(); i++) {
				final XorViewItem item = this.items.get(i);
				if (item.view instanceof PersistentParameter.View) {
					final XorViewItem<PersistentParameter.View<?>> pitem = item;
					pitem.view.parameter().setRaw(value.list.get(i));
				}
			}
			this.items.get(value.selection).radio.setSelected(true);
		}

	}

}
