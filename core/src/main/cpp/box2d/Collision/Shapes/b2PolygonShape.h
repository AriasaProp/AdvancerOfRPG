#ifndef B2_POLYGON_SHAPE_H
#define B2_POLYGON_SHAPE_H

#include "b2Shape.h"

/// A convex polygon. It is assumed that the interior of the polygon is to
/// the left of each edge.
/// Polygons have a maximum number of vertices equal to b2_maxPolygonVertices.
/// In most cases you should not need many vertices for a convex polygon.
class b2PolygonShape : public b2Shape {
public:
    b2PolygonShape();

    /// Implement b2Shape.
    b2Shape *Clone(b2BlockAllocator *allocator) const;

    /// @see b2Shape::GetChildCount
    int32 GetChildCount() const;

    /// Create a convex hull from the given array of local points.
    /// The count must be in the range [3, b2_maxPolygonVertices].
    /// @warning the points may be re-ordered, even if they form a convex polygon
    /// @warning collinear points are handled but not removed. Collinear points
    /// may lead to poor stacking behavior.
    void Set(const b2Vec2 *points, int32 count);

    /// Build vertices to represent an axis-aligned box centered on the local origin.
    /// @param hx the half-width.
    /// @param hy the half-height.
    void SetAsBox(float32 hx, float32 hy);

    /// Build vertices to represent an oriented box.
    /// @param hx the half-width.
    /// @param hy the half-height.
    /// @param center the center of the box in local coordinates.
    /// @param angle the rotation of the box in local coordinates.
    void SetAsBox(float32 hx, float32 hy, const b2Vec2 &center, float32 angle);

    /// @see b2Shape::TestPoint
    bool TestPoint(const b2Transform &transform, const b2Vec2 &p) const;

    /// Implement b2Shape.
    bool RayCast(b2RayCastOutput *output, const b2RayCastInput &input,
                 const b2Transform &transform, int32 childIndex) const;

    /// @see b2Shape::ComputeAABB
    void ComputeAABB(b2AABB *aabb, const b2Transform &transform, int32 childIndex) const;

    /// @see b2Shape::ComputeMass
    void ComputeMass(b2MassData *massData, float32 density) const;

    /// Get the vertex count.
    int32 GetVertexCount() const {
        return m_count;
    }

    /// Get a vertex by index.
    const b2Vec2 &GetVertex(int32 index) const;

    /// Validate convexity. This is a very time consuming operation.
    /// @returns true if valid
    bool Validate() const;

    b2Vec2 m_centroid;
    b2Vec2 m_vertices[b2_maxPolygonVertices];
    b2Vec2 m_normals[b2_maxPolygonVertices];
    int32 m_count;
};

inline b2PolygonShape::b2PolygonShape() {
    m_type = e_polygon;
    m_radius = b2_polygonRadius;
    m_count = 0;
    m_centroid.SetZero();
}

inline const b2Vec2 &b2PolygonShape::GetVertex(int32 index) const {
    b2Assert(0 <= index && index < m_count);
    return m_vertices[index];
}

#endif
