#include "Matrix4.h"
#include <memory.h>
#include <stdio.h>
#include <string.h>
#include <malloc.h>
#include <cmath>

#define M00 0
#define M01 4
#define M02 8
#define M03 12
#define M10 1
#define M11 5
#define M12 9
#define M13 13
#define M20 2
#define M21 6
#define M22 10
#define M23 14
#define M30 3
#define M31 7
#define M32 11
#define M33 15

//private function
static float tmp[16];//

void inline
from(float *mat, float tX, float tY, float tZ, float qX, float qY, float qZ, float qW, float sX,
     float sY, float sZ) {
    const float wx = qW * qX, wy = qW * qY, wz = qW * qZ;
    const float xx = qX * qX, xy = qX * qY, xz = qX * qZ;
    const float yy = qY * qY, yz = qY * qZ, zz = qZ * qZ;
    mat[M00] = sX * (0.5f - yy - zz) * 2;
    mat[M01] = sY * (xy - wz) * 2;
    mat[M02] = sZ * (xz + wy) * 2;
    mat[M03] = tX;
    mat[M10] = sX * (xy + wz) * 2;
    mat[M11] = sY * (0.5f - xx - zz) * 2;
    mat[M12] = sZ * (yz - wx) * 2;
    mat[M13] = tY;
    mat[M20] = sX * (xz - wy) * 2;
    mat[M21] = sY * (yz + wx) * 2;
    mat[M22] = sZ * (0.5f - xx - yy) * 2;
    mat[M23] = tZ;
    mat[M30] = mat[M31] = mat[M32] = 0;
    mat[M33] = 1;
}

void inline mul(float *mata, float *matb) {
    tmp[M00] = mata[M00] * matb[M00] + mata[M01] * matb[M10] + mata[M02] * matb[M20] +
               mata[M03] * matb[M30];
    tmp[M01] = mata[M00] * matb[M01] + mata[M01] * matb[M11] + mata[M02] * matb[M21] +
               mata[M03] * matb[M31];
    tmp[M02] = mata[M00] * matb[M02] + mata[M01] * matb[M12] + mata[M02] * matb[M22] +
               mata[M03] * matb[M32];
    tmp[M03] = mata[M00] * matb[M03] + mata[M01] * matb[M13] + mata[M02] * matb[M23] +
               mata[M03] * matb[M33];
    tmp[M10] = mata[M10] * matb[M00] + mata[M11] * matb[M10] + mata[M12] * matb[M20] +
               mata[M13] * matb[M30];
    tmp[M11] = mata[M10] * matb[M01] + mata[M11] * matb[M11] + mata[M12] * matb[M21] +
               mata[M13] * matb[M31];
    tmp[M12] = mata[M10] * matb[M02] + mata[M11] * matb[M12] + mata[M12] * matb[M22] +
               mata[M13] * matb[M32];
    tmp[M13] = mata[M10] * matb[M03] + mata[M11] * matb[M13] + mata[M12] * matb[M23] +
               mata[M13] * matb[M33];
    tmp[M20] = mata[M20] * matb[M00] + mata[M21] * matb[M10] + mata[M22] * matb[M20] +
               mata[M23] * matb[M30];
    tmp[M21] = mata[M20] * matb[M01] + mata[M21] * matb[M11] + mata[M22] * matb[M21] +
               mata[M23] * matb[M31];
    tmp[M22] = mata[M20] * matb[M02] + mata[M21] * matb[M12] + mata[M22] * matb[M22] +
               mata[M23] * matb[M32];
    tmp[M23] = mata[M20] * matb[M03] + mata[M21] * matb[M13] + mata[M22] * matb[M23] +
               mata[M23] * matb[M33];
    tmp[M30] = mata[M30] * matb[M00] + mata[M31] * matb[M10] + mata[M32] * matb[M20] +
               mata[M33] * matb[M30];
    tmp[M31] = mata[M30] * matb[M01] + mata[M31] * matb[M11] + mata[M32] * matb[M21] +
               mata[M33] * matb[M31];
    tmp[M32] = mata[M30] * matb[M02] + mata[M31] * matb[M12] + mata[M32] * matb[M22] +
               mata[M33] * matb[M32];
    tmp[M33] = mata[M30] * matb[M03] + mata[M31] * matb[M13] + mata[M32] * matb[M23] +
               mata[M33] * matb[M33];
    memcpy(mata, tmp, sizeof(float) * 16);
}

