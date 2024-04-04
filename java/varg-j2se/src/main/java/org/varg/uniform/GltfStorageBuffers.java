
package org.varg.uniform;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.gltfio.gltf2.JSONCamera;
import org.gltfio.gltf2.JSONMaterial;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPBRMetallicRoughness;
import org.gltfio.gltf2.JSONTexture;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.JSONTexture.TextureInfo;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.extensions.EnvironmentMap;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.KHRLightsPunctualReference;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.Light;
import org.gltfio.gltf2.extensions.KHRMaterialsEmissiveStrength;
import org.gltfio.gltf2.extensions.KHRdisplayencoding;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Matrix;
import org.gltfio.lib.MatrixUtils;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Transform;
import org.gltfio.lib.Vec3;
import org.gltfio.lighting.IrradianceMap;
import org.ktximageio.ktx.HalfFloatImageBuffer.FP16Convert;
import org.varg.assets.Assets;
import org.varg.assets.TextureImages;
import org.varg.assets.TextureImages.SamplerType;
import org.varg.assets.TextureImages.TextureSamplerInfo;
import org.varg.gltf.VulkanMesh;
import org.varg.gltf.VulkanScene;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.renderer.DrawCallBundle;
import org.varg.renderer.GltfRenderer;
import org.varg.renderer.MVPMatrices;
import org.varg.renderer.MVPMatrices.Matrices;
import org.varg.shader.Gltf2GraphicsShader;
import org.varg.shader.Gltf2GraphicsShader.GltfDescriptorSetTarget;
import org.varg.uniform.BindBuffer.BufferState;
import org.varg.vulkan.VulkanBackend.BackendIntProperties;
import org.varg.vulkan.VulkanRenderableScene;

/**
 *
 */
public class GltfStorageBuffers extends DescriptorBuffers<Gltf2GraphicsShader> {
    /*
     * layout(std430, set = UNIFORM_GLOBAL_SET, binding = GLTF_BINDING) uniform globaluniformstruct {
     * mat4[2] vpMatrix; //0 = view, 1 = projection
     * vec4[2] camera; 0 = camera position, 1 = viewvectors for reflection map background
     * f16vec4 displayEncoding; //Color primaries Ry,Gy,By + max white
     * f16vec4[9] irradianceCoefficients;
     * Environment[MAX_CUBEMAPS] cubemaps; //Must be set using define
     * DirectionalLight[MAX_D_LIGHTS] directionallight; //Must be set using define
     * PointLight[MAX_P_LIGHTS] pointlight; //Must be set using define
     * } uniforms;
     */
    /*
     * struct DirectionalLight {
     * vec4 color;
     * vec4 direction;
     * };
     * 
     * struct PointLight {
     * vec4 color;
     * vec4 position;
     * };
     * struct Environment {
     * // x = mipmap levels, y = intensity factor
     * vec4 cubeMapInfo;
     * vec4 boxMin;
     * vec4 boxMax;
     * } cubemap;
     * 
     * 
     */
    public static final int BASECOLOR_TEXTURE_INDEX = 0;
    public static final int NORMAL_TEXTURE_INDEX = 1;
    public static final int MR_TEXTURE_INDEX = 2;
    public static final int OCCLUSION_TEXTURE_INDEX = 3;
    public static final int EMISSIVE_TEXTURE_INDEX = 4;

    public static final int ORM_INDEX = 0;
    public static final int SCALE_FACTORS_INDEX = 4;
    public static final int BASECOLOR_INDEX = 8;
    // Samplers uses SAMPLERS_DATA.length slots
    // Must be std430 layout aligned.
    public static final int TEXTURE_SAMPLERS_INDEX = 12;
    public static final int ABSORB_INDEX = 16;
    public static final int MATERIAL_DATA_PADDING_BYTES = 4;
    public static final int MATERIAL_DATA_SIZE_IN_BYTES = TEXTURE_SAMPLERS_INDEX * Short.BYTES
            + JSONMaterial.SAMPLERS_DATA_LENGTH + MATERIAL_DATA_PADDING_BYTES;

