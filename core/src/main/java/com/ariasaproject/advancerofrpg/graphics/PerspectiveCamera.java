package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.math.Vector3;

/**
 * A Camera with perspective projection.
 *
 * @author mzechner
 */
public class PerspectiveCamera extends Camera {
	final Vector3 tmp = new Vector3();
	/**
	 * the field of view of the height, in degrees
	 **/
	public float fieldOfView = 67;

	public PerspectiveCamera() {
	}

	/**
	 * Constructs a new {@link PerspectiveCamera} with the given field of view and
	 * viewport size. The aspect ratio is derived from the viewport size.
	 *
	 * @param fieldOfViewY   the field of view of the height, in degrees, the field
	 *                       of view for the width will be calculated according to
	 *                       the aspect ratio.
	 * @param viewportWidth  the viewport width
	 * @param viewportHeight the viewport height
	 */
	public PerspectiveCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
		this.fieldOfView = fieldOfViewY;
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		update();
	}

	@Override
	public void update() {
		update(true);
	}

	@Override
	public void update(boolean updateFrustum) {
		float aspect = viewportWidth / viewportHeight;
		projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspect);
		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection);
		combined.mul(view);
		if (updateFrustum) {
			invProjectionView.set(combined).inv();
			frustum.update(invProjectionView);
		}
	}
}
