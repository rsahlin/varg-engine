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
#ifdef NORMAL
    getPerPixelBRDF(vec3(GETNORMALTEXTURE(material.samplersData[NORMAL_TEXTURE_INDEX])) * mTangentLight);
#else
    getPerPixelBRDF(surface.normal);
#endif
#ifdef ORM
    setupPBRMaterial(GETTEXTURE(material.samplersData[MR_TEXTURE_INDEX]));
#elif METALLICROUGHNESS
#ifdef OCCLUSION
    //occlusion + mr in different textures
    setupPBRMaterial(GETTEXTURE(material.samplersData[OCCLUSION_TEXTURE_INDEX]), GETTEXTURE(material.samplersData[MR_TEXTURE_INDEX]));
#else
    //Metallicroughness but not occlusion
    setupPBRMaterial(GETTEXTURE_GB(material.samplersData[MR_TEXTURE_INDEX]));
#endif
#elif OCCLUSION
    // Occlusion but not MR
    setupPBRMaterial(GETTEXTURE(material.samplersData[OCCLUSION_TEXTURE_INDEX]).r);
#else
    //No pbr texture channels
    setupPBRMaterial(vec2(material.ormp.g, material.ormp.b));
#endif
    vec4 pixel = brdf_main(getBaseColor());

#ifdef EMISSIVE
    outputPixel(pixel + vec4(GETTEXTURE(material.samplersData[EMISSIVE_TEXTURE_INDEX]).rgb * material.scaleFactors.rgb, 0));
#else
    outputPixel(pixel + vec4(material.scaleFactors.rgb, 0));
#endif
}
