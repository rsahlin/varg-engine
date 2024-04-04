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

void getPerPixelBRDF(in vec3 normal) {
    brdf.normal = normal;
    // Direction of incoming light to the current position
    brdf.NdotV = max(0, dot(normal, surface.V));
}

void getPerPixelBRDFDirectional(in vec3 lightDirection) {
    brdf.reflectedLight = reflect(lightDirection, brdf.normal);
    brdf.H = normalize(-lightDirection + surface.V);
    brdf.HdotV = max(0, dot(brdf.H, surface.V));
    brdf.NdotH = max(0, dot(brdf.normal, brdf.H));
    brdf.HdotL = max(0, dot(brdf.H, -lightDirection));
}


//From the glTF spec:
//The base color has two different interpretations depending on the value of metalness. 
//When the material is a metal, the base color is the specific measured reflectance value at normal incidence (F0). 
//For a non-metal the base color represents the reflected diffuse color of the material.
vec4 getBaseColor() {
#ifdef BASECOLOR
    return vBaseColor * GETTEXTURE(material.samplersData[BASECOLOR_TEXTURE_INDEX]);
 #else
    return vBaseColor;
#endif
}
 
/**
 * This is used for materials that do not have normal/metallicrough/occlusion texture maps
 */
void setupPBRMaterial() {
    brdf.orm = material.ormp.rgb;
}

/**
 * cdiff = lerp(baseColor.rgb * (1 - DIELECTRICSPECULAR.r), black, metallic) 
 * F0 = lerp(DIELECTRICSPECULAR, baseColor.rgb, metallic) 
 * a = roughness ^ 2
 * F = F0 + (1 - F0) * (1.0 - V * H)^5
 */
void setupPBRMaterial(const float occlusion) {
    brdf.orm.r = occlusion;
    brdf.orm.g = material.ormp.g;
    brdf.orm.b = material.ormp.b;
}

void setupPBRMaterial(in vec2 rm) {
    brdf.orm.r = 1;
    brdf.orm.g = rm.x;
    brdf.orm.b = rm.y;
}

void setupPBRMaterial(in vec3 orm) {
    brdf.orm = orm;
}

/**
 * cdiff = lerp(baseColor.rgb * (1 - DIELECTRICSPECULAR.r), black, metallic) 
 * F0 = lerp(DIELECTRICSPECULAR, baseColor.rgb, metallic) 
 * a = roughness ^ 2
 * F = F0 + (1 - F0) * (1.0 - V * H)^5
 */
void setupPBRMaterial(in vec4 occlusion, in vec4 metallicRoughness) {
    brdf.orm.r = occlusion.r;
    brdf.orm.g = metallicRoughness.g;
    brdf.orm.b = metallicRoughness.b;
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
