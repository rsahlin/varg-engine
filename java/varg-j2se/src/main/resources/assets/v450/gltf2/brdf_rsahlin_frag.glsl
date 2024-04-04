/**
 * Functions for gltf PBR model using custom brdf
 */ 

#ifdef CUBEMAP
#include "envmap_frag.glsl"
#endif

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
    return clamp(((m3) * x ) / (t * pow(m * x2 - x2 + (m2), 2)), 0.0, 1.0);
}

float D_GGX(float NdotH) {
    vec3 NxH = cross(brdf.normal, brdf.H);
    float linearRoughness = max(0.01, brdf.orm.g);
    float a = NdotH * linearRoughness;
    float k = linearRoughness / (dot(NxH, NxH) + a * a);
    return  k * k * (1.0 / pi);
}

float D_GLTF(vec3 NxH) {
    float a = max(0.0001, brdf.orm.g * brdf.orm.g);
    float a2 = a * a;
    float reflectedLobe = 1.0 - dot(NxH, NxH);
    return ((a2) / (pi * pow((reflectedLobe * (a2 - 1.0) + 1.0),2.0)));
}

float V_GLTF() {
    float k = brdf.orm.g * sqrtTwoByPi;
    float GL = clamp(brdf.HdotL / (brdf.HdotL * (1.0 - k) + k), 0.0, 1.0);
    float GN = clamp(brdf.NdotH /(brdf.NdotH * (1.0 - k) + k), 0.0, 1.0);
    return (GL * GN) / max(1.0, 4.0 * brdf.NdotL * brdf.NdotV);
}


float D_BLINN(float NdotH, float a) {
    float a2 = a * a;
    float oneByA2 = (1.0 / (pi * a2));
    return oneByA2 * pow(NdotH, (2 / a2) - 2); 
}

float D_BECKMAN(float NdotH, float a) {
    float NdotHSqr = NdotH*NdotH;
    return max(0.001,(1.0 / (3.1415926535*a*NdotHSqr*NdotHSqr)) * exp((NdotHSqr-1)/(a*NdotHSqr)));
}

float D_GTR1(float NdotH, float a) {
    float a2 = a*a;
    float t = 1 + (a2-1)*NdotH*NdotH;
    return (a2-1) / (pi*log(a2)*t);
}

float D_GTR2(float NdotH, float a) {
    float a2 = a*a;
    float t = 1 + (a2-1)*NdotH*NdotH;
    return a2 / (pi * t*t);
}



