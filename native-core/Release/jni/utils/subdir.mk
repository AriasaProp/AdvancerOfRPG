################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/utils/BufferUtils.cpp 

OBJS += \
./jni/utils/BufferUtils.o 

CPP_DEPS += \
./jni/utils/BufferUtils.d 


# Each subdirectory must supply rules for building sources it contributes
jni/utils/%.o: ../jni/utils/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cygwin C++ Compiler'
	g++ -D__int64="long long" -I"C:\Program Files\Java\jdk1.7.0_80\include" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32" -I"C:\Program Files\Java\jdk1.7.0_80\include\win32\bridge" -O0 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


