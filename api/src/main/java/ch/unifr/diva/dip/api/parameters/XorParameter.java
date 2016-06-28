package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.ValueListSelection;
import java.awt.event.MouseEvent;
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
public class XorParameter extends CompositeBase<ValueListSelection> {

	// TODO: we might wanna offer an XorParameter backed by something like a
	// ValueSelection*Map*. See CompositeGrid/CompositeGridMap.
	private final List<Parameter> children;

	/**
	 * Creates an XOR parameter. Default selection is 0 (the first option).
	 *
	 * @param label label.
	 * @param parameters list of parameters to choose one from.
	 */
	public XorParameter(String label, List<Parameter> parameters) {
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
	public XorParameter(String label, List<Parameter> parameters, int defaultSelection) {
		super(label, initValue(parameters, defaultSelection), initValue(parameters, defaultSelection));

		this.children = parameters;
		addChildListeners(Parameter.filterPersistent(this.children));
	}

	// yes, we do this twice to not override the default later on...
	protected static ValueListSelection initValue(List<Parameter> parameters, int defaultSelection) {
		final List<Object> defaultValues = new ArrayList<>();
		for (Parameter p : parameters) {
			if (p.isPersistent()) {
				final PersistentParameter pp = (PersistentParameter) p;
				defaultValues.add(pp.defaultValue());
			} else {
				defaultValues.add(true);
			}
		}
		return new ValueListSelection(defaultValues, defaultSelection);
	}

	@Override
	protected Collection<? extends Parameter> getChildren() {
		return this.children;
	}

	@Override
	protected boolean hasPersistentChildren() {
		return true;
	}

	@Override
	public void set(ValueListSelection value) {
		final boolean invalidate = this.valueProperty.get().equals(value);
		this.valueProperty.set(value);
		if (invalidate) {
			this.valueProperty.invalidate();
		}
		if (view != null) {
			view.set(value);
		}
	}

	@Override
	protected PersistentParameter.View newViewInstance() {
		return new XorView(this);
	}

	/**
	 * XOR item/option view.
	 * @param <T>
	 */
	public static class XorViewItem<T extends Parameter.View> {

		public final T view;
		public final RadioButton radio;
		public final Parent parent;

		public XorViewItem(int index, Parameter parameter, Parent parent, ToggleGroup toggleGroup) {
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
	public static class XorView extends ParameterViewBase<XorParameter, ValueListSelection, VBox> {

		// TODO: implement XorGridView?
		private final ToggleGroup toggleGroup = new ToggleGroup();
		private final List<XorViewItem> items;

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
			for (XorViewItem item : this.items) {
				if (item.radio.isSelected()) {
					item.parent.getStyleClass().remove("dip-disabled");
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
				final XorViewItem item = new XorViewItem(index, p, box, this.toggleGroup);
				this.items.add(item);

				if (p.isPersistent()) {
					final XorViewItem<PersistentParameter.View> pitem = item;
					pitem.view.parameter().property().addListener((obs) -> {
						parameter.get().list.set(index, pitem.view.parameter().get());
						if (item.radio.isSelected()) {
							parameter.valueProperty.invalidate();
						} else {
							item.radio.setSelected(true);
						}
					});

					final PersistentParameter pp = (PersistentParameter) p;
					if (!pp.label().equals("")) {
						box.getChildren().add(new Label(pp.label()));
					}
				} else {
					p.view().node().setOnMouseClicked((e) -> {
						item.radio.setSelected(true);
					});
				}

				box.getChildren().add(item.view.node());
				pane.setLeft(item.radio);
				pane.setCenter(box);
				root.getChildren().add(pane);
			}
		}

		@Override
		public final void set(ValueListSelection value) {
			for (int i = 0; i < value.list.size(); i++) {
				final XorViewItem item = this.items.get(i);
				if (item.view instanceof PersistentParameter.View) {
					final XorViewItem<PersistentParameter.View> pitem = item;
					pitem.view.parameter().set(value.list.get(i));
				}
			}
			this.items.get(value.selection).radio.setSelected(true);
		}
	}

}
