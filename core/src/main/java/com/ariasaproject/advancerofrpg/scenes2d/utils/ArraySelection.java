package com.ariasaproject.advancerofrpg.scenes2d.utils;

import com.ariasaproject.advancerofrpg.utils.Array;

import java.util.Iterator;

public class ArraySelection<T> extends Selection<T> {
    private final Array<T> array;
    private boolean rangeSelect = true;
    private T rangeStart;

    public ArraySelection(Array<T> array) {
        this.array = array;
    }

    @Override
    public void choose(T item) {
        if (item == null)
            throw new IllegalArgumentException("item cannot be null.");
        if (isDisabled)
            return;
        if (!rangeSelect || !multiple) {
            super.choose(item);
            return;
        }
        if (selected.size > 0) {
            int rangeStartIndex = rangeStart == null ? -1 : array.indexOf(rangeStart, false);
            if (rangeStartIndex != -1) {
                T oldRangeStart = rangeStart;
                snapshot();
                // Select new range.
                int start = rangeStartIndex, end = array.indexOf(item, false);
                if (start > end) {
                    int temp = end;
                    end = start;
                    start = temp;
                }
                selected.clear(8);
                for (int i = start; i <= end; i++)
                    selected.add(array.get(i));
                if (fireChangeEvent())
                    revert();
                else
                    changed();
                rangeStart = oldRangeStart;
                cleanup();
                return;
            }
        }
        super.choose(item);
        rangeStart = item;
    }

    /**
     * Called after the selection changes, clears the range start item.
     */
    @Override
    protected void changed() {
        rangeStart = null;
    }

    public boolean getRangeSelect() {
        return rangeSelect;
    }

    public void setRangeSelect(boolean rangeSelect) {
        this.rangeSelect = rangeSelect;
    }

    /**
     * Removes objects from the selection that are no longer in the items array. If
     * {@link #getRequired()} is true and there is no selected item, the first item
     * is selected.
     */
    public void validate() {
        Array<T> array = this.array;
        if (array.size == 0) {
            clear();
            return;
        }
        boolean changed = false;
        for (Iterator<T> iter = items().iterator(); iter.hasNext(); ) {
            T selected = iter.next();
            if (!array.contains(selected, false)) {
                iter.remove();
                changed = true;
            }
        }
        if (required && selected.size == 0)
            set(array.first());
        else if (changed) //
            changed();
    }
}
