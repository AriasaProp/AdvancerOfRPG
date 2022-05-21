package com.ariasaproject.advancerofrpg.physics.box2d.joints;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Joint;
import com.ariasaproject.advancerofrpg.physics.box2d.World;

/**
 * The pulley joint is connected to two bodies and two fixed ground points. The
 * pulley supports a ratio such that: length1 + ratio * length2 <= constant Yes,
 * the force transmitted is scaled by the ratio. The pulley also enforces a
 * maximum length limit on both sides. This is useful to prevent one side of the
 * pulley hitting the top.
 */
public class PulleyJoint extends Joint {
    // @off
    /*
     * JNI #include <Box2D/Box2D.h>
     */

    /**
     * Get the first ground anchor.
     */
    private final float[] tmp = new float[2];
    private final Vector2 groundAnchorA = new Vector2();
    /**
     * Get the second ground anchor.
     */
    private final Vector2 groundAnchorB = new Vector2();

    public PulleyJoint(World world, long addr) {
        super(world, addr);
    }

    public Vector2 getGroundAnchorA() {
        jniGetGroundAnchorA(addr, tmp);
        groundAnchorA.set(tmp[0], tmp[1]);
        return groundAnchorA;
    }

    private native void jniGetGroundAnchorA(long addr, float[] anchor); /*
     * b2PulleyJoint* joint = (b2PulleyJoint*)addr;
     * anchor[0] = joint->GetGroundAnchorA().x;
     * anchor[1] = joint->GetGroundAnchorA().y;
     */

    public Vector2 getGroundAnchorB() {
        jniGetGroundAnchorB(addr, tmp);
        groundAnchorB.set(tmp[0], tmp[1]);
        return groundAnchorB;
    }

    private native void jniGetGroundAnchorB(long addr, float[] anchor); /*
     * b2PulleyJoint* joint = (b2PulleyJoint*)addr;
     * anchor[0] = joint->GetGroundAnchorB().x;
     * anchor[1] = joint->GetGroundAnchorB().y;
     */

    /**
     * Get the current length of the segment attached to body1.
     */
    public float getLength1() {
        return jniGetLength1(addr);
    }

    private native float jniGetLength1(long addr); /*
     * b2PulleyJoint* joint = (b2PulleyJoint*)addr; return
     * joint->GetLengthA();
     */

    /**
     * Get the current length of the segment attached to body2.
     */
    public float getLength2() {
        return jniGetLength2(addr);
    }

    private native float jniGetLength2(long addr); /*
     * b2PulleyJoint* joint = (b2PulleyJoint*)addr; return
     * joint->GetLengthB();
     */

    /**
     * Get the pulley ratio.
     */
    public float getRatio() {
        return jniGetRatio(addr);
    }

    private native float jniGetRatio(long addr); /*
     * b2PulleyJoint* joint = (b2PulleyJoint*)addr; return
     * joint->GetRatio();
     */
}
