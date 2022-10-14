cmake_minimum_required(VERSION 3.4.1)

project(ext LANGUAGES CXX)
set(CMAKE_BUILD_TYPE Release)

include_directories(${JNI_INCLUDE_DIRS} "${CMAKE_SOURCE_DIR}")
set(GLOB_RECURSE SOURCE_FILES "${CMAKE_SOURCE_DIR}/*.cpp")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=c++11")
add_library(ext SHARED 
		GraphFunc.cpp
		utils/BufferUtils.cpp
		math/Matrix4.cpp
		graphics/Mesh.cpp
		graphics/Pixmap.cpp)

set_target_properties(ext PROPERTIES LINKER_LANGUAGE CXX)


