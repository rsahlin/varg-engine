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
    float m3 = m2 * m;
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

/**
 * u = dot
 * n = R0
 */
float16_t F_SCHLICK(in float16_t u, in float16_t n) {
    float16_t m = F16_ONE - u;
    float16_t m2 = m * m;
    return n + (F16_ONE - n) * m2*m2*m; // pow(m,5)
}

float16_t getFresnelFactor(in float16_t R0, in float16_t NdotL) {
    return F_SCHLICK(max(F16_ZERO, NdotL), R0);
}
/**
  * Only call if ndotl < 0 
  */
void scatteredTransmission(in float16_t ndotl,in float intensity) {
    ndotl = abs(ndotl);
    float16_t transmit = F16_ONE - getFresnelFactor(material.layerFresnel[0].x, ndotl);
    brdf.colors[TRANSMITTED_COLOR_INDEX] += (transmit * brdf.orma.a * oneByTwoPi * material.layer[0].rgb) * intensity;
    
}

void processPunctualLight(in vec3 lightDirection, in vec3 color, in float intensity, in float16_t solidAngle, in f16vec4 transmissionColor, in f16vec3 reflectionColor, in vec3 toView) {
    float16_t a = brdf.orma.g * brdf.orma.g;
    float16_t gafView = float16_t(GA_SMITH(brdf.NdotV, a));
    brdf.NdotL = float16_t(dot(brdf.normal, -lightDirection));
    //Calculate the reflective power for base layer
    //Will be minor diff at normal incidence, can be fixed by upscaling the specularFactor in material
    float16_t tempf = getFresnelFactor(mix(material.layerFresnel[0].x, material.layerFresnel[0].y, brdf.orma.b), brdf.NdotL);
    float16_t oneMinusRP = F16_ONE - tempf;
    float16_t RPower = mix(material.layerFresnel[0].z, F16_ONE, tempf) * tempf; 
    float16_t emitFactor = float16_t(1.0) - brdf.orma.a;
    //Using roughness root for reflected color will bring down "whiteness" for non smooth surfaces
    float16_t roughRoot = sqrt(brdf.orma.g);

#if defined(COAT) || defined(COAT_ROUGHNESS) || defined(COAT_FACTOR) || defined(COAT_NORMAL)
#ifdef COAT_FACTOR
    float16_t coatFactor = material.layer[1].y * float16_t(GETTEXTURE(material.samplersData[COAT_FACTOR_INDEX]).r);
#else
    float16_t coatFactor = material.layer[1].y;
#endif
#ifdef COAT_ROUGHNESS
     float16_t coatRoughness = material.layer[1].x * float16_t(GETTEXTURE(material.samplersData[COAT_ROUGHNESS_INDEX]).g);
#else
    float16_t coatRoughness = material.layer[1].x;
#endif
#ifdef COAT_NORMAL
    float16_t ndotl = float16_t(dot(brdf.coatNormal, -lightDirection));
    if (ndotl >= F16_ZERO) {
        float16_t gaf = float16_t(GA_SMITH(ndotl, a));
        float16_t CRPower = coatFactor * getFresnelFactor(material.layerFresnel[1].x, ndotl);
        vec3 coatReflectedLight = reflect(lightDirection, brdf.coatNormal);
        float coatView = max(0.0, dot(coatReflectedLight, toView));
        float coatndf = ND_SCHLICK(coatView , max(coatRoughness * coatRoughness, solidAngle));
        brdf.colors[REFLECTED_COLOR_INDEX] += CRPower * gaf * gafView * (coatndf * uniforms.brdfProperties.x) * (intensity * color);
        intensity -= CRPower;
    } else {
#if defined(SCATTEREDTRANSMIT)
    scatteredTransmit    
#endif
    }
    //Fresnel power now comes from the coat layer vs the material - based on coatfactor.
    RPower = mix(RPower, mix(material.layerFresnel[1].y, material.layerFresnel[1].z, brdf.orma.b), coatFactor);
#endif
#endif

    if (brdf.NdotL >= F16_ZERO) {
        getPerPixelBRDFDirectional(lightDirection, toView);
        float16_t gaf = float16_t(GA_SMITH(brdf.NdotL, a));
        //Here we assume transmitted light enters the material and exits over the hemisphere based on roughness, in the transmissionColor of the material
        //As the solid angle from lightsource decreases (the angle to the lightsource increases) the power goes down.
        //Note that there is no distinction here based on metalness since this is already given by RPower and absorbtion
        //Pointlights in glTF does not specify solid angle - this means we have to fudge the normal distribution function using the NDFFactor set to a value greater than 1.0
        float view = max(0.0, dot(brdf.reflectedLight, toView));
        float ndf = ND_SCHLICK(view , max(a, solidAngle));

#if defined(COAT) || defined(COAT_ROUGHNESS) || defined(COAT_FACTOR)
        float16_t CRPower = coatFactor * getFresnelFactor(material.layerFresnel[1].x, brdf.NdotL);
        //Need dot product of reflected light and view - but cannot use ndf since roughness may be different
        float coatndf = ND_SCHLICK(view , max(coatRoughness * coatRoughness, solidAngle));
        brdf.colors[REFLECTED_COLOR_INDEX] += (coatFactor * CRPower * gaf * gafView * (coatndf * uniforms.brdfProperties.x)) * mix(float16_t(1.0), float16_t(0.0), roughRoot) * (intensity * color);
        intensity -= CRPower;
        //Fresnel power now comes from the coat layer vs the material - based on coatfactor.
        RPower = mix(RPower, mix(material.layerFresnel[1].y, material.layerFresnel[1].z, brdf.orma.b), coatFactor);
#endif

#ifdef TRANSMISSION
        //Amount that will enter into surface: (1 - RPower) - when the light exits the material (1 - RPower) will be left, of that (RPower * absorbFactor) will reflect back and exit the material
        float absorb = oneMinusRP * brdf.orma.a;
        float reemit = (oneMinusRP - absorb) * RPower;
        float retransmit = reemit *  brdf.orma.a;
        brdf.colors[TRANSMITTED_COLOR_INDEX] += gaf * (absorb + retransmit) * mix(oneByPI, oneByFourPi, brdf.orma.g) * intensity * (transmissionColor.rgb * color);
        brdf.colors[REFLECTED_COLOR_INDEX] += (RPower + (reemit - retransmit)) * gaf * gafView * (ndf * uniforms.brdfProperties.x) * mix(float16_t(1.0), float16_t(0.0), roughRoot) * reflectionColor * (intensity * color) ;
#else
#ifdef MASK
        //Treat as normal if alpha != 0
        gaf *= transmissionColor.a;
#endif
        brdf.colors[TRANSMITTED_COLOR_INDEX] +=  gaf * emitFactor * (oneMinusRP * mix(oneByPI, oneByFourPi, brdf.orma.g)) * intensity * (transmissionColor.rgb * color);
        //The ndf will disperse the normal using some made up factor, to be correct the intensity shall be reduced by the increase in reflected area.
        //This is not known so use an inverse if the roughness and mix from 1.0 to zero - currently best guess.
        brdf.colors[REFLECTED_COLOR_INDEX] += (RPower * gaf * gafView * (ndf * uniforms.brdfProperties.x)) * mix(float16_t(1.0), float16_t(0.0), roughRoot) * reflectionColor * (intensity * color);
#endif
    } else {
#if defined(SCATTEREDTRANSMIT)
    scatteredTransmission(brdf.NdotL, intensity);
#endif
    }
}


