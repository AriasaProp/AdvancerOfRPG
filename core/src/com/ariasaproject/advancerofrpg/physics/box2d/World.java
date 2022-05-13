package com.ariasaproject.advancerofrpg.physics.box2d;

import java.util.Iterator;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.JointDef.JointType;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.DistanceJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.DistanceJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.FrictionJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.FrictionJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.GearJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.GearJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.MotorJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.MotorJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.MouseJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.MouseJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.PrismaticJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.PrismaticJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.PulleyJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.PulleyJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.RevoluteJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.RevoluteJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.RopeJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.RopeJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.WeldJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.WeldJointDef;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.WheelJoint;
import com.ariasaproject.advancerofrpg.physics.box2d.joints.WheelJointDef;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.LongMap;
import com.ariasaproject.advancerofrpg.utils.Pool;
public final class World implements Disposable {
	protected final Pool<Body> freeBodies = new Pool<Body>(100, 200) {
		@Override
		protected Body newObject () {
			return new Body(World.this, 0);
		}
	};
	protected final Pool<Fixture> freeFixtures = new Pool<Fixture>(100, 200) {
		@Override
		protected Fixture newObject () {
			return new Fixture(null, 0);
		}
	};
	protected long worldPtr;
	protected final LongMap<Body> bodies = new LongMap<Body>(100);
	protected final LongMap<Fixture> fixtures = new LongMap<Fixture>(100);
	protected final LongMap<Joint> joints = new LongMap<Joint>(100);
	protected ContactFilter contactFilter = null;
	protected ContactListener contactListener = null;

	private native static void initialize();

	static {
		initialize();
	}

	public World (Vector2 gravity, boolean doSleep) {
		worldPtr = newWorld(gravity.x, gravity.y, doSleep);

		contacts.ensureCapacity(contactworldPtrs.length);
		freeContacts.ensureCapacity(contactworldPtrs.length);

		for (int i = 0; i < contactworldPtrs.length; i++)
			freeContacts.add(new Contact(this, 0));
	}

	public native static long newWorld (float gravityX, float gravityY, boolean doSleep);
	public void setDestructionListener (DestructionListener listener) {

	}
	public void setContactFilter (ContactFilter filter) {
		this.contactFilter = filter;
		setUseDefaultContactFilter(filter == null);
	}
	private native void setUseDefaultContactFilter(boolean use);
	public void setContactListener (ContactListener listener) {
		this.contactListener = listener;
        }
	public Body createBody (BodyDef def) {
		long bodyworldPtr = jniCreateBody(def.type.getValue(), def.position.x, def.position.y, def.angle, def.linearVelocity.x,
			def.linearVelocity.y, def.angularVelocity, def.linearDamping, def.angularDamping, def.allowSleep, def.awake,
			def.fixedRotation, def.bullet, def.active, def.gravityScale);
		Body body = freeBodies.obtain();
		body.reset(bodyworldPtr);
		this.bodies.put(body.bodyPtr, body);
		return body;
	}

	private native long jniCreateBody (int type, float positionX, float positionY, float angle, float linearVelocityX,
		float linearVelocityY, float angularVelocity, float linearDamping, float angularDamping, boolean allowSleep, boolean awake,
		boolean fixedRotation, boolean bullet, boolean active, float inertiaScale);
	public void destroyBody (Body body) {
		Array<JointEdge> jointList = body.getJointList();
		while (jointList.size > 0)
			destroyJoint(body.getJointList().get(0).joint);
		jniDestroyBody(body.bodyPtr);
		body.setUserData(null);
		this.bodies.remove(body.bodyPtr);
		Array<Fixture> fixtureList = body.getFixtureList();
		while(fixtureList.size > 0) {
			Fixture fixtureToDelete = fixtureList.removeIndex(0);
 			this.fixtures.remove(fixtureToDelete.addr).setUserData(null);
 			freeFixtures.free(fixtureToDelete);
 		}

		freeBodies.free(body);
	}

	private native void jniDestroyBody (long bodyworldPtr);
	void destroyFixture(Body body, Fixture fixture) {
		jniDestroyFixture(body.bodyPtr, fixture.addr);
	}

