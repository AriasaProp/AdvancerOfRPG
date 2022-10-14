#include "Shape.h"
#include "Collision/Shapes/b2Shape.h"

Shape_M(void, initialize)(JNIEnv *env, jclass clazz) {
    //initialize field ID of JNI
    shapePtr = env->GetFieldID(clazz, "addr", "J");
}

Shape_M(jfloat, getRadius)(JNIEnv *env, jobject obj) {
    return ((b2Shape *) env->GetLongField(obj, shapePtr))->m_radius;
}

Shape_M(void, setRadius)(JNIEnv *env, jobject obj, jfloat radius) {
    ((b2Shape *) env->GetLongField(obj, shapePtr))->m_radius = radius;
}


Shape_M(void, jniDispose)(JNIEnv *env, jobject obj) {
    delete ((b2Shape *) env->GetLongField(obj, shapePtr));
}

Shape_M(jint, jniGetType)(JNIEnv *env, jobject obj) {
    switch (((b2Shape *) env->GetLongField(obj, shapePtr))->m_type) {
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

Shape_M(jint, getChildCount)(JNIEnv *env, jobject obj) {
    return ((b2Shape *) env->GetLongField(obj, shapePtr))->GetChildCount();
}
