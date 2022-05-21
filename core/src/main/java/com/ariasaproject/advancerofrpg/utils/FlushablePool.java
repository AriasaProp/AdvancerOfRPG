package com.ariasaproject.advancerofrpg.utils;

public abstract class FlushablePool<T> extends Pool<T> {
    protected Array<T> obtained = new Array<T>();

    public FlushablePool() {
        super();
    }

    public FlushablePool(int initialCapacity) {
        super(initialCapacity);
    }

    public FlushablePool(int initialCapacity, int max) {
        super(initialCapacity, max);
    }

    public FlushablePool(int initialCapacity, int max, boolean preFill) {
        super(initialCapacity, max, preFill);
    }

    @Override
    public T obtain() {
        T result = super.obtain();
        obtained.add(result);
        return result;
    }

    public void flush() {
        super.freeAll(obtained);
        obtained.clear();
    }

    @Override
    public void free(T object) {
        obtained.removeValue(object, true);
        super.free(object);
    }

    @Override
    public void freeAll(Array<T> objects) {
        obtained.removeAll(objects, true);
        super.freeAll(objects);
    }
}
