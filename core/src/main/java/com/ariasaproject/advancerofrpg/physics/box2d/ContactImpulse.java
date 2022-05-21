package com.ariasaproject.advancerofrpg.physics.box2d;

public class ContactImpulse {
    static {
        initialize();
    }

    final World world;
    long addr;
    float[] tmp = new float[2];

    protected ContactImpulse(World world, long addr) {
        this.world = world;
        this.addr = addr;
    }

    private static native void initialize();

    public native float[] getNormalImpulses();

    public native float[] getTangentImpulses();

    public native int getCount();
}
