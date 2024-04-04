#define INCLUDED_DEFINES_FRAG
#ifndef INCLUDED_DEFINES
#include "defines.glsl"
#endif

// out fragment locations
#define FRAGCOLOR_LOCATION_OUT 0


 #ifdef KHR_texture_transform
#define GETTEXCOORD(INDEX) INDEX.w > -1 ? (vec4(vTexCoord[INDEX.z], INDEX.y, 1) * uvmatrix.matrix[INDEX.w]).xyz : vec3(vTexCoord[INDEX.z], INDEX.y)
#else
#define GETTEXCOORD(INDEX) vec3(vTexCoord[INDEX.z], INDEX.y)
#endif

#define GETTEXTURE(INDEX) texture(uTexture[INDEX.x], GETTEXCOORD(INDEX))
#define GETTEXTURE_RGB(INDEX) vec3(texture(uTexture[INDEX.x], GETTEXCOORD(INDEX)))
#define GETTEXTURE_GB(INDEX) texture(uTexture[INDEX.x], GETTEXCOORD(INDEX)).gb
#define GETNORMALTEXTURE(INDEX) (vec3((texture(uTexture[INDEX.x], GETTEXCOORD(INDEX)) * 2.0 - 1.0)) * vec3(material.scaleFactors.a, material.scaleFactors.a, 1.0))
//#define GETNORMALTEXTURE(INDEX) (vec3((texture(uTexture[INDEX.x], GETTEXCOORD(INDEX)) * 2.0 - 1.0)))
