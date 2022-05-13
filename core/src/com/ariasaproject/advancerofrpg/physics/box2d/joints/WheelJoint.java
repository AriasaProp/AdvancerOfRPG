package com.ariasaproject.advancerofrpg.physics.box2d.joints;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.physics.box2d.Joint;
import com.ariasaproject.advancerofrpg.physics.box2d.World;

public class WheelJoint extends Joint {
	private final float[] tmp = new float[2];
	private final Vector2 localAnchorA = new Vector2();
	private final Vector2 localAnchorB = new Vector2();
	private final Vector2 localAxisA = new Vector2();

	public WheelJoint (World world, long addr) {
		super(world, addr);
	}

	public Vector2 getLocalAnchorA () {
		jniGetLocalAnchorA(addr, tmp);
		localAnchorA.set(tmp[0], tmp[1]);
		return localAnchorA;
	}

	private native void jniGetLocalAnchorA (long addr, float[] anchor); /*
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		anchor[0] = joint->GetLocalAnchorA().x;
		anchor[1] = joint->GetLocalAnchorA().y;
	*/

	public Vector2 getLocalAnchorB () {
		jniGetLocalAnchorB(addr, tmp);
		localAnchorB.set(tmp[0], tmp[1]);
		return localAnchorB;
	}

	private native void jniGetLocalAnchorB (long addr, float[] anchor); /*
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		anchor[0] = joint->GetLocalAnchorB().x;
		anchor[1] = joint->GetLocalAnchorB().y;
	*/

	public Vector2 getLocalAxisA(){
		jniGetLocalAxisA(addr, tmp);
		localAxisA.set(tmp[0], tmp[1]);
		return localAxisA;
	}

	private native void jniGetLocalAxisA (long addr, float[] anchor); /*
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		anchor[0] = joint->GetLocalAxisA().x;
		anchor[1] = joint->GetLocalAxisA().y;
	*/

	/** Get the current joint translation, usually in meters. */
	public float getJointTranslation () {
		return jniGetJointTranslation(addr);
	}

	private native float jniGetJointTranslation (long addr); /*
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetJointTranslation();
	*/

	/** Get the current joint translation speed, usually in meters per second. */
	public float getJointSpeed () {
		return jniGetJointSpeed(addr);
	}

	private native float jniGetJointSpeed (long addr); /*
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetJointSpeed();
	*/

	/** Is the joint motor enabled? */
	public boolean isMotorEnabled () {
		return jniIsMotorEnabled(addr);
	}

	private native boolean jniIsMotorEnabled (long addr); /*
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->IsMotorEnabled();
	*/

	/** Enable/disable the joint motor. */
	public void enableMotor (boolean flag) {
		jniEnableMotor(addr, flag);
	}

	private native void jniEnableMotor (long addr, boolean flag); /*
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->EnableMotor(flag);
	*/

	/** Set the motor speed, usually in radians per second. */
	public void setMotorSpeed (float speed) {
		jniSetMotorSpeed(addr, speed);
	}

	private native void jniSetMotorSpeed (long addr, float speed); /*
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->SetMotorSpeed(speed);
	*/

	/** Get the motor speed, usually in radians per second. */
	public float getMotorSpeed () {
		return jniGetMotorSpeed(addr);
	}

	private native float jniGetMotorSpeed (long addr); /*
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetMotorSpeed();
	*/

	/** Set/Get the maximum motor force, usually in N-m. */
	public void setMaxMotorTorque (float torque) {
		jniSetMaxMotorTorque(addr, torque);
	}

	private native void jniSetMaxMotorTorque (long addr, float torque); /*
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->SetMaxMotorTorque(torque);
	*/

	public float getMaxMotorTorque () {
		return jniGetMaxMotorTorque(addr);
	}

	private native float jniGetMaxMotorTorque (long addr); /*
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetMaxMotorTorque();
	*/

	/** Get the current motor torque given the inverse time step, usually in N-m. */
	public float getMotorTorque (float invDt) {
		return jniGetMotorTorque(addr, invDt);
	}

	private native float jniGetMotorTorque (long addr, float invDt); /*
	  	b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetMotorTorque(invDt);
	*/

	/** Set/Get the spring frequency in hertz. Setting the frequency to zero disables the spring. */
	public void setSpringFrequencyHz (float hz) {
		jniSetSpringFrequencyHz(addr, hz);
	}

	private native void jniSetSpringFrequencyHz (long addr, float hz); /*
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->SetSpringFrequencyHz(hz);
	*/

	public float getSpringFrequencyHz () {
		return jniGetSpringFrequencyHz(addr);
	}

	private native float jniGetSpringFrequencyHz (long addr); /*
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetSpringFrequencyHz();
	*/

	/** Set/Get the spring damping ratio */
	public void setSpringDampingRatio (float ratio) {
		jniSetSpringDampingRatio(addr, ratio);
	}

	private native void jniSetSpringDampingRatio (long addr, float ratio); /*
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		joint->SetSpringDampingRatio(ratio);
	*/

	public float getSpringDampingRatio () {
		return jniGetSpringDampingRatio(addr);
	}

	private native float jniGetSpringDampingRatio (long addr); /*
		b2WheelJoint* joint = (b2WheelJoint*)addr;
		return joint->GetSpringDampingRatio();
	*/

}
