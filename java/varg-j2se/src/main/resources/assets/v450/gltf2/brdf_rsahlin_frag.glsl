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
 * t = HdotN
 * m = roughness - adjust for min roughness (specular lobe fudge) before
 */
float ND_SCHLICK(in float t, in float16_t m) {
    float x = (t + m) - 1.0;
    float x2 = x * x;
    float m2 = m * m;
    float m3 = m2 + m;
    return clamp((m3*x) / (t * pow(m * x2 - x2 + m2, 2.15) ), 0.0, 1.0);
}

/**
 * dot = NdotV, NdotL
 * m = roughness
 */
float GA_SCHLICK(in float dot, in float16_t m) {
    float k = sqrt((m * m) * float16_t(2.0) / pi);
    return dot / (dot - k * dot + k);
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

/**
 * p = isotropy
 * w = TdotH (tangent dot(reflect(H,N) )
 */
float NDA_SCHLICK(in float p, in float w) {
    float w2 = w * w;
    float p2 = p * p;
    return sqrt( p / (p2 - p2 * w2 + w2));
}


float D_GGX(float NdotH) {
    vec3 NxH = cross(brdf.normal, brdf.H);
    float16_t linearRoughness = max(float16_t(0.01), brdf.orma.g);
    float a = NdotH * linearRoughness;
    float k = linearRoughness / (dot(NxH, NxH) + a * a);
    return  k * k * (1.0 / pi);
}

float D_GLTF(vec3 NxH) {
    float16_t a = max(float16_t(0.0001), brdf.orma.g * brdf.orma.g);
    float a2 = a * a;
    float reflectedLobe = 1.0 - dot(NxH, NxH);
    return ((a2) / (pi * pow((reflectedLobe * (a2 - 1.0) + 1.0),2.0)));
}

float V_GLTF() {
    float k = brdf.orma.g * sqrtTwoByPi;
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
    float alphaRoughness = brdf.orma.g * brdf.orma.g;
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

/**
 * u = dot
 * n = R0
 */
float16_t F_SCHLICK(in float16_t u, in float16_t n) {
    float16_t m = F16_ONE - u;
    float16_t m2 = m * m;
    return n + (F16_ONE - n) * m2*m2*m; // pow(m,5)
}


float NDF_GLTF(in float a, in vec3 NxH) {
    float a2 = a * a;
//    float a2 = a;
    float reflectedLobe = 1.0 - dot(NxH, NxH);
    return min(4.0 * pi, ((a2) / (pi * pow((reflectedLobe * (a2 - 1.0) + 1.0),2.0))));
}



vec3 fresnelNdotL(in vec3 F0, in vec3 F90, in float NdotL) {
    float a2 = NdotL * NdotL;
    return mix(F0, F90, 1 - a2 * a2 * NdotL);
}



float getFresnelCookTorrance(in float ior) {
    float c2 = brdf.HdotV * brdf.HdotV;
    float n2 = ior * ior;
    float g2 = n2 + c2 - 1;
    float g = sqrt(g2);
    return 0.5 * (pow(g - brdf.HdotV, 2) / pow(g + brdf.HdotV, 2)) 
    * (1 + (pow(brdf.HdotV * (g + brdf.HdotV) - 1, 2) / pow(brdf.HdotV * (g - brdf.HdotV), 2)));
}

float16_t getFresnelFactor(in float16_t R0, in float16_t NdotL) {
    return F_SCHLICK(max(F16_ZERO, NdotL), R0);
}
float getNormalDistributionFactor(in float a) {
    vec3 NxH = cross(brdf.normal, brdf.H);
    return NDF_GLTF(a, NxH);
}


/**
 * materialColor[0] = transmittedcolor
 * materialColor[1] = reflectedcolor
 */
vec4 brdf_main(in f16vec3 transmissionColor, in f16vec3 reflectionColor, in vec3 toView) {
    // Use power reflectance at normal incidence R0, where R0 = ((n1 - n2) / (n1 + n2))^2 
    // Light that is not reflected is transmitted (into the surface of the material, in most cases directly re-emitted)
    float16_t R0 = material.ormp.a;
    float16_t absorbFactor = brdf.orma.a;
    float16_t emitFactor = float16_t(1.0) - absorbFactor;
    float16_t NDFFactor = uniforms.brdfProperties.x;
    float16_t a = brdf.orma.g * brdf.orma.g;
    float16_t roughSquared = sqrt(brdf.orma.g);
    float16_t gafView = float16_t(GA_SMITH(brdf.NdotV, a));
    brdf.colors[REFLECTED_COLOR_INDEX] = vec3(0);
    brdf.colors[TRANSMITTED_COLOR_INDEX] = vec3(0);
    float NdotVPower = getFresnelFactor(R0, brdf.NdotV);
    
#ifdef COAT
#endif
    
#ifdef BLEND 
    // Transmission extension is used, absorbFactor is the amount of light scattered as light passes through the material.
    float alpha = NdotVPower * emitFactor + absorbFactor;
#else
    float alpha = 1.0;
#endif
 
#ifdef DIRECTIONAL
    for (int lightNumber = 0; lightNumber < DIRECTIONAL_LIGHT_COUNT; lightNumber++) {
        float intensity = uniforms.directionallight[lightNumber].color.a;
        vec3 lightColor = uniforms.directionallight[lightNumber].color.rgb;
        brdf.NdotL = float16_t(dot(brdf.normal, -uniforms.directionallight[lightNumber].direction.xyz));
        //Will be minor diff at normal incidence, can be fixed by upscaling the specularFactor in material
        float16_t tempf = getFresnelFactor(R0, brdf.NdotL);
        float16_t oneMinusRP = F16_ONE - tempf;
        float16_t RPower = mix(material.properties[1].z, F16_ONE, tempf) * tempf; 
#ifdef COAT
            //Clearcoat max roughness means fully dispersed - no attenuation or matte effect
#ifdef COAT_NORMAL
            vec3 coatNormal = normalize(vec3(GETNORMALTEXTURE(material.samplersData[COAT_NORMAL_INDEX]))) * mTangentLight;
            float16_t ndotl = float16_t(dot(coatNormal, -uniforms.directionallight[lightNumber].direction.xyz));
            tempf = getFresnelFactor(R0, ndotl);
            float16_t CRPower = mix(material.properties[1].z, F16_ONE, tempf) * tempf; 
            vec3 coatReflectedLight = reflect(uniforms.directionallight[lightNumber].direction.xyz, coatNormal);
            float coatView = max(0.0, dot(coatReflectedLight, toView));
            float coatndf = ND_SCHLICK(coatView , material.properties[0].b * material.properties[0].b);
            brdf.colors[REFLECTED_COLOR_INDEX] += CRPower * (coatndf * NDFFactor) * intensity * lightColor;
            intensity = intensity * (1.0 - CRPower);
#else
            brdf.colors[REFLECTED_COLOR_INDEX] += RPower * (ndf * NDFFactor) * intensity * (f16vec3(1.0) * lightColor);
            intensity = intensity * oneMinusRP;
#endif
            //Fresnel power now comes from the coat layer.
            RPower = material.properties[0].a;
#endif
        if (brdf.NdotL >= F16_ZERO) {
            getPerPixelBRDFDirectional(uniforms.directionallight[lightNumber].direction.xyz, toView);
            //Here we assume transmitted light enters the material and exits over the hemisphere based on roughness, in the transmissionColor of the material
            //As the solid angle from lightsource decreases (the angle to the lightsource increases) the power goes down.
            //Note that there is no distinction here based on metalness since this is already given by RPower and absorbtion
            //Pointlights in glTF does not specify solid angle - this means we have to fudge the normal distribution function using the NDFFactor set to a value greater than 1.0
            float view = max(0.0, dot(brdf.reflectedLight, toView));
            float ndf = ND_SCHLICK(view , max(a, uniforms.directionallight[lightNumber].property.x));
#ifdef BLEND
            //Amount that will enter into surface: (1 - RPower) - when the light exits the material (1 - RPower) will be left, of that (RPower * absorbFactor) will reflect back and exit the material
            float absorb = oneMinusRP * absorbFactor;
            float reemit = (oneMinusRP - absorb) * RPower;
            float retransmit = reemit * absorbFactor;
            brdf.colors[TRANSMITTED_COLOR_INDEX] += gafView * (absorb + retransmit) * mix(oneByPI, oneByFourPi, roughSquared) * intensity * (transmissionColor * lightColor);
            brdf.colors[REFLECTED_COLOR_INDEX] += (RPower * gafView + (reemit - retransmit)) * (ndf * NDFFactor) * mix(float16_t(1.0), float16_t(0.0), roughSquared) * intensity * (reflectionColor * lightColor);
#else
            brdf.colors[TRANSMITTED_COLOR_INDEX] +=  gafView * emitFactor * (oneMinusRP * mix(oneByPI, oneByFourPi, roughSquared)) * intensity * (transmissionColor * lightColor);
            //The ndf will disperse the normal using some made up factor, to be correct the intensity shall be reduced by the increase in reflected area.
            //This is not known so use an inverse if the roughness and mix from 1.0 to zero - currently best guess.
            brdf.colors[REFLECTED_COLOR_INDEX] += RPower * gafView * (ndf * NDFFactor) * mix(float16_t(1.0), float16_t(0.0), roughSquared) * intensity * (reflectionColor * lightColor);
#endif
        }
    }
#endif

#ifdef POINT
    for (int lightNumber = 0; lightNumber < POINT_LIGHT_COUNT; lightNumber++) {
        brdf.NdotL = float16_t(dot(brdf.normal, -vPointLight[lightNumber].xyz));
        float16_t RPower = getFresnelFactor(R0, brdf.NdotL);
        float oneMinusRP = 1.0 - RPower;
        if (brdf.NdotL >= F16_ZERO) {
            getPerPixelBRDFDirectional(vPointLight[lightNumber].xyz, toView);
            float intensity = uniforms.pointlight[lightNumber].color.a / pow(vPointLight[lightNumber].w,2);
            vec3 lightColor = uniforms.pointlight[lightNumber].color.rgb;
            //Here we assume transmitted light enters the material and exits evenly over the hemisphere (/ PI), in the transmissionColor of the material
            //As the solid angle from lightsource decreases (the angle to the lightsource increases) the power goes down.
            //Note that there is no distinction here based on metalness since this is already given by RPower
            //Pointlights in glTF does not specify solid angle - this means we have to fudge the normal distribution function
            float view = max(0, dot(brdf.reflectedLight, toView));
            float ndf = ND_SCHLICK(view , a);
#ifdef BLEND
            //TODO -update
            //Amount that will enter into surface: (1 - RPower) - when the light exits the material (1 - RPower) will be left, of that (RPower * absorbFactor) will reflect back and exit the material
            float absorb = oneMinusRP * absorbFactor;
            float reemit = (oneMinusRP - absorb) * RPower;
            float retransmit = reemit * absorbFactor;
            brdf.colors[TRANSMITTED_COLOR_INDEX] += gafView * (absorb + retransmit) * mix(oneByPI, oneByFourPi, roughSquared) * intensity * (transmissionColor * lightColor);
            brdf.colors[REFLECTED_COLOR_INDEX] += (RPower * gafView + (reemit - retransmit)) * (ndf * NDFFactor) * mix(float16_t(1.0), float16_t(0.0), roughSquared) * intensity * (reflectionColor * lightColor);
#else
            brdf.colors[TRANSMITTED_COLOR_INDEX] +=  gafView * emitFactor * (oneMinusRP * mix(oneByPI, oneByFourPi, roughSquared)) * intensity * (transmissionColor * lightColor);
            //The ndf will disperse the normal using some made up factor, to be correct the intensity shall be reduced by the increase in reflected area.
            //This is not known so use an inverse if the roughness and mix from 1.0 to zero - currently best guess.
            brdf.colors[REFLECTED_COLOR_INDEX] += RPower * gafView * (ndf * NDFFactor) * mix(float16_t(1.0), float16_t(0.0), roughSquared) * intensity * (reflectionColor * lightColor);
#endif
        }
    }
#endif

#ifdef CUBEMAP
    vec3 reflection = getReflection(brdf.orma.g, uniforms.cubemaps[0].cubeMapInfo.y);
    brdf.colors[REFLECTED_COLOR_INDEX] += NdotVPower * reflectionColor * reflection;
#endif
#ifdef CUBEMAP_SH
#ifdef BLEND
    brdf.colors[TRANSMITTED_COLOR_INDEX] += (1 - NdotVPower) * gafView * emitFactor * brdf.orma.r * transmissionColor *  mix(oneByPI, oneByFourPi, roughSquared) * surface.irradiance.rgb;
#else
    brdf.colors[TRANSMITTED_COLOR_INDEX] += (1 - NdotVPower) * gafView * emitFactor * brdf.orma.r * transmissionColor *  mix(oneByPI, oneByFourPi, roughSquared) * surface.irradiance.rgb;
#endif
#ifndef CUBEMAP
#ifdef BLEND
    brdf.colors[REFLECTED_COLOR_INDEX] +=  NdotVPower * gafView * brdf.orma.r * reflectionColor * surface.irradiance.rgb;
#else
    //Fallback to using sh coefficients for metal, otherwise it will end up black - todo - shall NdotVPower be used? 
    //The light is irradiance coming from all directions, but - at grazing angles more will be reflected....
    vec3 reflect = reflect(toView, brdf.normal);
    brdf.colors[REFLECTED_COLOR_INDEX] +=  NdotVPower * gafView * (brdf.orma.b * brdf.orma.r * mix(float16_t(1.0), oneByPI, roughSquared)) * reflectionColor * irradiance(uniforms.irradianceCoefficients, reflect).rgb;

#endif
#endif
#endif

/**
 * 
 * Debug options
 *
 */
#ifdef RAW_GAF
     //geometric attenuation factor
    return vec4(0, 0, gafView, 1);
#endif

/**
 *
 * End debug options
 */
 
 
 
    return vec4(brdf.colors[TRANSMITTED_COLOR_INDEX] + brdf.colors[REFLECTED_COLOR_INDEX], alpha);
}



