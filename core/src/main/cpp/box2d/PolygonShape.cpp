#include "PolygonShape.h"
#include "Shape.h"
#include "Collision/Shapes/b2PolygonShape.h"

PolygonShape_M(jlong, newPolygonShape)(JNIEnv *env, jclass clazz) {
    return (jlong) (new b2PolygonShape());
}

PolygonShape_M(void, set)(JNIEnv *env, jobject obj, jfloatArray vals, jint offset, jint len) {
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    int numVertices = len / 2;
    b2Vec2 *verticesOut = new b2Vec2[numVertices];
    for (int i = 0; i < numVertices; i++) {
        verticesOut[i] = b2Vec2(data[(i << 1) + offset], data[(i << 1) + offset + 1]);
    }
    ((b2PolygonShape *) env->GetLongField(obj, shapePtr))->Set(verticesOut, numVertices);
    delete[] verticesOut;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}

PolygonShape_M(void, setAsBox___3FF)(JNIEnv *env, jobject obj, jfloat hx, jfloat hy) {
    ((b2PolygonShape *) env->GetLongField(obj, shapePtr))->SetAsBox(hx, hy);
}

PolygonShape_M(void, setAsBox___3FFFFF)(JNIEnv *env, jobject obj, jfloat hx, jfloat hy, jfloat cx,
                                        jfloat cy, jfloat angle) {
    ((b2PolygonShape *) env->GetLongField(obj, shapePtr))->SetAsBox(hx, hy, b2Vec2(cx, cy), angle);
}

PolygonShape_M(jint, getVertexCount)(JNIEnv *env, jobject obj) {
    return ((b2PolygonShape *) env->GetLongField(obj, shapePtr))->GetVertexCount();
}

PolygonShape_M(void, jniGetVertex)(JNIEnv *env, jobject obj, jint index, jfloatArray vals) {
    jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
    const b2Vec2 v = ((b2PolygonShape *) env->GetLongField(obj, shapePtr))->GetVertex(index);
    data[0] = v.x;
    data[1] = v.y;
    env->ReleasePrimitiveArrayCritical(vals, data, 0);
}


