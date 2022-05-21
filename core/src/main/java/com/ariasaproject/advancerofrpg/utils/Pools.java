package com.ariasaproject.advancerofrpg.utils;

public class Pools {
    static private final ObjectMap<Class<?>, Pool<?>> typePools = new ObjectMap<Class<?>, Pool<?>>();

    private Pools() {
    }

    static public <T> Pool<T> get(Class<T> type, int max) {
        Pool<T> pool = (Pool<T>) typePools.get(type);
        if (pool == null) {
            pool = new ReflectionPool<T>(type, 4, max);
            typePools.put(type, pool);
        }
        return pool;
    }

    static public <T> Pool<T> get(Class<T> type) {
        return get(type, 100);
    }

    static public <T> void set(Class<T> type, Pool<T> pool) {
        typePools.put(type, pool);
    }

    static public <T> T obtain(Class<T> type) {
        return get(type).obtain();
    }

    static public <T> void free(T object) {
        if (object == null)
            throw new IllegalArgumentException("object cannot be null.");
        Pool pool = typePools.get(object.getClass());
        if (pool == null)
            return; // Ignore freeing an object that was never retained.
        pool.free(object);
    }

    static public void freeAll(Array objects) {
        freeAll(objects, false);
    }

    /**
     * Frees the specified objects from the {@link #get(Class) pool}. Null objects
     * within the array are silently ignored.
     *
     * @param samePool If true, objects don't need to be from the same pool but the
     *                 pool must be looked up for each object.
     */
    static public void freeAll(Array objects, boolean samePool) {
        if (objects == null)
            throw new IllegalArgumentException("objects cannot be null.");
        Pool pool = null;
        for (int i = 0, n = objects.size; i < n; i++) {
            Object object = objects.get(i);
            if (object == null)
                continue;
            if (pool == null) {
                pool = typePools.get(object.getClass());
                if (pool == null)
                    continue; // Ignore freeing an object that was never retained.
            }
            pool.free(object);
            if (!samePool)
                pool = null;
        }
    }
}
