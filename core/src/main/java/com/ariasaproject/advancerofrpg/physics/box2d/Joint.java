package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.JointDef.JointType;

public abstract class Joint {
	protected long addr;
	private final World world;
	private final float[] tmp = new float[2];
	private Object userData;
	protected JointEdge jointEdgeA;
	protected JointEdge jointEdgeB;

	private static native void initialize();

	static {
		initialize();
	}

	protected Joint(World world, long addr) {
		this.world = world;
		this.addr = addr;
	}

	public JointType getType() {
		int type = jniGetType();
		if (type > 0 && type < JointType.valueTypes.length)
			return JointType.valueTypes[type];
		else
			return JointType.Unknown;
	}

	private native int jniGetType();

	public Body getBodyA() {
		return world.bodies.get(jniGetBodyA());
	}

	private native long jniGetBodyA();

	public Body getBodyB() {
		return world.bodies.get(jniGetBodyB());
	}

	private native long jniGetBodyB();

	private final Vector2 anchorA = new Vector2();

	public Vector2 getAnchorA() {
		jniGetAnchorA(tmp);
		anchorA.x = tmp[0];
		anchorA.y = tmp[1];
		return anchorA;
	}

	private native void jniGetAnchorA(float[] anchorA);

	private final Vector2 anchorB = new Vector2();

	public Vector2 getAnchorB() {
		jniGetAnchorB(tmp);
		anchorB.x = tmp[0];
		anchorB.y = tmp[1];
		return anchorB;
	}

	private native void jniGetAnchorB(float[] anchorB);

	public native boolean getCollideConnected();

	private final Vector2 reactionForce = new Vector2();

	public Vector2 getReactionForce(float inv_dt) {
		jniGetReactionForce(inv_dt, tmp);
		reactionForce.x = tmp[0];
		reactionForce.y = tmp[1];
		return reactionForce;
	}

	private native void jniGetReactionForce(float inv_dt, float[] reactionForce);

	public native float getReactionTorque(float inv_dt);

// /// Get the next joint the world joint list.
// b2Joint* GetNext();
//
	public Object getUserData() {
		return userData;
	}

	public void setUserData(Object userData) {
		this.userData = userData;
	}

	public native boolean isActive();

}
