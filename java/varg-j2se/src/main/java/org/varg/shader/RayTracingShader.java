package org.varg.shader;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.pipeline.Pipelines.DescriptorSetTarget;
import org.varg.pipeline.Pipelines.SetType;
import org.varg.renderer.Renderers;
import org.varg.shader.Gltf2GraphicsShader.GltfDescriptorSetTarget;
import org.varg.shader.RayTracingShader.RayTracingCreateInfo;
import org.varg.vulkan.GLSLCompiler;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;
import org.varg.vulkan.Vulkan10.DescriptorType;
import org.varg.vulkan.Vulkan10.ShaderStageFlagBit;
import org.varg.vulkan.extensions.KHRRayTracingPipeline.RayTracingShaderGroupCreateInfoKHR;

public interface RayTracingShader extends Shader<RayTracingCreateInfo> {

    enum RayTracingShaderType implements Subtype {
        PICKING_SHADER("picking", "picking/", new Stage[] { Stage.RAYGEN, Stage.ANYHIT, }, RayTracingDescriptorSetTarget.values());

        private final Stage[] stages;
        private final String sourceName;
        private final String folder;
        public final DescriptorSetTarget[] targets;

        RayTracingShaderType(String sourceName, String folder, Stage[] stages, DescriptorSetTarget... targets) {
            this.sourceName = sourceName;
            this.folder = folder;
            this.stages = stages;
            this.targets = targets;
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

        @Override
        public boolean hasStage(Stage type) {
            for (Stage stage : stages) {
                if (stage == type) {
                    return true;
                }
            }
            return false;
        }
    }

    int RAYTRACING_BINDING = 2;

    enum RayTracingDescriptorSetTarget implements DescriptorSetTarget {
        TOPLEVEL(RAYTRACING_BINDING, 0, SetType.UNIFORM_TYPE, DescriptorType.VK_DESCRIPTOR_TYPE_ACCELERATION_STRUCTURE_KHR, BufferUsageFlagBit.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT),
        DATA(RAYTRACING_BINDING, 1, SetType.STORAGE_BUFFER_TYPE, DescriptorType.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, BufferUsageFlagBit.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT);

        private final SetType type;
        private final int set;
        private final int binding;
        private final DescriptorType descriptorType;
        private final BufferUsageFlagBit[] bufferUsage;

        RayTracingDescriptorSetTarget(int binding, int set, SetType type, DescriptorType descriptorType, BufferUsageFlagBit... bufferUsage) {
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

        public static RayTracingDescriptorSetTarget[] getTargets(SetType type) {
            ArrayList<RayTracingDescriptorSetTarget> result = new ArrayList<RayTracingDescriptorSetTarget>();
            for (RayTracingDescriptorSetTarget target : values()) {
                if (target.type == type) {
                    result.add(target);
                }
            }
            return result.toArray(new RayTracingDescriptorSetTarget[0]);
        }

        @Override
        public BufferUsageFlagBit[] getBufferUsage() {
            return bufferUsage;
        }

        @Override
        public ShaderStageFlagBit[] getStageBits() {
            return new ShaderStageFlagBit[] { ShaderStageFlagBit.VK_SHADER_STAGE_RAYGEN_BIT_KHR, ShaderStageFlagBit.VK_SHADER_STAGE_ANY_HIT_BIT_KHR };
        }

        @Override
        public String getName() {
            return name();
        }

        public static RayTracingDescriptorSetTarget get(String name) {
            for (RayTracingDescriptorSetTarget target : values()) {
                if (target.name().contentEquals(name)) {
                    return target;
                }
            }
            return null;
        }

    }

    RayTracingShaderGroupCreateInfoKHR[] createRayTracingShaderGroupCreateInfo();

    abstract class RayTracingCreateInfo extends ShaderCreateInfo implements Shader.StorageBufferConsumer<DescriptorSetTarget> {

        protected RayTracingCreateInfo(@NonNull Renderers version, @NonNull Subtype shaderType) {
            super(version, shaderType);
        }

        @Override
        public int[] getBufferSizes(DescriptorSetTarget... descriptorSetTargets) {
            int[] result = new int[descriptorSetTargets.length];
            int index = 0;
            for (DescriptorSetTarget target : descriptorSetTargets) {
                result[index++] = getBufferSize(target);
            }
            return result;
        }

        /**
         * Returns an empty compute shader instance
         * 
         * @return
         */
        public abstract RayTracingShader getInstance();

        /**
         * Internal method
         * Sets local workgroup sizes
         * 
         * @param compiler
         * @param stages
         */
        protected void setMacros(GLSLCompiler compiler, Stage... stages) {
        }

    }

}
