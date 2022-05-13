#include "Body.h"
#include "Dynamics/b2Body.h"
#include "Dynamics/b2Fixture.h"

static jfieldID bodyPtr;
Body_M(void, initialize) (JNIEnv *env, jclass clazz)
{
    //initialize field ID of JNI
    bodyPtr = env->GetFieldID(clazz, "bodyPtr", "J");
}
Body_M(jlong, jniCreateFixture___3JFFFZSSS) (JNIEnv *env, jobject object, jlong shapebodyPtr, jfloat friction, jfloat restitution, jfloat density, jboolean isSensor, short filterCategoryBits, short filterMaskBits, short filterGroupIndex)
{
    b2FixtureDef fixtureDef;
    fixtureDef.shape = (b2Shape*)shapebodyPtr;
    fixtureDef.friction = friction;
    fixtureDef.restitution = restitution;
    fixtureDef.density = density;
    fixtureDef.isSensor = isSensor;
    fixtureDef.filter.maskBits = filterMaskBits;
    fixtureDef.filter.categoryBits = filterCategoryBits;
    fixtureDef.filter.groupIndex = filterGroupIndex;
    return (jlong)((b2Body*)env->GetLongField(object, bodyPtr))->CreateFixture( &fixtureDef );
}
Body_M(jlong, jniCreateFixture___3JF) (JNIEnv *env, jobject object, jlong shapebodyPtr, jfloat density)
{
    return (jlong)((b2Body*)env->GetLongField(object, bodyPtr))->CreateFixture( (b2Shape*)shapebodyPtr, density );
}
Body_M(void, setTransform) (JNIEnv *env, jobject object, jfloat positionX, jfloat positionY, jfloat angle)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetTransform(b2Vec2(positionX, positionY), angle);
}
Body_M(void, jniGetTransform) (JNIEnv *env, jobject object, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Transform t = ((b2Body*)env->GetLongField(object, bodyPtr))->GetTransform();
    data[0] = t.p.x;
    data[1] = t.p.y;
    data[2] = t.q.c;
    data[3] = t.q.s;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(void, jniGetPosition) (JNIEnv *env, jobject object, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 p = ((b2Body*)env->GetLongField(object, bodyPtr))->GetPosition();
    data[0] = p.x;
    data[1] = p.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(jfloat, getAngle) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->GetAngle();
}
Body_M(void, jniGetWorldCenter) (JNIEnv *env, jobject object, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 w = ((b2Body*)env->GetLongField(object, bodyPtr))->GetWorldCenter();
    data[0] = w.x;
    data[1] = w.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(void, jniGetLocalCenter) (JNIEnv *env, jobject object, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 w = ((b2Body*)env->GetLongField(object, bodyPtr))->GetLocalCenter();
    data[0] = w.x;
    data[1] = w.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}

Body_M(void, setLinearVelocity) (JNIEnv *env, jobject object, jfloat x, jfloat y)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetLinearVelocity(b2Vec2(x, y));
}
Body_M(void, jniGetLinearVelocity) (JNIEnv *env, jobject object, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 l = ((b2Body*)env->GetLongField(object, bodyPtr))->GetLinearVelocity();
    data[0] = l.x;
    data[1] = l.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(void, setAngularVelocity) (JNIEnv *env, jobject object, jfloat omega)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetAngularVelocity(omega);
}
Body_M(jfloat, getAngularVelocity) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->GetAngularVelocity();
}
Body_M(void, applyForce) (JNIEnv *env, jobject object, jfloat forceX, jfloat forceY, jfloat pointX, jfloat pointY, jboolean wake)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->ApplyForce(b2Vec2(forceX, forceY), b2Vec2(pointX, pointY), wake);
}
Body_M(void, applyForceToCenter) (JNIEnv *env, jobject object, jfloat forceX, jfloat forceY, jboolean wake)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->ApplyForceToCenter(b2Vec2(forceX, forceY), wake);
}
Body_M(void, applyTorque) (JNIEnv *env, jobject object, jfloat torque, jboolean wake)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->ApplyTorque(torque, wake);
}
Body_M(void, applyLinearImpulse) (JNIEnv *env, jobject object, jfloat impulseX, jfloat impulseY, jfloat pointX, jfloat pointY, jboolean wake)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->ApplyLinearImpulse( b2Vec2( impulseX, impulseY ), b2Vec2( pointX, pointY ), wake);
}
Body_M(void, applyAngularImpulse) (JNIEnv *env, jobject object, jfloat impulse, jboolean wake)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->ApplyAngularImpulse(impulse, wake);
}
Body_M(jfloat, getMass) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->GetMass();
}
Body_M(jfloat, getInertia) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->GetInertia();
}
Body_M(void, jniGetMassData) (JNIEnv *env, jobject object, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2MassData m;
    ((b2Body*)env->GetLongField(object, bodyPtr))->GetMassData(&m);
    data[0] = m.mass;
    data[1] = m.center.x;
    data[2] = m.center.y;
    data[3] = m.I;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(void, setMassData) (JNIEnv *env, jobject object, jfloat mass, jfloat centerX, jfloat centerY, jfloat I)
{
    b2MassData m;
    m.mass = mass;
    m.center.x = centerX;
    m.center.y = centerY;
    m.I = I;
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetMassData(&m);
}
Body_M(void, resetMassData) (JNIEnv *env, jobject object)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->ResetMassData();
}
Body_M(void, jniGetWorldPoint) (JNIEnv *env, jobject object, jfloat localPointX, jfloat localPointY, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 w = ((b2Body*)env->GetLongField(object, bodyPtr))->GetWorldPoint( b2Vec2( localPointX, localPointY ) );
    data[0] = w.x;
    data[1] = w.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(void, jniGetWorldVector) (JNIEnv *env, jobject object, jfloat localVectorX, jfloat localVectorY, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 w = ((b2Body*)env->GetLongField(object, bodyPtr))->GetWorldVector( b2Vec2( localVectorX, localVectorY ) );
    data[0] = w.x;
    data[1] = w.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(void, jniGetLocalPoint) (JNIEnv *env, jobject object, jfloat worldPointX, jfloat worldPointY, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 w = ((b2Body*)env->GetLongField(object, bodyPtr))->GetLocalPoint( b2Vec2( worldPointX, worldPointY ) );
    data[0] = w.x;
    data[1] = w.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(void, jniGetLocalVector) (JNIEnv *env, jobject object, jfloat worldVectorX, jfloat worldVectorY, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 w = ((b2Body*)env->GetLongField(object, bodyPtr))->GetLocalVector( b2Vec2( worldVectorX, worldVectorY ) );
    data[0] = w.x;
    data[1] = w.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(void, jniGetLinearVelocityFromWorldPoint) (JNIEnv *env, jobject object, jfloat worldPointX, jfloat worldPointY, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 w = ((b2Body*)env->GetLongField(object, bodyPtr))->GetLinearVelocityFromWorldPoint( b2Vec2( worldPointX, worldPointY ) );
    data[0] = w.x;
    data[1] = w.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(void, jniGetLinearVelocityFromLocalPoint) (JNIEnv *env, jobject object, jfloat localPointX, jfloat localPointY, jfloatArray vals)
{
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Vec2 w = ((b2Body*)env->GetLongField(object, bodyPtr))->GetLinearVelocityFromLocalPoint( b2Vec2( localPointX, localPointY ) );
    data[0] = w.x;
    data[1] = w.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}
Body_M(jfloat, getLinearDamping) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->GetLinearDamping();
}
Body_M(void, setLinearDamping) (JNIEnv *env, jobject object, jfloat linearDamping)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetLinearDamping(linearDamping);
}
Body_M(jfloat, getAngularDamping) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->GetAngularDamping();
}
Body_M(void, setAngularDamping) (JNIEnv *env, jobject object, jfloat angularDamping)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetAngularDamping(angularDamping);
}
Body_M(void, jniSetType) (JNIEnv *env, jobject object, jint type)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetType(static_cast<b2BodyType>(type));
}
Body_M(jint, jniGetType) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->GetType();
}
Body_M(void, setBullet) (JNIEnv *env, jobject object, jboolean flag)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetBullet(flag);
}
Body_M(jboolean, isBullet) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->IsBullet();
}
Body_M(void, setSleepingAllowed) (JNIEnv *env, jobject object, jboolean flag)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetSleepingAllowed(flag);
}
Body_M(jboolean, isSleepingAllowed) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->IsSleepingAllowed();
}
Body_M(void, setAwake) (JNIEnv *env, jobject object, jboolean flag)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetAwake(flag);
}
Body_M(jboolean, isAwake) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->IsAwake();
}
Body_M(void, jniSetActive) (JNIEnv *env, jobject object, jboolean flag)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetActive(flag);
}
Body_M(jboolean, isActive) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->IsActive();
}
Body_M(void, setFixedRotation) (JNIEnv *env, jobject object, jboolean flag)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetFixedRotation(flag);
}
Body_M(jboolean, isFixedRotation) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->IsFixedRotation();
}
Body_M(jfloat, getGravityScale) (JNIEnv *env, jobject object)
{
    return ((b2Body*)env->GetLongField(object, bodyPtr))->GetGravityScale();
}
Body_M(void, setGravityScale) (JNIEnv *env, jobject object, jfloat scale)
{
    ((b2Body*)env->GetLongField(object, bodyPtr))->SetGravityScale(scale);
}
