package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.Touchable;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Layout;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.SnapshotArray;

public class Stack extends WidgetGroup {
    private float prefWidth, prefHeight, minWidth, minHeight, maxWidth, maxHeight;
    private boolean sizeInvalid = true;

    public Stack() {
        setTransform(false);
        setWidth(150);
        setHeight(150);
        setTouchable(Touchable.childrenOnly);
    }

    public Stack(Actor... actors) {
        this();
        for (Actor actor : actors)
            addActor(actor);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        sizeInvalid = true;
    }

    private void computeSize() {
        sizeInvalid = false;
        prefWidth = 0;
        prefHeight = 0;
        minWidth = 0;
        minHeight = 0;
        maxWidth = 0;
        maxHeight = 0;
        SnapshotArray<Actor> children = getChildren();
        for (int i = 0, n = children.size; i < n; i++) {
            Actor child = children.get(i);
            float childMaxWidth, childMaxHeight;
            if (child instanceof Layout) {
                Layout layout = (Layout) child;
                prefWidth = Math.max(prefWidth, layout.getPrefWidth());
                prefHeight = Math.max(prefHeight, layout.getPrefHeight());
                minWidth = Math.max(minWidth, layout.getMinWidth());
                minHeight = Math.max(minHeight, layout.getMinHeight());
                childMaxWidth = layout.getMaxWidth();
                childMaxHeight = layout.getMaxHeight();
            } else {
                prefWidth = Math.max(prefWidth, child.getWidth());
                prefHeight = Math.max(prefHeight, child.getHeight());
                minWidth = Math.max(minWidth, child.getWidth());
                minHeight = Math.max(minHeight, child.getHeight());
                childMaxWidth = 0;
                childMaxHeight = 0;
            }
            if (childMaxWidth > 0)
                maxWidth = maxWidth == 0 ? childMaxWidth : Math.min(maxWidth, childMaxWidth);
            if (childMaxHeight > 0)
                maxHeight = maxHeight == 0 ? childMaxHeight : Math.min(maxHeight, childMaxHeight);
        }
    }

    public void add(Actor actor) {
        addActor(actor);
    }

    @Override
    public void layout() {
        if (sizeInvalid)
            computeSize();
        float width = getWidth(), height = getHeight();
        Array<Actor> children = getChildren();
        for (int i = 0, n = children.size; i < n; i++) {
            Actor child = children.get(i);
            child.setBounds(0, 0, width, height);
            if (child instanceof Layout)
                ((Layout) child).validate();
        }
    }

    @Override
    public float getPrefWidth() {
        if (sizeInvalid)
            computeSize();
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        if (sizeInvalid)
            computeSize();
        return prefHeight;
    }

    @Override
    public float getMinWidth() {
        if (sizeInvalid)
            computeSize();
        return minWidth;
    }

    @Override
    public float getMinHeight() {
        if (sizeInvalid)
            computeSize();
        return minHeight;
    }

    @Override
    public float getMaxWidth() {
        if (sizeInvalid)
            computeSize();
        return maxWidth;
    }

    @Override
    public float getMaxHeight() {
        if (sizeInvalid)
            computeSize();
        return maxHeight;
    }
}
