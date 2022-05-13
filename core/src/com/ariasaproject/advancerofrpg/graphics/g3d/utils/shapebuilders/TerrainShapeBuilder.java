package com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders;

import java.util.Arrays;
import java.util.Random;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.MeshPartBuilder;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Array;

public class TerrainShapeBuilder extends BaseShapeBuilder {
	private static final Random random = new Random();

	// flat rect for terrain
	public static void buildFlat(MeshPartBuilder builder, Vector3 center, float width, float wide) {
		float hWidth = width / 2f, hWide = wide / 2f;
		vertTmp0.hasUV = vertTmp0.hasPosition = vertTmp0.hasColor = vertTmp0.hasNormal = true;
		vertTmp0.setCol(Color.WHITE);
		vertTmp0.setNor(0, 1, 0);
		vertTmp0.position.set(center).add(-hWidth, 0, -hWide);
		vertTmp0.uv.set(0, 0);
		vertTmp1.set(vertTmp0);
		vertTmp1.position.set(center).add(-hWidth, 0, hWide);
		vertTmp1.uv.set(0, 1);
		vertTmp2.set(vertTmp0);
		vertTmp2.position.set(center).add(hWidth, 0, hWide);
		vertTmp2.uv.set(1, 1);
		vertTmp3.set(vertTmp0);
		vertTmp3.position.set(center).add(hWidth, 0, -hWide);
		vertTmp3.uv.set(1, 0);
		builder.rect(vertTmp0, vertTmp1, vertTmp2, vertTmp3);
		freeAll();
	}

	// noise generator with random
	private static float[][] noiseHeightmap(int divU, int divV, float roughness) {
		float[][] heightmap = new float[divU][divV];
		heightmap[0][0] = random.nextFloat() - 0.5f;
		heightmap[0][divV - 1] = random.nextFloat() - 0.5f;
		heightmap[divU - 1][0] = random.nextFloat() - 0.5f;
		heightmap[divU - 1][divV - 1] = random.nextFloat() - 0.5f;
		Array<int[]> variy = new Array<int[]>();
		variy.add(new int[] { 0, 0, divU - 1, divV - 1 });
		while (!variy.isEmpty()) {
			int[] cV = variy.removeIndex(0);
			int xl = cV[0], yl = cV[1], xh = cV[2], yh = cV[3];
			int xm = (xl + xh) / 2;
			int ym = (yl + yh) / 2;
			if ((xl == xm) && (yl == ym))
				continue;
			heightmap[xm][yl] = 0.5f * (heightmap[xl][yl] + heightmap[xh][yl]);
			heightmap[xm][yh] = 0.5f * (heightmap[xl][yh] + heightmap[xh][yh]);
			heightmap[xl][ym] = 0.5f * (heightmap[xl][yl] + heightmap[xl][yh]);
			heightmap[xh][ym] = 0.5f * (heightmap[xh][yl] + heightmap[xh][yh]);
			heightmap[xm][ym] = (0.5f * (heightmap[xm][yl] + heightmap[xm][yh]))
					+ (roughness * (float) (random.nextGaussian() * ((xh + yh) - (xl + yl))));
			heightmap[xm][yl] += roughness * (float) (random.nextGaussian() * (xh - xl));
			heightmap[xm][yh] += roughness * (float) (random.nextGaussian() * (xh - xl));
			heightmap[xl][ym] += roughness * (float) (random.nextGaussian() * (yh - yl));
			heightmap[xh][ym] += roughness * (float) (random.nextGaussian() * (yh - yl));
			variy.add(new int[] { xl, yl, xm, ym });
			variy.add(new int[] { xm, yl, xh, ym });
			variy.add(new int[] { xl, ym, xm, yh });
			variy.add(new int[] { xm, ym, xh, yh });
		}
		return heightmap;
	}

	// noise terrain smooth normal
	public static void buildNoise(MeshPartBuilder builder, Vector3 center, float gSize, int divU, int divV,
			float roughness) {
		build(builder, center, gSize, divU, divV, noiseHeightmap(divU, divV, roughness));
	}

