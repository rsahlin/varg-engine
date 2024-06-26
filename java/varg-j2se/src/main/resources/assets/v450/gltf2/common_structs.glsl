#define INCLUDED_COMMON_STRUCTS

// Allows use of float16 arithmetic
#extension GL_EXT_shader_explicit_arithmetic_types_float16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int16 : require 
#extension GL_EXT_shader_explicit_arithmetic_types_int8 : require 

/**
 * Source for structs shared between vertex and fragment stage (possibly other stages)
 *
 */
 
const float16_t pi = float16_t(3.141);
const float16_t sqrtTwoByPi = float16_t(sqrt(2.0 / pi));
const float16_t oneByPI = float16_t(1.0 / pi);
const float16_t twoByPI = float16_t(2.0 / pi);
const float16_t oneByTwoPi = float16_t(1.0 / (2.0 * pi));
const float16_t oneByFourPi = float16_t(1.0 / (4.0 * pi));
const float16_t METAL_ABSORPTION = float16_t(1.0);
const float16_t F16_ZERO = float16_t(0.0);
const float16_t F16_ONE = float16_t(1.0);
const float gamma = 2.4;
const float oneByGamma = 1.0 / gamma;
const float fluxRadius = sqrt(oneByPI);

/**
 * PBR surface properties on a vertex level
 */ 
struct Surface {
    // Per vertex attributes
    vec3 normal;
    vec3 position;
    vec4 irradiance;
};
#define SURFACE_LOCATIONS 3

struct Instance {
    ivec4 primitive;
};

#define PRIMITIVE_LOCATIONS 1


/**
 * Data needed when shading using the BRDF, used in fragment step
 * Normally calculated per fragment
 */
struct BRDF {
    vec3[3] colors;
    vec3 normal;
    vec3 coatNormal;
    vec3 reflectedLight;
    vec3 H;
    float NdotH;
    float HdotL;
    float HdotV;
    //occlusion, roughness, metallic, absorbfactor
    f16vec4 orma;
    float16_t NdotV;
    float16_t NdotL;
};

#define TRANSMITTED_COLOR_INDEX 0
#define REFLECTED_COLOR_INDEX 1
#define CUBEMAP_COLOR_INDEX 2
#define BRDF_COLORS = CUBEMAP_COLOR_INDEX + 1

/**
 * BRDF shading variables, calculated within the BRDF
 */
struct Shading {
    // Normal distribution function
    //x = NDF
    //y = shadowing
    //z = masking
    vec4 visibility;
};
