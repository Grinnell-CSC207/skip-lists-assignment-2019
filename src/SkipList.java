import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import java.util.function.BiConsumer;

/**
 * An implementation of skip lists.
 */
public class SkipList<K, V> implements SimpleMap<K, V> {

	// +-----------+---------------------------------------------------
	// | Constants |
	// +-----------+

	/**
	 * The initial height of the skip list.
	 */
	static final int INITIAL_HEIGHT = 16;

	// +---------------+-----------------------------------------------
	// | Static Fields |
	// +---------------+

	static Random rand = new Random();

	// +--------+------------------------------------------------------
	// | Fields |
	// +--------+

	/**
	 * Pointers to all the front elements.
	 */
	ArrayList<SLNode<K, V>> front;

	/**
	 * The comparator used to determine the ordering in the list.
	 */
	Comparator<K> comparator;

	/**
	 * The number of values in the list.
	 */
	int size;

	/**
	 * The current height of the skiplist.
	 */
	int height;

	/**
	 * The probability used to determine the height of nodes.
	 */
	double prob = 0.5;

	// +--------------+------------------------------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Create a new skip list that orders values using the specified comparator.
	 */
	public SkipList(Comparator<K> comparator) {
		this.front = new ArrayList<SLNode<K, V>>(INITIAL_HEIGHT);
		for (int i = 0; i < INITIAL_HEIGHT; i++) {
			front.add(null);
		} // for
		this.comparator = comparator;
		this.size = 0;
		this.height = INITIAL_HEIGHT;
	} // SkipList(Comparator<K>)

	/**
	 * Create a new skip list that orders values using a not-very-clever default
	 * comparator.
	 */
	public SkipList() {
		this((k1, k2) -> k1.toString().compareTo(k2.toString()));
	} // SkipList()

	// +-------------------+-------------------------------------------
	// | SimpleMap methods |
	// +-------------------+

	@Override
	public V set(K key, V value) {
		if (key == null) {
			throw new NullPointerException("null key");
		} else {
			// find the position we want
			ArrayList<SLNode<K, V>> foundPtr = find(key);
			// if the node we want do not exist
			if (foundPtr.get(0) == null || comparator.compare(foundPtr.get(0).key, key) != 0) {
				int newHeight = randomHeight();
				SLNode<K, V> newNode = new SLNode<K, V>(key, value, newHeight);
				if (newHeight > height) {
					// update the front pointer to connect to the new node
					for (int i = height; i < newHeight; i++) {
						this.front.set(i, newNode);
					}
					this.height = newHeight;
				}
				// connect the new node to previous node
				for (int j = 0; j < newHeight; j++) {
					newNode.next.set(j, foundPtr.get(j));
					foundPtr.set(j, newNode);
				}
				this.size++;
				return null;
			} else if (comparator.compare(foundPtr.get(0).key, key) == 0) {
				V temp = foundPtr.get(0).value;
				foundPtr.get(0).value = value;
				return temp;
			} else if (comparator.compare(foundPtr.get(0).key, key) < 0){
				throw new NoSuchElementException("foundPtr.get(0).key < key");
			} else {
				throw new NoSuchElementException("set error");
			}
		}
	} // set(K,V)

	@Override
	public V get(K key) {

		if (key == null) {
			throw new NullPointerException("null key");
		} else {
			// find the position we want
			ArrayList<SLNode<K, V>> foundPtr = find(key);
			// if the node we want does exist, return the value
			if (foundPtr.get(0) != null && this.comparator.compare(foundPtr.get(0).key, key) == 0) {
				return foundPtr.get(0).value;
			} else {
				// when the key does not exist, throw exception
				throw new IndexOutOfBoundsException("key not found");
			}
		}
	} // get(K,V)

	@Override
	public int size() {
		return this.size;
	} // size()

