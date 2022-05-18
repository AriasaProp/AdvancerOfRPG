package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.math.Interpolation;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ChangeListener.ChangeEvent;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Disableable;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.Pools;

public class ProgressBar extends Widget implements Disableable {
	final boolean vertical;
	float position;
	boolean disabled;
	private ProgressBarStyle style;
	private float min, max, stepSize;
	private float value, animateFromValue;
	private float animateDuration, animateTime;
	private Interpolation animateInterpolation = Interpolation.linear;
	private Interpolation visualInterpolation = Interpolation.linear;

	public ProgressBar(float min, float max, float stepSize, boolean vertical, Skin skin) {
		this(min, max, stepSize, vertical, skin.get("default-" + (vertical ? "vertical" : "horizontal"), ProgressBarStyle.class));
	}

	public ProgressBar(float min, float max, float stepSize, boolean vertical, Skin skin, String styleName) {
		this(min, max, stepSize, vertical, skin.get(styleName, ProgressBarStyle.class));
	}

	public ProgressBar(float min, float max, float stepSize, boolean vertical, ProgressBarStyle style) {
		if (min > max)
			throw new IllegalArgumentException("max must be > min. min,max: " + min + ", " + max);
		if (stepSize <= 0)
			throw new IllegalArgumentException("stepSize must be > 0: " + stepSize);
		setStyle(style);
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.vertical = vertical;
		this.value = min;
		setSize(getPrefWidth(), getPrefHeight());
	}

	public ProgressBarStyle getStyle() {
		return style;
	}

	public void setStyle(ProgressBarStyle style) {
		if (style == null)
			throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		if (animateTime > 0) {
			animateTime -= delta;
		}
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		ProgressBarStyle style = this.style;
		boolean disabled = this.disabled;
		Drawable knob = style.knob;
		Drawable currentKnob = getKnobDrawable();
		Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
		Drawable knobBefore = (disabled && style.disabledKnobBefore != null) ? style.disabledKnobBefore : style.knobBefore;
		Drawable knobAfter = (disabled && style.disabledKnobAfter != null) ? style.disabledKnobAfter : style.knobAfter;
		Color color = getColor();
		float x = getX();
		float y = getY();
		float width = getWidth();
		float height = getHeight();
		float knobHeight = knob == null ? 0 : knob.getMinHeight();
		float knobWidth = knob == null ? 0 : knob.getMinWidth();
		float percent = getVisualPercent();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (vertical) {
			float positionHeight = height;
			float bgTopHeight = 0, bgBottomHeight = 0;
			if (bg != null) {
				bg.draw(batch, x + width - bg.getMinWidth() * 0.5f, y, bg.getMinWidth(), height);
				bgTopHeight = bg.getTopHeight();
				bgBottomHeight = bg.getBottomHeight();
				positionHeight -= bgTopHeight + bgBottomHeight;
			}
			float knobHeightHalf = 0;
			if (knob == null) {
				knobHeightHalf = knobBefore == null ? 0 : knobBefore.getMinHeight() * 0.5f;
				position = (positionHeight - knobHeightHalf) * percent;
				position = Math.min(positionHeight - knobHeightHalf, position);
			} else {
				knobHeightHalf = knobHeight * 0.5f;
				position = (positionHeight - knobHeight) * percent;
				position = Math.min(positionHeight - knobHeight, position) + bgBottomHeight;
			}
			position = Math.max(Math.min(0, bgBottomHeight), position);
			if (knobBefore != null) {
				knobBefore.draw(batch, x + (width - knobBefore.getMinWidth()) * 0.5f, y + bgTopHeight, knobBefore.getMinWidth(), position + knobHeightHalf);
			}
			if (knobAfter != null) {
				knobAfter.draw(batch, x + (width - knobAfter.getMinWidth()) * 0.5f, y + position + knobHeightHalf, knobAfter.getMinWidth(), height - position - knobHeightHalf - bgBottomHeight);
			}
			if (currentKnob != null) {
				float w = currentKnob.getMinWidth(), h = currentKnob.getMinHeight();
				x += (width - w) * 0.5f;
				y += (knobHeight - h) * 0.5f + position;
				currentKnob.draw(batch, x, y, w, h);
			}
		} else {
			float positionWidth = width;
			float bgLeftWidth = 0, bgRightWidth = 0;
			if (bg != null) {
				bg.draw(batch, x, y + (height - bg.getMinHeight()) * 0.5f, width, bg.getMinHeight());
				bgLeftWidth = bg.getLeftWidth();
				bgRightWidth = bg.getRightWidth();
				positionWidth -= bgLeftWidth + bgRightWidth;
			}
			float knobWidthHalf = 0;
			if (knob == null) {
				knobWidthHalf = knobBefore == null ? 0 : knobBefore.getMinWidth() * 0.5f;
				position = (positionWidth - knobWidthHalf) * percent;
				position = Math.min(positionWidth - knobWidthHalf, position);
			} else {
				knobWidthHalf = knobWidth * 0.5f;
				position = (positionWidth - knobWidth) * percent;
				position = Math.min(positionWidth - knobWidth, position) + bgLeftWidth;
			}
			position = Math.max(Math.min(0, bgLeftWidth), position);
			if (knobBefore != null) {
				knobBefore.draw(batch, x + bgLeftWidth, y + (height - knobBefore.getMinHeight()) * 0.5f, position + knobWidthHalf, knobBefore.getMinHeight());
			}
			if (knobAfter != null) {
				knobAfter.draw(batch, x + position + knobWidthHalf, y + (height - knobAfter.getMinHeight()) * 0.5f, width - position - knobWidthHalf - bgRightWidth, knobAfter.getMinHeight());
			}
			if (currentKnob != null) {
				float w = currentKnob.getMinWidth(), h = currentKnob.getMinHeight();
				x += (knobWidth - w) * 0.5f + position;
				y += (height - h) * 0.5f;
				currentKnob.draw(batch, x, y, w, h);
			}
		}
	}

