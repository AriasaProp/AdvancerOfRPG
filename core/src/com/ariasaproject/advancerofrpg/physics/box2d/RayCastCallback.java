package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;

public interface RayCastCallback {
	public float reportRayFixture (Fixture fixture, Vector2 point, Vector2 normal, float fraction);
}
