package com.ariasaproject.advancerofrpg.physics.box2d.joints;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Joint;
import com.ariasaproject.advancerofrpg.physics.box2d.World;

public class MouseJoint extends Joint {

    /**
     * Use this to update the target point.
     */
    final float[] tmp = new float[2];
    private final Vector2 target = new Vector2();

    public MouseJoint(World world, long addr) {
        super(world, addr);
    }

    private native void jniSetTarget(long addr, float x, float y); /*
     * b2MouseJoint* joint = (b2MouseJoint*)addr;
     * joint->SetTarget( b2Vec2(x, y ) );
     */

    public Vector2 getTarget() {
        jniGetTarget(addr, tmp);
        target.x = tmp[0];
        target.y = tmp[1];
        return target;
    }

    /**
     * Use this to update the target point.
     */
    public void setTarget(Vector2 target) {
        jniSetTarget(addr, target.x, target.y);
    }

    private native void jniGetTarget(long addr, float[] target); /*
     * b2MouseJoint* joint = (b2MouseJoint*)addr;
     * target[0] = joint->GetTarget().x; target[1] =
     * joint->GetTarget().y;
     */

    private native void jniSetMaxForce(long addr, float force); /*
     * b2MouseJoint* joint = (b2MouseJoint*)addr;
     * joint->SetMaxForce( force );
     */

    /**
     * Set/get the maximum force in Newtons.
     */
    public float getMaxForce() {
        return jniGetMaxForce(addr);
    }

    /**
     * Set/get the maximum force in Newtons.
     */
    public void setMaxForce(float force) {
        jniSetMaxForce(addr, force);
    }

    private native float jniGetMaxForce(long addr); /*
     * b2MouseJoint* joint = (b2MouseJoint*)addr; return
     * joint->GetMaxForce();
     */

    private native void jniSetFrequency(long addr, float hz); /*
     * b2MouseJoint* joint = (b2MouseJoint*)addr;
     * joint->SetFrequency(hz);
     */

    /**
     * Set/get the frequency in Hertz.
     */
    public float getFrequency() {
        return jniGetFrequency(addr);
    }

    /**
     * Set/get the frequency in Hertz.
     */
    public void setFrequency(float hz) {
        jniSetFrequency(addr, hz);
    }

    private native float jniGetFrequency(long addr); /*
     * b2MouseJoint* joint = (b2MouseJoint*)addr; return
     * joint->GetFrequency();
     */

    private native void jniSetDampingRatio(long addr, float ratio); /*
     * b2MouseJoint* joint = (b2MouseJoint*)addr;
     * joint->SetDampingRatio( ratio );
     */

    /**
     * Set/get the damping ratio (dimensionless).
     */
    public float getDampingRatio() {
        return jniGetDampingRatio(addr);
    }

    /**
     * Set/get the damping ratio (dimensionless).
     */
    public void setDampingRatio(float ratio) {
        jniSetDampingRatio(addr, ratio);
    }

    private native float jniGetDampingRatio(long addr); /*
     * b2MouseJoint* joint = (b2MouseJoint*)addr; return
     * joint->GetDampingRatio();
     */
}
