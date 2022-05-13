################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/box2d/Dynamics/Joints/b2DistanceJoint.cpp \
../jni/box2d/Dynamics/Joints/b2FrictionJoint.cpp \
../jni/box2d/Dynamics/Joints/b2GearJoint.cpp \
../jni/box2d/Dynamics/Joints/b2Joint.cpp \
../jni/box2d/Dynamics/Joints/b2MotorJoint.cpp \
../jni/box2d/Dynamics/Joints/b2MouseJoint.cpp \
../jni/box2d/Dynamics/Joints/b2PrismaticJoint.cpp \
../jni/box2d/Dynamics/Joints/b2PulleyJoint.cpp \
../jni/box2d/Dynamics/Joints/b2RevoluteJoint.cpp \
../jni/box2d/Dynamics/Joints/b2RopeJoint.cpp \
../jni/box2d/Dynamics/Joints/b2WeldJoint.cpp \
../jni/box2d/Dynamics/Joints/b2WheelJoint.cpp 

OBJS += \
./jni/box2d/Dynamics/Joints/b2DistanceJoint.o \
./jni/box2d/Dynamics/Joints/b2FrictionJoint.o \
./jni/box2d/Dynamics/Joints/b2GearJoint.o \
./jni/box2d/Dynamics/Joints/b2Joint.o \
./jni/box2d/Dynamics/Joints/b2MotorJoint.o \
./jni/box2d/Dynamics/Joints/b2MouseJoint.o \
./jni/box2d/Dynamics/Joints/b2PrismaticJoint.o \
./jni/box2d/Dynamics/Joints/b2PulleyJoint.o \
./jni/box2d/Dynamics/Joints/b2RevoluteJoint.o \
./jni/box2d/Dynamics/Joints/b2RopeJoint.o \
./jni/box2d/Dynamics/Joints/b2WeldJoint.o \
./jni/box2d/Dynamics/Joints/b2WheelJoint.o 

CPP_DEPS += \
./jni/box2d/Dynamics/Joints/b2DistanceJoint.d \
./jni/box2d/Dynamics/Joints/b2FrictionJoint.d \
./jni/box2d/Dynamics/Joints/b2GearJoint.d \
./jni/box2d/Dynamics/Joints/b2Joint.d \
./jni/box2d/Dynamics/Joints/b2MotorJoint.d \
./jni/box2d/Dynamics/Joints/b2MouseJoint.d \
./jni/box2d/Dynamics/Joints/b2PrismaticJoint.d \
./jni/box2d/Dynamics/Joints/b2PulleyJoint.d \
./jni/box2d/Dynamics/Joints/b2RevoluteJoint.d \
./jni/box2d/Dynamics/Joints/b2RopeJoint.d \
./jni/box2d/Dynamics/Joints/b2WeldJoint.d \
./jni/box2d/Dynamics/Joints/b2WheelJoint.d 


# Each subdirectory must supply rules for building sources it contributes
jni/box2d/Dynamics/Joints/%.o: ../jni/box2d/Dynamics/Joints/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C++ Compiler'
	g++ -D__int64="long long" -I"C:\Program Files\Java\jdk1.7.0_80\include" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32\bridge" -O0 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


