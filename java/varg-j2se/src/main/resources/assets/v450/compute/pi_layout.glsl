#define INCLUDED_LAYOUT

// Allows us to use 16 and 8 bit storage
#extension GL_EXT_shader_16bit_storage : require
#extension GL_EXT_shader_8bit_storage : require
// Allows use of float16 arithmetic
#extension GL_EXT_shader_explicit_arithmetic_types_float16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int8 : require 
#extension GL_EXT_scalar_block_layout : enable

layout (constant_id = 0) const int CURVE_ARRAYS = 2;


layout(std430, set = 0, binding = 0) readonly buffer _inputdata {
    uvec4 dimension;
    vec3 offset;
    vec3 scale;
} settings;

layout(std430, set = 1, binding = 0) buffer _output {
    uint8_t data[];
} procedural;

layout(std430, set = 2, binding = 0) readonly buffer _vec4inputs {
    vec4 inputData[];
} vec4inputs;

layout(std430, set = 3, binding = 0) readonly buffer _floatinputs {
    float inputData[];
} floatinputs;

layout(std430, set = 4, binding = 0) readonly buffer _opcodes {
    uint16_t code[];
} opcodes;

struct Opcode {
    vec4[OUTPUT_COUNT] outputData;
    uint index;
    uint code;
    uint param;
    uint outIndex;
    uint inputCount;
    uint operatorCount;
};

    Opcode opcode;


