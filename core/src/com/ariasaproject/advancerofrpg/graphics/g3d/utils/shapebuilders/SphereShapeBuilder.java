package com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders;

import com.ariasaproject.advancerofrpg.graphics.g3d.utils.MeshPartBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.utils.ShortArray;

public class SphereShapeBuilder extends BaseShapeBuilder {
	private final static ShortArray tmpIndices = new ShortArray();

	public static void build(MeshPartBuilder builder, float width, float height, float depth, int divisionsU, int divisionsV) {
		build(builder, width, height, depth, divisionsU, divisionsV, 0, 360, 0, 180);
	}

	public static void build(MeshPartBuilder builder, float width, float height, float depth, int divisionsU, int divisionsV, float angleUFrom, float angleUTo, float angleVFrom, float angleVTo) {
		final boolean closedVFrom = MathUtils.isEqual(angleVFrom, 0f);
		final boolean closedVTo = MathUtils.isEqual(angleVTo, 180f);
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float auo = MathUtils.degreesToRadians * angleUFrom;
		final float stepU = (MathUtils.degreesToRadians * (angleUTo - angleUFrom)) / divisionsU;
		final float avo = MathUtils.degreesToRadians * angleVFrom;
		final float stepV = (MathUtils.degreesToRadians * (angleVTo - angleVFrom)) / divisionsV;
		final float us = 1f / divisionsU;
		final float vs = 1f / divisionsV;
		float u = 0f;
		float v = 0f;
		float angleU = 0f;
		float angleV = 0f;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		curr1.hasUV = curr1.hasPosition = curr1.hasNormal = true;
		final int s = divisionsU + 3;
		tmpIndices.clear();
		tmpIndices.ensureCapacity(divisionsU * 2);
		tmpIndices.size = s;
		int tempOffset = 0;
		builder.ensureVertices((divisionsV + 1) * (divisionsU + 1));
		builder.ensureRectangleIndices(divisionsU);
		for (int iv = 0; iv <= divisionsV; iv++) {
			angleV = avo + stepV * iv;
			v = vs * iv;
			final float t = MathUtils.sin(angleV);
			final float h = MathUtils.cos(angleV) * hh;
			for (int iu = 0; iu <= divisionsU; iu++) {
				angleU = auo + stepU * iu;
				if (iv == 0 && closedVFrom || iv == divisionsV && closedVTo) {
					u = 1f - us * (iu - .5f);
				} else {
					u = 1f - us * iu;
				}
				curr1.position.set(MathUtils.cos(angleU) * hw * t, h, MathUtils.sin(angleU) * hd * t);
				curr1.normal.set(curr1.position).nor();
				curr1.uv.set(u, v);
				tmpIndices.set(tempOffset, builder.vertex(curr1));
				final int o = tempOffset + s;
				if ((iv > 0) && (iu > 0)) { // FIXME don't duplicate lines and points
					if (iv == 1 && closedVFrom) {
						builder.triangle(tmpIndices.get(tempOffset), tmpIndices.get((o - 1) % s), tmpIndices.get((o - (divisionsU + 1)) % s));
					} else if (iv == divisionsV && closedVTo) {
						builder.triangle(tmpIndices.get(tempOffset), tmpIndices.get((o - (divisionsU + 2)) % s), tmpIndices.get((o - (divisionsU + 1)) % s));
					} else {
						builder.rect(tmpIndices.get(tempOffset), tmpIndices.get((o - 1) % s), tmpIndices.get((o - (divisionsU + 2)) % s), tmpIndices.get((o - (divisionsU + 1)) % s));
					}
				}
				tempOffset = (tempOffset + 1) % tmpIndices.size;
			}
		}
	}

	// octahedron sphere lvl 5
	static final ShortArray ind = new ShortArray();

	public static void build(MeshPartBuilder builder, final float width, final float height, final float depth, final int subdivisions) {
		ind.clear();

		vertTmp0.set(null);
		vertTmp0.hasPosition = vertTmp0.hasColor = vertTmp0.hasNormal = vertTmp0.hasUV = true;
		vertTmp0.color.set(1, 1, 1, 1);
		vertTmp1.set(vertTmp0);
		vertTmp2.set(vertTmp0);
		vertTmp3.set(vertTmp0);

		final float hW = width * 0.5f;
		final float hH = height * 0.5f;
		final float hD = depth * 0.5f;

		for (int d = 0; d < 4; d++) {
			// float dirRad = MathUtils.PI / 2 * d;
			float step = MathUtils.PI / 2 / (subdivisions + 1);
			for (float v = MathUtils.PI / 2 * d; v < MathUtils.PI / 2 * (d + 1); v += step) {
				vertTmp0.position.set(MathUtils.sin(v) * hW, 0, MathUtils.cos(v) * hD);
				vertTmp0.normal.set(vertTmp0.position).nor();
				vertTmp0.uv.set(d / 4, 0.5f + 0.5f * vertTmp0.normal.y);

				vertTmp1.position.set(MathUtils.sin(v) * hW, 0, MathUtils.cos(v) * hD);
				vertTmp1.normal.set(vertTmp1.position).nor();
				vertTmp1.uv.set(d / 4, 0.5f + 0.5f * vertTmp1.normal.y);

				vertTmp2.position.set(MathUtils.sin(v + step) * hW, 0, MathUtils.cos(v + step) * hD);
				vertTmp2.normal.set(vertTmp2.position).nor();
				vertTmp2.uv.set(d / 4, 0.5f + 0.5f * vertTmp2.normal.y);

				builder.triangle(vertTmp0, vertTmp1, vertTmp2);
			}
		}
	}

}