	private native void jniDestroyFixture(long body, long fixture);
	void deactivateBody(Body body) {
		jniDeactivateBody(body.bodyPtr);
	}
	private native void jniDeactivateBody(long bodyworldPtr);
	public Joint createJoint (JointDef def) {
		long jointAddr = createProperJoint(def);
		Joint joint = null;
		if (def.type == JointType.DistanceJoint) joint = new DistanceJoint(this, jointAddr);
		if (def.type == JointType.FrictionJoint) joint = new FrictionJoint(this, jointAddr);
		if (def.type == JointType.GearJoint) joint = new GearJoint(this, jointAddr, ((GearJointDef) def).joint1, ((GearJointDef) def).joint2);
		if (def.type == JointType.MotorJoint) joint = new MotorJoint(this, jointAddr);
		if (def.type == JointType.MouseJoint) joint = new MouseJoint(this, jointAddr);
		if (def.type == JointType.PrismaticJoint) joint = new PrismaticJoint(this, jointAddr);
		if (def.type == JointType.PulleyJoint) joint = new PulleyJoint(this, jointAddr);
		if (def.type == JointType.RevoluteJoint) joint = new RevoluteJoint(this, jointAddr);
		if (def.type == JointType.RopeJoint) joint = new RopeJoint(this, jointAddr);
		if (def.type == JointType.WeldJoint) joint = new WeldJoint(this, jointAddr);
		if (def.type == JointType.WheelJoint) joint = new WheelJoint(this, jointAddr);
		if (joint == null) throw new RuntimeException("Unknown joint type: " + def.type);
		joints.put(joint.addr, joint);
		JointEdge jointEdgeA = new JointEdge(def.bodyB, joint);
		JointEdge jointEdgeB = new JointEdge(def.bodyA, joint);
		joint.jointEdgeA = jointEdgeA;
		joint.jointEdgeB = jointEdgeB;
		def.bodyA.joints.add(jointEdgeA);
		def.bodyB.joints.add(jointEdgeB);
		return joint;
	}

