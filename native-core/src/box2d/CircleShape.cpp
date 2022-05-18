#include "CircleShape.h"
#include "Shape.h"
#include "Collision/Shapes/b2CircleShape.h"

CircleShape_M(jlong, newCircleShape)(JNIEnv * env, jclass
clazz)
{
return (jlong)(new

b2CircleShape()

);
}

CircleShape_M(void, getPosition)(JNIEnv * env, jobject
obj,
jfloatArray vals
)
{
jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
b2CircleShape *circle = (b2CircleShape *) env->GetLongField(obj, shapePtr);
data[0] = circle->m_p.
x;
data[1] = circle->m_p.
y;
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
}
CircleShape_M(void, setPosition)(JNIEnv * env, jobject
obj,
jfloat x, jfloat
y)
{
b2CircleShape *circle = (b2CircleShape *) env->GetLongField(obj, shapePtr);
circle->m_p.
x = x;
circle->m_p.
y = y;
}