/**
 * materialColor[0] = transmittedcolor.rgb + alpha channel if alphamode is used
 * materialColor[1] = reflectedcolor
 */
vec4 brdf_main(in f16vec4 transmissionColor, in f16vec3 reflectionColor, in vec3 toView) {
    float16_t absorbFactor = brdf.orma.a;
    float16_t emitFactor = float16_t(1.0) - absorbFactor;
    float16_t NDFFactor = uniforms.brdfProperties.x;
    float16_t a = brdf.orma.g * brdf.orma.g;
    float16_t roughRoot = sqrt(brdf.orma.g);
    float16_t gafView = float16_t(GA_SMITH(brdf.NdotV, a));
    brdf.colors[REFLECTED_COLOR_INDEX] = vec3(0);
    brdf.colors[TRANSMITTED_COLOR_INDEX] = vec3(0);
 
#ifdef DIRECTIONAL
    for (int lightNumber = 0; lightNumber < DIRECTIONAL_LIGHT_COUNT; lightNumber++) {
        processPunctualLight(uniforms.directionallight[lightNumber].direction.xyz, uniforms.directionallight[lightNumber].color.rgb, uniforms.directionallight[lightNumber].color.a, 
            uniforms.directionallight[lightNumber].property.x, transmissionColor, reflectionColor, toView);
    }
#endif

#ifdef POINT
    for (int lightNumber = 0; lightNumber < POINT_LIGHT_COUNT; lightNumber++) {
        vec3 toPoint = surface.position - uniforms.pointlight[lightNumber].position;
        vec3 pointDirection = normalize(toPoint);
        float intensity = uniforms.pointlight[lightNumber].color.a / pow(length(toPoint),2);
        processPunctualLight(pointDirection, uniforms.pointlight[lightNumber].color.rgb, intensity, 
            uniforms.pointlight[lightNumber].property.x, transmissionColor, reflectionColor, toView);
    }
#endif

/**
 *
 * Environment/irradiance 
 *
 */

    /**
     * calculate possible reflection using direction of view and surface normal
     */
    // Use power reflectance at normal incidence R0, where R0 = ((n1 - n2) / (n1 + n2))^2 
    // Light that is not reflected is transmitted (into the surface of the material, in most cases directly re-emitted)

#if defined(COAT_NORMAL) || defined(COAT) || defined(COAT_ROUGHNESS) || defined(COAT_FACTOR)
    //If clearcoat is enabled the reflectioncolor is white.
    reflectionColor = f16vec3(1.0);
    float16_t NdotVPower = getFresnelFactor(material.layerFresnel[1].x, brdf.NdotV);
    float16_t shReflection = F16_ONE;
#else
    float16_t NdotVPower = getFresnelFactor(mix(material.layerFresnel[0].x, material.layerFresnel[0].y, brdf.orma.b), brdf.NdotV);
    float16_t shReflection = brdf.orma.b;
#endif

#ifdef CUBEMAP_SH
#ifdef TRANSMISSION
    brdf.colors[TRANSMITTED_COLOR_INDEX] += (F16_ONE - NdotVPower) * gafView * emitFactor * brdf.orma.r * transmissionColor.rgb *  mix(oneByPI, oneByFourPi, roughRoot) * surface.irradiance.rgb;
#else
    brdf.colors[TRANSMITTED_COLOR_INDEX] += (F16_ONE - NdotVPower) * gafView * emitFactor * brdf.orma.r * transmissionColor.rgb *  mix(oneByPI, oneByFourPi, roughRoot) * surface.irradiance.rgb;
#endif
#endif

#ifdef CUBEMAP
    vec3 reflection = getReflection(brdf.orma.g, uniforms.cubemaps[0].cubeMapInfo.y);
    brdf.colors[REFLECTED_COLOR_INDEX] += NdotVPower * reflectionColor * reflection;
#else
#ifdef TRANSMISSION
    brdf.colors[REFLECTED_COLOR_INDEX] +=  NdotVPower * gafView * brdf.orma.r * reflectionColor * surface.irradiance.rgb;
#else
    //Fallback to using sh coefficients for metal, otherwise it will end up black - todo - shall NdotVPower be used? 
    //The light is irradiance coming from all directions, but - at grazing angles more will be reflected....
    vec3 reflect = reflect(toView, brdf.normal);
    brdf.colors[REFLECTED_COLOR_INDEX] +=  NdotVPower * gafView * (shReflection * brdf.orma.r * mix(float16_t(1.0), oneByPI, roughRoot)) * reflectionColor * irradiance(uniforms.irradianceCoefficients, reflect).rgb;
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
 
    
#ifdef TRANSMISSION 
    // Transmission extension is used, absorbFactor is the amount of light scattered as light passes through the material.
    float16_t alpha = NdotVPower * emitFactor + absorbFactor;
#else
#if defined(BLEND) || defined(MASK)
    float16_t alpha = transmissionColor.a;
#else 
    float16_t alpha = float16_t(1.0);
#endif
#endif


/**
 * Adjust emissive light based on dot product of normal and view direction - reverse values since light is going out in the direction of the normal.
 * This is simply an approximation of the light that would hit the viewer from the emissive material.
 */
#ifdef EMISSIVE
    return vec4(brdf.colors[TRANSMITTED_COLOR_INDEX] + brdf.colors[REFLECTED_COLOR_INDEX] + 
        vec3(mix(oneByTwoPi, F16_ONE, brdf.NdotV) * GETTEXTURE(material.samplersData[EMISSIVE_TEXTURE_INDEX]).rgb * material.scaleFactors.rgb), alpha);
#endif
    return vec4(brdf.colors[TRANSMITTED_COLOR_INDEX] + brdf.colors[REFLECTED_COLOR_INDEX] + 
        vec3(mix(oneByTwoPi, F16_ONE, brdf.NdotV) * material.scaleFactors.rgb), alpha);
 
}



