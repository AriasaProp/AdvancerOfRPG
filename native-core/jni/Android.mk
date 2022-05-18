LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := ext
LOCAL_C_INCLUDES :=

LOCAL_SRC_FILES := ../src/GraphFunc.cpp\
	../src/graphics/Mesh.cpp\
	../src/graphics/Pixmap.cpp\
	../src/math/Matrix4.cpp\
	../src/utils/BufferUtils.cpp\
	../src/box2d/Body.cpp\
	../src/box2d/World.cpp\
	../src/box2d/ChainShape.cpp\
	../src/box2d/Contact.cpp\
	../src/box2d/ContactImpulse.cpp\
	../src/box2d/EdgeShape.cpp\
	../src/box2d/Fixture.cpp\
	../src/box2d/Joint.cpp\
	../src/box2d/Manifold.cpp\
	../src/box2d/PolygonShape.cpp\
	../src/box2d/Shape.cpp\
	../src/box2d/Collision/Shapes/b2ChainShape.cpp\
	../src/box2d/Collision/Shapes/b2CircleShape.cpp\
	../src/box2d/Collision/Shapes/b2EdgeShape.cpp\
	../src/box2d/Collision/Shapes/b2PolygonShape.cpp\
	../src/box2d/Collision/b2BroadPhase.cpp\
	../src/box2d/Collision/b2CollideCircle.cpp\
	../src/box2d/Collision/b2CollideEdge.cpp\
	../src/box2d/Collision/b2CollidePolygon.cpp\
	../src/box2d/Collision/b2Collision.cpp\
	../src/box2d/Collision/b2Distance.cpp\
	../src/box2d/Collision/b2DynamicTree.cpp\
	../src/box2d/Collision/b2TimeOfImpact.cpp\
	../src/box2d/Common/b2BlockAllocator.cpp\
	../src/box2d/Common/b2Draw.cpp\
	../src/box2d/Common/b2Math.cpp\
	../src/box2d/Common/b2Settings.cpp\
	../src/box2d/Common/b2StackAllocator.cpp\
	../src/box2d/Common/b2Timer.cpp\
	../src/box2d/Dynamics/Contacts/b2ChainAndCircleContact.cpp\
	../src/box2d/Dynamics/Contacts/b2ChainAndPolygonContact.cpp\
	../src/box2d/Dynamics/Contacts/b2CircleContact.cpp\
	../src/box2d/Dynamics/Contacts/b2Contact.cpp\
	../src/box2d/Dynamics/Contacts/b2ContactSolver.cpp\
	../src/box2d/Dynamics/Contacts/b2EdgeAndCircleContact.cpp\
	../src/box2d/Dynamics/Contacts/b2EdgeAndPolygonContact.cpp\
	../src/box2d/Dynamics/Contacts/b2PolygonAndCircleContact.cpp\
	../src/box2d/Dynamics/Contacts/b2PolygonContact.cpp\
	../src/box2d/Dynamics/Joints/b2DistanceJoint.cpp\
	../src/box2d/Dynamics/Joints/b2FrictionJoint.cpp\
	../src/box2d/Dynamics/Joints/b2GearJoint.cpp\
	../src/box2d/Dynamics/Joints/b2Joint.cpp\
	../src/box2d/Dynamics/Joints/b2MotorJoint.cpp\
	../src/box2d/Dynamics/Joints/b2MouseJoint.cpp\
	../src/box2d/Dynamics/Joints/b2PrismaticJoint.cpp\
	../src/box2d/Dynamics/Joints/b2PulleyJoint.cpp\
	../src/box2d/Dynamics/Joints/b2RevoluteJoint.cpp\
	../src/box2d/Dynamics/Joints/b2RopeJoint.cpp\
	../src/box2d/Dynamics/Joints/b2WeldJoint.cpp\
	../src/box2d/Dynamics/Joints/b2WheelJoint.cpp\
	../src/box2d/Dynamics/b2Body.cpp\
	../src/box2d/Dynamics/b2ContactManager.cpp\
	../src/box2d/Dynamics/b2Fixture.cpp\
	../src/box2d/Dynamics/b2Island.cpp\
	../src/box2d/Dynamics/b2World.cpp\
	../src/box2d/Dynamics/b2WorldCallbacks.cpp\
	../src/box2d/Rope/b2Rope.cpp

LOCAL_CPPFLAGS += -std=c++11

include $(BUILD_SHARED_LIBRARY)
