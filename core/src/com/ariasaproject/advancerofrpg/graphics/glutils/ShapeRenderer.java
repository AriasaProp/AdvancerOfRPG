package com.ariasaproject.advancerofrpg.graphics.glutils;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.Mesh;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.VertexAttribute;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public class ShapeRenderer implements Disposable {
	public enum ShapeType {
		Point(TGF.GL_POINTS), Line(TGF.GL_LINES), Filled(TGF.GL_TRIANGLES);

		private final int glType;

		ShapeType (int glType) {
			this.glType = glType;
		}

		public int getGlType () {
			return glType;
		}
	}

	private boolean matrixDirty = false;
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 combinedMatrix = new Matrix4();
	private final Vector2 tmp = new Vector2();
	private final Color color = new Color(1, 1, 1, 1);
	private ShapeType shapeType;
	private boolean autoShapeType;
	private float defaultRectLineWidth = 0.75f;
	
	//renderer
	private int primitiveType;
	private int vertexIdx;
	private final int maxVertices;
	private int numVertices;

	private final Mesh mesh;
	private final ShaderProgram shader;
	private final int vertexSize;
	private final int colorOffset;
	private final float[] vertices;
	public ShapeRenderer () {
		this(5000);
	}

	public ShapeRenderer (int maxVertices) {
		projectionMatrix.setToOrtho2D(0, 0, GraphFunc.app.getGraphics().getWidth(), GraphFunc.app.getGraphics().getHeight());
		matrixDirty = true;
		this.maxVertices = maxVertices;

		String shaderSource = "";
		shaderSource += "attribute vec4 a_position;\n";
		shaderSource += "attribute vec4 a_color;\n";
		shaderSource += "uniform mat4 u_projModelView;\n";
		shaderSource += "varying vec4 v_col;\n";
		shaderSource += "void main() {\n" + "   gl_Position = u_projModelView * a_position;\n";
		shaderSource += "   v_col = a_color;\n";
		shaderSource += "   v_col.a *= 255.0 / 254.0;\n";
		shaderSource += "   gl_PointSize = 1.0;\n";
		shaderSource += "}\n";
		shaderSource += "\n<break>\n";
		shaderSource += "varying vec4 v_col;\n";
		shaderSource += "void main() {\n";
		shaderSource += "   gl_FragColor = v_col;";
		shaderSource += "\n}";
		this.shader =  new ShaderProgram(shaderSource, "");

		VertexAttribute[] attribs = new VertexAttribute[2];
		attribs[0] = new VertexAttribute(Usage.Position, 3, "a_position");
		attribs[1] = new VertexAttribute(Usage.ColorPacked, 4, "a_color");
		mesh = new Mesh(false, maxVertices, 0, attribs);

		vertices = new float[maxVertices * (mesh.getVertexAttributes().vertexSize / 4)];
		vertexSize = mesh.getVertexAttributes().vertexSize / 4;
		colorOffset = mesh.getVertexAttribute(Usage.ColorPacked) != null ? mesh.getVertexAttribute(Usage.ColorPacked).offset / 4: 0;
	}

	public void color (Color color) {
		vertices[vertexIdx + colorOffset] = color.toFloatBits();
	}

	public void color (float r, float g, float b, float a) {
		vertices[vertexIdx + colorOffset] = Color.toFloatBits(r, g, b, a);
	}

	public void color (float colorBits) {
		vertices[vertexIdx + colorOffset] = colorBits;
	}

	public void vertex (float x, float y, float z) {
		final int idx = vertexIdx;
		vertices[idx] = x;
		vertices[idx + 1] = y;
		vertices[idx + 2] = z;
		vertexIdx += vertexSize;
		numVertices++;
	}

	public int getNumVertices () {
		return numVertices;
	}

	public int getMaxVertices () {
		return maxVertices;
	}


	/** Sets the color to be used by the next shapes drawn. */
	public void setColor (Color color) {
		this.color.set(color);
	}

	/** Sets the color to be used by the next shapes drawn. */
	public void setColor (float r, float g, float b, float a) {
		this.color.set(r, g, b, a);
	}

	public Color getColor () {
		return color;
	}

	public void updateMatrices () {
		matrixDirty = true;
	}

	/** Sets the projection matrix to be used for rendering. Usually this will be set to {@link Camera#combined}.
	 * @param matrix */
	public void setProjectionMatrix (Matrix4 matrix) {
		projectionMatrix.set(matrix);
		matrixDirty = true;
	}

	/** If the matrix is modified, {@link #updateMatrices()} must be called. */
	public Matrix4 getProjectionMatrix () {
		return projectionMatrix;
	}

	public void setTransformMatrix (Matrix4 matrix) {
		transformMatrix.set(matrix);
		matrixDirty = true;
	}

	/** If the matrix is modified, {@link #updateMatrices()} must be called. */
	public Matrix4 getTransformMatrix () {
		return transformMatrix;
	}

	/** Sets the transformation matrix to identity. */
	public void identity () {
		transformMatrix.idt();
		matrixDirty = true;
	}

	/** Multiplies the current transformation matrix by a translation matrix. */
	public void translate (float x, float y, float z) {
		transformMatrix.translate(x, y, z);
		matrixDirty = true;
	}

	/** Multiplies the current transformation matrix by a rotation matrix. */
	public void rotate (float axisX, float axisY, float axisZ, float degrees) {
		transformMatrix.rotate(axisX, axisY, axisZ, degrees);
		matrixDirty = true;
	}

	/** Multiplies the current transformation matrix by a scale matrix. */
	public void scale (float scaleX, float scaleY, float scaleZ) {
		transformMatrix.scale(scaleX, scaleY, scaleZ);
		matrixDirty = true;
	}

	/** If true, when drawing a shape cannot be performed with the current shape type, the batch is flushed and the shape type is
	 * changed automatically. This can increase the number of batch flushes if care is not taken to draw the same type of shapes
	 * together. Default is false. */
	public void setAutoShapeType (boolean autoShapeType) {
		this.autoShapeType = autoShapeType;
	}

	/** Begins a new batch without specifying a shape type.
	 * @throws IllegalStateException if {@link #autoShapeType} is false. */
	public void begin () {
		if (!autoShapeType) throw new IllegalStateException("autoShapeType must be true to use this method.");
		begin(ShapeType.Line);
	}

	public void begin (ShapeType type) {
		if (shapeType != null) throw new IllegalStateException("Call end() before beginning a new shape batch.");
		shapeType = type;
		this.primitiveType = shapeType.getGlType();
	}

	public void set (ShapeType type) {
		if (shapeType == type) return;
		if (shapeType == null) throw new IllegalStateException("begin must be called first.");
		if (!autoShapeType) throw new IllegalStateException("autoShapeType must be enabled.");
		end();
		begin(type);
	}

	/** Draws a point using {@link ShapeType#Point}, {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void point (float x, float y, float z) {
		if (shapeType == ShapeType.Line) {
			float size = defaultRectLineWidth * 0.5f;
			line(x - size, y - size, z, x + size, y + size, z);
			return;
		} else if (shapeType == ShapeType.Filled) {
			float size = defaultRectLineWidth * 0.5f;
			box(x - size, y - size, z - size, defaultRectLineWidth, defaultRectLineWidth, defaultRectLineWidth);
			return;
		}
		check(ShapeType.Point, null, 1);
		color(color);
		vertex(x, y, z);
	}

	/** Draws a line using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public final void line (float x, float y, float z, float x2, float y2, float z2) {
		line(x, y, z, x2, y2, z2, color, color);
	}

	/** @see #line(float, float, float, float, float, float) */
	public final void line (Vector3 v0, Vector3 v1) {
		line(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z, color, color);
	}

	/** @see #line(float, float, float, float, float, float) */
	public final void line (float x, float y, float x2, float y2) {
		line(x, y, 0.0f, x2, y2, 0.0f, color, color);
	}

	/** @see #line(float, float, float, float, float, float) */
	public final void line (Vector2 v0, Vector2 v1) {
		line(v0.x, v0.y, 0.0f, v1.x, v1.y, 0.0f, color, color);
	}

	/** @see #line(float, float, float, float, float, float, Color, Color) */
	public final void line (float x, float y, float x2, float y2, Color c1, Color c2) {
		line(x, y, 0.0f, x2, y2, 0.0f, c1, c2);
	}

	/** Draws a line using {@link ShapeType#Line} or {@link ShapeType#Filled}. The line is drawn with two colors interpolated
	 * between the start and end points. */
	public void line (float x, float y, float z, float x2, float y2, float z2, Color c1, Color c2) {
		if (shapeType == ShapeType.Filled) {
			rectLine(x, y, x2, y2, defaultRectLineWidth, c1, c2);
			return;
		}
		check(ShapeType.Line, null, 2);
		color(c1.r, c1.g, c1.b, c1.a);
		vertex(x, y, z);
		color(c2.r, c2.g, c2.b, c2.a);
		vertex(x2, y2, z2);
	}

	/** Draws a curve using {@link ShapeType#Line}. */
	public void curve (float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2, int segments) {
		check(ShapeType.Line, null, segments * 2 + 2);
		float colorBits = color.toFloatBits();
		
		float subdiv_step = 1f / segments;
		float subdiv_step2 = subdiv_step * subdiv_step;
		float subdiv_step3 = subdiv_step * subdiv_step * subdiv_step;

		float pre1 = 3 * subdiv_step;
		float pre2 = 3 * subdiv_step2;
		float pre4 = 6 * subdiv_step2;
		float pre5 = 6 * subdiv_step3;

		float tmp1x = x1 - cx1 * 2 + cx2;
		float tmp1y = y1 - cy1 * 2 + cy2;

		float tmp2x = (cx1 - cx2) * 3 - x1 + x2;
		float tmp2y = (cy1 - cy2) * 3 - y1 + y2;

		float fx = x1;
		float fy = y1;

		float dfx = (cx1 - x1) * pre1 + tmp1x * pre2 + tmp2x * subdiv_step3;
		float dfy = (cy1 - y1) * pre1 + tmp1y * pre2 + tmp2y * subdiv_step3;

		float ddfx = tmp1x * pre4 + tmp2x * pre5;
		float ddfy = tmp1y * pre4 + tmp2y * pre5;

		float dddfx = tmp2x * pre5;
		float dddfy = tmp2y * pre5;

		while (segments-- > 0) {
			color(colorBits);
			vertex(fx, fy, 0);
			fx += dfx;
			fy += dfy;
			dfx += ddfx;
			dfy += ddfy;
			ddfx += dddfx;
			ddfy += dddfy;
			color(colorBits);
			vertex(fx, fy, 0);
		}
		color(colorBits);
		vertex(fx, fy, 0);
		color(colorBits);
		vertex(x2, y2, 0);
	}

	/** Draws a triangle in x/y plane using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void triangle (float x1, float y1, float x2, float y2, float x3, float y3) {
		check(ShapeType.Line, ShapeType.Filled, 6);
		float colorBits = color.toFloatBits();
		if (shapeType == ShapeType.Line) {
			color(colorBits);
			vertex(x1, y1, 0);
			color(colorBits);
			vertex(x2, y2, 0);

			color(colorBits);
			vertex(x2, y2, 0);
			color(colorBits);
			vertex(x3, y3, 0);

			color(colorBits);
			vertex(x3, y3, 0);
			color(colorBits);
			vertex(x1, y1, 0);
		} else {
			color(colorBits);
			vertex(x1, y1, 0);
			color(colorBits);
			vertex(x2, y2, 0);
			color(colorBits);
			vertex(x3, y3, 0);
		}
	}

	/** Draws a triangle in x/y plane with colored corners using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void triangle (float x1, float y1, float x2, float y2, float x3, float y3, Color col1, Color col2, Color col3) {
		check(ShapeType.Line, ShapeType.Filled, 6);
		if (shapeType == ShapeType.Line) {
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x1, y1, 0);
			color(col2.r, col2.g, col2.b, col2.a);
			vertex(x2, y2, 0);

			color(col2.r, col2.g, col2.b, col2.a);
			vertex(x2, y2, 0);
			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x3, y3, 0);

			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x3, y3, 0);
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x1, y1, 0);
		} else {
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x1, y1, 0);
			color(col2.r, col2.g, col2.b, col2.a);
			vertex(x2, y2, 0);
			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x3, y3, 0);
		}
	}

	/** Draws a rectangle in the x/y plane using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void rect (float x, float y, float width, float height) {
		check(ShapeType.Line, ShapeType.Filled, 8);
		float colorBits = color.toFloatBits();
		if (shapeType == ShapeType.Line) {
			color(colorBits);
			vertex(x, y, 0);
			color(colorBits);
			vertex(x + width, y, 0);

			color(colorBits);
			vertex(x + width, y, 0);
			color(colorBits);
			vertex(x + width, y + height, 0);

			color(colorBits);
			vertex(x + width, y + height, 0);
			color(colorBits);
			vertex(x, y + height, 0);

			color(colorBits);
			vertex(x, y + height, 0);
			color(colorBits);
			vertex(x, y, 0);
		} else {
			color(colorBits);
			vertex(x, y, 0);
			color(colorBits);
			vertex(x + width, y, 0);
			color(colorBits);
			vertex(x + width, y + height, 0);

			color(colorBits);
			vertex(x + width, y + height, 0);
			color(colorBits);
			vertex(x, y + height, 0);
			color(colorBits);
			vertex(x, y, 0);
		}
	}

	/** Draws a rectangle in the x/y plane using {@link ShapeType#Line} or {@link ShapeType#Filled}. The x and y specify the lower
	 * left corner.
	 * @param col1 The color at (x, y).
	 * @param col2 The color at (x + width, y).
	 * @param col3 The color at (x + width, y + height).
	 * @param col4 The color at (x, y + height). */
	public void rect (float x, float y, float width, float height, Color col1, Color col2, Color col3, Color col4) {
		check(ShapeType.Line, ShapeType.Filled, 8);

		if (shapeType == ShapeType.Line) {
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x, y, 0);
			color(col2.r, col2.g, col2.b, col2.a);
			vertex(x + width, y, 0);

			color(col2.r, col2.g, col2.b, col2.a);
			vertex(x + width, y, 0);
			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x + width, y + height, 0);

			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x + width, y + height, 0);
			color(col4.r, col4.g, col4.b, col4.a);
			vertex(x, y + height, 0);

			color(col4.r, col4.g, col4.b, col4.a);
			vertex(x, y + height, 0);
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x, y, 0);
		} else {
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x, y, 0);
			color(col2.r, col2.g, col2.b, col2.a);
			vertex(x + width, y, 0);
			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x + width, y + height, 0);

			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x + width, y + height, 0);
			color(col4.r, col4.g, col4.b, col4.a);
			vertex(x, y + height, 0);
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x, y, 0);
		}
	}

	/** Draws a rectangle in the x/y plane using {@link ShapeType#Line} or {@link ShapeType#Filled}. The x and y specify the lower
	 * left corner. The originX and originY specify the point about which to rotate the rectangle. */
	public void rect (float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY,
		float degrees) {
		rect(x, y, originX, originY, width, height, scaleX, scaleY, degrees, color, color, color, color);
	}

	/** Draws a rectangle in the x/y plane using {@link ShapeType#Line} or {@link ShapeType#Filled}. The x and y specify the lower
	 * left corner. The originX and originY specify the point about which to rotate the rectangle.
	 * @param col1 The color at (x, y)
	 * @param col2 The color at (x + width, y)
	 * @param col3 The color at (x + width, y + height)
	 * @param col4 The color at (x, y + height) */
	public void rect (float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY,
		float degrees, Color col1, Color col2, Color col3, Color col4) {
		check(ShapeType.Line, ShapeType.Filled, 8);

		float cos = MathUtils.cosDeg(degrees);
		float sin = MathUtils.sinDeg(degrees);
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		float worldOriginX = x + originX;
		float worldOriginY = y + originY;

		float x1 = cos * fx - sin * fy + worldOriginX;
		float y1 = sin * fx + cos * fy + worldOriginY;

		float x2 = cos * fx2 - sin * fy + worldOriginX;
		float y2 = sin * fx2 + cos * fy + worldOriginY;

		float x3 = cos * fx2 - sin * fy2 + worldOriginX;
		float y3 = sin * fx2 + cos * fy2 + worldOriginY;

		float x4 = x1 + (x3 - x2);
		float y4 = y3 - (y2 - y1);

		if (shapeType == ShapeType.Line) {
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x1, y1, 0);
			color(col2.r, col2.g, col2.b, col2.a);
			vertex(x2, y2, 0);

			color(col2.r, col2.g, col2.b, col2.a);
			vertex(x2, y2, 0);
			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x3, y3, 0);

			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x3, y3, 0);
			color(col4.r, col4.g, col4.b, col4.a);
			vertex(x4, y4, 0);

			color(col4.r, col4.g, col4.b, col4.a);
			vertex(x4, y4, 0);
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x1, y1, 0);
		} else {
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x1, y1, 0);
			color(col2.r, col2.g, col2.b, col2.a);
			vertex(x2, y2, 0);
			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x3, y3, 0);

			color(col3.r, col3.g, col3.b, col3.a);
			vertex(x3, y3, 0);
			color(col4.r, col4.g, col4.b, col4.a);
			vertex(x4, y4, 0);
			color(col1.r, col1.g, col1.b, col1.a);
			vertex(x1, y1, 0);
		}

	}

	/** Draws a line using a rotated rectangle, where with one edge is centered at x1, y1 and the opposite edge centered at x2, y2. */
	public void rectLine (float x1, float y1, float x2, float y2, float width) {
		check(ShapeType.Line, ShapeType.Filled, 8);
		float colorBits = color.toFloatBits();
		Vector2 t = tmp.set(y2 - y1, x1 - x2).nor();
		width *= 0.5f;
		float tx = t.x * width;
		float ty = t.y * width;
		if (shapeType == ShapeType.Line) {
			color(colorBits);
			vertex(x1 + tx, y1 + ty, 0);
			color(colorBits);
			vertex(x1 - tx, y1 - ty, 0);

			color(colorBits);
			vertex(x2 + tx, y2 + ty, 0);
			color(colorBits);
			vertex(x2 - tx, y2 - ty, 0);

			color(colorBits);
			vertex(x2 + tx, y2 + ty, 0);
			color(colorBits);
			vertex(x1 + tx, y1 + ty, 0);

			color(colorBits);
			vertex(x2 - tx, y2 - ty, 0);
			color(colorBits);
			vertex(x1 - tx, y1 - ty, 0);
		} else {
			color(colorBits);
			vertex(x1 + tx, y1 + ty, 0);
			color(colorBits);
			vertex(x1 - tx, y1 - ty, 0);
			color(colorBits);
			vertex(x2 + tx, y2 + ty, 0);

			color(colorBits);
			vertex(x2 - tx, y2 - ty, 0);
			color(colorBits);
			vertex(x2 + tx, y2 + ty, 0);
			color(colorBits);
			vertex(x1 - tx, y1 - ty, 0);
		}
	}
	
	/** Draws a line using a rotated rectangle, where with one edge is centered at x1, y1 and the opposite edge centered at x2, y2. */
	public void rectLine (float x1, float y1, float x2, float y2, float width, Color c1, Color c2) {
		check(ShapeType.Line, ShapeType.Filled, 8);
		float col1Bits = c1.toFloatBits();
		float col2Bits = c2.toFloatBits();
		Vector2 t = tmp.set(y2 - y1, x1 - x2).nor();
		width *= 0.5f;
		float tx = t.x * width;
		float ty = t.y * width;
		if (shapeType == ShapeType.Line) {
			color(col1Bits);
			vertex(x1 + tx, y1 + ty, 0);
			color(col1Bits);
			vertex(x1 - tx, y1 - ty, 0);

			color(col2Bits);
			vertex(x2 + tx, y2 + ty, 0);
			color(col2Bits);
			vertex(x2 - tx, y2 - ty, 0);

			color(col2Bits);
			vertex(x2 + tx, y2 + ty, 0);
			color(col1Bits);
			vertex(x1 + tx, y1 + ty, 0);

			color(col2Bits);
			vertex(x2 - tx, y2 - ty, 0);
			color(col1Bits);
			vertex(x1 - tx, y1 - ty, 0);
		} else {
			color(col1Bits);
			vertex(x1 + tx, y1 + ty, 0);
			color(col1Bits);
			vertex(x1 - tx, y1 - ty, 0);
			color(col2Bits);
			vertex(x2 + tx, y2 + ty, 0);

			color(col2Bits);
			vertex(x2 - tx, y2 - ty, 0);
			color(col2Bits);
			vertex(x2 + tx, y2 + ty, 0);
			color(col1Bits);
			vertex(x1 - tx, y1 - ty, 0);
		}
	}

	/** @see #rectLine(float, float, float, float, float) */
	public void rectLine (Vector2 p1, Vector2 p2, float width) {
		rectLine(p1.x, p1.y, p2.x, p2.y, width);
	}

	/** Draws a cube using {@link ShapeType#Line} or {@link ShapeType#Filled}. The x, y and z specify the bottom, left, front corner
	 * of the rectangle. */
	public void box (float x, float y, float z, float width, float height, float depth) {
		depth = -depth;
		float colorBits = color.toFloatBits();
		if (shapeType == ShapeType.Line) {
			check(ShapeType.Line, ShapeType.Filled, 24);

			color(colorBits);
			vertex(x, y, z);
			color(colorBits);
			vertex(x + width, y, z);

			color(colorBits);
			vertex(x + width, y, z);
			color(colorBits);
			vertex(x + width, y, z + depth);

			color(colorBits);
			vertex(x + width, y, z + depth);
			color(colorBits);
			vertex(x, y, z + depth);

			color(colorBits);
			vertex(x, y, z + depth);
			color(colorBits);
			vertex(x, y, z);

			color(colorBits);
			vertex(x, y, z);
			color(colorBits);
			vertex(x, y + height, z);

			color(colorBits);
			vertex(x, y + height, z);
			color(colorBits);
			vertex(x + width, y + height, z);

			color(colorBits);
			vertex(x + width, y + height, z);
			color(colorBits);
			vertex(x + width, y + height, z + depth);

			color(colorBits);
			vertex(x + width, y + height, z + depth);
			color(colorBits);
			vertex(x, y + height, z + depth);

			color(colorBits);
			vertex(x, y + height, z + depth);
			color(colorBits);
			vertex(x, y + height, z);

			color(colorBits);
			vertex(x + width, y, z);
			color(colorBits);
			vertex(x + width, y + height, z);

			color(colorBits);
			vertex(x + width, y, z + depth);
			color(colorBits);
			vertex(x + width, y + height, z + depth);

			color(colorBits);
			vertex(x, y, z + depth);
			color(colorBits);
			vertex(x, y + height, z + depth);
		} else {
			check(ShapeType.Line, ShapeType.Filled, 36);

			// Front
			color(colorBits);
			vertex(x, y, z);
			color(colorBits);
			vertex(x + width, y, z);
			color(colorBits);
			vertex(x + width, y + height, z);

			color(colorBits);
			vertex(x, y, z);
			color(colorBits);
			vertex(x + width, y + height, z);
			color(colorBits);
			vertex(x, y + height, z);

			// Back
			color(colorBits);
			vertex(x + width, y, z + depth);
			color(colorBits);
			vertex(x, y, z + depth);
			color(colorBits);
			vertex(x + width, y + height, z + depth);

			color(colorBits);
			vertex(x + width, y + height, z + depth);
			color(colorBits);
			vertex(x, y, z + depth);
			color(colorBits);
			vertex(x, y + height, z + depth);

			// Left
			color(colorBits);
			vertex(x, y, z + depth);
			color(colorBits);
			vertex(x, y, z);
			color(colorBits);
			vertex(x, y + height, z);

			color(colorBits);
			vertex(x, y, z + depth);
			color(colorBits);
			vertex(x, y + height, z);
			color(colorBits);
			vertex(x, y + height, z + depth);

			// Right
			color(colorBits);
			vertex(x + width, y, z);
			color(colorBits);
			vertex(x + width, y, z + depth);
			color(colorBits);
			vertex(x + width, y + height, z + depth);

			color(colorBits);
			vertex(x + width, y, z);
			color(colorBits);
			vertex(x + width, y + height, z + depth);
			color(colorBits);
			vertex(x + width, y + height, z);

			// Top
			color(colorBits);
			vertex(x, y + height, z);
			color(colorBits);
			vertex(x + width, y + height, z);
			color(colorBits);
			vertex(x + width, y + height, z + depth);

			color(colorBits);
			vertex(x, y + height, z);
			color(colorBits);
			vertex(x + width, y + height, z + depth);
			color(colorBits);
			vertex(x, y + height, z + depth);

			// Bottom
			color(colorBits);
			vertex(x, y, z + depth);
			color(colorBits);
			vertex(x + width, y, z + depth);
			color(colorBits);
			vertex(x + width, y, z);

			color(colorBits);
			vertex(x, y, z + depth);
			color(colorBits);
			vertex(x + width, y, z);
			color(colorBits);
			vertex(x, y, z);
		}

	}

	/** Draws two crossed lines using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void x (float x, float y, float size) {
		line(x - size, y - size, x + size, y + size);
		line(x - size, y + size, x + size, y - size);
	}

	/** @see #x(float, float, float) */
	public void x (Vector2 p, float size) {
		x(p.x, p.y, size);
	}

	/** Calls {@link #arc(float, float, float, float, float, int)} by estimating the number of segments needed for a smooth arc. */
	public void arc (float x, float y, float radius, float start, float degrees) {
		arc(x, y, radius, start, degrees, Math.max(1, (int)(6 * (float)Math.cbrt(radius) * (degrees / 360.0f))));
	}

	/** Draws an arc using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void arc (float x, float y, float radius, float start, float degrees, int segments) {
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		float colorBits = color.toFloatBits();
		float theta = (2 * MathUtils.PI * (degrees / 360.0f)) / segments;
		float cos = MathUtils.cos(theta);
		float sin = MathUtils.sin(theta);
		float cx = radius * MathUtils.cos(start * MathUtils.degreesToRadians);
		float cy = radius * MathUtils.sin(start * MathUtils.degreesToRadians);

		if (shapeType == ShapeType.Line) {
			check(ShapeType.Line, ShapeType.Filled, segments * 2 + 2);

			color(colorBits);
			vertex(x, y, 0);
			color(colorBits);
			vertex(x + cx, y + cy, 0);
			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				color(colorBits);
				vertex(x + cx, y + cy, 0);
			}
			color(colorBits);
			vertex(x + cx, y + cy, 0);
		} else {
			check(ShapeType.Line, ShapeType.Filled, segments * 3 + 3);

			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(x, y, 0);
				color(colorBits);
				vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				color(colorBits);
				vertex(x + cx, y + cy, 0);
			}
			color(colorBits);
			vertex(x, y, 0);
			color(colorBits);
			vertex(x + cx, y + cy, 0);
		}

		float temp = cx;
		cx = 0;
		cy = 0;
		color(colorBits);
		vertex(x + cx, y + cy, 0);
	}

	/** Calls {@link #circle(float, float, float, int)} by estimating the number of segments needed for a smooth circle. */
	public void circle (float x, float y, float radius) {
		circle(x, y, radius, Math.max(1, (int)(6 * (float)Math.cbrt(radius))));
	}

	/** Draws a circle using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void circle (float x, float y, float radius, int segments) {
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		float colorBits = color.toFloatBits();
		float angle = 2 * MathUtils.PI / segments;
		float cos = MathUtils.cos(angle);
		float sin = MathUtils.sin(angle);
		float cx = radius, cy = 0;
		if (shapeType == ShapeType.Line) {
			check(ShapeType.Line, ShapeType.Filled, segments * 2 + 2);
			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				color(colorBits);
				vertex(x + cx, y + cy, 0);
			}
			// Ensure the last segment is identical to the first.
			color(colorBits);
			vertex(x + cx, y + cy, 0);
		} else {
			check(ShapeType.Line, ShapeType.Filled, segments * 3 + 3);
			segments--;
			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(x, y, 0);
				color(colorBits);
				vertex(x + cx, y + cy, 0);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				color(colorBits);
				vertex(x + cx, y + cy, 0);
			}
			// Ensure the last segment is identical to the first.
			color(colorBits);
			vertex(x, y, 0);
			color(colorBits);
			vertex(x + cx, y + cy, 0);
		}

		float temp = cx;
		cx = radius;
		cy = 0;
		color(colorBits);
		vertex(x + cx, y + cy, 0);
	}

	/** Calls {@link #ellipse(float, float, float, float, int)} by estimating the number of segments needed for a smooth ellipse. */
	public void ellipse (float x, float y, float width, float height) {
		ellipse(x, y, width, height, Math.max(1, (int)(12 * (float)Math.cbrt(Math.max(width * 0.5f, height * 0.5f)))));
	}

	/** Draws an ellipse using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void ellipse (float x, float y, float width, float height, int segments) {
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		check(ShapeType.Line, ShapeType.Filled, segments * 3);
		float colorBits = color.toFloatBits();
		float angle = 2 * MathUtils.PI / segments;

		float cx = x + width / 2, cy = y + height / 2;
		if (shapeType == ShapeType.Line) {
			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(cx + (width * 0.5f * MathUtils.cos(i * angle)), cy + (height * 0.5f * MathUtils.sin(i * angle)), 0);

				color(colorBits);
				vertex(cx + (width * 0.5f * MathUtils.cos((i + 1) * angle)),
					cy + (height * 0.5f * MathUtils.sin((i + 1) * angle)), 0);
			}
		} else {
			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(cx + (width * 0.5f * MathUtils.cos(i * angle)), cy + (height * 0.5f * MathUtils.sin(i * angle)), 0);

				color(colorBits);
				vertex(cx, cy, 0);

				color(colorBits);
				vertex(cx + (width * 0.5f * MathUtils.cos((i + 1) * angle)),
					cy + (height * 0.5f * MathUtils.sin((i + 1) * angle)), 0);
			}
		}
	}
	
	/** Calls {@link #ellipse(float, float, float, float, float, int)} by estimating the number of segments needed for a smooth ellipse. */
	public void ellipse (float x, float y, float width, float height, float rotation) {
		ellipse(x, y, width, height, rotation, Math.max(1, (int)(12 * (float)Math.cbrt(Math.max(width * 0.5f, height * 0.5f)))));
	}

	/** Draws an ellipse using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void ellipse (float x, float y, float width, float height, float rotation, int segments) {
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		check(ShapeType.Line, ShapeType.Filled, segments * 3);
		float colorBits = color.toFloatBits();
		float angle = 2 * MathUtils.PI / segments;
		
		rotation = MathUtils.PI * rotation / 180f;
		float sin = MathUtils.sin(rotation);
		float cos = MathUtils.cos(rotation);

		float cx = x + width / 2, cy = y + height / 2;
		float x1 = width * 0.5f;
		float y1 = 0;
		if (shapeType == ShapeType.Line) {
			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(cx + cos * x1 - sin * y1, cy + sin * x1 + cos * y1, 0);
				
				x1 = (width * 0.5f * MathUtils.cos((i + 1) * angle));
				y1 = (height * 0.5f * MathUtils.sin((i + 1) * angle));

				color(colorBits);
				vertex(cx + cos * x1 - sin * y1, cy + sin * x1 + cos * y1, 0);
			}
		} else {
			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(cx + cos * x1 - sin * y1, cy + sin * x1 + cos * y1, 0);

				color(colorBits);
				vertex(cx, cy, 0);
				
				x1 = (width * 0.5f * MathUtils.cos((i + 1) * angle));
				y1 = (height * 0.5f * MathUtils.sin((i + 1) * angle));

				color(colorBits);
				vertex(cx + cos * x1 - sin * y1, cy + sin * x1 + cos * y1, 0);
			}
		}
	}

	/** Calls {@link #cone(float, float, float, float, float, int)} by estimating the number of segments needed for a smooth
	 * circular base. */
	public void cone (float x, float y, float z, float radius, float height) {
		cone(x, y, z, radius, height, Math.max(1, (int)(4 * (float)Math.sqrt(radius))));
	}

	/** Draws a cone using {@link ShapeType#Line} or {@link ShapeType#Filled}. */
	public void cone (float x, float y, float z, float radius, float height, int segments) {
		if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
		check(ShapeType.Line, ShapeType.Filled, segments * 4 + 2);
		float colorBits = color.toFloatBits();
		float angle = 2 * MathUtils.PI / segments;
		float cos = MathUtils.cos(angle);
		float sin = MathUtils.sin(angle);
		float cx = radius, cy = 0;
		if (shapeType == ShapeType.Line) {
			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(x + cx, y + cy, z);
				color(colorBits);
				vertex(x, y, z + height);
				color(colorBits);
				vertex(x + cx, y + cy, z);
				float temp = cx;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				color(colorBits);
				vertex(x + cx, y + cy, z);
			}
			// Ensure the last segment is identical to the first.
			color(colorBits);
			vertex(x + cx, y + cy, z);
		} else {
			segments--;
			for (int i = 0; i < segments; i++) {
				color(colorBits);
				vertex(x, y, z);
				color(colorBits);
				vertex(x + cx, y + cy, z);
				float temp = cx;
				float temp2 = cy;
				cx = cos * cx - sin * cy;
				cy = sin * temp + cos * cy;
				color(colorBits);
				vertex(x + cx, y + cy, z);

				color(colorBits);
				vertex(x + temp, y + temp2, z);
				color(colorBits);
				vertex(x + cx, y + cy, z);
				color(colorBits);
				vertex(x, y, z + height);
			}
			// Ensure the last segment is identical to the first.
			color(colorBits);
			vertex(x, y, z);
			color(colorBits);
			vertex(x + cx, y + cy, z);
		}
		float temp = cx;
		float temp2 = cy;
		cx = radius;
		cy = 0;
		color(colorBits);
		vertex(x + cx, y + cy, z);
		if (shapeType != ShapeType.Line) {
			color(colorBits);
			vertex(x + temp, y + temp2, z);
			color(colorBits);
			vertex(x + cx, y + cy, z);
			color(colorBits);
			vertex(x, y, z + height);
		}
	}

	/** Draws a polygon in the x/y plane using {@link ShapeType#Line}. The vertices must contain at least 3 points (6 floats x,y). */
	public void polygon (float[] vertices, int offset, int count) {
		if (count < 6) throw new IllegalArgumentException("Polygons must contain at least 3 points.");
		if (count % 2 != 0) throw new IllegalArgumentException("Polygons must have an even number of vertices.");

		check(ShapeType.Line, null, count);
		float colorBits = color.toFloatBits();
		float firstX = vertices[0];
		float firstY = vertices[1];

		for (int i = offset, n = offset + count; i < n; i += 2) {
			float x1 = vertices[i];
			float y1 = vertices[i + 1];

			float x2;
			float y2;

			if (i + 2 >= count) {
				x2 = firstX;
				y2 = firstY;
			} else {
				x2 = vertices[i + 2];
				y2 = vertices[i + 3];
			}

			color(colorBits);
			vertex(x1, y1, 0);
			color(colorBits);
			vertex(x2, y2, 0);
		}
	}

	/** @see #polygon(float[], int, int) */
	public void polygon (float[] vertices) {
		polygon(vertices, 0, vertices.length);
	}

	/** Draws a polyline in the x/y plane using {@link ShapeType#Line}. The vertices must contain at least 2 points (4 floats x,y). */
	public void polyline (float[] vertices, int offset, int count) {
		if (count < 4) throw new IllegalArgumentException("Polylines must contain at least 2 points.");
		if (count % 2 != 0) throw new IllegalArgumentException("Polylines must have an even number of vertices.");

		check(ShapeType.Line, null, count);
		float colorBits = color.toFloatBits();
		for (int i = offset, n = offset + count - 2; i < n; i += 2) {
			float x1 = vertices[i];
			float y1 = vertices[i + 1];

			float x2;
			float y2;

			x2 = vertices[i + 2];
			y2 = vertices[i + 3];

			color(colorBits);
			vertex(x1, y1, 0);
			color(colorBits);
			vertex(x2, y2, 0);
		}
	}

	/** @see #polyline(float[], int, int) */
	public void polyline (float[] vertices) {
		polyline(vertices, 0, vertices.length);
	}

	/** @param other May be null. */
	private void check (ShapeType preferred, ShapeType other, int newVertices) {
		if (shapeType == null) throw new IllegalStateException("begin must be called first.");

		if (shapeType != preferred && shapeType != other) {
			// Shape type is not valid.
			if (!autoShapeType) {
				if (other == null)
					throw new IllegalStateException("Must call begin(ShapeType." + preferred + ").");
				else
					throw new IllegalStateException("Must call begin(ShapeType." + preferred + ") or begin(ShapeType." + other + ").");
			}
			end();
			begin(preferred);
		} else if (matrixDirty) {
			// Matrix has been changed.
			ShapeType type = shapeType;
			end();
			begin(type);
		} else if (getMaxVertices() - getNumVertices() < newVertices) {
			// Not enough space.
			ShapeType type = shapeType;
			end();
			begin(type);
		}
	}

	public void flush () {
		ShapeType type = shapeType;
		if (type == null) return;
		end();
		begin(type);
	}
	
	public void end () {
		shapeType = null;
		if (numVertices == 0) return;
		shader.bind();

		if (matrixDirty) {
			combinedMatrix.set(projectionMatrix);
			combinedMatrix.mul(transformMatrix);
			shader.setUniformMatrix("u_projModelView", combinedMatrix);
			matrixDirty = false;
		}
		mesh.setVertices(vertices, 0, vertexIdx);
		mesh.render(shader, primitiveType);

		vertexIdx = 0;
		numVertices = 0;
	}

	/** Returns the current shape type. */
	public ShapeType getCurrentType () {
		return shapeType;
	}

	/** @return true if currently between begin and end. */
	public boolean isDrawing () {
		return shapeType != null;
	}

	@Override
	public void dispose () {
		shader.dispose();
		mesh.dispose();
	}
}
