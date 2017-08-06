package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.components.ProcessorDocumentation;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.StructuredText;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.model.PrototypeProcessor;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.dialogs.AbstractDialog;
import ch.unifr.diva.dip.osgi.OSGiService;
import ch.unifr.diva.dip.osgi.ServiceCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Processor information dialog.
 *
 * @param <T> type of the processor wrapper.
 */
public class ProcessorInformationDialog<T extends PrototypeProcessor> extends AbstractDialog {

	public final static double PREF_DIALOG_WIDTH = 640;
	public final static double PREF_DIALOG_HEIGHT = 360;
	protected final ApplicationHandler handler;
	protected final T wrapper;
	private final Button ok;

	/**
	 * Creates a new processor information dialog.
	 *
	 * @param handler the application handler.
	 * @param wrapper the processor wrapper.
	 */
	public ProcessorInformationDialog(ApplicationHandler handler, T wrapper) {
		this(handler.uiStrategy.getStage(), handler, wrapper);
	}

	/**
	 * Creates a new processor information dialog.
	 *
	 * @param owner the window owner.
	 * @param handler the application handler.
	 * @param wrapper the processor wrapper.
	 */
	public ProcessorInformationDialog(Window owner, ApplicationHandler handler, T wrapper) {
		super(owner);
		setTitle(localize("processor.info"));

		this.handler = handler;
		this.wrapper = wrapper;

		final ServiceCollection<Processor> services = handler.osgi.getProcessorCollection(wrapper.pid());
		final OSGiService<Processor> service = (services == null) ? null : services.getService(wrapper.version());

		final ScrollPane scrollpane = new ScrollPane(getProcessorInformation(handler, wrapper, service));
		scrollpane.getStyleClass().add("edge-to-edge");
		scrollpane.setFitToWidth(true);

		root.setTop(getProcessorTitle(wrapper, service));
		root.setCenter(scrollpane);

		ok = getDefaultButton(localize("ok"));
		ok.setOnAction((c) -> onAction());
		buttons.add(ok);
		root.setPrefWidth(PREF_DIALOG_WIDTH);
		root.setPrefHeight(PREF_DIALOG_HEIGHT);

		attachCancelOnEscapeHandler();
	}

	private void onAction() {
		stage.close();
	}

	/**
	 * Creates a new processor information pane.
	 *
	 * @param <T> type of the processor wrapper.
	 * @param handler the application handler.
	 * @param wrapper the processor wrapper.
	 * @param service the OSGi service.
	 * @return the processor information pane.
	 */
	public final static <T extends PrototypeProcessor> VBox getProcessorInformation(ApplicationHandler handler, T wrapper, OSGiService<Processor> service) {
		final VBox root = new VBox();
		root.setMaxWidth(Double.MAX_VALUE);

		final ProcessorDocumentation doc = wrapper.getProcessorDocumentation();
		if (doc != null) {
			doc.setHostServices(handler.hostServices);
			root.getChildren().add(doc.getNode());
			final Region region = new Region();
			region.setMinHeight(45);
			root.getChildren().add(region);
		}

		root.getChildren().add(new ProcessorInformation<>(handler, wrapper, service));
		return root;
	}

	/**
	 * Creates a new processor title pane.
	 *
	 * @param <T> type of the processor wrapper.
	 * @param wrapper the processor wrapper.
	 * @param service the OSGi service.
	 * @return the processor title pane.
	 */
	public final static <T extends PrototypeProcessor> HBox getProcessorTitle(T wrapper, OSGiService<Processor> service) {
		final Processor p;
		if (service != null) {
			p = (service.serviceObject == null) ? wrapper.processor() : service.serviceObject;
		} else {
			p = wrapper.processor();
		}
		final Glyph glyph = UIStrategyGUI.Glyphs.newGlyph(wrapper.glyph(), Glyph.Size.NORMAL);
		final Label label = new Label(
				wrapper.isAvailable()
						? p.name()
						: wrapper.pid()
		);
		final HBox title = new HBox();
		title.setMaxWidth(Double.MAX_VALUE);
		title.setPadding(new Insets(0, 0, 10, 0));
		title.setSpacing(UIStrategyGUI.Stage.insets);
		title.setAlignment(Pos.CENTER_LEFT);
		title.getChildren().setAll(glyph, label);
		return title;
	}

