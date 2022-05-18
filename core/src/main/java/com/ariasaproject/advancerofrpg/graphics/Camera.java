package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.math.Frustum;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Quaternion;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.math.collision.Ray;

public abstract class Camera {
	public final Vector3 position = new Vector3(), direction = new Vector3(0, 0, -1), up = new Vector3(0, 1, 0);
	public final Matrix4 projection = new Matrix4(), view = new Matrix4(), combined = new Matrix4(),
			invProjectionView = new Matrix4();
	public final Frustum frustum = new Frustum();
	private final Vector3 tmpVec = new Vector3();
	private final Ray ray = new Ray(new Vector3(), new Vector3());
	public float near = 1, far = 100, viewportWidth = 0, viewportHeight = 0;

	public abstract void update();

	public abstract void update(boolean updateFrustum);

	public void lookAt(float x, float y, float z) {
		tmpVec.set(x, y, z).sub(position).nor();
		if (!tmpVec.isZero()) {
			float dot = tmpVec.dot(up); // up and direction must ALWAYS be orthonormal vectors
			if (Math.abs(dot - 1) < 0.000000001f) {
				// Collinear
				up.set(direction).scl(-1);
			} else if (Math.abs(dot + 1) < 0.000000001f) {
				// Collinear opposite
				up.set(direction);
			}
			direction.set(tmpVec);
			normalizeUp();
		}
	}

	public void normalizeUp() {
		tmpVec.set(direction).crs(up).nor();
		up.set(tmpVec).crs(direction).nor();
	}

	public void rotate(float angle, float axisX, float axisY, float axisZ) {
		direction.rotate(angle, axisX, axisY, axisZ);
		up.rotate(angle, axisX, axisY, axisZ);
	}

	public void rotate(Vector3 axis, float angle) {
		direction.rotate(axis, angle);
		up.rotate(axis, angle);
	}

	public void rotate(final Matrix4 transform) {
		direction.rot(transform);
		up.rot(transform);
	}

	public void rotate(final Quaternion quat) {
		quat.transform(direction);
		quat.transform(up);
	}

	public void rotateAround(Vector3 point, Vector3 axis, float angle) {
		tmpVec.set(point);
		tmpVec.sub(position);
		translate(tmpVec);
		rotate(axis, angle);
		tmpVec.rotate(axis, angle);
		translate(-tmpVec.x, -tmpVec.y, -tmpVec.z);
	}

	public void transform(final Matrix4 transform) {
		position.mul(transform);
		rotate(transform);
	}

	public void translate(float x, float y, float z) {
		position.add(x, y, z);
	}

	public void translate(Vector3 vec) {
		position.add(vec);
	}

	public Vector3 unproject(Vector3 screenCoords, float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
		float x = screenCoords.x, y = screenCoords.y;
		x = x - viewportX;
		y = GraphFunc.app.getGraphics().getHeight() - y;
		y = y - viewportY;
		screenCoords.x = (2 * x) / viewportWidth - 1;
		screenCoords.y = (2 * y) / viewportHeight - 1;
		screenCoords.z = 2 * screenCoords.z - 1;
		screenCoords.prj(invProjectionView);
		return screenCoords;
	}

	public Vector3 unproject(Vector3 screenCoords) {
		unproject(screenCoords, 0, 0, GraphFunc.app.getGraphics().getWidth(), GraphFunc.app.getGraphics().getHeight());
		return screenCoords;
	}

	public Vector3 project(Vector3 worldCoords) {
		project(worldCoords, 0, 0, GraphFunc.app.getGraphics().getWidth(), GraphFunc.app.getGraphics().getHeight());
		return worldCoords;
	}

	public Vector3 project(Vector3 worldCoords, float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
		worldCoords.prj(combined);
		worldCoords.x = viewportWidth * (worldCoords.x + 1) / 2 + viewportX;
		worldCoords.y = viewportHeight * (worldCoords.y + 1) / 2 + viewportY;
		worldCoords.z = (worldCoords.z + 1) / 2;
		return worldCoords;
	}

	public Ray getPickRay(float screenX, float screenY, float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
		unproject(ray.origin.set(screenX, screenY, 0), viewportX, viewportY, viewportWidth, viewportHeight);
		unproject(ray.direction.set(screenX, screenY, 1), viewportX, viewportY, viewportWidth, viewportHeight);
		ray.direction.sub(ray.origin).nor();
		return ray;
	}

	public Ray getPickRay(float screenX, float screenY) {
		return getPickRay(screenX, screenY, 0, 0, GraphFunc.app.getGraphics().getWidth(), GraphFunc.app.getGraphics().getHeight());
	}
}
