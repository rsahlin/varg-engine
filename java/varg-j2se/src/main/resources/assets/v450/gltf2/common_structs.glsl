#define INCLUDED_COMMON_STRUCTS
/**
 * Source for structs shared between vertex and fragment stage (possibly other stages)
 *
 */
 
const float MIN_ROUGHNESS = 0.00001;
const float MIN_METALLIC = 0.0;
const float MAX_METALLIC = 1.0;
const float REFLECTED_LOBE_TINT = 0.85;
const float pi = 3.141592;
const float sqrtTwoByPi = sqrt(2.0 / pi);
const float oneByPI = 1.0 / pi;
const float twoByPI = 2.0 / pi;
const float oneByTwoPi = 1.0 / (2 * pi);
const float gamma = 2.4;
const float oneByGamma = 1.0 / gamma;
const vec4 BLACK = vec4(0.0, 0.0, 0.0, 0.0);
const float fluxRadius = sqrt(oneByPI);

/**
 * PBR surface properties on a vertex level
 */ 
struct Surface {
    // Per vertex attributes
    vec3 normal;
    // View direction, from world coordinate to eye - normalized
    vec3 V;
    vec3 position;
    vec4 irradiance;
};
#define SURFACE_LOCATIONS 4

struct Instance {
    ivec4 primitive;
};

#define PRIMITIVE_LOCATIONS 1


/**
 * Data needed when shading using the BRDF, used in fragment step
 * Normally calculated per fragment
 */
struct BRDF {
    vec3 normal;
    vec3 reflectedLight;
    vec3 H;
    float NdotH;
    float NdotL;
    float NdotV;
    float HdotL;
    float HdotV;
    //x = ndf, y = gaf
    vec4 debug;
    vec4 ormp;
    vec3[3] colors;
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
