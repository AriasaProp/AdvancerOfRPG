################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/box2d/Dynamics/Contacts/b2ChainAndCircleContact.cpp \
../jni/box2d/Dynamics/Contacts/b2ChainAndPolygonContact.cpp \
../jni/box2d/Dynamics/Contacts/b2CircleContact.cpp \
../jni/box2d/Dynamics/Contacts/b2Contact.cpp \
../jni/box2d/Dynamics/Contacts/b2ContactSolver.cpp \
../jni/box2d/Dynamics/Contacts/b2EdgeAndCircleContact.cpp \
../jni/box2d/Dynamics/Contacts/b2EdgeAndPolygonContact.cpp \
../jni/box2d/Dynamics/Contacts/b2PolygonAndCircleContact.cpp \
../jni/box2d/Dynamics/Contacts/b2PolygonContact.cpp 

OBJS += \
./jni/box2d/Dynamics/Contacts/b2ChainAndCircleContact.o \
./jni/box2d/Dynamics/Contacts/b2ChainAndPolygonContact.o \
./jni/box2d/Dynamics/Contacts/b2CircleContact.o \
./jni/box2d/Dynamics/Contacts/b2Contact.o \
./jni/box2d/Dynamics/Contacts/b2ContactSolver.o \
./jni/box2d/Dynamics/Contacts/b2EdgeAndCircleContact.o \
./jni/box2d/Dynamics/Contacts/b2EdgeAndPolygonContact.o \
./jni/box2d/Dynamics/Contacts/b2PolygonAndCircleContact.o \
./jni/box2d/Dynamics/Contacts/b2PolygonContact.o 

CPP_DEPS += \
./jni/box2d/Dynamics/Contacts/b2ChainAndCircleContact.d \
./jni/box2d/Dynamics/Contacts/b2ChainAndPolygonContact.d \
./jni/box2d/Dynamics/Contacts/b2CircleContact.d \
./jni/box2d/Dynamics/Contacts/b2Contact.d \
./jni/box2d/Dynamics/Contacts/b2ContactSolver.d \
./jni/box2d/Dynamics/Contacts/b2EdgeAndCircleContact.d \
./jni/box2d/Dynamics/Contacts/b2EdgeAndPolygonContact.d \
./jni/box2d/Dynamics/Contacts/b2PolygonAndCircleContact.d \
./jni/box2d/Dynamics/Contacts/b2PolygonContact.d 


# Each subdirectory must supply rules for building sources it contributes
jni/box2d/Dynamics/Contacts/%.o: ../jni/box2d/Dynamics/Contacts/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C++ Compiler'
	g++ -D__int64="long long" -I"C:\Program Files\Java\jdk1.7.0_80\include" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32\bridge" -O0 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


