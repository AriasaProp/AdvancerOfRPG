#ifndef Included_EdgeShape
#define Included_EdgeShape

#include <jni.h>

extern "C" {

#define EdgeShape_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_EdgeShape_##M

EdgeShape_M(jlong, newEdgeShape)(JNIEnv *, jclass);
EdgeShape_M(void, set)(JNIEnv *, jobject, jfloat, jfloat, jfloat, jfloat);
EdgeShape_M(void, jniGetVertex0)(JNIEnv *, jobject, jfloatArray);
EdgeShape_M(void, setVertex0)(JNIEnv *, jobject, jfloat, jfloat);
EdgeShape_M(void, jniGetVertex1)(JNIEnv *, jobject, jfloatArray);
EdgeShape_M(void, jniGetVertex2)(JNIEnv *, jobject, jfloatArray);
EdgeShape_M(void, jniGetVertex3)(JNIEnv *, jobject, jfloatArray);
EdgeShape_M(void, setVertex3)(JNIEnv *, jobject, jfloat, jfloat);
EdgeShape_M(jboolean, hasVertex0)(JNIEnv *, jobject);
EdgeShape_M(void, setHasVertex0)(JNIEnv *, jobject, jboolean);
EdgeShape_M(jboolean, hasVertex3)(JNIEnv *, jobject);
EdgeShape_M(void, setHasVertex3)(JNIEnv *, jobject, jboolean);

}

#endif // Included_EdgeShape
