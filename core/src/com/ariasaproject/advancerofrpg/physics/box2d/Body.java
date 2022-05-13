package com.ariasaproject.advancerofrpg.physics.box2d;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.BodyDef.BodyType;
import com.ariasaproject.advancerofrpg.utils.Array;

public class Body {
	protected long bodyPtr;
	private final float[] tmp = new float[4];
	private final World world;
	private Array<Fixture> fixtures = new Array<Fixture>(2);
	protected Array<JointEdge> joints = new Array<JointEdge>(2);
	private Object userData;

	private static final native void initialize();
    
	static {
	    initialize();
	}

	protected Body (World world, long bodyPtr) {
		this.world = world;
		this.bodyPtr = bodyPtr;
	}
	protected void reset (long bodyPtr) {
		this.bodyPtr = bodyPtr;
		this.userData = null;
		for (int i = 0; i < fixtures.size; i++)
			this.world.freeFixtures.free(fixtures.get(i));
		fixtures.clear();
		this.joints.clear();
	}

	public Fixture createFixture (FixtureDef def) {
		long fixturebodyPtr = jniCreateFixture(def.shape.addr, def.friction, def.restitution, def.density, def.isSensor,
			def.filter.categoryBits, def.filter.maskBits, def.filter.groupIndex);
		Fixture fixture = this.world.freeFixtures.obtain();
		fixture.reset(this, fixturebodyPtr);
		this.world.fixtures.put(fixture.addr, fixture);
		this.fixtures.add(fixture);
		return fixture;
	}
	private native long jniCreateFixture (long shape, float friction, float restitution, float density, boolean isSensor, short filterCategoryBits, short filterMaskBits, short filterGroupIndex);
	public Fixture createFixture (Shape shape, float density) {
		long fixturebodyPtr = jniCreateFixture(shape.addr,density);
		Fixture fixture = this.world.freeFixtures.obtain();
		fixture.reset(this, fixturebodyPtr);
		this.world.fixtures.put(fixture.addr, fixture);
		this.fixtures.add(fixture);
		return fixture;
	}
	private native long jniCreateFixture (long shape, float density);

	public void destroyFixture (Fixture fixture) {
		this.world.destroyFixture(this, fixture);
		fixture.setUserData(null);
		this.world.fixtures.remove(fixture.addr);
		this.fixtures.removeValue(fixture, true);
		this.world.freeFixtures.free(fixture);
	}
	public void setTransform (Vector2 position, float angle) {
		setTransform(position.x, position.y, angle);
	}
	public native void setTransform (float x, float y, float angle);
	private final Transform transform = new Transform();
	public Transform getTransform () {
		jniGetTransform(transform.vals);
		return transform;
	}
	private native void jniGetTransform (float[] vals);
	private final Vector2 position = new Vector2();
	public Vector2 getPosition () {
		jniGetPosition(tmp);
		position.x = tmp[0];
		position.y = tmp[1];
		return position;
	}

	private native void jniGetPosition (float[] position);
	public native float getAngle ();

	private final Vector2 worldCenter = new Vector2();

	public Vector2 getWorldCenter () {
		jniGetWorldCenter(tmp);
		worldCenter.x = tmp[0];
		worldCenter.y = tmp[1];
		return worldCenter;
	}

	private native void jniGetWorldCenter (float[] worldCenter);

	private final Vector2 localCenter = new Vector2();
	public Vector2 getLocalCenter () {
		jniGetLocalCenter(tmp);
		localCenter.x = tmp[0];
		localCenter.y = tmp[1];
		return localCenter;
	}

	private native void jniGetLocalCenter (float[] localCenter);
	public void setLinearVelocity (Vector2 v) {
		setLinearVelocity(v.x, v.y);
	}
	public native void setLinearVelocity (float x, float y);

	private final Vector2 linearVelocity = new Vector2();
	public Vector2 getLinearVelocity () {
		jniGetLinearVelocity(tmp);
		linearVelocity.x = tmp[0];
		linearVelocity.y = tmp[1];
		return linearVelocity;
	}

	private native void jniGetLinearVelocity (float[] linearVelocity);

	public native void setAngularVelocity (float omega);
	public native float getAngularVelocity (long bodyPtr);

	public void applyForce (Vector2 force, Vector2 point, boolean wake) {
		applyForce(force.x, force.y, point.x, point.y, wake);
	}
	public native void applyForce (float forceX, float forceY, float pointX, float pointY, boolean wake);

	public void applyForceToCenter (Vector2 force, boolean wake) {
		applyForceToCenter(force.x, force.y, wake);
	}
	public native void applyForceToCenter (float forceX, float forceY, boolean wake);
	public native void applyTorque (float torque, boolean wake);
	public void applyLinearImpulse (Vector2 impulse, Vector2 point, boolean wake) {
		applyLinearImpulse(impulse.x, impulse.y, point.x, point.y, wake);
	}
	public native void applyLinearImpulse (float impulseX, float impulseY, float pointX, float pointY, boolean wake);
	public native void applyAngularImpulse (float impulse, boolean wake);
	public native float getMass ();
	public native float getInertia ();