    public static final int MAX_CUBEMAP_COUNT = 2;
    public static final int MAX_DIRECTIONAL_LIGHTS = 8;
    public static final int MAX_POINT_LIGHTS = 8;

    public static final int PUSH_CONSTANTS_SIZE = PushConstants.PUSH_DATASIZE * 4;
    // 3 - 4 x 4 matrices
    public static final int UNIFORM_MATRIX_BUFFER_SIZE = Matrix.MATRIX_ELEMENTS * Float.BYTES;

    public static final int UNIFORM_MATERIAL_SIZE = MATERIAL_DATA_SIZE_IN_BYTES;
    private static final int UNIFORM_PRIMITIVE_INTSIZE = 4;
    private static final int UNIFORM_PRIMITIVE_SIZE = UNIFORM_PRIMITIVE_INTSIZE * Integer.BYTES;
    public static final int ENVIRONMENT_SIZE = VEC4_SIZE_IN_BYTES * 3;
    public static final int IRRADIANCE_SIZE = F16VEC4_SIZE_IN_BYTES * 9;
    public static final int DIRECTIONAL_LIGHT_SIZE = VEC4_SIZE_IN_BYTES * 2;
    public static final int POINT_LIGHT_SIZE = VEC4_SIZE_IN_BYTES * 2;
    public static final int DIRECTIONAL_LIGHTS_TOTLSIZE = DIRECTIONAL_LIGHT_SIZE * MAX_DIRECTIONAL_LIGHTS;
    public static final int POINT_LIGHTS_TOTALSIZE = POINT_LIGHT_SIZE * MAX_POINT_LIGHTS;

    public static final int VP_MATRIX_OFFSET = 0;
    public static final int CAMERA_OFFSET = VP_MATRIX_OFFSET + MAT4_SIZE_IN_BYTES * 2;
    public static final int DISPLAYENCODING_OFFSET = CAMERA_OFFSET + VEC4_SIZE_IN_BYTES * 2;
    public static final int IRRADIANCE_OFFSET = DISPLAYENCODING_OFFSET + F16VEC4_SIZE_IN_BYTES;
    public static final int ENVIRONMENT_OFFSET = IRRADIANCE_OFFSET + IRRADIANCE_SIZE;
    public static final int DIRECTIONAL_LIGHT_OFFSET = ENVIRONMENT_OFFSET + ENVIRONMENT_SIZE * MAX_CUBEMAP_COUNT;
    public static final int POINT_LIGHT_OFFSET = DIRECTIONAL_LIGHT_OFFSET + DIRECTIONAL_LIGHTS_TOTLSIZE;

    public static final int GLOBAL_UNIFORMS_SIZE = POINT_LIGHT_OFFSET + POINT_LIGHTS_TOTALSIZE;

    private final TextureImages textureImages;

    public GltfStorageBuffers(TextureImages textureImages) {
        this.textureImages = textureImages;
    }

    /**
     * Returns the number of ints needed for uniform primitive storage
     * 
     * @param primitiveCount
     * @return
     */
    public static int getUniformPrimitiveSize(int primitiveCount) {
        return primitiveCount * UNIFORM_PRIMITIVE_INTSIZE;
    }

    /**
     * Returns the (int) offset into primitive data for the instance
     * 
     * @param primitiveInstance
     * @return
     */
    public static int getUniformPrimitiveIndex(int primitiveInstance) {
        return primitiveInstance * UNIFORM_PRIMITIVE_INTSIZE;
    }

    /**
     * Returns the size, in ints, for one primitive instance
     * 
     * @return
     */
    public static int getUniformPrimitiveDataSize() {
        return UNIFORM_PRIMITIVE_INTSIZE;
    }