// Smith Joint GGX
// Note: Vis = G / (4 * NdotL * NdotV)
// see Eric Heitz. 2014. Understanding the Masking-Shadowing Function in Microfacet-Based BRDFs. Journal of Computer Graphics Techniques, 3
// see Real-Time Rendering. Page 331 to 336.
// see https://google.github.io/filament/Filament.md.html#materialsystem/specularbrdf/geometricshadowing(specularg)
float V_GGX() {
    float alphaRoughness = brdf.orm.b * brdf.orm.b;
    float alphaRoughnessSq = alphaRoughness * alphaRoughness;

    float GGXV = brdf.NdotL * sqrt(brdf.NdotV * brdf.NdotV * (1.0 - alphaRoughnessSq) + alphaRoughnessSq);
    float GGXL = brdf.NdotV * sqrt(brdf.NdotL * brdf.NdotL * (1.0 - alphaRoughnessSq) + alphaRoughnessSq);

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

float GTR1(float NdotH, float a) {
    float a2 = a*a;
    float t = 1 + (a2)*NdotH*NdotH;
    return (a2-1) / (log(a2)*t);
}

float F_SCHLICK(in float u, in float n) {
    float m = 1 - u;
    float m2 = m * m;
    return n + (1 - n) * m2*m2*m; // pow(m,5)
}

float GA_SCHLICK(in float m, in float v) {
    //k should be precomputed
    float k = sqrt(2 * m * m / pi);
    return v / (v - k * v + k);
}

/**
 * t = HdotN
 * m = roughness
 */
float ND_SCHLICK(in float t, in float m) {
    float x = t + m - 1;
    float x2 = x * x;
    return (m*m*m*x) / (t * pow(m*x2 - x2 + m2,2));
}
/** 
 * t = HdotN
 * r = roughness
 */
float NDZ_SCHLICK(in float t, in float r) {
    r = max(0.0001, r);
    float t2 = t * t;
    float z = 1 + r * t2 - t2;
    return r / (z * z);
}

float NDF_CUSTOM(in float RdotV, in float r) {
    return pow(RdotV, 5.0);
}


float NDF_GLTF(in float a, in vec3 NxH) {
    float a2 = a * a;
//    float a2 = a;
    float reflectedLobe = 1.0 - dot(NxH, NxH);
    return min(4 * pi, ((a2) / (pi * pow((reflectedLobe * (a2 - 1.0) + 1.0),2.0))));
}


/**
 * p = isotropy
 * w = TdotH (tangent dot(reflect(H,N) )
 */
float NDA_SCHLICK(in float p, in float w) {
    float w2 = w * w;
    float p2 = p * p;
    return sqrt( p / (p2 - p2 * w2 + w2));
}

vec3 fresnelNdotL(in vec3 F0, in vec3 F90, in float NdotL) {
    float a2 = NdotL * NdotL;
    return mix(F0, F90, 1 - a2 * a2 * NdotL);
}


float getFresnelReflection() {
//    float R0 = pow(1 - 1.5 / 1 + 1.5, 2);
//    float schlickReflect = R0 + (1 - R0) * pow(1 - brdf.NdotV, 5);
    float n1 = 1;
    float n2 = 1.5;
    float n1sin = (n1 / n2) * (1 - brdf.NdotL);
    float pn1 = n1 * sqrt(1 - n1sin * n1sin);
    float n2cos = n2 * brdf.NdotL;
    float fresnelReflection = pn1 - n2cos / pn1 + n2cos;
    return fresnelReflection * fresnelReflection;
}

float getFresnelCookTorrance(in float ior) {
    float c2 = brdf.HdotV * brdf.HdotV;
    float n2 = ior * ior;
    float g2 = n2 + c2 - 1;
    float g = sqrt(g2);
    return 0.5 * (pow(g - brdf.HdotV, 2) / pow(g + brdf.HdotV, 2)) 
    * (1 + (pow(brdf.HdotV * (g + brdf.HdotV) - 1, 2) / pow(brdf.HdotV * (g - brdf.HdotV), 2)));
}

float getFresnelFactor(in float R0, in float NdotL) {
    brdf.debug.r = F_SCHLICK(max(0, NdotL), R0);
    return brdf.debug.r;
}
float getNormalDistributionFactor(in float a) {
     vec3 NxH = cross(brdf.normal, brdf.H);
    brdf.debug.g = NDF_GLTF(a, NxH);
    return brdf.debug.g;
}

float getGeometricAttenuationFactor(in float a, in float dot) {
    return GA_SMITH(a, dot);
}
float getGeometricAttenuationFactor(in float a) {
    brdf.debug.b = GA_SMITH(a, brdf.NdotL) * GA_SMITH(a, brdf.NdotV);
    return brdf.debug.b;
}


vec4 brdf_main(in vec4 baseColor) {
    // Reflected light at F0 - for a dielectric this is the IOR (defaults to 1.5 = 0.04%)
    // For metals this is 1 - all light count as being reflected, although it is 'tinted' by metals baseColor
    // Use power reflectance at normal incidence R0, where R0 = ((n1 - n2) / (n1 + n2))^2 
    // Light that is not reflected is transmitted (into the surface of the material, in most cases directly re-emitted)
    vec3 transmittedColor = mix(baseColor.rgb, vec3(0), brdf.orm.b).rgb;
    vec3 reflectedColor = mix(vec3(1), baseColor.rgb, brdf.orm.b).rgb;
    float R0 = mix(float(material.ormp.a), 1.0, brdf.orm.b);
    float a = max(0.001, brdf.orm.g * brdf.orm.g);
    brdf.colors[REFLECTED_COLOR_INDEX] = vec3(0);
    brdf.colors[TRANSMITTED_COLOR_INDEX] = vec3(0);
    float NdotVPower = getFresnelFactor(R0, brdf.NdotV);
#ifdef BLEND 
    // Transmission extension is used, absorbFactor is the amount of light scattered as light passes through the material.
    float absorbFactor = material.absorbFactor.r / 255.0;
    float alpha = NdotVPower * (1.0 - absorbFactor) + absorbFactor;
#else
    float alpha = 1.0;
#endif

 
#ifdef DIRECTIONAL
    for (int lightNumber = 0; lightNumber < DIRECTIONAL_LIGHT_COUNT; lightNumber++) {
        brdf.NdotL = max(0, dot(brdf.normal, -uniforms.directionallight[lightNumber].direction.xyz));
        float RPower = getFresnelFactor(R0, brdf.NdotL);
        float oneMinusRP = 1.0 - RPower;
        if (brdf.NdotL >= 0) {
            getPerPixelBRDFDirectional(uniforms.directionallight[lightNumber].direction.xyz);
            vec3 illumination = vec3((uniforms.directionallight[lightNumber].color.rgb * uniforms.directionallight[lightNumber].color.a));
            //Here we assume transmitted light enters the material and exits evenly over the hemisphere (/ PI), in the baseColor of the material
            //As the solid angle from lightsource decreases (the angle to the lightsource increases) the power goes down.
            //Note that there is no distinction here based on metalness since this is already given by RPower
            //Pointlights in glTF does not specify solid angle - this means we have to fudge the normal distribution function
            //We do this by choosing a max return value greater than 1.0
            float ndf = getNormalDistributionFactor(a);
            float gaf = getGeometricAttenuationFactor(a);
#ifdef BLEND
            //Amount that will enter into surface: (1 - RPower) - when the light exits the material (1 - RPower) will be left, of that (RPower * absorbFactor) will reflect back and exit the material
            float absorb = oneMinusRP * absorbFactor;
            float reemit = (oneMinusRP - absorb) * RPower;
            float retransmit = reemit * absorbFactor;
            brdf.colors[TRANSMITTED_COLOR_INDEX] += (absorb + retransmit) * mix(0, oneByTwoPi, gaf) * transmittedColor * illumination;
            brdf.colors[REFLECTED_COLOR_INDEX] += (RPower + (reemit - retransmit)) * ndf * gaf * (reflectedColor * illumination);
#else
            brdf.colors[TRANSMITTED_COLOR_INDEX] += oneMinusRP * mix(0, oneByTwoPi, gaf) * transmittedColor * illumination;
            brdf.colors[REFLECTED_COLOR_INDEX] += RPower * ndf * gaf * (reflectedColor * illumination);
#endif
        } 
    }
#endif
#ifdef POINT
    for (int lightNumber = 0; lightNumber < POINT_LIGHT_COUNT; lightNumber++) {
        getPerPixelBRDFDirectional(vPointLight[lightNumber].xyz);
        float FV = getSchlick(brdf.NdotV);
        float FL = getSchlick(brdf.NdotL);
        float Fd90 = 0.5 + 2 * (brdf.HdotL* brdf.HdotL) * a;
        float Fd = mix(1.0, Fd90, FL) * mix(1.0, Fd90, FV);
        vec3 c_diff = Fd * baseColor.rgb * (1 - brdf.orm.b);
        float intensity = uniforms.pointlight[lightNumber].color.a / pow(vPointLight[lightNumber].w,2);
        vec3 illumination = vec3((uniforms.pointlight[lightNumber].color.rgb * intensity) * max(0, brdf.NdotL));
        brdf.colors[TRANSMITTED_COLOR_INDEX] += GETTRANSMITTED;
        brdf.colors[REFLECTED_COLOR_INDEX] += brdf.fresnel * disney_ndf(a, anisotropic, uniforms.directionallight[lightNumber].direction.xyz) * illumination;
    }
#endif
#ifdef CUBEMAP
    vec3 reflection = getReflection(brdf.orm.g, uniforms.cubemaps[0].cubeMapInfo.y);
    brdf.colors[REFLECTED_COLOR_INDEX] += NdotVPower * reflectedColor * reflection;
#endif
#ifdef CUBEMAP_SH
    float gaf = GA_SMITH(a, brdf.NdotV);
#ifdef BLEND
    brdf.colors[TRANSMITTED_COLOR_INDEX] += absorbFactor * brdf.orm.r * mix(transmittedColor, reflectedColor, brdf.orm.b) *  mix(0, oneByTwoPi, gaf) * surface.irradiance.rgb;
#else
    brdf.colors[TRANSMITTED_COLOR_INDEX] += brdf.orm.r * mix(transmittedColor, reflectedColor, brdf.orm.b) *  mix(0, oneByTwoPi, gaf) * surface.irradiance.rgb;
#endif
#ifndef CUBEMAP
    //Fallback to using sh coefficients for metal, otherwise it will end up black.
    vec3 reflect = reflect(surface.V, brdf.normal);
    brdf.colors[REFLECTED_COLOR_INDEX] += (NdotVPower * brdf.orm.b) * reflectedColor * irradiance(uniforms.irradianceCoefficients, reflect).rgb *  mix(0, oneByTwoPi, gaf) * brdf.orm.r;
#endif
#endif
    return vec4(brdf.colors[TRANSMITTED_COLOR_INDEX] + brdf.colors[REFLECTED_COLOR_INDEX], alpha);
}


