package com.ariasaproject.advancerofrpg.physics.box2d.joints;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Body;
import com.ariasaproject.advancerofrpg.physics.box2d.JointDef;

public class RevoluteJointDef extends JointDef {
	public RevoluteJointDef () {
		type = JointType.RevoluteJoint;
	}

	/** Initialize the bodies, anchors, and reference angle using a world anchor point. */
	public void initialize (Body bodyA, Body bodyB, Vector2 anchor) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		localAnchorA.set(bodyA.getLocalPoint(anchor));
		localAnchorB.set(bodyB.getLocalPoint(anchor));
		referenceAngle = bodyB.getAngle() - bodyA.getAngle();
	}

	/** The local anchor point relative to body1's origin. */
	public final Vector2 localAnchorA = new Vector2();

	/** The local anchor point relative to body2's origin. */
	public final Vector2 localAnchorB = new Vector2();;

	/** The body2 angle minus body1 angle in the reference state (radians). */
	public float referenceAngle = 0;

	/** A flag to enable joint limits. */
	public boolean enableLimit = false;

	/** The lower angle for the joint limit (radians). */
	public float lowerAngle = 0;

	/** The upper angle for the joint limit (radians). */
	public float upperAngle = 0;

	/** A flag to enable the joint motor. */
	public boolean enableMotor = false;

	/** The desired motor speed. Usually in radians per second. */
	public float motorSpeed = 0;

	/** The maximum motor torque used to achieve the desired motor speed. Usually in N-m. */
	public float maxMotorTorque = 0;
}
