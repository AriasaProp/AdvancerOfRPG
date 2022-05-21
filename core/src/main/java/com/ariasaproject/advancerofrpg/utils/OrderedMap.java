package com.ariasaproject.advancerofrpg.utils;

import java.util.Collections;
import java.util.NoSuchElementException;

public class OrderedMap<K, V> extends ObjectMap<K, V> {
    final Array<K> keys;

    public OrderedMap() {
        keys = new Array();
    }

    public OrderedMap(int initialCapacity) {
        super(initialCapacity);
        keys = new Array(initialCapacity);
    }

    public OrderedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        keys = new Array(initialCapacity);
    }

    public OrderedMap(OrderedMap<? extends K, ? extends V> map) {
        super(map);
        keys = new Array(map.keys);
    }

    @Override
    public V put(K key, V value) {
        int i = locateKey(key);
        if (i >= 0) { // Existing key was found.
            V oldValue = valueTable[i];
            valueTable[i] = value;
            return oldValue;
        }
        i = -(i + 1); // Empty space was found.
        keyTable[i] = key;
        valueTable[i] = value;
        keys.add(key);
        if (++size >= threshold)
            resize(keyTable.length << 1);
        return null;
    }

    public <T extends K> void putAll(OrderedMap<T, ? extends V> map) {
        ensureCapacity(map.size);
        K[] keys = map.keys.items;
        for (int i = 0, n = map.keys.size; i < n; i++) {
            K key = keys[i];
            put(key, map.get((T) key));
        }
    }

    @Override
    public V remove(K key) {
        keys.removeValue(key, false);
        return super.remove(key);
    }

    public V removeIndex(int index) {
        return super.remove(keys.removeIndex(index));
    }

    /**
     * Changes the key {@code before} to {@code after} without changing its position
     * in the order or its value. Returns true if {@code after} has been added to
     * the OrderedMap and {@code before} has been removed; returns false if
     * {@code after} is already present or {@code before} is not present. If you are
     * iterating over an OrderedMap and have an index, you should prefer
     * {@link #alterIndex(int, Object)}, which doesn't need to search for an index
     * like this does and so can be faster.
     *
     * @param before a key that must be present for this to succeed
     * @param after  a key that must not be in this map for this to succeed
     * @return true if {@code before} was removed and {@code after} was added, false
     * otherwise
     */
    public boolean alter(K before, K after) {
        if (containsKey(after))
            return false;
        int index = keys.indexOf(before, false);
        if (index == -1)
            return false;
        super.put(after, super.remove(before));
        keys.set(index, after);
        return true;
    }

    /**
     * Changes the key at the given {@code index} in the order to {@code after},
     * without changing the ordering of other entries or any values. If
     * {@code after} is already present, this returns false; it will also return
     * false if {@code index} is invalid for the size of this map. Otherwise, it
     * returns true. Unlike {@link #alter(Object, Object)}, this operates in
     * constant time.
     *
     * @param index the index in the order of the key to change; must be
     *              non-negative and less than {@link #size}
     * @param after the key that will replace the contents at {@code index}; this
     *              key must not be present for this to succeed
     * @return true if {@code after} successfully replaced the key at {@code index},
     * false otherwise
     */
    public boolean alterIndex(int index, K after) {
        if (index < 0 || index >= size || containsKey(after))
            return false;
        super.put(after, super.remove(keys.get(index)));
        keys.set(index, after);
        return true;
    }

    @Override
    public void clear(int maximumCapacity) {
        keys.clear();
        super.clear(maximumCapacity);
    }

    @Override
    public void clear() {
        keys.clear();
        super.clear();
    }

    public Array<K> orderedKeys() {
        return keys;
    }

    @Override
    public Entries<K, V> iterator() {
        return entries();
    }

    /**
     * Returns an iterator for the entries in the map. Remove is supported.
     * <p>
     * If {@link Collections#allocateIterators} is false, the same iterator instance
     * is returned each time this method is called. Use the
     * {@link OrderedMapEntries} constructor for nested or multithreaded iteration.
     */
    @Override
    public Entries<K, V> entries() {
        if (entries1 == null) {
            entries1 = new OrderedMapEntries(this);
            entries2 = new OrderedMapEntries(this);
        }
        if (!entries1.valid) {
            entries1.reset();
            entries1.valid = true;
            entries2.valid = false;
            return entries1;
        }
        entries2.reset();
        entries2.valid = true;
        entries1.valid = false;
        return entries2;
    }

    /**
     * Returns an iterator for the values in the map. Remove is supported.
     * <p>
     * If {@link Collections#allocateIterators} is false, the same iterator instance
     * is returned each time this method is called. Use the {@link OrderedMapValues}
     * constructor for nested or multithreaded iteration.
     */
    @Override
    public Values<V> values() {
        if (values1 == null) {
            values1 = new OrderedMapValues(this);
            values2 = new OrderedMapValues(this);
        }
        if (!values1.valid) {
            values1.reset();
            values1.valid = true;
            values2.valid = false;
            return values1;
        }
        values2.reset();
        values2.valid = true;
        values1.valid = false;
        return values2;
    }

    /**
     * Returns an iterator for the keys in the map. Remove is supported.
     * <p>
     * If {@link Collections#allocateIterators} is false, the same iterator instance
     * is returned each time this method is called. Use the {@link OrderedMapKeys}
     * constructor for nested or multithreaded iteration.
     */
    @Override
    public Keys<K> keys() {
        if (keys1 == null) {
            keys1 = new OrderedMapKeys(this);
            keys2 = new OrderedMapKeys(this);
        }
        if (!keys1.valid) {
            keys1.reset();
            keys1.valid = true;
            keys2.valid = false;
            return keys1;
        }
        keys2.reset();
        keys2.valid = true;
        keys1.valid = false;
        return keys2;
    }

    @Override
    protected String toString(String separator, boolean braces) {
        if (size == 0)
            return braces ? "{}" : "";
        java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
        if (braces)
            buffer.append('{');
        Array<K> keys = this.keys;
        for (int i = 0, n = keys.size; i < n; i++) {
            K key = keys.get(i);
            if (i > 0)
                buffer.append(separator);
            buffer.append(key == this ? "(this)" : key);
            buffer.append('=');
            V value = get(key);
            buffer.append(value == this ? "(this)" : value);
        }
        if (braces)
            buffer.append('}');
        return buffer.toString();
    }

    static public class OrderedMapEntries<K, V> extends Entries<K, V> {
        private final Array<K> keys;

        public OrderedMapEntries(OrderedMap<K, V> map) {
            super(map);
            keys = map.keys;
        }

        @Override
        public void reset() {
            currentIndex = -1;
            nextIndex = 0;
            hasNext = map.size > 0;
        }

        @Override
        public Entry next() {
            if (!hasNext)
                throw new NoSuchElementException();
            if (!valid)
                throw new RuntimeException("#iterator() cannot be used nested.");
            currentIndex = nextIndex;
            entry.key = keys.get(nextIndex);
            entry.value = map.get(entry.key);
            nextIndex++;
            hasNext = nextIndex < map.size;
            return entry;
        }

        @Override
        public void remove() {
            if (currentIndex < 0)
                throw new IllegalStateException("next must be called before remove.");
            map.remove(entry.key);
            nextIndex--;
            currentIndex = -1;
        }
    }

    static public class OrderedMapKeys<K> extends Keys<K> {
        private final Array<K> keys;

        public OrderedMapKeys(OrderedMap<K, ?> map) {
            super(map);
            keys = map.keys;
        }

        @Override
        public void reset() {
            currentIndex = -1;
            nextIndex = 0;
            hasNext = map.size > 0;
        }

        @Override
        public K next() {
            if (!hasNext)
                throw new NoSuchElementException();
            if (!valid)
                throw new RuntimeException("#iterator() cannot be used nested.");
            K key = keys.get(nextIndex);
            currentIndex = nextIndex;
            nextIndex++;
            hasNext = nextIndex < map.size;
            return key;
        }

        @Override
        public void remove() {
            if (currentIndex < 0)
                throw new IllegalStateException("next must be called before remove.");
            ((OrderedMap) map).removeIndex(currentIndex);
            nextIndex = currentIndex;
            currentIndex = -1;
        }

        @Override
        public Array<K> toArray(Array<K> array) {
            array.addAll(keys, nextIndex, keys.size - nextIndex);
            nextIndex = keys.size;
            hasNext = false;
            return array;
        }

        @Override
        public Array<K> toArray() {
            return toArray(new Array(true, keys.size - nextIndex));
        }
    }

    static public class OrderedMapValues<V> extends Values<V> {
        private final Array keys;

        public OrderedMapValues(OrderedMap<?, V> map) {
            super(map);
            keys = map.keys;
        }

        @Override
        public void reset() {
            currentIndex = -1;
            nextIndex = 0;
            hasNext = map.size > 0;
        }

        @Override
        public V next() {
            if (!hasNext)
                throw new NoSuchElementException();
            if (!valid)
                throw new RuntimeException("#iterator() cannot be used nested.");
            V value = map.get(keys.get(nextIndex));
            currentIndex = nextIndex;
            nextIndex++;
            hasNext = nextIndex < map.size;
            return value;
        }

        @Override
        public void remove() {
            if (currentIndex < 0)
                throw new IllegalStateException("next must be called before remove.");
            ((OrderedMap) map).removeIndex(currentIndex);
            nextIndex = currentIndex;
            currentIndex = -1;
        }

        @Override
        public Array<V> toArray(Array<V> array) {
            int n = keys.size;
            array.ensureCapacity(n - nextIndex);
            Object[] keys = this.keys.items;
            for (int i = nextIndex; i < n; i++)
                array.add(map.get(keys[i]));
            currentIndex = n - 1;
            nextIndex = n;
            hasNext = false;
            return array;
        }

        @Override
        public Array<V> toArray() {
            return toArray(new Array(true, keys.size - nextIndex));
        }
    }
}
