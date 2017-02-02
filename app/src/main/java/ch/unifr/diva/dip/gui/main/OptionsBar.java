package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.api.parameters.PersistentParameter;
import ch.unifr.diva.dip.api.parameters.SingleRowParameter;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.Presenter;
import ch.unifr.diva.dip.gui.VisibilityMode;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * The options bar. The options bar is closely tied to the tool bar, by
 * providing options (single row parameters) for these tools. The options bar is
 * build from a set of options from the editable processor itself (those options
 * appear for all tools), and from a set of options from the selected tool.
 */
public class OptionsBar implements Presenter {

	/**
	 * The preferred height of the options bar.
	 */
	public final static double OPTIONS_BAR_HEIGHT = 32;

	private final ApplicationHandler handler;
	private final HBox bar;
	private final Insets padding;

	/**
	 * Creates a new options bar.
	 *
	 * @param handler the application handler.
	 */
	public OptionsBar(ApplicationHandler handler) {
		this.handler = handler;
		this.bar = new HBox();
		bar.getStyleClass().add("dip-options-bar");
		bar.setAlignment(Pos.CENTER_LEFT);

		this.padding = new Insets(
				2,
				UIStrategyGUI.Stage.insets,
				2,
				UIStrategyGUI.Stage.insets
		);
	}

	private VisibilityMode readVisibilityMode() {
		return VisibilityMode.get(
				this.handler.settings.primaryStage.optionsBarVisibility.get()
		);
	}

	/**
	 * Clears the options bar.
	 */
	public void clear() {
		bar.getChildren().clear();
		bar.setPadding(Insets.EMPTY);
		bar.setPrefHeight(0);
	}

	/**
	 * (Re-)builds the options bar.
	 *
	 * @param optionMaps option maps, usually one of the processor, and another
	 * from the selected tool. May be null.
	 */
	public void build(Map<String, SingleRowParameter>... optionMaps) {
		clear();

		final VisibilityMode mode = readVisibilityMode();
		if (mode.equals(VisibilityMode.NEVER)) {
			return;
		}

		for (Map<String, SingleRowParameter> map : optionMaps) {
			if (map != null) {
				add(map, false);
			}
		}

		setPadding(mode);
	}

	/**
	 * Adds options to the options bar.
	 *
	 * @param options an option map.
	 */
	public void add(Map<String, SingleRowParameter> options) {
		add(options, true);
	}

	private void add(Map<String, SingleRowParameter> options, boolean setPadding) {
		for (Map.Entry<String, SingleRowParameter> opt : options.entrySet()) {
			add(opt.getKey(), opt.getValue(), false);
		}

		if (setPadding) {
			setPadding();
		}
	}

	/**
	 * Adds a single option to the options bar.
	 *
	 * @param key key of the option.
	 * @param option the option.
	 */
	public void add(String key, SingleRowParameter option) {
		add(key, option, true);
	}

	private void add(String key, SingleRowParameter option, boolean setPadding) {
		if (option.isPersistent()) {
			final PersistentParameter pp = (PersistentParameter) option;
			if (!pp.label().isEmpty()) {
				final Label label = newLabel(pp.label() + ": ");
				bar.getChildren().add(label);
			}
		}
		option.initSingleRowView();
		bar.getChildren().addAll(
				option.view().node(),
				newFiller()
		);

		if (setPadding) {
			setPadding();
		}
	}

	private void setPadding() {
		setPadding(VisibilityMode.AUTO);
	}

	private void setPadding(VisibilityMode mode) {
		if (!bar.getChildren().isEmpty() || mode.equals(VisibilityMode.ALWAYS)) {
			bar.setPadding(padding);
			bar.setPrefHeight(OPTIONS_BAR_HEIGHT);
		}
	}

	private Label newLabel(String text) {
		final Label label = new Label(text);
		label.getStyleClass().add("dip-small");
		return label;
	}

	private Region newFiller() {
		final Region r = new Region();
		r.setMinWidth(UIStrategyGUI.Stage.insets);
		return r;
	}

	@Override
	public Parent getComponent() {
		return bar;
	}

}
