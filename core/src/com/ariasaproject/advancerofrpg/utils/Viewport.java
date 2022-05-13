package com.ariasaproject.advancerofrpg.utils;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Camera;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Rectangle;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.math.collision.Ray;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ScissorStack;

public class Viewport {
	private final Vector3 tmp = new Vector3();
	private final Camera camera;
	protected final int minWidth, minHeight, maxWidth, maxHeight;
	private int screenWidth, screenHeight;

	public Viewport(Camera camera, int minWidth, int minHeight, int maxWidth, int maxHeight) {
		this.screenWidth = this.minWidth = minWidth;
		this.screenHeight = this.minHeight = minHeight;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.camera = camera;
	}

	public Camera getCamera() {
		return camera;
	}

	public void update() {
		screenWidth = MathUtils.clamp(GraphFunc.app.getGraphics().getWidth(), minWidth, maxWidth);
		screenHeight = MathUtils.clamp(GraphFunc.app.getGraphics().getHeight(), minHeight, maxHeight);
		camera.viewportWidth = screenWidth;
		camera.viewportHeight = screenHeight;
		camera.position.set(screenWidth / 2, screenHeight / 2, 0);
		camera.update();
	}

	public Vector2 unproject(Vector2 screenCoords) {
		tmp.set(screenCoords.x, screenCoords.y, 1);
		camera.unproject(tmp, 0, 0, screenWidth, screenHeight);
		screenCoords.set(tmp.x, tmp.y);
		return screenCoords;
	}

	public Vector2 project(Vector2 worldCoords) {
		tmp.set(worldCoords.x, worldCoords.y, 1);
		camera.project(tmp, 0, 0, screenWidth, screenHeight);
		worldCoords.set(tmp.x, tmp.y);
		return worldCoords;
	}

	public Vector3 unproject(Vector3 screenCoords) {
		camera.unproject(screenCoords, 0, 0, screenWidth, screenHeight);
		return screenCoords;
	}

	public Vector3 project(Vector3 worldCoords) {
		camera.project(worldCoords, 0, 0, screenWidth, screenHeight);
		return worldCoords;
	}

	public Ray getPickRay(float screenX, float screenY) {
		return camera.getPickRay(screenX, screenY, 0, 0, screenWidth, screenHeight);
	}

	public void calculateScissors(Matrix4 batchTransform, Rectangle area, Rectangle scissor) {
		ScissorStack.calculateScissors(camera, 0, 0, screenWidth, screenHeight, batchTransform, area, scissor);
	}

	public Vector2 toScreenCoordinates(Vector2 worldCoords, Matrix4 transformMatrix) {
		tmp.set(worldCoords.x, worldCoords.y, 0);
		tmp.mul(transformMatrix);
		camera.project(tmp, 0, 0, screenWidth, screenHeight);
		tmp.y = GraphFunc.app.getGraphics().getHeight() - tmp.y;
		worldCoords.x = tmp.x;
		worldCoords.y = tmp.y;
		return worldCoords;
	}

	public float getScreenWidth() {
		return screenWidth;
	}

	public float getScreenHeight() {
		return screenHeight;
	}
}
