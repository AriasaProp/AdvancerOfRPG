#include <jni.h>

extern "C" {
#define GraphFunc_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_GraphFunc_##M
GraphFunc_M(jstring, nativeLog)(JNIEnv *, jclass);
}

GraphFunc_M(jstring, nativeLog)(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF("28/05/2022");
}
