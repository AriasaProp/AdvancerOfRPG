#ifndef Included_ChainShape
#define Included_ChainShape

#include <jni.h>

extern "C" {

#define ChainShape_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_physics_box2d_ChainShape_##M

ChainShape_M(jlong, newChainShape)(JNIEnv *, jclass);
ChainShape_M(void, jniCreateLoop) (JNIEnv * , jobject , jfloatArray , jint , jint ) ;
ChainShape_M(void, jniCreateChain) (JNIEnv * , jobject , jfloatArray , jint , jint ) ;
ChainShape_M(void, setPrevVertex) (JNIEnv * , jobject , jfloat , jfloat ) ;
ChainShape_M(void, setNextVertex) (JNIEnv * , jobject , jfloat , jfloat ) ;
ChainShape_M(jint, getVertexCount)(JNIEnv * , jobject);
ChainShape_M(void, jniGetVertex) (JNIEnv * , jobject , jint , jfloatArray ) ;
}

#endif // Included_ChainShape
