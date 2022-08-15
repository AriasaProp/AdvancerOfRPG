#include "Fixture.h"
#include "Dynamics/b2Fixture.h"

//java value id
static jfieldID fixturePtr;

Fixture_M(void, initialize)(JNIEnv *env, jclass clazz) {
    //initialize field ID of JNI
    fixturePtr = env->GetFieldID(clazz, "addr", "J");
}

Fixture_M(jint, jniGetType)(JNIEnv *env, jobject obj) {
    b2Shape::Type type = ((b2Fixture *) env->GetLongField(obj, fixturePtr))->GetType();
    switch (type) {
        case b2Shape::e_circle:
            return 0;
        case b2Shape::e_edge:
            return 1;
        case b2Shape::e_polygon:
            return 2;
        case b2Shape::e_chain:
            return 3;
        default:
            return -1;
    }
}

Fixture_M(void, jniGetShape)(JNIEnv *env, jobject obj, jlongArray vals) {
    jlong *data = (jlong *) env->GetPrimitiveArrayCritical(vals, 0);
    data[0] = (jlong) ((b2Fixture *) env->GetLongField(obj, fixturePtr))->GetShape();
    switch (((b2Fixture *) env->GetLongField(obj, fixturePtr))->GetShape()->GetType()) {
        case b2Shape::e_circle:
            data[1] = 0;
            break;
        case b2Shape::e_edge:
            data[1] = 1;
            break;
        case b2Shape::e_polygon:
            data[1] = 2;
            break;
        case b2Shape::e_chain:
            data[1] = 3;
            break;
        default:
            data[0] = -1;
            break;
    }
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}

Fixture_M(void, setSensor)(JNIEnv *env, jobject obj, jboolean sensor) {
    ((b2Fixture *) env->GetLongField(obj, fixturePtr))->SetSensor(sensor);
}

Fixture_M(jboolean, isSensor)(JNIEnv *env, jobject obj) {
    return ((b2Fixture *) env->GetLongField(obj, fixturePtr))->IsSensor();
}

Fixture_M(void, jniSetFilterData)(JNIEnv *env, jobject obj, jshort categoryBits, jshort maskBits,
                                  jshort groupIndex) {
    b2Filter filter;
    filter.categoryBits = categoryBits;
    filter.maskBits = maskBits;
    filter.groupIndex = groupIndex;
    ((b2Fixture *) env->GetLongField(obj, fixturePtr))->SetFilterData(filter);
}

Fixture_M(void, jniGetFilterData)(JNIEnv *env, jobject obj, jshortArray vals) {
    unsigned short *data = (unsigned short *) env->GetPrimitiveArrayCritical(vals, 0);
    b2Filter f = ((b2Fixture *) env->GetLongField(obj, fixturePtr))->GetFilterData();
    data[0] = f.maskBits;
    data[1] = f.categoryBits;
    data[2] = f.groupIndex;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}

Fixture_M(void, refilter)(JNIEnv *env, jobject obj) {
    ((b2Fixture *) env->GetLongField(obj, fixturePtr))->Refilter();
}

Fixture_M(jboolean, testPoint)(JNIEnv *env, jobject obj, jfloat x, jfloat y) {
    return ((b2Fixture *) env->GetLongField(obj, fixturePtr))->TestPoint(b2Vec2(x, y));
}

Fixture_M(void, setDensity)(JNIEnv *env, jobject obj, jfloat density) {
    ((b2Fixture *) env->GetLongField(obj, fixturePtr))->SetDensity(density);
}

Fixture_M(jfloat, getDensity)(JNIEnv *env, jobject obj) {
    return ((b2Fixture *) env->GetLongField(obj, fixturePtr))->GetDensity();
}

Fixture_M(jfloat, getFriction)(JNIEnv *env, jobject obj) {
    return ((b2Fixture *) env->GetLongField(obj, fixturePtr))->GetFriction();
}

Fixture_M(void, setFriction)(JNIEnv *env, jobject obj, jfloat friction) {
    ((b2Fixture *) env->GetLongField(obj, fixturePtr))->SetFriction(friction);
}

Fixture_M(jfloat, getRestitution)(JNIEnv *env, jobject obj) {
    return ((b2Fixture *) env->GetLongField(obj, fixturePtr))->GetRestitution();
}

Fixture_M(void, setRestitution)(JNIEnv *env, jobject obj, jfloat restitution) {
    ((b2Fixture *) env->GetLongField(obj, fixturePtr))->SetRestitution(restitution);

}

