#ifndef B2_BODY_H
#define B2_BODY_H

#include "../Common/b2Math.h"
#include "../Collision/Shapes/b2Shape.h"
#include <new>

class b2Fixture;

class b2Joint;

class b2Contact;

class b2Controller;

class b2World;

struct b2FixtureDef;
struct b2JointEdge;
struct b2ContactEdge;

enum b2BodyType {
    b2_staticBody = 0,
    b2_kinematicBody,
    b2_dynamicBody

};

struct b2BodyDef {

    b2BodyDef() {
        userData = NULL;
        position.Set(0.0f, 0.0f);
        angle = 0.0f;
        linearVelocity.Set(0.0f, 0.0f);
        angularVelocity = 0.0f;
        linearDamping = 0.0f;
        angularDamping = 0.0f;
        allowSleep = true;
        awake = true;
        fixedRotation = false;
        bullet = false;
        type = b2_staticBody;
        active = true;
        gravityScale = 1.0f;
    }

    b2BodyType type;
    b2Vec2 position;
    float32 angle;
    b2Vec2 linearVelocity;
    float32 angularVelocity;
    float32 linearDamping;
    float32 angularDamping;
    bool allowSleep;
    bool awake;
    bool fixedRotation;
    bool bullet;
    bool active;
    void *userData;
    float32 gravityScale;
};


class b2Body {
public:
    b2Fixture *CreateFixture(const b2FixtureDef *def);

    b2Fixture *CreateFixture(const b2Shape *shape, float32 density);

    void DestroyFixture(b2Fixture *fixture);

    void SetTransform(const b2Vec2 &position, float32 angle);

    const b2Transform &GetTransform() const;

    const b2Vec2 &GetPosition() const;

    float32 GetAngle() const;

    const b2Vec2 &GetWorldCenter() const;

    const b2Vec2 &GetLocalCenter() const;

    void SetLinearVelocity(const b2Vec2 &v);

    const b2Vec2 &GetLinearVelocity() const;

    void SetAngularVelocity(float32 omega);

    float32 GetAngularVelocity() const;

    void ApplyForce(const b2Vec2 &force, const b2Vec2 &point, bool wake);

    void ApplyForceToCenter(const b2Vec2 &force, bool wake);

    void ApplyTorque(float32 torque, bool wake);

    void ApplyLinearImpulse(const b2Vec2 &impulse, const b2Vec2 &point, bool wake);

    void ApplyAngularImpulse(float32 impulse, bool wake);

    float32 GetMass() const;

    float32 GetInertia() const;

    void GetMassData(b2MassData *data) const;


    void SetMassData(const b2MassData *data);


    void ResetMassData();


    b2Vec2 GetWorldPoint(const b2Vec2 &localPoint) const;


    b2Vec2 GetWorldVector(const b2Vec2 &localVector) const;


    b2Vec2 GetLocalPoint(const b2Vec2 &worldPoint) const;


    b2Vec2 GetLocalVector(const b2Vec2 &worldVector) const;


    b2Vec2 GetLinearVelocityFromWorldPoint(const b2Vec2 &worldPoint) const;


    b2Vec2 GetLinearVelocityFromLocalPoint(const b2Vec2 &localPoint) const;


    float32 GetLinearDamping() const;


    void SetLinearDamping(float32 linearDamping);


    float32 GetAngularDamping() const;


    void SetAngularDamping(float32 angularDamping);


    float32 GetGravityScale() const;


    void SetGravityScale(float32 scale);


    void SetType(b2BodyType type);


    b2BodyType GetType() const;


    void SetBullet(bool flag);


    bool IsBullet() const;


    void SetSleepingAllowed(bool flag);


    bool IsSleepingAllowed() const;


    void SetAwake(bool flag);


    bool IsAwake() const;


    void SetActive(bool flag);


    bool IsActive() const;


    void SetFixedRotation(bool flag);


    bool IsFixedRotation() const;


    b2Fixture *GetFixtureList();

    const b2Fixture *GetFixtureList() const;


    b2JointEdge *GetJointList();

    const b2JointEdge *GetJointList() const;


    b2ContactEdge *GetContactList();

    const b2ContactEdge *GetContactList() const;


    b2Body *GetNext();

    const b2Body *GetNext() const;


    void *GetUserData() const;


    void SetUserData(void *data);


    b2World *GetWorld();

    const b2World *GetWorld() const;


    void Dump();

private:

    friend class b2World;

    friend class b2Island;

    friend class b2ContactManager;

    friend class b2ContactSolver;

    friend class b2Contact;

    friend class b2DistanceJoint;

    friend class b2FrictionJoint;

    friend class b2GearJoint;

    friend class b2MotorJoint;

    friend class b2MouseJoint;

    friend class b2PrismaticJoint;

    friend class b2PulleyJoint;

    friend class b2RevoluteJoint;

    friend class b2RopeJoint;

    friend class b2WeldJoint;

