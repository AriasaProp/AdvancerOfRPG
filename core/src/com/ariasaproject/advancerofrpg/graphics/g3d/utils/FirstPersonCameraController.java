package com.ariasaproject.advancerofrpg.graphics.g3d.utils;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Camera;
import com.ariasaproject.advancerofrpg.input.Input.Keys;
import com.ariasaproject.advancerofrpg.input.InputAdapter;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.IntIntMap;

public class FirstPersonCameraController extends InputAdapter {
	private final Camera camera;
	private final IntIntMap keys = new IntIntMap();
	private final int STRAFE_LEFT = Keys.A;
	private final int STRAFE_RIGHT = Keys.D;
	private final int FORWARD = Keys.W;
	private final int BACKWARD = Keys.S;
	private final int UP = Keys.Q;
	private final int DOWN = Keys.E;
	private final Vector3 tmp = new Vector3();
	private float velocity = 5;
	private float degreesPerPixel = 0.5f;

	public FirstPersonCameraController(Camera camera) {
		this.camera = camera;
	}

	@Override
	public boolean keyDown(int keycode) {
		keys.put(keycode, keycode);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		keys.remove(keycode, 0);
		return true;
	}

	/**
	 * Sets the velocity in units per second for moving forward, backward and
	 * strafing left/right.
	 *
	 * @param velocity the velocity in units per second
	 */
	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}

	/**
	 * Sets how many degrees to rotate per pixel the mouse moved.
	 *
	 * @param degreesPerPixel
	 */
	public void setDegreesPerPixel(float degreesPerPixel) {
		this.degreesPerPixel = degreesPerPixel;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		float deltaX = -GraphFunc.app.getInput().getDeltaX() * degreesPerPixel;
		float deltaY = -GraphFunc.app.getInput().getDeltaY() * degreesPerPixel;
		camera.direction.rotate(camera.up, deltaX);
		tmp.set(camera.direction).crs(camera.up).nor();
		camera.direction.rotate(tmp, deltaY);
		// camera.up.rotate(tmp, deltaY);
		return true;
	}

	public void update() {
		update(GraphFunc.app.getGraphics().getDeltaTime());
	}

	public void update(float deltaTime) {
		if (keys.containsKey(FORWARD)) {
			tmp.set(camera.direction).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(BACKWARD)) {
			tmp.set(camera.direction).nor().scl(-deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(STRAFE_LEFT)) {
			tmp.set(camera.direction).crs(camera.up).nor().scl(-deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(STRAFE_RIGHT)) {
			tmp.set(camera.direction).crs(camera.up).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(UP)) {
			tmp.set(camera.up).nor().scl(deltaTime * velocity);
			camera.position.add(tmp);
		}
		if (keys.containsKey(DOWN)) {
			tmp.set(camera.up).nor().scl(-deltaTime * velocity);
			camera.position.add(tmp);
		}
		camera.update(true);
	}
}