    @Override
    public DescriptorSetTarget[] setDynamicStorage(Gltf2GraphicsShader source) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSetCount(DescriptorSetTarget target) {
        GltfDescriptorSetTarget gltfTarget = GltfDescriptorSetTarget.get(target.getName());
        switch (gltfTarget) {
            case MATERIAL_TEXTURE:
                if (textureImages != null) {
                    return Math.max(1, textureImages.getDescriptorCount(SamplerType.sampler2DArray));
                }
                return 1;
            case CUBEMAP_TEXTURE:
                if (textureImages != null) {
                    return Math.max(1, textureImages.getDescriptorCount(SamplerType.samplerCubeArray));
                }
                return 1;
            default:
                return 1;
        }
    }

    @Override
    public void setStaticStorage(VulkanRenderableScene glTF, GltfRenderer<VulkanRenderableScene, VulkanMesh> renderer) {
        setTextureChannels(glTF, renderer.getAssets());
        setMeshData(glTF);
        int textureTransformCount = glTF.getRoot().getGltfExtensions().getKHRTextureTransformCount();
        if (textureTransformCount > 0) {
            float[] transformData = glTF.getRoot().getGltfExtensions().createTextureTransformBuffer();
            storeFloatData(GltfDescriptorSetTarget.TEXTURE_TRANSFORM, 0, transformData);
        }
    }

    private void setTextureChannels(RenderableScene glTF, Assets assets) {
        for (JSONMaterial material : glTF.getMaterials()) {
            if (material != null) {
                for (Channel channel : Channel.values()) {
                    JSONTexture texture = material.getTexture(channel);
                    if (texture != null) {
                        TextureSamplerInfo samplerInfo = textureImages.getSamplerInfo(texture);
                        storeChannel(material, channel, samplerInfo);
                    }
                }
            }
        }
    }

