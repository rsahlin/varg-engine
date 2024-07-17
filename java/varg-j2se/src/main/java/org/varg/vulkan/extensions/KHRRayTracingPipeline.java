package org.varg.vulkan.extensions;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.BitFlag;
import org.gltfio.lib.Buffers;
import org.varg.shader.RayTracingShader;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.PipelineCreateFlagBit;
import org.varg.vulkan.extensions.KHRAccelerationStructure.DeviceOrHostAddress;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.pipeline.Pipeline;
import org.varg.vulkan.pipeline.PipelineCreateInfo;
import org.varg.vulkan.pipeline.PipelineLayout;
import org.varg.vulkan.pipeline.PipelineShaderStageCreateInfo;
import org.varg.vulkan.pipeline.RayTracingPipeline;

/**
 * VK_KHR_ray_tracing_pipeline extension
 */
public abstract class KHRRayTracingPipeline<Q extends Queue> {

    public static final int VK_SHADER_UNUSED_KHR = -1;

    protected final PhysicalDeviceRayTracingPipelineFeaturesKHR rayTracingFeatures;
    protected final PhysicalDeviceRayTracingPipelinePropertiesKHR rayTracingProperties;

    protected KHRRayTracingPipeline(PhysicalDeviceRayTracingPipelineFeaturesKHR rayTracingFeatures, PhysicalDeviceRayTracingPipelinePropertiesKHR rayTracingProperties) {
        this.rayTracingFeatures = rayTracingFeatures;
        this.rayTracingProperties = rayTracingProperties;
    }

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
            this.generalShader = VK_SHADER_UNUSED_KHR;
            this.closestHitShader = VK_SHADER_UNUSED_KHR;
            this.anyHitShader = VK_SHADER_UNUSED_KHR;
            this.intersectionShader = VK_SHADER_UNUSED_KHR;
        }

        public RayTracingShaderGroupCreateInfoKHR(RayTracingShaderGroupTypeKHR type, PipelineShaderStageCreateInfo... stageInfos) {
            this.type = type;
            switch (this.type) {
                case VK_RAY_TRACING_SHADER_GROUP_TYPE_TRIANGLES_HIT_GROUP_KHR:
                    generalShader = VK_SHADER_UNUSED_KHR;
                    closestHitShader = KHRRayTracingPipeline.getStageIndex(Vulkan10.ShaderStageFlagBit.VK_SHADER_STAGE_CLOSEST_HIT_BIT_KHR, stageInfos);
                    anyHitShader = KHRRayTracingPipeline.getStageIndex(Vulkan10.ShaderStageFlagBit.VK_SHADER_STAGE_ANY_HIT_BIT_KHR, stageInfos);
                    intersectionShader = KHRRayTracingPipeline.getStageIndex(Vulkan10.ShaderStageFlagBit.VK_SHADER_STAGE_INTERSECTION_BIT_KHR, stageInfos);
                    break;
                case VK_RAY_TRACING_SHADER_GROUP_TYPE_PROCEDURAL_HIT_GROUP_KHR:
                case VK_RAY_TRACING_SHADER_GROUP_TYPE_GENERAL_KHR:
                default:
                    throw new IllegalArgumentException();
            }
        }

    }

    /**
     * Wrapper for SkStridedDeviceAddressRegionKHR
     */
    public static class StridedDeviceAddressRegionKHR {
        public final long deviceAddress;
        public final int stride;
        public final int size;

        public StridedDeviceAddressRegionKHR(long deviceAddress, int stride, int size) {
            this.deviceAddress = deviceAddress;
            this.stride = stride;
            this.size = size;
        }
    }

    /**
     * Creates a bytebuffer to store group handles
     * 
     * @param groups
     * @return
     */
    public ByteBuffer createGroupHandleBuffer(RayTracingShaderGroupCreateInfoKHR... groups) {
        ByteBuffer pipelineHandles = Buffers.createByteBuffer(groups.length * rayTracingProperties.shaderGroupHandleSize);
        return pipelineHandles;
    }

    public static int getStageIndex(Vulkan10.ShaderStageFlagBit stage, PipelineShaderStageCreateInfo... stageInfos) {
        for (int i = 0; i < stageInfos.length; i++) {
            if (stage == stageInfos[i].getStage()) {
                return i;
            }
        }
        return VK_SHADER_UNUSED_KHR;
    }

    /**
     * Wrapper for VkRayTracingPipelineCreateInfoKHR
     * Provided by VK_KHR_ray_tracing_pipeline
     */
    public static class RayTracingPipelineCreateInfoKHR extends PipelineCreateInfo {

        public final RayTracingShaderGroupCreateInfoKHR[] groups;
        public final int maxPipelineRayRecursionDepth;

        public RayTracingPipelineCreateInfoKHR(PipelineCreateFlagBit[] flags, @NonNull PipelineShaderStageCreateInfo[] stages, RayTracingShaderGroupCreateInfoKHR[] groups, int maxPipelineRayRecursionDepth, @NonNull PipelineLayout layout,
                int basePipelineIndex, Pipeline basePipelineHandle) {
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
    public abstract RayTracingPipeline createRayTracingPipeline(RayTracingPipelineCreateInfoKHR createInfo, RayTracingShader rayTracingShader);

    /**
     * Query for the pipeline handles
     * VkStridedDeviceAddressRegionKHR* pRaygenShaderBindingTable
     * VkStridedDeviceAddressRegionKHR* pMissShaderBindingTable
     * VkStridedDeviceAddressRegionKHR* pHitShaderBindingTable
     * VkStridedDeviceAddressRegionKHR* pCallableShaderBindingTable
     * 
     * @param pipeline
     * @param firstGroup
     * @param groupCount
     * @param buffer
     */
    public abstract void getRayTracingShaderGroupHandlesKHR(Pipeline pipeline, int firstGroup, int groupCount, ByteBuffer buffer);

    /**
     * Creates device memorybuffer and memory for the SBT, the returned memorybuffer will be bound
     * 
     * @param allocator
     * @param rayTracingPipeline
     * @param groupHandleBuffer Buffer containing the shader group handles (vkGetRayTracingShaderGroupHandlesKHR)
     * @return
     */
    public abstract DeviceOrHostAddress createSBT(DeviceMemory allocator, KHRAccelerationStructure asExtension, RayTracingPipeline rayTracingPipeline, ByteBuffer groupHandleBuffer);

    /**
     * Wrapper for vkCmdTraceRaysKHR
     * 
     * @param raygenShaderBindingTable
     * @param missShaderBindingTable
     * @param hitShaderBindingTable
     * @param callableShaderBindingTable
     * @param width
     * @param height
     * @param depth
     */
    public abstract void cmdTraceRaysKHR(StridedDeviceAddressRegionKHR raygenShaderBindingTable, StridedDeviceAddressRegionKHR missShaderBindingTable, StridedDeviceAddressRegionKHR hitShaderBindingTable,
            StridedDeviceAddressRegionKHR callableShaderBindingTable, int width, int height, int depth, Q queue);

}
