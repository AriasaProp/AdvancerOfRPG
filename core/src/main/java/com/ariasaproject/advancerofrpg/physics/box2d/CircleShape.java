package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;

public class CircleShape extends Shape {
    private final float[] tmp = new float[2];
    private final Vector2 position = new Vector2();

    public CircleShape() {
        addr = newCircleShape();
    }

    protected CircleShape(long addr) {
        this.addr = addr;
    }

    private native long newCircleShape();

    @Override
    public Type getType() {
        return Type.Circle;
    }

    public Vector2 getPosition() {
        getPosition(tmp);
        position.x = tmp[0];
        position.y = tmp[1];
        return position;
    }

    public void setPosition(Vector2 position) {
        setPosition(position.x, position.y);
    }

    private native void getPosition(float[] position);

    public native void setPosition(float x, float y);
}
