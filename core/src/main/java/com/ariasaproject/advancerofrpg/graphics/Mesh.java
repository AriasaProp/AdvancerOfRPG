package com.ariasaproject.advancerofrpg.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.math.Matrix3;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.math.collision.BoundingBox;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.IntArray;

public class Mesh implements Disposable {
	private static final Vector3 tmpV = new Vector3();
	// vertices data
	private final boolean verticesStatic;
	private final VertexAttributes vertexAttr;
	private final FloatBuffer verticesBuffer;
	private final ByteBuffer verticesByteBuffer;
	// end vertices data
	// indices data
	private final ShortBuffer indexBuffer;
	private final ByteBuffer indexByteBuffer;
	private final boolean indexStatic;
	private final int indexCapacity;
	// end indices data
	private int[] Handlers;// contain gl30 -> vao, vertices, indices;
	private boolean verticesDirty = true;
	private boolean indexDirty = true;
	private boolean autoBind = true;
	private IntArray cachedLocations = new IntArray();

	public Mesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
		this(isStatic, maxVertices, maxIndices, new VertexAttributes(attributes));
	}

	public Mesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttributes attributes) {
		this(isStatic, isStatic, maxVertices, maxIndices, attributes);
	}

	public Mesh(boolean staticVertices, boolean staticIndices, int maxVertices, int maxIndices, VertexAttribute... attributes) {
		this(staticVertices, staticIndices, maxVertices, maxIndices, new VertexAttributes(attributes));
	}

	public Mesh(boolean staticVertices, boolean staticIndices, int maxVertices, int maxIndices, VertexAttributes attributes) {
		if (maxIndices <= 0)
			throw new RuntimeException("Mesh should contain an index");
		// vertices prepare
		this.verticesStatic = staticVertices;
		this.vertexAttr = attributes;
		this.verticesByteBuffer = create(attributes.vertexSize * maxVertices).order(ByteOrder.nativeOrder());
		this.verticesBuffer = verticesByteBuffer.asFloatBuffer();
		verticesBuffer.flip();
		verticesByteBuffer.flip();
		// vertices done
		// indices prepare
		this.indexCapacity = Math.max(maxIndices * 2, 1);
		this.indexByteBuffer = create(indexCapacity).order(ByteOrder.nativeOrder());
		this.indexBuffer = indexByteBuffer.asShortBuffer();
		this.indexStatic = staticIndices;
		indexBuffer.flip();
		indexByteBuffer.flip();
		// indices done
	}

	public static void transformUV(final Matrix3 matrix, final float[] vertices, int vertexSize, int offset, int start, int count) {
		if (start < 0 || count < 1 || ((start + count) * vertexSize) > vertices.length)
			throw new IndexOutOfBoundsException("start = " + start + ", count = " + count + ", vertexSize = " + vertexSize + ", length = " + vertices.length);
		final Vector2 tmp = new Vector2();
		int idx = offset + (start * vertexSize);
		for (int i = 0; i < count; i++) {
			tmp.set(vertices[idx], vertices[idx + 1]).mul(matrix);
			vertices[idx] = tmp.x;
			vertices[idx + 1] = tmp.y;
			idx += vertexSize;
		}
	}

	public static void transform(final Matrix4 matrix, final float[] vertices, int stride, int posOffset, int numComponents, int start, int count) {
		if (posOffset < 0 || numComponents < 1 || (posOffset + numComponents) > stride)
			throw new IndexOutOfBoundsException();
		if (start < 0 || count < 1 || ((start + count) * stride) > vertices.length)
			throw new IndexOutOfBoundsException("start = " + start + ", count = " + count + ", vertexSize = " + stride + ", length = " + vertices.length);
		final Vector3 tmp = new Vector3();
		int idx = posOffset + (start * stride);
		switch (numComponents) {
		case 1:
			for (int i = 0; i < count; i++) {
				tmp.set(vertices[idx], 0, 0).mul(matrix);
				vertices[idx] = tmp.x;
				idx += stride;
			}
			break;
		case 2:
			for (int i = 0; i < count; i++) {
				tmp.set(vertices[idx], vertices[idx + 1], 0).mul(matrix);
				vertices[idx] = tmp.x;
				vertices[idx + 1] = tmp.y;
				idx += stride;
			}
			break;
		case 3:
			for (int i = 0; i < count; i++) {
				tmp.set(vertices[idx], vertices[idx + 1], vertices[idx + 2]).mul(matrix);
				vertices[idx] = tmp.x;
				vertices[idx + 1] = tmp.y;
				vertices[idx + 2] = tmp.z;
				idx += stride;
			}
			break;
		}
	}

	private static native ByteBuffer create(int byteCapacty);

	private static native void updateShort(short[] a, int o_a, ByteBuffer o, int o_o, int c);

	private static native void updateFloat(float[] a, int o_a, ByteBuffer o, int o_o, int c);

	private static native void destroy(ByteBuffer o);

	public Mesh setVertices(float[] vertices) {
		return setVertices(vertices, 0, vertices.length);
	}

	public Mesh setVertices(float[] vertices, int offset, int count) {
		updateFloat(vertices, offset, verticesByteBuffer, 0, count);
		verticesBuffer.position(0).limit(count);
		verticesDirty = true;
		return this;
	}

	public Mesh updateVertices(int targetOffset, float[] source) {
		return updateVertices(targetOffset, source, 0, source.length);
	}

	public Mesh updateVertices(int targetOffset, float[] source, int sourceOffset, int count) {
		updateFloat(source, sourceOffset, verticesByteBuffer, targetOffset, count);
		verticesBuffer.position(0);
		verticesDirty = true;
		return this;
	}

	public float[] getVertices(float[] vertices) {
		return getVertices(0, -1, vertices);
	}

	public float[] getVertices(int srcOffset, float[] vertices) {
		return getVertices(srcOffset, -1, vertices);
	}

	public float[] getVertices(int srcOffset, int count, float[] vertices) {
		return getVertices(srcOffset, count, vertices, 0);
	}

	public float[] getVertices(int srcOffset, int count, float[] vertices, int destOffset) {
		final int max = verticesBuffer.limit();
		if (count == -1) {
			count = max - srcOffset;
			if (count > vertices.length - destOffset)
				count = vertices.length - destOffset;
		}
		if (srcOffset < 0 || count <= 0 || (srcOffset + count) > max || destOffset < 0 || destOffset >= vertices.length)
			throw new IndexOutOfBoundsException();
		if ((vertices.length - destOffset) < count)
			throw new IllegalArgumentException("not enough room in vertices array, has " + vertices.length + " floats, needs " + count);
		int pos = verticesBuffer.position();
		verticesBuffer.position(srcOffset);
		verticesBuffer.get(vertices, destOffset, count);
		verticesBuffer.position(pos);
		return vertices;
	}

	public Mesh setIndices(short[] indices) {
		return setIndices(indices, 0, indices.length);
	}

	public Mesh setIndices(short[] indices, int offset, int count) {
		if (indexCapacity > 1) {
			indexBuffer.clear();
			indexBuffer.put(indices, offset, count);
			indexBuffer.flip();
			indexByteBuffer.position(0);
			indexByteBuffer.limit(count << 1);
			indexDirty = true;
		}
		return this;
	}

	public void getIndices(short[] indices) {
		getIndices(indices, 0);
	}

	public void getIndices(short[] indices, int destOffset) {
		getIndices(0, indices, destOffset);
	}

	public void getIndices(int srcOffset, short[] indices, int destOffset) {
		getIndices(srcOffset, -1, indices, destOffset);
	}

	public void getIndices(int src_o, int count, short[] indices, int dst_o) {
		if (indexCapacity <= 1)
			return;
		int max = getNumIndices();
		if (count < 0)
			count = max - src_o;
		if (src_o < 0 || src_o >= max || src_o + count > max)
			throw new IllegalArgumentException("Invalid range specified, offset: " + src_o + ", count: " + count + ", max: " + max);
		if ((indices.length - dst_o) < count)
			throw new IllegalArgumentException("not enough room in indices array, has " + indices.length + " shorts, needs " + count);
		int pos = indexBuffer.position();
		indexBuffer.position(src_o);
		indexBuffer.get(indices, dst_o, count);
		indexBuffer.position(pos);
		indexDirty = true;
	}

	public void updateIndices(int targetOffset, short[] indices, int offset, int count) {
		if (indexCapacity <= 1)
			return;
		final int pos = indexByteBuffer.position();
		indexByteBuffer.position(targetOffset * 2);
		updateShort(indices, offset, indexByteBuffer, indexByteBuffer.position(), count);
		indexByteBuffer.position(pos);
		indexBuffer.position(0);
		indexDirty = true;
	}

	public ShortBuffer getIndicesBuffer() {
		indexDirty = true;
		return indexBuffer;
	}

	public int getNumVertices() {
		return verticesBuffer.limit() * 4 / vertexAttr.vertexSize;
	}

	public int getMaxVertices() {
		return verticesByteBuffer.capacity() / vertexAttr.vertexSize;
	}

	public int getNumIndices() {
		return indexBuffer.limit();
	}

	public int getMaxIndices() {
		return indexBuffer.capacity();
	}

	public int getVertexSize() {
		return vertexAttr.vertexSize;
	}

	public void setAutoBind(boolean autoBind) {
		this.autoBind = autoBind;
	}

	public void bind(final ShaderProgram shader) {
		bind(shader, null);
	}

	public void bind(final ShaderProgram shader, final int[] locations) {
		final TGF g = GraphFunc.tgf;

		// vertices invalid
		if (Handlers == null || !g.validMesh(Handlers)) {
			if (cachedLocations.notEmpty())
				cachedLocations.clear();
			Handlers = g.genMesh(verticesByteBuffer.capacity(), verticesStatic, indexByteBuffer.capacity(), indexStatic);
			verticesDirty = true;
			indexDirty = true;
			// done
		} else
			g.glBindVertexArray(Handlers[0]);

		boolean stillValid = this.cachedLocations.size != 0;
		final int numAttributes = vertexAttr.size();

		if (stillValid) {
			if (locations == null) {
				for (int i = 0; stillValid && i < numAttributes; i++) {
					int location = shader.getAttributeLocation(vertexAttr.get(i).alias);
					stillValid = location == this.cachedLocations.get(i);
				}
			} else {
				stillValid = locations.length == this.cachedLocations.size;
				for (int i = 0; stillValid && i < numAttributes; i++) {
					stillValid = locations[i] == this.cachedLocations.get(i);
				}
			}
		}

		if (verticesDirty || !stillValid) {
			g.glBindBuffer(TGF.GL_ARRAY_BUFFER, Handlers[1]);
			if (verticesDirty) {
				verticesByteBuffer.limit(verticesByteBuffer.capacity());
				g.glBufferSubData(TGF.GL_ARRAY_BUFFER, 0, verticesByteBuffer.limit(), verticesByteBuffer);
				verticesDirty = false;
			}
			if (!stillValid) {
				if (cachedLocations.size != 0) {
					int location = -1;
					while (cachedLocations.size != 0) {
						location = cachedLocations.pop();
						if (location < 0)
							continue;
						shader.disableVertexAttribute(location);
					}
				}

				for (int i = 0, l = -1; i < numAttributes; i++) {
					VertexAttribute attribute = vertexAttr.get(i);
					if (locations == null)
						this.cachedLocations.add(l = shader.getAttributeLocation(attribute.alias));
					else
						this.cachedLocations.add(l = locations[i]);
					if (l < 0)
						continue;
					shader.enableVertexAttribute(l);
					shader.setVertexAttribute(l, attribute.numComponents, attribute.type, attribute.normalized, vertexAttr.vertexSize, attribute.offset);
				}
			}
			g.glBindBuffer(TGF.GL_ARRAY_BUFFER, 0);
		}

		// indices
		g.glBindBuffer(TGF.GL_ELEMENT_ARRAY_BUFFER, Handlers[2]);
		if (indexDirty) {
			indexByteBuffer.limit(indexBuffer.limit() * 2);
			g.glBufferSubData(TGF.GL_ELEMENT_ARRAY_BUFFER, 0, indexByteBuffer.limit(), indexByteBuffer);
			indexDirty = false;

		}
	}

	public void unbind() {
		final TGF g = GraphFunc.tgf;
		g.glBindBuffer(TGF.GL_ELEMENT_ARRAY_BUFFER, 0);
		g.glBindVertexArray(0);
	}

	public void render(ShaderProgram shader, int primitiveType) {
		render(shader, primitiveType, 0, getNumIndices(), autoBind);
	}

	public void render(ShaderProgram shader, int primitiveType, int offset, int count) {
		render(shader, primitiveType, offset, count, autoBind);
	}

	public void render(ShaderProgram shader, int primitiveType, int offset, int count, boolean autoBind) {
		if (count == 0)
			return;
		if (autoBind)
			bind(shader);

		if (count + offset > getMaxIndices()) {
			throw new RuntimeException("Mesh attempting to access memory outside of the indexBuffer (count: " + count + ", offset: " + offset + ", max: " + getMaxIndices() + ")");
		}
		GraphFunc.tgf.glDrawElements(primitiveType, count, TGF.GL_UNSIGNED_SHORT, offset * 2);
		if (autoBind)
			unbind();
	}

	@Override
	public void dispose() {
		if (Handlers == null)
			return;
		destroy(verticesByteBuffer);
		destroy(indexByteBuffer);
		GraphFunc.tgf.destroyMesh(Handlers);
		Handlers = null;
	}

	public VertexAttribute getVertexAttribute(int usage) {
		int len = vertexAttr.size();
		for (int i = 0; i < len; i++)
			if (vertexAttr.get(i).usage == usage)
				return vertexAttr.get(i);
		return null;
	}

	public VertexAttributes getVertexAttributes() {
		return vertexAttr;
	}

	public FloatBuffer getVerticesBuffer() {
		return verticesBuffer;
	}

	public BoundingBox calculateBoundingBox() {
		BoundingBox bbox = new BoundingBox();
		calculateBoundingBox(bbox);
		return bbox;
	}

	public void calculateBoundingBox(BoundingBox bbox) {
		final int numVertices = getNumVertices();
		if (numVertices == 0)
			throw new RuntimeException("No vertices defined");
		final FloatBuffer verts = verticesBuffer;
		bbox.inf();
		final VertexAttribute posAttrib = getVertexAttribute(Usage.Position);
		final int offset = posAttrib.offset / 4;
		final int vertexSize = vertexAttr.vertexSize / 4;
		int idx = offset;
		switch (posAttrib.numComponents) {
		case 1:
			for (int i = 0; i < numVertices; i++) {
				bbox.ext(verts.get(idx), 0, 0);
				idx += vertexSize;
			}
			break;
		case 2:
			for (int i = 0; i < numVertices; i++) {
				bbox.ext(verts.get(idx), verts.get(idx + 1), 0);
				idx += vertexSize;
			}
			break;
		case 3:
			for (int i = 0; i < numVertices; i++) {
				bbox.ext(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
				idx += vertexSize;
			}
			break;
		}
	}

	public BoundingBox calculateBoundingBox(final BoundingBox out, int offset, int count) {
		return extendBoundingBox(out.inf(), offset, count);
	}

	public BoundingBox calculateBoundingBox(final BoundingBox out, int offset, int count, final Matrix4 transform) {
		return extendBoundingBox(out.inf(), offset, count, transform);
	}

	public BoundingBox extendBoundingBox(final BoundingBox out, int offset, int count) {
		return extendBoundingBox(out, offset, count, null);
	}

	public BoundingBox extendBoundingBox(final BoundingBox out, int offset, int count, final Matrix4 transform) {
		final int numIndices = getNumIndices();
		final int numVertices = getNumVertices();
		final int max = numIndices == 0 ? numVertices : numIndices;
		if (offset < 0 || count < 1 || offset + count > max)
			throw new RuntimeException("Invalid part specified ( offset=" + offset + ", count=" + count + ", max=" + max + " )");
		final FloatBuffer verts = verticesBuffer;
		final ShortBuffer ind = indexBuffer;
		final VertexAttribute posAttrib = getVertexAttribute(Usage.Position);
		final int posoff = posAttrib.offset / 4;
		final int vertexSize = vertexAttr.vertexSize / 4;
		final int end = offset + count;
		int i;// for loop function
		switch (posAttrib.numComponents) {
		case 1:
			if (numIndices > 0) {
				for (i = offset; i < end; i++) {
					final int idx = (ind.get(i) & 0xFFFF) * vertexSize + posoff;
					tmpV.set(verts.get(idx), 0, 0);
					if (transform != null)
						tmpV.mul(transform);
					out.ext(tmpV);
				}
			} else {
				for (i = offset; i < end; i++) {
					final int idx = i * vertexSize + posoff;
					tmpV.set(verts.get(idx), 0, 0);
					if (transform != null)
						tmpV.mul(transform);
					out.ext(tmpV);
				}
			}
			break;
		case 2:
			if (numIndices > 0) {
				for (i = offset; i < end; i++) {
					final int idx = (ind.get(i) & 0xFFFF) * vertexSize + posoff;
					tmpV.set(verts.get(idx), verts.get(idx + 1), 0);
					if (transform != null)
						tmpV.mul(transform);
					out.ext(tmpV);
				}
			} else {
				for (i = offset; i < end; i++) {
					final int idx = i * vertexSize + posoff;
					tmpV.set(verts.get(idx), verts.get(idx + 1), 0);
					if (transform != null)
						tmpV.mul(transform);
					out.ext(tmpV);
				}
			}
			break;
		case 3:
			if (numIndices > 0) {
				for (i = offset; i < end; i++) {
					final int idx = (ind.get(i) & 0xFFFF) * vertexSize + posoff;
					tmpV.set(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
					if (transform != null)
						tmpV.mul(transform);
					out.ext(tmpV);
				}
			} else {
				for (i = offset; i < end; i++) {
					final int idx = i * vertexSize + posoff;
					tmpV.set(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
					if (transform != null)
						tmpV.mul(transform);
					out.ext(tmpV);
				}
			}
			break;
		}
		return out;
	}

	public float calculateRadius(final float centerX, final float centerY, final float centerZ, int offset, int count, final Matrix4 transform) {
		return (float) Math.sqrt(calculateRadiusSquared(centerX, centerY, centerZ, offset, count, transform));
	}

	public float calculateRadius(final Vector3 center, int offset, int count, final Matrix4 transform) {
		return calculateRadius(center.x, center.y, center.z, offset, count, transform);
	}

	public float calculateRadius(final float centerX, final float centerY, final float centerZ, int offset, int count) {
		return calculateRadius(centerX, centerY, centerZ, offset, count, null);
	}

	public float calculateRadius(final Vector3 center, int offset, int count) {
		return calculateRadius(center.x, center.y, center.z, offset, count, null);
	}

	public float calculateRadius(final float centerX, final float centerY, final float centerZ) {
		return calculateRadius(centerX, centerY, centerZ, 0, getNumIndices(), null);
	}

	public float calculateRadius(final Vector3 center) {
		return calculateRadius(center.x, center.y, center.z, 0, getNumIndices(), null);
	}

	public float calculateRadiusSquared(final float centerX, final float centerY, final float centerZ, int offset, int count, final Matrix4 transform) {
		int numIndices = getNumIndices();
		if (offset < 0 || count < 1 || offset + count > numIndices)
			throw new RuntimeException("Not enough indices");
		final FloatBuffer verts = verticesBuffer;
		indexDirty = true;
		final VertexAttribute posAttrib = getVertexAttribute(Usage.Position);
		final int posoff = posAttrib.offset / 4;
		final int vertexSize = vertexAttr.vertexSize / 4;
		final int end = offset + count;
		float result = 0;
		switch (posAttrib.numComponents) {
		case 1:
			for (int i = offset; i < end; i++) {
				final int idx = (indexBuffer.get(i) & 0xFFFF) * vertexSize + posoff;
				tmpV.set(verts.get(idx), 0, 0);
				if (transform != null)
					tmpV.mul(transform);
				final float r = tmpV.sub(centerX, centerY, centerZ).len2();
				if (r > result)
					result = r;
			}
			break;
		case 2:
			for (int i = offset; i < end; i++) {
				final int idx = (indexBuffer.get(i) & 0xFFFF) * vertexSize + posoff;
				tmpV.set(verts.get(idx), verts.get(idx + 1), 0);
				if (transform != null)
					tmpV.mul(transform);
				final float r = tmpV.sub(centerX, centerY, centerZ).len2();
				if (r > result)
					result = r;
			}
			break;
		case 3:
			for (int i = offset; i < end; i++) {
				final int idx = (indexBuffer.get(i) & 0xFFFF) * vertexSize + posoff;
				tmpV.set(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
				if (transform != null)
					tmpV.mul(transform);
				final float r = tmpV.sub(centerX, centerY, centerZ).len2();
				if (r > result)
					result = r;
			}
			break;
		}
		return result;
	}

	public void scale(float scaleX, float scaleY, float scaleZ) {
		final VertexAttribute posAttr = getVertexAttribute(Usage.Position);
		final int offset = posAttr.offset / 4;
		final int numComponents = posAttr.numComponents;
		final int numVertices = getNumVertices();
		final int vertexSize = getVertexSize() / 4;
		final float[] vertices = new float[numVertices * vertexSize];
		getVertices(vertices);
		int idx = offset;
		switch (numComponents) {
		case 1:
			for (int i = 0; i < numVertices; i++) {
				vertices[idx] *= scaleX;
				idx += vertexSize;
			}
			break;
		case 2:
			for (int i = 0; i < numVertices; i++) {
				vertices[idx] *= scaleX;
				vertices[idx + 1] *= scaleY;
				idx += vertexSize;
			}
			break;
		case 3:
			for (int i = 0; i < numVertices; i++) {
				vertices[idx] *= scaleX;
				vertices[idx + 1] *= scaleY;
				vertices[idx + 2] *= scaleZ;
				idx += vertexSize;
			}
			break;
		}
		setVertices(vertices);
	}

	public void transform(final Matrix4 matrix) {
		transform(matrix, 0, getNumVertices());
	}

	public void transform(final Matrix4 matrix, final int start, final int count) {
		final VertexAttribute posAttr = getVertexAttribute(Usage.Position);
		final int posOffset = posAttr.offset / 4;
		final int stride = getVertexSize() / 4;
		final int numComponents = posAttr.numComponents;
		final float[] vertices = new float[count * stride];
		getVertices(start * stride, count * stride, vertices);
		transform(matrix, vertices, stride, posOffset, numComponents, 0, count);
		updateVertices(start * stride, vertices);
	}

	public void transformUV(final Matrix3 matrix) {
		transformUV(matrix, 0, getNumVertices());
	}

	protected void transformUV(final Matrix3 matrix, final int start, final int count) {
		final VertexAttribute posAttr = getVertexAttribute(Usage.TextureCoordinates);
		final int offset = posAttr.offset / 4;
		final int vertexSize = getVertexSize() / 4;
		final int numVertices = getNumVertices();
		final float[] vertices = new float[numVertices * vertexSize];
		// TODO: getVertices(vertices, start * vertexSize, count * vertexSize);
		getVertices(0, vertices.length, vertices);
		transformUV(matrix, vertices, vertexSize, offset, start, count);
		setVertices(vertices, 0, vertices.length);
		// TODO: setVertices(start * vertexSize, vertices, 0, vertices.length);
	}

	public Mesh copy(boolean isStatic, boolean removeDuplicates, final int[] usage) {
		final int vertexSize = getVertexSize() / 4;
		int numVertices = getNumVertices();
		float[] vertices = new float[numVertices * vertexSize];
		getVertices(0, vertices.length, vertices);
		short[] checks = null;
		VertexAttribute[] attrs = null;
		int newVertexSize = 0;
		if (usage != null) {
			int size = 0;
			int as = 0;
			for (int i = 0; i < usage.length; i++)
				if (getVertexAttribute(usage[i]) != null) {
					size += getVertexAttribute(usage[i]).numComponents;
					as++;
				}
			if (size > 0) {
				attrs = new VertexAttribute[as];
				checks = new short[size];
				int idx = -1;
				int ai = -1;
				for (int i = 0; i < usage.length; i++) {
					VertexAttribute a = getVertexAttribute(usage[i]);
					if (a == null)
						continue;
					for (int j = 0; j < a.numComponents; j++)
						checks[++idx] = (short) (a.offset + j);
					attrs[++ai] = a.copy();
					newVertexSize += a.numComponents;
				}
			}
		}
		if (checks == null) {
			checks = new short[vertexSize];
			for (short i = 0; i < vertexSize; i++)
				checks[i] = i;
			newVertexSize = vertexSize;
		}
		int numIndices = getNumIndices();
		short[] indices = null;
		if (numIndices > 0) {
			indices = new short[numIndices];
			getIndices(indices);
			if (removeDuplicates || newVertexSize != vertexSize) {
				float[] tmp = new float[vertices.length];
				int size = 0;
				for (int i = 0; i < numIndices; i++) {
					final int idx1 = indices[i] * vertexSize;
					short newIndex = -1;
					if (removeDuplicates) {
						for (short j = 0; j < size && newIndex < 0; j++) {
							final int idx2 = j * newVertexSize;
							boolean found = true;
							for (int k = 0; k < checks.length && found; k++) {
								if (tmp[idx2 + k] != vertices[idx1 + checks[k]])
									found = false;
							}
							if (found)
								newIndex = j;
						}
					}
					if (newIndex > 0)
						indices[i] = newIndex;
					else {
						final int idx = size * newVertexSize;
						for (int j = 0; j < checks.length; j++)
							tmp[idx + j] = vertices[idx1 + checks[j]];
						indices[i] = (short) size;
						size++;
					}
				}
				vertices = tmp;
				numVertices = size;
			}
		}
		Mesh result;
		if (attrs == null)
			result = new Mesh(isStatic, numVertices, indices.length, getVertexAttributes());
		else
			result = new Mesh(isStatic, numVertices, indices.length, attrs);
		result.setVertices(vertices, 0, numVertices * newVertexSize);
		if (indices != null)
			result.setIndices(indices);
		return result;
	}

	public Mesh copy(boolean isStatic) {
		return copy(isStatic, false, null);
	}
}
