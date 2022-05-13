package com.ariasaproject.advancerofrpg.math;

import java.io.Serializable;

public class Matrix4 implements Serializable {
	private static final long serialVersionUID = 8189572388708539292L;
	public static final int M00 = 0, M10 = 1, M20 = 2, M30 = 3, M01 = 4, M11 = 5, M21 = 6, M31 = 7, M02 = 8, M12 = 9,
	M22 = 10, M32 = 11, M03 = 12, M13 = 13, M23 = 14, M33 = 15;
	private static final float[] identity = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
	private static final Vector3 right = new Vector3(), tmpForward = new Vector3(), tmpUp = new Vector3(),
	l_vez = new Vector3(), l_vex = new Vector3(), l_vey = new Vector3(), tmpVec = new Vector3();
	private static final float[] tmp = new float[16];
	private static final Quaternion quat = new Quaternion(), quat2 = new Quaternion();
	public final float[] val = new float[16];

	static {
		initialize();
	}
	
	private static native void initialize();
	
	public Matrix4() {
		idt();
	}

	public Matrix4(Matrix4 matrix) {
		set(matrix);
	}

	public Matrix4(float[] values) {
		set(values);
	}

	public Matrix4(Quaternion quaternion) {
		set(quaternion);
	}

	public Matrix4(Vector3 position, Quaternion rotation) {
		set(position, rotation);
	}
	
	public Matrix4(Vector3 position, Quaternion rotation, Vector3 scale) {
		set(position, rotation, scale);
	}
	
	public native Matrix4 set(float tX, float tY, float tZ, float qX, float qY, float qZ, float qW, float sX, float sY, float sZ);

	private static native void mul(float[] mata, float[] matb);

	public native void mulVec(float[] vec);

	public native void mulVec(float[] vecs, int offset, int numVecs, int stride);

	private native void lerp(float[] matb, float alpha);
	
	public native Matrix4 translate(float x, float y, float z);
	
	public native Matrix4 rotate(float qX, float qY, float qZ, float qW);
	
	public native Matrix4 scale(float x, float y, float z);
	
	public native void prj(float[] vec);

	public native void prj(float[] vecs, int offset, int numVecs, int stride);

	public native void rot(float[] vec);

	public native void rot(float[] vecs, int offset, int numVecs, int stride);
	
	//fovy in degree
	public native Matrix4 setToProjection(float near, float far, float fovy, float aspectRatio);

	public native Matrix4 setToProjection(float left, float right, float bottom, float top, float near, float far);

	public native Matrix4 setToOrtho(float left, float right, float bottom, float top, float near, float far);

	public native Matrix4 inv();

	public native float det();

	public Matrix4 set(Matrix4 matrix) {
		return this.set(matrix.val);
	}

	public Matrix4 set(float[] values) {
		System.arraycopy(values, 0, val, 0, 16);
		return this;
	}

	public Matrix4 set(Vector3 position) {
		return set(position.x, position.y, position.z);
	}
	
	public Matrix4 set(Quaternion quaternion) {
		return set(0, 0, 0, quaternion.x, quaternion.y, quaternion.z, quaternion.w);
	}

	public Matrix4 set(float positionX, float positionY, float positionZ) {
		return set(positionX, positionY, positionZ, 0, 0, 0, 1);
	}

	public Matrix4 set(Vector3 position, Quaternion orientation) {
		return set(position.x, position.y, position.z, orientation.x, orientation.y, orientation.z, orientation.w);
	}

	public Matrix4 set(Vector3 pos, Quaternion orient, Vector3 scl) {
		return set(pos.x, pos.y, pos.z, orient.x, orient.y, orient.z, orient.w, scl.x, scl.y, scl.z);
	}

	public Matrix4 set(float trnX, float trnY, float trnZ, float qX, float qY, float qZ, float qW) {
		return set(trnX, trnY, trnZ, qX, qY, qZ, qW, 1, 1, 1);
	}
	
