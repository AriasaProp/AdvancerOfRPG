package com.ariasaproject.advancerofrpg.physics.box2d.joints;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Body;
import com.ariasaproject.advancerofrpg.physics.box2d.JointDef;

public class PrismaticJointDef extends JointDef {
	public PrismaticJointDef () {
		type = JointType.PrismaticJoint;
	}

	/** Initialize the bodies, anchors, axis, and reference angle using the world anchor and world axis. */
	public void initialize (Body bodyA, Body bodyB, Vector2 anchor, Vector2 axis) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		localAnchorA.set(bodyA.getLocalPoint(anchor));
		localAnchorB.set(bodyB.getLocalPoint(anchor));
		localAxisA.set(bodyA.getLocalVector(axis));
		referenceAngle = bodyB.getAngle() - bodyA.getAngle();

	}

	/** The local anchor point relative to body1's origin. */
	public final Vector2 localAnchorA = new Vector2();

	/** The local anchor point relative to body2's origin. */
	public final Vector2 localAnchorB = new Vector2();

	/** The local translation axis in body1. */
	public final Vector2 localAxisA = new Vector2(1, 0);

	/** The constrained angle between the bodies: body2_angle - body1_angle. */
	public float referenceAngle = 0;

	/** Enable/disable the joint limit. */
	public boolean enableLimit = false;

	/** The lower translation limit, usually in meters. */
	public float lowerTranslation = 0;

	/** The upper translation limit, usually in meters. */
	public float upperTranslation = 0;

	/** Enable/disable the joint motor. */
	public boolean enableMotor = false;

	/** The maximum motor torque, usually in N-m. */
	public float maxMotorForce = 0;

	/** The desired motor speed in radians per second. */
	public float motorSpeed = 0;
}
