package com.ariasaproject.advancerofrpg.utils;

import com.ariasaproject.advancerofrpg.utils.reflect.ClassReflection;
import com.ariasaproject.advancerofrpg.utils.reflect.Constructor;
import com.ariasaproject.advancerofrpg.utils.reflect.ReflectionException;

/**
 * Pool that creates new instances of a type using reflection. The type must
 * have a zero argument constructor. {@link Constructor#setAccessible(boolean)}
 * will be used if the class and/or constructor is not visible.
 *
 * @author Nathan Sweet
 */
public class ReflectionPool<T> extends Pool<T> {
	private final Constructor constructor;

	public ReflectionPool(Class<T> type) {
		this(type, 16, Integer.MAX_VALUE, false);
	}

	public ReflectionPool(Class<T> type, int initialCapacity) {
		this(type, initialCapacity, Integer.MAX_VALUE, false);
	}

	public ReflectionPool(Class<T> type, int initialCapacity, int max) {
		this(type, initialCapacity, max, false);
	}

	public ReflectionPool(Class<T> type, int initialCapacity, int max, boolean preFill) {
		super(initialCapacity, max, preFill);
		constructor = findConstructor(type);
		if (constructor == null)
			throw new RuntimeException("Class cannot be created (missing no-arg constructor): " + type.getName());
	}

	@Null
	private Constructor findConstructor(Class<T> type) {
		try {
			return ClassReflection.getConstructor(type, (Class[]) null);
		} catch (Exception ex1) {
			try {
				Constructor constructor = ClassReflection.getDeclaredConstructor(type, (Class[]) null);
				constructor.setAccessible(true);
				return constructor;
			} catch (ReflectionException ex2) {
				return null;
			}
		}
	}

	@Override
	protected T newObject() {
		try {
			return (T) constructor.newInstance((Object[]) null);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to create new instance: " + constructor.getDeclaringClass().getName(), ex);
		}
	}
}
