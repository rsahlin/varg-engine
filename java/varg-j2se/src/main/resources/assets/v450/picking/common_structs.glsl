#define INCLUDED_COMMON_STRUCTS

// Allows use of float16 arithmetic
#extension GL_EXT_shader_explicit_arithmetic_types_float16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int8 : require 

/**
 * Source for structs shared 
 *
 */
 
struct payload {
    vec4 result;
};

layout(std430, set = 1, binding = 2) buffer pickingdata {
    int[250] result;
};


