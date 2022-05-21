#include "World.h"
#include "Box2D.h"

//java value
static jfieldID worldPtr;
//default value
static b2ContactFilter defaultFilter;

World_M(void, initialize) (JNIEnv
*env,
jclass clazz
)
{
//initialize field ID of JNI
worldPtr = env->GetFieldID(clazz, "worldPtr", "J");
}

class CustomRayCastCallback : public b2RayCastCallback {
private:
    JNIEnv *env;
    jobject obj;
    jmethodID reportRayFixtureID;

public:
    CustomRayCastCallback(JNIEnv *env, jobject obj) {
        this->env = env;
        this->obj = obj;
    }

    virtual float32
    ReportFixture(b2Fixture *fixture, const b2Vec2 &point, const b2Vec2 &normal, float32 fraction) {
        if (reportRayFixtureID == NULL) {
            jclass clazz = env->GetObjectClass(obj);
            reportRayFixtureID = env->GetMethodID(clazz, "reportRayFixture", "(FFFFF)F");
        }
        return env->CallFloatMethod(obj, reportRayFixtureID, (jlong)
        fixture, (jfloat)
        point.x, (jfloat)
        point.y, (jfloat)
        normal.x, (jfloat)
        normal.y, (jfloat)
        fraction);
    }
};

class CustomContactFilter : public b2ContactFilter {
private:
    JNIEnv *env;
    jobject obj;
    jmethodID shouldCollideID;

public:
    CustomContactFilter(JNIEnv *env, jobject obj) {
        this->env = env;
        this->obj = obj;
    }

    virtual bool ShouldCollide(b2Fixture *fixtureA, b2Fixture *fixtureB) {
        if (shouldCollideID == NULL) {
            jclass clazz = env->GetObjectClass(obj);
            shouldCollideID = env->GetMethodID(clazz, "contactFilter", "(JJ)B");
        }
        return env->CallBooleanMethod(obj, shouldCollideID, (jlong)
        fixtureA, (jlong)
        fixtureB );
    }
};

class CustomContactListener : public b2ContactListener {
private:
    JNIEnv *env;
    jobject obj;
    jmethodID beginContactID, endContactID, preSolveID, postSolveID;

public:
    CustomContactListener(JNIEnv *env, jobject obj) {
        this->env = env;
        this->obj = obj;
    }

    /// Called when two fixtures begin to touch.
    virtual void BeginContact(b2Contact *contact) {
        if (beginContactID == NULL) {
            jclass clazz = env->GetObjectClass(obj);
            beginContactID = env->GetMethodID(clazz, "beginContact", "(J)V");
        }
        env->CallVoidMethod(obj, beginContactID, (jlong)
        contact );
    }

    /// Called when two fixtures cease to touch.
    virtual void EndContact(b2Contact *contact) {
        if (endContactID == NULL) {
            jclass clazz = env->GetObjectClass(obj);
            endContactID = env->GetMethodID(clazz, "endContact", "(J)V");
        }
        env->CallVoidMethod(obj, endContactID, (jlong)
        contact);
    }

    /// This is called after a contact is updated.
    virtual void PreSolve(b2Contact *contact, const b2Manifold *oldManifold) {
        if (preSolveID == NULL) {
            jclass clazz = env->GetObjectClass(obj);
            preSolveID = env->GetMethodID(clazz, "preSolve", "(JJ)V");
        }
        env->CallVoidMethod(obj, preSolveID, (jlong)
        contact, (jlong)
        oldManifold);
    }

    /// This lets you inspect a contact after the solver is finished.
    virtual void PostSolve(b2Contact *contact, const b2ContactImpulse *impulse) {
        if (postSolveID == NULL) {
            jclass clazz = env->GetObjectClass(obj);
            postSolveID = env->GetMethodID(clazz, "postSolve", "(JJ)V");
        }
        env->CallVoidMethod(obj, postSolveID, (jlong)
        contact, (jlong)
        impulse);
    }
};

