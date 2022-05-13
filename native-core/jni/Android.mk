LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := ext
LOCAL_C_INCLUDES :=

LOCAL_SRC_FILES := GraphFunc.cpp\
	graphics/Mesh.cpp\
	graphics/Pixmap.cpp\
	math/Matrix4.cpp\
	utils/BufferUtils.cpp\
	box2d/Body.cpp\
	box2d/World.cpp\
	box2d/ChainShape.cpp\
	box2d/Contact.cpp\
	box2d/ContactImpulse.cpp\
	box2d/EdgeShape.cpp\
	box2d/Fixture.cpp\
	box2d/Joint.cpp\
	box2d/Manifold.cpp\
	box2d/PolygonShape.cpp\
	box2d/Shape.cpp\
	box2d/Collision/Shapes/b2ChainShape.cpp\
	box2d/Collision/Shapes/b2CircleShape.cpp\
	box2d/Collision/Shapes/b2EdgeShape.cpp\
	box2d/Collision/Shapes/b2PolygonShape.cpp\
	box2d/Collision/b2BroadPhase.cpp\
	box2d/Collision/b2CollideCircle.cpp\
	box2d/Collision/b2CollideEdge.cpp\
	box2d/Collision/b2CollidePolygon.cpp\
	box2d/Collision/b2Collision.cpp\
	box2d/Collision/b2Distance.cpp\
	box2d/Collision/b2DynamicTree.cpp\
	box2d/Collision/b2TimeOfImpact.cpp\
	box2d/Common/b2BlockAllocator.cpp\
	box2d/Common/b2Draw.cpp\
	box2d/Common/b2Math.cpp\
	box2d/Common/b2Settings.cpp\
	box2d/Common/b2StackAllocator.cpp\
	box2d/Common/b2Timer.cpp\
	box2d/Dynamics/Contacts/b2ChainAndCircleContact.cpp\
	box2d/Dynamics/Contacts/b2ChainAndPolygonContact.cpp\
	box2d/Dynamics/Contacts/b2CircleContact.cpp\
	box2d/Dynamics/Contacts/b2Contact.cpp\
	box2d/Dynamics/Contacts/b2ContactSolver.cpp\
	box2d/Dynamics/Contacts/b2EdgeAndCircleContact.cpp\
	box2d/Dynamics/Contacts/b2EdgeAndPolygonContact.cpp\
	box2d/Dynamics/Contacts/b2PolygonAndCircleContact.cpp\
	box2d/Dynamics/Contacts/b2PolygonContact.cpp\
	box2d/Dynamics/Joints/b2DistanceJoint.cpp\
	box2d/Dynamics/Joints/b2FrictionJoint.cpp\
	box2d/Dynamics/Joints/b2GearJoint.cpp\
	box2d/Dynamics/Joints/b2Joint.cpp\
	box2d/Dynamics/Joints/b2MotorJoint.cpp\
	box2d/Dynamics/Joints/b2MouseJoint.cpp\
	box2d/Dynamics/Joints/b2PrismaticJoint.cpp\
	box2d/Dynamics/Joints/b2PulleyJoint.cpp\
	box2d/Dynamics/Joints/b2RevoluteJoint.cpp\
	box2d/Dynamics/Joints/b2RopeJoint.cpp\
	box2d/Dynamics/Joints/b2WeldJoint.cpp\
	box2d/Dynamics/Joints/b2WheelJoint.cpp\
	box2d/Dynamics/b2Body.cpp\
	box2d/Dynamics/b2ContactManager.cpp\
	box2d/Dynamics/b2Fixture.cpp\
	box2d/Dynamics/b2Island.cpp\
	box2d/Dynamics/b2World.cpp\
	box2d/Dynamics/b2WorldCallbacks.cpp\
	box2d/Rope/b2Rope.cpp

include $(BUILD_SHARED_LIBRARY)
