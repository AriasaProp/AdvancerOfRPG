package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.utils.Null;

/**
 * Sets the alpha for an actor's color (or a specified color), from the current
 * alpha to the new alpha. Note this action transitions from the alpha at the
 * time the action starts to the specified alpha.
 *
 * @author Nathan Sweet
 */
public class AlphaAction extends TemporalAction {
	private float start, end;
	@Null
	private Color color;

	@Override
	protected void begin() {
		if (color == null)
			color = target.getColor();
		start = color.a;
	}

	@Override
	protected void update(float percent) {
		if (percent == 0)
			color.a = start;
		else if (percent == 1)
			color.a = end;
		else
			color.a = start + (end - start) * percent;
	}

	@Override
	public void reset() {
		super.reset();
		color = null;
	}

	@Null
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color to modify. If null (the default), the {@link #getActor()
	 * actor's} {@link Actor#getColor() color} will be used.
	 */
	public void setColor(@Null Color color) {
		this.color = color;
	}

	public float getAlpha() {
		return end;
	}

	public void setAlpha(float alpha) {
		this.end = alpha;
	}
}
