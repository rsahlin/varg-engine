#version 450
precision highp float;

#extension GL_GOOGLE_include_directive : enable

#include "defines.glsl"

layout(location = 0) in vec4 vNormal[];
layout(location = 1) in vec4 vTangent[];
layout(location = 2) in vec4 vBiTangent[];
layout(location = 3 + 1) in vec3 vNormalFace[];

layout(location = DIFFUSECOLOR_LOCATION_OUT) out vec4 vColor;

/**
 * Geometry shader to output 3 lines for the TBN vectors
 */
precision highp float;

layout(triangles) in;
layout(line_strip, max_vertices = 6) out;

/**
 * Used to draw vector from a position
 */
void main() {
    if (vNormalFace[0].z >= 0.0) {
    
        vColor = vec4(1.0, 0.0, 0.0, 1.0);
        vec4 pos = gl_in[0].gl_Position;
        
        gl_Position = pos;
        EmitVertex();
        gl_Position = pos + vNormal[0];
        EmitVertex();
        EndPrimitive();
        
        vColor = vec4(0.0, 1.0, 0.0, 1.0);
        gl_Position = pos;
        EmitVertex();
        gl_Position = pos + vTangent[0];
        EmitVertex();
        EndPrimitive();
        
        vColor = vec4(0.0, 0.0, 1.0, 1.0);
        gl_Position = pos;
        EmitVertex();
        gl_Position = pos + vBiTangent[0];
        EmitVertex();
        EndPrimitive();
    }
}
