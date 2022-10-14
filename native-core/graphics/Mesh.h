#ifndef Included_Mesh
#define Included_Mesh

#include <jni.h>

extern "C" {

#define Mesh_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_graphics_Mesh_##M

Mesh_M(jobject, create)(JNIEnv *, jclass clazz, jint);
Mesh_M(void, updateShort)(JNIEnv *, jclass, jshortArray, jint, jobject, jint, jint);
Mesh_M(void, updateFloat)(JNIEnv *, jclass, jfloatArray, jint, jobject, jint, jint);
Mesh_M(void, destroy)(JNIEnv *, jclass clazz, jobject);

}
#endif //Included_Mesh
