package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;

public class Manifold {
	long addr;
	final ManifoldPoint[] points = new ManifoldPoint[] {new ManifoldPoint(), new ManifoldPoint()};
	final Vector2 localNormal = new Vector2();
	final Vector2 localPoint = new Vector2();

	final int[] tmpInt = new int[2];
	final float[] tmpFloat = new float[4];
	
	private static native void initialize();
	static {
		initialize();
	}

	protected Manifold (long addr) {
		this.addr = addr;
	}

	public ManifoldType getType () {
		int type = jniGetType();
		if (type == 0) return ManifoldType.Circle;
		if (type == 1) return ManifoldType.FaceA;
		if (type == 2) return ManifoldType.FaceB;
		return ManifoldType.Circle;
	}

	private native int jniGetType ();
	public native int getPointCount ();
	public Vector2 getLocalNormal () {
		jniGetLocalNormal(tmpFloat);
		localNormal.set(tmpFloat[0], tmpFloat[1]);
		return localNormal;
	}

	private native void jniGetLocalNormal (float[] values);
	public Vector2 getLocalPoint () {
		jniGetLocalPoint(tmpFloat);
		localPoint.set(tmpFloat[0], tmpFloat[1]);
		return localPoint;
	}

	private native void jniGetLocalPoint (float[] values); 
	public ManifoldPoint[] getPoints () {
		int count = getPointCount();
		for (int i = 0; i < count; i++) {
			int contactID = jniGetPoint(tmpFloat, i);
			ManifoldPoint point = points[i];
			point.contactID = contactID;
			point.localPoint.set(tmpFloat[0], tmpFloat[1]);
			point.normalImpulse = tmpFloat[2];
			point.tangentImpulse = tmpFloat[3];
		}
		return points;
	}

	private native int jniGetPoint (float[] values, int idx);

	public class ManifoldPoint {
		public final Vector2 localPoint = new Vector2();
		public float normalImpulse;
		public float tangentImpulse;
		public int contactID = 0;

		@Override
		public String toString () {
			return "id: " + contactID + ", " + localPoint + ", " + normalImpulse + ", " + tangentImpulse;
		}
	}

	public enum ManifoldType {
		Circle, FaceA, FaceB
	}
}
