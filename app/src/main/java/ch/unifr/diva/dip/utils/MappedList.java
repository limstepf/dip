package ch.unifr.diva.dip.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 * Mapped view of a JavaFX ObservableList.
 *
 * This is Tomas Mikula's suggestion/implementation of a MappedList that will
 * hopefully make it into JavaFX soon enough...
 *
 * @param <E> type of the destination/slave list.
 * @param <F> type of the source/master list.
 * @see <a href="https://gist.github.com/TomasMikula/8883719">
 * https://gist.github.com/TomasMikula/8883719</a>
 * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8091967">
 * https://bugs.openjdk.java.net/browse/JDK-8091967</a>
 */
public class MappedList<E, F> extends TransformationList<E, F> {

	private final Function<F, E> mapper;

	/**
	 * Creates a mapped list that gets modified/updated as the source list does.
	 *
	 * @param source source list of type E.
	 * @param mapper mapper function that takes E and returns F.
	 */
	public MappedList(ObservableList<? extends F> source, Function<F, E> mapper) {
		super(source);
		this.mapper = mapper;
	}

	@Override
	public int getSourceIndex(int index) {
		return index;
	}

	@Override
	public E get(int index) {
		return mapper.apply(getSource().get(index));
	}

	@Override
	public int size() {
		return getSource().size();
	}

	@Override
	protected void sourceChanged(Change<? extends F> c) {
		fireChange(new Change<E>(this) {

			@Override
			public boolean wasAdded() {
				return c.wasAdded();
			}

			@Override
			public boolean wasRemoved() {
				return c.wasRemoved();
			}

			@Override
			public boolean wasReplaced() {
				return c.wasReplaced();
			}

			@Override
			public boolean wasUpdated() {
				return c.wasUpdated();
			}

			@Override
			public boolean wasPermutated() {
				return c.wasPermutated();
			}

			@Override
			public int getPermutation(int i) {
				return c.getPermutation(i);
			}

			@Override
			protected int[] getPermutation() {
				// This method is only called by the superclass methods
				// wasPermutated() and getPermutation(int), which are
				// both overriden by this class. There is no other way
				// this method can be called.
				throw new AssertionError("Unreachable code");
			}

			@Override
			public List<E> getRemoved() {
				ArrayList<E> res = new ArrayList<>(c.getRemovedSize());
				for (F e : c.getRemoved()) {
					res.add(mapper.apply(e));
				}
				return res;
			}

			@Override
			public int getFrom() {
				return c.getFrom();
			}

			@Override
			public int getTo() {
				return c.getTo();
			}

			@Override
			public boolean next() {
				return c.next();
			}

			@Override
			public void reset() {
				c.reset();
			}
		});
	}
}
