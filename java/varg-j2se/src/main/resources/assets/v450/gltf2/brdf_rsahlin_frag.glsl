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
    m = max(0.0005, m); //This creates the reflection lobe
    float x = t + m - 1.0;
    float x2 = x*x;
    float m2 = m*m;
    float m3 = m*m*m;
    return clamp(((m3) * x ) / (t * pow(m * x2 - x2 + (m2), 2) ), 0.0, 1.0);
}

float D_GGX(float NdotH) {
    vec3 NxH = cross(brdf.normal, brdf.H);
    float16_t linearRoughness = max(float16_t(0.01), brdf.ormp.g);
    float a = NdotH * linearRoughness;
    float k = linearRoughness / (dot(NxH, NxH) + a * a);
    return  k * k * (1.0 / pi);
}

float D_GLTF(vec3 NxH) {
    float16_t a = max(float16_t(0.0001), brdf.ormp.g * brdf.ormp.g);
    float a2 = a * a;
    float reflectedLobe = 1.0 - dot(NxH, NxH);
    return ((a2) / (pi * pow((reflectedLobe * (a2 - 1.0) + 1.0),2.0)));
}

float V_GLTF() {
    float k = brdf.ormp.g * sqrtTwoByPi;
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
    float alphaRoughness = brdf.ormp.g * brdf.ormp.g;
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
    return min(4.0 * pi, ((a2) / (pi * pow((reflectedLobe * (a2 - 1.0) + 1.0),2.0))));
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

const float NDFFactor = 3.14;

/**
 * materialColor[0] = transmittedcolor
 * materialColor[1] = reflectedcolor
 */
vec4 brdf_main(in f16vec3 transmissionColor, in f16vec3 reflectionColor, in vec3 toView) {
    // Use power reflectance at normal incidence R0, where R0 = ((n1 - n2) / (n1 + n2))^2 
    // Light that is not reflected is transmitted (into the surface of the material, in most cases directly re-emitted)
    float R0 = material.ormp.a;
    float16_t absorbFactor = material.properties.r;
    float16_t a = brdf.ormp.g * brdf.ormp.g;
    brdf.colors[REFLECTED_COLOR_INDEX] = vec3(0);
    brdf.colors[TRANSMITTED_COLOR_INDEX] = vec3(0);
    float NdotVPower = getFresnelFactor(R0, brdf.NdotV);
    
#ifdef COAT
#endif
    
#ifdef BLEND 
    // Transmission extension is used, absorbFactor is the amount of light scattered as light passes through the material.
    float alpha = NdotVPower * (1.0 - absorbFactor) + absorbFactor;
#else
    float alpha = 1.0;
#endif
 
#ifdef DIRECTIONAL
    for (int lightNumber = 0; lightNumber < DIRECTIONAL_LIGHT_COUNT; lightNumber++) {
        float intensity = uniforms.directionallight[lightNumber].color.a;
        vec3 lightColor = uniforms.directionallight[lightNumber].color.rgb;
        brdf.NdotL = dot(brdf.normal, -uniforms.directionallight[lightNumber].direction.xyz);
        float RPower = getFresnelFactor(R0, brdf.NdotL);
        float oneMinusRP = 1.0 - RPower;
        
#ifdef COAT
            //Clearcoat max roughness means fully dispersed - no attenuation or matte effect
#ifdef COAT_NORMAL
            vec3 coatNormal = vec3(GETNORMALTEXTURE(material.samplersData[COAT_NORMAL_INDEX])) * mTangentLight;
            float ndotl = dot(coatNormal, -uniforms.directionallight[lightNumber].direction.xyz);
            float CRPower = getFresnelFactor(R0, ndotl);
            vec3 coatReflectedLight = reflect(uniforms.directionallight[lightNumber].direction.xyz, coatNormal);
            float coatView = max(0.0, dot(coatReflectedLight, toView));
            float coatndf = D_SCHLICK(coatView , a);
            
            brdf.colors[REFLECTED_COLOR_INDEX] += CRPower * (coatndf * NDFFactor) * intensity * (reflectionColor * lightColor);
            intensity = intensity * (1.0 - CRPower);
#else
            brdf.colors[REFLECTED_COLOR_INDEX] += RPower * (ndf * NDFFactor) * intensity * (reflectionColor * lightColor);
            intensity = intensity * oneMinusRP;
#endif
#endif
        
        if (brdf.NdotL >= 0) {
            getPerPixelBRDFDirectional(uniforms.directionallight[lightNumber].direction.xyz, toView);
//            vec3 illumination = vec3(uniforms.directionallight[lightNumber].color.a * uniforms.directionallight[lightNumber].color.rgb);
            //Here we assume transmitted light enters the material and exits over the hemisphere based on roughness, in the transmissionColor of the material
            //As the solid angle from lightsource decreases (the angle to the lightsource increases) the power goes down.
            //Note that there is no distinction here based on metalness since this is already given by RPower and absorbtion
            //Pointlights in glTF does not specify solid angle - this means we have to fudge the normal distribution function using the NDFFactor set to a value greater than 1.0
            float view = max(0.0, dot(brdf.reflectedLight, toView));
            float ndf = D_SCHLICK(view , a);
//            float gaf = getGeometricAttenuationFactor(a);
#ifdef BLEND
            //Amount that will enter into surface: (1 - RPower) - when the light exits the material (1 - RPower) will be left, of that (RPower * absorbFactor) will reflect back and exit the material
            float absorb = oneMinusRP * absorbFactor;
            float reemit = (oneMinusRP - absorb) * RPower;
            float retransmit = reemit * absorbFactor;
            brdf.colors[TRANSMITTED_COLOR_INDEX] += (absorb + retransmit) * mix(1.0, oneByTwoPi, a) * intensity * (transmissionColor * lightColor);
            brdf.colors[REFLECTED_COLOR_INDEX] += (RPower + (reemit - retransmit)) * (ndf * NDFFactor) * mix(1.0, 0.0, a) * intensity * (reflectionColor * lightColor);
#else
            brdf.colors[TRANSMITTED_COLOR_INDEX] +=  ((float16_t(1.0) - absorbFactor) *(oneMinusRP * mix(oneByPI, oneByTwoPi, brdf.ormp.g)) * intensity * (transmissionColor * lightColor));
            brdf.colors[REFLECTED_COLOR_INDEX] += RPower * (ndf * NDFFactor) * mix(float16_t(1.0), float16_t(0.0), brdf.ormp.g) * intensity * (reflectionColor * lightColor);
#endif
        }
    }
#endif

#ifdef POINT
    for (int lightNumber = 0; lightNumber < POINT_LIGHT_COUNT; lightNumber++) {
        brdf.NdotL = dot(brdf.normal, -vPointLight[lightNumber].xyz);
        float RPower = getFresnelFactor(R0, brdf.NdotL);
        float oneMinusRP = 1.0 - RPower;
        if (brdf.NdotL >= 0) {
            getPerPixelBRDFDirectional(vPointLight[lightNumber].xyz, toView);
            float intensity = uniforms.pointlight[lightNumber].color.a / pow(vPointLight[lightNumber].w,2);
            vec3 lightColor = uniforms.directionallight[lightNumber].color.rgb;
//            vec3 illumination = vec3(uniforms.pointlight[lightNumber].color.rgb * intensity);
            //Here we assume transmitted light enters the material and exits evenly over the hemisphere (/ PI), in the transmissionColor of the material
            //As the solid angle from lightsource decreases (the angle to the lightsource increases) the power goes down.
            //Note that there is no distinction here based on metalness since this is already given by RPower
            //Pointlights in glTF does not specify solid angle - this means we have to fudge the normal distribution function
            float view = max(0, dot(brdf.reflectedLight, toView));
            float ndf = D_SCHLICK(view , a);
            float gaf = getGeometricAttenuationFactor(a);
#ifdef BLEND
            //TODO -update
            //Amount that will enter into surface: (1 - RPower) - when the light exits the material (1 - RPower) will be left, of that (RPower * absorbFactor) will reflect back and exit the material
            float absorb = oneMinusRP * absorbFactor;
            float reemit = (oneMinusRP - absorb) * RPower;
            float retransmit = reemit * absorbFactor;
            brdf.colors[TRANSMITTED_COLOR_INDEX] += (absorb + retransmit) * mix(1, oneByTwoPi, a) *  * transmissionColor * illumination;
            brdf.colors[REFLECTED_COLOR_INDEX] += (RPower + (reemit - retransmit)) * mix(1, oneByTwoPi, a) * ndf * (reflectionColor * illumination);
#else
            brdf.colors[TRANSMITTED_COLOR_INDEX] += ((float16_t(1.0) - absorbFactor) * (oneMinusRP * mix(oneByPI, oneByTwoPi, brdf.ormp.g)) * intensity * (transmissionColor * lightColor));
            brdf.colors[REFLECTED_COLOR_INDEX] += RPower * (ndf * NDFFactor) * mix(float16_t(1.0), float16_t(0.0), brdf.ormp.g) * intensity * (reflectionColor * lightColor);
#endif
        }
    }
#endif

#ifdef CUBEMAP
    vec3 reflection = getReflection(brdf.ormp.g, uniforms.cubemaps[0].cubeMapInfo.y);
    brdf.colors[REFLECTED_COLOR_INDEX] += NdotVPower * reflectionColor * reflection;
#endif
#ifdef CUBEMAP_SH
    float gaf = GA_SMITH(a, brdf.NdotV);
#ifdef BLEND
    brdf.colors[TRANSMITTED_COLOR_INDEX] += absorbFactor * brdf.ormp.r * transmissionColor *  mix(1, oneByTwoPi, a) * surface.irradiance.rgb;
#else
    brdf.colors[TRANSMITTED_COLOR_INDEX] += brdf.ormp.r * transmissionColor *  mix(float16_t(1.0), oneByTwoPi, a) * surface.irradiance.rgb;
#endif
#ifndef CUBEMAP
#ifdef BLEND
    brdf.colors[REFLECTED_COLOR_INDEX] +=  NdotVPower * reflectionColor * surface.irradiance.rgb;
#else
    //Fallback to using sh coefficients for metal, otherwise it will end up black.
    vec3 reflect = reflect(toView, brdf.normal);
    brdf.colors[REFLECTED_COLOR_INDEX] +=  NdotVPower * (brdf.ormp.b * brdf.ormp.r * mix(float16_t(1.0), float16_t(0.0), a)) * reflectionColor * irradiance(uniforms.irradianceCoefficients, reflect).rgb;
#endif
#endif
#endif
    return vec4(brdf.colors[TRANSMITTED_COLOR_INDEX] + brdf.colors[REFLECTED_COLOR_INDEX], alpha);
}



