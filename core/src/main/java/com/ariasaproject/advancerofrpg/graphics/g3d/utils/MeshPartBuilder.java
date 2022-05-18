package com.ariasaproject.advancerofrpg.graphics.g3d.utils;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.Mesh;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;
import com.ariasaproject.advancerofrpg.graphics.g2d.TextureRegion;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.MeshPart;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Pool.Poolable;

public interface MeshPartBuilder {
	/**
	 * @return The {@link MeshPart} currently building.
	 */
	MeshPart getMeshPart();

	/**
	 * @return The primitive type used for building, e.g. {@link GL20#GL_TRIANGLES}
	 *         or {@link GL20#GL_LINES}.
	 */
	int getPrimitiveType();

	/**
	 * @return The {@link VertexAttributes} available for building.
	 */
	VertexAttributes getAttributes();

	/**
	 * Set the color used to tint the vertex color, defaults to white. Only
	 * applicable for {@link Usage#ColorPacked} or {@link Usage#ColorUnpacked}.
	 */
	void setColor(final Color color);

	/**
	 * Set the color used to tint the vertex color, defaults to white. Only
	 * applicable for {@link Usage#ColorPacked} or {@link Usage#ColorUnpacked}.
	 */
	void setColor(float r, float g, float b, float a);

	/**
	 * Set range of texture coordinates used (default is 0,0,1,1).
	 */
	void setUVRange(float u1, float v1, float u2, float v2);

	/**
	 * Set range of texture coordinates from the specified TextureRegion.
	 */
	void setUVRange(TextureRegion r);

	/**
	 * Get the current vertex transformation matrix.
	 */
	Matrix4 getVertexTransform(Matrix4 out);

	/**
	 * Set the current vertex transformation matrix and enables vertex
	 * transformation.
	 */
	void setVertexTransform(Matrix4 transform);

	/**
	 * Indicates whether vertex transformation is enabled.
	 */
	boolean isVertexTransformationEnabled();

	/**
	 * Sets whether vertex transformation is enabled.
	 */
	void setVertexTransformationEnabled(boolean enabled);

	/**
	 * Increases the size of the backing vertices array to accommodate the specified
	 * number of additional vertices. Useful before adding many vertices to avoid
	 * multiple backing array resizes.
	 *
	 * @param numVertices The number of vertices you are about to add
	 */
	void ensureVertices(int numVertices);

	/**
	 * Increases the size of the backing indices array to accommodate the specified
	 * number of additional indices. Useful before adding many indices to avoid
	 * multiple backing array resizes.
	 *
	 * @param numIndices The number of indices you are about to add
	 */
	void ensureIndices(int numIndices);

	/**
	 * Increases the size of the backing vertices and indices arrays to accommodate
	 * the specified number of additional vertices and indices. Useful before adding
	 * many vertices and indices to avoid multiple backing array resizes.
	 *
	 * @param numVertices The number of vertices you are about to add
	 * @param numIndices  The number of indices you are about to add
	 */
	void ensureCapacity(int numVertices, int numIndices);

	/**
	 * Increases the size of the backing indices array to accommodate the specified
	 * number of additional triangles. Useful before adding many triangles using
	 * {@link #triangle(short, short, short)} to avoid multiple backing array
	 * resizes. The actual number of indices accounted for depends on the primitive
	 * type (see {@link #getPrimitiveType()}).
	 *
	 * @param numTriangles The number of triangles you are about to add
	 */
	void ensureTriangleIndices(int numTriangles);

	/**
	 * Increases the size of the backing indices array to accommodate the specified
	 * number of additional rectangles. Useful before adding many rectangles using
	 * {@link #rect(short, short, short, short)} to avoid multiple backing array
	 * resizes.
	 *
	 * @param numRectangles The number of rectangles you are about to add
	 */
	void ensureRectangleIndices(int numRectangles);

	/**
	 * Add one or more vertices, returns the index of the last vertex added. The
	 * length of values must a power of the vertex size.
	 */
	short vertex(final float... values);

	/**
	 * Add a vertex, returns the index. Null values are allowed. Use
	 * {@link #getAttributes} to check which values are available.
	 */
	short vertex(Vector3 pos, Vector3 nor, Color col, Vector2 uv);

