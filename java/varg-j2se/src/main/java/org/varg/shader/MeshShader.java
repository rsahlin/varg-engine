package org.varg.shader;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.RenderableScene;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.pipeline.Pipelines.SetType;
import org.varg.renderer.Renderers;
import org.varg.shader.Gltf2GraphicsShader.GltfDescriptorSetTarget;
import org.varg.shader.MeshShader.MeshShaderCreateInfo;
import org.varg.vulkan.GLSLCompiler;
import org.varg.vulkan.GLSLCompiler.MacroIntProperties;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.Vulkan10.DescriptorType;
import org.varg.vulkan.Vulkan10.PolygonMode;
import org.varg.vulkan.Vulkan10.ShaderStageFlagBit;
import org.varg.vulkan.extensions.KHRFragmentShadingRate.PipelineFragmentShadingRateStateCreateInfoKHR;

/**
 * Mesh shader interface, this is used for the task/mesh shader pipeline.
 *
 */
public interface MeshShader<T extends MeshShaderCreateInfo> extends Shader<T> {

    int MESH_BINDING = 1;

    enum MeshDescriptorSetTarget implements DescriptorSetTarget {
        DATA(MESH_BINDING, 0, SetType.STORAGE_BUFFER_TYPE, DescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
                new ShaderStageFlagBit[] { ShaderStageFlagBit.VK_SHADER_STAGE_TASK_BIT_EXT,
                        ShaderStageFlagBit.VK_SHADER_STAGE_MESH_BIT_EXT },
                BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        OUTPUTS(MESH_BINDING, 1, SetType.STORAGE_BUFFER_TYPE, DescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
                new ShaderStageFlagBit[] { ShaderStageFlagBit.VK_SHADER_STAGE_MESH_BIT_EXT,
                        ShaderStageFlagBit.VK_SHADER_STAGE_TASK_BIT_EXT },
                BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        SPRITE(MESH_BINDING, 2, SetType.STORAGE_BUFFER_TYPE, DescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,
                new ShaderStageFlagBit[] { ShaderStageFlagBit.VK_SHADER_STAGE_TASK_BIT_EXT,
                        ShaderStageFlagBit.VK_SHADER_STAGE_MESH_BIT_EXT },
                BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        MATRIX(MESH_BINDING, 3, SetType.UNIFORM_TYPE, DescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                new ShaderStageFlagBit[] { ShaderStageFlagBit.VK_SHADER_STAGE_MESH_BIT_EXT },
                BufferUsageFlagBit.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT),
        GLOBAL(MESH_BINDING, 4, SetType.UNIFORM_TYPE, DescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
                new ShaderStageFlagBit[] { ShaderStageFlagBit.VK_SHADER_STAGE_TASK_BIT_EXT,
                        ShaderStageFlagBit.VK_SHADER_STAGE_MESH_BIT_EXT },
                BufferUsageFlagBit.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT);

        private final SetType type;
        private final int set;
        private final int binding;
        private final DescriptorType descriptorType;
        private final BufferUsageFlagBit[] bufferUsage;
        private final ShaderStageFlagBit[] stageFlagBits;

        MeshDescriptorSetTarget(int binding, int set, SetType type, DescriptorType descriptorType,
                ShaderStageFlagBit[] stageFlagBits, BufferUsageFlagBit... bufferUsage) {
            this.binding = binding;
            this.type = type;
            this.set = set;
            this.descriptorType = descriptorType;
            this.stageFlagBits = stageFlagBits;
            this.bufferUsage = bufferUsage;
        }

        public static MeshDescriptorSetTarget get(String name) {
            for (MeshDescriptorSetTarget target : values()) {
                if (target.name().contentEquals(name)) {
                    return target;
                }
            }
            return null;
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
            return MeshDescriptorSetTarget.values();
        }

        @Override
        public BufferUsageFlagBit[] getBufferUsage() {
            return bufferUsage;
        }

        @Override
        public ShaderStageFlagBit[] getStageBits() {
            return stageFlagBits;
        }

        public static MeshDescriptorSetTarget[] getTargets(SetType type) {
            ArrayList<MeshDescriptorSetTarget> result = new ArrayList<>();
            for (MeshDescriptorSetTarget target : values()) {
                if (target.type == type) {
                    result.add(target);
                }
            }
            return result.toArray(new MeshDescriptorSetTarget[0]);
        }

        @Override
        public String getName() {
            return name();
        }

    }

    enum MeshShaderType implements GraphicsSubtype {
        VOXELS("voxels", "voxels/", PolygonMode.VK_POLYGON_MODE_FILL, new Stage[] { Stage.TASK, Stage.MESH,
                Stage.FRAGMENT }, MeshDescriptorSetTarget.values()),
        GLTF_BACKGROUND("background", "gltf2/", PolygonMode.VK_POLYGON_MODE_FILL, new Stage[] { Stage.MESH,
                Stage.FRAGMENT }, GltfDescriptorSetTarget.values());

        private final Stage[] stages;
        private final String sourceName;
        private final String folder;
        private final PolygonMode polygonMode;
        public final DescriptorSetTarget[] targets;

        MeshShaderType(String sourceName, String folder, PolygonMode polygonMode, Stage[] stages,
                DescriptorSetTarget... targets) {
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

        public static MeshShaderType get(String name) {
            for (MeshShaderType type : values()) {
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
            return DescriptorSetTarget.getHash(targets);
        }
    }

    abstract class MeshShaderCreateInfo extends ShaderCreateInfo implements
            Shader.StorageBufferConsumer<MeshDescriptorSetTarget> {

        public final MeshShaderType meshShaderType;
        private final RenderableScene asset;
        private PipelineFragmentShadingRateStateCreateInfoKHR fragmentShadingRate;

        public MeshShaderCreateInfo(@NonNull RenderableScene asset,
                @NonNull Renderers version, @NonNull MeshShaderType shaderType) {
            super(version, shaderType);
            meshShaderType = shaderType;
            this.asset = asset;
        }

        /**
         * Returns an empty mesh shader instance
         * 
         * @return
         */
        public abstract MeshShader getInstance();

        /**
         * Returns the global workgroup count for x, y and z.
         * 
         * @return
         */
        public abstract int[] getWorkGroupCounts(Shader.Stage stage);

        /**
         * Returns the local workgroup invocations in up to 3 dimensions for the stage, or null if not used
         * 
         * @return Array with 1 to 3 dimensions, or null if not used.
         */
        public abstract int[] getLocalSizes(Shader.Stage stage);

        /**
         * Returns the glTF asset or null
         */
        public RenderableScene getAsset() {
            return asset;
        }

        /**
         * Sets the fragmentshadingrate - to use the VK_KHR_fragment_shading_rate extension must be enabled
         * 
         * @return
         */
        public void setFragmentShadingRate(PipelineFragmentShadingRateStateCreateInfoKHR fragmentShadingRate) {
            this.fragmentShadingRate = fragmentShadingRate;
        }

        /**
         * Returns the fragmentshadingrate if specified, or null
         * 
         * @return
         */
        public PipelineFragmentShadingRateStateCreateInfoKHR getFragmentShadingRate() {
            return fragmentShadingRate;
        }

        /**
         * Internal method
         * 
         * @param compiler
         * @param stages
         */
        protected void setMacros(GLSLCompiler compiler, Stage... stages) {
            for (Stage stage : stages) {
                int[] localSizes = getLocalSizes(stage);
                compiler.addMacro(new MacroIntProperties[] { MacroIntProperties.SIZE_X,
                        MacroIntProperties.SIZE_Y, MacroIntProperties.SIZE_Z }, localSizes,
                        stage);

            }
        }

    }

}
