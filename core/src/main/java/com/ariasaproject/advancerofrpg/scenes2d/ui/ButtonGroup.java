package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Null;

public class ButtonGroup<T extends Button> {
    private final Array<T> buttons = new Array<T>();
    private final Array<T> checkedButtons = new Array<T>(1);
    private int minCheckCount, maxCheckCount = 1;
    private boolean uncheckLast = true;
    private T lastChecked;

    public ButtonGroup() {
        minCheckCount = 1;
    }

    public ButtonGroup(T... buttons) {
        minCheckCount = 0;
        add(buttons);
        minCheckCount = 1;
    }

    public void add(T button) {
        if (button == null)
            throw new IllegalArgumentException("button cannot be null.");
        button.buttonGroup = null;
        boolean shouldCheck = button.isChecked() || buttons.size < minCheckCount;
        button.setChecked(false);
        button.buttonGroup = this;
        buttons.add(button);
        button.setChecked(shouldCheck);
    }

    public void add(T... buttons) {
        if (buttons == null)
            throw new IllegalArgumentException("buttons cannot be null.");
        for (int i = 0, n = buttons.length; i < n; i++)
            add(buttons[i]);
    }

    public void remove(T button) {
        if (button == null)
            throw new IllegalArgumentException("button cannot be null.");
        button.buttonGroup = null;
        buttons.removeValue(button, true);
        checkedButtons.removeValue(button, true);
    }

    public void remove(T... buttons) {
        if (buttons == null)
            throw new IllegalArgumentException("buttons cannot be null.");
        for (int i = 0, n = buttons.length; i < n; i++)
            remove(buttons[i]);
    }

    public void clear() {
        buttons.clear();
        checkedButtons.clear();
    }

    protected boolean canCheck(T button, boolean newState) {
        if (button.isChecked == newState)
            return false;
        if (!newState) {
            // Keep button checked to enforce minCheckCount.
            if (checkedButtons.size <= minCheckCount)
                return false;
            checkedButtons.removeValue(button, true);
        } else {
            // Keep button unchecked to enforce maxCheckCount.
            if (maxCheckCount != -1 && checkedButtons.size >= maxCheckCount) {
                if (uncheckLast) {
                    int old = minCheckCount;
                    minCheckCount = 0;
                    lastChecked.setChecked(false);
                    minCheckCount = old;
                } else
                    return false;
            }
            checkedButtons.add(button);
            lastChecked = button;
        }
        return true;
    }

    public void uncheckAll() {
        int old = minCheckCount;
        minCheckCount = 0;
        for (int i = 0, n = buttons.size; i < n; i++) {
            T button = buttons.get(i);
            button.setChecked(false);
        }
        minCheckCount = old;
    }

    @Null
    public T getChecked() {
        if (checkedButtons.size > 0)
            return checkedButtons.get(0);
        return null;
    }

    public void setChecked(String text) {
        if (text == null)
            throw new IllegalArgumentException("text cannot be null.");
        for (int i = 0, n = buttons.size; i < n; i++) {
            T button = buttons.get(i);
            if (button instanceof TextButton && text.contentEquals(((TextButton) button).getText())) {
                button.setChecked(true);
                return;
            }
        }
    }

    public int getCheckedIndex() {
        if (checkedButtons.size > 0)
            return buttons.indexOf(checkedButtons.get(0), true);
        return -1;
    }

    public Array<T> getAllChecked() {
        return checkedButtons;
    }

    public Array<T> getButtons() {
        return buttons;
    }

    public void setMinCheckCount(int minCheckCount) {
        this.minCheckCount = minCheckCount;
    }

    public void setMaxCheckCount(int maxCheckCount) {
        if (maxCheckCount == 0)
            maxCheckCount = -1;
        this.maxCheckCount = maxCheckCount;
    }

    public void setUncheckLast(boolean uncheckLast) {
        this.uncheckLast = uncheckLast;
    }
}
