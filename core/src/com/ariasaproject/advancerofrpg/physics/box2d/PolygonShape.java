package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;

public class PolygonShape extends Shape {
	public PolygonShape() {
		addr = newPolygonShape();
	}

	protected PolygonShape(long addr) {
		this.addr = addr;
	}

	private native long newPolygonShape();

	@Override
	public Type getType() {
		return Type.Polygon;
	}

	public void set(Vector2[] vertices) {
		float[] verts = new float[vertices.length * 2];
		for (int i = 0, j = 0; i < vertices.length * 2; i += 2, j++) {
			verts[i] = vertices[j].x;
			verts[i + 1] = vertices[j].y;
		}
		set(verts, 0, verts.length);
	}

	public void set(float[] vertices) {
		set(vertices, 0, vertices.length);
	}

	public native void set(float[] vertices, int offset, int len); /*
																	 * b2PolygonShape* poly = (b2PolygonShape*)addr; int
																	 * numVertices = len / 2; b2Vec2* verticesOut = new
																	 * b2Vec2[numVertices]; for(int i = 0; i <
																	 * numVertices; i++) { verticesOut[i] =
																	 * b2Vec2(verts[(i<<1) + offset], verts[(i<<1) +
																	 * offset + 1]); } poly->Set(verticesOut,
																	 * numVertices); delete[] verticesOut;
																	 */

	public native void setAsBox(float hx, float hy); /*
														 * b2PolygonShape* poly = (b2PolygonShape*)addr;
														 * poly->SetAsBox(hx, hy);
														 */

	public void setAsBox(float hx, float hy, Vector2 center, float angle) {
		setAsBox(hx, hy, center.x, center.y, angle);
	}

	public native void setAsBox(float hx, float hy, float cx, float cy, float angle); /*
																						 * b2PolygonShape* poly =
																						 * (b2PolygonShape*)addr;
																						 * poly->SetAsBox( hx, hy,
																						 * b2Vec2( centerX, centerY ),
																						 * angle );
																						 */

	public native int getVertexCount();/*
										 * b2PolygonShape* poly = (b2PolygonShape*)addr; return poly->GetVertexCount();
										 */

	private static float[] verts = new float[2];

	public void getVertex(int index, Vector2 vertex) {
		jniGetVertex(addr, index, verts);
		vertex.x = verts[0];
		vertex.y = verts[1];
	}

	private native void jniGetVertex(long addr, int index, float[] verts); /*
																			 * b2PolygonShape* poly =
																			 * (b2PolygonShape*)addr; const b2Vec2 v =
																			 * poly->GetVertex( index ); verts[0] = v.x;
																			 * verts[1] = v.y;
																			 */
}
