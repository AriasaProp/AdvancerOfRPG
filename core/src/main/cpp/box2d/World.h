#ifndef Included_World
#define Included_World

#include <jni.h>

extern "C" {

#define World_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_World_##M

World_M(void, initialize) (JNIEnv * , jclass ) ;
World_M(jlong, newWorld)(JNIEnv * , jclass, jfloat, jfloat, jboolean);
World_M(jlong, jniCreateBody)(JNIEnv * , jobject, jint, jfloat, jfloat, jfloat, jfloat, jfloat,
                              jfloat, jfloat, jfloat, jboolean, jboolean, jboolean, jboolean,
                              jboolean, jfloat);
World_M(void, jniDestroyBody)(JNIEnv * , jobject , jlong ) ;
World_M(void, jniDestroyFixture) (JNIEnv * , jobject , jlong , jlong ) ;
World_M(void, jniDeactivateBody) (JNIEnv * , jobject , jlong ) ;
World_M(jlong, jniCreateWheelJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat,
                                    jfloat, jfloat, jfloat, jfloat, jboolean, jfloat, jfloat,
                                    jfloat, jfloat);
World_M(jlong, jniCreateRopeJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat,
                                   jfloat, jfloat, jfloat);
World_M(jlong, jniCreateDistanceJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat,
                                       jfloat, jfloat, jfloat, jfloat, jfloat);
World_M(jlong, jniCreateFrictionJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat,
                                       jfloat, jfloat, jfloat, jfloat);
World_M(jlong, jniCreateGearJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jlong, jlong,
                                   jfloat);
World_M(jlong, jniCreateMotorJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat,
                                    jfloat, jfloat, jfloat, jfloat);
World_M(jlong, jniCreateMouseJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat
targetY , jfloat , jfloat , jfloat ) ;
World_M(jlong, jniCreatePrismaticJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat,
                                        jfloat, jfloat, jfloat, jfloat, jfloat, jboolean, jfloat,
                                        jfloat, jboolean, jfloat, jfloat);
World_M(jlong, jniCreatePulleyJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat,
                                     jfloat, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat,
                                     jfloat);
World_M(jlong, jniCreateRevoluteJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat,
                                       jfloat, jfloat, jfloat, jboolean, jfloat, jfloat, jboolean,
                                       jfloat, jfloat);
World_M(jlong, jniCreateWeldJoint)(JNIEnv * , jobject, jlong, jlong, jboolean, jfloat, jfloat,
                                   jfloat, jfloat, jfloat, jfloat, jfloat);
World_M(void, jniDestroyJoint) (JNIEnv * , jobject , jlong ) ;
World_M(void, step) (JNIEnv * , jobject , jfloat , jint , jint ) ;
World_M(void, clearForces) (JNIEnv * , jobject ) ;
World_M(void, setWarmStarting) (JNIEnv * , jobject , jboolean ) ;
World_M(void, setContinuousPhysics) (JNIEnv * , jobject , jboolean ) ;
World_M(jint, getProxyCount)(JNIEnv * , jobject);
World_M(jint, getBodyCount)(JNIEnv * , jobject);
World_M(jint, getJointCount)(JNIEnv * , jobject);
World_M(jint, getContactCount)(JNIEnv * , jobject);
World_M(void, setGravity) (JNIEnv * , jobject , jfloat , jfloat ) ;
World_M(void, jniGetGravity) (JNIEnv * , jobject , jfloatArray ) ;
World_M(jboolean, isLocked)(JNIEnv * , jobject);
World_M(void, setAutoClearForces) (JNIEnv * , jobject , jboolean ) ;
World_M(jboolean, getAutoClearForces)(JNIEnv * , jobject);
World_M(void, jniQueryAABB) (JNIEnv * , jobject , jfloat , jfloat , jfloat , jfloat ) ;
World_M(void, jniGetContactList) (JNIEnv * , jobject , jlongArray ) ;
World_M(void, jniDispose) (JNIEnv * , jobject ) ;
World_M(void, setVelocityThreshold) (JNIEnv * env , jclass , jfloat ) ;
World_M(jfloat, getVelocityThreshold)(JNIEnv * env, jclass);
World_M(void, jniRayCast) (JNIEnv * , jobject , jfloat , jfloat , jfloat , jfloat ) ;
}


#endif // Included_World
