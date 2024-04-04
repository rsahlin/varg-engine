#define INCLUDED_CUBEMAP_FRAG

vec3 getLocalizedReflectionVector(in vec3 BBoxMin, in vec3 BBoxMax, in vec3 cubemapPosition, in vec3 localPosWS, in vec3 reflDirWS) {
    vec3 intersectMaxPointPlanes = (BBoxMax - localPosWS) / reflDirWS;
    vec3 intersectMinPointPlanes = (BBoxMin - localPosWS) / reflDirWS;
    // Looking only for intersections in the forward direction of the ray.
    vec3 largestParams = max(intersectMaxPointPlanes, intersectMinPointPlanes);
    // Smallest value of the ray parameters gives us the intersection.
    float distToIntersect = min(min(largestParams.x, largestParams.y), largestParams.z);
    // Find the position of the intersection point.
    vec3 intersectPositionWS = localPosWS + reflDirWS * distToIntersect;
    // Get local corrected reflection vector.
    return normalize(intersectPositionWS - cubemapPosition);
}

vec3 localizedReflectionVector(in vec3 BBoxMin, in vec3 BBoxMax, in vec3 cubemapPosition) {
    // Find reflected vector in WS.
//    vec3 viewDirWS = normalize(input.viewDirInWorld);
//    vec3 normalWS = normalize(input.normalInWorld);
    // Working in World Coordinate System.
    return getLocalizedReflectionVector(BBoxMin, BBoxMax, cubemapPosition, surface.position, reflect(-surface.V, brdf.normal));
}


vec3 getReflection(in float roughness, float intensity) {
//TODO - use localized reflection if model will move - default is to move camera.  
#ifdef CUBEMAP_BBOX
    vec3 reflect = localizedReflectionVector(uniforms.cubemaps[0].boxMin.xyz, uniforms.cubemaps[0].boxMax.xyz, vec3(0, 0, 0));
#else
    vec3 reflect = reflect(-surface.V, brdf.normal) * vec3(-1, 1, 1);
#endif
//    vec3 Fr = max(vec3(vec3(1.0 - roughness)), F0) - F0;
//    vec3 k_S = F0 + Fr * pow(1.0 - brdf.NdotV, 5.0); 
//    brdf.colors[CUBEMAP_COLOR_INDEX] = texture(uEnvironmentTexture[0], vec4(reflect, 0), lod).rgb * brdf.fresnel;
    float gaf = GA_SMITH(roughness, brdf.NdotV);
    float lod = textureQueryLod(uEnvironmentTexture[0], reflect).x;  
    lod = lod + roughness * (uniforms.cubemaps[0].cubeMapInfo.x - lod);
    return brdf.orma.r * (gaf * gaf) * textureLod(uEnvironmentTexture[0], vec4(reflect, 0), lod).rgb * intensity;
//    return brdf.orma.r * (gaf * gaf) * mix(1, oneByPI, roughness) * textureLod(uEnvironmentTexture[0], vec4(reflect, 0), lod).rgb * intensity;
}

vec3 getBackground(in float blur, in float intensity, in vec3 viewDirection) {
    float lod = floor(blur * uniforms.cubemaps[0].cubeMapInfo.x);
    return texture(uEnvironmentTexture[0], vec4(viewDirection, 0), lod).rgb * intensity;
}

