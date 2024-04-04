#version 450
precision highp float;

#extension GL_GOOGLE_include_directive : enable
#extension GL_EXT_scalar_block_layout : enable

#include "common_structs.glsl"
#include "layout_vert.glsl" 
#include "pbr_vert.glsl"


/* 
 * Vertex shader for textured glTF asset without texture, with normal map and possibly metallicRoughness
 */
void main() {
    instance.primitive = primitives.index[gl_InstanceIndex];
    positionLightTexture();
}
