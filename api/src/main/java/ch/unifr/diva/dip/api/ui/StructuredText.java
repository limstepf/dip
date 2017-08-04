package ch.unifr.diva.dip.api.ui;

import java.util.List;
import java.util.Map;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Structured text utilities and factory methods.
 */
public class StructuredText extends GridPane {

	/**
	 * Text style/representation. Or how to turn a {@code String} into a JavaFX
	 * node.
	 */
	public enum TextStyle {

		/**
		 * A {@code Label}.
		 */
		LABEL() {
					@Override
					public Node toNode(String text) {
						return label(text);
					}
				},
		/**
		 * A text wrapping {@code Label}.
		 */
		LABEL_WRAP() {
					@Override
					public Node toNode(String text) {
						return label(text, false, 0);
					}
				},
		/**
		 * A bold {@code Label}.
		 */
		LABEL_BOLD() {
					@Override
					public Node toNode(String text) {
						return label(text, true);
					}
				},
		/**
		 * A {@code Text}.
		 */
		TEXT() {
					@Override
					public Node toNode(String text) {
						return text(text);
					}
				},
		/**
		 * A bold {@code Text}.
		 */
		TEXT_BOLD() {
					@Override
					public Node toNode(String text) {
						return text(text, true);
					}
				},
		/**
		 * A {@code TextFlow}.
		 */
		TEXTFLOW() {
					@Override
					public Node toNode(String text) {
						return textFlow(text(text));
					}
				},
		/**
		 * A {@code TextFlow}.
		 */
		TEXTFLOW_BOLD() {
					@Override
					public Node toNode(String text) {
						return textFlow(text(text, true));
					}
				};

		/**
		 * Turns a string into the given text node.
		 *
		 * @param text the text.
		 * @return the JavaFX node.
		 */
		public abstract Node toNode(String text);

	}

	/**
	 * List counter style.
	 */
	public enum CounterStyle {

		/**
		 * Arabic. E.g. {@code 1, 2, 3, ...}.
		 */
		ARABIC() {
					@Override
					public String toStyle(int i) {
						return String.valueOf(i + 1);
					}
				},
		/**
		 * Alpha. E.g. {@code a, b, c, ...}.
		 */
		ALPHA() {
					@Override
					public String toStyle(int i) {
						return String.valueOf((char) (97 + i));
					}
				};

		/**
		 * Turns an index into the list counter.
		 *
		 * @param i the index of the item.
		 * @return the list counter.
		 */
		public abstract String toStyle(int i);

	}

	protected static double COUNTER_MIN_LENGTH = 10;

	final protected ColumnConstraints cc[];
	final protected RowConstraints rc;

	protected int row;

	/**
	 * Creates a new, empty structured list with 2 columns.
	 */
	protected StructuredText() {
		this(2);
	}

	/**
	 * Creates a new, empty structured grid.
	 *
	 * @param n number of columns in the grid.
	 */
	protected StructuredText(int n) {
		this.cc = new ColumnConstraints[n];
		for (int i = 0; i < n; i++) {
			cc[i] = new ColumnConstraints();
			if (i > 0 || n > 2) {
				cc[i].setFillWidth(true);
				cc[i].setHgrow(Priority.ALWAYS);
			}
		}
		getColumnConstraints().addAll(cc);

		this.rc = new RowConstraints();
		rc.setValignment(VPos.TOP);
		this.row = 0;

		setHgap(10);
		setVgap(5);
	}

	/**
	 * Adds the next item to the (2 column wide) list.
	 *
	 * @param key the key, counter, or bullet point.
	 * @param value the value, or list item.
	 */
	protected void addItem(Node key, Node value) {
		add(key, 0, row);
		add(value, 1, row);
		getRowConstraints().add(rc);
		row++;
	}

	/**
	 * Creates a new {@code Text} node.
	 *
	 * @param text the text.
	 * @return the {@code Text} node.
	 */
	public static Text text(String text) {
		return text(text, false);
	}

