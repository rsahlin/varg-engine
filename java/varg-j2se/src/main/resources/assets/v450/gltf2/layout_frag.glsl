#define INCLUDED_LAYOUT_FRAG

#include "defines_frag.glsl"

#ifndef INCLUDED_LAYOUT
#include "layout.glsl"
#endif
// Texture bindings

#ifdef CUBEMAP
layout(set = CUBEMAP_TEXTURE_SET, binding = GLTF_BINDING) uniform samplerCubeArray uEnvironmentTexture[CUBEMAP];
#endif
layout(set = MATERIAL_TEXTURE_SET, binding = GLTF_BINDING) uniform sampler2DArray uTexture[MATERIAL_SAMPLER_COUNT];

layout(location = TEXCOORD_LOCATION_OUT) in vec2 vTexCoord[MAX_TEXTURE_COORDINATES];
layout(location = SURFACE_LOCATION_OUT) in Surface surface;
layout(location = PRIMITIVE_LOCATION_OUT) flat in Instance instance;
layout(location = TANGENTLIGHT_LOCATION_OUT) in mat3 mTangentLight;
layout(location = BASECOLOR_LOCATION_OUT) in vec4 vBaseColor;
#ifdef DIRECTIONAL
  layout(location = DIRECTIONAL_LOCATION_OUT) in vec4 vDirectionalLight[DIRECTIONAL];
#endif
#ifdef POINT
  layout(location = POINT_LOCATION_OUT) in vec4 vPointLight[POINT];
#endif
//Fragment output
layout(location = FRAGCOLOR_LOCATION_OUT) out vec4 fragColor;

BRDF brdf;
Shading shading;
