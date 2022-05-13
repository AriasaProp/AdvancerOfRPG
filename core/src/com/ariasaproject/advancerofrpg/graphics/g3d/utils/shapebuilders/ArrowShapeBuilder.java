package com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders;

import com.ariasaproject.advancerofrpg.graphics.g3d.utils.MeshPartBuilder;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Vector3;

public class ArrowShapeBuilder extends BaseShapeBuilder {
	public static void build(MeshPartBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2,
							 float capLength, float stemThickness, int divisions) {
		Vector3 begin = obtainV3().set(x1, y1, z1), end = obtainV3().set(x2, y2, z2);
		float length = begin.dst(end);
		float coneHeight = length * capLength;
		float coneDiameter = 2 * (float) (coneHeight * Math.sqrt(1f / 3));
		float stemLength = length - coneHeight;
		float stemDiameter = coneDiameter * stemThickness;
		Vector3 up = obtainV3().set(end).sub(begin).nor();
		Vector3 forward = obtainV3().set(up).crs(0, 0, 1);
		if (forward.isZero())
			forward.set(1, 0, 0);
		forward.crs(up).nor();
		Vector3 left = obtainV3().set(up).crs(forward).nor();
		Vector3 direction = obtainV3().set(end).sub(begin).nor();
		// Matrices
		Matrix4 userTransform = builder.getVertexTransform(obtainM4());
		Matrix4 transform = obtainM4();
		float[] val = transform.val;
		val[Matrix4.M00] = left.x;
		val[Matrix4.M01] = up.x;
		val[Matrix4.M02] = forward.x;
		val[Matrix4.M10] = left.y;
		val[Matrix4.M11] = up.y;
		val[Matrix4.M12] = forward.y;
		val[Matrix4.M20] = left.z;
		val[Matrix4.M21] = up.z;
		val[Matrix4.M22] = forward.z;
		Matrix4 temp = obtainM4();
		// Stem
		transform.set(obtainV3().set(direction).scl(stemLength / 2).add(x1, y1, z1));
		builder.setVertexTransform(temp.set(transform).mul(userTransform));
		CylinderShapeBuilder.build(builder, stemDiameter, stemLength, stemDiameter, divisions);
		// Cap
		transform.set(obtainV3().set(direction).scl(stemLength).add(x1, y1, z1));
		builder.setVertexTransform(temp.set(transform).mul(userTransform));
		ConeShapeBuilder.build(builder, coneDiameter, coneHeight, coneDiameter, divisions);
		builder.setVertexTransform(userTransform);
		freeAll();
	}
}