	// terain with hightmap and smooth
	public static void build(MeshPartBuilder builder, Vector3 center, float gSize, int divU, int divV,
			float[][] heightmap) {
		tmpV0.set(center).sub(gSize * divU / 2f, 0, gSize * divV / 2f);
		vertTmp0.hasUV = vertTmp0.hasPosition = vertTmp0.hasColor = vertTmp0.hasNormal = true;
		vertTmp0.setCol(Color.WHITE);
		final short[] oldRaw = new short[divU], newRaw = new short[divU];
		for (int z = 0; z < divV; z++) {
			for (int x = 0; x < divU; x++) {
				float LR = heightmap[z][Math.max(x - 1, 0)] - heightmap[z][Math.min(x + 1, divU - 1)];
				float BT = heightmap[Math.max(z - 1, 0)][x] - heightmap[Math.min(z + 1, divV - 1)][x];
				vertTmp0.position.set(tmpV0).add(x * gSize, heightmap[z][x], z * gSize);
				vertTmp0.normal.set(LR, 2f, BT).nor();
				vertTmp0.uv.set((float) x / (float) (divU - 1), (float) z / (float) (divV - 1));
				newRaw[x] = builder.vertex(vertTmp0);
				if (z > 0 && x > 0) {
					builder.rect(oldRaw[x - 1], newRaw[x - 1], newRaw[x], oldRaw[x]);
				}
			}
			System.arraycopy(newRaw, 0, oldRaw, 0, divU);
			Arrays.fill(newRaw, (short) 0);
		}
		freeAll();
	}

	// noise terrain fract normal
	public static void buildNoiseN(MeshPartBuilder builder, Vector3 center, float gSize, int divU, int divV,
			float roughness) {
		buildN(builder, center, gSize, divU, divV, noiseHeightmap(divU, divV, roughness));
	}

	// noise terrain fract normal
	public static void buildN(MeshPartBuilder builder, Vector3 center, float gSize, int divU, int divV,
			float[][] heightmap) {
		tmpV0.set(center).sub(gSize * divU / 2f, 0, gSize * divV / 2f);
		vertTmp0.set(null, null, null, null);
		vertTmp0.hasPosition = vertTmp0.hasNormal = vertTmp0.hasColor = vertTmp0.hasUV = true;
		vertTmp0.color.set(Color.WHITE);
		vertTmp1.set(vertTmp0);
		vertTmp2.set(vertTmp0);
		final float X = divU - 1, Z = divV - 1;
		for (int z = 0; z < Z; z += 1f) {
			for (int x = 0; x < X; x += 1f) {
				// corner 00
				vertTmp0.position.set(tmpV0).add(x * gSize, heightmap[z][x], z * gSize);
				vertTmp0.setUV(x / X, z / Z);
				// corner 10
				vertTmp1.position.set(tmpV0).add(x * gSize, heightmap[z + 1][x], (z + 1) * gSize);
				vertTmp1.setUV(x / X, (z + 1) / Z);
				// corner 11
				vertTmp2.position.set(tmpV0).add((x + 1) * gSize, heightmap[z + 1][x + 1], (z + 1) * gSize);
				vertTmp2.setUV((x + 1) / X, (z + 1) / Z);
				// normal 1
				tmpV2.set(vertTmp2.position).sub(vertTmp0.position);// u
				tmpV3.set(vertTmp1.position).sub(vertTmp0.position);// v
				tmpV1.set(tmpV2.y * tmpV3.z - tmpV2.z * tmpV2.y, tmpV2.z * tmpV3.x - tmpV2.x * tmpV3.z,
						tmpV2.x * tmpV3.y - tmpV2.y * tmpV3.x);
				tmpV1.nor();
				vertTmp0.setNor(tmpV1);
				vertTmp1.setNor(tmpV1);
				vertTmp2.setNor(tmpV1);
				builder.triangle(vertTmp0, vertTmp1, vertTmp2);
				// corner 01
				vertTmp1.position.set(tmpV0).add((x + 1) * gSize, heightmap[z][x + 1], z * gSize);
				vertTmp1.setUV((x + 1) / X, z / Z);
				// normal 2
				tmpV2.set(vertTmp1.position).sub(vertTmp0.position);// u
				tmpV3.set(vertTmp2.position).sub(vertTmp0.position);// v
				tmpV1.set(tmpV2.y * tmpV3.z - tmpV2.z * tmpV2.y, tmpV2.z * tmpV3.x - tmpV2.x * tmpV3.z,
						tmpV2.x * tmpV3.y - tmpV2.y * tmpV3.x);
				tmpV1.nor();
				vertTmp0.setNor(tmpV1);
				vertTmp1.setNor(tmpV1);
				vertTmp2.setNor(tmpV1);
				builder.triangle(vertTmp2, vertTmp1, vertTmp0);
			}
		}
		freeAll();
	}
}
