#ifndef Included_Fixture
#define Included_Fixture


#include <jni.h>

extern "C" {

#define Fixture_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_Fixture_##M

	Fixture_M(void, initialize) (JNIEnv *, jclass);
	Fixture_M(jint, jniGetType) (JNIEnv *, jobject); 
	Fixture_M(void, jniGetShape) (JNIEnv *, jobject, jlongArray);
	Fixture_M(void, setSensor) (JNIEnv *, jobject, jboolean);
	Fixture_M(jboolean, isSensor) (JNIEnv *, jobject);
	Fixture_M(void, jniSetFilterData) (JNIEnv *, jobject, jshort, jshort, jshort);
	Fixture_M(void, jniGetFilterData) (JNIEnv *, jobject, jshortArray);
	Fixture_M(void, refilter) (JNIEnv *, jobject);
	Fixture_M(jboolean, testPoint) (JNIEnv *, jobject, jfloat, jfloat);
	Fixture_M(void, setDensity) (JNIEnv *, jobject, jfloat);
	Fixture_M(jfloat, getDensity) (JNIEnv *, jobject);
	Fixture_M(jfloat, getFriction) (JNIEnv *, jobject);
	Fixture_M(void, setFriction) (JNIEnv *, jobject, jfloat);
	Fixture_M(jfloat, getRestitution) (JNIEnv *, jobject);
	Fixture_M(void, setRestitution) (JNIEnv *, jobject, jfloat);

}
#endif // Included_Fixture
