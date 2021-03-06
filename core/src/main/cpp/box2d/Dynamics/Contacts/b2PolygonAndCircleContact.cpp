#include "b2PolygonAndCircleContact.h"
#include "../../Common/b2BlockAllocator.h"
#include "../b2Fixture.h"

#include <new>

using namespace std;

b2Contact *b2PolygonAndCircleContact::Create(b2Fixture *fixtureA, int32, b2Fixture *fixtureB, int32,
                                             b2BlockAllocator *allocator) {
    void *mem = allocator->Allocate(sizeof(b2PolygonAndCircleContact));
    return new(mem) b2PolygonAndCircleContact(fixtureA, fixtureB);
}

void b2PolygonAndCircleContact::Destroy(b2Contact *contact, b2BlockAllocator *allocator) {
    ((b2PolygonAndCircleContact *) contact)->~b2PolygonAndCircleContact();
    allocator->Free(contact, sizeof(b2PolygonAndCircleContact));
}

b2PolygonAndCircleContact::b2PolygonAndCircleContact(b2Fixture *fixtureA, b2Fixture *fixtureB)
        : b2Contact(fixtureA, 0, fixtureB, 0) {
    b2Assert(m_fixtureA->GetType() == b2Shape::e_polygon);
    b2Assert(m_fixtureB->GetType() == b2Shape::e_circle);
}

void b2PolygonAndCircleContact::Evaluate(b2Manifold *manifold, const b2Transform &xfA,
                                         const b2Transform &xfB) {
    b2CollidePolygonAndCircle(manifold,
                              (b2PolygonShape *) m_fixtureA->GetShape(), xfA,
                              (b2CircleShape *) m_fixtureB->GetShape(), xfB);
}
