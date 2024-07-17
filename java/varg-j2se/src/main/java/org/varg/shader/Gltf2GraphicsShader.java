
package org.varg.shader;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Matrix;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.pipeline.Pipelines.SetType;
import org.varg.renderer.Renderers;
import org.varg.shader.GraphicsShader.GraphicsShaderCreateInfo;
import org.varg.uniform.GltfStorageBuffers;
import org.varg.vulkan.GLSLCompiler;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.Vulkan10.DescriptorType;
import org.varg.vulkan.Vulkan10.PolygonMode;
import org.varg.vulkan.Vulkan10.ShaderStageFlagBit;
import org.varg.vulkan.Vulkan10Backend;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo.SpecializationInfo;
import org.varg.vulkan.pipeline.PipelineVertexInputState;

/**
 * Handles loading of shader binaries and the graphics shader modules for a graphics shader that
 * can render glTF 2 scenes
 *
 */
public class Gltf2GraphicsShader extends BaseShaderImplementation<GraphicsShaderCreateInfo> implements GraphicsShader {

    /**
     * The following defined constants MUST be aligned with the shader code in defines_uniform.glsl
     * DO NOT CHANGE WITHOUT UPDATING SHADER CODE
     */
    public static final int GLTF_BINDING = 0;
    public static final int MAX_TEXTURE_COORDINATES = 2;

    public static final int UNIFORM_GLOBAL_SET = 0;
    public static final int UNIFORM_TEXTURE_TRANSFORM_SET = 1;
    public static final int UNIFORM_MATRIX_SET = 2;
    public static final int UNIFORM_MATERIAL_SET = 3;
    public static final int MATERIAL_TEXTURE_SET = 4;
    public static final int CUBEMAP_TEXTURE_SET = 5;
    public static final int UNIFORM_PRIMITIVE_SET = 6;

