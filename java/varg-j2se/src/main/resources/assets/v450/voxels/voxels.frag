#version 450

#extension GL_EXT_mesh_shader : require
#extension GL_GOOGLE_include_directive : require

#extension GL_EXT_shader_16bit_storage : require
#extension GL_EXT_shader_8bit_storage : require
// Allows use of float16 arithmetic
#extension GL_EXT_shader_explicit_arithmetic_types_float16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int8 : require 
#extension GL_EXT_scalar_block_layout : enable



layout (location = 0) in VertexInput {
    vec4 color;
} vertexInput;

layout(location = 0) out vec4 outFragColor;

void main() {
    outFragColor = vertexInput.color;
}