void inline mulVec(float *mat, float *vec) {
    const float
            x = vec[0] * mat[M00] + vec[1] * mat[M01] + vec[2] * mat[M02] + mat[M03],
            y = vec[0] * mat[M10] + vec[1] * mat[M11] + vec[2] * mat[M12] + mat[M13],
            z = vec[0] * mat[M20] + vec[1] * mat[M21] + vec[2] * mat[M22] + mat[M23];
    vec[0] = x;
    vec[1] = y;
    vec[2] = z;
}
//lerp

void inline translate(float *val, jfloat x, jfloat y, jfloat z) {
    val[M03] += val[M00] * x + val[M01] * y + val[M02] * z;
    val[M13] += val[M10] * x + val[M11] * y + val[M12] * z;
    val[M23] += val[M20] * x + val[M21] * y + val[M22] * z;
    val[M33] += val[M30] * x + val[M31] * y + val[M32] * z;
}

void inline rotate(float *val, jfloat x, jfloat y, jfloat z, jfloat w) {
    const float wx = w * x, wy = w * y, wz = w * z;
    const float xx = x * x, xy = x * y, xz = x * z;
    const float yy = y * y, yz = y * z, zz = z * z;
    memcpy(tmp, val, sizeof(float) * 16);
    val[M00] = (tmp[M00] * (0.5f - yy - zz) + tmp[M01] * (xy + wz) + tmp[M02] * (xz - wy)) * 2;
    val[M01] = (tmp[M00] * (xy - wz) + tmp[M01] * (0.5f - xx - zz) + tmp[M02] * (yz + wx)) * 2;
    val[M02] = (tmp[M00] * (xz + wy) + tmp[M01] * (yz - wx) + tmp[M02] * (0.5f - xx - yy)) * 2;
    val[M10] = (tmp[M10] * (0.5f - yy - zz) + tmp[M11] * (xy + wz) + tmp[M12] * (xz - wy)) * 2;
    val[M11] = (tmp[M10] * (xy - wz) + tmp[M11] * (0.5f - xx - zz) + tmp[M12] * (yz + wx)) * 2;
    val[M12] = (tmp[M10] * (xz + wy) + tmp[M11] * (yz - wx) + tmp[M12] * (0.5f - xx - yy)) * 2;
    val[M20] = (tmp[M20] * (0.5f - yy - zz) + tmp[M21] * (xy + wz) + tmp[M22] * (xz - wy)) * 2;
    val[M21] = (tmp[M20] * (xy - wz) + tmp[M21] * (0.5f - xx - zz) + tmp[M22] * (yz + wx)) * 2;
    val[M22] = (tmp[M20] * (xz + wy) + tmp[M21] * (yz - wx) + tmp[M22] * (0.5f - xx - yy)) * 2;
    val[M30] = (tmp[M30] * (0.5f - yy - zz) + tmp[M31] * (xy + wz) + tmp[M32] * (xz - wy)) * 2;
    val[M31] = (tmp[M30] * (xy - wz) + tmp[M31] * (0.5f - xx - zz) + tmp[M32] * (yz + wx)) * 2;
    val[M32] = (tmp[M30] * (xz + wy) + tmp[M31] * (yz - wx) + tmp[M32] * (0.5f - xx - yy)) * 2;
}

void inline scale(float *val, jfloat x, jfloat y, jfloat z) {
    val[M00] *= x;
    val[M10] *= x;
    val[M20] *= x;
    val[M30] *= x;
    val[M01] *= y;
    val[M11] *= y;
    val[M21] *= y;
    val[M31] *= y;
    val[M02] *= z;
    val[M12] *= z;
    val[M22] *= z;
    val[M32] *= z;
}

void inline prjVec(float *mat, float *vec) {
    const float
            x = vec[0] * mat[M00] + vec[1] * mat[M01] + vec[2] * mat[M02] + mat[M03],
            y = vec[0] * mat[M10] + vec[1] * mat[M11] + vec[2] * mat[M12] + mat[M13],
            z = vec[0] * mat[M20] + vec[1] * mat[M21] + vec[2] * mat[M22] + mat[M23],
            w = vec[0] * mat[M30] + vec[1] * mat[M31] + vec[2] * mat[M32] + mat[M33];
    vec[0] = x / w;
    vec[1] = y / w;
    vec[2] = z / w;
}