	public Matrix4 set(Vector3 xAxis, Vector3 yAxis, Vector3 zAxis, Vector3 pos) {
		val[M00] = xAxis.x;
		val[M01] = xAxis.y;
		val[M02] = xAxis.z;
		val[M10] = yAxis.x;
		val[M11] = yAxis.y;
		val[M12] = yAxis.z;
		val[M20] = zAxis.x;
		val[M21] = zAxis.y;
		val[M22] = zAxis.z;
		val[M03] = pos.x;
		val[M13] = pos.y;
		val[M23] = pos.z;
		val[M30] = val[M31] = val[M32] = 0;
		val[M33] = 1;
		return this;
	}

	public Matrix4 cpy() {
		return new Matrix4(this);
	}

	public Matrix4 trn(Vector3 vector) {
		return trn(vector.x, vector.y, vector.z);
	}

	public Matrix4 trn(float x, float y, float z) {
		val[M03] += x;
		val[M13] += y;
		val[M23] += z;
		return this;
	}

	public Matrix4 scl(Vector3 scale) {
		return scl(scale.x, scale.y, scale.z);
	}

	public Matrix4 scl(float scale) {
		return scl(scale, scale, scale);
	}

	public Matrix4 scl(float x, float y, float z) {
		val[M00] *= x;
		val[M11] *= y;
		val[M22] *= z;
		return this;
	}

	public float[] getValues() {
		return val;
	}

	public Matrix4 mul(Matrix4 matrix) {
		mul(val, matrix.val);
		return this;
	}

	public Matrix4 mulLeft(Matrix4 matrix) {
		System.arraycopy(matrix.val, 0, tmp, 0, 16);
		mul(tmp, val);
		return set(tmp);
	}

	public Matrix4 tra() {
		tmp[M00] = val[M00];
		tmp[M01] = val[M10];
		tmp[M02] = val[M20];
		tmp[M03] = val[M30];
		tmp[M10] = val[M01];
		tmp[M11] = val[M11];
		tmp[M12] = val[M21];
		tmp[M13] = val[M31];
		tmp[M20] = val[M02];
		tmp[M21] = val[M12];
		tmp[M22] = val[M22];
		tmp[M23] = val[M32];
		tmp[M30] = val[M03];
		tmp[M31] = val[M13];
		tmp[M32] = val[M23];
		tmp[M33] = val[M33];
		return set(tmp);
	}

	public Matrix4 idt() {
		set(identity);
		return this;
	}

	public float det3x3() {
		return val[M00] * val[M11] * val[M22] + val[M01] * val[M12] * val[M20] + val[M02] * val[M10] * val[M21]
			- val[M00] * val[M12] * val[M21] - val[M01] * val[M10] * val[M22] - val[M02] * val[M11] * val[M20];
	}

	public Matrix4 setToOrtho2D(float x, float y, float width, float height) {
		return setToOrtho(x, x + width, y, y + height, 0, 1);
	}

	public Matrix4 setToOrtho2D(float x, float y, float width, float height, float near, float far) {
		return setToOrtho(x, x + width, y, y + height, near, far);
	}

	public Matrix4 setToRotation(Vector3 axis, float degrees) {
		if ((degrees % 360) == 0) {
			idt();
			return this;
		}
		return set(quat.set(axis, degrees));
	}

	public Matrix4 setToRotationRad(Vector3 axis, float radians) {
		if (radians == 0) {
			idt();
			return this;
		}
		return set(quat.setFromAxisRad(axis, radians));
	}

	public Matrix4 setToRotation(float axisX, float axisY, float axisZ, float degrees) {
		if ((degrees % 360) == 0) {
			idt();
			return this;
		}
		return set(quat.setFromAxis(axisX, axisY, axisZ, degrees));
	}

