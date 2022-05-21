#include "Contact.h"
#include "Collision/b2Collision.h"
#include "Dynamics/Contacts/b2Contact.h"

//java value id
static jfieldID contactPtr;

Contact_M(void, initialize) (JNIEnv
*env,
jclass clazz
)
{
//initialize field ID of JNI
contactPtr = env->GetFieldID(clazz, "addr", "J");
}
Contact_M(jint, jniGetWorldManifold)(JNIEnv * env, jobject
obj,
jfloatArray vals
)
{
jfloat *data = (jfloat *) env->GetPrimitiveArrayCritical(vals, 0);
b2Contact *contact = (b2Contact *) env->GetLongField(obj, contactPtr);
b2WorldManifold manifold;
contact->
GetWorldManifold(&manifold);
int numPoints = contact->GetManifold()->pointCount;
data[0] = manifold.normal.
x;
data[1] = manifold.normal.
y;
for(
int i = 0;
i<numPoints;
i++ )
{
data[2 + i*2] = manifold.points[i].
x;
data[2 + i*2+1] = manifold.points[i].
y;
}
data[6] = manifold.separations[0];
data[7] = manifold.separations[1];
env->
ReleasePrimitiveArrayCritical(vals, data,
0);
return
numPoints;
}
Contact_M(jboolean, isTouching)(JNIEnv * env, jobject
obj)
{
return ((b2Contact*)env->
GetLongField(obj, contactPtr
))->

IsTouching();

}
Contact_M(void, setEnabled) (JNIEnv
*env,
jobject obj, jboolean
flag)
{
((b2Contact*)env->
GetLongField(obj, contactPtr
))->
SetEnabled(flag);
}
Contact_M(jboolean, isEnabled)(JNIEnv * env, jobject
obj)
{
return ((b2Contact*)env->
GetLongField(obj, contactPtr
))->

IsEnabled();

}
Contact_M(jlong, jniGetFixtureA)(JNIEnv * env, jobject
obj)
{
return (jlong)((b2Contact*)env->
GetLongField(obj, contactPtr
))->

GetFixtureA();

}
Contact_M(jlong, jniGetFixtureB)(JNIEnv * env, jobject
obj)
{
return (jlong)((b2Contact*)env->
GetLongField(obj, contactPtr
))->

GetFixtureB();

}
Contact_M(jint, getChildIndexA)(JNIEnv * env, jobject
obj)
{
return ((b2Contact*)env->
GetLongField(obj, contactPtr
))->

GetChildIndexA();

}
Contact_M(jint, getChildIndexB)(JNIEnv * env, jobject
obj)
{
return ((b2Contact*)env->
GetLongField(obj, contactPtr
))->

GetChildIndexB();

}
Contact_M(void, setFriction) (JNIEnv
*env,
jobject obj, jfloat
friction)
{
((b2Contact*)env->
GetLongField(obj, contactPtr
))->
SetFriction(friction);
}
Contact_M(float, getFriction) (JNIEnv
*env,
jobject obj
)
{
return ((b2Contact*)env->
GetLongField(obj, contactPtr
))->

GetFriction();

}
Contact_M(void, resetFriction) (JNIEnv
*env,
jobject obj
)
{
((b2Contact*)env->
GetLongField(obj, contactPtr
))->

ResetFriction();

}
Contact_M(void, setRestitution) (JNIEnv
*env,
jobject obj, jfloat
restitution)
{
((b2Contact*)env->
GetLongField(obj, contactPtr
))->
SetRestitution(restitution);
}
Contact_M(float, getRestitution) (JNIEnv
*env,
jobject obj
)
{
return ((b2Contact*)env->
GetLongField(obj, contactPtr
))->

GetRestitution();

}
Contact_M(void, resetRestitution) (JNIEnv
*env,
jobject obj
)
{
((b2Contact*)env->
GetLongField(obj, contactPtr
))->

ResetRestitution();

}
Contact_M(float, getTangentSpeed) (JNIEnv
*env,
jobject obj
)
{
return ((b2Contact*)env->
GetLongField(obj, contactPtr
))->

GetTangentSpeed();

}
Contact_M(void, setTangentSpeed) (JNIEnv
*env,
jobject obj, jfloat
speed)
{
((b2Contact*)env->
GetLongField(obj, contactPtr
))->
SetTangentSpeed(speed);
}

