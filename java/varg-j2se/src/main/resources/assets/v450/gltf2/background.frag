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

#include "common_structs.glsl"
#include "layout_frag.glsl"
#include "pbr_frag.glsl"

#ifdef BACK_CUBEMAP
#include "envmap_frag.glsl"
#endif

void main() {
#ifdef BACK_CUBEMAP
//    vec3 view = vec3(surface.normal.xy, sqrt(1.0 - (surface.normal.x * surface.normal.x + surface.normal.y * surface.normal.y)));
    vec3 view = surface.normal;
    vec3 background = getBackground(0.0, uniforms.cubemaps[0].cubeMapInfo.y, view);
#elif BACK_SH
    vec3 background = max(vec3(0), irradiance(uniforms.irradianceCoefficients, normalize(surface.normal)).rgb);
#else
    vec3 background = uniforms.directionallight[0].color.rgb * uniforms.directionallight[0].a;
#endif
    fragColor = vec4(displayencode(background, uniforms.displayEncoding.a), 1);

}