void inline rotVec(float *mat, float *vec) {
    const float
            x = vec[0] * mat[M00] + vec[1] * mat[M01] + vec[2] * mat[M02],
            y = vec[0] * mat[M10] + vec[1] * mat[M11] + vec[2] * mat[M12],
            z = vec[0] * mat[M20] + vec[1] * mat[M21] + vec[2] * mat[M22];
    vec[0] = x;
    vec[1] = y;
    vec[2] = z;
}

void inline toProjection(float *mat, float near, float far, float fovy, float aspectRatio) {
    const float field = (float) std::tan((180.0 - fovy) / 360.0 * M_PI), depth = near - far;
    mat[M00] = field / aspectRatio;
    mat[M11] = field;
    mat[M22] = (far + near) / depth;
    mat[M32] = -1;
    mat[M23] = 2 * far * near / depth;
    mat[M10] = mat[M20] = mat[M30] = mat[M01] = mat[M21] = mat[M31] = mat[M02] = mat[M12] = mat[M03] = mat[M13] = mat[M33] = 0;
}

void inline
toProjection(float *mat, float left, float right, float bottom, float top, float near, float far) {
    const float width = right - left, height = top - bottom, depth = far - near;
    mat[M00] = 2 * near / width;
    mat[M11] = 2 * near / height;
    mat[M02] = (right + left) / width;
    mat[M12] = (top + bottom) / height;
    mat[M22] = (far + near) / depth;
    mat[M23] = 2 * far * near / depth;
    mat[M32] = -1;
    mat[M21] = mat[M31] = mat[M10] = mat[M20] = mat[M30] = mat[M01] = mat[M03] = mat[M13] = mat[M33] = 0;
}

void inline
toOrtho(float *mat, float left, float right, float bottom, float top, float near, float far) {
    const float width = (right - left), height = (top - bottom), depth = (far - near);
    mat[M00] = 2 / width;
    mat[M11] = 2 / height;
    mat[M22] = -2 / depth;
    mat[M03] = -(right + left) / width;
    mat[M13] = -(top + bottom) / height;
    mat[M23] = -(far + near) / depth;
    mat[M33] = 1;
    mat[M10] = mat[M20] = mat[M30] = mat[M01] = mat[M21] = mat[M31] = mat[M02] = mat[M12] = mat[M32] = 0;
}

float inline determinant(float *mat) {
    return mat[M30] * mat[M21] * mat[M12] * mat[M03] - mat[M20] * mat[M31] * mat[M12] * mat[M03] -
           mat[M30] * mat[M11] * mat[M22] * mat[M03] + mat[M10] * mat[M31] * mat[M22] * mat[M03] +
           mat[M20] * mat[M11] * mat[M32] * mat[M03] -
           mat[M10] * mat[M21] * mat[M32] * mat[M03] - mat[M30] * mat[M21] * mat[M02] * mat[M13] +
           mat[M20] * mat[M31] * mat[M02] * mat[M13] + mat[M30] * mat[M01] * mat[M22] * mat[M13] -
           mat[M00] * mat[M31] * mat[M22] * mat[M13] -
           mat[M20] * mat[M01] * mat[M32] * mat[M13] + mat[M00] * mat[M21] * mat[M32] * mat[M13] +
           mat[M30] * mat[M11] * mat[M02] * mat[M23] - mat[M10] * mat[M31] * mat[M02] * mat[M23] -
           mat[M30] * mat[M01] * mat[M12] * mat[M23] +
           mat[M00] * mat[M31] * mat[M12] * mat[M23] + mat[M10] * mat[M01] * mat[M32] * mat[M23] -
           mat[M00] * mat[M11] * mat[M32] * mat[M23] - mat[M20] * mat[M11] * mat[M02] * mat[M33] +
           mat[M10] * mat[M21] * mat[M02] * mat[M33] +
           mat[M20] * mat[M01] * mat[M12] * mat[M33] - mat[M00] * mat[M21] * mat[M12] * mat[M33] -
           mat[M10] * mat[M01] * mat[M22] * mat[M33] + mat[M00] * mat[M11] * mat[M22] * mat[M33];
}