class CustomQueryCallback : public b2QueryCallback {
private:
    JNIEnv *env;
    jobject obj;
    jmethodID reportFixtureID;

public:
    CustomQueryCallback(JNIEnv *env, jobject obj) {
        this->env = env;
        this->obj = obj;
    }

    virtual bool ReportFixture(b2Fixture *fixture) {
        if (reportFixtureID == NULL) {
            jclass clazz = env->GetObjectClass(obj);
            reportFixtureID = env->GetMethodID(clazz, "reportFixture", "(J)B");
        }
        return env->CallBooleanMethod(obj, reportFixtureID, (jlong)
        fixture );
    }
};

inline b2BodyType getBodyType(int type) {
    switch (type) {
        case 0:
            return b2_staticBody;
        case 1:
            return b2_kinematicBody;
        case 2:
            return b2_dynamicBody;
        default:
            return b2_staticBody;
    }
}

World_M(jlong, newWorld)(JNIEnv * env, jclass
clazz,
jfloat gx, jfloat
gy,
jboolean doSleep
)
{
b2World *world = new b2World(b2Vec2(gx, gy));
world->
SetAllowSleeping( doSleep );
return (jlong)
world;
}

World_M(jlong, jniCreateBody)(JNIEnv * env, jobject
object,
jint type, jfloat
positionX,
jfloat positionY, jfloat
angle,
jfloat linearVelocityX,
        jfloat
linearVelocityY,
jfloat angularVelocity, jfloat
linearDamping,
jfloat angularDamping, jboolean
allowSleep,
jboolean awake,
        jboolean
fixedRotation,
jboolean bullet, jboolean
active,
jfloat inertiaScale
)
{
b2BodyDef bodyDef;
bodyDef.
type = getBodyType(type);
bodyDef.position.
Set( positionX, positionY
);
bodyDef.
angle = angle;
bodyDef.linearVelocity.
Set( linearVelocityX, linearVelocityY
);
bodyDef.
angularVelocity = angularVelocity;
bodyDef.
linearDamping = linearDamping;
bodyDef.
angularDamping = angularDamping;
bodyDef.
allowSleep = allowSleep;
bodyDef.
awake = awake;
bodyDef.
fixedRotation = fixedRotation;
bodyDef.
bullet = bullet;
bodyDef.
active = active;
bodyDef.
gravityScale = inertiaScale;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateBody( &bodyDef );
}

World_M(void, jniDestroyBody)(JNIEnv
*env,
jobject object, jlong
bodyworldPtr)
{
b2World *world = (b2World *) env->GetLongField(object, worldPtr);
b2Body *body = (b2Body *) bodyworldPtr;
CustomContactFilter contactFilter(env, object);
CustomContactListener contactListener(env, object);
world->
SetContactFilter(&contactFilter);
world->
SetContactListener(&contactListener);
world->
DestroyBody(body);
world->
SetContactFilter(&defaultFilter);
world->SetContactListener(0);
}
World_M(void, jniDestroyFixture) (JNIEnv
*env,
jobject object, jlong
bodyPtr,
jlong fixturePtr
)
{
b2World *world = (b2World *) env->GetLongField(object, worldPtr);
CustomContactFilter contactFilter(env, object);
CustomContactListener contactListener(env, object);
world->
SetContactFilter(&contactFilter);
world->
SetContactListener(&contactListener);
((b2Body*)bodyPtr)->
DestroyFixture((b2Fixture
*)fixturePtr);
world->
SetContactFilter(&defaultFilter);
world->SetContactListener(0);
}

World_M(void, jniDeactivateBody) (JNIEnv
*env,
jobject object, jlong
bodyworldPtr)
{
b2World *world = (b2World *) env->GetLongField(object, worldPtr);
CustomContactFilter contactFilter(env, object);
CustomContactListener contactListener(env, object);
world->
SetContactFilter(&contactFilter);
world->
SetContactListener(&contactListener);
((b2Body*)bodyworldPtr)->SetActive(false);
world->
SetContactFilter(&defaultFilter);
world->SetContactListener(0);
}

