/*******************************************************************************
 * Copyright 2021 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.dongbat.jbump.util;

import java.util.*;

/**
 * An unordered set where the keys are objects. Null keys are not allowed. No allocation is done except when growing the table
 * size.
 * <p>
 * This class performs fast contains and remove (typically O(1), worst case O(n) but that is rare in practice). Add may be
 * slightly slower, depending on hash collisions. Hashcodes are rehashed to reduce collisions and the need to resize. Load factors
 * greater than 0.91 greatly increase the chances to resize to the next higher POT size.
 * <p>
 * Unordered sets and maps are not designed to provide especially fast iteration, but the {@link #iterator()} method does reuse
 * one of two Iterator objects instead of potentially creating many of them when iterated over repeatedly (as HashSet does).
 * This can improve GC performance when compared to JDK collections.
 * <p>
 * You can customize most behavior of this map by extending it. {@link #place(Object)} can be overridden to change how hashCodes
 * are calculated (which can be useful for types like {@link StringBuilder} that don't implement hashCode()), and
 * {@link #equate(Object, Object)} can be overridden to change how equality is calculated.
 * <p>
 * This implementation uses linear probing with the backward shift algorithm for removal. Hashcodes are not rehashed by default, but
 * user code can subclass this and change the {@link #place(Object)} method if rehashing or an alternate hash is optimal. Linear
 * probing continues to work even when all hashCodes collide; it just works more slowly in that case.
 * <p>
 * Mostly copied from jdkgdxds, which was developed as a part of libGDX initially.
 * @author Nathan Sweet
 * @author Tommy Ettinger
 */
public class ObjectSet<T> implements Iterable<T>, Set<T> {


	protected int size;

	protected T[] keyTable;

	protected float loadFactor;
	protected int threshold;

	protected int shift;

	/**
	 * A bitmask used to confine hashcodes to the size of the table. Must be all 1 bits in its low positions, ie a power of two
	 * minus 1.
	 */
	protected int mask;
	protected transient ObjectSetIterator<T> iterator1;
	protected transient ObjectSetIterator<T> iterator2;
	/**
	 * Used to establish the size of a hash table for {@link ObjectSet} or related code.
	 * The table size will always be a power of two, and should be the next power of two that is at least equal
	 * to {@code capacity / loadFactor}.
	 *
	 * @param capacity   the amount of items the hash table should be able to hold
	 * @param loadFactor between 0.0 (exclusive) and 1.0 (inclusive); the fraction of how much of the table can be filled
	 * @return the size of a hash table that can handle the specified capacity with the given loadFactor
	 */
	public static int tableSize (int capacity, float loadFactor) {
		if (capacity < 0) {
			throw new IllegalArgumentException("capacity must be >= 0: " + capacity);
		}
		int tableSize = 1 << -Integer.numberOfLeadingZeros(Math.max(2, (int)Math.ceil(capacity / loadFactor)) - 1);
		if (tableSize > 1 << 30 || tableSize < 0) {
			throw new IllegalArgumentException("The required capacity is too large: " + capacity);
		}
		return tableSize;
	}

	/**
	 * Creates a new set with an initial capacity of 51 and a load factor of 0.7.
	 */
	public ObjectSet () {
		this(51, 0.7f);
	}

