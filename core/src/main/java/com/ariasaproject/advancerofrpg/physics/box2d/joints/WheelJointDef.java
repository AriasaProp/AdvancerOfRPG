package com.ariasaproject.advancerofrpg.physics.box2d.joints;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Body;
import com.ariasaproject.advancerofrpg.physics.box2d.JointDef;

public class WheelJointDef extends JointDef {
    /**
     * The local anchor point relative to body1's origin.
     **/
    public final Vector2 localAnchorA = new Vector2();
    /**
     * The local anchor point relative to body2's origin.
     **/
    public final Vector2 localAnchorB = new Vector2();
    /**
     * The local translation axis in body1.
     **/
    public final Vector2 localAxisA = new Vector2(1, 0);
    /**
     * Enable/disable the joint motor.
     **/
    public boolean enableMotor = false;
    /**
     * The maximum motor torque, usually in N-m.
     */
    public float maxMotorTorque = 0;
    /**
     * The desired motor speed in radians per second.
     */
    public float motorSpeed = 0;
    /**
     * Suspension frequency, zero indicates no suspension
     */
    public float frequencyHz = 2;
    /**
     * Suspension damping ratio, one indicates critical damping
     */
    public float dampingRatio = 0.7f;

    public WheelJointDef() {
        type = JointType.WheelJoint;
    }

    public void initialize(Body bodyA, Body bodyB, Vector2 anchor, Vector2 axis) {
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        localAnchorA.set(bodyA.getLocalPoint(anchor));
        localAnchorB.set(bodyB.getLocalPoint(anchor));
        localAxisA.set(bodyA.getLocalVector(axis));
    }
}