	/**
	 * Add a vertex, returns the index. Use {@link #getAttributes} to check which
	 * values are available.
	 */
	short vertex(final VertexInfo info);

	/**
	 * @return The index of the last added vertex.
	 */
	short lastIndex();

	/**
	 * Add an index, MeshPartBuilder expects all meshes to be indexed.
	 */
	void index(final short value);

	/**
	 * Add multiple indices, MeshPartBuilder expects all meshes to be indexed.
	 */
	void index(short value1, short value2);

	/**
	 * Add multiple indices, MeshPartBuilder expects all meshes to be indexed.
	 */
	void index(short value1, short value2, short value3);

	/**
	 * Add multiple indices, MeshPartBuilder expects all meshes to be indexed.
	 */
	void index(short value1, short value2, short value3, short value4);

	/**
	 * Add multiple indices, MeshPartBuilder expects all meshes to be indexed.
	 */
	void index(short value1, short value2, short value3, short value4, short value5, short value6);

	/**
	 * Add multiple indices, MeshPartBuilder expects all meshes to be indexed.
	 */
	void index(short value1, short value2, short value3, short value4, short value5, short value6, short value7, short value8);

	/**
	 * Add a line by indices. Requires GL_LINES primitive type.
	 */
	void line(short index1, short index2);

	/**
	 * Add a line. Requires GL_LINES primitive type.
	 */
	void line(VertexInfo p1, VertexInfo p2);

	/**
	 * Add a line. Requires GL_LINES primitive type.
	 */
	void line(Vector3 p1, Vector3 p2);

	/**
	 * Add a line. Requires GL_LINES primitive type.
	 */
	void line(float x1, float y1, float z1, float x2, float y2, float z2);

	/**
	 * Add a line. Requires GL_LINES primitive type.
	 */
	void line(Vector3 p1, Color c1, Vector3 p2, Color c2);

	/**
	 * Add a triangle by indices. Requires GL_POINTS, GL_LINES or GL_TRIANGLES
	 * primitive type.
	 */
	void triangle(short index1, short index2, short index3);

	/**
	 * Add a triangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type.
	 */
	void triangle(VertexInfo p1, VertexInfo p2, VertexInfo p3);

	/**
	 * Add a triangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type.
	 */
	void triangle(Vector3 p1, Vector3 p2, Vector3 p3);

	/**
	 * Add a triangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type.
	 */
	void triangle(Vector3 p1, Color c1, Vector3 p2, Color c2, Vector3 p3, Color c3);

	/**
	 * Add a rectangle by indices. Requires GL_POINTS, GL_LINES or GL_TRIANGLES
	 * primitive type.
	 */
	void rect(short corner00, short corner10, short corner11, short corner01);

	/**
	 * Add a rectangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type.
	 */
	void rect(VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01);

	/**
	 * Add a rectangle. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type.
	 */
	void rect(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal);

	/**
	 * Add a rectangle Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type.
	 */
	void rect(float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01, float normalX, float normalY, float normalZ);

	/**
	 * Copies a mesh to the mesh (part) currently being build.
	 *
	 * @param mesh The mesh to copy, must have the same vertex attributes and must
	 *             be indexed.
	 */
	void addMesh(Mesh mesh);

	/**
	 * Copies a MeshPart to the mesh (part) currently being build.
	 *
	 * @param meshpart The MeshPart to copy, must have the same vertex attributes,
	 *                 primitive type and must be indexed.
	 */
	void addMesh(MeshPart meshpart);

	/**
	 * Copies a (part of a) mesh to the mesh (part) currently being build.
	 *
	 * @param mesh        The mesh to (partly) copy, must have the same vertex
	 *                    attributes and must be indexed.
	 * @param indexOffset The zero-based offset of the first index of the part of
	 *                    the mesh to copy.
	 * @param numIndices  The number of indices of the part of the mesh to copy.
	 */
	void addMesh(Mesh mesh, int indexOffset, int numIndices);

