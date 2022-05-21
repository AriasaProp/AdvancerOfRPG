package com.ariasaproject.advancerofrpg.physics.box2d.joints;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Joint;
import com.ariasaproject.advancerofrpg.physics.box2d.World;

public class RevoluteJoint extends Joint {
    /**
     * Get the first ground anchor.
     */
    private final float[] tmp = new float[2];
    private final Vector2 localAnchorA = new Vector2();
    private final Vector2 localAnchorB = new Vector2();

    public RevoluteJoint(World world, long addr) {
        super(world, addr);
    }

    /**
     * Get the current joint angle in radians.
     */
    public float getJointAngle() {
        return jniGetJointAngle(addr);
    }

    private native float jniGetJointAngle(long addr); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr; return
     * joint->GetJointAngle();
     */

    /**
     * Get the current joint angle speed in radians per second.
     */
    public float getJointSpeed() {
        return jniGetJointSpeed(addr);
    }

    private native float jniGetJointSpeed(long addr); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr; return
     * joint->GetJointSpeed();
     */

    /**
     * Is the joint limit enabled?
     */
    public boolean isLimitEnabled() {
        return jniIsLimitEnabled(addr);
    }

    private native boolean jniIsLimitEnabled(long addr); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr; return
     * joint->IsLimitEnabled();
     */

    /**
     * Enable/disable the joint limit.
     */
    public void enableLimit(boolean flag) {
        jniEnableLimit(addr, flag);
    }

    private native void jniEnableLimit(long addr, boolean flag); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
     * joint->EnableLimit(flag);
     */

    /**
     * Get the lower joint limit in radians.
     */
    public float getLowerLimit() {
        return jniGetLowerLimit(addr);
    }

    private native float jniGetLowerLimit(long addr); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr; return
     * joint->GetLowerLimit();
     */

    /**
     * Get the upper joint limit in radians.
     */
    public float getUpperLimit() {
        return jniGetUpperLimit(addr);
    }

    private native float jniGetUpperLimit(long addr); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr; return
     * joint->GetUpperLimit();
     */

    /**
     * Set the joint limits in radians.
     *
     * @param upper
     */
    public void setLimits(float lower, float upper) {
        jniSetLimits(addr, lower, upper);
    }

    private native void jniSetLimits(long addr, float lower, float upper); /*
     * b2RevoluteJoint* joint =
     * (b2RevoluteJoint*)addr;
     * joint->SetLimits(lower, upper );
     */

    /**
     * Is the joint motor enabled?
     */
    public boolean isMotorEnabled() {
        return jniIsMotorEnabled(addr);
    }

    private native boolean jniIsMotorEnabled(long addr); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr; return
     * joint->IsMotorEnabled();
     */

    /**
     * Enable/disable the joint motor.
     */
    public void enableMotor(boolean flag) {
        jniEnableMotor(addr, flag);
    }

    private native void jniEnableMotor(long addr, boolean flag); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
     * joint->EnableMotor(flag);
     */

    private native void jniSetMotorSpeed(long addr, float speed); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
     * joint->SetMotorSpeed(speed);
     */

    /**
     * Get the motor speed in radians per second.
     */
    public float getMotorSpeed() {
        return jniGetMotorSpeed(addr);
    }

    /**
     * Set the motor speed in radians per second.
     */
    public void setMotorSpeed(float speed) {
        jniSetMotorSpeed(addr, speed);
    }

    private native float jniGetMotorSpeed(long addr); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr; return
     * joint->GetMotorSpeed();
     */

    private native void jniSetMaxMotorTorque(long addr, float torque); /*
     * b2RevoluteJoint* joint =
     * (b2RevoluteJoint*)addr;
     * joint->SetMaxMotorTorque(torque);
     */

    /**
     * Get the current motor torque, usually in N-m.
     */
    public float getMotorTorque(float invDt) {
        return jniGetMotorTorque(addr, invDt);
    }

    private native float jniGetMotorTorque(long addr, float invDt); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr;
     * return joint->GetMotorTorque(invDt);
     */

    public Vector2 getLocalAnchorA() {
        jniGetLocalAnchorA(addr, tmp);
        localAnchorA.set(tmp[0], tmp[1]);
        return localAnchorA;
    }

    private native void jniGetLocalAnchorA(long addr, float[] anchor); /*
     * b2RevoluteJoint* joint =
     * (b2RevoluteJoint*)addr; anchor[0] =
     * joint->GetLocalAnchorA().x; anchor[1] =
     * joint->GetLocalAnchorA().y;
     */

    public Vector2 getLocalAnchorB() {
        jniGetLocalAnchorB(addr, tmp);
        localAnchorB.set(tmp[0], tmp[1]);
        return localAnchorB;
    }

    private native void jniGetLocalAnchorB(long addr, float[] anchor); /*
     * b2RevoluteJoint* joint =
     * (b2RevoluteJoint*)addr; anchor[0] =
     * joint->GetLocalAnchorB().x; anchor[1] =
     * joint->GetLocalAnchorB().y;
     */

    /**
     * Get the current motor torque, usually in N-m.
     */
    public float getReferenceAngle() {
        return jniGetReferenceAngle(addr);
    }

    private native float jniGetReferenceAngle(long addr); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr; return
     * joint->GetReferenceAngle();
     */

    public float getMaxMotorTorque() {
        return jniGetMaxMotorTorque(addr);
    }

    /**
     * Set the maximum motor torque, usually in N-m.
     */
    public void setMaxMotorTorque(float torque) {
        jniSetMaxMotorTorque(addr, torque);
    }

    private native float jniGetMaxMotorTorque(long addr); /*
     * b2RevoluteJoint* joint = (b2RevoluteJoint*)addr; return
     * joint->GetMaxMotorTorque();
     */
}