	/**
	 * Creates a new set with a load factor of 0.7.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public ObjectSet (int initialCapacity) {
		this(initialCapacity, 0.7f);
	}

	/**
	 * Creates a new set with the specified initial capacity and load factor. This set will hold initialCapacity items before
	 * growing the backing table.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 * @param loadFactor what fraction of the capacity can be filled before this has to resize; 0 &lt; loadFactor &lt;= 1
	 */
	public ObjectSet (int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor > 1f) { throw new IllegalArgumentException("loadFactor must be > 0 and <= 1: " + loadFactor); }
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int)(tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyTable = (T[])new Object[tableSize];
	}

	/**
	 * Creates a new set identical to the specified set.
	 */
	public ObjectSet (ObjectSet<? extends T> set) {
		loadFactor = set.loadFactor;
		threshold = set.threshold;
		mask = set.mask;
		shift = set.shift;
		keyTable = Arrays.copyOf(set.keyTable, set.keyTable.length);
		size = set.size;
	}

	/**
	 * Creates a new set that contains all distinct elements in {@code coll}.
	 */
	public ObjectSet (Collection<? extends T> coll) {
		this(coll.size());
		addAll(coll);
	}

	/**
	 * Creates a new set using {@code length} items from the given {@code array}, starting at {@code} offset (inclusive).
	 * @param array an array to draw items from
	 * @param offset the first index in array to draw an item from
	 * @param length how many items to take from array; bounds-checking is the responsibility of the using code
	 */
	public ObjectSet(T[] array, int offset, int length) {
		this(length);
		addAll(array, offset, length);
	}

	/**
	 * Creates a new set containing all of the items in the given array.
	 * @param array an array that will be used in full, except for duplicate items
	 */
	public ObjectSet(T[] array) {
		this(array, 0, array.length);
	}

	/**
	 * Returns an index &gt;= 0 and &lt;= {@link #mask} for the specified {@code item}.
	 * <p>
	 * The default implementation assumes the low-order bits of item.hashCode() are likely enough to avoid collisions,
	 * and so just returns {@code item.hashCode() & mask}. This method can be overridden to customizing hashing. If you
	 * aren't confident that the hashCode() implementation used by item will have reasonable quality, you can override
	 * this with something such as {@code return (int)(item.hashCode() * 0x9E3779B97F4A7C15L >>> shift);}. That "magic
	 * number" is 2 to the 64, divided by the golden ratio; the golden ratio is used because of various properties it
	 * has that make it better at randomizing bits. You should usually override this method if you also override
	 * {@link #equate(Object, Object)}, because two equal values should have the same hash.
	 * @param item any non-null Object
	 * @return an index between 0 and {@link #mask} (both inclusive)
	 */
	protected int place (Object item) {
		return item.hashCode() & mask;
	}

	/**
	 * Compares the objects left and right, which are usually keys, for equality, returning true if they are considered
	 * equal. This is used by the rest of this class to determine whether two keys are considered equal. Normally, this
	 * returns {@code left.equals(right)}, but subclasses can override it to use reference equality, fuzzy equality, deep
	 * array equality, or any other custom definition of equality. Usually, {@link #place(Object)} is also overridden if
	 * this method is.
	 * @param left must be non-null; typically a key being compared, but not necessarily
	 * @param right may be null; typically a key being compared, but can often be null for an empty key slot, or some other type
	 * @return true if left and right are considered equal for the purposes of this class
	 */
	protected boolean equate(Object left, Object right){
		return left.equals(right);
	}

	/**
	 * Returns the index of the key if already present, else {@code ~index} for the next empty index. This calls
	 * {@link #equate(Object, Object)} to determine if two keys are equivalent.
	 * @param key a non-null K key
	 * @return a negative index if the key was not found, or the non-negative index of the existing key if found
	 */
	protected int locateKey (Object key) {
		T[] keyTable = this.keyTable;
		for (int i = place(key);; i = i + 1 & mask) {
			T other = keyTable[i];
			if (equate(key, other)) return i; // Same key was found.
			if (other == null) return ~i; // Always negative; means empty space is available at i.
		}
	}

	/**
	 * Returns true if the key was not already in the set. If this set already contains the key, the call leaves the set unchanged
	 * and returns false.
	 */
	@Override
	public boolean add (T key) {
		T[] keyTable = this.keyTable;
		for (int i = place(key);; i = i + 1 & mask) {
			T other = keyTable[i];
			if (equate(key, other)) return false; // Existing key was found.
			if (other == null) {
				keyTable[i] = key;
				if (++size >= threshold) { resize(keyTable.length << 1); }
				return true;
			}
		}
	}

	@Override
	public boolean containsAll (Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) { return false; }
		}
		return true;
	}

	@Override
	public boolean addAll (Collection<? extends T> coll) {
		final int length = coll.size();
		ensureCapacity(length);
		int oldSize = size;
		for (T t : coll) { add(t); }
		return oldSize != size;

	}

	@Override
	public boolean retainAll (Collection<?> c) {
		boolean modified = false;
		for (Object o : this) {
			if (!c.contains(o)) { modified |= remove(o); }

		}
		return modified;
	}

	@Override
	public boolean removeAll (Collection<?> c) {
		boolean modified = false;
		for (Object o : c) {
			modified |= remove(o);
		}
		return modified;
	}

	public boolean addAll (T[] array) {
		return addAll(array, 0, array.length);
	}

	public boolean addAll (T[] array, int offset, int length) {
		ensureCapacity(length);
		int oldSize = size;
		for (int i = offset, n = i + length; i < n; i++) { add(array[i]); }
		return oldSize != size;
	}

	public boolean addAll (ObjectSet<T> set) {
		ensureCapacity(set.size);
		T[] keyTable = set.keyTable;
		int oldSize = size;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			T key = keyTable[i];
			if (key != null) { add(key); }
		}
		return size != oldSize;
	}

	/**
	 * Skips checks for existing keys, doesn't increment size.
	 */
	private void addResize (T key) {
		T[] keyTable = this.keyTable;
		for (int i = place(key); ; i = i + 1 & mask) {
			if (keyTable[i] == null) {
				keyTable[i] = key;
				return;
			}
		}
	}

	/**
	 * Returns true if the key was removed.
	 */
	@Override
	public boolean remove (Object key) {
		int i = locateKey(key);
		if (i < 0) { return false; }
		T[] keyTable = this.keyTable;
		int mask = this.mask, next = i + 1 & mask;
		while ((key = keyTable[next]) != null) {
			int placement = place(key);
			if ((next - placement & mask) > (i - placement & mask)) {
				keyTable[i] = (T)key;
				i = next;
			}
			next = next + 1 & mask;
		}
		keyTable[i] = null;
		size--;
		return true;
	}

	/**
	 * Returns true if the set has one or more items.
	 */
	public boolean notEmpty () {
		return size > 0;
	}

	/**
	 * Returns the number of elements in this set (its cardinality).  If this
	 * set contains more than {@code Integer.MAX_VALUE} elements, returns
	 * {@code Integer.MAX_VALUE}.
	 *
	 * @return the number of elements in this set (its cardinality)
	 */
	@Override
	public int size () {
		return size;
	}

	/**
	 * Returns true if the set is empty.
	 */
	@Override
	public boolean isEmpty () {
		return size == 0;
	}

	/**
	 * Reduces the size of the backing arrays to be the specified capacity / loadFactor, or less. If the capacity is already less,
	 * nothing is done. If the set contains more items than the specified capacity, the next highest power of two capacity is used
	 * instead.
	 */
	public void shrink (int maximumCapacity) {
		if (maximumCapacity < 0) { throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity); }
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length > tableSize) { resize(tableSize); }
	}

	/**
	 * Clears the set and reduces the size of the backing arrays to be the specified capacity / loadFactor, if they are larger.
	 * The reduction is done by allocating new arrays, though for large arrays this can be faster than clearing the existing
	 * array.
	 */
	public void clear (int maximumCapacity) {
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length <= tableSize) {
			clear();
			return;
		}
		size = 0;
		resize(tableSize);
	}

	/**
	 * Clears the set, leaving the backing arrays at the current capacity. When the capacity is high and the population is low,
	 * iteration can be unnecessarily slow. {@link #clear(int)} can be used to reduce the capacity.
	 */
	@Override
	public void clear () {
		if (size == 0) { return; }
		size = 0;
		Arrays.fill(keyTable, null);
	}

	@Override
	public boolean contains (Object key) {
		T[] keyTable = this.keyTable;
		for (int i = place(key);; i = i + 1 & mask) {
			T other = keyTable[i];
			if (equate(key, other)) return true;
			if (other == null) return false;
		}
	}

	public T get (T key) {
		T[] keyTable = this.keyTable;
		for (int i = place(key);; i = i + 1 & mask) {
			T other = keyTable[i];
			if (equate(key, other)) return other;
			if (other == null) return null;
		}
	}

	public T first () {
		T[] keyTable = this.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++) { if (keyTable[i] != null) { return keyTable[i]; } }
		throw new IllegalStateException("ObjectSet is empty.");
	}

	/**
	 * Increases the size of the backing array to accommodate the specified number of additional items / loadFactor. Useful before
	 * adding many items to avoid multiple backing array resizes.
	 *
	 * @param additionalCapacity how many additional items this should be able to hold without resizing (probably)
	 */
	public void ensureCapacity (int additionalCapacity) {
		int tableSize = tableSize(size + additionalCapacity, loadFactor);
		if (keyTable.length < tableSize) { resize(tableSize); }
	}

	protected void resize (int newSize) {
		int oldCapacity = keyTable.length;
		threshold = (int)(newSize * loadFactor);
		mask = newSize - 1;
		shift = Long.numberOfLeadingZeros(mask);
		T[] oldKeyTable = keyTable;

		keyTable = (T[])new Object[newSize];

		if (size > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				T key = oldKeyTable[i];
				if (key != null) { addResize(key); }
			}
		}
	}

	@Override
	public Object[] toArray () {
		return toArray(new Object[size()]);
	}

	/**
	 * Returns an array containing all of the elements in this set; the
	 * runtime type of the returned array is that of the specified array.
	 * If the set fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the size of this set.
	 * <br>
	 * Implementation is mostly copied from GWT, but uses Arrays.copyOf() instead of their internal APIs.
	 *
	 * @param a   the array into which the elements of this set are to be
	 *            stored, if it is big enough; otherwise, a new array of the same
	 *            runtime type is allocated for this purpose.
	 * @param <E> must be the same as {@code T} or a superclass/interface of it; not checked
	 * @return an array containing all the elements in this set
	 */
	@Override
	public <E> E[] toArray (E[] a) {
		int size = size();
		if (a.length < size) {
			a = Arrays.copyOf(a, size);
		}
		Object[] result = a;
		Iterator<T> it = iterator();
		for (int i = 0; i < size; ++i) {
			result[i] = it.next();
		}
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

	public float getLoadFactor () {
		return loadFactor;
	}

	public void setLoadFactor (float loadFactor) {
		if (loadFactor <= 0f || loadFactor > 1f) { throw new IllegalArgumentException("loadFactor must be > 0 and <= 1: " + loadFactor); }
		this.loadFactor = loadFactor;
		int tableSize = tableSize(size, loadFactor);
		if (tableSize - 1 != mask) {
			resize(tableSize);
		}
	}

	@Override
	public int hashCode () {
		int h = size;
		T[] keyTable = this.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			T key = keyTable[i];
			if (key != null) { h += key.hashCode(); }
		}
		return h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Set))
			return false;
		Set<?> s = (Set<?>) o;
		if (s.size() != size())
			return false;
		try {
			return containsAll(s);
		} catch (ClassCastException | NullPointerException unused) {
			return false;
		}
	}

	@Override
	public String toString () {
		return '{' + toString(", ") + '}';
	}

	public String toString (String separator) {
		if (size == 0) { return ""; }
		StringBuilder buffer = new StringBuilder(32);
		T[] keyTable = this.keyTable;
		int i = keyTable.length;
		while (i-- > 0) {
			T key = keyTable[i];
			if (key == null) { continue; }
			buffer.append(key == this ? "(this)" : key);
			break;
		}
		while (i-- > 0) {
			T key = keyTable[i];
			if (key == null) { continue; }
			buffer.append(separator);
			buffer.append(key == this ? "(this)" : key);
		}
		return buffer.toString();
	}

	/**
	 * Returns an iterator for the keys in the set. Remove is supported.
	 * <p>
	 * Reuses one of two iterators for this set. For nested or multithreaded
	 * iteration, use {@link ObjectSetIterator#ObjectSetIterator(ObjectSet)}.
	 */
	@Override
	public ObjectSetIterator<T> iterator () {
		if (iterator1 == null || iterator2 == null) {
			iterator1 = new ObjectSetIterator<T>(this);
			iterator2 = new ObjectSetIterator<T>(this);
		}
		if (!iterator1.valid) {
			iterator1.reset();
			iterator1.valid = true;
			iterator2.valid = false;
			return iterator1;
		}
		iterator2.reset();
		iterator2.valid = true;
		iterator1.valid = false;
		return iterator2;
	}

	public static class ObjectSetIterator<T> implements Iterable<T>, Iterator<T> {
		public boolean hasNext;

		final ObjectSet<T> set;
		int nextIndex, currentIndex;
		boolean valid = true;

		public ObjectSetIterator (ObjectSet<T> set) {
			this.set = set;
			reset();
		}

		public void reset () {
			currentIndex = -1;
			nextIndex = -1;
			findNextIndex();
		}

		private void findNextIndex () {
			T[] keyTable = set.keyTable;
			for (int n = set.keyTable.length; ++nextIndex < n; ) {
				if (keyTable[nextIndex] != null) {
					hasNext = true;
					return;
				}
			}
			hasNext = false;
		}

		@Override
		public void remove () {
			int i = currentIndex;
			if (i < 0) { throw new IllegalStateException("next must be called before remove."); }
			T[] keyTable = set.keyTable;
			int mask = set.mask, next = i + 1 & mask;
			T key;
			while ((key = keyTable[next]) != null) {
				int placement = set.place(key);
				if ((next - placement & mask) > (i - placement & mask)) {
					keyTable[i] = key;
					i = next;
				}
				next = next + 1 & mask;
			}
			keyTable[i] = null;
			set.size--;
			if (i != currentIndex) { --nextIndex; }
			currentIndex = -1;
		}

		@Override
		public boolean hasNext () {
			if (!valid) { throw new RuntimeException("#iterator() cannot be used nested."); }
			return hasNext;
		}

		@Override
		public T next () {
			if (!hasNext) { throw new NoSuchElementException(); }
			if (!valid) { throw new RuntimeException("#iterator() cannot be used nested."); }
			T key = set.keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		@Override
		public ObjectSetIterator<T> iterator () {
			return this;
		}

		/**
		 * Returns a new {@link ArrayList} containing the remaining items.
		 * Does not change the position of this iterator.
		 */
		public ArrayList<T> toList () {
			ArrayList<T> list = new ArrayList<T>(set.size);
			int currentIdx = currentIndex, nextIdx = nextIndex;
			boolean hn = hasNext;
			while (hasNext) { list.add(next()); }
			currentIndex = currentIdx;
			nextIndex = nextIdx;
			hasNext = hn;
			return list;
		}
	}

	public static <T> ObjectSet<T> with(T item) {
		ObjectSet<T> set = new ObjectSet<T>(1);
		set.add(item);
		return set;
	}

	@SafeVarargs
	public static <T> ObjectSet<T> with (T... array) {
		return new ObjectSet<T>(array);
	}
}