    public enum GltfDescriptorSetTarget implements DescriptorSetTarget {
        GLOBAL_RENDERPASS(GLTF_BINDING, UNIFORM_GLOBAL_SET, SetType.UNIFORM_TYPE,
                DescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, BufferUsageFlagBit.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        TEXTURE_TRANSFORM(GLTF_BINDING, UNIFORM_TEXTURE_TRANSFORM_SET, SetType.UNIFORM_TYPE,
                DescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, BufferUsageFlagBit.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        MATRIX(GLTF_BINDING, UNIFORM_MATRIX_SET, SetType.UNIFORM_TYPE,
                DescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
                BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_BUILD_INPUT_READ_ONLY_BIT_KHR),
        MATERIAL(GLTF_BINDING, UNIFORM_MATERIAL_SET, SetType.UNIFORM_TYPE,
                DescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        MATERIAL_TEXTURE(GLTF_BINDING, MATERIAL_TEXTURE_SET, SetType.TEXTURE_TYPE,
                DescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        CUBEMAP_TEXTURE(GLTF_BINDING, CUBEMAP_TEXTURE_SET, SetType.TEXTURE_TYPE,
                DescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        PRIMITIVE(GLTF_BINDING, UNIFORM_PRIMITIVE_SET, SetType.UNIFORM_TYPE,
                DescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
                BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT);

        private final SetType type;
        private final int set;
        private final int binding;
        private final DescriptorType descriptorType;
        private final BufferUsageFlagBit[] bufferUsage;

        GltfDescriptorSetTarget(int binding, int set, SetType type, DescriptorType descriptorType, BufferUsageFlagBit... bufferUsage) {
            this.binding = binding;
            this.type = type;
            this.set = set;
            this.descriptorType = descriptorType;
            this.bufferUsage = bufferUsage;
        }

        @Override
        public SetType getSetType() {
            return type;
        }

        @Override
        public int getSet() {
            return set;
        }

        @Override
        public int getBinding() {
            return binding;
        }

        @Override
        public DescriptorType getDescriptorType() {
            return descriptorType;
        }

        @Override
        public DescriptorSetTarget[] getValues() {
            return GltfDescriptorSetTarget.values();
        }

        public static GltfDescriptorSetTarget[] getTargets(SetType type) {
            ArrayList<GltfDescriptorSetTarget> result = new ArrayList<GltfDescriptorSetTarget>();
            for (GltfDescriptorSetTarget target : values()) {
                if (target.type == type) {
                    result.add(target);
                }
            }
            return result.toArray(new GltfDescriptorSetTarget[0]);
        }

        @Override
        public BufferUsageFlagBit[] getBufferUsage() {
            return bufferUsage;
        }

        @Override
        public ShaderStageFlagBit[] getStageBits() {
            switch (this) {
                case CUBEMAP_TEXTURE:
                case MATERIAL_TEXTURE:
                    return new ShaderStageFlagBit[] { ShaderStageFlagBit.VK_SHADER_STAGE_FRAGMENT_BIT };
                case GLOBAL_RENDERPASS:
                    return new ShaderStageFlagBit[] { ShaderStageFlagBit.VK_SHADER_STAGE_ALL_GRAPHICS,
                            ShaderStageFlagBit.VK_SHADER_STAGE_MESH_BIT_EXT };
                default:
                    return new ShaderStageFlagBit[] { ShaderStageFlagBit.VK_SHADER_STAGE_ALL_GRAPHICS };
            }
        }

        @Override
        public String getName() {
            return name();
        }

        public static GltfDescriptorSetTarget get(String name) {
            for (GltfDescriptorSetTarget target : values()) {
                if (target.name().contentEquals(name)) {
                    return target;
                }
            }
            return null;
        }

    }

    private static final String GLTF_SOURCE = "main";

    private static final String GLTF2_FOLDER = "gltf2/";

    public enum GraphicsShaderType implements GraphicsSubtype {
        GLTF2(GLTF_SOURCE, GLTF2_FOLDER, PolygonMode.VK_POLYGON_MODE_FILL, new Stage[] { Stage.VERTEX, Stage.FRAGMENT }, GltfDescriptorSetTarget.values());

        private final Stage[] stages;
        private final String sourceName;
        private final String folder;
        private final PolygonMode polygonMode;
        private final DescriptorSetTarget[] targets;

        GraphicsShaderType(String sourceName, String folder, PolygonMode polygonMode, Stage[] stages, GltfDescriptorSetTarget... targets) {
            this.sourceName = sourceName;
            this.folder = folder;
            this.polygonMode = polygonMode;
            this.stages = stages;
            this.targets = targets;
        }

        @Override
        public boolean hasStage(Stage type) {
            for (Stage stage : stages) {
                if (stage == type) {
                    return true;
                }
            }
            return false;
        }

        public static GraphicsShaderType get(String name) {
            for (GraphicsShaderType type : values()) {
                if (name.contentEquals(type.name())) {
                    return type;
                }
            }
            return null;
        }

        @Override
        public PolygonMode getPolygonMode() {
            return polygonMode;
        }

        @Override
        public String getFolder() {
            return folder;
        }

        @Override
        public String getSourceName() {
            return sourceName;
        }

        @Override
        public Stage[] getStages() {
            return stages;
        }

        @Override
        public DescriptorSetTarget[] getTargets() {
            return targets;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public int getDescriptorSetLayoutHash() {
            final int prime = 31;
            int result = 1;
            for (DescriptorSetTarget t : targets) {
                result += prime * t.hashCode();
            }
            return result;
        }
    }

    public static class Gltf2GraphicsShaderCreateInfo extends GraphicsShader.GraphicsShaderCreateInfo {

        public Gltf2GraphicsShaderCreateInfo(@NonNull RenderableScene asset,
                @NonNull Renderers version, @NonNull GraphicsShaderType shaderType,
                @NonNull SpecializationInfo specializationInfo) {
            super(version, shaderType, specializationInfo);
            this.scene = asset;
        }

        private final RenderableScene scene;

        @Override
        public GraphicsShader getInstance(PipelineVertexInputState inputState) {
            return new Gltf2GraphicsShader(this, inputState);
        }

        @Override
        public int getBufferSize(DescriptorSetTarget target) {
            GltfDescriptorSetTarget gltfTarget = GltfDescriptorSetTarget.get(target.getName());
            switch (gltfTarget) {
                case GLOBAL_RENDERPASS:
                    return GltfStorageBuffers.GLOBAL_UNIFORMS_SIZE;
                case TEXTURE_TRANSFORM:
                    return scene.getRoot().getGltfExtensions().getKHRTextureTransformCount() * Matrix.MATRIX_ELEMENTS * Float.BYTES;
                case MATRIX:
                    return scene.getMeshCount() * GltfStorageBuffers.UNIFORM_MATRIX_BUFFER_SIZE;
                case MATERIAL:
                    return scene.getMaterialCount() * GltfStorageBuffers.UNIFORM_MATERIAL_SIZE;
                /**
                 * No uniform buffers for cubemap or material texture - tied to shader using sampler
                 */
                case CUBEMAP_TEXTURE:
                case MATERIAL_TEXTURE:
                    break;
                case PRIMITIVE:
                    return GltfStorageBuffers.getUniformPrimitiveSize(scene.getPrimitiveInstanceCount()) * Integer.BYTES;
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE + ", target " + target);
            }
            return -1;
        }

        @Override
        public int[] getBufferSizes(DescriptorSetTarget... descriptorSetTargets) {
            int[] result = new int[descriptorSetTargets.length];
            for (int i = 0; i < result.length; i++) {
                DescriptorSetTarget target = descriptorSetTargets[i];
                if (result[target.getSet()] != 0) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_STATE + "Conflicting sets");
                }
                result[target.getSet()] = getBufferSize(target);
            }
            return result;
        }

        @Override
        public int[] getDynamicOffsets(DescriptorSetTarget... descriptorSetTargets) {
            int[] result = new int[descriptorSetTargets.length];
            for (int i = 0; i < result.length; i++) {
                GltfDescriptorSetTarget gltfTarget = GltfDescriptorSetTarget.get(descriptorSetTargets[i].getName());
                switch (gltfTarget) {
                    case MATRIX:
                        if (gltfTarget.descriptorType.isDynamic()) {
                            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Not implemented");
                        }
                    default:

                }
            }
            return result;
        }

        @Override
        public void setMacros(GLSLCompiler compiler) {
            throw new IllegalArgumentException();
        }
    }

    final PipelineVertexInputState inputState;

    private Gltf2GraphicsShader(GraphicsShaderCreateInfo info, PipelineVertexInputState inputState) {
        super(info);
        if (inputState == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.inputState = inputState;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadModules(Vulkan10Backend<?> backend, int pipelineHash) throws IOException {
        internalLoadModules(backend, this, shaderInfo, pipelineHash);
    }

    @Override
    public String toString() {
        return inputState.toString();
    }

    @Override
    public ShaderBinary getShaderSource(Stage stage) {
        VulkanShaderBinary spirv = new VulkanShaderBinary();
        switch (stage) {
            case VERTEX:
            case FRAGMENT:
            case GEOMETRY:
                return internalGetShaderSource(stage, getShaderInfo(), spirv);
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + stage);
        }
    }

    @Override
    public PipelineVertexInputState getPipelineVertexInputState() {
        return inputState;
    }

    @Override
    public boolean hasStage(Stage stage) {
        return internalHasStage(stage);
    }

    @Override
    public PipelineShaderStageCreateInfo[] createShaderStageInfos(SpecializationInfo specializationInfo) {
        return createShaderStageInfos(shaderInfo, specializationInfo);
    }

    @Override
    public GraphicsShaderCreateInfo getShaderInfo() {
        return shaderInfo;
    }

}