	/**
	 * Creates a new {@code Text} node.
	 *
	 * @param text the text.
	 * @param bold {@code true} for bold text, {@code false} for regular text.
	 * @return the {@code Text} node.
	 */
	public static Text text(String text, boolean bold) {
		final Text node = new Text(text);
		if (bold) {
			node.getStyleClass().add("dip-title");
		}
		return node;
	}

	/**
	 * Creates a new {@code TextFlow} node.
	 *
	 * @param children the child nodes (JavaFX nodes, typically {@code Text}).
	 * @return the {@code TextFlow} node.
	 */
	public static TextFlow textFlow(Node... children) {
		final TextFlow flow = new TextFlow(children);
		flow.setPrefWidth(Region.USE_COMPUTED_SIZE);
		return flow;
	}

	/**
	 * Creates a new {@code TextFlow} node.
	 *
	 * @param children the child nodes. Child nodes may be either JavaFX nodes
	 * (e.g. {@code Text}), or {@code String} which will get converted to a
	 * JavaFX {@code Text} node automatically.
	 * @return the {@code TextFlow} node.
	 */
	public static TextFlow textFlow(List<Object> children) {
		final TextFlow flow = textFlow();
		for (Object obj : children) {
			final Node node = toNode(obj, TextStyle.TEXT);
			if (node != null) {
				flow.getChildren().add(node);
			}
		}
		return flow;
	}

	/**
	 * Creates a new {@code Label} node.
	 *
	 * @param text the text.
	 * @return the {@code Label} node.
	 */
	public static Label label(String text) {
		return label(text, false);
	}

	/**
	 * Creates a new {@code Label} node.
	 *
	 * @param text the text.
	 * @param bold {@code true} for a bold label, {@code false} for a regular
	 * one.
	 * @return the {@code Label} node.
	 */
	public static Label label(String text, boolean bold) {
		return label(text, bold, Region.USE_PREF_SIZE);
	}

	/**
	 * Creates a new {@code Label} node.
	 *
	 * @param text the text.
	 * @param bold {@code true} for a bold label, {@code false} for a regular
	 * one.
	 * @param minWidth minimum width of the label. {@code 0} for wrapped text,
	 * {@code Region.USE_PREF_SIZE} for a single line.
	 * @return the {@code Label} node.
	 */
	public static Label label(String text, boolean bold, double minWidth) {
		final Label label = new Label(text);
		label.setWrapText(true);
		label.setMinWidth(minWidth);
		label.getStyleClass().add("dip-small");
		if (bold) {
			label.getStyleClass().add("dip-title");
		}
		return label;
	}

	/**
	 * Creates a new {@code Hyperlink} node.
	 *
	 * @param url the url (also used as label).
	 * @return the {@code Hyperlink} node.
	 */
	public static Hyperlink hyperlink(String url) {
		return hyperlink(url, url);
	}

	/**
	 * Creates a new {@code Hyperlink} node.
	 *
	 * @param label the label.
	 * @param url the url.
	 * @return the {@code Hyperlink} node.
	 */
	public static Hyperlink hyperlink(String label, String url) {
		final Hyperlink link = new Hyperlink(label);
		link.setWrapText(true);
		link.setPrefWidth(Region.USE_COMPUTED_SIZE);
		return link;
	}

	protected static Text bullet() {
		return text("\u25CF"); // 'BLACK CIRCLE' (U+25CF)
	}

	protected static Text counter(String label) {
		return text(label + "\u2008)"); // 'PUNCTUATION SPACE' (U+2008)
	}