void inline inverse(float *mat) {
    const float d = determinant(mat);
    if (d == 0) return;
    tmp[M00] = (mat[M12] * mat[M23] * mat[M31] - mat[M13] * mat[M22] * mat[M31] +
                mat[M13] * mat[M21] * mat[M32] - mat[M11] * mat[M23] * mat[M32] -
                mat[M12] * mat[M21] * mat[M33] + mat[M11] * mat[M22] * mat[M33]) / d;
    tmp[M01] = (mat[M03] * mat[M22] * mat[M31] - mat[M02] * mat[M23] * mat[M31] -
                mat[M03] * mat[M21] * mat[M32] + mat[M01] * mat[M23] * mat[M32] +
                mat[M02] * mat[M21] * mat[M33] - mat[M01] * mat[M22] * mat[M33]) / d;
    tmp[M02] = (mat[M02] * mat[M13] * mat[M31] - mat[M03] * mat[M12] * mat[M31] +
                mat[M03] * mat[M11] * mat[M32] - mat[M01] * mat[M13] * mat[M32] -
                mat[M02] * mat[M11] * mat[M33] + mat[M01] * mat[M12] * mat[M33]) / d;
    tmp[M03] = (mat[M03] * mat[M12] * mat[M21] - mat[M02] * mat[M13] * mat[M21] -
                mat[M03] * mat[M11] * mat[M22] + mat[M01] * mat[M13] * mat[M22] +
                mat[M02] * mat[M11] * mat[M23] - mat[M01] * mat[M12] * mat[M23]) / d;
    tmp[M10] = (mat[M13] * mat[M22] * mat[M30] - mat[M12] * mat[M23] * mat[M30] -
                mat[M13] * mat[M20] * mat[M32] + mat[M10] * mat[M23] * mat[M32] +
                mat[M12] * mat[M20] * mat[M33] - mat[M10] * mat[M22] * mat[M33]) / d;
    tmp[M11] = (mat[M02] * mat[M23] * mat[M30] - mat[M03] * mat[M22] * mat[M30] +
                mat[M03] * mat[M20] * mat[M32] - mat[M00] * mat[M23] * mat[M32] -
                mat[M02] * mat[M20] * mat[M33] + mat[M00] * mat[M22] * mat[M33]) / d;
    tmp[M12] = (mat[M03] * mat[M12] * mat[M30] - mat[M02] * mat[M13] * mat[M30] -
                mat[M03] * mat[M10] * mat[M32] + mat[M00] * mat[M13] * mat[M32] +
                mat[M02] * mat[M10] * mat[M33] - mat[M00] * mat[M12] * mat[M33]) / d;
    tmp[M13] = (mat[M02] * mat[M13] * mat[M20] - mat[M03] * mat[M12] * mat[M20] +
                mat[M03] * mat[M10] * mat[M22] - mat[M00] * mat[M13] * mat[M22] -
                mat[M02] * mat[M10] * mat[M23] + mat[M00] * mat[M12] * mat[M23]) / d;
    tmp[M20] = (mat[M11] * mat[M23] * mat[M30] - mat[M13] * mat[M21] * mat[M30] +
                mat[M13] * mat[M20] * mat[M31] - mat[M10] * mat[M23] * mat[M31] -
                mat[M11] * mat[M20] * mat[M33] + mat[M10] * mat[M21] * mat[M33]) / d;
    tmp[M21] = (mat[M03] * mat[M21] * mat[M30] - mat[M01] * mat[M23] * mat[M30] -
                mat[M03] * mat[M20] * mat[M31] + mat[M00] * mat[M23] * mat[M31] +
                mat[M01] * mat[M20] * mat[M33] - mat[M00] * mat[M21] * mat[M33]) / d;
    tmp[M22] = (mat[M01] * mat[M13] * mat[M30] - mat[M03] * mat[M11] * mat[M30] +
                mat[M03] * mat[M10] * mat[M31] - mat[M00] * mat[M13] * mat[M31] -
                mat[M01] * mat[M10] * mat[M33] + mat[M00] * mat[M11] * mat[M33]) / d;
    tmp[M23] = (mat[M03] * mat[M11] * mat[M20] - mat[M01] * mat[M13] * mat[M20] -
                mat[M03] * mat[M10] * mat[M21] + mat[M00] * mat[M13] * mat[M21] +
                mat[M01] * mat[M10] * mat[M23] - mat[M00] * mat[M11] * mat[M23]) / d;
    tmp[M30] = (mat[M12] * mat[M21] * mat[M30] - mat[M11] * mat[M22] * mat[M30] -
                mat[M12] * mat[M20] * mat[M31] + mat[M10] * mat[M22] * mat[M31] +
                mat[M11] * mat[M20] * mat[M32] - mat[M10] * mat[M21] * mat[M32]) / d;
    tmp[M31] = (mat[M01] * mat[M22] * mat[M30] - mat[M02] * mat[M21] * mat[M30] +
                mat[M02] * mat[M20] * mat[M31] - mat[M00] * mat[M22] * mat[M31] -
                mat[M01] * mat[M20] * mat[M32] + mat[M00] * mat[M21] * mat[M32]) / d;
    tmp[M32] = (mat[M02] * mat[M11] * mat[M30] - mat[M01] * mat[M12] * mat[M30] -
                mat[M02] * mat[M10] * mat[M31] + mat[M00] * mat[M12] * mat[M31] +
                mat[M01] * mat[M10] * mat[M32] - mat[M00] * mat[M11] * mat[M32]) / d;
    tmp[M33] = (mat[M01] * mat[M12] * mat[M20] - mat[M02] * mat[M11] * mat[M20] +
                mat[M02] * mat[M10] * mat[M21] - mat[M00] * mat[M12] * mat[M21] -
                mat[M01] * mat[M10] * mat[M22] + mat[M00] * mat[M11] * mat[M22]) / d;
    memcpy(mat, tmp, sizeof(float) * 16);
}