    friend class b2WheelJoint;


    enum {
        e_islandFlag = 0x0001,
        e_awakeFlag = 0x0002,
        e_autoSleepFlag = 0x0004,
        e_bulletFlag = 0x0008,
        e_fixedRotationFlag = 0x0010,
        e_activeFlag = 0x0020,
        e_toiFlag = 0x0040
    };

    b2Body(const b2BodyDef *bd, b2World *world);

    ~b2Body();

    void SynchronizeFixtures();

    void SynchronizeTransform();


    bool ShouldCollide(const b2Body *other) const;

    void Advance(float32 t);

    b2BodyType m_type;

    uint16 m_flags;

    int32 m_islandIndex;

    b2Transform m_xf;
    b2Sweep m_sweep;

    b2Vec2 m_linearVelocity;
    float32 m_angularVelocity;

    b2Vec2 m_force;
    float32 m_torque;

    b2World *m_world;
    b2Body *m_prev;
    b2Body *m_next;

    b2Fixture *m_fixtureList;
    int32 m_fixtureCount;

    b2JointEdge *m_jointList;
    b2ContactEdge *m_contactList;

    float32 m_mass, m_invMass;


    float32 m_I, m_invI;

    float32 m_linearDamping;
    float32 m_angularDamping;
    float32 m_gravityScale;

    float32 m_sleepTime;

    void *m_userData;
};

inline b2BodyType b2Body::GetType() const {
    return m_type;
}

inline const b2Transform &b2Body::GetTransform() const {
    return m_xf;
}

inline const b2Vec2 &b2Body::GetPosition() const {
    return m_xf.p;
}

inline float32 b2Body::GetAngle() const {
    return m_sweep.a;
}

inline const b2Vec2 &b2Body::GetWorldCenter() const {
    return m_sweep.c;
}

inline const b2Vec2 &b2Body::GetLocalCenter() const {
    return m_sweep.localCenter;
}

inline void b2Body::SetLinearVelocity(const b2Vec2 &v) {
    if (m_type == b2_staticBody) {
        return;
    }

    if (b2Dot(v, v) > 0.0f) {
        SetAwake(true);
    }

    m_linearVelocity = v;
}

inline const b2Vec2 &b2Body::GetLinearVelocity() const {
    return m_linearVelocity;
}

inline void b2Body::SetAngularVelocity(float32 w) {
    if (m_type == b2_staticBody) {
        return;
    }

    if (w * w > 0.0f) {
        SetAwake(true);
    }

    m_angularVelocity = w;
}

inline float32 b2Body::GetAngularVelocity() const {
    return m_angularVelocity;
}

inline float32 b2Body::GetMass() const {
    return m_mass;
}

inline float32 b2Body::GetInertia() const {
    return m_I + m_mass * b2Dot(m_sweep.localCenter, m_sweep.localCenter);
}

inline void b2Body::GetMassData(b2MassData *data) const {
    data->mass = m_mass;
    data->I = m_I + m_mass * b2Dot(m_sweep.localCenter, m_sweep.localCenter);
    data->center = m_sweep.localCenter;
}

inline b2Vec2 b2Body::GetWorldPoint(const b2Vec2 &localPoint) const {
    return b2Mul(m_xf, localPoint);
}

inline b2Vec2 b2Body::GetWorldVector(const b2Vec2 &localVector) const {
    return b2Mul(m_xf.q, localVector);
}

inline b2Vec2 b2Body::GetLocalPoint(const b2Vec2 &worldPoint) const {
    return b2MulT(m_xf, worldPoint);
}

inline b2Vec2 b2Body::GetLocalVector(const b2Vec2 &worldVector) const {
    return b2MulT(m_xf.q, worldVector);
}

inline b2Vec2 b2Body::GetLinearVelocityFromWorldPoint(const b2Vec2 &worldPoint) const {
    return m_linearVelocity + b2Cross(m_angularVelocity, worldPoint - m_sweep.c);
}

inline b2Vec2 b2Body::GetLinearVelocityFromLocalPoint(const b2Vec2 &localPoint) const {
    return GetLinearVelocityFromWorldPoint(GetWorldPoint(localPoint));
}

inline float32 b2Body::GetLinearDamping() const {
    return m_linearDamping;
}

inline void b2Body::SetLinearDamping(float32 linearDamping) {
    m_linearDamping = linearDamping;
}

inline float32 b2Body::GetAngularDamping() const {
    return m_angularDamping;
}

inline void b2Body::SetAngularDamping(float32 angularDamping) {
    m_angularDamping = angularDamping;
}

inline float32 b2Body::GetGravityScale() const {
    return m_gravityScale;
}

inline void b2Body::SetGravityScale(float32 scale) {
    m_gravityScale = scale;
}

inline void b2Body::SetBullet(bool flag) {
    if (flag) {
        m_flags |= e_bulletFlag;
    } else {
        m_flags &= ~e_bulletFlag;
    }
}

