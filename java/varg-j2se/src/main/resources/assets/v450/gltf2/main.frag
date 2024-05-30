#version 450
precision highp float;

/**
 * Main fragment shader entrypoint, always call the setupPBRMaterial() functions which setup BRDF values - 
 * and then calls the brdf_main() method in the included brdf variant (rsahlin, disney, etc)
 */
 
#extension GL_GOOGLE_include_directive : enable
#extension GL_EXT_scalar_block_layout : enable

#include "common_structs.glsl"
#include "layout_frag.glsl"
#include "pbr_frag.glsl"

#ifdef BRDF_DISNEY
#include "brdf_disney_frag.glsl"
#elif BRDF_RSAHLIN
#include "brdf_rsahlin_frag.glsl"
#endif


void main() {
    // TODO - do not fetch each fragment?
    material = materials.material[instance.primitive.x];
    vec3 toView = normalize(uniforms.camera[0].xyz - surface.position);
    
#ifdef NORMAL
    getPerPixelBRDF(vec3(GETNORMALTEXTURE(material.samplersData[NORMAL_TEXTURE_INDEX])) * mTangentLight, toView);
#else
    getPerPixelBRDF(normalize(surface.normal), toView);
#endif
#ifdef ORM
    setupPBRMaterial(f16vec3(GETTEXTURE(material.samplersData[ORM_TEXTURE_INDEX]).rgb));
#elif METALLICROUGHNESS
#ifdef OCCLUSION
    //occlusion + mr in different textures
    setupPBRMaterial(float16_t(GETTEXTURE(material.samplersData[OCCLUSION_TEXTURE_INDEX]).r), f16vec4(GETTEXTURE(material.samplersData[MR_TEXTURE_INDEX])));
#else
    //Metallicroughness but not occlusion
    setupPBRMaterial(f16vec2(GETTEXTURE_GB(material.samplersData[MR_TEXTURE_INDEX])));
#endif
#elif OCCLUSION
    // Occlusion but not MR
    setupPBRMaterial(float16_t(GETTEXTURE(material.samplersData[OCCLUSION_TEXTURE_INDEX]).r));
#else
    //No pbr texture channels
    setupPBRMaterial();
#endif

    float16_t metal = float16_t(brdf.orma.b);
#ifdef BASECOLOR
    f16vec4 basecolor = f16vec4(GETTEXTURE(material.samplersData[BASECOLOR_TEXTURE_INDEX]));
    vec4 pixel = brdf_main(mix(f16vec4(vMaterialColor[0]) * basecolor, f16vec4(0.0), metal).rgb, f16vec4(vMaterialColor[1]).rgb, toView);
#else
    vec4 pixel = brdf_main(mix(f16vec4(vMaterialColor[0]),f16vec4(0.0), metal).rgb, f16vec4(vMaterialColor[1]).rgb, toView);
#endif

#ifdef EMISSIVE
    outputPixel(pixel + vec4(mix(F16_ONE, oneByTwoPi, brdf.NdotV)) * GETTEXTURE(material.samplersData[EMISSIVE_TEXTURE_INDEX]).rgb * material.scaleFactors.rgb, 0));
#else
    outputPixel(pixel + vec4(mix(F16_ONE, oneByTwoPi, brdf.NdotV)) * vec4(material.scaleFactors.rgb, 0));
#endif
}
