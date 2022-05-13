################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/box2d/Collision/b2BroadPhase.cpp \
../jni/box2d/Collision/b2CollideCircle.cpp \
../jni/box2d/Collision/b2CollideEdge.cpp \
../jni/box2d/Collision/b2CollidePolygon.cpp \
../jni/box2d/Collision/b2Collision.cpp \
../jni/box2d/Collision/b2Distance.cpp \
../jni/box2d/Collision/b2DynamicTree.cpp \
../jni/box2d/Collision/b2TimeOfImpact.cpp 

OBJS += \
./jni/box2d/Collision/b2BroadPhase.o \
./jni/box2d/Collision/b2CollideCircle.o \
./jni/box2d/Collision/b2CollideEdge.o \
./jni/box2d/Collision/b2CollidePolygon.o \
./jni/box2d/Collision/b2Collision.o \
./jni/box2d/Collision/b2Distance.o \
./jni/box2d/Collision/b2DynamicTree.o \
./jni/box2d/Collision/b2TimeOfImpact.o 

CPP_DEPS += \
./jni/box2d/Collision/b2BroadPhase.d \
./jni/box2d/Collision/b2CollideCircle.d \
./jni/box2d/Collision/b2CollideEdge.d \
./jni/box2d/Collision/b2CollidePolygon.d \
./jni/box2d/Collision/b2Collision.d \
./jni/box2d/Collision/b2Distance.d \
./jni/box2d/Collision/b2DynamicTree.d \
./jni/box2d/Collision/b2TimeOfImpact.d 


# Each subdirectory must supply rules for building sources it contributes
jni/box2d/Collision/%.o: ../jni/box2d/Collision/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C++ Compiler'
	g++ -D__int64="long long" -I"C:\Program Files\Java\jdk1.7.0_80\include" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32\bridge" -O0 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


