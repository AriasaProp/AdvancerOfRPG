package com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders;

import com.ariasaproject.advancerofrpg.graphics.g3d.utils.MeshPartBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.utils.ShortArray;

public class CapsuleShapeBuilder extends BaseShapeBuilder {
	private final static float PI = (float) Math.PI;
	private final static ShortArray tmpIndices = new ShortArray();

	public static void build(MeshPartBuilder builder, float radius, float height, int divisions) {
		final float d = 2f * radius;
		if (height < d)
			throw new RuntimeException("Height must be at least twice the radius");
		final float hh = height * 0.5f - radius;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		curr1.hasUV = curr1.hasPosition = curr1.hasNormal = true;
		VertexInfo curr2 = vertTmp4.set(null, null, null, null);
		curr2.hasUV = curr2.hasPosition = curr2.hasNormal = true;

		// capsule top
		final float stepU = (PI * 2) / divisions;
		final float stepV = (PI * 0.5f) / divisions;
		final int s = divisions + 3;
		float u = 0f;
		float v = 0f;
		float angleU = 0f;
		float angleV = 0f;
		int tempOffset = 0;
		tmpIndices.clear();
		tmpIndices.ensureCapacity(divisions * 2);
		tmpIndices.size = s;
		builder.ensureVertices((divisions + 1) * (divisions + 1));
		builder.ensureRectangleIndices(divisions);
		for (float iv = 0; iv <= divisions; iv += 1f) {
			angleV = stepV * iv;
			v = iv / divisions;
			final float t = MathUtils.sin(angleV);
			final float h = MathUtils.cos(angleV) * radius;
			for (float iu = 0; iu <= divisions; iu += 1f) {
				angleU = stepU * iu;
				if (iv == 0) {
					u = 1f - (iu - .5f) / divisions;
				} else {
					u = 1f - iu / divisions;
				}
				curr1.normal.set(MathUtils.cos(angleU) * radius * t, h, MathUtils.sin(angleU) * radius * t).nor();
				curr1.position.set(MathUtils.cos(angleU) * radius * t, h + hh, MathUtils.sin(angleU) * radius * t);
				curr1.uv.set(u, v);
				tmpIndices.set(tempOffset, builder.vertex(curr1));
				final int o = tempOffset + s;
				if (iv > 0 && iu > 0) { // FIXME don't duplicate lines and points
					if (iv == 1) {
						builder.triangle(tmpIndices.get(tempOffset), tmpIndices.get((o - 1) % s),
								tmpIndices.get((o - (divisions + 1)) % s));
					} else {
						builder.rect(tmpIndices.get(tempOffset), tmpIndices.get((o - 1) % s),
								tmpIndices.get((o - (divisions + 2)) % s), tmpIndices.get((o - (divisions + 1)) % s));
					}
				}
				tempOffset = (tempOffset + 1) % tmpIndices.size;
			}
		}

		// capsule body cylinder
		short i1, i2, i3 = 0, i4 = 0;
		builder.ensureVertices(2 * (divisions + 1));
		builder.ensureRectangleIndices(divisions);
		for (float i = 0; i <= divisions; i += 1f) {
			float angle = (PI * 2 * i) / divisions;
			u = 1f - i / divisions;
			curr1.position.set(MathUtils.cos(angle) * radius, 0f, MathUtils.sin(angle) * radius);
			curr1.normal.set(curr1.position).nor();
			curr1.position.y = -hh;
			curr1.uv.set(u, 1);
			curr2.position.set(curr1.position);
			curr2.normal.set(curr1.normal);
			curr2.position.y = hh;
			curr2.uv.set(u, 0);
			i2 = builder.vertex(curr1);
			i1 = builder.vertex(curr2);
			if (i != 0)
				builder.rect(i3, i1, i2, i4); // FIXME don't duplicate lines and points
			i4 = i2;
			i3 = i1;
		}

		// capsule bottom
		u = 0f;
		v = 0f;
		angleU = 0f;
		angleV = 0f;
		tempOffset = 0;
		tmpIndices.clear();
		tmpIndices.ensureCapacity(divisions * 2);
		tmpIndices.size = s;
		builder.ensureVertices((divisions + 1) * (divisions + 1));
		builder.ensureRectangleIndices(divisions);
		final float avo = PI * 0.5f;
		for (float iv = 0; iv <= divisions; iv += 1f) {
			angleV = avo + stepV * iv;
			v = iv / divisions;
			final float t = MathUtils.sin(angleV);
			final float h = MathUtils.cos(angleV) * radius;
			for (float iu = 0; iu <= divisions; iu += 1f) {
				angleU = stepU * iu;
				if (iv == divisions) {
					u = 1f - (iu - .5f) / divisions;
				} else {
					u = 1f - iu / divisions;
				}
				curr1.normal.set(MathUtils.cos(angleU) * radius * t, h, MathUtils.sin(angleU) * radius * t).nor();
				curr1.position.set(MathUtils.cos(angleU) * radius * t, h - hh, MathUtils.sin(angleU) * radius * t);
				curr1.uv.set(u, v);
				tmpIndices.set(tempOffset, builder.vertex(curr1));
				final int o = tempOffset + s;
				if ((iv > 0) && (iu > 0)) { // FIXME don't duplicate lines and points
					if (iv == divisions) {
						builder.triangle(tmpIndices.get(tempOffset), tmpIndices.get((o - (divisions + 2)) % s),
								tmpIndices.get((o - (divisions + 1)) % s));
					} else {
						builder.rect(tmpIndices.get(tempOffset), tmpIndices.get((o - 1) % s),
								tmpIndices.get((o - (divisions + 2)) % s), tmpIndices.get((o - (divisions + 1)) % s));
					}
				}
				tempOffset = (tempOffset + 1) % tmpIndices.size;
			}

		}
	}

	public static void buildN(MeshPartBuilder builder, float radius, float height, int divisions) {
		final float d = 2f * radius;
		if (height < d)
			throw new RuntimeException("Height must be at least twice the radius");

		// top
		for (float i = 0; i <= divisions; i += 1f) {

			for (float j = 0; j <= (i + 1) * 4; j += 1f) {

			}
		}
		// center
		for (float i = 0; i <= divisions; i += 1f) {

		}
		// bottom
		for (float i = 0; i <= divisions; i += 1f) {

		}
	}
}
