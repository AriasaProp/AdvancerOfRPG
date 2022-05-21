package com.ariasaproject.advancerofrpg.math;

import com.ariasaproject.advancerofrpg.utils.Pool;

public class Transform implements Pool.Poolable {
    public final Vector3 translation = new Vector3();
    public final Quaternion rotation = new Quaternion();
    public final Vector3 scale = new Vector3(1, 1, 1);
    private final Matrix4 outMatrix = new Matrix4();

    public Transform() {
    }

    public Transform idt() {
        translation.set(0, 0, 0);
        rotation.idt();
        scale.set(1, 1, 1);
        return this;
    }

    public Transform set(final Vector3 t, final Quaternion r, final Vector3 s) {
        translation.set(t);
        rotation.set(r);
        scale.set(s);
        return this;
    }

    public Transform set(final Transform other) {
        return set(other.translation, other.rotation, other.scale);
    }

    public Transform lerp(final Transform target, final float alpha) {
        return lerp(target.translation, target.rotation, target.scale, alpha);
    }

    public Transform lerp(final Vector3 targetT, final Quaternion targetR, final Vector3 targetS, final float alpha) {
        translation.lerp(targetT, alpha);
        rotation.slerp(targetR, alpha);
        scale.lerp(targetS, alpha);
        return this;
    }

    public Matrix4 getMatrix() {
        return outMatrix.set(translation, rotation, scale);
    }

    @Override
    public void reset() {
        idt();
    }

    @Override
    public String toString() {
        return translation.toString() + " - " + rotation.toString() + " - " + scale.toString();
    }
}