	/**
	 * Returns a JavaFX text node.
	 *
	 * @param obj the object, which is either already a JavaFX node, or a
	 * {@code String}.
	 * @param style the text node to convert {@code obj} to a JavaFX node if
	 * it's a string.
	 * @return the JavaFX text node, or {@code null} if {@code obj} couldn't be
	 * transformed.
	 */
	public static Node toNode(Object obj, TextStyle style) {
		if (obj instanceof String) {
			return style.toNode((String) obj);
		} else if (obj instanceof Node) {
			return (Node) obj;
		}
		return null;
	}

	/**
	 * Creates a new, unordered list.
	 *
	 * @param items the list items (JavaFX {@code Node}s or {@code String}s).
	 * @return an unordered list.
	 */
	public static StructuredText unorderedList(List<Object> items) {
		final StructuredText list = new StructuredText();
		list.cc[0].setHalignment(HPos.RIGHT);
		list.cc[0].setMinWidth(COUNTER_MIN_LENGTH);
		Node val;
		for (Object item : items) {
			val = toNode(item, TextStyle.TEXTFLOW);
			if (val != null) {
				list.addItem(bullet(), val);
			}
		}
		return list;
	}

	/**
	 * Creates a new, ordered list with {@code ARABIC} counter style.
	 *
	 * @param items the list items (JavaFX {@code Node}s or {@code String}s).
	 * @return an ordered list.
	 */
	public static StructuredText orderedList(List<Object> items) {
		return orderedList(items, CounterStyle.ARABIC);
	}

	/**
	 * Creates a new, ordered list.
	 *
	 * @param items the list items (JavaFX {@code Node}s or {@code String}s).
	 * @param style the list counter style.
	 * @return an ordered list.
	 */
	public static StructuredText orderedList(List<Object> items, CounterStyle style) {
		final StructuredText list = new StructuredText();
		list.cc[0].setHalignment(HPos.RIGHT);
		list.cc[0].setMinWidth(COUNTER_MIN_LENGTH);
		Node val;
		for (Object item : items) {
			val = toNode(item, TextStyle.TEXTFLOW);
			if (val != null) {
				list.addItem(counter(style.toStyle(list.row)), val);
			}
		}
		return list;
	}

	/**
	 * Creates a new, small description list.
	 *
	 * @param items the items. `Definition term` (keys) and `definition values`
	 * (values) may be either JavaFX nodes (e.g. {@code Text}), or
	 * {@code String}s which will get converted to a JavaFX {@code Text} node
	 * automatically.
	 * @return the description list.
	 */
	public static StructuredText smallDescriptionList(Map<Object, Object> items) {
		return descriptionList(items, TextStyle.LABEL_BOLD, TextStyle.LABEL_WRAP);
	}

	/**
	 * Creates a new description list.
	 *
	 * @param items the items. `Definition term` (keys) and `definition values`
	 * (values) may be either JavaFX nodes (e.g. {@code Text}), or
	 * {@code String}s which will get converted to a JavaFX {@code Text} node
	 * automatically.
	 * @return the description list.
	 */
	public static StructuredText descriptionList(Map<Object, Object> items) {
		return descriptionList(items, TextStyle.TEXT_BOLD, TextStyle.TEXTFLOW);
	}

	/**
	 * Creates a new description list.
	 *
	 * @param items the items. `Definition term` (keys) and `definition values`
	 * (values) may be either JavaFX nodes (e.g. {@code Text}), or
	 * {@code String}s which will get converted to a JavaFX {@code Text} node
	 * automatically.
	 * @param dtStyle the term style used to turn {@code String}s into JavaFX
	 * nodes.
	 * @param ddStyle the definition style used to turn {@code String}s into
	 * JavaFX nodes.
	 * @return the description list.
	 */
	public static StructuredText descriptionList(Map<Object, Object> items, TextStyle dtStyle, TextStyle ddStyle) {
		final StructuredText list = new StructuredText();
		Node dt;
		Node dd;
		for (Map.Entry<Object, Object> item : items.entrySet()) {
			dt = toNode(item.getKey(), dtStyle);
			dd = toNode(item.getValue(), ddStyle);
			list.addItem(dt, dd);
		}
		return list;
	}

