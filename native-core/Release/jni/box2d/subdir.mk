################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/box2d/Body.cpp \
../jni/box2d/ChainShape.cpp \
../jni/box2d/CircleShape.cpp \
../jni/box2d/Contact.cpp \
../jni/box2d/ContactImpulse.cpp \
../jni/box2d/EdgeShape.cpp \
../jni/box2d/Fixture.cpp \
../jni/box2d/Joint.cpp \
../jni/box2d/Manifold.cpp \
../jni/box2d/PolygonShape.cpp \
../jni/box2d/Shape.cpp \
../jni/box2d/World.cpp 

OBJS += \
./jni/box2d/Body.o \
./jni/box2d/ChainShape.o \
./jni/box2d/CircleShape.o \
./jni/box2d/Contact.o \
./jni/box2d/ContactImpulse.o \
./jni/box2d/EdgeShape.o \
./jni/box2d/Fixture.o \
./jni/box2d/Joint.o \
./jni/box2d/Manifold.o \
./jni/box2d/PolygonShape.o \
./jni/box2d/Shape.o \
./jni/box2d/World.o 

CPP_DEPS += \
./jni/box2d/Body.d \
./jni/box2d/ChainShape.d \
./jni/box2d/CircleShape.d \
./jni/box2d/Contact.d \
./jni/box2d/ContactImpulse.d \
./jni/box2d/EdgeShape.d \
./jni/box2d/Fixture.d \
./jni/box2d/Joint.d \
./jni/box2d/Manifold.d \
./jni/box2d/PolygonShape.d \
./jni/box2d/Shape.d \
./jni/box2d/World.d 


# Each subdirectory must supply rules for building sources it contributes
jni/box2d/%.o: ../jni/box2d/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C++ Compiler'
	g++ -D__int64="long long" -I"C:\Program Files\Java\jdk1.7.0_80\include" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32\bridge" -O0 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


