package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.Group;
import com.ariasaproject.advancerofrpg.scenes2d.Stage;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Layout;

public class Widget extends Actor implements Layout {
    private boolean needsLayout = true;
    private boolean fillParent;
    private boolean layoutEnabled = true;

    @Override
    public float getMinWidth() {
        return getPrefWidth();
    }

    @Override
    public float getMinHeight() {
        return getPrefHeight();
    }

    @Override
    public float getPrefWidth() {
        return 0;
    }

    @Override
    public float getPrefHeight() {
        return 0;
    }

    @Override
    public float getMaxWidth() {
        return 0;
    }

    @Override
    public float getMaxHeight() {
        return 0;
    }

    @Override
    public void setLayoutEnabled(boolean enabled) {
        layoutEnabled = enabled;
        if (enabled)
            invalidateHierarchy();
    }

    @Override
    public void validate() {
        if (!layoutEnabled)
            return;
        Group parent = getParent();
        if (fillParent && parent != null) {
            float parentWidth, parentHeight;
            Stage stage = getStage();
            if (stage != null && parent == stage.getRoot()) {
                parentWidth = stage.getWidth();
                parentHeight = stage.getHeight();
            } else {
                parentWidth = parent.getWidth();
                parentHeight = parent.getHeight();
            }
            setSize(parentWidth, parentHeight);
        }
        if (!needsLayout)
            return;
        needsLayout = false;
        layout();
    }

    /**
     * Returns true if the widget's layout has been {@link #invalidate()
     * invalidated}.
     */
    public boolean needsLayout() {
        return needsLayout;
    }

    @Override
    public void invalidate() {
        needsLayout = true;
    }

    @Override
    public void invalidateHierarchy() {
        if (!layoutEnabled)
            return;
        invalidate();
        Group parent = getParent();
        if (parent instanceof Layout)
            ((Layout) parent).invalidateHierarchy();
    }

    @Override
    protected void sizeChanged() {
        invalidate();
    }

    @Override
    public void pack() {
        setSize(getPrefWidth(), getPrefHeight());
        validate();
    }

    @Override
    public void setFillParent(boolean fillParent) {
        this.fillParent = fillParent;
    }

    /**
     * If this method is overridden, the super method or {@link #validate()} should
     * be called to ensure the widget is laid out.
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
    }

    @Override
    public void layout() {
    }
}
