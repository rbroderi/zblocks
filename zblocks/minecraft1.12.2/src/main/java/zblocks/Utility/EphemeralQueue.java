package zblocks.Utility;

import java.util.*;

public class EphemeralQueue<T> extends AbstractCollection<T> {

	private ArrayList<T> items = new ArrayList<T>();
	private ArrayList<T> pendingItems = new ArrayList<T>();
	private boolean inLoop = false;

	public T dequeue() {
		T item = items.get(0);
		items.remove(0);
		return item;
	}

	/**
	 * Adds item to queue, places in pending queue until after iterator if in a foreach loop.
	 * 
	 * @param item
	 */
	public void enqueue(T item) {
		if (!inLoop) {
			items.add(item);
		} else {
			pendingItems.add(item);
		}
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public void clear() {
		items.clear();
		pendingItems.clear();
	}

	@Deprecated
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public boolean removeAll(Collection<?> t) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Override
	public boolean addAll(Collection<? extends T> t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public QueueIt iterator() {
		return new QueueIt();
	}

	class QueueIt implements Iterator<T> {

		public boolean hasNext() {
			if (size() == 0) {
				for (T t : pendingItems) {
					items.add(t);
				}
				pendingItems.clear();
				inLoop = false;
				return false;
			}
			return size() > 0;
		}

		public T next() {
			inLoop = true;
			T node = dequeue();
			return node;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}