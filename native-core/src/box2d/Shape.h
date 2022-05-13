#ifndef Included_Shape
#define Included_Shape

#include <jni.h>
//java value
static jfieldID shapePtr;

extern "C" {

#define Shape_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_Shape_##M

    Shape_M(void, initialize) (JNIEnv *, jclass);
    Shape_M(jfloat, getRadius) (JNIEnv *, jobject);
    Shape_M(void, setRadius) (JNIEnv *, jobject, jfloat);
    Shape_M(void, jniDispose) (JNIEnv *, jobject);
    Shape_M(jint, jniGetType) (JNIEnv *, jobject);
    Shape_M(jint, getChildCount) (JNIEnv *, jobject);
}

#endif // Included_Shape