	public Matrix4 setToRotationRad(float axisX, float axisY, float axisZ, float radians) {
		if (radians == 0) {
			idt();
			return this;
		}
		return set(quat.setFromAxisRad(axisX, axisY, axisZ, radians));
	}

	public Matrix4 setToRotation(final Vector3 v1, final Vector3 v2) {
		return set(quat.setFromCross(v1, v2));
	}

	public Matrix4 setToRotation(float x1, float y1, float z1, float x2, float y2, float z2) {
		return set(quat.setFromCross(x1, y1, z1, x2, y2, z2));
	}

	public Matrix4 setFromEulerAngles(float yaw, float pitch, float roll) {
		quat.setEulerAngles(yaw, pitch, roll);
		return set(quat);
	}

	public Matrix4 setFromEulerAnglesRad(float yaw, float pitch, float roll) {
		quat.setEulerAnglesRad(yaw, pitch, roll);
		return set(quat);
	}

	public Matrix4 setToScaling(Vector3 vector) {
		return setToScaling(vector.x, vector.y, vector.z);
	}

	public Matrix4 setToScaling(float x, float y, float z) {
		return set(0,0,0, 0,0,0,1, x, y, z);
	}

	public Matrix4 setToLookAt(Vector3 direction, Vector3 up) {
		l_vez.set(direction).nor();
		l_vey.set(up).nor();
		l_vex.set(l_vez).crs(l_vey).nor();
		l_vey.set(l_vex).crs(l_vez).nor();
		set(l_vex, l_vey, l_vez.scl(-1), tmpVec.set(0,0,0));
		return this;
	}

	public Matrix4 setToLookAt(Vector3 position, Vector3 target, Vector3 up) {
		tmpVec.set(target).sub(position);
		setToLookAt(tmpVec, up);
		translate(tmpVec.set(position).scl(-1));
		return this;
	}

	public Matrix4 setToWorld(Vector3 position, Vector3 forward, Vector3 up) {
		tmpForward.set(forward).nor();
		right.set(tmpForward).crs(up).nor();
		tmpUp.set(right).crs(tmpForward).nor();
		this.set(right, tmpUp, tmpForward.scl(-1), position);
		return this;
	}

	@Override
	public String toString() {
		return "[" + val[M00] + "|" + val[M01] + "|" + val[M02] + "|" + val[M03] + "]\n" + "[" + val[M10] + "|"
			+ val[M11] + "|" + val[M12] + "|" + val[M13] + "]\n" + "[" + val[M20] + "|" + val[M21] + "|" + val[M22]
			+ "|" + val[M23] + "]\n" + "[" + val[M30] + "|" + val[M31] + "|" + val[M32] + "|" + val[M33] + "]\n";
	}

	public Matrix4 lerp(Matrix4 matrix, float alpha) {
		lerp(matrix.val, alpha);
		return this;
	}

	public Matrix4 avg(Matrix4 other, float w) {
		getScale(tmpVec);
		other.getScale(tmpForward);
		getRotation(quat);
		other.getRotation(quat2);
		getTranslation(tmpUp);
		other.getTranslation(right);
		setToScaling(tmpVec.scl(w).add(tmpForward.scl(1 - w)));
		rotate(quat.slerp(quat2, 1 - w));
		set(tmpUp.scl(w).add(right.scl(1 - w)));
		return this;
	}

	public Matrix4 avg(Matrix4[] t) {
		final float w = 1.0f / t.length;
		tmpVec.set(t[0].getScale(tmpUp).scl(w));
		quat.set(t[0].getRotation(quat2).exp(w));
		tmpForward.set(t[0].getTranslation(tmpUp).scl(w));
		for (int i = 1; i < t.length; i++) {
			tmpVec.add(t[i].getScale(tmpUp).scl(w));
			quat.mul(t[i].getRotation(quat2).exp(w));
			tmpForward.add(t[i].getTranslation(tmpUp).scl(w));
		}
		quat.nor();
		setToScaling(tmpVec);
		rotate(quat);
		set(tmpForward);
		return this;
	}

