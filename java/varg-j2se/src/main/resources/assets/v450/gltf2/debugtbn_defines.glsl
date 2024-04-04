 #extension GL_EXT_scalar_block_layout : enable
 
#define BASECOLOR_TEXTURE_INDEX 0
#define NORMAL_TEXTURE_INDEX 1
#define MR_TEXTURE_INDEX 2
#define OCCLUSION_TEXTURE_INDEX 3
#define EMISSIVE_TEXTURE_INDEX  4

#define MAT3_LOCATIONS 3


// in vertex shader locations - these MUST match the glTF attribute locations
/**
 *       POSITION(0),
 *       NORMAL(1),
 *       TANGENT(2),
 *       TEXCOORD_0(3),
 *       TEXCOORD_1(4),
 *       COLOR_0(5);
 */      
#define POSITION_LOCATION  0
#define NORMAL_LOCATION 1
#define TANGENT_LOCATION 2
// Use an array of samplers
#define TEXCOORD_LOCATION 3
// Use an array for color(s)
#define BASECOLOR_LOCATION  TEXCOORD_LOCATION + MAX_TEXTURE_COORDINATES
