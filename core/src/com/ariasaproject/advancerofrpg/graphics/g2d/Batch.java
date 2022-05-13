package com.ariasaproject.advancerofrpg.graphics.g2d;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.Mesh;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.graphics.VertexAttribute;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.math.Affine2;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public class Batch implements Disposable {
	public static final int X1 = 0;
	public static final int Y1 = 1;
	public static final int C1 = 2;
	public static final int U1 = 3;
	public static final int V1 = 4;
	public static final int X2 = 5;
	public static final int Y2 = 6;
	public static final int C2 = 7;
	public static final int U2 = 8;
	public static final int V2 = 9;
	public static final int X3 = 10;
	public static final int Y3 = 11;
	public static final int C3 = 12;
	public static final int U3 = 13;
	public static final int V3 = 14;
	public static final int X4 = 15;
	public static final int Y4 = 16;
	public static final int C4 = 17;
	public static final int U4 = 18;
	public static final int V4 = 19;

	private final ShaderProgram shader;
	private final float[] vertices;
	private final Mesh mesh;
	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Matrix4 combinedMatrix = new Matrix4();
	private final Matrix4 virtualMatrix = new Matrix4();
	private final Affine2 adjustAffine = new Affine2();
	private final Color color = new Color(1, 1, 1, 1);
	int idx = 0;
	Texture lastTexture = null;
	boolean drawing = false;
	float colorPacked = Color.WHITE_FLOAT_BITS;
	private boolean adjustNeeded;
	private boolean haveIdentityRealMatrix = true;
	private int blendSrcFunc = TGF.GL_SRC_ALPHA;
	private int blendDstFunc = TGF.GL_ONE_MINUS_SRC_ALPHA;

	public Batch() {
		this(500);
	}
	public Batch(int size) {
		// 32767 is max vertex index, so 32767 / 4 vertices per sprite = 8191 sprites
		// max.
		if (size > 8191)
			throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);
		mesh = new Mesh(false, true, size * 5, size * 6,
						new VertexAttribute(Usage.Position, 2, "a_position"),
						new VertexAttribute(Usage.ColorPacked, 4, "a_color"),
						new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		projectionMatrix.setToOrtho2D(0, 0, GraphFunc.app.getGraphics().getWidth(),
									  GraphFunc.app.getGraphics().getHeight());
		vertices = new float[size * Sprite.SPRITE_SIZE];
		int len = size * 6;
		short[] indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i] = j;
			indices[i + 1] = (short) (j + 1);
			indices[i + 2] = (short) (j + 2);
			indices[i + 3] = (short) (j + 2);
			indices[i + 4] = (short) (j + 3);
			indices[i + 5] = j;
		}
		mesh.setIndices(indices);
		shader = new ShaderProgram(GraphFunc.app.getFiles().internal("shader/batch.shaderprogram"));
	}

	public void begin() {
		if (drawing)
			throw new IllegalStateException("Spriteend must be called before begin.");
		GraphFunc.tgf.setDepthMask(false);
		shader.bind();
		setupMatrices();
		drawing = true;
	}

	public void end() {
		if (!drawing)
			throw new IllegalStateException("Spritebegin must be called before end.");
		if (idx > 0)
			flush();
		lastTexture = null;
		drawing = false;
		TGF tgf = GraphFunc.tgf;
		tgf.setDepthMask(true);
		tgf.capabilitySwitch(false, TGF.GL_BLEND);
	}

	public void setColor(float r, float g, float b, float a) {
		color.set(r, g, b, a);
		colorPacked = color.toFloatBits();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color tint) {
		color.set(tint);
		colorPacked = tint.toFloatBits();
	}

	public float getPackedColor() {
		return colorPacked;
	}

	public void setPackedColor(float packedColor) {
		Color.abgr8888ToColor(color, packedColor);
		this.colorPacked = packedColor;
	}

	public void draw(Texture texture, float x, float y) {
		draw(texture, x, y, texture.getWidth(), texture.getHeight());
	}

	public void draw(Texture texture, float x, float y, float width, float height) {
		draw(texture, x, y, width, height, 0, 1, 1, 0);
	}

	public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2,
					 float v2) {
		if (!drawing)
			throw new IllegalStateException("Spritebegin must be called before draw.");
		float[] vertex = vertices;
		if (!texture.equals(lastTexture))
			switchTexture(texture);
		else if (idx == vertex.length)
			flush();
		final float x2 = x + width;
		final float y2 = y + height;
		if (adjustNeeded) {
			Affine2 t = adjustAffine;
			vertex[idx + X1] = t.m00 * x + t.m01 * y + t.m02;
			vertex[idx + X2] = t.m00 * x + t.m01 * y2 + t.m02;
			vertex[idx + X3] = t.m00 * x2 + t.m01 * y2 + t.m02;
			vertex[idx + X4] = t.m00 * x2 + t.m01 * y + t.m02;
			vertex[idx + Y1] = t.m10 * x + t.m11 * y + t.m12;
			vertex[idx + Y2] = t.m10 * x + t.m11 * y2 + t.m12;
			vertex[idx + Y3] = t.m10 * x2 + t.m11 * y2 + t.m12;
			vertex[idx + Y4] = t.m10 * x2 + t.m11 * y + t.m12;
		} else {
			vertex[idx + X1] = x;
			vertex[idx + X2] = x;
			vertex[idx + X3] = x2;
			vertex[idx + X4] = x2;
			vertex[idx + Y1] = y;
			vertex[idx + Y2] = y2;
			vertex[idx + Y3] = y2;
			vertex[idx + Y4] = y;
		}
		vertex[idx + C1] = colorPacked;
		vertex[idx + C2] = colorPacked;
		vertex[idx + C3] = colorPacked;
		vertex[idx + C4] = colorPacked;
		vertex[idx + U1] = u;
		vertex[idx + U2] = u;
		vertex[idx + U3] = u2;
		vertex[idx + U4] = u2;
		vertex[idx + V1] = v;
		vertex[idx + V2] = v;
		vertex[idx + V3] = v2;
		vertex[idx + V4] = v2;
		idx += 20;
	}

	public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
		if (!drawing)
			throw new IllegalStateException("Spritebegin must be called before draw.");
		if (count % 20 != 0)
			throw new RuntimeException("invalid vertex count");
		float[] vertex = vertices;
		int verticesLength = vertex.length;
		int remainingVertices = verticesLength;
		if (!texture.equals(lastTexture))
			switchTexture(texture);
		else {
			remainingVertices -= idx;
			if (remainingVertices == 0) {
				flush();
				remainingVertices = verticesLength;
			}
		}
		if (adjustNeeded) {
			Affine2 t = adjustAffine;
			int copyCount = Math.min(vertex.length - idx, count);
			while (count > 0) {
				count -= copyCount;
				while (copyCount > 0) {
					float x = spriteVertices[offset];
					float y = spriteVertices[offset + 1];
					vertex[idx] = t.m00 * x + t.m01 * y + t.m02; // x
					vertex[idx + 1] = t.m10 * x + t.m11 * y + t.m12; // y
					vertex[idx + 2] = spriteVertices[offset + 2]; // color
					vertex[idx + 3] = spriteVertices[offset + 3]; // u
					vertex[idx + 4] = spriteVertices[offset + 4]; // v
					idx += Sprite.VERTEX_SIZE;
					offset += Sprite.VERTEX_SIZE;
					copyCount -= Sprite.VERTEX_SIZE;
				}
				if (count > 0) {
					flush();
					copyCount = Math.min(vertex.length, count);
				}
			}
		} else {
			int copyCount = Math.min(remainingVertices, count);
			System.arraycopy(spriteVertices, offset, vertex, idx, copyCount);
			idx += copyCount;
			count -= copyCount;
			while (count > 0) {
				offset += copyCount;
				flush();
				copyCount = Math.min(verticesLength, count);
				System.arraycopy(spriteVertices, offset, vertex, 0, copyCount);
				idx += copyCount;
				count -= copyCount;
			}
		}
	}

	public void draw(TextureRegion region, float x, float y) {
		draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());
	}

	public void draw(TextureRegion region, float x, float y, float width, float height) {
		draw(region, x, y, 0, 0, width, height, 1, 1, 0);
	}

	public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
		if (!drawing)
			throw new IllegalStateException("Spritebegin must be called before draw.");
		float[] vertex = vertices;
		if (!region.texture.equals(lastTexture))
			switchTexture(region.texture);
		else if (idx == vertex.length)
			flush();
		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;
		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}
		// construct corner points, start from top left and go counter clockwise
		float x1, y1, x2, y2, x3, y3, x4, y4;
		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);
			x1 = cos * fx - sin * fy;
			y1 = sin * fx + cos * fy;
			x2 = cos * fx - sin * fy2;
			y2 = sin * fx + cos * fy2;
			x3 = cos * fx2 - sin * fy2;
			y3 = sin * fx2 + cos * fy2;
			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = fx;
			y1 = fy;
			x2 = fx;
			y2 = fy2;
			x3 = fx2;
			y3 = fy2;
			x4 = fx2;
			y4 = fy;
		}
		x1 += worldOriginX;
		y1 += worldOriginY;
		x2 += worldOriginX;
		y2 += worldOriginY;
		x3 += worldOriginX;
		y3 += worldOriginY;
		x4 += worldOriginX;
		y4 += worldOriginY;
		if (adjustNeeded) {
			Affine2 t = adjustAffine;
			vertex[idx + X1] = t.m00 * x1 + t.m01 * y1 + t.m02;
			vertex[idx + X2] = t.m00 * x2 + t.m01 * y2 + t.m02;
			vertex[idx + X3] = t.m00 * x3 + t.m01 * y3 + t.m02;
			vertex[idx + X4] = t.m00 * x4 + t.m01 * y4 + t.m02;
			vertex[idx + Y1] = t.m10 * x1 + t.m11 * y1 + t.m12;
			vertex[idx + Y2] = t.m10 * x2 + t.m11 * y2 + t.m12;
			vertex[idx + Y3] = t.m10 * x3 + t.m11 * y3 + t.m12;
			vertex[idx + Y4] = t.m10 * x4 + t.m11 * y4 + t.m12;
		} else {
			vertex[idx + X1] = x1;
			vertex[idx + X2] = x2;
			vertex[idx + X3] = x3;
			vertex[idx + X4] = x4;
			vertex[idx + Y1] = y1;
			vertex[idx + Y2] = y2;
			vertex[idx + Y3] = y3;
			vertex[idx + Y4] = y4;
		}
		vertex[idx + C1] = colorPacked;
		vertex[idx + C2] = colorPacked;
		vertex[idx + C3] = colorPacked;
		vertex[idx + C4] = colorPacked;

		vertex[idx + U1] = region.u;
		vertex[idx + U2] = region.u2;
		vertex[idx + U3] = region.u2;
		vertex[idx + U4] = region.u;
		vertex[idx + V1] = region.v;
		vertex[idx + V2] = region.v;
		vertex[idx + V3] = region.v2;
		vertex[idx + V4] = region.v2;
		
		idx += 20;
	}

	public void draw(TextureRegion region, float width, float height, Affine2 transform) {
		if (!drawing)
			throw new IllegalStateException("Spritebegin must be called before draw.");
		if (!region.texture.equals(lastTexture))
			switchTexture(region.texture);
		else if (idx == vertices.length)
			flush();
		// construct corner points
		float x1 = transform.m02;
		float y1 = transform.m12;
		float x2 = transform.m01 * height + transform.m02;
		float y2 = transform.m11 * height + transform.m12;
		float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
		float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
		float x4 = transform.m00 * width + transform.m02;
		float y4 = transform.m10 * width + transform.m12;
		if (adjustNeeded) {
			Affine2 t = adjustAffine;
			vertices[idx + X1] = t.m00 * x1 + t.m01 * y1 + t.m02;
			vertices[idx + X2] = t.m00 * x2 + t.m01 * y2 + t.m02;
			vertices[idx + X3] = t.m00 * x3 + t.m01 * y3 + t.m02;
			vertices[idx + X4] = t.m00 * x4 + t.m01 * y4 + t.m02;
			vertices[idx + Y1] = t.m10 * x1 + t.m11 * y1 + t.m12;
			vertices[idx + Y2] = t.m10 * x2 + t.m11 * y2 + t.m12;
			vertices[idx + Y3] = t.m10 * x3 + t.m11 * y3 + t.m12;
			vertices[idx + Y4] = t.m10 * x4 + t.m11 * y4 + t.m12;
		} else {
			vertices[idx + X1] = x1;
			vertices[idx + X2] = x2;
			vertices[idx + X3] = x3;
			vertices[idx + X4] = x4;
			vertices[idx + Y1] = y1;
			vertices[idx + Y2] = y2;
			vertices[idx + Y3] = y3;
			vertices[idx + Y4] = y4;
		}
		vertices[idx + C1] = colorPacked;
		vertices[idx + C2] = colorPacked;
		vertices[idx + C3] = colorPacked;
		vertices[idx + C4] = colorPacked;
		vertices[idx + U1] = region.u;
		vertices[idx + U2] = region.u;
		vertices[idx + U3] = region.u2;
		vertices[idx + U4] = region.u2;
		vertices[idx + V1] = region.v2;
		vertices[idx + V2] = region.v2;
		vertices[idx + V3] = region.v;
		vertices[idx + V4] = region.v;
		idx += 20;
	}
	public void flush() {
		if (idx == 0)
			return;
		int spritesInBatch = idx / 20;
		int count = spritesInBatch * 6;
		lastTexture.bind();
		mesh.setVertices(vertices, 0, idx);
		mesh.getIndicesBuffer().position(0);
		mesh.getIndicesBuffer().limit(count);
		TGF tgf = GraphFunc.tgf;
		tgf.setBlending(true, blendSrcFunc, blendDstFunc);
		mesh.render(shader, TGF.GL_TRIANGLES, 0, count);
		idx = 0;
		if (adjustNeeded) {
			// vertices flushed, safe now to replace matrix
			haveIdentityRealMatrix = new Affine2(virtualMatrix).isIdt();
			if (!haveIdentityRealMatrix && virtualMatrix.det() == 0)
				throw new RuntimeException("Transform matrix is singular, can't sync");
			adjustNeeded = false;
			setTransformMatrix(virtualMatrix);
		}
	}

	public void setBlendFunction(int srcFunc, int dstFunc) {
		if (blendSrcFunc == srcFunc && blendDstFunc == dstFunc)
			return;
		flush();
		blendSrcFunc = srcFunc;
		blendDstFunc = dstFunc;
	}

	@Override
	public void dispose() {
		mesh.dispose();
		shader.dispose();
	}

	public Matrix4 getProjectionMatrix() {
		return projectionMatrix;
	}

	public void setProjectionMatrix(Matrix4 projection) {
		if (drawing)
			flush();
		projectionMatrix.set(projection);
		if (drawing)
			setupMatrices();
	}

	public Matrix4 getTransformMatrix() {
		return (adjustNeeded ? virtualMatrix : transformMatrix);
	}

	public void setTransformMatrix(Affine2 transform) {
		Matrix4 realMatrix = transformMatrix;
		if (realMatrix.equalAsAfiine2(transform)) {
			adjustNeeded = false;
		} else {
			if (drawing) {
				virtualMatrix.setAsAffine(transform);
				adjustNeeded = true;
				if (haveIdentityRealMatrix) {
					adjustAffine.set(transform);
				} else {
					adjustAffine.set(realMatrix).inv().mul(transform);
				}
			} else {
				realMatrix.setAsAffine(transform);
				haveIdentityRealMatrix = new Affine2(realMatrix).isIdt();
			}
		}
	}

	public void setTransformMatrix(Matrix4 transform) {
		setTransformMatrix(new Affine2().set(transform));
	}

	private void setupMatrices() {
		combinedMatrix.set(projectionMatrix).mul(transformMatrix);
		shader.setUniformMatrix("u_projTrans", combinedMatrix);
		shader.setUniformi("u_texture", 0);
	}

	protected void switchTexture(Texture texture) {
		flush();
		lastTexture = texture;
	}
}