	public Matrix4 avg(Matrix4[] t, float[] w) {
		tmpVec.set(t[0].getScale(tmpUp).scl(w[0]));
		quat.set(t[0].getRotation(quat2).exp(w[0]));
		tmpForward.set(t[0].getTranslation(tmpUp).scl(w[0]));
		for (int i = 1; i < t.length; i++) {
			tmpVec.add(t[i].getScale(tmpUp).scl(w[i]));
			quat.mul(t[i].getRotation(quat2).exp(w[i]));
			tmpForward.add(t[i].getTranslation(tmpUp).scl(w[i]));
		}
		quat.nor();
		setToScaling(tmpVec);
		rotate(quat);
		set(tmpForward);
		return this;
	}

	public Matrix4 set(Matrix3 mat) {
		val[0] = mat.val[0];
		val[1] = mat.val[1];
		val[2] = mat.val[2];
		val[4] = mat.val[3];
		val[5] = mat.val[4];
		val[6] = mat.val[5];
		val[12] = mat.val[6];
		val[13] = mat.val[7];
		val[15] = mat.val[8];
		val[10] = 1;
		val[3] = val[7] = val[8] = 0;
		val[9] = val[11] = val[14] = 0;
		return this;
	}

	public Matrix4 set(Affine2 affine) {
		val[M00] = affine.m00;
		val[M10] = affine.m10;
		val[M01] = affine.m01;
		val[M11] = affine.m11;
		val[M03] = affine.m02;
		val[M13] = affine.m12;
		val[M20] = val[M30] = val[M21] = val[M31] = 0;
		val[M02] = val[M12] = val[M32] = val[M23] = 0;
		val[M22] = val[M33] = 1;
		return this;
	}

	public Matrix4 setAsAffine(Affine2 affine) {
		val[M00] = affine.m00;
		val[M10] = affine.m10;
		val[M01] = affine.m01;
		val[M11] = affine.m11;
		val[M03] = affine.m02;
		val[M13] = affine.m12;
		return this;
	}

	public Matrix4 setAsAffine(Matrix4 mat) {
		val[M00] = mat.val[M00];
		val[M10] = mat.val[M10];
		val[M01] = mat.val[M01];
		val[M11] = mat.val[M11];
		val[M03] = mat.val[M03];
		val[M13] = mat.val[M13];
		return this;
	}

	public Vector3 getTranslation(Vector3 position) {
		position.x = val[M03];
		position.y = val[M13];
		position.z = val[M23];
		return position;
	}

	public Quaternion getRotation(Quaternion rotation, boolean normalizeAxes) {
		return rotation.setFromMatrix(normalizeAxes, this);
	}

	public Quaternion getRotation(Quaternion rotation) {
		return rotation.setFromMatrix(this);
	}

	public float getScaleXSquared() {
		return val[Matrix4.M00] * val[Matrix4.M00] + val[Matrix4.M01] * val[Matrix4.M01]
			+ val[Matrix4.M02] * val[Matrix4.M02];
	}

	public float getScaleYSquared() {
		return val[Matrix4.M10] * val[Matrix4.M10] + val[Matrix4.M11] * val[Matrix4.M11]
			+ val[Matrix4.M12] * val[Matrix4.M12];
	}

	public float getScaleZSquared() {
		return val[Matrix4.M20] * val[Matrix4.M20] + val[Matrix4.M21] * val[Matrix4.M21]
			+ val[Matrix4.M22] * val[Matrix4.M22];
	}

	public float getScaleX() {
		return (MathUtils.isZero(val[Matrix4.M01]) && MathUtils.isZero(val[Matrix4.M02])) ? Math.abs(val[Matrix4.M00])
			: (float) Math.sqrt(getScaleXSquared());
	}

