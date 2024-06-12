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
float GA_SMITH( in float16_t v, in float16_t r) {
    v = max(float16_t(0.01), v);
    return v / (r - r * v + v);
}
/**
 * Call once for fragment shader - then call getPerPixelBRDFDirectional() for each lightsource.
 */
void getPerPixelBRDF(in vec3 toView) {

#ifdef NORMAL
    brdf.normal = normalize(vec3(GETNORMALTEXTURE(material.samplersData[NORMAL_TEXTURE_INDEX])) * mTangentLight);
#else
    brdf.normal = normalize(surface.normal);
#endif

#ifdef COAT_NORMAL
    brdf.coatNormal = normalize(vec3(GETNORMALTEXTURE(material.samplersData[COAT_NORMAL_INDEX])) * mTangentLight);
#endif

    brdf.NdotV = float16_t(max(0, dot(brdf.normal, toView)));
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
    brdf.orma.rgb = material.orm.rgb;
    brdf.orma.a = material.layer[0].a;
}

void setupPBRMaterial(in f16vec3 orm) {
    brdf.orma.rgb = orm;
    brdf.orma.a = mix(material.layer[0].a, METAL_ABSORPTION, orm.b);
}


void setupPBRMaterial(const float16_t occlusion) {
    brdf.orma.rgb = material.orm.rgb;
    brdf.orma.r = occlusion;
    brdf.orma.a = material.layer[0].a;
}

void setupPBRMaterial(const float16_t occlusion, in f16vec4 rm) {
    brdf.orma.r = occlusion;
    brdf.orma.gb = rm.gb;
    brdf.orma.a = mix(material.layer[0].a, METAL_ABSORPTION, rm.b);
}


void setupPBRMaterial(in f16vec2 rm) {
    brdf.orma = material.orm;
    brdf.orma.g = rm.x;
    brdf.orma.b = rm.y;
    brdf.orma.a = mix(material.layer[0].a, METAL_ABSORPTION, rm.y);
    
}


/**
 * Takes the incoming pbr calculated pixel and applies displayencoding
 * @param pbr The pbr calculated pixel - including alpha from texture if used.
 */
void outputPixel(vec4 pbr) {
#ifdef RAW_BASECOLOR
    fragColor = vec4(GETTEXTURE(material.samplersData[BASECOLOR_TEXTURE_INDEX]).rgb, 1.0);
#elif RAW_ORM
    fragColor = vec4(brdf.orma.rgb, 1.0);
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
