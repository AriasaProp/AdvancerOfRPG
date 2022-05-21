package com.ariasaproject.advancerofrpg.utils;

abstract public class Pool<T> {
    public final int max;
    private final Array<T> freeObjects;
    public int peak;

    public Pool() {
        this(16, Integer.MAX_VALUE, false);
    }

    public Pool(int initialCapacity) {
        this(initialCapacity, Integer.MAX_VALUE, false);
    }

    public Pool(int initialCapacity, int max) {
        this(initialCapacity, max, false);
    }

    public Pool(int initialCapacity, int max, boolean preFill) {
        if (initialCapacity > max && preFill)
            throw new IllegalArgumentException("max must be larger than initialCapacity if preFill is set to true.");
        freeObjects = new Array<T>(false, initialCapacity);
        this.max = max;
        if (preFill) {
            for (int i = 0; i < initialCapacity; i++)
                freeObjects.add(newObject());
            peak = freeObjects.size;
        }
    }

    abstract protected T newObject();

    public T obtain() {
        return freeObjects.size == 0 ? newObject() : freeObjects.pop();
    }

    public void free(T object) {
        if (object == null)
            throw new IllegalArgumentException("object cannot be null.");
        if (freeObjects.size < max) {
            freeObjects.add(object);
            peak = Math.max(peak, freeObjects.size);
        }
        reset(object);
    }

    public void fill(int size) {
        for (int i = 0; i < size; i++)
            if (freeObjects.size < max)
                freeObjects.add(newObject());
        peak = Math.max(peak, freeObjects.size);
    }

    protected void reset(T object) {
        if (object instanceof Poolable)
            ((Poolable) object).reset();
    }

    public void freeAll(Array<T> objects) {
        if (objects == null)
            throw new IllegalArgumentException("objects cannot be null.");
        Array<T> freeObjects = this.freeObjects;
        int max = this.max;
        for (int i = 0; i < objects.size; i++) {
            T object = objects.get(i);
            if (object == null)
                continue;
            if (freeObjects.size < max)
                freeObjects.add(object);
            reset(object);
        }
        peak = Math.max(peak, freeObjects.size);
    }

    public void clear() {
        freeObjects.clear();
    }

    public int getFree() {
        return freeObjects.size;
    }

    public interface Poolable {
        void reset();
    }
}