//public function
static jfieldID id;
Matrix4_M(void, initialize)(JNIEnv
*env,
jclass clazz
)
{
id = env->GetFieldID(clazz, "val", "[F");
}
Matrix4_M(jobject, set)(JNIEnv * env, jobject
object,
jfloat tX, jfloat
tY,
jfloat tZ, jfloat
qX,
jfloat qY, jfloat
qZ,
jfloat qW, jfloat
sX,
jfloat sY, jfloat
sZ)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *r = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
from(r, tX, tY, tZ, qX, qY, qZ, qW, sX, sY, sZ
);
env->
ReleasePrimitiveArrayCritical(obj_mat, r,
0);
return
object;
}
Matrix4_M(void, mul)(JNIEnv
*env,
jclass clazz, jfloatArray
obj_mata,
jfloatArray obj_matb
)
{
float *mata = (float *) env->GetPrimitiveArrayCritical(obj_mata, 0);
float *matb = (float *) env->GetPrimitiveArrayCritical(obj_matb, 0);
mul(mata, matb
);
env->
ReleasePrimitiveArrayCritical(obj_mata, mata,
0);
env->
ReleasePrimitiveArrayCritical(obj_matb, matb,
0);
}

Matrix4_M(void, mulVec___3F)(JNIEnv
*env,
jobject object, jfloatArray
obj_vec)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mat = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
float *vec = (float *) env->GetPrimitiveArrayCritical(obj_vec, 0);
mulVec(mat, vec
);
env->
ReleasePrimitiveArrayCritical(obj_mat, mat,
0);
env->
ReleasePrimitiveArrayCritical(obj_vec, vec,
0);
}

Matrix4_M(void, mulVec___3FIII)(JNIEnv
*env,
jobject object, jfloatArray
obj_vecs,
jint offset, jint
numVecs,
jint stride
)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mat = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
float *vecs = (float *) env->GetPrimitiveArrayCritical(obj_vecs, 0);
float *vec = vecs + offset;
for (
int i = 0;
i<numVecs;
i++)
{
mulVec(mat, vec
);
vec +=
stride;
}
env->
ReleasePrimitiveArrayCritical(obj_mat, mat,
0);
env->
ReleasePrimitiveArrayCritical(obj_vecs, vecs,
0);
}
Matrix4_M(void, lerp)(JNIEnv
*env,
jobject object, jfloatArray
obj_matb,
jfloat alpha
)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mata = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
float *matb = (float *) env->GetPrimitiveArrayCritical(obj_matb, 0);
for (
int i = 0;
i < 16; i++)
mata[i] += (matb[i] -  mata[i]) *
alpha;
env->
ReleasePrimitiveArrayCritical(obj_mat, mata,
0);
env->
ReleasePrimitiveArrayCritical(obj_matb, matb,
0);
}

Matrix4_M(jobject, translate)(JNIEnv * env, jobject
object,
jfloat x, jfloat
y,
jfloat z
)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mat = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
translate(mat, x, y, z
);
env->
ReleasePrimitiveArrayCritical(obj_mat, mat,
0);
return
object;
}
Matrix4_M(jobject, rotate)(JNIEnv * env, jobject
object,
jfloat x, jfloat
y,
jfloat z, jfloat
w)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mat = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
rotate(mat, x, y, z, w
);
env->
ReleasePrimitiveArrayCritical(obj_mat, mat,
0);
return
object;
}
Matrix4_M(jobject, scale)(JNIEnv * env, jobject
object,
jfloat x, jfloat
y,
jfloat z
)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mat = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
scale(mat, x, y, z
);
env->
ReleasePrimitiveArrayCritical(obj_mat, mat,
0);
return
object;
}

