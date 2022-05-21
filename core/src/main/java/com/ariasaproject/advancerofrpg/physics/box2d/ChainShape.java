package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;

public class ChainShape extends Shape {
    private static float[] verts = new float[2];
    boolean isLooped = false;

    public ChainShape() {
        addr = newChainShape();
    }

    ChainShape(long addr) {
        this.addr = addr;
    }

    private native long newChainShape();

    @Override
    public Type getType() {
        return Type.Chain;
    }

    public void createLoop(float[] vertices) {
        jniCreateLoop(vertices, 0, vertices.length / 2);
        isLooped = true;
    }

    public void createLoop(float[] vertices, int offset, int length) {
        jniCreateLoop(vertices, offset, length / 2);
        isLooped = true;
    }

    public void createLoop(Vector2[] vertices) {
        float[] verts = new float[vertices.length * 2];
        for (int i = 0, j = 0; i < vertices.length * 2; i += 2, j++) {
            verts[i] = vertices[j].x;
            verts[i + 1] = vertices[j].y;
        }
        jniCreateLoop(verts, 0, verts.length / 2);
        isLooped = true;
    }

    private native void jniCreateLoop(float[] verts, int offset, int numVertices);

    public void createChain(float[] vertices) {
        jniCreateChain(vertices, 0, vertices.length / 2);
        isLooped = false;
    }

    public void createChain(float[] vertices, int offset, int length) {
        jniCreateChain(vertices, offset, length / 2);
        isLooped = false;
    }

    public void createChain(Vector2[] vertices) {
        float[] verts = new float[vertices.length * 2];
        for (int i = 0, j = 0; i < vertices.length * 2; i += 2, j++) {
            verts[i] = vertices[j].x;
            verts[i + 1] = vertices[j].y;
        }
        jniCreateChain(verts, 0, vertices.length);
        isLooped = false;
    }

    private native void jniCreateChain(float[] verts, int offset, int numVertices);

    public void setPrevVertex(Vector2 prevVertex) {
        setPrevVertex(prevVertex.x, prevVertex.y);
    }

    public native void setPrevVertex(float x, float y);

    public void setNextVertex(Vector2 nextVertex) {
        setNextVertex(nextVertex.x, nextVertex.y);
    }

    public native void setNextVertex(float x, float y);

    public native int getVertexCount();

    public void getVertex(int index, Vector2 vertex) {
        jniGetVertex(index, verts);
        vertex.x = verts[0];
        vertex.y = verts[1];
    }

    private native void jniGetVertex(int index, float[] verts);

    public boolean isLooped() {
        return isLooped;
    }

// /// Implement b2Shape. Vertices are cloned using b2Alloc.
// b2Shape* Clone(b2BlockAllocator* allocator) const;
//
// /// @see b2Shape::GetChildCount
// int32_t GetChildCount() const;
//
// /// Get a child edge.
// void GetChildEdge(b2EdgeShape* edge, int32_t index) const;
//
// /// This always return false.
// /// @see b2Shape::TestPoint
// bool TestPoint(const b2Transform& transform, const b2Vec2& p) const;
//
// /// Implement b2Shape.
// bool RayCast(b2RayCastOutput* output, const b2RayCastInput& input,
// const b2Transform& transform, int32_t childIndex) const;
//
// /// @see b2Shape::ComputeAABB
// void ComputeAABB(b2AABB* aabb, const b2Transform& transform, int32_t childIndex) const;
//
// /// Chains have zero mass.
// /// @see b2Shape::ComputeMass
// void ComputeMass(b2MassData* massData, float32 density) const;
//
}
