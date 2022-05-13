package com.ariasaproject.advancerofrpg.physics.box2d.joints;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Joint;
import com.ariasaproject.advancerofrpg.physics.box2d.World;

public class RopeJoint extends Joint {
	private final float[] tmp = new float[2];
	private final Vector2 localAnchorA = new Vector2();
	private final Vector2 localAnchorB = new Vector2();

	public RopeJoint(World world, long addr) {
		super(world, addr);
	}

	public Vector2 getLocalAnchorA() {
		jniGetLocalAnchorA(addr, tmp);
		localAnchorA.set(tmp[0], tmp[1]);
		return localAnchorA;
	}

	private native void jniGetLocalAnchorA(long addr, float[] anchor); /*
																		 * b2RopeJoint* joint = (b2RopeJoint*)addr;
																		 * anchor[0] = joint->GetLocalAnchorA().x;
																		 * anchor[1] = joint->GetLocalAnchorA().y;
																		 */

	public Vector2 getLocalAnchorB() {
		jniGetLocalAnchorB(addr, tmp);
		localAnchorB.set(tmp[0], tmp[1]);
		return localAnchorB;
	}

	private native void jniGetLocalAnchorB(long addr, float[] anchor); /*
																		 * b2RopeJoint* joint = (b2RopeJoint*)addr;
																		 * anchor[0] = joint->GetLocalAnchorB().x;
																		 * anchor[1] = joint->GetLocalAnchorB().y;
																		 */

	/** Get the maximum length of the rope. */
	public float getMaxLength() {
		return jniGetMaxLength(addr);
	}

	private native float jniGetMaxLength(long addr); /*
														 * b2RopeJoint* rope = (b2RopeJoint*)addr; return
														 * rope->GetMaxLength();
														 */

	/** Set the maximum length of the rope. */
	public void setMaxLength(float length) {
		jniSetMaxLength(addr, length);
	}

	private native void jniSetMaxLength(long addr, float length); /*
																	 * b2RopeJoint* rope = (b2RopeJoint*)addr;
																	 * rope->SetMaxLength(length);
																	 */
}
