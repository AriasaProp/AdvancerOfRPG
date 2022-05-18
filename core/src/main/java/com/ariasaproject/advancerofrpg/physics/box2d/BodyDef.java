package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;

public class BodyDef {
	public enum BodyType {
		StaticBody(0), KinematicBody(1), DynamicBody(2);

		private int value;

		private BodyType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	};

	/**
	 * The body type: static, kinematic, or dynamic. Note: if a dynamic body would
	 * have zero mass, the mass is set to one.
	 **/
	public BodyType type = BodyType.StaticBody;

	/**
	 * The world position of the body. Avoid creating bodies at the origin since
	 * this can lead to many overlapping shapes.
	 **/
	public final Vector2 position = new Vector2();

	/** The world angle of the body in radians. **/
	public float angle = 0;

	/** The linear velocity of the body's origin in world co-ordinates. **/
	public final Vector2 linearVelocity = new Vector2();

	/** The angular velocity of the body. **/
	public float angularVelocity = 0;

	/**
	 * Linear damping is use to reduce the linear velocity. The damping parameter
	 * can be larger than 1.0f but the damping effect becomes sensitive to the time
	 * step when the damping parameter is large.
	 **/
	public float linearDamping = 0;

	/**
	 * Angular damping is use to reduce the angular velocity. The damping parameter
	 * can be larger than 1.0f but the damping effect becomes sensitive to the time
	 * step when the damping parameter is large.
	 **/
	public float angularDamping = 0;

	/**
	 * Set this flag to false if this body should never fall asleep. Note that this
	 * increases CPU usage.
	 **/
	public boolean allowSleep = true;

	/** Is this body initially awake or sleeping? **/
	public boolean awake = true;

	/** Should this body be prevented from rotating? Useful for characters. **/
	public boolean fixedRotation = false;

	/**
	 * Is this a fast moving body that should be prevented from tunneling through
	 * other moving bodies? Note that all bodies are prevented from tunneling
	 * through kinematic and static bodies. This setting is only considered on
	 * dynamic bodies.
	 * 
	 * @warning You should use this flag sparingly since it increases processing
	 *          time.
	 **/
	public boolean bullet = false;

	/** Does this body start out active? **/
	public boolean active = true;

	/** Scale the gravity applied to this body. **/
	public float gravityScale = 1;
}
