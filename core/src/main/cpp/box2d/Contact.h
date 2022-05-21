#ifndef Included_Contact
#define Included_Contact

#include <jni.h>

extern "C" {

#define Contact_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_Contact_##M

Contact_M(void, initialize) (JNIEnv * , jclass ) ;
Contact_M(jint, jniGetWorldManifold)(JNIEnv * , jobject, jfloatArray);
Contact_M(jboolean, isTouching)(JNIEnv * , jobject);
Contact_M(void, setEnabled) (JNIEnv * , jobject , jboolean ) ;
Contact_M(jboolean, isEnabled)(JNIEnv * , jobject);
Contact_M(jlong, jniGetFixtureA)(JNIEnv * , jobject);
Contact_M(jlong, jniGetFixtureB)(JNIEnv * , jobject);
Contact_M(jint, getChildIndexA)(JNIEnv * , jobject);
Contact_M(jint, getChildIndexB)(JNIEnv * , jobject);
Contact_M(void, setFriction) (JNIEnv * , jobject , jfloat ) ;
Contact_M(float, getFriction) (JNIEnv * , jobject ) ;
Contact_M(void, resetFriction) (JNIEnv * , jobject ) ;
Contact_M(void, setRestitution) (JNIEnv * , jobject , jfloat ) ;
Contact_M(float, getRestitution) (JNIEnv * , jobject ) ;
Contact_M(void, resetRestitution) (JNIEnv * , jobject ) ;
Contact_M(float, getTangentSpeed) (JNIEnv * , jobject ) ;
Contact_M(void, setTangentSpeed) (JNIEnv * , jobject , jfloat ) ;

}

#endif // Included_Contact