    /**
     * Stores the texture channel info in the samplersData, internal method.
     * Call once for each texture channel that is used
     * 
     * @param channel
     * @param samplerInfo
     */
    private void storeChannel(JSONMaterial material, Channel channel, TextureSamplerInfo samplerInfo) {
        TextureInfo textureInfo = material.getTextureInfo(channel);
        ByteBuffer samplersData = material.getSamplersData();
        samplersData.position(0);
        if (textureInfo != null) {
            if (channel == null) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
            }
            byte texCoord = (byte) material.getTexCoord(textureInfo);
            byte transformIndex = (byte) textureInfo.getTextureTransformIndex();
            if (transformIndex < 0 && textureInfo.getExtension(ExtensionTypes.KHR_texture_transform) != null) {
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_VALUE.message + "Invalid transform index " + transformIndex);
            }
            int index = getChannelIndex(channel);
            samplersData.position(index * 4);
            samplersData.put(samplerInfo.getDescriptorIndex());
            samplersData.put(samplerInfo.layer);
            samplersData.put(texCoord);
            samplersData.put(transformIndex);
        }
        return;
    }

    /**
     * Returns the texture index for the channel
     * 
     * @param channel
     * @return
     */
    private int getChannelIndex(Channel channel) {
        switch (channel) {
            case BASECOLOR:
                return BASECOLOR_TEXTURE_INDEX;
            case NORMAL:
                return NORMAL_TEXTURE_INDEX;
            case METALLICROUGHNESS:
                return MR_TEXTURE_INDEX;
            case OCCLUSION:
                return OCCLUSION_TEXTURE_INDEX;
            case EMISSIVE:
                return EMISSIVE_TEXTURE_INDEX;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
        }
    }

    /**
     * Precomputes the pbr/material data - call this method once at start or when pbr or material parameters have
     * changed.
     */
    private void storeMaterialData(JSONMaterial material, ByteBuffer destination, float environmentIOR) {
        if (material != null) {
            if (material.getNormalTextureInfo() != null && material.getNormalTextureInfo().getScale() < 1.0f) {
                Logger.e(getClass(), "Normal texture has scale < 1.0 : " + material.getNormalTextureInfo().getScale()
                        + " index " + material.getNormalTextureInfo().getIndex());
            }
            JSONPBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
            int startPos = destination.position();
            ShortBuffer buffer = destination.asShortBuffer();
            buffer.position(ORM_INDEX);
            put(buffer, material.getOcclusionTextureInfo() != null ? material.getOcclusionTextureInfo().getStrength()
                    : 1.0f);
            put(buffer, pbr.getRoughnessFactor());
            put(buffer, pbr.getMetallicFactor());
            float fresnelPower = (environmentIOR - material.getIOR()) / (environmentIOR + material.getIOR());
            fresnelPower = fresnelPower * fresnelPower;
            put(buffer, fresnelPower);
            buffer.position(SCALE_FACTORS_INDEX);
            KHRMaterialsEmissiveStrength khrEmissive = (KHRMaterialsEmissiveStrength) material.getExtension(
                    ExtensionTypes.KHR_materials_emissive_strength);
            float maxWhite = Settings.getInstance().getInt(BackendIntProperties.MAX_WHITE);
            float emissiveStrength = khrEmissive != null ? khrEmissive.getEmissiveStrength() : maxWhite;
            put(buffer, material.getEmissiveFactor() != null ? material.getEmissiveFactor()[0] * emissiveStrength
                    : 0.0f);
            put(buffer, material.getEmissiveFactor() != null ? material.getEmissiveFactor()[1] * emissiveStrength
                    : 0.0f);
            put(buffer, material.getEmissiveFactor() != null ? material.getEmissiveFactor()[2] * emissiveStrength
                    : 0.0f);
            put(buffer, material.getNormalTextureInfo() != null ? material.getNormalTextureInfo().getScale() : 1.0f);
            buffer.position(BASECOLOR_INDEX);
            put(buffer, pbr.getBaseColorFactor());
            // Store texcoord data
            destination.position((TEXTURE_SAMPLERS_INDEX * Short.BYTES) + startPos);
            ByteBuffer buf = material.getSamplersData().position(0);
            destination.put(buf);
            byte afactor = (byte) (material.getAbsorbtion() * 255);
            byte[] absorb = new byte[] { afactor, 1, 1, 1 };
            destination.put(absorb);
            // destination.position(destination.position() + MATERIAL_DATA_PADDING_BYTES);
            if (destination.position() != startPos + MATERIAL_DATA_SIZE_IN_BYTES) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Misaligned");
            }
        }
    }

    private void put(ShortBuffer buffer, float... data) {
        short[] result = new short[data.length];
        FP16Convert convert = new FP16Convert(result);
        convert.convert(data);
        buffer.put(result);
    }

    /**
     * Store material plus the material index into the primitive data
     * 
     * @param glTF
     */
    private void setMeshData(RenderableScene glTF) {
        BindBuffer buffer = getBuffer(GltfDescriptorSetTarget.MATERIAL);
        BindBuffer primitive = getBuffer(GltfDescriptorSetTarget.PRIMITIVE);
        JSONMaterial[] materials = glTF.getMaterials();
        ByteBuffer byteBuffer = buffer.getBackingBuffer();
        KHREnvironmentMapReference environmentMapExtension = glTF.getEnvironmentExtension();
        float environmentIOR =
                environmentMapExtension != null ? environmentMapExtension.getEnvironmentMap().getIOR() : 1.0f;
        if (environmentIOR < 1.0f) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "IOR in environment too small: "
                    + environmentIOR);
        }
        for (JSONMaterial material : materials) {
            storeMaterialData(material, byteBuffer, environmentIOR);
        }
        buffer.setState(BufferState.updated);
        DrawCallBundle drawBundle = ((VulkanScene) glTF).getDrawCallBundle();
        int[] uniformPrimitiveBuffer = drawBundle.getPrimitiveUniformArray();
        IntBuffer backingIntBuffer = primitive.getBackingBuffer().asIntBuffer();
        backingIntBuffer.put(uniformPrimitiveBuffer);
        primitive.setState(BufferState.updated);

    }

    /**
     * Updates dynamic uniform storage
     * 
     * @param root
     */
    public void setDynamicStorage(RenderableScene root, JSONCamera camera) {
        BindBuffer buffer = getBuffer(GltfDescriptorSetTarget.GLOBAL_RENDERPASS);
        setCamera(root, buffer, camera);
        KHREnvironmentMapReference environmentMapExtension = root.getEnvironmentExtension();
        JSONNode[] lights = root.getLightNodes();
        if (environmentMapExtension != null) {
            EnvironmentMap environmentMap = environmentMapExtension.getEnvironmentMap();
            setEnvironment(buffer, camera, environmentMapExtension);
            IrradianceMap irradiance = environmentMap.getIrradianceMap();
            if (irradiance != null) {
                setIrradianceMap(buffer, environmentMap);
            }
        }
        setLights(buffer, lights);
        // Todo - move to static - displayencoding does not change dynamically
        KHRdisplayencoding dm = (KHRdisplayencoding) (root.getRoot()
                .getExtension(ExtensionTypes.KHR_displayencoding));
        short[] shortvalues = new short[4];
        FP16Convert convert = new FP16Convert(shortvalues);
        if (dm == null) {
            float[] val = new float[] { 1, 1, 1, Settings.getInstance().getInt(BackendIntProperties.MAX_WHITE) };
            convert.convert(val);
            setDisplayEncodingParameters(buffer, shortvalues);
        } else {
            float maxLight = Settings.getInstance().getInt(BackendIntProperties.HDR_MAX_CONTENT_LIGHTLEVEL);
            // setDisplayEncodingParameters(buffer, maxLight / 10000, 10000);
            float[] hdrValues = new float[] { 1, 1, 1, maxLight };
            convert.convert(hdrValues);
            setDisplayEncodingParameters(buffer, shortvalues);
        }
        buffer.setState(BufferState.updated);
        BindBuffer matrixBuffer = getBuffer(GltfDescriptorSetTarget.MATRIX);
        setNodeModelMatrices(root, matrixBuffer);
        matrixBuffer.setState(BufferState.updated);
    }

    private void setNodeModelMatrices(RenderableScene root, BindBuffer buffer) {
        // buffer.resetElement();
        JSONNode[] sceneNodes = root.getNodes();
        MVPMatrices matrices = new MVPMatrices();
        if (sceneNodes != null) {
            Transform sceneTransform = root.getSceneTransform();
            matrices.setMatrix(Matrices.MODEL, sceneTransform.updateMatrix());
            for (int i = 0; i < sceneNodes.length; i++) {
                setNodeModelMatrices(sceneNodes[i], matrices, buffer);
            }
        }

    }

    private void setNodeModelMatrices(JSONNode node, MVPMatrices matrices, BindBuffer buffer) {
        if (node != null && (node.getChildCount() > 0 || node.getMeshIndex() >= 0)) {
            matrices.push(Matrices.MODEL);
            if (node.getCamera() != null) {
                System.out.println("camera");
            } else {
                matrices.concatModelMatrix(node.getTransform().updateMatrix());
            }
            JSONMesh mesh = node.getMesh();
            if (mesh != null) {
                buffer.storeFloatData(node.getMatrixIndex() * Matrix.MATRIX_ELEMENTS, matrices.getMatrix(
                        Matrices.MODEL));
            }
            setNodeModelMatrices(node.getChildren(), matrices, buffer);
            matrices.pop(Matrices.MODEL);
        }
    }

    private void setNodeModelMatrices(JSONNode[] children, MVPMatrices matrices, BindBuffer buffer) {
        if (children != null && children.length > 0) {
            for (JSONNode n : children) {
                setNodeModelMatrices(n, matrices, buffer);
            }
        }
    }

    private void setCamera(RenderableScene asset, BindBuffer buffer, JSONCamera camera) {
        buffer.storeFloatData((VP_MATRIX_OFFSET >> 2), camera.updateViewMatrix());
        buffer.storeFloatData((VP_MATRIX_OFFSET >> 2) + Matrix.MATRIX_ELEMENTS, camera.getProjectionMatrix(true));
        // Get the camera position relative to world coordinates (which is applied for each node)
        // float[] position = camera.getNode().getTransform().getTranslate();
        float[] cameraMatrix = camera.getCameraMatrix();
        float[] position = MatrixUtils.getTranslate(cameraMatrix);
        buffer.storeFloatData((CAMERA_OFFSET >> 2), position);
        buffer.storeFloatData((CAMERA_OFFSET >> 2) + 4, camera.getViewVectors());
        // Check camera position against possible boundingbox
        KHREnvironmentMapReference environmentMapExtension = asset.getEnvironmentExtension();
        EnvironmentMap envMap = environmentMapExtension != null ? environmentMapExtension.getEnvironmentMap() : null;
        if (envMap != null && envMap.getBoundingBox() != null) {
            float[][] bbox = envMap.getBoundingBox();
            if (position[0] < bbox[0][0] || position[1] < bbox[0][1] || position[2] < bbox[0][2]) {
                Logger.e(getClass(), "Warning - camera outside scene boundingbox");
            } else if (position[0] > bbox[1][0] || position[1] > bbox[1][1] || position[2] > bbox[1][2]) {
                Logger.e(getClass(), "Warning - camera outside scene boundingbox");
            }
        }
    }

    private void setEnvironment(BindBuffer buffer, JSONCamera camera, KHREnvironmentMapReference... envMapReference) {
        FloatBuffer destination = buffer.getBackingBuffer().position(ENVIRONMENT_OFFSET).asFloatBuffer();
        float[] vec4 = new float[4];
        if (envMapReference.length > MAX_CUBEMAP_COUNT) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < envMapReference.length; i++) {
            EnvironmentMap map = envMapReference[i].getEnvironmentMap();
            float[][] boundingBox = map.getBoundingBox();
            int mipLevels = map.getMipLevels();
            vec4[0] = mipLevels > 1 ? mipLevels - 1 : 1;
            vec4[1] = envMapReference[i].getCubemap() != null ? envMapReference[i].getCubemap().getIntensity() : 0;
            vec4[2] = envMapReference[i].getTexelPerPixelRatio(camera.getScreenSize());
            destination.put(vec4);
            if (boundingBox != null) {
                // Min boundingbox
                vec4[0] = boundingBox[0][0];
                vec4[1] = boundingBox[0][1];
                vec4[2] = boundingBox[0][2];
                destination.put(vec4);
                // Max boundingbox
                vec4[0] = boundingBox[1][0];
                vec4[1] = boundingBox[1][1];
                vec4[2] = boundingBox[1][2];
                destination.put(vec4);
            } else {
                destination.put(new float[] { -10, -10, -10 });
                destination.put(new float[] { 10, 10, 10 });
            }
        }
    }

    private void setLights(BindBuffer buffer, JSONNode[] lights) {
        if (lights != null) {
            int directionalIndex = 0;
            int pointIndex = 0;
            FloatBuffer destination = buffer.getBackingBuffer().position(0).asFloatBuffer();
            for (int i = 0; i < lights.length; i++) {
                if (lights[i] != null) {
                    if (directionalIndex >= MAX_DIRECTIONAL_LIGHTS || pointIndex >= MAX_POINT_LIGHTS) {
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Too many lights");
                    }
                    KHRLightsPunctualReference lightNode = lights[i].getLight();
                    Light light = lightNode.getLight();
                    float[] lightMatrix = getLightMatrix(lights[i]);
                    switch (light.getType()) {
                        case directional:
                            /**
                             * From KHR_lights_punctual:
                             * Directional lights are light sources that act as though they are infinitely far away and
                             * emit light in the direction of the local -z axis. This light type inherits the
                             * orientation of the node that it belongs to; position and scale are ignored except for
                             * their effect on the inherited node orientation
                             */
                            float[] nodePosition = new float[] { 0, 0, -1 };
                            float[] transformedPosition = new float[4];
                            MatrixUtils.setTranslate(lightMatrix, 0, 0, 0);
                            MatrixUtils.transformVec3(lightMatrix, 0, nodePosition, transformedPosition, 1);
                            Vec3.normalize(transformedPosition);
                            setDirectionalLight(destination, directionalIndex, light, transformedPosition);
                            directionalIndex++;
                            break;
                        case point:
                            setPointLight(destination, pointIndex, light, MatrixUtils.getTranslate(lightMatrix));
                            pointIndex++;
                            break;
                        default:
                            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                                    + "Not implemented for type " + light.getType());
                    }
                }
            }
        }
    }

    private float[] getLightMatrix(JSONNode node) {
        Transform t = node.getTransform();
        float[] lightMatrix = t.updateMatrix();
        JSONNode parent = null;
        JSONNode current = node;
        while ((parent = current.getParent()) != null) {
            lightMatrix = parent.getTransform().concatTransform(lightMatrix);
            current = parent;
        }
        return lightMatrix;
    }

    private void setIrradianceMap(BindBuffer buffer, EnvironmentMap environmentMap) {
        IrradianceMap irradiance = environmentMap.getIrradianceMap();
        if (irradiance != null) {
            short[] coefficients = convertIrradianceF16(irradiance);
            ShortBuffer destination = buffer.getBackingBuffer().position(IRRADIANCE_OFFSET).asShortBuffer();
            setAsVec4(destination, coefficients);
        }
    }

    private void setAsVec4(ShortBuffer destination, short[] coefficients) {
        int source = 0;
        for (int i = 0; i < IrradianceMap.IRRADIANCE_COEFFICIENT_COUNT; i++) {
            destination.put(coefficients[source++]);
            destination.put(coefficients[source++]);
            destination.put(coefficients[source++]);
            destination.put((short) 0);
        }

    }

    private short[] convertIrradianceF16(IrradianceMap irradiance) {
        float[] floatCoeffs = irradiance.getCoefficients();
        short[] result = new short[floatCoeffs.length];
        FP16Convert converter = new FP16Convert(result);
        converter.convert(floatCoeffs);
        return result;
    }

    private void setDisplayEncodingParameters(BindBuffer buffer, short... inData) {
        ShortBuffer destination = buffer.getBackingBuffer().position(DISPLAYENCODING_OFFSET).asShortBuffer();
        destination.put(inData);
    }

    /**
     * Sets the light direction, use this for directional lights
     * 
     * @param buffer Destination float buffer
     * @param lightIndex
     * @param light
     * @param direction Normalized light direction
     */
    private void setDirectionalLight(FloatBuffer buffer, int lightIndex, Light light, float[] direction) {
        buffer.position((DIRECTIONAL_LIGHT_OFFSET + (lightIndex * DIRECTIONAL_LIGHT_SIZE)) >> 2);
        buffer.put(light.getColorIntensity());
        buffer.put(direction);
    }

    /**
     * Sets the light position - use this for point or spot-lights
     * 
     * @param position
     */
    private void setPointLight(FloatBuffer buffer, int lightIndex, Light light, float[] position) {
        buffer.position((POINT_LIGHT_OFFSET + (lightIndex * POINT_LIGHT_SIZE)) >> 2);
        buffer.put(light.getColorIntensity());
        buffer.put(position);
    }

}
