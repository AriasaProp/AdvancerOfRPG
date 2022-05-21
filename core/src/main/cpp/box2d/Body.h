#ifndef Included_Body
#define Included_Body

#include <jni.h>

extern "C" {

#define Body_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_Body_##M

Body_M(void, initialize) (JNIEnv * , jclass ) ;
Body_M(jlong, jniCreateFixture___3JFFFZSSS)(JNIEnv * , jobject, jlong, jfloat, jfloat, jfloat,
                                            jboolean, jshort, jshort, jshort);
Body_M(jlong, jniCreateFixture___3JF)(JNIEnv * , jobject, jlong, jfloat);
Body_M(void, setTransform) (JNIEnv * , jobject , jfloat , jfloat , jfloat ) ;
Body_M(void, jniGetTransform) (JNIEnv * , jobject , jfloatArray ) ;
Body_M(void, jniGetPosition) (JNIEnv * , jobject , jfloatArray ) ;
Body_M(jfloat, getAngle)(JNIEnv * , jobject);
Body_M(void, jniGetWorldCenter) (JNIEnv * , jobject , jfloatArray ) ;
Body_M(void, jniGetLocalCenter) (JNIEnv * , jobject , jfloatArray ) ;
Body_M(void, setLinearVelocity) (JNIEnv * , jobject , jfloat , jfloat ) ;
Body_M(void, jniGetLinearVelocity) (JNIEnv * , jobject , jfloatArray ) ;
Body_M(void, setAngularVelocity) (JNIEnv * , jobject , jfloat ) ;
Body_M(jfloat, getAngularVelocity)(JNIEnv * , jobject);
Body_M(void, applyForce) (JNIEnv * , jobject , jfloat , jfloat , jfloat , jfloat , jboolean ) ;
Body_M(void, applyForceToCenter) (JNIEnv * , jobject , jfloat , jfloat , jboolean ) ;
Body_M(void, applyTorque) (JNIEnv * , jobject , jfloat , jboolean ) ;
Body_M(void,
       applyLinearImpulse) (JNIEnv * , jobject , jfloat , jfloat , jfloat , jfloat , jboolean ) ;
Body_M(void, applyAngularImpulse) (JNIEnv * , jobject , jfloat impulse, jboolean
wake ) ;
Body_M(jfloat, getMass)(JNIEnv * , jobject);
Body_M(jfloat, getInertia)(JNIEnv * , jobject);
Body_M(void, jniGetMassData) (JNIEnv * , jobject , jfloatArray ) ;
Body_M(void, setMassData) (JNIEnv * , jobject , jfloat , jfloat , jfloat , jfloat ) ;
Body_M(void, resetMassData) (JNIEnv * , jobject ) ;
Body_M(void, jniGetWorldPoint) (JNIEnv * , jobject , jfloat , jfloat , jfloatArray ) ;
Body_M(void, jniGetWorldVector) (JNIEnv * , jobject , jfloat , jfloat , jfloatArray ) ;
Body_M(void, jniGetLocalPoint) (JNIEnv * , jobject , jfloat , jfloat , jfloatArray ) ;
Body_M(void, jniGetLocalVector) (JNIEnv * , jobject , jfloat , jfloat , jfloatArray ) ;
Body_M(void,
       jniGetLinearVelocityFromWorldPoint) (JNIEnv * , jobject , jfloat , jfloat , jfloatArray ) ;
Body_M(void,
       jniGetLinearVelocityFromLocalPoint) (JNIEnv * , jobject , jfloat , jfloat , jfloatArray ) ;
Body_M(jfloat, getLinearDamping)(JNIEnv * , jobject);
Body_M(void, setLinearDamping) (JNIEnv * , jobject , jfloat ) ;
Body_M(jfloat, jniGetAngularDamping)(JNIEnv * , jobject);
Body_M(void, setAngularDamping) (JNIEnv * , jobject , jfloat ) ;
Body_M(void, jniSetType) (JNIEnv * , jobject , jint ) ;
Body_M(jint, jniGetType)(JNIEnv * , jobject);
Body_M(void, setBullet) (JNIEnv * , jobject , jboolean ) ;
Body_M(jboolean, IsBullet)(JNIEnv * , jobject);
Body_M(void, setSleepingAllowed) (JNIEnv * , jobject , jboolean ) ;
Body_M(jboolean, isSleepingAllowed)(JNIEnv * , jobject);
Body_M(void, setAwake) (JNIEnv * , jobject , jboolean ) ;
Body_M(jboolean, isAwake)(JNIEnv * , jobject);
Body_M(void, jniSetActive) (JNIEnv * , jobject , jboolean ) ;
Body_M(jboolean, isActive)(JNIEnv * , jobject);
Body_M(void, setFixedRotation) (JNIEnv * , jobject , jboolean ) ;
Body_M(jboolean, isFixedRotation)(JNIEnv * , jobject);
Body_M(jfloat, getGravityScale)(JNIEnv * , jobject);
Body_M(void, setGravityScale) (JNIEnv * , jobject , jfloat ) ;

}

#endif // Included_Body