inline bool b2Body::IsBullet() const {
    return (m_flags & e_bulletFlag) == e_bulletFlag;
}

inline void b2Body::SetAwake(bool flag) {
    if (flag) {
        if ((m_flags & e_awakeFlag) == 0) {
            m_flags |= e_awakeFlag;
            m_sleepTime = 0.0f;
        }
    } else {
        m_flags &= ~e_awakeFlag;
        m_sleepTime = 0.0f;
        m_linearVelocity.SetZero();
        m_angularVelocity = 0.0f;
        m_force.SetZero();
        m_torque = 0.0f;
    }
}

inline bool b2Body::IsAwake() const {
    return (m_flags & e_awakeFlag) == e_awakeFlag;
}

inline bool b2Body::IsActive() const {
    return (m_flags & e_activeFlag) == e_activeFlag;
}

inline bool b2Body::IsFixedRotation() const {
    return (m_flags & e_fixedRotationFlag) == e_fixedRotationFlag;
}

inline void b2Body::SetSleepingAllowed(bool flag) {
    if (flag) {
        m_flags |= e_autoSleepFlag;
    } else {
        m_flags &= ~e_autoSleepFlag;
        SetAwake(true);
    }
}

inline bool b2Body::IsSleepingAllowed() const {
    return (m_flags & e_autoSleepFlag) == e_autoSleepFlag;
}

inline b2Fixture *b2Body::GetFixtureList() {
    return m_fixtureList;
}

inline const b2Fixture *b2Body::GetFixtureList() const {
    return m_fixtureList;
}

inline b2JointEdge *b2Body::GetJointList() {
    return m_jointList;
}

inline const b2JointEdge *b2Body::GetJointList() const {
    return m_jointList;
}

inline b2ContactEdge *b2Body::GetContactList() {
    return m_contactList;
}

inline const b2ContactEdge *b2Body::GetContactList() const {
    return m_contactList;
}

inline b2Body *b2Body::GetNext() {
    return m_next;
}

inline const b2Body *b2Body::GetNext() const {
    return m_next;
}

inline void b2Body::SetUserData(void *data) {
    m_userData = data;
}

inline void *b2Body::GetUserData() const {
    return m_userData;
}

inline void b2Body::ApplyForce(const b2Vec2 &force, const b2Vec2 &point, bool wake) {
    if (m_type != b2_dynamicBody) {
        return;
    }

    if (wake && (m_flags & e_awakeFlag) == 0) {
        SetAwake(true);
    }


    if (m_flags & e_awakeFlag) {
        m_force += force;
        m_torque += b2Cross(point - m_sweep.c, force);
    }
}

inline void b2Body::ApplyForceToCenter(const b2Vec2 &force, bool wake) {
    if (m_type != b2_dynamicBody) {
        return;
    }

    if (wake && (m_flags & e_awakeFlag) == 0) {
        SetAwake(true);
    }


    if (m_flags & e_awakeFlag) {
        m_force += force;
    }
}

inline void b2Body::ApplyTorque(float32 torque, bool wake) {
    if (m_type != b2_dynamicBody) {
        return;
    }

    if (wake && (m_flags & e_awakeFlag) == 0) {
        SetAwake(true);
    }


    if (m_flags & e_awakeFlag) {
        m_torque += torque;
    }
}

inline void b2Body::ApplyLinearImpulse(const b2Vec2 &impulse, const b2Vec2 &point, bool wake) {
    if (m_type != b2_dynamicBody) {
        return;
    }

    if (wake && (m_flags & e_awakeFlag) == 0) {
        SetAwake(true);
    }


    if (m_flags & e_awakeFlag) {
        m_linearVelocity += m_invMass * impulse;
        m_angularVelocity += m_invI * b2Cross(point - m_sweep.c, impulse);
    }
}

inline void b2Body::ApplyAngularImpulse(float32 impulse, bool wake) {
    if (m_type != b2_dynamicBody) {
        return;
    }

    if (wake && (m_flags & e_awakeFlag) == 0) {
        SetAwake(true);
    }


    if (m_flags & e_awakeFlag) {
        m_angularVelocity += m_invI * impulse;
    }
}

inline void b2Body::SynchronizeTransform() {
    m_xf.q.Set(m_sweep.a);
    m_xf.p = m_sweep.c - b2Mul(m_xf.q, m_sweep.localCenter);
}

inline void b2Body::Advance(float32 alpha) {

    m_sweep.Advance(alpha);
    m_sweep.c = m_sweep.c0;
    m_sweep.a = m_sweep.a0;
    m_xf.q.Set(m_sweep.a);
    m_xf.p = m_sweep.c - b2Mul(m_xf.q, m_sweep.localCenter);
}

inline b2World *b2Body::GetWorld() {
    return m_world;
}

inline const b2World *b2Body::GetWorld() const {
    return m_world;
}

#endif