World_M(jlong, jniCreateWheelJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
localAnchorAX,
jfloat localAnchorAY, jfloat
localAnchorBX,
jfloat localAnchorBY, jfloat
localAxisAX,
jfloat localAxisAY, jboolean
enableMotor,
jfloat maxMotorTorque, jfloat
motorSpeed,
jfloat frequencyHz, jfloat
dampingRatio)
{
b2WheelJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
def.
localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
def.
localAxisA = b2Vec2(localAxisAX, localAxisAY);
def.
enableMotor = enableMotor;
def.
maxMotorTorque = maxMotorTorque;
def.
motorSpeed = motorSpeed;
def.
frequencyHz = frequencyHz;
def.
dampingRatio = dampingRatio;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}

World_M(jlong, jniCreateRopeJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
localAnchorAX,
jfloat localAnchorAY, jfloat
localAnchorBX,
jfloat localAnchorBY, jfloat
maxLength)
{
b2RopeJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
def.
localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
def.
maxLength = maxLength;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}

World_M(jlong, jniCreateDistanceJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
localAnchorAX,
jfloat localAnchorAY, jfloat
localAnchorBX,
jfloat localAnchorBY, jfloat
length,
jfloat frequencyHz, jfloat
dampingRatio)
{
b2DistanceJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
def.
localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
def.
length = length;
def.
frequencyHz = frequencyHz;
def.
dampingRatio = dampingRatio;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}

World_M(jlong, jniCreateFrictionJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
localAnchorAX,
jfloat localAnchorAY, jfloat
localAnchorBX,
jfloat localAnchorBY, jfloat
maxForce,
jfloat maxTorque
)
{
b2FrictionJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
def.
localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
def.
maxForce = maxForce;
def.
maxTorque = maxTorque;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}

World_M(jlong, jniCreateGearJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jlong
joint1,
jlong joint2, jfloat
ratio)
{
b2GearJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
joint1 = (b2Joint *) joint1;
def.
joint2 = (b2Joint *) joint2;
def.
ratio = ratio;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}

World_M(jlong, jniCreateMotorJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
linearOffsetX,
jfloat linearOffsetY, jfloat
angularOffset,
jfloat maxForce, jfloat
maxTorque,
jfloat correctionFactor
)
{
b2MotorJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
linearOffset = b2Vec2(linearOffsetX, linearOffsetY);
def.
angularOffset = angularOffset;
def.
maxForce = maxForce;
def.
maxTorque = maxTorque;
def.
correctionFactor = correctionFactor;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}

World_M(jlong, jniCreateMouseJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
targetX,
jfloat targetY, jfloat
maxForce,
jfloat frequencyHz, jfloat
dampingRatio)
{
b2MouseJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
target = b2Vec2(targetX, targetY);
def.
maxForce = maxForce;
def.
frequencyHz = frequencyHz;
def.
dampingRatio = dampingRatio;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);

}

World_M(jlong, jniCreatePrismaticJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
localAnchorAX,
jfloat localAnchorAY, jfloat
localAnchorBX,
jfloat localAnchorBY, jfloat
localAxisAX,
jfloat localAxisAY, jfloat
referenceAngle,
jboolean enableLimit, jfloat
lowerTranslation,
jfloat upperTranslation, jboolean
enableMotor,
jfloat maxMotorForce, jfloat
motorSpeed)
{
b2PrismaticJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
def.
localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
def.
localAxisA = b2Vec2(localAxisAX, localAxisAY);
def.
referenceAngle = referenceAngle;
def.
enableLimit = enableLimit;
def.
lowerTranslation = lowerTranslation;
def.
upperTranslation = upperTranslation;
def.
enableMotor = enableMotor;
def.
maxMotorForce = maxMotorForce;
def.
motorSpeed = motorSpeed;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}

