#ifndef Included_Matrix4
#define Included_Matrix4

#include <jni.h>

extern "C" {

#undef com_ariasaproject_advancerofrpg_math_Matrix4_serialVersionUID
#define com_ariasaproject_advancerofrpg_math_Matrix4_serialVersionUID -2717655254359579617i64
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M00
#define com_ariasaproject_advancerofrpg_math_Matrix4_M00 0L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M01
#define com_ariasaproject_advancerofrpg_math_Matrix4_M01 4L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M02
#define com_ariasaproject_advancerofrpg_math_Matrix4_M02 8L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M03
#define com_ariasaproject_advancerofrpg_math_Matrix4_M03 12L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M10
#define com_ariasaproject_advancerofrpg_math_Matrix4_M10 1L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M11
#define com_ariasaproject_advancerofrpg_math_Matrix4_M11 5L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M12
#define com_ariasaproject_advancerofrpg_math_Matrix4_M12 9L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M13
#define com_ariasaproject_advancerofrpg_math_Matrix4_M13 13L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M20
#define com_ariasaproject_advancerofrpg_math_Matrix4_M20 2L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M21
#define com_ariasaproject_advancerofrpg_math_Matrix4_M21 6L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M22
#define com_ariasaproject_advancerofrpg_math_Matrix4_M22 10L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M23
#define com_ariasaproject_advancerofrpg_math_Matrix4_M23 14L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M30
#define com_ariasaproject_advancerofrpg_math_Matrix4_M30 3L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M31
#define com_ariasaproject_advancerofrpg_math_Matrix4_M31 7L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M32
#define com_ariasaproject_advancerofrpg_math_Matrix4_M32 11L
#undef com_ariasaproject_advancerofrpg_math_Matrix4_M33
#define com_ariasaproject_advancerofrpg_math_Matrix4_M33 15L

#define Matrix4_M(R, M) JNIEXPORT R JNICALL Java_com_ariasaproject_advancerofrpg_math_Matrix4_##M

Matrix4_M(void, initialize)(JNIEnv *, jclass);
Matrix4_M(jobject, set)(JNIEnv *, jobject, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat,
                        jfloat, jfloat, jfloat);
Matrix4_M(void, mul)(JNIEnv *, jclass clazz, jfloatArray, jfloatArray);
Matrix4_M(void, mulVec___3F)(JNIEnv *, jobject, jfloatArray);
Matrix4_M(void, mulVec___3FIII)(JNIEnv *, jobject, jfloatArray, jint, jint, jint);
Matrix4_M(void, lerp)(JNIEnv *, jobject, jfloatArray, jfloat);
Matrix4_M(jobject, translate)(JNIEnv *, jobject, jfloat, jfloat, jfloat);
Matrix4_M(jobject, rotate)(JNIEnv *, jobject, jfloat, jfloat, jfloat, jfloat);
Matrix4_M(jobject, scale)(JNIEnv *, jobject, jfloat, jfloat, jfloat);
Matrix4_M(void, prj___3F)(JNIEnv *, jobject, jfloatArray);
Matrix4_M(void, prj___3FIII)(JNIEnv *, jobject, jfloatArray, jint, jint, jint);
Matrix4_M(void, rot___3F)(JNIEnv *, jobject, jfloatArray);
Matrix4_M(void, rot___3FIII)(JNIEnv *, jobject, jfloatArray, jint, jint, jint);
Matrix4_M(jobject, setToProjection__FFFF)(JNIEnv *, jobject, jfloat, jfloat, jfloat, jfloat);
Matrix4_M(jobject, setToProjection__FFFFFF)(JNIEnv *, jobject, jfloat, jfloat, jfloat, jfloat,
                                            jfloat, jfloat);
Matrix4_M(jobject, setToOrtho)(JNIEnv *, jobject, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat);
Matrix4_M(jobject, inv)(JNIEnv *, jobject);
Matrix4_M(jfloat, det)(JNIEnv *, jobject);

}
#endif //Included_Matrix4
