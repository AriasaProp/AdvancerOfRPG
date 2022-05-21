package com.ariasaproject.advancerofrpg.scenes2d.utils;

import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.reflect.ClassReflection;

public class BaseDrawable implements Drawable {
    @Null
    private String name;
    private float leftWidth, rightWidth, topHeight, bottomHeight, minWidth, minHeight;

    public BaseDrawable() {
    }

    public BaseDrawable(Drawable drawable) {
        if (drawable instanceof BaseDrawable)
            name = ((BaseDrawable) drawable).getName();
        leftWidth = drawable.getLeftWidth();
        rightWidth = drawable.getRightWidth();
        topHeight = drawable.getTopHeight();
        bottomHeight = drawable.getBottomHeight();
        minWidth = drawable.getMinWidth();
        minHeight = drawable.getMinHeight();
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
    }

    @Override
    public float getLeftWidth() {
        return leftWidth;
    }

    @Override
    public void setLeftWidth(float leftWidth) {
        this.leftWidth = leftWidth;
    }

    @Override
    public float getRightWidth() {
        return rightWidth;
    }

    @Override
    public void setRightWidth(float rightWidth) {
        this.rightWidth = rightWidth;
    }

    @Override
    public float getTopHeight() {
        return topHeight;
    }

    @Override
    public void setTopHeight(float topHeight) {
        this.topHeight = topHeight;
    }

    @Override
    public float getBottomHeight() {
        return bottomHeight;
    }

    @Override
    public void setBottomHeight(float bottomHeight) {
        this.bottomHeight = bottomHeight;
    }

    public void setPadding(float topHeight, float leftWidth, float bottomHeight, float rightWidth) {
        setTopHeight(topHeight);
        setLeftWidth(leftWidth);
        setBottomHeight(bottomHeight);
        setRightWidth(rightWidth);
    }

    @Override
    public float getMinWidth() {
        return minWidth;
    }

    @Override
    public void setMinWidth(float minWidth) {
        this.minWidth = minWidth;
    }

    @Override
    public float getMinHeight() {
        return minHeight;
    }

    @Override
    public void setMinHeight(float minHeight) {
        this.minHeight = minHeight;
    }

    public void setMinSize(float minWidth, float minHeight) {
        setMinWidth(minWidth);
        setMinHeight(minHeight);
    }

    @Null
    public String getName() {
        return name;
    }

    public void setName(@Null String name) {
        this.name = name;
    }

    @Override
    @Null
    public String toString() {
        if (name == null)
            return ClassReflection.getSimpleName(getClass());
        return name;
    }
}
