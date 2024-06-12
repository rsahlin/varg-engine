#define INCLUDED_DEFINES
/**
 * Defines shared by shader stages
 * Usage in rendering must match these defines
 *
 */

#define GLTF_BINDING 0
#define UNIFORM_GLOBAL_SET 0
#define UNIFORM_TEXTURE_TRANSFORM_SET 1
#define UNIFORM_MATRIX_SET 2
#define UNIFORM_MATERIAL_SET 3
#define MATERIAL_TEXTURE_SET 4
#define CUBEMAP_TEXTURE_SET 5
#define PRIMITIVE_SET 6
 
#define BASECOLOR_TEXTURE_INDEX 0
#define NORMAL_TEXTURE_INDEX 1
#define MR_TEXTURE_INDEX 2
#define OCCLUSION_TEXTURE_INDEX 3
#define ORM_TEXTURE_INDEX 2
#define EMISSIVE_TEXTURE_INDEX  4
#define TRANSMISSION_INDEX 5
#define COAT_FACTOR_INDEX 6
#define COAT_NORMAL_INDEX 7
#define COAT_ROUGHNESS_INDEX 8
#define SCATTERED_TRANSMISSION_INDEX 9
#define SCATTERED_TRANSMISSION_COLOR_INDEX 10
#define PBR_TEXTURE_COUNT 11

#define MAX_TEXTURE_COORDINATES 3

#define MAT3_LOCATIONS 3

// out vertex locations (in fragment)
#define NORMAL_LOCATION_OUT 0
#define TANGENT_LOCATION_OUT 1
#define TEXCOORD_LOCATION_OUT 2
#define SURFACE_LOCATION_OUT TEXCOORD_LOCATION_OUT + MAX_TEXTURE_COORDINATES
#define PRIMITIVE_LOCATION_OUT SURFACE_LOCATION_OUT + SURFACE_LOCATIONS
#define TANGENTLIGHT_LOCATION_OUT PRIMITIVE_LOCATION_OUT + PRIMITIVE_LOCATIONS
#define BASECOLOR_LOCATION_OUT TANGENTLIGHT_LOCATION_OUT + MAT3_LOCATIONS

#define DIRECTIONAL_LOCATION_OUT BASECOLOR_LOCATION_OUT + 2

#ifdef DIRECTIONAL
#define POINT_LOCATION_OUT DIRECTIONAL_LOCATION_OUT + 1
#else
#define POINT_LOCATION_OUT DIRECTIONAL_LOCATION_OUT
#endif
