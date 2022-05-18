package com.ariasaproject.advancerofrpg.scenes2d.utils;

import java.util.Iterator;

import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ChangeListener.ChangeEvent;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.OrderedSet;
import com.ariasaproject.advancerofrpg.utils.Pools;

/**
 * Manages selected objects. Optionally fires a {@link ChangeEvent} on an actor.
 * Selection changes can be vetoed via {@link ChangeEvent#cancel()}.
 *
 * @author Nathan Sweet
 */
public class Selection<T> implements Disableable, Iterable<T> {
	final OrderedSet<T> selected = new OrderedSet<T>();
	private final OrderedSet<T> old = new OrderedSet<T>();
	boolean isDisabled;
	boolean multiple;
	boolean required;
	@Null
	T lastSelected;
	@Null
	private Actor actor;
	private boolean toggle;
	private boolean programmaticChangeEvents = true;

	/**
	 * @param actor An actor to fire {@link ChangeEvent} on when the selection
	 *              changes, or null.
	 */
	public void setActor(@Null Actor actor) {
		this.actor = actor;
	}

	/**
	 * Selects or deselects the specified item based on how the selection is
	 * configured, whether ctrl is currently pressed, etc. This is typically invoked
	 * by user interaction.
	 */
	public void choose(T item) {
		if (item == null)
			throw new IllegalArgumentException("item cannot be null.");
		if (isDisabled)
			return;
		snapshot();
		try {
			if ((toggle) && selected.contains(item)) {
				if (required && selected.size == 1)
					return;
				selected.remove(item);
				lastSelected = null;
			} else {
				boolean modified = false;
				if (!multiple || !toggle) {
					if (selected.size == 1 && selected.contains(item))
						return;
					modified = selected.size > 0;
					selected.clear(8);
				}
				if (!selected.add(item) && !modified)
					return;
				lastSelected = item;
			}
			if (fireChangeEvent())
				revert();
			else
				changed();
		} finally {
			cleanup();
		}
	}

	public boolean notEmpty() {
		return selected.size > 0;
	}

	public boolean isEmpty() {
		return selected.size == 0;
	}

	public int size() {
		return selected.size;
	}

	public OrderedSet<T> items() {
		return selected;
	}

	/**
	 * Returns the first selected item, or null.
	 */
	@Null
	public T first() {
		return selected.size == 0 ? null : selected.first();
	}

	void snapshot() {
		old.clear(selected.size);
		old.addAll(selected);
	}

	void revert() {
		selected.clear(old.size);
		selected.addAll(old);
	}

	void cleanup() {
		old.clear(32);
	}

	/**
	 * Sets the selection to only the specified item.
	 */
	public void set(T item) {
		if (item == null)
			throw new IllegalArgumentException("item cannot be null.");
		if (selected.size == 1 && selected.first() == item)
			return;
		snapshot();
		selected.clear(8);
		selected.add(item);
		if (programmaticChangeEvents && fireChangeEvent())
			revert();
		else {
			lastSelected = item;
			changed();
		}
		cleanup();
	}

	public void setAll(Array<T> items) {
		boolean added = false;
		snapshot();
		lastSelected = null;
		selected.clear(items.size);
		for (int i = 0, n = items.size; i < n; i++) {
			T item = items.get(i);
			if (item == null)
				throw new IllegalArgumentException("item cannot be null.");
			if (selected.add(item))
				added = true;
		}
		if (added) {
			if (programmaticChangeEvents && fireChangeEvent())
				revert();
			else if (items.size > 0) {
				lastSelected = items.peek();
				changed();
			}
		}
		cleanup();
	}

	/**
	 * Adds the item to the selection.
	 */
	public void add(T item) {
		if (item == null)
			throw new IllegalArgumentException("item cannot be null.");
		if (!selected.add(item))
			return;
		if (programmaticChangeEvents && fireChangeEvent())
			selected.remove(item);
		else {
			lastSelected = item;
			changed();
		}
	}

