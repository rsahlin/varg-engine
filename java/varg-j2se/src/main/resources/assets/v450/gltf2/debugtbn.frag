#version 450
precision highp float;

#extension GL_GOOGLE_include_directive : enable
#include "defines.glsl"

layout(location = DIFFUSECOLOR_LOCATION_OUT) in vec4 vColor;
layout(location = FRAGCOLOR_LOCATION_OUT) out vec4 fragColor;

void main()
{
    fragColor = vColor;
}
