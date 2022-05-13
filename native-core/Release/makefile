################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include obj/local/x86_64/objs/ext/utils/subdir.mk
-include obj/local/x86_64/objs/ext/math/subdir.mk
-include obj/local/x86_64/objs/ext/graphics/subdir.mk
-include obj/local/x86_64/objs/ext/box2d/Rope/subdir.mk
-include obj/local/x86_64/objs/ext/box2d/Dynamics/Joints/subdir.mk
-include obj/local/x86_64/objs/ext/box2d/Dynamics/Contacts/subdir.mk
-include obj/local/x86_64/objs/ext/box2d/Dynamics/subdir.mk
-include obj/local/x86_64/objs/ext/box2d/Common/subdir.mk
-include obj/local/x86_64/objs/ext/box2d/Collision/Shapes/subdir.mk
-include obj/local/x86_64/objs/ext/box2d/Collision/subdir.mk
-include obj/local/x86_64/objs/ext/box2d/subdir.mk
-include obj/local/x86_64/objs/ext/subdir.mk
-include obj/local/x86/objs/ext/utils/subdir.mk
-include obj/local/x86/objs/ext/math/subdir.mk
-include obj/local/x86/objs/ext/graphics/subdir.mk
-include obj/local/x86/objs/ext/box2d/Rope/subdir.mk
-include obj/local/x86/objs/ext/box2d/Dynamics/Joints/subdir.mk
-include obj/local/x86/objs/ext/box2d/Dynamics/Contacts/subdir.mk
-include obj/local/x86/objs/ext/box2d/Dynamics/subdir.mk
-include obj/local/x86/objs/ext/box2d/Common/subdir.mk
-include obj/local/x86/objs/ext/box2d/Collision/Shapes/subdir.mk
-include obj/local/x86/objs/ext/box2d/Collision/subdir.mk
-include obj/local/x86/objs/ext/box2d/subdir.mk
-include obj/local/x86/objs/ext/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/utils/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/math/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/graphics/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/box2d/Rope/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/box2d/Dynamics/Joints/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/box2d/Dynamics/Contacts/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/box2d/Dynamics/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/box2d/Common/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/box2d/Collision/Shapes/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/box2d/Collision/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/box2d/subdir.mk
-include obj/local/armeabi-v7a/objs/ext/subdir.mk
-include obj/local/arm64-v8a/objs/ext/utils/subdir.mk
-include obj/local/arm64-v8a/objs/ext/math/subdir.mk
-include obj/local/arm64-v8a/objs/ext/graphics/subdir.mk
-include obj/local/arm64-v8a/objs/ext/box2d/Rope/subdir.mk
-include obj/local/arm64-v8a/objs/ext/box2d/Dynamics/Joints/subdir.mk
-include obj/local/arm64-v8a/objs/ext/box2d/Dynamics/Contacts/subdir.mk
-include obj/local/arm64-v8a/objs/ext/box2d/Dynamics/subdir.mk
-include obj/local/arm64-v8a/objs/ext/box2d/Common/subdir.mk
-include obj/local/arm64-v8a/objs/ext/box2d/Collision/Shapes/subdir.mk
-include obj/local/arm64-v8a/objs/ext/box2d/Collision/subdir.mk
-include obj/local/arm64-v8a/objs/ext/box2d/subdir.mk
-include obj/local/arm64-v8a/objs/ext/subdir.mk
-include jni/utils/subdir.mk
-include jni/math/subdir.mk
-include jni/graphics/subdir.mk
-include jni/box2d/Rope/subdir.mk
-include jni/box2d/Dynamics/Joints/subdir.mk
-include jni/box2d/Dynamics/Contacts/subdir.mk
-include jni/box2d/Dynamics/subdir.mk
-include jni/box2d/Common/subdir.mk
-include jni/box2d/Collision/Shapes/subdir.mk
-include jni/box2d/Collision/subdir.mk
-include jni/box2d/subdir.mk
-include jni/subdir.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(CC_DEPS)),)
-include $(CC_DEPS)
endif
ifneq ($(strip $(C++_DEPS)),)
-include $(C++_DEPS)
endif
ifneq ($(strip $(C_UPPER_DEPS)),)
-include $(C_UPPER_DEPS)
endif
ifneq ($(strip $(CXX_DEPS)),)
-include $(CXX_DEPS)
endif
ifneq ($(strip $(CPP_DEPS)),)
-include $(CPP_DEPS)
endif
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: ext.dll

# Tool invocations
ext.dll: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: Cygwin C++ Linker'
	g++ -shared -o "ext.dll" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(LIBRARIES)$(CC_DEPS)$(C++_DEPS)$(OBJS)$(C_UPPER_DEPS)$(CXX_DEPS)$(CPP_DEPS)$(C_DEPS) ext.dll
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