	@Override
	public boolean containsKey(K key) {
		try {
			get(key);
			return true;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	} // containsKey(K)

	@Override
	public V remove(K key) {
		// find the position we want
				ArrayList<SLNode<K, V>> foundPtr = find(key);
				// if node to remove does not exist, throw exception
				if (foundPtr.get(0) == null || (comparator.compare(foundPtr.get(0).key, key) != 0)) {
					throw new NoSuchElementException("No element with Key exists to remove");
				}
				// remove node
				V valueToReturn = foundPtr.get(0).value;
				for (int i = foundPtr.get(0).next.size() - 1; i >= 0; i--) {
					// set pointers
					foundPtr.set(i, foundPtr.get(i).next.get(i));
				} // for
				this.size--;
				return valueToReturn;
			} // remove(key)


	@Override
	public Iterator<K> keys() {
		return new Iterator<K>() {
			Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

			@Override
			public boolean hasNext() {
				return nit.hasNext();
			} // hasNext()

			@Override
			public K next() {
				return nit.next().key;
			} // next()

			@Override
			public void remove() {
				nit.remove();
			} // remove()
		};
	} // keys()

	@Override
	public Iterator<V> values() {
		return new Iterator<V>() {
			Iterator<SLNode<K, V>> nit = SkipList.this.nodes();

			@Override
			public boolean hasNext() {
				return nit.hasNext();
			} // hasNext()

			@Override
			public V next() {
				return nit.next().value;
			} // next()

			@Override
			public void remove() {
				nit.remove();
			} // remove()
		};
	} // values()

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		Iterator<SLNode<K, V>> it = nodes();
		while (it.hasNext()) {
			SLNode<K, V> current = it.next();
			action.accept(current.key, current.value);
			System.err.println(current.next.size());
		}
	} // forEach

	// +----------------------+----------------------------------------
	// | Other public methods |
	// +----------------------+

	/**
	 * Dump the tree to some output location.
	 */
	public void dump(PrintWriter pen) {
		// Forthcoming
	} // dump(PrintWriter)

	// +---------+-----------------------------------------------------
	// | Helpers |
	// +---------+

	/**
	 * Pick a random height for a new node.
	 */
	int randomHeight() {
		int result = 1;
		while (rand.nextDouble() < prob) {
			result = result + 1;
		}
		return result;
	} // randomHeight()

	/**
	 * Get an iterator for all of the nodes. (Useful for implementing the other
	 * iterators.)
	 */
	Iterator<SLNode<K, V>> nodes() {
		return new Iterator<SLNode<K, V>>() {

			/**
			 * A reference to the next node to return.
			 */
			SLNode<K, V> next = SkipList.this.front.get(0);

			@Override
			public boolean hasNext() {
				return this.next != null;
			} // hasNext()

			@Override
			public SLNode<K, V> next() {
				if (this.next == null) {
					throw new IllegalStateException();
				}
				SLNode<K, V> temp = this.next;
				this.next = this.next.next.get(0);
				return temp;
			} // next();
		}; // new Iterator
	} // nodes()

	// +---------+-----------------------------------------------------
	// | Helpers |
	// +---------+

	/**
	 * 
	 * @param key
	 * @return the Arraylist of all the node pointers to be updated to insert
	 */
	private ArrayList<SLNode<K, V>> find(K key) {
		// check empty skiplist
		if (this.size == 0) {
			return this.front;
		}

		ArrayList<SLNode<K, V>> update = new ArrayList<SLNode<K, V>>();
		for (int i = 0; i < height; i++) {
			update.add(null);
		} 
		
		// current node ArrayList
		ArrayList<SLNode<K, V>> currArrList = this.front;

		// Iterate until the last level
		for (int i = height - 1; i >= 0; i--) {
			while (currArrList.get(i) != null && currArrList.get(i).next(i) != null
					&& this.comparator.compare(currArrList.get(i).key, key) < 0) {
				// update arraylist
					currArrList = currArrList.get(i).next;
			} // while
			update.set(i, currArrList.get(i));
		} // for
		return update;
	}

} // class SkipList

/**
 * Nodes in the skip list.
 */
class SLNode<K, V> {

	// +--------+------------------------------------------------------
	// | Fields |
	// +--------+

	/**
	 * The key.
	 */
	K key;

	/**
	 * The value.
	 */
	V value;

	/**
	 * Pointers to the next nodes.
	 */
	ArrayList<SLNode<K, V>> next;

	// +--------------+------------------------------------------------
	// | Constructors |
	// +--------------+

	/**
	 * Create a new node of height n with the specified key and value.
	 */
	public SLNode(K key, V value, int n) {
		this.key = key;
		this.value = value;
		this.next = new ArrayList<SLNode<K, V>>(n);
		for (int i = 0; i < n; i++) {
			this.next.add(null);
		} // for
	} // SLNode(K, V, int)

	// +---------+-----------------------------------------------------
	// | Methods |
	// +---------+

	/**
	 * returns next node
	 */
	public SLNode<K, V> next(int level) {
		return this.next.get(level);
	}

	/**
	 * sets next node at that level's reference
	 */
	public void setNext(int level, SLNode<K, V> next) {
		this.next.set(level, next);
	}
} // SLNode<K,V>
