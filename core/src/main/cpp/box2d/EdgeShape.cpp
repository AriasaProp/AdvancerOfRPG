#include "EdgeShape.h"
#include "Shape.h"
#include "Collision/Shapes/b2EdgeShape.h"

EdgeShape_M(jlong, newEdgeShape)(JNIEnv * , jclass) {
    return (jlong)(new b2EdgeShape());
}

EdgeShape_M(void, set) (JNIEnv
*env,
jobject obj, jfloat
x1,
jfloat y1, jfloat
x2,
jfloat y2
)
{
((b2EdgeShape*)env->
GetLongField(obj, shapePtr
))->
Set(b2Vec2(x1, y1), b2Vec2(x2, y2)
);
}

EdgeShape_M(void, jniGetVertex0) (JNIEnv
*env,
jobject obj, jfloatArray
vals)
{
jfloat *data = (jfloat * )
env->
GetPrimitiveArrayCritical(vals,
0);
b2EdgeShape *edge = (b2EdgeShape *) env->GetLongField(obj, shapePtr);
data[0] = edge->m_vertex0.
x;
data[1] = edge->m_vertex0.
y;
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
}
EdgeShape_M(void, setVertex0) (JNIEnv
*env,
jobject obj, jfloat
x,
jfloat y
)
{
b2EdgeShape *edge = (b2EdgeShape *) env->GetLongField(obj, shapePtr);
edge->m_vertex0.
x = x;
edge->m_vertex0.
y = y;
}
EdgeShape_M(void, jniGetVertex1) (JNIEnv
*env,
jobject obj, jfloatArray
vals)
{
jfloat *data = (jfloat * )
env->
GetPrimitiveArrayCritical(vals,
0);
b2EdgeShape *edge = (b2EdgeShape *) env->GetLongField(obj, shapePtr);
data[0] = edge->m_vertex1.
x;
data[1] = edge->m_vertex1.
y;
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
}
EdgeShape_M(void, jniGetVertex2) (JNIEnv
*env,
jobject obj, jfloatArray
vals)
{
jfloat *data = (jfloat * )
env->
GetPrimitiveArrayCritical(vals,
0);
b2EdgeShape *edge = (b2EdgeShape *) env->GetLongField(obj, shapePtr);
data[0] = edge->m_vertex2.
x;
data[1] = edge->m_vertex2.
y;
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
}
EdgeShape_M(void, jniGetVertex3) (JNIEnv
*env,
jobject obj, jfloatArray
vals)
{
jfloat *data = (jfloat * )
env->
GetPrimitiveArrayCritical(vals,
0);
b2EdgeShape *edge = (b2EdgeShape *) env->GetLongField(obj, shapePtr);
data[0] = edge->m_vertex3.
x;
data[1] = edge->m_vertex3.
y;
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
}
EdgeShape_M(void, setVertex3) (JNIEnv
*env,
jobject obj, jfloat
x,
jfloat y
)
{
b2EdgeShape *edge = (b2EdgeShape *) env->GetLongField(obj, shapePtr);
edge->m_vertex3.
x = x;
edge->m_vertex3.
y = y;
}
EdgeShape_M(jboolean, hasVertex0)(JNIEnv * env, jobject
obj)
{
return ((b2EdgeShape*)env->
GetLongField(obj, shapePtr
))->
m_hasVertex0;
}
EdgeShape_M(void, setHasVertex0) (JNIEnv
*env,
jobject obj, jboolean
has)
{
((b2EdgeShape*)env->
GetLongField(obj, shapePtr
))->
m_hasVertex0 = has;
}
EdgeShape_M(jboolean, hasVertex3)(JNIEnv * env, jobject
obj)
{
return ((b2EdgeShape*)env->
GetLongField(obj, shapePtr
))->
m_hasVertex3;
}
EdgeShape_M(void, setHasVertex3) (JNIEnv
*env,
jobject obj, jboolean
has)
{
((b2EdgeShape*)env->
GetLongField(obj, shapePtr
))->
m_hasVertex3 = has;
}
	
	
