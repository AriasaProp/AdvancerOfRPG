package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;

public class EdgeShape extends Shape {
    static final float[] vertex = new float[2];

    public EdgeShape() {
        addr = newEdgeShape();
    }

    EdgeShape(long addr) {
        this.addr = addr;
    }

    private native long newEdgeShape();

    public void set(Vector2 v1, Vector2 v2) {
        set(v1.x, v1.y, v2.x, v2.y);
    }

    public native void set(float v1X, float v1Y, float v2X, float v2Y);

    public void getVertex1(Vector2 vec) {
        jniGetVertex1(vertex);
        vec.x = vertex[0];
        vec.y = vertex[1];
    }

    private native void jniGetVertex1(float[] vertex);

    public void getVertex2(Vector2 vec) {
        jniGetVertex2(vertex);
        vec.x = vertex[0];
        vec.y = vertex[1];
    }

    private native void jniGetVertex2(float[] vertex);

    public void getVertex0(Vector2 vec) {
        jniGetVertex0(vertex);
        vec.x = vertex[0];
        vec.y = vertex[1];
    }

    private native void jniGetVertex0(float[] vertex);

    public void setVertex0(Vector2 vec) {
        setVertex0(vec.x, vec.y);
    }

    public native void setVertex0(float x, float y);

    public void getVertex3(Vector2 vec) {
        jniGetVertex3(vertex);
        vec.x = vertex[0];
        vec.y = vertex[1];
    }

    private native void jniGetVertex3(float[] vertex);

    public void setVertex3(Vector2 vec) {
        setVertex3(vec.x, vec.y);
    }

    public native void setVertex3(float x, float y);

    public native boolean hasVertex0();

    public native void setHasVertex0(boolean hasVertex0);

    public native boolean hasVertex3();

    public native void setHasVertex3(boolean hasVertex3);

    @Override
    public Type getType() {
        return Type.Edge;
    }

}