	private long createProperJoint (JointDef def) {
		if (def.type == JointType.DistanceJoint) {
			DistanceJointDef d = (DistanceJointDef)def;
			return jniCreateDistanceJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.length, d.frequencyHz, d.dampingRatio);
		}
		if (def.type == JointType.FrictionJoint) {
			FrictionJointDef d = (FrictionJointDef)def;
			return jniCreateFrictionJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.maxForce, d.maxTorque);
		}
		if (def.type == JointType.GearJoint) {
			GearJointDef d = (GearJointDef)def;
			return jniCreateGearJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.joint1.addr, d.joint2.addr ,d.ratio);
		}
		if (def.type == JointType.MotorJoint) {
			MotorJointDef d = (MotorJointDef)def;
			return jniCreateMotorJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.linearOffset.x, d.linearOffset.y,
				d.angularOffset, d.maxForce, d.maxTorque, d.correctionFactor);
		}
		if (def.type == JointType.MouseJoint) {
			MouseJointDef d = (MouseJointDef)def;
			return jniCreateMouseJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.target.x, d.target.y, d.maxForce,
				d.frequencyHz, d.dampingRatio);
		}
		if (def.type == JointType.PrismaticJoint) {
			PrismaticJointDef d = (PrismaticJointDef)def;
			return jniCreatePrismaticJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.localAxisA.x, d.localAxisA.y, d.referenceAngle, d.enableLimit,
				d.lowerTranslation, d.upperTranslation, d.enableMotor, d.maxMotorForce, d.motorSpeed);
		}
		if (def.type == JointType.PulleyJoint) {
			PulleyJointDef d = (PulleyJointDef)def;
			return jniCreatePulleyJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.groundAnchorA.x, d.groundAnchorA.y,
				d.groundAnchorB.x, d.groundAnchorB.y, d.localAnchorA.x, d.localAnchorA.y, d.localAnchorB.x, d.localAnchorB.y,
				d.lengthA, d.lengthB, d.ratio);

		}
		if (def.type == JointType.RevoluteJoint) {
			RevoluteJointDef d = (RevoluteJointDef)def;
			return jniCreateRevoluteJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.referenceAngle, d.enableLimit, d.lowerAngle, d.upperAngle, d.enableMotor,
				d.motorSpeed, d.maxMotorTorque);
		}
		if (def.type == JointType.RopeJoint) {
			RopeJointDef d = (RopeJointDef)def;
			return jniCreateRopeJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.maxLength);
		}
		if (def.type == JointType.WeldJoint) {
			WeldJointDef d = (WeldJointDef)def;
			return jniCreateWeldJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.referenceAngle, d.frequencyHz, d.dampingRatio);
		}
		if (def.type == JointType.WheelJoint) {
			WheelJointDef d = (WheelJointDef)def;
			return jniCreateWheelJoint(d.bodyA.bodyPtr, d.bodyB.bodyPtr, d.collideConnected, d.localAnchorA.x, d.localAnchorA.y,
				d.localAnchorB.x, d.localAnchorB.y, d.localAxisA.x, d.localAxisA.y, d.enableMotor, d.maxMotorTorque, d.motorSpeed,
				d.frequencyHz, d.dampingRatio);
		}

		return 0;
	}

	private native long jniCreateWheelJoint (long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,float localAnchorAY, float localAnchorBX, float localAnchorBY, float localAxisAX, float localAxisAY, boolean enableMotor,float maxMotorTorque, float motorSpeed, float frequencyHz, float dampingRatio);
	private native long jniCreateRopeJoint (long bodyA, long bodyB, boolean collideConnected, float localAnchorAX,float localAnchorAY, float localAnchorBX, float localAnchorBY, float maxLength);
	private native long jniCreateDistanceJoint (long bodyA, long bodyB, boolean collideConnected, float localAnchorAX, float localAnchorAY, float localAnchorBX, float localAnchorBY, float length, float frequencyHz, float dampingRatio);
	private native long jniCreateFrictionJoint (long bodyA, long bodyB, boolean collideConnected, float localAnchorAX, float localAnchorAY, float localAnchorBX, float localAnchorBY, float maxForce, float maxTorque);
	private native long jniCreateGearJoint (long bodyA, long bodyB, boolean collideConnected, long joint1, long joint2, float ratio);
	private native long jniCreateMotorJoint (long bodyA, long bodyB, boolean collideConnected, float linearOffsetX, float linearOffsetY, float angularOffset, float maxForce, float maxTorque, float correctionFactor);
	private native long jniCreateMouseJoint (long bodyA, long bodyB, boolean collideConnected, float targetX, float targetY, float maxForce, float frequencyHz, float dampingRatio);
	private native long jniCreatePrismaticJoint (long bodyA, long bodyB, boolean collideConnected, float localAnchorAX, float localAnchorAY, float localAnchorBX, float localAnchorBY, float localAxisAX, float localAxisAY, float referenceAngle, boolean enableLimit, float lowerTranslation, float upperTranslation, boolean enableMotor, float maxMotorForce, float motorSpeed);
	private native long jniCreatePulleyJoint (long bodyA, long bodyB, boolean collideConnected, float groundAnchorAX, float groundAnchorAY, float groundAnchorBX, float groundAnchorBY, float localAnchorAX, float localAnchorAY, float localAnchorBX, float localAnchorBY, float lengthA, float lengthB, float ratio);
	private native long jniCreateRevoluteJoint (long bodyA, long bodyB, boolean collideConnected, float localAnchorAX, float localAnchorAY, float localAnchorBX, float localAnchorBY, float referenceAngle, boolean enableLimit, float lowerAngle, float upperAngle, boolean enableMotor, float motorSpeed, float maxMotorTorque);
	private native long jniCreateWeldJoint (long bodyA, long bodyB, boolean collideConnected, float localAnchorAX, float localAnchorAY, float localAnchorBX, float localAnchorBY, float referenceAngle, float frequencyHz, float dampingRatio);
	public void destroyJoint (Joint joint) {
		joint.setUserData(null);
		joints.remove(joint.addr);
		joint.jointEdgeA.other.joints.removeValue(joint.jointEdgeB, true);
		joint.jointEdgeB.other.joints.removeValue(joint.jointEdgeA, true);
		jniDestroyJoint(joint.addr);
	}

	private native void jniDestroyJoint (long jointworldPtr);
	public native void step (float timeStep, int velocityIterations, int positionIterations);
	public native void clearForces ();
	public native void setWarmStarting (boolean flag);
	public native void setContinuousPhysics (boolean flag);
	public native int getProxyCount ();
	public native int getBodyCount ();
	public int getFixtureCount () {
		return fixtures.size;
	}
	public native int getJointCount ();
	public native int getContactCount ();
	public void setGravity (Vector2 gravity) {
		setGravity(gravity.x, gravity.y);
	}
	public native void setGravity (float x, float y);
	final float[] tmpGravity = new float[2];
	final Vector2 gravity = new Vector2();

	public Vector2 getGravity () {
		jniGetGravity(tmpGravity);
		gravity.x = tmpGravity[0];
		gravity.y = tmpGravity[1];
		return gravity;
	}
	private native void jniGetGravity (float[] gravity);
	public native boolean isLocked ();
	public native void setAutoClearForces (boolean flag);
	public native boolean getAutoClearForces ();
	public void QueryAABB (QueryCallback callback, float lowerX, float lowerY, float upperX, float upperY) {
		queryCallback = callback;
		jniQueryAABB(lowerX, lowerY, upperX, upperY);
	}
	private QueryCallback queryCallback = null;;
	private native void jniQueryAABB (float lowX, float lowY, float upX, float upY);
	private long[] contactworldPtrs = new long[200];
	private final Array<Contact> contacts = new Array<Contact>();
	private final Array<Contact> freeContacts = new Array<Contact>();
	public Array<Contact> getContactList () {
		int numContacts = getContactCount();
		if (numContacts > contactworldPtrs.length) {
			int newSize = 2 * numContacts;
			contactworldPtrs = new long[newSize];
			contacts.ensureCapacity(newSize);
			freeContacts.ensureCapacity(newSize);
		}
		if (numContacts > freeContacts.size) {
			int freeConts = freeContacts.size;
			for (int i = 0; i < numContacts - freeConts; i++)
				freeContacts.add(new Contact(this, 0));
		}
		jniGetContactList(contactworldPtrs);

		contacts.clear();
		for (int i = 0; i < numContacts; i++) {
			Contact contact = freeContacts.get(i);
			contact.addr = contactworldPtrs[i];
			contacts.add(contact);
		}

		return contacts;
	}

	public void getBodies (Array<Body> bodies) {
		bodies.clear();
		bodies.ensureCapacity(this.bodies.size);
		for (Iterator<Body> iter = this.bodies.values(); iter.hasNext();) {
			bodies.add(iter.next());
		}
	}

	public void getFixtures (Array<Fixture> fixtures) {
		fixtures.clear();
		fixtures.ensureCapacity(this.fixtures.size);
		for (Iterator<Fixture> iter = this.fixtures.values(); iter.hasNext();) {
			fixtures.add(iter.next());
		}
	}

	public void getJoints (Array<Joint> joints) {
		joints.clear();
		joints.ensureCapacity(this.joints.size);
		for (Iterator<Joint> iter = this.joints.values(); iter.hasNext();) {
			joints.add(iter.next());
		}
	}

	private native void jniGetContactList (long[] contacts);
	
	@Override
	public void dispose () {
		jniDispose();
	}
	private native void jniDispose ();
	boolean contactFilter (long fixtureA, long fixtureB) {
		if (contactFilter != null)
			return contactFilter.shouldCollide(fixtures.get(fixtureA), fixtures.get(fixtureB));
		else {
			Filter filterA = fixtures.get(fixtureA).getFilterData();
			Filter filterB = fixtures.get(fixtureB).getFilterData();

			if (filterA.groupIndex == filterB.groupIndex && filterA.groupIndex != 0) {
				return filterA.groupIndex > 0;
			}

			return (filterA.maskBits & filterB.categoryBits) != 0 && (filterA.categoryBits & filterB.maskBits) != 0;
		}
	}

	private final Contact contact = new Contact(this, 0);
	private final Manifold manifold = new Manifold(0);
	private final ContactImpulse impulse = new ContactImpulse(this, 0);

	void beginContact (long contactAddr) {
        if (contactListener != null) {
            contact.addr = contactAddr;
			contactListener.beginContact(contact);
		}
	}

	void endContact (long contactAddr) {
        if (contactListener != null) {
            contact.addr = contactAddr;
			contactListener.endContact(contact);
		}
	}

	void preSolve (long contactAddr, long manifoldAddr) {
		if (contactListener != null) {
			contact.addr = contactAddr;
			manifold.addr = manifoldAddr;
			contactListener.preSolve(contact, manifold);
		}
	}

	void postSolve (long contactAddr, long impulseAddr) {
		if (contactListener != null) {
			contact.addr = contactAddr;
			impulse.addr = impulseAddr;
			contactListener.postSolve(contact, impulse);
		}
	}

	boolean reportFixture (long addr) {
		if (queryCallback != null)
			return queryCallback.reportFixture(fixtures.get(addr));
		else
			return false;
	}
	public static native void setVelocityThreshold (float threshold);
	public static native float getVelocityThreshold ();
	public void rayCast (RayCastCallback callback, Vector2 point1, Vector2 point2) {
		rayCast(callback, point1.x, point1.y, point2.x, point2.y);
	}
	public void rayCast (RayCastCallback callback, float point1X, float point1Y, float point2X, float point2Y) {
		rayCastCallback = callback;
		jniRayCast(point1X, point1Y, point2X, point2Y);
	}

	private RayCastCallback rayCastCallback = null;

	private native void jniRayCast (float aX, float aY, float bX, float bY);
	private Vector2 rayPoint = new Vector2();
	private Vector2 rayNormal = new Vector2();

	float reportRayFixture (float pX, float pY, float nX, float nY, float fraction) {
		if (rayCastCallback != null) {
			rayPoint.x = pX;
			rayPoint.y = pY;
			rayNormal.x = nX;
			rayNormal.y = nY;
			return rayCastCallback.reportRayFixture(fixtures.get(worldPtr), rayPoint, rayNormal, fraction);
		} else {
			return 0.0f;
		}
	}
}