	/**
	 * Creates a new, small table. Cell items may be either JavaFX {@code Node}s
	 * or {@code String}s.
	 *
	 * @param rows the table rows.
	 * @return the table.
	 */
	public static StructuredText smallTable(List<List<Object>> rows) {
		return smallTable(null, rows, -1);
	}

	/**
	 * Creates a new, small table. Cell items may be either JavaFX {@code Node}s
	 * or {@code String}s.
	 *
	 * @param head the table head, or {@code null}.
	 * @param rows the table rows.
	 * @return the table.
	 */
	public static StructuredText smallTable(List<Object> head, List<List<Object>> rows) {
		return smallTable(head, rows, -1);
	}

	/**
	 * Creates a new, small table. Cell items may be either JavaFX {@code Node}s
	 * or {@code String}s.
	 *
	 * @param head the table head, or {@code null}.
	 * @param rows the table rows.
	 * @param columns the number of columns, or {@code -1} to let the first row
	 * determine the number of columns.
	 * @return the table.
	 */
	public static StructuredText smallTable(List<Object> head, List<List<Object>> rows, int columns) {
		return table(head, rows, TextStyle.LABEL_BOLD, TextStyle.LABEL_WRAP, columns);
	}

	/**
	 * Creates a new table. Cell items may be either JavaFX {@code Node}s or
	 * {@code String}s.
	 *
	 * @param rows the table rows.
	 * @return the table.
	 */
	public static StructuredText table(List<List<Object>> rows) {
		return table(null, rows, -1);
	}

	/**
	 * Creates a new table. Cell items may be either JavaFX {@code Node}s or
	 * {@code String}s.
	 *
	 * @param head the table head, or {@code null}.
	 * @param rows the table rows.
	 * @return the table.
	 */
	public static StructuredText table(List<Object> head, List<List<Object>> rows) {
		return table(head, rows, -1);
	}

	/**
	 * Creates a new table. Cell items may be either JavaFX {@code Node}s or
	 * {@code String}s.
	 *
	 * @param head the table head, or {@code null}.
	 * @param rows the table rows.
	 * @param columns the number of columns, or {@code -1} to let the first row
	 * determine the number of columns.
	 * @return the table.
	 */
	public static StructuredText table(List<Object> head, List<List<Object>> rows, int columns) {
		return table(head, rows, TextStyle.TEXT_BOLD, TextStyle.TEXTFLOW, columns);
	}

	/**
	 * Creates a new table. Cell items may be either JavaFX {@code Node}s or
	 * {@code String}s.
	 *
	 * @param head the table head, or {@code null}.
	 * @param rows the table rows.
	 * @param headStyle the head style used to turn {@code String}s into JavaFX
	 * nodes.
	 * @param rowStyle the row style used to turn {@code String}s into JavaFX
	 * nodes.
	 * @param columns the number of columns, or {@code -1} to let the first row
	 * determine the number of columns.
	 * @return the table.
	 */
	public static StructuredText table(List<Object> head, List<List<Object>> rows, TextStyle headStyle, TextStyle rowStyle, int columns) {
		final int m = rows.size();
		final int n = (columns > 0) ? columns : rows.get(0).size();
		final StructuredText table = new StructuredText(n);

		if (head != null) {
			final int c = Math.min(m, head.size());
			for (int i = 0; i < c; i++) {
				table.add(
						toNode(head.get(i), headStyle),
						i,
						table.row
				);
			}
			table.getRowConstraints().add(table.rc);
			table.row++;
		}

		for (List<Object> row : rows) {
			final int c = Math.min(m, row.size());
			for (int i = 0; i < c; i++) {
				table.add(
						toNode(row.get(i), rowStyle),
						i,
						table.row
				);
			}
			table.getRowConstraints().add(table.rc);
			table.row++;
		}

		return table;
	}

}
