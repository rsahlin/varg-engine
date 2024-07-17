#ifndef INCLUDED_PBR
#include "pbr.glsl"
#endif

/**
 * Calculate the tangent light matrix, use this for per pixel normal maps
 */
void createTangentLightMatrix(in mat3 matrix) {
    // From the glTF spec: 
    // bitangent = cross(normal, tangent.xyz) * tangent.w
    vec3 tangent = vec3(TANGENT_ATTRIBUTE) * matrix;
    vec3 bitangent =  TANGENT_ATTRIBUTE.w * cross(surface.normal, tangent);
    mTangentLight = transpose(mat3(tangent,bitangent,surface.normal));
}


void setLight(in vec4 worldPos) {
    surface.position = worldPos.xyz;
    
#ifdef DIRECTIONAL
    for (int lightNumber = 0; lightNumber < DIRECTIONAL; lightNumber++) {
        vDirectionalLight[lightNumber] = vec4(uniforms.directionallight[lightNumber].direction.xyz, 1);
    }
#endif
#ifdef POINT
    for (int lightNumber = 0; lightNumber < POINT; lightNumber++) {
        vPointLight[lightNumber].xyz = uniforms.pointlight[lightNumber].position;
        vPointLight[lightNumber].w = length(worldPos - vec4(uniforms.pointlight[lightNumber].position, 1.0));
    }
#endif
    
#ifdef CUBEMAP_SH
    surface.irradiance = max(vec4(0), irradiance(uniforms.irradianceCoefficients, normalize(surface.normal)));
#endif
}

/**
 * Calculates the position and light for gltf materials
 */
void glTFVertexSetup() {
    // Create model matrix from as instance
    vec4[] m = asInstance.accelerationInstance[instance.primitive.y].modelMatrix;
    mat4 model = mat4(m[0], m[1], m[2], vec4(0.0, 0.0, 0.0, 1.0));

    surface.normal = NORMAL_ATTRIBUTE * mat3(model);
#if defined(NORMAL) || defined(COAT_NORMAL)
    createTangentLightMatrix(mat3(model));
#endif
    vec4 worldPos = vec4(POSITION_ATTRIBUTE, 1.0) * model;
    gl_Position = (worldPos * uniforms.vpMatrix[0]) * uniforms.vpMatrix[1];
    setLight(worldPos);
#ifdef COLOR_0
    vMaterialColor[0] = COLOR_ATTRIBUTE * materials.material[instance.primitive.x].materialColor[0];
    vMaterialColor[1] = mix(vec4(1), vec4(materials.material[instance.primitive.x].materialColor[1] * COLOR_ATTRIBUTE), float(materials.material[instance.primitive.x].orm.b));
#else
    vMaterialColor[0] = materials.material[instance.primitive.x].materialColor[0];
    vMaterialColor[1] = materials.material[instance.primitive.x].materialColor[1];
#endif
    
#ifdef KHR_texture_transform
    //To support texture transform in vertex shader, new varyings may be needed for samplers that use texture transform.
    vTexCoord[0] = TEXCOORD_ATTRIBUTE[0];
    vTexCoord[1] = TEXCOORD_ATTRIBUTE[1];
#else
    vTexCoord[0] = TEXCOORD_ATTRIBUTE[0];
    vTexCoord[1] = TEXCOORD_ATTRIBUTE[1];
#endif
    
}

