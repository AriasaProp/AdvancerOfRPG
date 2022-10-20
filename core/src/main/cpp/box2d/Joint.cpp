#include "Joint.h"
#include "Dynamics/Joints/b2Joint.h"

//java value id
static jfieldID jointPtr;

Joint_M(void, initialize)(JNIEnv *env, jclass clazz) {
    //initialize field ID of JNI
    jointPtr = env->GetFieldID(clazz, "addr", "J");
}

Joint_M(jint, jniGetType)(JNIEnv *env, jobject obj) {
    return ((b2Joint *) env->GetLongField(obj, jointPtr))->GetType();
}

Joint_M(jlong, jniGetBodyA)(JNIEnv *env, jobject obj) {
    return (jlong) ((b2Joint *) env->GetLongField(obj, jointPtr))->GetBodyA();
}

Joint_M(jlong, jniGetBodyB)(JNIEnv *env, jobject obj) {
    return (jlong) ((b2Joint *) env->GetLongField(obj, jointPtr))->GetBodyB();
}

Joint_M(void, jniGetAnchorA)(JNIEnv *env, jobject obj, jfloatArray vals) {
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    const b2Vec2 a = ((b2Joint *) env->GetLongField(obj, jointPtr))->GetAnchorA();
    data[0] = a.x;
    data[1] = a.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}

Joint_M(void, jniGetAnchorB)(JNIEnv *env, jobject obj, jfloatArray vals) {
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    const b2Vec2 a = ((b2Joint *) env->GetLongField(obj, jointPtr))->GetAnchorB();
    data[0] = a.x;
    data[1] = a.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}

Joint_M(jboolean, getCollideConnected)(JNIEnv *env, jobject obj) {
    return ((b2Joint *) env->GetLongField(obj, jointPtr))->GetCollideConnected();
}

Joint_M(void, jniGetReactionForce)(JNIEnv *env, jobject obj, jfloat inv_dt, jfloatArray vals) {
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    const b2Vec2 a = ((b2Joint *) env->GetLongField(obj, jointPtr))->GetReactionForce(inv_dt);
    data[0] = a.x;
    data[1] = a.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}

Joint_M(jfloat, getReactionTorque)(JNIEnv *env, jobject obj, jfloat inv_dt) {
    return ((b2Joint *) env->GetLongField(obj, jointPtr))->GetReactionTorque(inv_dt);
}

Joint_M(jboolean, isActive)(JNIEnv *env, jobject obj) {
    return ((b2Joint *) env->GetLongField(obj, jointPtr))->IsActive();
}