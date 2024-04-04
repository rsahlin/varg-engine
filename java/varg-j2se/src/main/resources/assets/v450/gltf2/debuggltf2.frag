#version 450
precision highp float;

#extension GL_GOOGLE_include_directive : enable
#include "defines.glsl"
#include "common_structs.glsl"
#include "layout_frag.glsl"
#include "pbr_frag.glsl"

void main()
{
    fragColor = vBaseColor;
}