	public float getScaleY() {
		return (MathUtils.isZero(val[Matrix4.M10]) && MathUtils.isZero(val[Matrix4.M12])) ? Math.abs(val[Matrix4.M11])
			: (float) Math.sqrt(getScaleYSquared());
	}

	public float getScaleZ() {
		return (MathUtils.isZero(val[Matrix4.M20]) && MathUtils.isZero(val[Matrix4.M21])) ? Math.abs(val[Matrix4.M22])
			: (float) Math.sqrt(getScaleZSquared());
	}

	public Vector3 getScale(Vector3 scale) {
		return scale.set(getScaleX(), getScaleY(), getScaleZ());
	}

	public Matrix4 toNormalMatrix() {
		val[M03] = val[M13] = val[M23] = 0;
		inv();
		return tra();
	}

	public Matrix4 translate(Vector3 translation) {
		return translate(translation.x, translation.y, translation.z);
	}

	public Matrix4 rotate(Vector3 axis, float degrees) {
		degrees %= 360;
		if (degrees == 0)
			return this;
		quat.set(axis, degrees);
		return rotate(quat);
	}

	public Matrix4 rotateRad(Vector3 axis, float radians) {
		radians %= MathUtils.PI2;
		if (radians == 0)
			return this;
		quat.setFromAxisRad(axis, radians);
		return rotate(quat);
	}

	public Matrix4 rotate(Quaternion r) {
		return rotate(r.x, r.y, r.z, r.w);
	}

	public Matrix4 rotateTowardDirection(final Vector3 direction, final Vector3 up) {
		l_vez.set(direction).nor();
		l_vey.set(up).nor();
		l_vex.set(l_vez).crs(l_vey).nor();
		l_vey.set(l_vex).crs(l_vez).nor();
		tmp[M00] = l_vex.x;
		tmp[M10] = l_vex.y;
		tmp[M20] = l_vex.z;
		tmp[M01] = l_vey.x;
		tmp[M11] = l_vey.y;
		tmp[M21] = l_vey.z;
		tmp[M02] = -l_vez.x;
		tmp[M12] = -l_vez.y;
		tmp[M22] = -l_vez.z;
		tmp[M30] = tmp[M31] = tmp[M32] = tmp[M03] = tmp[M13] = tmp[M23] = 0f;
		tmp[M33] = 1f;
		mul(val, tmp);
		return this;
	}

	public Matrix4 rotateTowardTarget(final Vector3 target, final Vector3 up) {
		tmpVec.set(target.x - val[M03], target.y - val[M13], target.z - val[M23]);
		return rotateTowardDirection(tmpVec, up);
	}

	public Matrix4 scale(float scale) {
		return scale(scale, scale, scale);
	}
	
	public void extract4x3Matrix(float[] dst) {
		dst[0] = val[M00];
		dst[1] = val[M10];
		dst[2] = val[M20];
		dst[3] = val[M01];
		dst[4] = val[M11];
		dst[5] = val[M21];
		dst[6] = val[M02];
		dst[7] = val[M12];
		dst[8] = val[M22];
		dst[9] = val[M03];
		dst[10] = val[M13];
		dst[11] = val[M23];
	}

	public boolean hasRotationOrScaling() {
		return !(MathUtils.isEqual(val[M00], 1) && MathUtils.isEqual(val[M11], 1) && MathUtils.isEqual(val[M22], 1)
			&& MathUtils.isZero(val[M01]) && MathUtils.isZero(val[M02]) && MathUtils.isZero(val[M10])
			&& MathUtils.isZero(val[M12]) && MathUtils.isZero(val[M20]) && MathUtils.isZero(val[M21]));
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public boolean equalAsAfiine2(Affine2 affine) {
		if (val[M00] != affine.m00)
			return false;
		if (val[M10] != affine.m10)
			return false;
		if (val[M01] != affine.m01)
			return false;
		if (val[M11] != affine.m11)
			return false;
		if (val[M03] != affine.m02)
			return false;
		return val[M13] == affine.m12;
	}
}
