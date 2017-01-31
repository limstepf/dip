package ch.unifr.diva.dip.api.ui;

import java.util.HashMap;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;

/**
 * A persistent toggle group for toggle buttons. Unlike the ordinary toggle
 * group, with this one a selected toggle can't be deselected by clicking it,
 * which would lead to a state where no toggle is selected.
 */
public class PersistentToggleButtonGroup extends ToggleGroup {

	// isn't this just a waste of memory? Do toggles ever get removed from
	// the group?
	protected final HashMap<ToggleButton, EventHandler<MouseEvent>> filters;

	/**
	 * Creates a new persistent toggle button group.
	 */
	public PersistentToggleButtonGroup() {
		super();
		this.filters = new HashMap<>();
		getToggles().addListener(new ListChangeListener<Toggle>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Toggle> c) {
				while (c.next()) {
					for (final Toggle a : c.getRemoved()) {
						final ToggleButton tba = (ToggleButton) a;
						final EventHandler<MouseEvent> filter = filters.remove(tba);
						tba.removeEventFilter(MouseEvent.MOUSE_RELEASED, filter);
					}
					for (final Toggle a : c.getAddedSubList()) {
						final EventHandler<MouseEvent> filter = (e) -> {
							if (a.equals(getSelectedToggle())) {
								e.consume();
							}
						};
						final ToggleButton tba = (ToggleButton) a;
						filters.put(tba, filter);
						tba.addEventFilter(MouseEvent.MOUSE_RELEASED, filter);
					}
				}
			}
		});
	}

}
