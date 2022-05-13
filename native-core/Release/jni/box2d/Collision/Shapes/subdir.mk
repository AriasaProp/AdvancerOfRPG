################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/box2d/Collision/Shapes/b2ChainShape.cpp \
../jni/box2d/Collision/Shapes/b2CircleShape.cpp \
../jni/box2d/Collision/Shapes/b2EdgeShape.cpp \
../jni/box2d/Collision/Shapes/b2PolygonShape.cpp 

OBJS += \
./jni/box2d/Collision/Shapes/b2ChainShape.o \
./jni/box2d/Collision/Shapes/b2CircleShape.o \
./jni/box2d/Collision/Shapes/b2EdgeShape.o \
./jni/box2d/Collision/Shapes/b2PolygonShape.o 

CPP_DEPS += \
./jni/box2d/Collision/Shapes/b2ChainShape.d \
./jni/box2d/Collision/Shapes/b2CircleShape.d \
./jni/box2d/Collision/Shapes/b2EdgeShape.d \
./jni/box2d/Collision/Shapes/b2PolygonShape.d 


# Each subdirectory must supply rules for building sources it contributes
jni/box2d/Collision/Shapes/%.o: ../jni/box2d/Collision/Shapes/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C++ Compiler'
	g++ -D__int64="long long" -I"C:\Program Files\Java\jdk1.7.0_80\include" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32\bridge" -O0 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


