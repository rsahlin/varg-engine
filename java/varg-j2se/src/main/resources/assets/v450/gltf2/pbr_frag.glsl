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
 * v = dot product (cos angle)
 * r = roughness
 */
float GA_SMITH( in float v, in float r) {
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
    brdf.orma.rgb = material.ormp.rgb;
    brdf.orma.a = material.properties.r;
}

void setupPBRMaterial(in f16vec3 orm) {
    brdf.orma.rgb = orm;
    brdf.orma.a = mix(material.properties.r, METAL_ABSORPTION, orm.b);
}


void setupPBRMaterial(const float16_t occlusion) {
    brdf.orma.rgb = material.ormp.rgb;
    brdf.orma.r = occlusion;
    brdf.orma.a = material.properties.r;
}

void setupPBRMaterial(const float16_t occlusion, in f16vec4 rm) {
    brdf.orma.r = occlusion;
    brdf.orma.gb = rm.gb;
    brdf.orma.a = mix(material.properties.r, METAL_ABSORPTION, rm.b);
}


void setupPBRMaterial(in f16vec2 rm) {
    brdf.orma = material.ormp;
    brdf.orma.g = rm.x;
    brdf.orma.b = rm.y;
    brdf.orma.a = mix(material.properties.r, METAL_ABSORPTION, rm.y);
    
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
