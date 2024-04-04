/**
 * Functions for gltf PBR model using disney modified
 */ 


float sqr(float val) {
    return val * val;
}

/**
 * t = NdotH
 * H = bisector of incoming and outcoming light
 * m is roughness factor 
 */
float D_SCHLICK(in float t, in float m) {
    m = max(0.001, m);
    float x = t + m - 1;
    float x2 = x*x;
    float m2 = m*m;
    float m3 = m*m*m;
    return max(0, ((m3) * x ) / (t * pow(m * x2 - x2 + (m2), 2)));
}

// The following equation(s) model the distribution of microfacet normals across the area being drawn (aka D())
// Implementation from "Average Irregularity Representation of a Roughened Surface for Ray Reflection" by T. S. Trowbridge, and K. P. Reitz
// Follows the distribution function recommended in the SIGGRAPH 2013 course notes from EPIC Games [1], Equation 3.
float D_GGX(float NdotH, float alphaRoughness) {
    alphaRoughness = max(0.001, alphaRoughness);
    float alphaRoughnessSq = alphaRoughness * alphaRoughness;
    float f = (NdotH * NdotH) * (alphaRoughnessSq - 1.0) + 1.0;
    return min(1, alphaRoughnessSq / (pi * f * f));
}

// Smith Joint GGX
// Note: Vis = G / (4 * NdotL * NdotV)
// see Eric Heitz. 2014. Understanding the Masking-Shadowing Function in Microfacet-Based BRDFs. Journal of Computer Graphics Techniques, 3
// see Real-Time Rendering. Page 331 to 336.
// see https://google.github.io/filament/Filament.md.html#materialsystem/specularbrdf/geometricshadowing(specularg)
float V_GGX(float NdotL, float NdotV, float alphaRoughness)
{
    float alphaRoughnessSq = alphaRoughness * alphaRoughness;

    float GGXV = NdotL * sqrt(NdotV * NdotV * (1.0 - alphaRoughnessSq) + alphaRoughnessSq);
    float GGXL = NdotV * sqrt(NdotL * NdotL * (1.0 - alphaRoughnessSq) + alphaRoughnessSq);

    float GGX = GGXV + GGXL;
    if (GGX > 0.0)
    {
        return 0.5 / GGX;
    }
    return 0.0;
}


float D_PHONG(in float RdotV, float alpha) {
    //kd * LdotN * (im, id) + ks (RdotV)^shininess * im, is
    return pow(RdotV, mix(128.0, 1.0, alpha));
}


float smithG_GGX(in float NdotV, in float alphaG) {
    float a = alphaG * alphaG;
    float b = NdotV * NdotV;
    return min(1,1 / (NdotV + sqrt(a + b - a * b)));
}

float smithG_GGX_aniso(in float NdotV, in float VdotX, in float VdotY, in float ax, in float ay) {
    return 1 / (sqrt(sqr(VdotX*ax) + sqr(VdotY*ay) + sqr(NdotV)));
}



float GTR2(float NdotH, float a) {
    float a2 = a*a;
    float t = 1 + (a2 - 1)*NdotH*NdotH;
    return a2 / (pi * t*t);
}

float GTR1(float NdotH, float a) {
    float a2 = a*a;
    float t = 1 + (a2)*NdotH*NdotH;
    return (a2-1) / (log(a2)*t);
}


float GTR2_aniso(in float NdotH,in float HdotX,in float HdotY,in float ax,in float ay) {
    return (1 / (pi * ax*ay * sqr(sqr(HdotX/ax) + sqr(HdotY/ay) + NdotH*NdotH )));
}

float getSchlick(float u) {
    float m = clamp(1 - u, 0, 1);
    float m2 = m*m;
    return m2*m2*m; // pow(m,5)
}

vec3 fresnelView(in vec3 F0, in vec3 F90) {
      return mix(F0, F90, getSchlick(brdf.HdotL));
}

vec3 fresnelNdotL(in vec3 F0, in vec3 F90, in float NdotL) {
    float a2 = NdotL * NdotL;
    return mix(F0, F90, 1 - a2 * a2 * NdotL);
}

