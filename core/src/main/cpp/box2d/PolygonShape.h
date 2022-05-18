#ifndef Included_PolygonShape
#define Included_PolygonShape


#include <jni.h>

extern "C" {

#define PolygonShape_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_PolygonShape_##M

	PolygonShape_M(jlong, newPolygonShape) (JNIEnv *, jclass);
	PolygonShape_M(void, set) (JNIEnv *, jobject, jfloatArray, jint, jint);
	PolygonShape_M(void, setAsBox___3FF) (JNIEnv *, jobject, jfloat, jfloat);
	PolygonShape_M(void, setAsBox___3FFFFF) (JNIEnv *, jobject, jfloat, jfloat, jfloat, jfloat, jfloat);
	PolygonShape_M(jint, getVertexCount) (JNIEnv *, jobject);
	PolygonShape_M(void, jniGetVertex) (JNIEnv *, jobject, jint, jfloatArray);
	
}

#endif // Included_PolygonShape