Matrix4_M(void, prj___3F)(JNIEnv
*env,
jobject object, jfloatArray
obj_vec)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mat = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
float *vec = (float *) env->GetPrimitiveArrayCritical(obj_vec, 0);
prjVec(mat, vec
);
env->
ReleasePrimitiveArrayCritical(obj_mat, mat,
0);
env->
ReleasePrimitiveArrayCritical(obj_vec, vec,
0);
}

Matrix4_M(void, prj___3FIII)(JNIEnv
*env,
jobject object, jfloatArray
obj_vecs,
jint offset, jint
numVecs,
jint stride
)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mat = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
float *vecs = (float *) env->GetPrimitiveArrayCritical(obj_vecs, 0);
float *vec = vecs + offset;
for (
int i = 0;
i<numVecs;
i++)
{
prjVec(mat, vec
);
vec +=
stride;
}
env->
ReleasePrimitiveArrayCritical(obj_mat, mat,
0);
env->
ReleasePrimitiveArrayCritical(obj_vecs, vecs,
0);
}

Matrix4_M(void, rot___3F)(JNIEnv
*env,
jobject object, jfloatArray
obj_vec)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mat = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
float *vec = (float *) env->GetPrimitiveArrayCritical(obj_vec, 0);
rotVec(mat, vec
);
env->
ReleasePrimitiveArrayCritical(obj_mat, mat,
0);
env->
ReleasePrimitiveArrayCritical(obj_vec, vec,
0);
}

Matrix4_M(void, rot___3FIII)(JNIEnv
*env,
jobject object, jfloatArray
obj_vecs,
jint offset, jint
numVecs,
jint stride
)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *mat = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
float *vecs = (float *) env->GetPrimitiveArrayCritical(obj_vecs, 0);
float *vec = vecs + offset;
for (
int i = 0;
i<numVecs;
i++)
{
rotVec(mat, vec
);
vec +=
stride;
}
env->
ReleasePrimitiveArrayCritical(obj_mat, mat,
0);
env->
ReleasePrimitiveArrayCritical(obj_vecs, vecs,
0);
}

Matrix4_M(jobject, setToProjection__FFFF)(JNIEnv * env, jobject
object,
jfloat near, jfloat
far,
jfloat fovy, jfloat
aspectRatio)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *val = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
toProjection(val, near, far, fovy, aspectRatio
);
env->
ReleasePrimitiveArrayCritical(obj_mat, val,
0);
return
object;
}

Matrix4_M(jobject, setToProjection__FFFFFF)(JNIEnv * env, jobject
object,
jfloat left, jfloat
right,
jfloat bottom, jfloat
top,
jfloat near, jfloat
far)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *val = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
toProjection(val, left, right, bottom, top, near, far
);
env->
ReleasePrimitiveArrayCritical(obj_mat, val,
0);
return
object;
}

Matrix4_M(jobject, setToOrtho)(JNIEnv * env, jobject
object,
jfloat left, jfloat
right,
jfloat bottom, jfloat
top,
jfloat near, jfloat
far)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *val = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
toOrtho(val, left, right, bottom, top, near, far
);
env->
ReleasePrimitiveArrayCritical(obj_mat, val,
0);
return
object;
}

Matrix4_M(jobject, inv)(JNIEnv * env, jobject
object)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *val = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
inverse(val);
env->
ReleasePrimitiveArrayCritical(obj_mat, val,
0);
return
object;
}

Matrix4_M(jfloat, det)(JNIEnv * env, jobject
object)
{
jfloatArray obj_mat = (jfloatArray)
env->
GetObjectField(object, id
);
float *v = (float *) env->GetPrimitiveArrayCritical(obj_mat, 0);
const float r = determinant(v);
env->
ReleasePrimitiveArrayCritical(obj_mat, v,
0);
return
r;
}

#undef M00
#undef M01
#undef M02
#undef M03
#undef M10
#undef M11
#undef M12
#undef M13
#undef M20
#undef M21
#undef M22
#undef M23
#undef M30
#undef M31
#undef M32
#undef M33
