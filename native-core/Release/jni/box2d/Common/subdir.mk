################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/box2d/Common/b2BlockAllocator.cpp \
../jni/box2d/Common/b2Draw.cpp \
../jni/box2d/Common/b2Math.cpp \
../jni/box2d/Common/b2Settings.cpp \
../jni/box2d/Common/b2StackAllocator.cpp \
../jni/box2d/Common/b2Timer.cpp 

OBJS += \
./jni/box2d/Common/b2BlockAllocator.o \
./jni/box2d/Common/b2Draw.o \
./jni/box2d/Common/b2Math.o \
./jni/box2d/Common/b2Settings.o \
./jni/box2d/Common/b2StackAllocator.o \
./jni/box2d/Common/b2Timer.o 

CPP_DEPS += \
./jni/box2d/Common/b2BlockAllocator.d \
./jni/box2d/Common/b2Draw.d \
./jni/box2d/Common/b2Math.d \
./jni/box2d/Common/b2Settings.d \
./jni/box2d/Common/b2StackAllocator.d \
./jni/box2d/Common/b2Timer.d 


# Each subdirectory must supply rules for building sources it contributes
jni/box2d/Common/%.o: ../jni/box2d/Common/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C++ Compiler'
	g++ -D__int64="long long" -I"C:\Program Files\Java\jdk1.7.0_80\include" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32\bridge" -O0 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


