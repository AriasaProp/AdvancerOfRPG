#ifndef Included_Manifold
#define Included_Manifold

#include <jni.h>

extern "C" {

#define Manifold_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_Manifold_##M

Manifold_M(void, initialize)(JNIEnv *, jclass);
Manifold_M(jint, jniGetType)(JNIEnv *, jobject);
Manifold_M(jint, getPointCount)(JNIEnv *, jobject);
Manifold_M(void, jniGetLocalNormal)(JNIEnv *, jobject, jfloatArray);
Manifold_M(void, jniGetLocalPoint)(JNIEnv *, jobject, jfloatArray);
Manifold_M(jint, jniGetPoint)(JNIEnv *, jobject, jfloatArray, jint);

}
#endif // Included_Manifold
