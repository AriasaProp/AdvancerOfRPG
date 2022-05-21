package com.ariasaproject.advancerofrpg.physics.box2d;

public abstract class Shape {
    static {
        initialize();
    }

    protected long addr;

    private static native void initialize();

    ;

    public abstract Type getType();

    public native float getRadius();

    public native void setRadius(float radius);

    public void dispose() {
        jniDispose();
    }

    private native void jniDispose();

    protected native int jniGetType();

    public native int getChildCount();

    public enum Type {
        Circle, Edge, Polygon, Chain,
    }

// /// Test a point for containment in this shape. This only works for convex shapes.
// /// @param xf the shape world transform.
// /// @param p a point in world coordinates.
// virtual bool TestPoint(const b2Transform& xf, const b2Vec2& p) const = 0;
//
// /// Cast a ray against this shape.
// /// @param output the ray-cast results.
// /// @param input the ray-cast input parameters.
// /// @param transform the transform to be applied to the shape.
// virtual bool RayCast(b2RayCastOutput* output, const b2RayCastInput& input, const b2Transform& transform) const = 0;
//
// /// Given a transform, compute the associated axis aligned bounding box for this shape.
// /// @param aabb returns the axis aligned box.
// /// @param xf the world transform of the shape.
// virtual void ComputeAABB(b2AABB* aabb, const b2Transform& xf) const = 0;
//
// /// Compute the mass properties of this shape using its dimensions and density.
// /// The inertia tensor is computed about the local origin.
// /// @param massData returns the mass data for this shape.
// /// @param density the density in kilograms per meter squared.
// virtual void ComputeMass(b2MassData* massData, float32 density) const = 0;
}
