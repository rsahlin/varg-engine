#version 450
precision highp float;

#extension GL_GOOGLE_include_directive : enable
#include "defines.glsl"
#include "layout.glsl"

layout(location = POSITION_LOCATION) in vec3 POSITION;
layout(location = TEXCOORD_LOCATION) in vec2 TEXCOORD[MAX_TEXTURE_COORDINATES];
layout(location = NORMAL_LOCATION) in vec3 NORMAL;
layout(location = TANGENT_LOCATION) in vec4 TANGENT;
layout(location = BASECOLOR_LOCATION) in vec4 COLOR_0;

layout(location = 0) out vec4 vNormal;
layout(location = 1) out vec4 vTangent;
layout(location = 2) out vec4 vBiTangent;
layout(location = 3) out vec3 vNormalFace;

const float scale = 0.01;

/**
 * Used to debug computed vectors, for instance the tangent, bitangent, normal vectors.
 * 
 */
void main() {
    //TODO - this shall be same calculations as in pbrvertex
    gl_Position = vec4(vec4(POSITION, 1.0) * matrix.uModelMatrix[0] * matrix.uModelMatrix[1]) * matrix.uModelMatrix[2];
    vNormalFace = NORMAL * mat3(matrix.uModelMatrix[0]) * mat3(matrix.uModelMatrix[1]);
    
    vec4 bitangent = vec4(TANGENT.w * cross(NORMAL, TANGENT.xyz), 1.0);
    vNormal = scale * (vec4(vNormalFace, 1.0) * matrix.uModelMatrix[2]);
    vTangent = scale * (vec4(TANGENT.xyz, 1.0) * matrix.uModelMatrix[0] * matrix.uModelMatrix[1] * matrix.uModelMatrix[2]);
    vBiTangent = scale * (bitangent * matrix.uModelMatrix[0] * matrix.uModelMatrix[1] * matrix.uModelMatrix[2]);
}

