#include <jni.h>

#define GraphFunc_M(R, M) extern "C" JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_GraphFunc_##M

GraphFunc_M(jstring, nativeLog)(JNIEnv *env, jclass clazz)
{
    return env->NewStringUTF("17/05/2022");
}
