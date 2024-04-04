#define INCLUDED_LAYOUT

// Allows us to use 16 and 8 bit storage
#extension GL_EXT_shader_16bit_storage : require
#extension GL_EXT_shader_8bit_storage : require
// Allows use of float16 arithmetic
#extension GL_EXT_shader_explicit_arithmetic_types_float16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int8 : require 
#extension GL_EXT_scalar_block_layout : enable

#define VOXEL_BINDING 1


struct TaskPayload {
    uint taskIndex;
};

taskPayloadSharedEXT TaskPayload taskPayload;


layout(std430, set = 0, binding = VOXEL_BINDING) readonly buffer _voxeldata {
    vec4 offsets[MAX_VOXEL_COUNT];
//    f16vec4 offsets[MAX_VOXEL_COUNT];
    uint8_t indexes[MAX_VOXEL_COUNT];
} voxelData;

layout(std430, set = 1, binding = VOXEL_BINDING) buffer _output {
    vec4 positions[MAX_VOXEL_COUNT];
} dataOut;

layout(std430, set = 2, binding = VOXEL_BINDING) readonly buffer _sprite {
    f16vec4 position[MAX_SPRITE_COUNT];
    f16mat4 matrix[MAX_SPRITE_COUNT];
    /**
     * Array of cube vertices - order is front facing, upper left, upper right, lower right, lower left.
     */
    f16vec4[MAX_SPRITE_COUNT][4][2] cubePos;
} spriteData;

// Todo : only use modelmatrix, share global uniform data with gltf and take view/projection matrix from there
layout(std430, set = 3, binding = VOXEL_BINDING) uniform MatrixStruct {
    f16mat4 uModelMatrix[3];
} matrix;

layout(std430, set = 4, binding = VOXEL_BINDING) uniform GlobalData {
    f16vec4 colorTable[128];
    u16vec4 taskWorkGroups;
    f16mat4 cameraMatrix;
} globals;
