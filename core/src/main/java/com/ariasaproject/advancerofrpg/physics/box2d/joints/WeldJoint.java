package com.ariasaproject.advancerofrpg.physics.box2d.joints;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Joint;
import com.ariasaproject.advancerofrpg.physics.box2d.World;

public class WeldJoint extends Joint {
    private final float[] tmp = new float[2];
    private final Vector2 localAnchorA = new Vector2();
    private final Vector2 localAnchorB = new Vector2();

    public WeldJoint(World world, long addr) {
        super(world, addr);
    }

    public Vector2 getLocalAnchorA() {
        jniGetLocalAnchorA(addr, tmp);
        localAnchorA.set(tmp[0], tmp[1]);
        return localAnchorA;
    }

    private native void jniGetLocalAnchorA(long addr, float[] anchor); /*
     * b2WeldJoint* joint = (b2WeldJoint*)addr;
     * anchor[0] = joint->GetLocalAnchorA().x;
     * anchor[1] = joint->GetLocalAnchorA().y;
     */

    public Vector2 getLocalAnchorB() {
        jniGetLocalAnchorB(addr, tmp);
        localAnchorB.set(tmp[0], tmp[1]);
        return localAnchorB;
    }

    private native void jniGetLocalAnchorB(long addr, float[] anchor); /*
     * b2WeldJoint* joint = (b2WeldJoint*)addr;
     * anchor[0] = joint->GetLocalAnchorB().x;
     * anchor[1] = joint->GetLocalAnchorB().y;
     */

    public float getReferenceAngle() {
        return jniGetReferenceAngle(addr);
    }

    private native float jniGetReferenceAngle(long addr); /*
     * b2WeldJoint* joint = (b2WeldJoint*)addr; return
     * joint->GetReferenceAngle();
     */

    public float getFrequency() {
        return jniGetFrequency(addr);
    }

    public void setFrequency(float hz) {
        jniSetFrequency(addr, hz);
    }

    private native float jniGetFrequency(long addr); /*
     * b2WeldJoint* joint = (b2WeldJoint*)addr; return
     * joint->GetFrequency();
     */

    private native void jniSetFrequency(long addr, float hz); /*
     * b2WeldJoint* joint = (b2WeldJoint*)addr;
     * joint->SetFrequency(hz);
     */

    public float getDampingRatio() {
        return jniGetDampingRatio(addr);
    }

    public void setDampingRatio(float ratio) {
        jniSetDampingRatio(addr, ratio);
    }

    private native float jniGetDampingRatio(long addr); /*
     * b2WeldJoint* joint = (b2WeldJoint*)addr; return
     * joint->GetDampingRatio();
     */

    private native void jniSetDampingRatio(long addr, float ratio); /*
     * b2WeldJoint* joint = (b2WeldJoint*)addr;
     * joint->SetDampingRatio(ratio);
     */

}
