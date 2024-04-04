#define INCLUDED_PBR

/**
 * Functions for gltf PBR model
 */
vec4 irradiance(f16vec4[9] irradianceCoefficients, vec3 normal) {
    return
          irradianceCoefficients[0]
        + irradianceCoefficients[1] * (normal.y)
        + irradianceCoefficients[2] * (normal.z)
        + irradianceCoefficients[3] * (normal.x)
        + irradianceCoefficients[4] * (normal.y * normal.x)
        + irradianceCoefficients[5] * (normal.y * normal.z)
        + irradianceCoefficients[6] * (3.0 * normal.z * normal.z - 1.0)
        + irradianceCoefficients[7] * (normal.z * normal.x)
        + irradianceCoefficients[8] * (normal.x * normal.x - normal.y * normal.y);
}

vec3 getIntersection(in vec3 BBoxMin, in vec3 BBoxMax, in vec3 localPos, in vec3 direction) {
    vec3 intersectMaxPointPlanes = (BBoxMax - localPos) / direction;
    vec3 intersectMinPointPlanes = (BBoxMin - localPos) / direction;
    // Looking only for intersections in the forward direction of the ray.
    vec3 largestParams = max(intersectMaxPointPlanes, intersectMinPointPlanes);
    // Smallest value of the ray parameters gives us the intersection.
    float distToIntersect = min(min(largestParams.x, largestParams.y), largestParams.z);
    // Find the position of the intersection point.
    return localPos + direction * distToIntersect;
}
