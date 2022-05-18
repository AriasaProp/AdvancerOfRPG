#ifndef Included_ContactImpulse
#define Included_ContactImpulse

#include <jni.h>
//java value
static jfieldID contactImpulsePtr;

extern "C" {

#define ContactImpulse_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_ContactImpulse_##M

    ContactImpulse_M(void, initialize) (JNIEnv *, jclass);
    ContactImpulse_M(jfloatArray, getNormalImpulses) (JNIEnv *, jobject);
    ContactImpulse_M(jfloatArray, getTangentImpulses) (JNIEnv *, jobject);
    ContactImpulse_M(jint, getCount) (JNIEnv *, jobject);
}

#endif // Included_ContactImpulse
