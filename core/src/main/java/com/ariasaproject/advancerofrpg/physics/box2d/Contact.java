package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;

public class Contact {
    static {
        initialize();
    }

    protected final WorldManifold worldManifold = new WorldManifold();
    private final float[] tmp = new float[8];
    protected long addr;
    protected World world;

    protected Contact(World world, long addr) {
        this.addr = addr;
        this.world = world;
    }

    private static native void initialize();

    public WorldManifold getWorldManifold() {
        int numContactPoints = jniGetWorldManifold(tmp);

        worldManifold.numContactPoints = numContactPoints;
        worldManifold.normal.set(tmp[0], tmp[1]);
        for (int i = 0; i < numContactPoints; i++) {
            Vector2 point = worldManifold.points[i];
            point.x = tmp[2 + i * 2];
            point.y = tmp[2 + i * 2 + 1];
        }
        worldManifold.separations[0] = tmp[6];
        worldManifold.separations[1] = tmp[7];

        return worldManifold;
    }

    private native int jniGetWorldManifold(float[] tmp);

    public native boolean isEnabled();

    public native void setEnabled(boolean flag);

    public Fixture getFixtureA() {
        return world.fixtures.get(jniGetFixtureA());
    }

    private native long jniGetFixtureA();

    public Fixture getFixtureB() {
        return world.fixtures.get(jniGetFixtureB());
    }

    private native long jniGetFixtureB();

    public native int getChildIndexA();

    public native int getChildIndexB();

    public native float getFriction();

    public native void setFriction(float friction);

    public native void resetFriction();

    public native float getRestitution();

    public native void setRestitution(float restitution);

    public native void ResetRestitution();

    public native float getTangentSpeed();

    public native void setTangentSpeed(float speed);
}
