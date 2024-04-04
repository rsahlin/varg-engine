package org.varg.lwjgl3.vulkan.extensions;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkRayTracingPipelineCreateInfoKHR;
import org.lwjgl.vulkan.VkRayTracingShaderGroupCreateInfoKHR;
import org.varg.lwjgl3.vulkan.LWJGL3Vulkan12Backend;
import org.varg.lwjgl3.vulkan.LWJGLVulkan12Queue;
import org.varg.shader.RayTracingShader;
import org.varg.vulkan.VulkanBackend;
import org.varg.vulkan.extensions.KHRRayTracingPipeline;
import org.varg.vulkan.pipeline.RayTracingPipeline;

public class LWJGL3KHRRayTracingPipeline extends KHRRayTracingPipeline<LWJGLVulkan12Queue> {

    private final VkDevice deviceInstance;

    public LWJGL3KHRRayTracingPipeline(VkDevice deviceInstance) {
        if (deviceInstance == null) {
            throw new IllegalArgumentException();
        }
        this.deviceInstance = deviceInstance;
    }

    private VkRayTracingShaderGroupCreateInfoKHR.Buffer
            createVkShaderGroupCreateInfo(RayTracingShaderGroupCreateInfoKHR... createInfos) {

        VkRayTracingShaderGroupCreateInfoKHR.Buffer vkInfo =
                VkRayTracingShaderGroupCreateInfoKHR.calloc(createInfos.length);

        for (RayTracingShaderGroupCreateInfoKHR createInfo : createInfos) {
            vkInfo.get()
                    .sType(org.lwjgl.vulkan.KHRRayTracingPipeline.VK_STRUCTURE_TYPE_RAY_TRACING_SHADER_GROUP_CREATE_INFO_KHR)
                    .pNext(MemoryUtil.NULL)
                    .type(createInfo.type.value)
                    .generalShader(createInfo.generalShader)
                    .closestHitShader(createInfo.closestHitShader)
                    .anyHitShader(createInfo.anyHitShader)
                    .intersectionShader(createInfo.intersectionShader)
                    .pShaderGroupCaptureReplayHandle(MemoryUtil.NULL);
        }
        return vkInfo;
    }

    @Override
    public RayTracingPipeline createRayTracingPipeline(RayTracingPipelineCreateInfoKHR createInfo,
            RayTracingShader rayTracingShader) {

        VkPipelineShaderStageCreateInfo.Buffer shaderStages =
                LWJGL3Vulkan12Backend.createVkShaderStageCreateInfo(createInfo.getStages());
        VkRayTracingShaderGroupCreateInfoKHR.Buffer vkGroupInfo = createVkShaderGroupCreateInfo(createInfo.groups);

        VkRayTracingPipelineCreateInfoKHR.Buffer vkCreateInfo = VkRayTracingPipelineCreateInfoKHR.calloc(1)
                .sType(org.lwjgl.vulkan.KHRRayTracingPipeline.VK_STRUCTURE_TYPE_RAY_TRACING_PIPELINE_CREATE_INFO_KHR)
                .pNext(MemoryUtil.NULL)
                .flags(createInfo.getCreateFlagsValue())
                .pStages(shaderStages)
                .pGroups(vkGroupInfo)
                .maxPipelineRayRecursionDepth(createInfo.maxPipelineRayRecursionDepth)
                .layout(createInfo.getPipelineLayout().getPipelineLayout());
        LongBuffer pPipelines = MemoryUtil.memAllocLong(1);

        int result = org.lwjgl.vulkan.KHRRayTracingPipeline.vkCreateRayTracingPipelinesKHR(deviceInstance,
                VK12.VK_NULL_HANDLE, VK12.VK_NULL_HANDLE, vkCreateInfo, null, pPipelines);
        shaderStages.free();
        vkCreateInfo.free();
        VulkanBackend.assertResult(result);
        RayTracingPipeline rayTracingPipeline = new RayTracingPipeline(pPipelines.get(0), createInfo);
        MemoryUtil.memFree(pPipelines);
        return rayTracingPipeline;
    }

}
