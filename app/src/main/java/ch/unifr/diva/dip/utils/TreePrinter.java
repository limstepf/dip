package ch.unifr.diva.dip.utils;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;

/**
 * A simple tree printer.
 */
public class TreePrinter {

	private char c_hline = '─';
	private char c_vline = '│';
	private char c_child = '├';
	private char c_lastChild = '└';

	final private PrintStream out;

	/**
	 * Creates a new tree printer. Prints to {@code System.out}.
	 */
	public TreePrinter() {
		this(System.out);
	}

	/**
	 * Creates a new tree printer.
	 *
	 * @param out a print stream.
	 */
	public TreePrinter(PrintStream out) {
		this.out = out;
	}

	/**
	 * Returns an UTF-8 print stream to {@code System.out}. The returned print
	 * stream just wraps {@code System.out}, so do not close it!
	 *
	 * @return an UTF-8 print stream to {@code System.out}.
	 */
	public static PrintStream getUTF8PrintStream() {
		try {
			return new PrintStream(System.out, true, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return System.out;
		}
	}

	/**
	 * Prints a tree.
	 *
	 * @param node the root node.
	 */
	public void print(Node node) {
		print(node, getDefaultExtractor(), "", -1);
	}

	/**
	 * Prints a tree.
	 *
	 * @param node the root node.
	 * @param extractor the extractor extracts name and children of arbitrary
	 * objects. Can be null if all children (incl. the root node) are of type
	 * {@code TreePrinter.Node} (can be mixed too).
	 */
	public void print(Object node, Extractor extractor) {
		print(node, extractor, "", -1);
	}

	/**
	 * Prints a tree.
	 *
	 * @param root the name of the root node.
	 * @param children the children of the root node.
	 * @param extractor the extractor extracts name and children of arbitrary
	 * objects. Can be null if all children are of type {@code TreePrinter.Node}
	 * (can be mixed too).
	 */
	public void print(String root, Collection children, Extractor extractor) {
		print(root, children.toArray(), extractor, "", -1);
	}

	/**
	 * Prints a tree.
	 *
	 * @param root the name of the root node.
	 * @param children the children of the root node.
	 * @param extractor the extractor extracts name and children of arbitrary
	 * objects. Can be null if all children are of type {@code TreePrinter.Node}
	 * (can be mixed too).
	 */
	public void print(String root, Object[] children, Extractor extractor) {
		print(root, children, extractor, "", -1);
	}

	private void print(Object node, Extractor extractor, String prefix, int nodeType) {
		final Object[] children;
		final String name;
		if (node == null) {
			children = new Object[]{};
			name = "<null>";
		} else if (node instanceof Node) {
			final Node n = (Node) node;
			children = n.getChildren();
			name = n.getName();
		} else {
			children = extractor.getChildren(node);
			name = extractor.getName(node);
		}
		print(name, children, extractor, prefix, nodeType);
	}

	private void print(String name, Object[] children, Extractor extractor, String prefix, int nodeType) {

		final boolean isTail = nodeType != 0;
		final boolean isRoot = nodeType < 0;
		final String c_space;
		final String t_space;
		if (isRoot) {
			c_space = "";
			t_space = " ";
			out.println(prefix + name);
		} else {
			c_space = " ";
			t_space = "  ";
			out.println(prefix + (isTail ? c_lastChild : c_child) + " " + name);
			if (isTail && children.length == 0) {
				out.println(prefix);
			}
		}

		for (int i = 0; i < children.length - 1; i++) {
			print(
					children[i],
					extractor,
					prefix + (isTail ? c_space : c_vline) + t_space,
					0
			);
		}
		if (children.length > 0) {
			print(
					children[children.length - 1],
					extractor,
					prefix + (isTail ? c_space : c_vline) + t_space,
					1
			);
		}
	}

	/**
	 * A node extractor. Extracts name and children from arbitrary objects.
	 */
	public interface Extractor {

		/**
		 * Returns the children of an object.
		 *
		 * @param obj the object.
		 * @return the children of the object.
		 */
		public Object[] getChildren(Object obj);

		/**
		 * Returns the name of an object.
		 *
		 * @param obj the object.
		 * @return the name of the object.
		 */
		public String getName(Object obj);

	}

	/**
	 * Returns a default extractor. Doesn't extract any child nodes, and uses
	 * the {@code toString()} method to extract the name of an object.
	 *
	 * @return the default extractor.
	 */
	public static Extractor getDefaultExtractor() {
		return new Extractor() {
			@Override
			public Object[] getChildren(Object obj) {
				return new Object[]{};
			}

			@Override
			public String getName(Object obj) {
				return obj.toString();
			}
		};
	}

	/**
	 * A TreePrinter node.
	 */
	public static class Node {

		private final String name;
		private final Object[] children;

		/**
		 * Creates a new TreePrinter node.
		 *
		 * @param name the name of the node.
		 * @param child the only child of the node.
		 */
		public Node(String name, Object child) {
			this(name, new Object[]{child});
		}

		/**
		 * Creates a new TreePrinter node.
		 *
		 * @param name the name of the node.
		 * @param children the children of the node.
		 */
		public Node(String name, Collection children) {
			this(name, children.toArray());
		}

		/**
		 * Creates a new TreePrinter node.
		 *
		 * @param name the name of the node.
		 * @param children the children of the node.
		 */
		public Node(String name, Object[] children) {
			this.name = name;
			this.children = children;
		}

		/**
		 * Returns the children of the node.
		 *
		 * @return the children of the node.
		 */
		public Object[] getChildren() {
			return children;
		}

		/**
		 * Returns the name of the node.
		 *
		 * @return the name of the node.
		 */
		public String getName() {
			return name;
		}

	}

	/**
	 * A node extractor makes {@code Node} out of arbitrary objects.
	 *
	 * @param <T> type of the object to wrap in a {@code Node}.
	 */
	public interface NodeExtractor<T> {

		/**
		 * Returns the {@code Node} wrapping the given object.
		 *
		 * @param obj the object.
		 * @return the node.
		 */
		public Node extractNode(T obj);
	}

	/**
	 * Turns a collection of objects into an array of (child-)nodes.
	 *
	 * @param <T> type of the objects.
	 * @param objects the objects.
	 * @param extractor the node extractor.
	 * @return an array of nodes.
	 */
	public static <T> Node[] toNodes(Collection<T> objects, NodeExtractor<T> extractor) {
		final Node[] nodes = new Node[objects.size()];
		int i = 0;
		for (T obj : objects) {
			nodes[i++] = extractor.extractNode(obj);
		}
		return nodes;
	}

	/**
	 * Returns (and formats) dictonary entries as TreePrinter node children.
	 *
	 * @param dictionary the dictionary.
	 * @return an array of TreePrinter node children.
	 */
	public static Object[] toChildren(Dictionary dictionary) {
		return toChildren(dictionary, (key, val) -> {
			return String.format("%s: %s", key, val);
		});
	}

	/**
	 * Returns (and formats) dictonary entries as TreePrinter node children.
	 *
	 * @param dictionary the dictionary.
	 * @param mapper the dictionary (entry) mapper.
	 * @return an array of TreePrinter node children.
	 */
	public static Object[] toChildren(Dictionary dictionary, DictMapper mapper) {
		final Object[] children = new Object[dictionary.size()];
		int i = 0;
		for (Enumeration e = dictionary.keys(); e.hasMoreElements(); i++) {
			final Object key = e.nextElement();
			final Object val = dictionary.get(key);
			children[i] = mapper.map(key, val);
		}
		return children;
	}

	/**
	 * A dictionary (entry) mapper.
	 */
	public interface DictMapper {

		/**
		 * Maps a dictionary entry (key and value) to some output.
		 *
		 * @param key the key of the entry.
		 * @param value the value of the entry.
		 * @return some output.
		 */
		public Object map(Object key, Object value);
	}

}
