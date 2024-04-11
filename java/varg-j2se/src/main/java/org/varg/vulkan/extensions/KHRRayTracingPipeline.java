package org.varg.vulkan.extensions;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.BitFlag;
import org.varg.shader.RayTracingShader;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10.PipelineCreateFlagBit;
import org.varg.vulkan.pipeline.Pipeline;
import org.varg.vulkan.pipeline.PipelineCreateInfo;
import org.varg.vulkan.pipeline.PipelineLayout;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.RayTracingPipeline;

/**
 * VK_KHR_ray_tracing_pipeline extension
 */
public abstract class KHRRayTracingPipeline<Q extends Queue> {

    public static final int VK_SHADER_UNUSED = 0;

    public enum RayTracingShaderGroupTypeKHR implements BitFlag {
        VK_RAY_TRACING_SHADER_GROUP_TYPE_GENERAL_KHR(0),
        VK_RAY_TRACING_SHADER_GROUP_TYPE_TRIANGLES_HIT_GROUP_KHR(1),
        VK_RAY_TRACING_SHADER_GROUP_TYPE_PROCEDURAL_HIT_GROUP_KHR(2);

        public final int value;

        RayTracingShaderGroupTypeKHR(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    /**
     * Wrapper for VkRayTracingShaderGroupCreateInfoKHR
     * Provided by VK_KHR_ray_tracing_pipeline
     */
    public static class RayTracingShaderGroupCreateInfoKHR {
        public final RayTracingShaderGroupTypeKHR type;
        public final int generalShader;
        public final int closestHitShader;
        public final int anyHitShader;
        public final int intersectionShader;

        public RayTracingShaderGroupCreateInfoKHR(RayTracingShaderGroupTypeKHR type) {
            this.type = type;
            this.generalShader = VK_SHADER_UNUSED;
            this.closestHitShader = VK_SHADER_UNUSED;
            this.anyHitShader = VK_SHADER_UNUSED;
            this.intersectionShader = VK_SHADER_UNUSED;
        }

    }

    /**
     * Wrapper for VkRayTracingPipelineCreateInfoKHR
     * Provided by VK_KHR_ray_tracing_pipeline
     */
    public static class RayTracingPipelineCreateInfoKHR extends PipelineCreateInfo {

        public final RayTracingShaderGroupCreateInfoKHR[] groups;
        public final int maxPipelineRayRecursionDepth;

        public RayTracingPipelineCreateInfoKHR(PipelineCreateFlagBit[] flags,
                @NonNull PipelineShaderStageCreateInfo[] stages, RayTracingShaderGroupCreateInfoKHR[] groups,
                int maxPipelineRayRecursionDepth, @NonNull PipelineLayout layout, int basePipelineIndex,
                Pipeline basePipelineHandle) {
            super(flags, stages, layout, basePipelineIndex, basePipelineHandle);
            this.maxPipelineRayRecursionDepth = maxPipelineRayRecursionDepth;
            this.groups = groups;
        }

    }

    /**
     * Creates a raytracing pipeline
     * 
     * @param createInfo
     * @return
     */
    public abstract RayTracingPipeline createRayTracingPipeline(RayTracingPipelineCreateInfoKHR createInfo,
            RayTracingShader rayTracingShader);

}