float disney_ndf(in float a, in float anisotropic, in vec3 lightDirection) {
    // radiance = lumen / m2 / projected solid angle.
    float aspect = sqrt(1-anisotropic*.9);
    float ax = max(.001, a/aspect);
    float ay = max(.001, a*aspect);
    vec3 tangent =  normalize( cross( vec3(1,0,0), brdf.normal ) );
    vec3 bitangent = normalize( cross( brdf.normal, tangent ) );
//    float shadowing = smithG_GGX_aniso(brdf.NdotL, dot(lightDirection, tangent), dot(lightDirection, bitangent), ax, ay);
//    float masking = smithG_GGX_aniso(brdf.NdotV, dot(surface.toEye, tangent), dot(surface.toEye, bitangent), ax, ay);
//    shading.NDF = GTR2_aniso(brdf.NdotH, dot(brdf.H, tangent), dot(brdf.H, bitangent), ax, ay) * shadowing * masking;
//    shading.visibility.xyz = vec3(GTR2_aniso(brdf.NdotH, dot(brdf.H, tangent), dot(brdf.H, bitangent),ax, ay), smithG_GGX((brdf.NdotL), a), smithG_GGX((brdf.NdotV), a));
//    shading.visibility.xyz = vec3(GTR2(brdf.NdotH, a), smithG_GGX(( 1- brdf.NdotL), a), smithG_GGX((1 - brdf.NdotV), a));
    shading.visibility.xyz = vec3(D_GGX(brdf.NdotH, a), smithG_GGX(( brdf.NdotL), a), smithG_GGX((brdf.NdotV), a));
//    return max(0, shading.visibility.x * shading.visibility.y * shading.visibility.z);
    return shading.visibility.x;
}
vec4 brdf_main(in vec4 baseColor) {
    vec3 F0 = mix(vec4(0.04), baseColor, brdf.orm.b).rgb;
    // From Disney principled BRDF
    // Diffuse fresnel - go from 1 at normal incidence to .5 at grazing
    // and mix in diffuse retro-reflection based on roughness
    float a = brdf.orm.g * brdf.orm.g;
    brdf.colors[TRANSMITTED_COLOR_INDEX] = vec3(0);
    brdf.colors[REFLECTED_COLOR_INDEX] = vec3(0);
    //Cspec0 is F0
//    vec3 Cspec0 = mix(vec3(0.02*.08), baseColor, brdf.orm.b);
    float Cdlum = 0.3 * baseColor.r + 0.6 * baseColor.g  + 0.1 * baseColor.b; // luminance approx.
#ifdef DIRECTIONAL
    for (int lightNumber = 0; lightNumber < DIRECTIONAL; lightNumber++) {
        brdf.NdotL = dot(brdf.normal, -uniforms.directionallight[lightNumber].direction.xyz);
        if (brdf.NdotL > 0) {
            getPerPixelBRDFDirectional(uniforms.directionallight[lightNumber].direction.xyz);
            float FL = getSchlick(brdf.NdotL);
            float FV = getSchlick(brdf.NdotV);
            float Fd90 = 0.5 + 2 * (brdf.HdotL * brdf.HdotL) * a;
//            float Fd = mix(1.0, Fd90, FL) * mix(1.0, Fd90, FV);
            float Fd = brdf.NdotL;
            vec3 illumination = vec3((uniforms.directionallight[lightNumber].color.rgb * uniforms.directionallight[lightNumber].color.a));
//            brdf.colors[TRANSMITTED_COLOR_INDEX] += illumination * oneByPI * Fd * baseColor * (1 - brdf.orm.b);
            brdf.colors[REFLECTED_COLOR_INDEX] += F0 * disney_ndf(a, 0.0, uniforms.directionallight[lightNumber].direction.xyz) * illumination;
        }
    }
#endif
#ifdef POINT
    for (int lightNumber = 0; lightNumber < POINT; lightNumber++) {
        getPerPixelBRDFDirectional(vPointLight[lightNumber].xyz);
        float FV = getSchlick(brdf.NdotV);
        float FL = getSchlick(brdf.NdotL);
        float Fd90 = 0.5 + 2 * (brdf.HdotL* brdf.HdotL) * a;
        float Fd = mix(1.0, Fd90, FL) * mix(1.0, Fd90, FV);
        vec3 c_diff = Fd * baseColor * (1 - brdf.orm.b);
        float intensity = uniforms.pointlight[lightNumber].color.a / pow(vPointLight[lightNumber].w,2);
        vec3 illumination = vec3((uniforms.pointlight[lightNumber].color.rgb * intensity) * max(0, brdf.NdotL));
        brdf.fresnel = F0 + (1.0 - F0) * getSchlick(brdf.HdotV);
        brdf.colors[TRANSMITTED_COLOR_INDEX] += GETTRANSMITTED;
        brdf.colors[REFLECTED_COLOR_INDEX] += brdf.fresnel * disney_ndf(a, anisotropic, uniforms.directionallight[lightNumber].direction.xyz) * illumination;
    }
#endif
#ifdef CUBEMAP
    vec3 reflect = reflect(-surface.V, brdf.normal);
    getReflection(brdf.orm.g, F0);
    brdf.colors[REFLECTED_COLOR_INDEX] = brdf.colors[CUBEMAP_COLOR_INDEX];
#endif
    return vec4(brdf.colors[TRANSMITTED_COLOR_INDEX] + brdf.colors[REFLECTED_COLOR_INDEX], baseColor.a);
}



