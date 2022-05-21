#ifndef Included_Joint
#define Included_Joint

#include <jni.h>

extern "C" {

#define Joint_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_Joint_##M

Joint_M(void, initialize) (JNIEnv * , jclass ) ;
Joint_M(jint, jniGetType)(JNIEnv * , jobject);
Joint_M(jlong, jniGetBodyA)(JNIEnv * , jobject);
Joint_M(jlong, jniGetBodyB)(JNIEnv * , jobject);
Joint_M(void, jniGetAnchorA) (JNIEnv * , jobject , jfloatArray ) ;
Joint_M(void, jniGetAnchorB) (JNIEnv * , jobject , jfloatArray ) ;
Joint_M(jboolean, getCollideConnected)(JNIEnv * , jobject);
Joint_M(void, jniGetReactionForce) (JNIEnv * , jobject , jfloat , jfloatArray ) ;
Joint_M(jfloat, getReactionTorque)(JNIEnv * , jobject, jfloat);
Joint_M(jboolean, isActive)(JNIEnv * , jobject);
}
#endif // Included_Joint