	/**
	 * Copies a mesh to the mesh (part) currently being build. The entire vertices
	 * array is added, even if some of the vertices are not indexed by the indices
	 * array. If you want to add only the vertices that are actually indexed, then
	 * use the {@link #addMesh(float[], short[], int, int)} method instead.
	 *
	 * @param vertices The vertices to copy, must be in the same vertex layout as
	 *                 the mesh being build.
	 * @param indices  Array containing the indices to copy, each index should be
	 *                 valid in the vertices array.
	 */
	void addMesh(float[] vertices, short[] indices);

	/**
	 * Copies a (part of a) mesh to the mesh (part) currently being build.
	 *
	 * @param vertices    The vertices to (partly) copy, must be in the same vertex
	 *                    layout as the mesh being build.
	 * @param indices     Array containing the indices to (partly) copy, each index
	 *                    should be valid in the vertices array.
	 * @param indexOffset The zero-based offset of the first index of the part of
	 *                    indices array to copy.
	 * @param numIndices  The number of indices of the part of the indices array to
	 *                    copy.
	 */
	void addMesh(float[] vertices, short[] indices, int indexOffset, int numIndices);

	/**
	 * Class that contains all vertex information the builder can use.
	 *
	 * @author Xoppa
	 */
	class VertexInfo implements Poolable {
		public final Vector3 position = new Vector3();
		public final Vector3 normal = new Vector3(0, 1, 0);
		public final Color color = new Color(1, 1, 1, 1);
		public final Vector2 uv = new Vector2();
		public boolean hasPosition;
		public boolean hasNormal;
		public boolean hasColor;
		public boolean hasUV;

		@Override
		public void reset() {
			position.set(0, 0, 0);
			normal.set(0, 1, 0);
			color.set(1, 1, 1, 1);
			uv.set(0, 0);
		}

		public VertexInfo set(Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
			reset();
			hasPosition = pos != null;
			if (hasPosition)
				position.set(pos);
			hasNormal = nor != null;
			if (hasNormal)
				normal.set(nor);
			hasColor = col != null;
			if (hasColor)
				color.set(col);
			hasUV = uv != null;
			if (hasUV)
				this.uv.set(uv);
			return this;
		}

		public VertexInfo set(final VertexInfo other) {
			if (other == null)
				return set(null, null, null, null);
			hasPosition = other.hasPosition;
			position.set(other.position);
			hasNormal = other.hasNormal;
			normal.set(other.normal);
			hasColor = other.hasColor;
			color.set(other.color);
			hasUV = other.hasUV;
			uv.set(other.uv);
			return this;
		}

		public VertexInfo setPos(float x, float y, float z) {
			position.set(x, y, z);
			hasPosition = true;
			return this;
		}

		public VertexInfo setPos(Vector3 pos) {
			hasPosition = pos != null;
			if (hasPosition)
				position.set(pos);
			return this;
		}

		public VertexInfo setNor(float x, float y, float z) {
			normal.set(x, y, z);
			hasNormal = true;
			return this;
		}

		public VertexInfo setNor(Vector3 nor) {
			hasNormal = nor != null;
			if (hasNormal)
				normal.set(nor);
			return this;
		}

		public VertexInfo setCol(float r, float g, float b, float a) {
			color.set(r, g, b, a);
			hasColor = true;
			return this;
		}

		public VertexInfo setCol(Color col) {
			hasColor = col != null;
			if (hasColor)
				color.set(col);
			return this;
		}

		public VertexInfo setUV(float u, float v) {
			uv.set(u, v);
			hasUV = true;
			return this;
		}

		public VertexInfo setUV(Vector2 uv) {
			hasUV = uv != null;
			if (hasUV)
				this.uv.set(uv);
			return this;
		}

		public VertexInfo lerp(final VertexInfo target, float alpha) {
			if (hasPosition && target.hasPosition)
				position.lerp(target.position, alpha);
			if (hasNormal && target.hasNormal)
				normal.lerp(target.normal, alpha);
			if (hasColor && target.hasColor)
				color.lerp(target.color, alpha);
			if (hasUV && target.hasUV)
				uv.lerp(target.uv, alpha);
			return this;
		}
	}
}
