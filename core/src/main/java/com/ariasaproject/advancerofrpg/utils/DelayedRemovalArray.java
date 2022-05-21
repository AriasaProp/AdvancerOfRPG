package com.ariasaproject.advancerofrpg.utils;

import java.util.Comparator;

public class DelayedRemovalArray<T> extends Array<T> {
    private final IntArray remove = new IntArray(0);
    private int iterating;
    private int clear;

    public DelayedRemovalArray() {
        super();
    }

    public DelayedRemovalArray(Array array) {
        super(array);
    }

    public DelayedRemovalArray(boolean ordered, int capacity, Class arrayType) {
        super(ordered, capacity, arrayType);
    }

    public DelayedRemovalArray(boolean ordered, int capacity) {
        super(ordered, capacity);
    }

    public DelayedRemovalArray(boolean ordered, T[] array, int startIndex, int count) {
        super(ordered, array, startIndex, count);
    }

    public DelayedRemovalArray(Class arrayType) {
        super(arrayType);
    }

    public DelayedRemovalArray(int capacity) {
        super(capacity);
    }

    public DelayedRemovalArray(T[] array) {
        super(array);
    }

    /**
     * @see #DelayedRemovalArray(Object[])
     */
    static public <T> DelayedRemovalArray<T> with(T... array) {
        return new DelayedRemovalArray(array);
    }

    public void begin() {
        iterating++;
    }

    public void end() {
        if (iterating == 0)
            throw new IllegalStateException("begin must be called before end.");
        iterating--;
        if (iterating == 0) {
            if (clear > 0 && clear == size) {
                remove.clear();
                clear();
            } else {
                for (int i = 0, n = remove.size; i < n; i++) {
                    int index = remove.pop();
                    if (index >= clear)
                        removeIndex(index);
                }
                for (int i = clear - 1; i >= 0; i--)
                    removeIndex(i);
            }
            clear = 0;
        }
    }

    private void remove(int index) {
        if (index < clear)
            return;
        for (int i = 0, n = remove.size; i < n; i++) {
            int removeIndex = remove.get(i);
            if (index == removeIndex)
                return;
            if (index < removeIndex) {
                remove.insert(i, index);
                return;
            }
        }
        remove.add(index);
    }

    @Override
    public boolean removeValue(T value, boolean identity) {
        if (iterating > 0) {
            int index = indexOf(value, identity);
            if (index == -1)
                return false;
            remove(index);
            return true;
        }
        return super.removeValue(value, identity);
    }

    @Override
    public T removeIndex(int index) {
        if (iterating > 0) {
            remove(index);
            return get(index);
        }
        return super.removeIndex(index);
    }

    @Override
    public void removeRange(int start, int end) {
        if (iterating > 0) {
            for (int i = end; i >= start; i--)
                remove(i);
        } else
            super.removeRange(start, end);
    }

    @Override
    public void clear() {
        if (iterating > 0) {
            clear = size;
            return;
        }
        super.clear();
    }

    @Override
    public void set(int index, T value) {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        super.set(index, value);
    }

    @Override
    public void insert(int index, T value) {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        super.insert(index, value);
    }

    @Override
    public void swap(int first, int second) {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        super.swap(first, second);
    }

    @Override
    public T pop() {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        return super.pop();
    }

    @Override
    public void sort() {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        super.sort();
    }

    @Override
    public void sort(Comparator<? super T> comparator) {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        super.sort(comparator);
    }

    @Override
    public void reverse() {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        super.reverse();
    }

    @Override
    public void shuffle() {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        super.shuffle();
    }

    @Override
    public void truncate(int newSize) {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        super.truncate(newSize);
    }

    @Override
    public T[] setSize(int newSize) {
        if (iterating > 0)
            throw new IllegalStateException("Invalid between begin/end.");
        return super.setSize(newSize);
    }
}
