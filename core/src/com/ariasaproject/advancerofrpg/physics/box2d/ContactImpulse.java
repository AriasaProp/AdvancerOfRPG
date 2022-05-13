package com.ariasaproject.advancerofrpg.physics.box2d;

public class ContactImpulse {
	final World world;
	long addr;
	float[] tmp = new float[2];

	private static native void initialize();

	static {
		initialize();
	}

	protected ContactImpulse(World world, long addr) {
		this.world = world;
		this.addr = addr;
	}

	public native float[] getNormalImpulses();

	public native float[] getTangentImpulses();

	public native int getCount();
}
