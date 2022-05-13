#ifndef Included_BufferUtils
#define Included_BufferUtils

#include <jni.h>

extern "C" {
#define BufferUtils_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_utils_BufferUtils_##M

    BufferUtils_M(void, freeMemory)(JNIEnv *, jclass, jobject);
    BufferUtils_M(jobject, newDisposableByteBuffer)(JNIEnv *, jclass, jint);
    BufferUtils_M(jlong, getBufferAddress)(JNIEnv *, jclass, jobject);
    BufferUtils_M(void, clear)(JNIEnv *, jclass, jobject, jint);
    BufferUtils_M(void, copyJni___3FLjava_nio_Buffer_2II)(JNIEnv *, jclass, jfloatArray, jobject, jint, jint);
    BufferUtils_M(void, copyJni___3BILjava_nio_Buffer_2II)(JNIEnv *, jclass, jbyteArray, jint, jobject, jint, jint);
    BufferUtils_M(void, copyJni___3CILjava_nio_Buffer_2II)(JNIEnv *, jclass, jcharArray, jint, jobject, jint, jint);
    BufferUtils_M(void, copyJni___3SILjava_nio_Buffer_2II)(JNIEnv *, jclass, jshortArray, jint, jobject, jint, jint);
    BufferUtils_M(void, copyJni___3IILjava_nio_Buffer_2II)(JNIEnv *, jclass, jintArray, jint, jobject, jint, jint);
    BufferUtils_M(void, copyJni___3JILjava_nio_Buffer_2II)(JNIEnv *, jclass, jlongArray, jint, jobject, jint, jint);
    BufferUtils_M(void, copyJni___3FILjava_nio_Buffer_2II)(JNIEnv *, jclass, jfloatArray, jint, jobject, jint, jint);
    BufferUtils_M(void, copyJni___3DILjava_nio_Buffer_2II)(JNIEnv *, jclass, jdoubleArray, jint, jobject, jint, jint);
    BufferUtils_M(void, copyJni__Ljava_nio_Buffer_2ILjava_nio_Buffer_2II)(JNIEnv *, jclass, jobject, jint, jobject, jint, jint);
    BufferUtils_M(void, transformV4M4Jni__Ljava_nio_Buffer_2II_3FI)(JNIEnv *, jclass, jobject, jint, jint, jfloatArray, jint);
    BufferUtils_M(void, transformV4M4Jni___3FII_3FI)(JNIEnv *, jclass, jfloatArray, jint, jint, jfloatArray, jint);
    BufferUtils_M(void, transformV3M4Jni__Ljava_nio_Buffer_2II_3FI)(JNIEnv *, jclass, jobject, jint, jint, jfloatArray, jint);
    BufferUtils_M(void, transformV3M4Jni___3FII_3FI)(JNIEnv *, jclass, jfloatArray, jint, jint, jfloatArray, jint);
    BufferUtils_M(void, transformV2M4Jni__Ljava_nio_Buffer_2II_3FI)(JNIEnv *, jclass, jobject, jint, jint, jfloatArray, jint);
    BufferUtils_M(void, transformV2M4Jni___3FII_3FI)(JNIEnv *, jclass, jfloatArray, jint, jint, jfloatArray, jint);
    BufferUtils_M(void, transformV3M3Jni__Ljava_nio_Buffer_2II_3FI)(JNIEnv *, jclass, jobject, jint, jint, jfloatArray, jint);
    BufferUtils_M(void, transformV3M3Jni___3FII_3FI)(JNIEnv *, jclass, jfloatArray, jint, jint, jfloatArray, jint);
    BufferUtils_M(void, transformV2M3Jni__Ljava_nio_Buffer_2II_3FI)(JNIEnv *, jclass, jobject, jint, jint, jfloatArray, jint);
    BufferUtils_M(void, transformV2M3Jni___3FII_3FI)(JNIEnv *, jclass, jfloatArray, jint, jint, jfloatArray, jint);
    BufferUtils_M(jlong, find__Ljava_nio_Buffer_2IILjava_nio_Buffer_2II)(JNIEnv *, jclass, jobject, jint, jint, jobject, jint, jint);
    BufferUtils_M(jlong, find___3FIILjava_nio_Buffer_2II)(JNIEnv *, jclass, jfloatArray, jint, jint, jobject, jint, jint);
    BufferUtils_M(jlong, find__Ljava_nio_Buffer_2II_3FII)(JNIEnv *, jclass, jobject, jint, jint, jfloatArray, jint, jint);
    BufferUtils_M(jlong, find___3FII_3FII)(JNIEnv *, jclass, jfloatArray, jint, jint, jfloatArray, jint, jint);
    BufferUtils_M(jlong, find__Ljava_nio_Buffer_2IILjava_nio_Buffer_2IIF)(JNIEnv *, jclass, jobject, jint, jint, jobject, jint, jint, jfloat);
    BufferUtils_M(jlong, find___3FIILjava_nio_Buffer_2IIF)(JNIEnv *, jclass, jfloatArray, jint, jint, jobject, jint, jint, jfloat);
    BufferUtils_M(jlong, find__Ljava_nio_Buffer_2II_3FIIF)(JNIEnv *, jclass, jobject, jint, jint, jfloatArray, jint, jint, jfloat);
    BufferUtils_M(jlong, find___3FII_3FIIF)(JNIEnv *, jclass, jfloatArray, jint, jint, jfloatArray, jint, jint, jfloat);


}
#endif //Included_BufferUtils
