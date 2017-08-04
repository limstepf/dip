package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.ui.StructuredText;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.slf4j.LoggerFactory;

/**
 * Simple processor documentation. Optional/custom documentation utility class
 * to describe/document a processor.
 */
public class SimpleProcessorDocumentation implements ProcessorDocumentation {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleProcessorDocumentation.class);

	protected final VBox root;
	private HostServices hostServices;

	/**
	 * Creates new, custom processor documentation.
	 */
	public SimpleProcessorDocumentation() {
		this.root = new VBox();
		root.setMaxWidth(Double.MAX_VALUE);
		root.setSpacing(5);
	}

	@Override
	public void setHostServices(HostServices hostServices) {
		this.hostServices = hostServices;
	}

	@Override
	public Node getNode() {
		return root;
	}

	/**
	 * Adds a JavaFX node to the documentation.
	 *
	 * @param <T> type of the node.
	 * @param node the JavaFX node.
	 * @return the JavaFX node.
	 */
	public <T extends Node> T addNode(T node) {
		root.getChildren().add(node);
		return node;
	}

	/**
	 * Adds {@code StructuredText} to the documentation. This is similar to
	 * {@code addNode} but also adds insets/padding in order to indent the
	 * structured text.
	 *
	 * @param node the structured text.
	 * @return the structured text.
	 */
	public StructuredText addStructuredText(StructuredText node) {
		node.setPadding(new Insets(10, 25, 10, 25));
		return addNode(node);
	}

	/**
	 * Adds a label to the documentation.
	 *
	 * @param text the text.
	 * @return the label.
	 */
	public Label addLabel(String text) {
		return addLabel(text, false);
	}

	/**
	 * Adds a label to the documentation.
	 *
	 * @param text the text.
	 * @param bold {@code true} for a bold label, {@code false} for a regular
	 * one.
	 * @return the label.
	 */
	public Label addLabel(String text, boolean bold) {
		final Label node = StructuredText.label(text, bold);
		return addNode(node);
	}

	/**
	 * Adds a hyperlink to the documentation.
	 *
	 * @param url the url (also used as label).
	 * @return the hyperlink.
	 */
	public Hyperlink addHyperlink(String url) {
		return addHyperlink(url, url);
	}

	/**
	 * Adds a hyperlink to the documentation.
	 *
	 * @param label the label.
	 * @param url the url.
	 * @return the hyperlink.
	 */
	public Hyperlink addHyperlink(String label, String url) {
		return addNode(newHyperlink(label, url));
	}

	/**
	 * Adds a text flow to the documentation.
	 *
	 * @param children the child nodes. Child nodes may be either JavaFX nodes
	 * (e.g. {@code Text}), or {@code String} which will get converted to a
	 * JavaFX {@code Text} node automatically.
	 * @return the text flow.
	 */
	public TextFlow addTextFlow(Object... children) {
		final TextFlow node = StructuredText.textFlow(Arrays.asList(children));
		return addNode(node);
	}

	/**
	 * Adds a text flow to the documentation.
	 *
	 * @param children the child nodes. Child nodes may be either JavaFX nodes
	 * (e.g. {@code Text}), or {@code String} which will get converted to a
	 * JavaFX {@code Text} node automatically.
	 * @return the text flow.
	 */
	public TextFlow addTextFlow(List<Object> children) {
		final TextFlow node = StructuredText.textFlow(children);
		return addNode(node);
	}

	/**
	 * Adds a new, unordered list.
	 *
	 * @param items the list items (JavaFX {@code Node}s or {@code String}s).
	 * @return an unordered list.
	 */
	public StructuredText addUnorderedList(List<Object> items) {
		final StructuredText list = StructuredText.unorderedList(items);
		return addStructuredText(list);
	}

	/**
	 * Adds a new, ordered list with {@code ARABIC} counter style.
	 *
	 * @param items the list items (JavaFX {@code Node}s or {@code String}s).
	 * @return an ordered list.
	 */
	public StructuredText addOrderedList(List<Object> items) {
		final StructuredText list = StructuredText.orderedList(items);
		return addStructuredText(list);
	}

	/**
	 * Adds a new, small description list.
	 *
	 * @param items the items. `Definition term` (keys) and `definition values`
	 * (values) may be either JavaFX nodes (e.g. {@code Text}), or
	 * {@code String}s which will get converted to a JavaFX {@code Text} node
	 * automatically.
	 * @return the description list.
	 */
	public StructuredText addSmallDescriptionList(Map<Object, Object> items) {
		final StructuredText list = StructuredText.smallDescriptionList(items);
		return addStructuredText(list);
	}

	/**
	 * Adds a new description list.
	 *
	 * @param items the items. `Definition term` (keys) and `definition values`
	 * (values) may be either JavaFX nodes (e.g. {@code Text}), or
	 * {@code String}s which will get converted to a JavaFX {@code Text} node
	 * automatically.
	 * @return the description list.
	 */
	public StructuredText addDescriptionList(Map<Object, Object> items) {
		final StructuredText list = StructuredText.descriptionList(items);
		return addStructuredText(list);
	}

	/**
	 * Creates a new hyperlink.
	 *
	 * @param url the url, also used as label.
	 * @return the hyperlink.
	 */
	public Hyperlink newHyperlink(String url) {
		return newHyperlink(url, url);
	}

	/**
	 * Creates a new hyperlink.
	 *
	 * @param label the label.
	 * @param url the url.
	 * @return the hyperlink.
	 */
	public Hyperlink newHyperlink(String label, String url) {
		final Hyperlink node = StructuredText.hyperlink(label, url);
		node.setOnAction(new HyperlinkHandler(url));
		return node;
	}

	/**
	 * Hyperlink handler.
	 */
	protected class HyperlinkHandler implements EventHandler<ActionEvent> {

		protected final String url;

		/**
		 * Creates a new hyperlink handler.
		 *
		 * @param url the URL to open in a browser.
		 */
		public HyperlinkHandler(String url) {
			this.url = url;
		}

		@Override
		public void handle(ActionEvent event) {
			if (hostServices == null) {
				log.warn("FX application host services unavailable: {}" + this);
				return;
			}
			hostServices.showDocument(url);
		}

	}

}
