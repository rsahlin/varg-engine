#ifndef INCLUDED_PBR
#include "pbr.glsl"
#endif

/**
 * Calculate the tangent light matrix, use this for per pixel normal maps
 */
void createTangentLightMatrix() {
    // From the glTF spec: 
    // bitangent = cross(normal, tangent.xyz) * tangent.w
    vec3 tangent = normalize(vec3(TANGENT_ATTRIBUTE) * mat3(matrix.uModelMatrix[instance.primitive.y]));
    vec3 bitangent =  TANGENT_ATTRIBUTE.w * cross(surface.normal, tangent);
    mTangentLight = transpose(mat3(tangent,bitangent,surface.normal));
}


void setLight(in vec3 worldPos) {
    surface.V = normalize(uniforms.camera[0].xyz - worldPos);
    surface.position = worldPos;
#ifdef CUBEMAP_SH
    surface.irradiance = max(vec4(0), irradiance(uniforms.irradianceCoefficients, surface.normal));
#endif
}

/**
 * Calculates the position and light for gltf materials
 */
void glTFVertexSetup() {
    surface.normal = normalize(NORMAL_ATTRIBUTE * mat3(matrix.uModelMatrix[instance.primitive.y]));
#ifdef NORMAL
    createTangentLightMatrix();
#endif
    vec4 worldPos = vec4(POSITION_ATTRIBUTE, 1.0) * matrix.uModelMatrix[instance.primitive.y];
    gl_Position = (worldPos * uniforms.vpMatrix[0]) * uniforms.vpMatrix[1];
    setLight(worldPos.xyz);
#ifdef COLOR_0
    vMaterialColor[0] = COLOR_ATTRIBUTE * materials.material[instance.primitive.x].materialColor[0];
    vMaterialColor[1] = mix(f16vec4(1), materials.material[instance.primitive.x].materialColor[1] * COLOR_ATTRIBUTE, materials.material[instance.primitive].ormp.b);
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
    
#ifdef DIRECTIONAL
    for (int lightNumber = 0; lightNumber < DIRECTIONAL; lightNumber++) {
        vDirectionalLight[lightNumber] = vec4(uniforms.directionallight[lightNumber].direction.xyz, 1);
    }
#endif
#ifdef POINT
    for (int lightNumber = 0; lightNumber < POINT; lightNumber++) {
        vec4 lightVec = worldPos - uniforms.pointlight[lightNumber].position;
        vPointLight[lightNumber].xyz = normalize(lightVec.xyz);
        vPointLight[lightNumber].w = length(lightVec);
    }
#endif
}

