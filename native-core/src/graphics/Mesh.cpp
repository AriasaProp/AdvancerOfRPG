#include "Mesh.h"
#include <malloc.h>
#include <string.h>

Mesh_M(jobject, create)(JNIEnv *env, jclass clazz, jint numBytes) {
    return env->NewDirectByteBuffer((char *) malloc(numBytes), numBytes);
}

Mesh_M(void, updateShort)(JNIEnv *env, jclass clazz, jshortArray obj_src, jint srcOffset,
                          jobject obj_dst, jint dstOffset, jint numShort) {
    unsigned char *dst = (unsigned char *) (obj_dst ? env->GetDirectBufferAddress(obj_dst) : 0);
    short *src = (short *) env->GetPrimitiveArrayCritical(obj_src, 0);
    memcpy(dst + dstOffset, src + srcOffset, numShort * sizeof(short));
    env->ReleasePrimitiveArrayCritical(obj_src, src, 0);
}

Mesh_M(void, updateFloat)(JNIEnv *env, jclass clazz, jfloatArray obj_src, jint srcOffset,
                          jobject obj_dst, jint dstOffset, jint numFloat) {
    unsigned char *dst = (unsigned char *) (obj_dst ? env->GetDirectBufferAddress(obj_dst) : 0);
    float *src = (float *) env->GetPrimitiveArrayCritical(obj_src, 0);
    memcpy(dst + (dstOffset * sizeof(float)), src + srcOffset, numFloat * sizeof(float));
    env->ReleasePrimitiveArrayCritical(obj_src, src, 0);
}

Mesh_M(void, destroy)(JNIEnv *env, jclass clazz, jobject obj_buffer) {
    if (!obj_buffer) return;
    free(env->GetDirectBufferAddress(obj_buffer));
}

