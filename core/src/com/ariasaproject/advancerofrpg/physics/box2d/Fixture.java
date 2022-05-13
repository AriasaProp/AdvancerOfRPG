package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Shape.Type;

public class Fixture {
	private Body body;
	protected long addr; // used in native
	protected Shape shape;
	protected Object userData;
	private final Filter filter = new Filter();
	private boolean dirtyFilter = true;
	static native void initialize();
	static {
		initialize();
	}
	protected Fixture(Body body, long addr) {
		this.body = body;
		this.addr = addr;
	}

	protected void reset(Body body, long addr) {
		this.body = body;
		this.addr = addr;
		this.shape = null;
		this.userData = null;
		this.dirtyFilter = true;
	}
	public Type getType() {
		int type = jniGetType();
		switch (type) {
			case 0:
				return Type.Circle;
			case 1:
				return Type.Edge;
			case 2:
				return Type.Polygon;
			case 3:
				return Type.Chain;
			default:
				throw new RuntimeException("Unknown shape type!");
		}
	}
	private native int jniGetType();
	private final long[] shapeTmp = new long[2];
	public Shape getShape() {
		if (shape == null) {
			jniGetShape(shapeTmp);
			if (shapeTmp[0] == 0) throw new RuntimeException("Null shape address!");
			switch ((int)shapeTmp[1]) {
				case 0:
					shape = new CircleShape(shapeTmp[0]);
					break;
				case 1:
					shape = new EdgeShape(shapeTmp[0]);
					break;
				case 2:
					shape = new PolygonShape(shapeTmp[0]);
					break;
				case 3:
					shape = new ChainShape(shapeTmp[0]);
					break;
				default:
					throw new RuntimeException("Unknown shape type!");
			}
		}

		return shape;
	}
	private native long jniGetShape(long[] tmp);
	public native void setSensor(boolean sensor);
	public native boolean isSensor();
	public void setFilterData(Filter filter) {
		jniSetFilterData(filter.categoryBits, filter.maskBits, filter.groupIndex);
		this.filter.set(filter);
		dirtyFilter = false;
	}

	private native void jniSetFilterData(short categoryBits, short maskBits, short groupIndex);
	private final short[] tmp = new short[3];

	public Filter getFilterData() {
		if (dirtyFilter) {
			jniGetFilterData(tmp);
			filter.maskBits = tmp[0];
			filter.categoryBits = tmp[1];
			filter.groupIndex = tmp[2];
			dirtyFilter = false;
		}
		return filter;
	}

	private native void jniGetFilterData(short[] filter);

	public native void refilter();
	public Body getBody() {
		return body;
	}
	public boolean testPoint(Vector2 p) {
		return testPoint(p.x, p.y);
	}
	public native boolean testPoint(float x, float y);

// const b2Body* GetBody() const;
//
// /// Get the next fixture in the parent body's fixture list.
// /// @return the next shape.
// b2Fixture* GetNext();
// const b2Fixture* GetNext() const;
//
// /// Get the user data that was assigned in the fixture definition. Use this to
// /// store your application specific data.
// void* GetUserData() const;
//
// /// Set the user data. Use this to store your application specific data.
// void SetUserData(void* data);
//
// /// Cast a ray against this shape.
// /// @param output the ray-cast results.
// /// @param input the ray-cast input parameters.
// bool RayCast(b2RayCastOutput* output, const b2RayCastInput& input) const;
//
// /// Get the mass data for this fixture. The mass data is based on the density and
// /// the shape. The rotational inertia is about the shape's origin. This operation
// /// may be expensive.
// void GetMassData(b2MassData* massData) const;

	public native void setDensity(float density);
	public native float getDensity(); 
	public native float getFriction();
	public native void setFriction(float friction);
	public native float getRestitution();
	public native void setRestitution(float restitution);
// /// Get the fixture's AABB. This AABB may be enlarge and/or stale.
// /// If you need a more accurate AABB, compute it using the shape and
// /// the body transform.
// const b2AABB& GetAABB() const;

	public void setUserData(Object userData) {
		this.userData = userData;
	}
	public Object getUserData() {
		return userData;
	}
}