World_M(jlong, jniCreatePulleyJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
groundAnchorAX,
jfloat groundAnchorAY, jfloat
groundAnchorBX,
jfloat groundAnchorBY, jfloat
localAnchorAX,
jfloat localAnchorAY, jfloat
localAnchorBX,
jfloat localAnchorBY, jfloat
lengthA,
jfloat lengthB, jfloat
ratio)
{
b2PulleyJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
groundAnchorA = b2Vec2(groundAnchorAX, groundAnchorAY);
def.
groundAnchorB = b2Vec2(groundAnchorBX, groundAnchorBY);
def.
localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
def.
localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
def.
lengthA = lengthA;
def.
lengthB = lengthB;
def.
ratio = ratio;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}

World_M(jlong, jniCreateRevoluteJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
localAnchorAX,
jfloat localAnchorAY, jfloat
localAnchorBX,
jfloat localAnchorBY, jfloat
referenceAngle,
jboolean enableLimit, jfloat
lowerAngle,
jfloat upperAngle, jboolean
enableMotor,
jfloat motorSpeed, jfloat
maxMotorTorque)
{
b2RevoluteJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
def.
localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
def.
referenceAngle = referenceAngle;
def.
enableLimit = enableLimit;
def.
lowerAngle = lowerAngle;
def.
upperAngle = upperAngle;
def.
enableMotor = enableMotor;
def.
motorSpeed = motorSpeed;
def.
maxMotorTorque = maxMotorTorque;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}