	private final MassData massData = new MassData();
	public MassData getMassData () {
		jniGetMassData(tmp);
		massData.mass = tmp[0];
		massData.center.x = tmp[1];
		massData.center.y = tmp[2];
		massData.I = tmp[3];
		return massData;
	}
	private native void jniGetMassData (float[] massData);
	public void setMassData (MassData data) {
		setMassData(data.mass, data.center.x, data.center.y, data.I);
	}
	public native void setMassData (float mass, float centerX, float centerY, float I);
	public native void resetMassData ();
	private final Vector2 localPoint = new Vector2();
	public Vector2 getWorldPoint (Vector2 localPoint) {
		jniGetWorldPoint(localPoint.x, localPoint.y, tmp);
		this.localPoint.x = tmp[0];
		this.localPoint.y = tmp[1];
		return this.localPoint;
	}
	private native void jniGetWorldPoint (float localPointX, float localPointY, float[] worldPoint);
	private final Vector2 worldVector = new Vector2();
	public Vector2 getWorldVector (Vector2 localVector) {
		jniGetWorldVector(localVector.x, localVector.y, tmp);
		worldVector.x = tmp[0];
		worldVector.y = tmp[1];
		return worldVector;
	}

	private native void jniGetWorldVector (float localVectorX, float localVectorY, float[] worldVector);
	public final Vector2 localPoint2 = new Vector2();
	public Vector2 getLocalPoint (Vector2 worldPoint) {
		jniGetLocalPoint(worldPoint.x, worldPoint.y, tmp);
		localPoint2.x = tmp[0];
		localPoint2.y = tmp[1];
		return localPoint2;
	}
	private native void jniGetLocalPoint (float worldPointX, float worldPointY, float[] localPoint);
	public final Vector2 localVector = new Vector2();
	public Vector2 getLocalVector (Vector2 worldVector) {
		jniGetLocalVector(worldVector.x, worldVector.y, tmp);
		localVector.x = tmp[0];
		localVector.y = tmp[1];
		return localVector;
	}
	private native void jniGetLocalVector (float worldVectorX, float worldVectorY, float[] worldVector);
	public final Vector2 linVelWorld = new Vector2();
	public Vector2 getLinearVelocityFromWorldPoint (Vector2 worldPoint) {
		jniGetLinearVelocityFromWorldPoint(worldPoint.x, worldPoint.y, tmp);
		linVelWorld.x = tmp[0];
		linVelWorld.y = tmp[1];
		return linVelWorld;
	}
	private native void jniGetLinearVelocityFromWorldPoint (float worldPointX, float worldPointY, float[] linVelWorld);
	public final Vector2 linVelLoc = new Vector2();
	public Vector2 getLinearVelocityFromLocalPoint (Vector2 localPoint) {
		jniGetLinearVelocityFromLocalPoint(localPoint.x, localPoint.y, tmp);
		linVelLoc.x = tmp[0];
		linVelLoc.y = tmp[1];
		return linVelLoc;
	}

	private native void jniGetLinearVelocityFromLocalPoint (float localPointX, float localPointY, float[] linVelLoc);
	public native float getLinearDamping ();
	public native void setLinearDamping (float linearDamping);
	public native float getAngularDamping ();
	public native void setAngularDamping (float angularDamping);
	public void setType (BodyType type) {
		jniSetType(type.getValue());
	}
	private native void jniSetType (int type);
	public BodyType getType () {
		int type = jniGetType();
		if (type == 0) return BodyType.StaticBody;
		if (type == 1) return BodyType.KinematicBody;
		if (type == 2) return BodyType.DynamicBody;
		return BodyType.StaticBody;
	}
	private native int jniGetType ();
	public native void setBullet (boolean flag);
	public native boolean isBullet ();
	public native void setSleepingAllowed (boolean flag);
	public native boolean isSleepingAllowed ();
	public native void setAwake (boolean flag);
	public native boolean isAwake ();
	public void setActive (boolean flag) {
		if (flag) {
			jniSetActive(flag);
		} else {
			this.world.deactivateBody(this);
		}
	}
	private native void jniSetActive (boolean flag);
	public native boolean isActive ();
	public native void setFixedRotation (boolean flag);
	public native boolean isFixedRotation ();
	public Array<Fixture> getFixtureList () {
		return fixtures;
	}
	public Array<JointEdge> getJointList () {
		return joints;
	}
	public native float getGravityScale ();
	public native void setGravityScale (float scale);
	public World getWorld () {
		return world;
	}
	public Object getUserData () {
		return userData;
	}
	public void setUserData (Object userData) {
		this.userData = userData;
	}
}
