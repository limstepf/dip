package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base class for (typed/non-generic) list data structures.
 *
 * @param <T> type of the list items.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractList<T> implements List<T>, RandomAccess {

	@XmlElement(name = "element")
	public final List<T> elements;

	@SuppressWarnings("unused")
	public AbstractList() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list.
	 *
	 * @param elements the list items.
	 */
	public AbstractList(List<T> elements) {
		this.elements = elements;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{"
				+ elements
				+ "}";
	}

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AbstractList<T> other = (AbstractList<T>) obj;
		return this.elements.equals(other.elements);
	}

	/*
	 * List<T>
	 */
	@Override
	public int size() {
		return this.elements.size();
	}

	@Override
	public boolean isEmpty() {
		return this.elements.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.elements.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return this.elements.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.elements.toArray();
	}

	@Override
	public <T> T[] toArray(T[] ts) {
		return this.elements.toArray(ts);
	}

	@Override
	public boolean add(T e) {
		return this.elements.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return this.elements.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> clctn) {
		return this.elements.containsAll(clctn);
	}

	@Override
	public boolean addAll(Collection<? extends T> clctn) {
		return this.elements.addAll(clctn);
	}

	@Override
	public boolean addAll(int i, Collection<? extends T> clctn) {
		return this.elements.addAll(i, clctn);
	}

	@Override
	public boolean removeAll(Collection<?> clctn) {
		return this.elements.removeAll(clctn);
	}

	@Override
	public boolean retainAll(Collection<?> clctn) {
		return this.elements.retainAll(clctn);
	}

	@Override
	public void clear() {
		this.elements.clear();
	}

	@Override
	public T get(int i) {
		return this.elements.get(i);
	}

	@Override
	public T set(int i, T e) {
		return this.elements.set(i, e);
	}

	@Override
	public void add(int i, T e) {
		this.elements.add(i, e);
	}

	@Override
	public T remove(int i) {
		return this.elements.remove(i);
	}

	@Override
	public int indexOf(Object o) {
		return this.elements.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.elements.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return this.elements.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int i) {
		return this.elements.listIterator(i);
	}

	@Override
	public List<T> subList(int i, int i1) {
		return this.elements.subList(i, i1);
	}

}
