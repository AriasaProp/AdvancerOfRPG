################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/box2d/Dynamics/b2Body.cpp \
../jni/box2d/Dynamics/b2ContactManager.cpp \
../jni/box2d/Dynamics/b2Fixture.cpp \
../jni/box2d/Dynamics/b2Island.cpp \
../jni/box2d/Dynamics/b2World.cpp \
../jni/box2d/Dynamics/b2WorldCallbacks.cpp 

OBJS += \
./jni/box2d/Dynamics/b2Body.o \
./jni/box2d/Dynamics/b2ContactManager.o \
./jni/box2d/Dynamics/b2Fixture.o \
./jni/box2d/Dynamics/b2Island.o \
./jni/box2d/Dynamics/b2World.o \
./jni/box2d/Dynamics/b2WorldCallbacks.o 

CPP_DEPS += \
./jni/box2d/Dynamics/b2Body.d \
./jni/box2d/Dynamics/b2ContactManager.d \
./jni/box2d/Dynamics/b2Fixture.d \
./jni/box2d/Dynamics/b2Island.d \
./jni/box2d/Dynamics/b2World.d \
./jni/box2d/Dynamics/b2WorldCallbacks.d 


# Each subdirectory must supply rules for building sources it contributes
jni/box2d/Dynamics/%.o: ../jni/box2d/Dynamics/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C++ Compiler'
	g++ -D__int64="long long" -I"C:\Program Files\Java\jdk1.7.0_80\include" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32\bridge" -O0 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


