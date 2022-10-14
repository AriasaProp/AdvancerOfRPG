cmake_minimum_required(VERSION 3.4.1)
project(ext LANGUAGES CXX)
set(CMAKE_BUILD_TYPE Release)

find_package(JNI REQUIRED)
if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif()

include_directories(${JNI_INCLUDE_DIRS} "${PROJECT_SOURCE_DIR}/src")
set(GLOB_RECURSE SOURCE_FILES "${PROJECT_SOURCE_DIR}/src/*.cpp")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -wall -std=c++11")
add_library(ext SHARED ${SOURCE_FILES})

set_target_properties(ext PROPERTIES LINKER_LANGUAGE CXX)


