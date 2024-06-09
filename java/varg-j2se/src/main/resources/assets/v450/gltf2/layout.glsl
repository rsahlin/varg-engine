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
 * Size of material struct must match alilgnment of the first datatype, for instance 16 bytes if it's a vec4, 8 bytes if it's a f16vec4
 */
struct Material {
    //occlusion, roughness, metallic
    f16vec4 orm;
    //emissive factor [RGB], normalscale
    f16vec4 scaleFactors;
    f16vec4[2] materialColor;
    //absorbfactor, coatfactor, coatroughness, reflectionFactor
    //metal F0, dielectric F0, coat metal F0, coat dielectric F0 
    f16vec4[2] properties; 
    u8vec4[PBR_TEXTURE_COUNT] samplersData;
    u8vec4 padding;
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

/**
 * color.rgb = light color
 * color.a = intensity
 * direction.xyz = vector
 */
struct DirectionalLight {
    vec4 color;
    vec3 direction;
    f16vec2 property;
};

/**
 * color.rgb = light color
 * color.a = intensity
 * position.xyz = position
 */
struct PointLight {
    vec4 color;
    vec3 position;
    f16vec2 property;
};

struct Environment {
    // x = mipmap levels, y = intensity factor, z = sh background scale
    vec4 cubeMapInfo;
    vec4 boxMin;
    vec4 boxMax;
} cubemap;

/**
 * Beware padding between variables - put all float variables first - then f16 and last int8
 * Since scalar block layout is enabled variables can be on byte offsets of the datatype - beware when mixing float, f16 and int8
  * This may lead to padding if a wider datatype follows a smaller - for instance putting vec4 after f16vec4 - vec4 must be 16 byte aligned.
  * f16vec4 must be 8 byte aligned.
 */
layout(std430, set = UNIFORM_GLOBAL_SET, binding = GLTF_BINDING) uniform globaluniformstruct {
    mat4[2] vpMatrix;       //0 = view, 1 = projection
    vec4[2] camera;         //0 = camera position, 1 = viewvectors for reflection map background
    Environment[MAX_CUBEMAPS] cubemaps;
    DirectionalLight[MAX_D_LIGHTS] directionallight;
    PointLight[MAX_P_LIGHTS] pointlight;
    f16vec4 displayEncoding;  //Color primaries Ry,Gy,By + max white
    f16vec4 brdfProperties; //NDFFactor
    f16vec4[9] irradianceCoefficients;
} uniforms;

#ifdef KHR_texture_transform
layout(std430, set = UNIFORM_TEXTURE_TRANSFORM_SET, binding = GLTF_BINDING) uniform texturetransformstruct {
    mat4[TEXTURE_TRANSFORM_COUNT] matrix;
} uvmatrix;
#endif
