#ifndef Included_CircleShape
#define Included_CircleShape

#include <jni.h>

extern "C" {

#define CircleShape_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_CircleShape_##M

CircleShape_M(jlong, newCircleShape)(JNIEnv *, jclass);
CircleShape_M(void, getPosition) (JNIEnv * , jobject , jfloatArray ) ;
CircleShape_M(void, setPosition) (JNIEnv * , jobject , jfloat , jfloat ) ;
}

#endif // Included_CircleShape
