#define INCLUDED_LAYOUT_VERT

#include "defines_vert.glsl"

#ifndef INCLUDED_LAYOUT
#include "layout.glsl"
#endif

layout(location = POSITION_LOCATION) in vec3 POSITION_ATTRIBUTE;
layout(location = TEXCOORD_LOCATION) in vec2 TEXCOORD_ATTRIBUTE[MAX_TEXTURE_COORDINATES];
layout(location = NORMAL_LOCATION) in vec3 NORMAL_ATTRIBUTE;
layout(location = TANGENT_LOCATION) in vec4 TANGENT_ATTRIBUTE;
#ifdef COLOR_0
layout(location = BASECOLOR_LOCATION) in vec4 COLOR_ATTRIBUTE;
#endif
layout(location = TEXCOORD_LOCATION_OUT) out vec2 vTexCoord[MAX_TEXTURE_COORDINATES];
layout(location = SURFACE_LOCATION_OUT) out Surface surface;
layout(location = PRIMITIVE_LOCATION_OUT) flat out Instance instance;
layout(location = TANGENTLIGHT_LOCATION_OUT) out mat3 mTangentLight;
layout(location = BASECOLOR_LOCATION_OUT) out vec4[2] vMaterialColor;
#ifdef DIRECTIONAL
  layout(location = DIRECTIONAL_LOCATION_OUT) out  vec4 vDirectionalLight[DIRECTIONAL];
#endif
#ifdef POINT
  layout(location = POINT_LOCATION_OUT) out  vec4 vPointLight[POINT];
#endif


/**
 * modelMatrix is a 3 X 4 matrix
 *
 * instanceCustomIndex and mask occupy the same memory as if a single uint32_t was specified in their place
 * instanceCustomIndex occupies the 24 least significant bits of that memory
 * mask occupies the 8 most significant bits of that memory
 *
 * instanceShaderBindingTableRecordOffset and flags occupy the same memory as if a single uint32_t was specified in their place
 * instanceShaderBindingTableRecordOffset occupies the 24 least significant bits of that memory
 *flags occupies the 8 most significant bits of that memory 
 *
 */
struct AccelerationInstance {
    vec4[3] modelMatrix;
    int instanceCustomIndexAndMask;
    int instanceSBTOffsetAndFlags;
    int[2] accelerationStructureReference;
};

layout(std430, set = UNIFORM_MATRIX_SET, binding = GLTF_BINDING) readonly buffer matrixstruct {
    AccelerationInstance[MATRIX_COUNT] accelerationInstance;
} asInstance;

