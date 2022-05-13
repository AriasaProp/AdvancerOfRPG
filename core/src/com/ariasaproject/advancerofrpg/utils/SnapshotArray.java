package com.ariasaproject.advancerofrpg.utils;

import java.util.Comparator;

public class SnapshotArray<T> extends Array<T> {
	private T[] snapshot, recycled;
	private int snapshots;

	public SnapshotArray() {
		super();
	}

	public SnapshotArray(Array<T> array) {
		super(array);
	}

	public SnapshotArray(boolean ordered, int capacity, Class<T> arrayType) {
		super(ordered, capacity, arrayType);
	}

	public SnapshotArray(boolean ordered, int capacity) {
		super(ordered, capacity);
	}

	public SnapshotArray(boolean ordered, T[] array, int startIndex, int count) {
		super(ordered, array, startIndex, count);
	}

	public SnapshotArray(Class<T> arrayType) {
		super(arrayType);
	}

	public SnapshotArray(int capacity) {
		super(capacity);
	}

	public SnapshotArray(T[] array) {
		super(array);
	}

	static public <T> SnapshotArray<T> with(T... array) {
		return new SnapshotArray<T>(array);
	}

	public T[] begin() {
		modified();
		snapshot = items;
		snapshots++;
		return items;
	}

	public void end() {
		snapshots = Math.max(0, snapshots - 1);
		if (snapshot == null)
			return;
		if (snapshot != items && snapshots == 0) {
			recycled = snapshot;
			for (int i = 0, n = recycled.length; i < n; i++)
				recycled[i] = null;
		}
		snapshot = null;
	}

	private void modified() {
		if (snapshot == null || snapshot != items)
			return;
		if (recycled != null && recycled.length >= size) {
			System.arraycopy(items, 0, recycled, 0, size);
			items = recycled;
			recycled = null;
		} else
			resize(items.length);
	}

	@Override
	public void set(int index, T value) {
		modified();
		super.set(index, value);
	}

	@Override
	public void insert(int index, T value) {
		modified();
		super.insert(index, value);
	}

	@Override
	public void swap(int first, int second) {
		modified();
		super.swap(first, second);
	}

	@Override
	public boolean removeValue(T value, boolean identity) {
		modified();
		return super.removeValue(value, identity);
	}

	@Override
	public T removeIndex(int index) {
		modified();
		return super.removeIndex(index);
	}

	@Override
	public void removeRange(int start, int end) {
		modified();
		super.removeRange(start, end);
	}

	@Override
	public boolean removeAll(Array<? extends T> array, boolean identity) {
		modified();
		return super.removeAll(array, identity);
	}

	@Override
	public T pop() {
		modified();
		return super.pop();
	}

	@Override
	public void clear() {
		modified();
		super.clear();
	}

	@Override
	public void sort() {
		modified();
		super.sort();
	}

	@Override
	public void sort(Comparator<? super T> comparator) {
		modified();
		super.sort(comparator);
	}

	@Override
	public void reverse() {
		modified();
		super.reverse();
	}

	@Override
	public void shuffle() {
		modified();
		super.shuffle();
	}

	@Override
	public void truncate(int newSize) {
		modified();
		super.truncate(newSize);
	}

	@Override
	public T[] setSize(int newSize) {
		modified();
		return super.setSize(newSize);
	}
}
