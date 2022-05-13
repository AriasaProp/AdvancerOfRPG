package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.math.Interpolation;
import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.Pool;

/**
 * Base class for actions that transition over time using the percent complete.
 *
 * @author Nathan Sweet
 */
abstract public class TemporalAction extends Action {
	private float duration, time;
	@Null
	private Interpolation interpolation;
	private boolean reverse, began, complete;

	public TemporalAction() {
	}

	public TemporalAction(float duration) {
		this.duration = duration;
	}

	public TemporalAction(float duration, @Null Interpolation interpolation) {
		this.duration = duration;
		this.interpolation = interpolation;
	}

	@Override
	public boolean act(float delta) {
		if (complete)
			return true;
		Pool pool = getPool();
		setPool(null); // Ensure this action can't be returned to the pool while executing.
		try {
			if (!began) {
				begin();
				began = true;
			}
			time += delta;
			complete = time >= duration;
			float percent = complete ? 1 : time / duration;
			if (interpolation != null)
				percent = interpolation.apply(percent);
			update(reverse ? 1 - percent : percent);
			if (complete)
				end();
			return complete;
		} finally {
			setPool(pool);
		}
	}

	/**
	 * Called the first time {@link #act(float)} is called. This is a good place to
	 * query the {@link #actor actor's} starting state.
	 */
	protected void begin() {
	}

	/**
	 * Called the last time {@link #act(float)} is called.
	 */
	protected void end() {
	}

	/**
	 * Called each frame.
	 *
	 * @param percent The percentage of completion for this action, growing from 0
	 *                to 1 over the duration. If {@link #setReverse(boolean)
	 *                reversed}, this will shrink from 1 to 0.
	 */
	abstract protected void update(float percent);

	/**
	 * Skips to the end of the transition.
	 */
	public void finish() {
		time = duration;
	}

	@Override
	public void restart() {
		time = 0;
		began = false;
		complete = false;
	}

	@Override
	public void reset() {
		super.reset();
		reverse = false;
		interpolation = null;
	}

	/**
	 * Gets the transition time so far.
	 */
	public float getTime() {
		return time;
	}

	/**
	 * Sets the transition time so far.
	 */
	public void setTime(float time) {
		this.time = time;
	}

	public float getDuration() {
		return duration;
	}

	/**
	 * Sets the length of the transition in seconds.
	 */
	public void setDuration(float duration) {
		this.duration = duration;
	}

	@Null
	public Interpolation getInterpolation() {
		return interpolation;
	}

	public void setInterpolation(@Null Interpolation interpolation) {
		this.interpolation = interpolation;
	}

	public boolean isReverse() {
		return reverse;
	}

	/**
	 * When true, the action's progress will go from 100% to 0%.
	 */
	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	/**
	 * Returns true after {@link #act(float)} has been called where time >=
	 * duration.
	 */
	public boolean isComplete() {
		return complete;
	}
}
