cmake_minimum_required(VERSION 3.6.0)
project(ext LANGUAGES CXX)
set(CMAKE_BUILD_TYPE Release)

set(JAVA_AWT_LIBRARY "$ENV{JAVA_HOME}/lib/libjawt.so")
set(JAVA_JVM_LIBRARY "$ENV{JAVA_HOME}/lib/server/libjvm.so")
set(JAVA_INCLUDE_PATH "$ENV{JAVA_HOME}/include")
set(JAVA_INCLUDE_PATH2 "$ENV{JAVA_HOME}/include/linux")
set(JAVA_AWT_INCLUDE_PATH "$ENV{JAVA_HOME}/include")
find_package(JNI REQUIRED)

include_directories(${JNI_INCLUDE_DIRS} "${PROJECT_SOURCE_DIR}/src")
set(GLOB_RECURSE SOURCE_FILES "${PROJECT_SOURCE_DIR}/src/*.cpp")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -wall -std=c++11")
add_library(ext SHARED ${SOURCE_FILES})

set_target_properties(ext PROPERTIES LINKER_LANGUAGE CXX)


