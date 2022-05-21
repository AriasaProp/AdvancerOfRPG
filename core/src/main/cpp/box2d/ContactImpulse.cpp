#include "ContactImpulse.h"
#include "Dynamics/b2WorldCallbacks.h"

ContactImpulse_M(void, initialize) (JNIEnv
*env,
jclass clazz
)
{
//initialize field ID of JNI
contactImpulsePtr = env->GetFieldID(clazz, "addr", "J");
}
ContactImpulse_M(jfloatArray, getNormalImpulses)(JNIEnv * env, jobject
obj)
{
jfloatArray result = env->NewFloatArray(2);
env->
SetFloatArrayRegion(result,
0, 2, ((b2ContactImpulse*)env->
GetLongField(obj, contactImpulsePtr
))->normalImpulses);
return
result;
}
ContactImpulse_M(jfloatArray, getTangentImpulses)(JNIEnv * env, jobject
obj)
{
jfloatArray result = env->NewFloatArray(2);
env->
SetFloatArrayRegion(result,
0, 2, ((b2ContactImpulse*)env->
GetLongField(obj, contactImpulsePtr
))->tangentImpulses);
return
result;
}
ContactImpulse_M(jint, getCount)(JNIEnv * env, jobject
obj)
{
return ((b2ContactImpulse*)env->
GetLongField(obj, contactImpulsePtr
))->
count;
}