package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.math.Circle;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent;
import com.ariasaproject.advancerofrpg.scenes2d.InputListener;
import com.ariasaproject.advancerofrpg.scenes2d.Touchable;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ChangeListener.ChangeEvent;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.Pools;

public class Touchpad extends Widget {
    public final Vector2 knobPosition = new Vector2();
    public final Vector2 knobPercent = new Vector2();
    private final Circle knobBounds = new Circle(0, 0, 0);
    private final Circle touchBounds = new Circle(0, 0, 0);
    boolean touched;
    boolean resetOnTouchUp = true;
    private TouchpadStyle style;

    public Touchpad(Skin skin) {
        this(skin.get(TouchpadStyle.class));
    }

    public Touchpad(Skin skin, String styleName) {
        this(skin.get(styleName, TouchpadStyle.class));
    }

    public Touchpad(TouchpadStyle style) {
        knobPosition.set(getWidth() / 2f, getHeight() / 2f);
        setStyle(style);
        setSize(getPrefWidth(), getPrefHeight());
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (touched)
                    return false;
                touched = true;
                calculatePositionAndValue(x, y, false);
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                calculatePositionAndValue(x, y, false);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                touched = false;
                calculatePositionAndValue(x, y, resetOnTouchUp);
            }
        });
    }

    void calculatePositionAndValue(float x, float y, boolean isTouchUp) {
        float oldPositionX = knobPosition.x;
        float oldPositionY = knobPosition.y;
        float oldPercentX = knobPercent.x;
        float oldPercentY = knobPercent.y;
        float centerX = knobBounds.x;
        float centerY = knobBounds.y;
        knobPosition.set(centerX, centerY);
        knobPercent.set(0f, 0f);
        if (!isTouchUp) {
            knobPercent.set((x - centerX) / knobBounds.radius, (y - centerY) / knobBounds.radius);
            float length = knobPercent.len();
            if (length > 1)
                knobPercent.scl(1 / length);
            if (knobBounds.contains(x, y)) {
                knobPosition.set(x, y);
            } else {
                knobPosition.set(knobPercent).nor().scl(knobBounds.radius).add(knobBounds.x, knobBounds.y);
            }
        }
        if (oldPercentX != knobPercent.x || oldPercentY != knobPercent.y) {
            ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
            if (fire(changeEvent)) {
                knobPercent.set(oldPercentX, oldPercentY);
                knobPosition.set(oldPositionX, oldPositionY);
            }
            Pools.free(changeEvent);
        }
    }

    /**
     * Returns the touchpad's style. Modifying the returned style may not have an
     * effect until {@link #setStyle(TouchpadStyle)} is called.
     */
    public TouchpadStyle getStyle() {
        return style;
    }

    public void setStyle(TouchpadStyle style) {
        if (style == null)
            throw new IllegalArgumentException("style cannot be null");
        this.style = style;
        invalidateHierarchy();
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (touchable && this.getTouchable() != Touchable.enabled)
            return null;
        if (!isVisible())
            return null;
        return touchBounds.contains(x, y) ? this : null;
    }

    @Override
    public void layout() {
        // Recalc pad and deadzone bounds
        float halfWidth = getWidth() / 2;
        float halfHeight = getHeight() / 2;
        float radius = Math.min(halfWidth, halfHeight);
        touchBounds.set(halfWidth, halfHeight, radius);
        if (style.knob != null)
            radius -= Math.max(style.knob.getMinWidth(), style.knob.getMinHeight()) / 2;
        knobBounds.set(halfWidth, halfHeight, radius);
        // Recalc pad values and knob position
        knobPosition.set(halfWidth, halfHeight);
        knobPercent.set(0, 0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();
        final Drawable bg = style.background;
        if (bg != null)
            bg.draw(batch, x, y, w, h);
        final Drawable knob = style.knob;
        if (knob != null) {
            x += knobPosition.x - knob.getMinWidth() / 2f;
            y += knobPosition.y - knob.getMinHeight() / 2f;
            knob.draw(batch, x, y, knob.getMinWidth(), knob.getMinHeight());
        }
    }

    @Override
    public float getPrefWidth() {
        return style.background != null ? style.background.getMinWidth() : 0;
    }

    @Override
    public float getPrefHeight() {
        return style.background != null ? style.background.getMinHeight() : 0;
    }

    public boolean isTouched() {
        return touched;
    }

    public boolean getResetOnTouchUp() {
        return resetOnTouchUp;
    }

    public void setResetOnTouchUp(boolean reset) {
        this.resetOnTouchUp = reset;
    }

    public static class TouchpadStyle {
        @Null
        public Drawable background;
        @Null
        public Drawable knob;

        public TouchpadStyle() {
        }

        public TouchpadStyle(@Null Drawable background, @Null Drawable knob) {
            this.background = background;
            this.knob = knob;
        }

        public TouchpadStyle(TouchpadStyle style) {
            this.background = style.background;
            this.knob = style.knob;
        }
    }
}
