package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.math.Vector3;

public class OrthographicCamera extends Camera {
	private final Vector3 tmp = new Vector3();
	public float zoom = 1;

	public OrthographicCamera() {
		this(GraphFunc.app.getGraphics().getWidth(), GraphFunc.app.getGraphics().getHeight());
	}

	public OrthographicCamera(float viewportWidth, float viewportHeight) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		this.near = 0;
		update();
	}

	@Override
	public void update() {
		update(true);
	}

	@Override
	public void update(boolean updateFrustum) {
		projection.setToOrtho(zoom * -viewportWidth / 2, zoom * (viewportWidth / 2), zoom * -(viewportHeight / 2), zoom * viewportHeight / 2, near, far);
		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection);
		combined.mul(view);
		if (updateFrustum) {
			invProjectionView.set(combined).inv();
			frustum.update(invProjectionView);
		}
	}

	public void setToOrtho(boolean yDown) {
		setToOrtho(yDown, GraphFunc.app.getGraphics().getWidth(), GraphFunc.app.getGraphics().getHeight());
	}

	public void setToOrtho(boolean yDown, float viewportWidth, float viewportHeight) {
		if (yDown) {
			up.set(0, -1, 0);
			direction.set(0, 0, 1);
		} else {
			up.set(0, 1, 0);
			direction.set(0, 0, -1);
		}
		position.set(zoom * viewportWidth / 2.0f, zoom * viewportHeight / 2.0f, 0);
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		update();
	}

	public void rotate(float angle) {
		rotate(direction, angle);
	}

	public void translate(float x, float y) {
		translate(x, y, 0);
	}

	public void translate(Vector2 vec) {
		translate(vec.x, vec.y, 0);
	}
}
