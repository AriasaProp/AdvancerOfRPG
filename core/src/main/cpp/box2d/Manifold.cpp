#include "Manifold.h"
#include "Collision/b2Collision.h"

//java value id
static jfieldID manifoldPtr;

Manifold_M(void, initialize) (JNIEnv
*env,
jclass clazz
)
{
//initialize field ID of JNI
manifoldPtr = env->GetFieldID(clazz, "addr", "J");
}

Manifold_M(jint, jniGetType)(JNIEnv * env, jobject
obj)
{
return ((b2Manifold*)env->
GetLongField(obj, manifoldPtr
))->
type;
}
Manifold_M(jint, getPointCount)(JNIEnv * env, jobject
obj)
{
return ((b2Manifold*)env->
GetLongField(obj, manifoldPtr
))->
pointCount;
}
Manifold_M(void, jniGetLocalNormal) (JNIEnv
*env,
jobject obj, jfloatArray
vals)
{
jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
const b2Vec2 a = ((b2Manifold *) env->GetLongField(obj, manifoldPtr))->localNormal;
data[0] = a.
x;
data[1] = a.
y;
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
}
Manifold_M(void, jniGetLocalPoint) (JNIEnv
*env,
jobject obj, jfloatArray
vals)
{
jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
const b2Vec2 a = ((b2Manifold *) env->GetLongField(obj, manifoldPtr))->localPoint;
data[0] = a.
x;
data[1] = a.
y;
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
}
Manifold_M(jint, jniGetPoint)(JNIEnv * env, jobject
obj,
jfloatArray vals, jint
idx)
{
jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
const b2ManifoldPoint a = ((b2Manifold *) env->GetLongField(obj, manifoldPtr))->points[idx];
data[0] = a.localPoint.
x;
data[1] = a.localPoint.
y;
data[2] = a.
normalImpulse;
data[3] = a.
tangentImpulse;
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
return (jint) a.id.
key;
}