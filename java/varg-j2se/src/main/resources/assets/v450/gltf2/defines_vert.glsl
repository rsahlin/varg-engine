#define INCLUDED_DEFINES_VERT

#ifndef INCLUDED_DEFINES
#include "defines.glsl"
#endif

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
#ifdef COLOR_0
#define BASECOLOR_LOCATION  TEXCOORD_LOCATION + MAX_TEXTURE_COORDINATES
#endif