	public float getValue() {
		return value;
	}

	/**
	 * If {@link #setAnimateDuration(float) animating} the progress bar value, this
	 * returns the value current displayed.
	 */
	public float getVisualValue() {
		if (animateTime > 0)
			return animateInterpolation.apply(animateFromValue, value, 1 - animateTime / animateDuration);
		return value;
	}

	public float getPercent() {
		if (min == max)
			return 0;
		return (value - min) / (max - min);
	}

	public float getVisualPercent() {
		if (min == max)
			return 0;
		return visualInterpolation.apply((getVisualValue() - min) / (max - min));
	}

	@Null
	protected Drawable getKnobDrawable() {
		return (disabled && style.disabledKnob != null) ? style.disabledKnob : style.knob;
	}

	/**
	 * Returns progress bar visual position within the range.
	 */
	protected float getKnobPosition() {
		return this.position;
	}

	public boolean setValue(float value) {
		value = clamp(Math.round(value / stepSize) * stepSize);
		float oldValue = this.value;
		if (value == oldValue)
			return false;
		float oldVisualValue = getVisualValue();
		this.value = value;
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		boolean cancelled = fire(changeEvent);
		if (cancelled)
			this.value = oldValue;
		else if (animateDuration > 0) {
			animateFromValue = oldVisualValue;
			animateTime = animateDuration;
		}
		Pools.free(changeEvent);
		return !cancelled;
	}

	/**
	 * Clamps the value to the progress bar's min/max range. This can be overridden
	 * to allow a range different from the progress bar knob's range.
	 */
	protected float clamp(float value) {
		return MathUtils.clamp(value, min, max);
	}

	/**
	 * Sets the range of this progress bar. The progress bar's current value is
	 * clamped to the range.
	 */
	public void setRange(float min, float max) {
		if (min > max)
			throw new IllegalArgumentException("min must be <= max: " + min + " <= " + max);
		this.min = min;
		this.max = max;
		if (value < min)
			setValue(min);
		else if (value > max)
			setValue(max);
	}

	@Override
	public float getPrefWidth() {
		if (vertical) {
			Drawable knob = style.knob;
			Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
			return Math.max(knob == null ? 0 : knob.getMinWidth(), bg == null ? 0 : bg.getMinWidth());
		} else
			return 140;
	}

	@Override
	public float getPrefHeight() {
		if (vertical)
			return 140;
		else {
			Drawable knob = style.knob;
			Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
			return Math.max(knob == null ? 0 : knob.getMinHeight(), bg == null ? 0 : bg.getMinHeight());
		}
	}

	public float getMinValue() {
		return this.min;
	}

	public float getMaxValue() {
		return this.max;
	}

	public float getStepSize() {
		return this.stepSize;
	}

	public void setStepSize(float stepSize) {
		if (stepSize <= 0)
			throw new IllegalArgumentException("steps must be > 0: " + stepSize);
		this.stepSize = stepSize;
	}

	/**
	 * If > 0, changes to the progress bar value via {@link #setValue(float)} will
	 * happen over this duration in seconds.
	 */
	public void setAnimateDuration(float duration) {
		this.animateDuration = duration;
	}

	/**
	 * Sets the interpolation to use for {@link #setAnimateDuration(float)}.
	 */
	public void setAnimateInterpolation(Interpolation animateInterpolation) {
		if (animateInterpolation == null)
			throw new IllegalArgumentException("animateInterpolation cannot be null.");
		this.animateInterpolation = animateInterpolation;
	}

	/**
	 * Sets the interpolation to use for display.
	 */
	public void setVisualInterpolation(Interpolation interpolation) {
		this.visualInterpolation = interpolation;
	}

	public boolean isAnimating() {
		return animateTime > 0;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * True if the progress bar is vertical, false if it is horizontal.
	 **/
	public boolean isVertical() {
		return vertical;
	}

	/**
	 * The style for a progress bar, see {@link ProgressBar}.
	 *
	 * @author mzechner
	 * @author Nathan Sweet
	 */
	static public class ProgressBarStyle {
		/**
		 * The progress bar background, stretched only in one direction. Optional.
		 */
		@Null
		public Drawable background;
		/**
		 * Optional.
		 **/
		@Null
		public Drawable disabledBackground;
		/**
		 * Optional, centered on the background.
		 */
		@Null
		public Drawable knob, disabledKnob;
		/**
		 * Optional.
		 */
		@Null
		public Drawable knobBefore, knobAfter, disabledKnobBefore, disabledKnobAfter;

		public ProgressBarStyle() {
		}

		public ProgressBarStyle(@Null Drawable background, @Null Drawable knob) {
			this.background = background;
			this.knob = knob;
		}

		public ProgressBarStyle(ProgressBarStyle style) {
			this.background = style.background;
			this.disabledBackground = style.disabledBackground;
			this.knob = style.knob;
			this.disabledKnob = style.disabledKnob;
			this.knobBefore = style.knobBefore;
			this.knobAfter = style.knobAfter;
			this.disabledKnobBefore = style.disabledKnobBefore;
			this.disabledKnobAfter = style.disabledKnobAfter;
		}
	}
}
