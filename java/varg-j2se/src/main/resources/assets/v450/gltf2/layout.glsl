#define INCLUDED_LAYOUT

// Allows us to use 16 and 8 bit storage
#extension GL_EXT_shader_16bit_storage : require
#extension GL_EXT_shader_8bit_storage : require
// Allows use of float16 arithmetic
#extension GL_EXT_shader_explicit_arithmetic_types_float16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int8 : require 
#extension GL_EXT_scalar_block_layout : require


/**
 * Layouts shared by shader stages
 *
 * This file depends on knowing structs used in shaders - this means
 * that common_structs is included BEFORE this file.
 */
// The specialization constants MUST be in synk with specialization usage on the client side
//see SpecializationConstant enum
layout (constant_id = 0) const int TEXTURE_TRANSFORM_COUNT = 1;
layout (constant_id = 1) const int MATERIAL_COUNT = 1;
layout (constant_id = 2) const int MATERIAL_SAMPLER_COUNT = 1;
layout (constant_id = 3) const int DIRECTIONAL_LIGHT_COUNT = 1;
layout (constant_id = 4) const int POINT_LIGHT_COUNT = 1;
layout (constant_id = 5) const int MATRIX_COUNT = 1;


/**
 * This must be aligned with the data in glTF material 
 * TODO - change orm , scaleFactors and transmissionColor to ubyte
 */
struct Material {
    //occlusion, roughness, metallic, fresnel power
    f16vec4 ormp;
    //emissive factor [RGB], normalscale
    f16vec4 scaleFactors;
    f16vec4[2] materialColor;
    u8vec4[PBR_TEXTURE_COUNT] samplersData;
    u8vec4 absorbFactor; 
    //Needs padding if texture count is even
} material;


layout(std430, set = UNIFORM_MATERIAL_SET, binding = GLTF_BINDING) readonly buffer materialstruct {
    Material[] material;
} materials;

layout(std430, set = PRIMITIVE_SET, binding = GLTF_BINDING) readonly buffer primitivestruct {
    /** 
     * materialIndex
     * matrixIndex
     */
    ivec4[] index;
} primitives;


struct DirectionalLight {
    vec4 color;
    vec4 direction;
};

struct PointLight {
    vec4 color;
    vec4 position;
};

struct Environment {
    // x = mipmap levels, y = intensity factor
    vec4 cubeMapInfo;
    vec4 boxMin;
    vec4 boxMax;
} cubemap;

layout(std430, set = UNIFORM_GLOBAL_SET, binding = GLTF_BINDING) uniform globaluniformstruct {
    mat4[2] vpMatrix;       //0 = view, 1 = projection
    vec4[2] camera;         //0 = camera position, 1 = viewvectors for reflection map background
    f16vec4 displayEncoding;  //Color primaries Ry,Gy,By + max white
    f16vec4[9] irradianceCoefficients;
    Environment[MAX_CUBEMAPS] cubemaps;
    DirectionalLight[MAX_D_LIGHTS] directionallight;
    PointLight[MAX_P_LIGHTS] pointlight;
} uniforms;

#ifdef KHR_texture_transform
layout(std430, set = UNIFORM_TEXTURE_TRANSFORM_SET, binding = GLTF_BINDING) uniform texturetransformstruct {
    mat4[TEXTURE_TRANSFORM_COUNT] matrix;
} uvmatrix;
#endif
