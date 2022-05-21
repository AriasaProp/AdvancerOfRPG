package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.math.MathUtils;

/**
 * Sets the actor's rotation from its current value to a specific value.
 * <p>
 * By default, the rotation will take you from the starting value to the
 * specified value via simple subtraction. For example, setting the start at 350
 * and the target at 10 will result in 340 degrees of movement.
 * <p>
 * If the action is instead set to useShortestDirection instead, it will rotate
 * straight to the target angle, regardless of where the angle starts and stops.
 * For example, starting at 350 and rotating to 10 will cause 20 degrees of
 * rotation.
 *
 * @author Nathan Sweet
 * @see com.ariasaproject.advancerofrpg.math.MathUtils#lerpAngleDeg(float,
 * float, float)
 */
public class RotateToAction extends TemporalAction {
    private float start, end;

    private boolean useShortestDirection = false;

    public RotateToAction() {
    }

    /**
     * @param useShortestDirection Set to true to move directly to the closest angle
     */
    public RotateToAction(boolean useShortestDirection) {
        this.useShortestDirection = useShortestDirection;
    }

    @Override
    protected void begin() {
        start = target.getRotation();
    }

    @Override
    protected void update(float percent) {
        float rotation;
        if (percent == 0)
            rotation = start;
        else if (percent == 1)
            rotation = end;
        else if (useShortestDirection)
            rotation = MathUtils.lerpAngleDeg(this.start, this.end, percent);
        else
            rotation = start + (end - start) * percent;
        target.setRotation(rotation);
    }

    public float getRotation() {
        return end;
    }

    public void setRotation(float rotation) {
        this.end = rotation;
    }

    public boolean isUseShortestDirection() {
        return useShortestDirection;
    }

    public void setUseShortestDirection(boolean useShortestDirection) {
        this.useShortestDirection = useShortestDirection;
    }
}
