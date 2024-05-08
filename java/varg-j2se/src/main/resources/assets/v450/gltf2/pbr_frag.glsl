/**
 * Common functions for microfacet pbr (gltf) rendering, always included regardless of which brdf that is used.
 * Entrypoint are the
 * setupPBRMaterial() methods.
 * These sets up roughness, metalness, occlusion depending on textures
 */

#ifndef INCLUDED_PBR
#include "pbr.glsl"
#endif

#ifndef INCLUDED_DISPLAYENCODE
#include "displayencode.glsl"
#endif

/** 
 * Geometric attenuation factor
 * r = roughness
 * v = dot product (cos angle)
 */
float GA_SMITH(in float r, in float v) {
    return v / (r - r * v + v);
}
/**
 * Call once for fragment shader - then call getPerPixelBRDFDirectional() for each lightsource.
 */
void getPerPixelBRDF(in vec3 normal, in vec3 toView) {
    brdf.normal = normal;
    brdf.NdotV = max(0, dot(normal, toView));
}

void getPerPixelBRDFDirectional(in vec3 lightDirection, in vec3 toView) {
    brdf.reflectedLight = reflect(lightDirection, brdf.normal);
    brdf.H = normalize(-lightDirection + toView);
    brdf.HdotV = max(0, dot(brdf.H, toView));
    brdf.NdotH = max(0, dot(brdf.normal, brdf.H));
    brdf.HdotL = max(0, dot(brdf.H, -lightDirection));
}
 
/**
 * This is used for materials that do not have normal/metallicrough/occlusion texture maps
 */
void setupPBRMaterial() {
    brdf.ormp = material.ormp;
}

void setupPBRMaterial(const float occlusion) {
    brdf.ormp = material.ormp;
    brdf.ormp.r = occlusion;
}

void setupPBRMaterial(const float occlusion, in vec4 rm) {
    brdf.ormp = material.ormp;
    brdf.ormp.r = occlusion;
    brdf.ormp.gb = rm.gb;
}


void setupPBRMaterial(in vec2 rm) {
    brdf.ormp = material.ormp;
    brdf.ormp.g = rm.x;
    brdf.ormp.b = rm.y;
}


/**
 * Takes the incoming pbr calculated pixel and applies displayencoding
 * @param pbr The pbr calculated pixel - including alpha from texture if used.
 */
void outputPixel(vec4 pbr) {
#ifdef RAW_BASECOLOR
    fragColor = getBaseColor();
#elif RAW_ORM
    fragColor = vec4(GETTEXTURE(material.samplersData[MR_TEXTURE_INDEX]).rgb, 1.0);
#elif RAW_OCCLUSION
    float o = GETTEXTURE(material.samplersData[OCCLUSION_TEXTURE_INDEX]).r;
    fragColor = vec4(o, o, o, 1.0);
#elif RAW_NORMAL
    fragColor = vec4(GETTEXTURE(material.samplersData[NORMAL_TEXTURE_INDEX]).rgb, 1.0);
#elif RAW_REFLECTED
    fragColor = vec4(displayencode(brdf.colors[REFLECTED_COLOR_INDEX], uniforms.displayEncoding.a), pbr.a);
#elif RAW_TRANSMITTED
    fragColor = vec4(displayencode(brdf.colors[TRANSMITTED_COLOR_INDEX], uniforms.displayEncoding.a), pbr.a);
#elif RAW_FRESNEL
    //Fresnel power function
    fragColor = vec4(displayencode(vec3(brdf.debug.r, 0, 0), 1), pbr.a);
#elif RAW_NDF
    //normal distribution factor
    fragColor = vec4(vec3(0, brdf.debug.g, 0), 1);
#elif RAW_GAF
     //geometric attenuation factor
    fragColor = vec4(vec3(0, 0, brdf.debug.b), 1);
#elif RAW_HSL
    vec3 hsl = rgb2hsl(displayencode(pbr.rgb, uniforms.displayEncoding.a));
    fragColor = vec4(hsl, pbr.a);
#elif RAW_CHROMA
    vec3 chroma = chroma(displayencode(pbr.rgb, uniforms.displayEncoding.a));
    fragColor = vec4(chroma, pbr.a);
#elif RAW_CUBEMAP
    fragColor = vec4(displayencode(brdf.colors[CUBEMAP_COLOR_INDEX], uniforms.displayEncoding.a), 1);
#else 
    fragColor = vec4(displayencode(pbr.rgb, uniforms.displayEncoding.a), pbr.a);
#endif
}
