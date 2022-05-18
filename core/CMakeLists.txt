cmake_minimum_required(VERSION 3.3)
project(example_cmake)

#
# todo;; eventually this is specified externally
set(ENV{JAVA_HOME} "/usr/local/dev/java/jdk-7u51-windows")

#
# we have a custom findjni to find the cross compile jre libs
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules")


#
# this is the linux compilation configuration
# todo;; windows configuration
#

#
# if on linux, move the cc path into the standard pat#h
set(CMAKE_SYSTEM_NAME Windows)

# which compilers to use for C and C++
set(CMAKE_C_COMPILER i586-mingw32msvc-gcc)
set(CMAKE_CXX_COMPILER i586-mingw32msvc-g++)
set(CMAKE_RC_COMPILER i586-mingw32msvc-windres)

# here is the target environment located
set(CMAKE_FIND_ROOT_PATH  /usr/bin/i586-mingw32msvc)

# adjust the default behaviour of the FIND_XXX() commands:
# search headers and libraries in the target environment, search
# programs in the host environment
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

#
# also on linux change the lib search
set(CMAKE_FIND_LIBRARY_PREFIXES "")
set(CMAKE_FIND_LIBRARY_SUFFIXES ".dll")

find_package(JNI)
if (JNI_FOUND)
    message(STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message(STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif ()

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}")

set (_classes "${PROJECT_SOURCE_DIR}/build/classes/main")
set (_jnih "${PROJECT_SOURCE_DIR}/build/jnih")

#
# call javah to generate headers
add_custom_command(
    OUTPUT Example.h
    COMMAND ${Java_JAVAH_EXECUTABLE} -verbose
    -classpath ${_classes}
    -d ${_jnih}
    example.Example
)


include_directories(${JAVA_INCLUDE_PATH} ${JNI_INCLUDE_DIRS} ${_jnih})
add_library(example MODULE src/main/cpp/Example.cpp Example.h)
set_target_properties(example PROPERTIES SUFFIX ".jnilib")
target_link_libraries(example ${JNI_LIBRARIES})