World_M(jlong, jniCreateWeldJoint)(JNIEnv * env, jobject
object,
jlong bodyA, jlong
bodyB,
jboolean collideConnected, jfloat
localAnchorAX,
jfloat localAnchorAY, jfloat
localAnchorBX,
jfloat localAnchorBY, jfloat
referenceAngle,
jfloat frequencyHz, jfloat
dampingRatio)
{
b2WeldJointDef def;
def.
bodyA = (b2Body *) bodyA;
def.
bodyB = (b2Body *) bodyB;
def.
collideConnected = collideConnected;
def.
localAnchorA = b2Vec2(localAnchorAX, localAnchorAY);
def.
localAnchorB = b2Vec2(localAnchorBX, localAnchorBY);
def.
referenceAngle = referenceAngle;
def.
frequencyHz = frequencyHz;
def.
dampingRatio = dampingRatio;
return (jlong)((b2World*)env->
GetLongField(object, worldPtr
))->
CreateJoint(&def);
}
World_M(void, jniDestroyJoint) (JNIEnv
*env,
jobject object, jlong
jointworldPtr)
{
b2World *world = (b2World *) env->GetLongField(object, worldPtr);
CustomContactFilter contactFilter(env, object);
CustomContactListener contactListener(env, object);
world->
SetContactFilter(&contactFilter);
world->
SetContactListener(&contactListener);
world->
DestroyJoint((b2Joint
*)jointworldPtr );
world->
SetContactFilter(&defaultFilter);
world->SetContactListener(0);
}
World_M(void, step) (JNIEnv
*env,
jobject object, jfloat
timeStep,
jint velocityIterations, jint
positionIterations)
{
b2World *world = (b2World *) env->GetLongField(object, worldPtr);
CustomContactFilter contactFilter(env, object);
CustomContactListener contactListener(env, object);
world->
SetContactFilter(&contactFilter);
world->
SetContactListener(&contactListener);
world->
Step(timeStep, velocityIterations, positionIterations
);
world->
SetContactFilter(&defaultFilter);
world->SetContactListener(0);
}
World_M(void, clearForces) (JNIEnv
*env,
jobject object
)
{
((b2World*)env->
GetLongField(object, worldPtr
))->

ClearForces();

}
World_M(void, setWarmStarting) (JNIEnv
*env,
jobject object, jboolean
flag)
{
((b2World*)env->
GetLongField(object, worldPtr
))->
SetWarmStarting(flag);
}
World_M(void, setContinuousPhysics) (JNIEnv
*env,
jobject object, jboolean
flag)
{
((b2World*)env->
GetLongField(object, worldPtr
))->
SetContinuousPhysics(flag);
}
World_M(jint, getProxyCount)(JNIEnv * env, jobject
object)
{
return (jint) ((b2World*)env->
GetLongField(object, worldPtr
))->

GetProxyCount();

}
World_M(jint, getBodyCount)(JNIEnv * env, jobject
object)
{
return (jint) ((b2World*)env->
GetLongField(object, worldPtr
))->

GetBodyCount();

}
World_M(jint, getJointCount)(JNIEnv * env, jobject
object)
{
return (jint) ((b2World*)env->
GetLongField(object, worldPtr
))->

GetJointCount();

}
World_M(jint, getContactCount)(JNIEnv * env, jobject
object)
{
return (jint) ((b2World*)env->
GetLongField(object, worldPtr
))->

GetContactCount();

}
World_M(void, setGravity) (JNIEnv
*env,
jobject object, jfloat
x,
jfloat y
)
{
return ((b2World*)env->
GetLongField(object, worldPtr
))->
SetGravity(b2Vec2(x, y)
);
}
World_M(void, jniGetGravity) (JNIEnv
*env,
jobject object, jfloatArray
vals)
{
jfloat *data = (jfloat * )
env->
GetPrimitiveArrayCritical(vals,
0);
b2Vec2 g = ((b2World *) env->GetLongField(object, worldPtr))->GetGravity();
data[0] = g.
x;
data[1] = g.
y;
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
}
World_M(jboolean, isLocked)(JNIEnv * env, jobject
object)
{
return ((b2World*)env->
GetLongField(object, worldPtr
))->

IsLocked();

}
World_M(void, setAutoClearForces) (JNIEnv
*env,
jobject object, jboolean
flag)
{
((b2World*)env->
GetLongField(object, worldPtr
))->
SetAutoClearForces(flag);
}
World_M(jboolean, getAutoClearForces)(JNIEnv * env, jobject
object)
{
return ((b2World*)env->
GetLongField(object, worldPtr
))->

GetAutoClearForces();

}
World_M(void, jniQueryAABB) (JNIEnv
*env,
jobject object, jfloat
lowX,
jfloat lowY, jfloat
upX,
jfloat upY
)
{
b2AABB aabb;
aabb.
lowerBound = b2Vec2(lowX, lowY);
aabb.
upperBound = b2Vec2(upX, upY);
CustomQueryCallback callback(env, object);
((b2World*)env->
GetLongField(object, worldPtr
))->
QueryAABB( &callback, aabb
);
}
World_M(void, jniGetContactList) (JNIEnv
*env,
jobject object, jlongArray
vals)
{
jfloat *data = (jfloat * )
env->
GetPrimitiveArrayCritical(vals,
0);
b2Contact *contact = ((b2World *) env->GetLongField(object, worldPtr))->GetContactList();
int i = 0;
while( contact != 0 )
{
data[i++] = (long long)
contact;
contact = contact->GetNext();
}
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
}
World_M(void, jniDispose) (JNIEnv
*env,
jobject object
)
{
delete ((b2World*)env->
GetLongField(object, worldPtr
));
env->
SetLongField(object, worldPtr,
0);
}
World_M(void, setVelocityThreshold) (JNIEnv
*env,
jclass clazz, jfloat
threshold)
{
b2_velocityThreshold = threshold;
}
World_M(jfloat, getVelocityThreshold)(JNIEnv * env, jclass
clazz)
{
return
b2_velocityThreshold;
}
World_M(void, jniRayCast) (JNIEnv
*env,
jobject object, jfloat
aX,
jfloat aY, jfloat
bX,
jfloat bY
)
{
CustomRayCastCallback callback(env, object);
((b2World*)env->
GetLongField(object, worldPtr
))->
RayCast( &callback, b2Vec2(aX, aY), b2Vec2(bX, bY)
);
}