	public void addAll(Array<T> items) {
		boolean added = false;
		snapshot();
		for (int i = 0, n = items.size; i < n; i++) {
			T item = items.get(i);
			if (item == null)
				throw new IllegalArgumentException("item cannot be null.");
			if (selected.add(item))
				added = true;
		}
		if (added) {
			if (programmaticChangeEvents && fireChangeEvent())
				revert();
			else {
				lastSelected = items.peek();
				changed();
			}
		}
		cleanup();
	}

	public void remove(T item) {
		if (item == null)
			throw new IllegalArgumentException("item cannot be null.");
		if (!selected.remove(item))
			return;
		if (programmaticChangeEvents && fireChangeEvent())
			selected.add(item);
		else {
			lastSelected = null;
			changed();
		}
	}

	public void removeAll(Array<T> items) {
		boolean removed = false;
		snapshot();
		for (int i = 0, n = items.size; i < n; i++) {
			T item = items.get(i);
			if (item == null)
				throw new IllegalArgumentException("item cannot be null.");
			if (selected.remove(item))
				removed = true;
		}
		if (removed) {
			if (programmaticChangeEvents && fireChangeEvent())
				revert();
			else {
				lastSelected = null;
				changed();
			}
		}
		cleanup();
	}

	public void clear() {
		if (selected.size == 0)
			return;
		snapshot();
		selected.clear(8);
		if (programmaticChangeEvents && fireChangeEvent())
			revert();
		else {
			lastSelected = null;
			changed();
		}
		cleanup();
	}

	/**
	 * Called after the selection changes. The default implementation does nothing.
	 */
	protected void changed() {
	}

	/**
	 * Fires a change event on the selection's actor, if any. Called internally when
	 * the selection changes, depending on
	 * {@link #setProgrammaticChangeEvents(boolean)}.
	 *
	 * @return true if the change should be undone.
	 */
	public boolean fireChangeEvent() {
		if (actor == null)
			return false;
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		try {
			return actor.fire(changeEvent);
		} finally {
			Pools.free(changeEvent);
		}
	}

	/**
	 * @param item May be null (returns false).
	 */
	public boolean contains(@Null T item) {
		if (item == null)
			return false;
		return selected.contains(item);
	}

	/**
	 * Makes a best effort to return the last item selected, else returns an
	 * arbitrary item or null if the selection is empty.
	 */
	@Null
	public T getLastSelected() {
		if (lastSelected != null) {
			return lastSelected;
		} else if (selected.size > 0) {
			return selected.first();
		}
		return null;
	}

	@Override
	public Iterator<T> iterator() {
		return selected.iterator();
	}

	public Array<T> toArray() {
		return selected.iterator().toArray();
	}

	public Array<T> toArray(Array<T> array) {
		return selected.iterator().toArray(array);
	}

	@Override
	public boolean isDisabled() {
		return isDisabled;
	}

	/**
	 * If true, prevents {@link #choose(Object)} from changing the selection.
	 * Default is false.
	 */
	@Override
	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public boolean getToggle() {
		return toggle;
	}

	/**
	 * If true, prevents {@link #choose(Object)} from clearing the selection.
	 * Default is false.
	 */
	public void setToggle(boolean toggle) {
		this.toggle = toggle;
	}

	public boolean getMultiple() {
		return multiple;
	}

	/**
	 * If true, allows {@link #choose(Object)} to select multiple items. Default is
	 * false.
	 */
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public boolean getRequired() {
		return required;
	}

	/**
	 * If true, prevents {@link #choose(Object)} from selecting none. Default is
	 * false.
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * If false, only {@link #choose(Object)} will fire a change event. Default is
	 * true.
	 */
	public void setProgrammaticChangeEvents(boolean programmaticChangeEvents) {
		this.programmaticChangeEvents = programmaticChangeEvents;
	}

	@Override
	public String toString() {
		return selected.toString();
	}
}
