#version 450
precision highp float;

#extension GL_GOOGLE_include_directive : enable
#include "defines.glsl"
#include "common_structs.glsl"
#include "layout_vert.glsl"
#include "pbr_vert.glsl"

/**
 * Used to debug vertices, faces and material on glTF model
 * 
 */
void main() {
    positionLightTexture();
}