	/**
	 * Processor information.
	 *
	 * @param <T> type of the processor wrapper.
	 */
	public static class ProcessorInformation<T extends PrototypeProcessor> extends VBox implements Localizable {

		/**
		 * Creates new processor information.
		 *
		 * @param handler the application handler.
		 * @param wrapper the processor wrapper.
		 * @param service the OSGi service.
		 */
		public ProcessorInformation(ApplicationHandler handler, T wrapper, OSGiService<Processor> service) {
			setSpacing(5);
			setMaxWidth(Double.MAX_VALUE);
			setMaxHeight(Double.MAX_VALUE);

			if (service != null) {
				final Map<Object, Object> basics = new LinkedHashMap<>();
				basics.put("PID", service.pid);
				basics.put("Bundle", service.symbolicBundleName);
				basics.put("Version", service.version.toString());

				final StructuredText basicsPane = StructuredText.smallDescriptionList(basics);
				getChildren().add(basicsPane);

				final Processor serviceObject = service.serviceObject;

				if (serviceObject != null) {
					final List<List<Object>> rows = new ArrayList<>();

					final Insets tinsets = new Insets(15, 0, 0, 0);
					final Label in = StructuredText.label(localize("processor.inputs"), true);
					in.setPadding(tinsets);
					rows.add(Arrays.asList(in));
					if (serviceObject.inputs().size() > 0) {
						for (Map.Entry<String, InputPort<?>> input : serviceObject.inputs().entrySet()) {
							final List<Object> row = new ArrayList<>();
							final InputPort<?> port = input.getValue();
							InputPort<?> portW = wrapper.processor().input(input.getKey());
							boolean visible = true;
							if (portW == null) {
								portW = port;
								visible = false;
							}
							row.add(newLabel(input.getKey(), visible));
							row.add(newLabel(port.getLabel(), visible));
							row.add(newLabel(
									port.getDataType().dataFormat().toString()
									+ "\n"
									+ port.getDataType().type().getCanonicalName(),
									visible
							));
							row.add(newLabel(portW.getPortState().name(), visible));
							row.add(newLabel(
									port.isRequired()
											? localize("required").toLowerCase()
											: localize("required.not").toLowerCase(),
									visible
							));

							rows.add(row);
						}
					} else {
						rows.add(Arrays.asList(StructuredText.label("-")));
					}

					final Label out = StructuredText.label(localize("processor.outputs"), true);
					out.setPadding(tinsets);
					rows.add(Arrays.asList(out));
					if (serviceObject.outputs().size() > 0) {
						for (Map.Entry<String, OutputPort<?>> output : serviceObject.outputs().entrySet()) {
							final List<Object> row = new ArrayList<>();
							final OutputPort<?> port = output.getValue();
							OutputPort<?> portW = wrapper.processor().output(output.getKey());
							boolean visible = true;
							if (portW == null) {
								portW = port;
								visible = false;
							}
							row.add(newLabel(output.getKey(), visible));
							row.add(newLabel(port.getLabel(), visible));
							row.add(newLabel(
									port.getDataType().dataFormat().toString()
									+ "\n"
									+ port.getDataType().type().getCanonicalName(),
									visible
							));
							row.add(newLabel(portW.getPortState().name(), visible));
							rows.add(row);
						}
					} else {
						rows.add(Arrays.asList(StructuredText.label("-")));
					}
					final StructuredText portPane = StructuredText.smallTable(null, rows, 5);
					getChildren().add(portPane);
				}
			} else {
				final Label unavailable = StructuredText.label(
						localize("available.not.x", localize("processor.info"))
				);
				getChildren().add(unavailable);
			}
		}

		protected static Label newLabel(String text) {
			return newLabel(text, true);
		}

		protected static Label newLabel(String text, boolean visible) {
			final Label label = StructuredText.label(text);
			if (!visible) {
				label.setOpacity(.6);
			}
			return label;
		}

	}

}
