package org.varg.lwjgl3.vulkan.extensions;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkRayTracingPipelineCreateInfoKHR;
import org.lwjgl.vulkan.VkRayTracingShaderGroupCreateInfoKHR;
import org.lwjgl.vulkan.VkStridedDeviceAddressRegionKHR;
import org.varg.lwjgl3.vulkan.LWJGL3Vulkan12Backend;
import org.varg.lwjgl3.vulkan.LWJGLVulkan12Queue;
import org.varg.shader.RayTracingShader;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan12;
import org.varg.vulkan.VulkanBackend;
import org.varg.vulkan.extensions.KHRAccelerationStructure;
import org.varg.vulkan.extensions.KHRAccelerationStructure.DeviceOrHostAddress;
import org.varg.vulkan.extensions.KHRRayTracingPipeline;
import org.varg.vulkan.extensions.PhysicalDeviceRayTracingPipelineFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceRayTracingPipelinePropertiesKHR;
import org.varg.vulkan.memory.DeviceMemory;
import org.varg.vulkan.memory.Memory;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.pipeline.Pipeline;
import org.varg.vulkan.pipeline.RayTracingPipeline;

public class LWJGL3KHRRayTracingPipeline extends KHRRayTracingPipeline<LWJGLVulkan12Queue> {

    private final VkDevice deviceInstance;

    public LWJGL3KHRRayTracingPipeline(VkDevice deviceInstance, PhysicalDeviceRayTracingPipelineFeaturesKHR rayTracingFeatures, PhysicalDeviceRayTracingPipelinePropertiesKHR rayTracingProperties) {
        super(rayTracingFeatures, rayTracingProperties);
        if (deviceInstance == null) {
            throw new IllegalArgumentException();
        }
        this.deviceInstance = deviceInstance;
    }

    private VkRayTracingShaderGroupCreateInfoKHR.Buffer createVkShaderGroupCreateInfo(RayTracingShaderGroupCreateInfoKHR... createInfos) {

        VkRayTracingShaderGroupCreateInfoKHR.Buffer vkInfo = VkRayTracingShaderGroupCreateInfoKHR.calloc(createInfos.length);

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
    public RayTracingPipeline createRayTracingPipeline(RayTracingPipelineCreateInfoKHR createInfo, RayTracingShader rayTracingShader) {

        VkPipelineShaderStageCreateInfo.Buffer shaderStages = LWJGL3Vulkan12Backend.createVkShaderStageCreateInfo(createInfo.getStages());
        VkRayTracingShaderGroupCreateInfoKHR.Buffer vkGroupInfo = createVkShaderGroupCreateInfo(createInfo.groups);

        VkRayTracingPipelineCreateInfoKHR.Buffer vkCreateInfo = VkRayTracingPipelineCreateInfoKHR.calloc(1)
                .sType(org.lwjgl.vulkan.KHRRayTracingPipeline.VK_STRUCTURE_TYPE_RAY_TRACING_PIPELINE_CREATE_INFO_KHR)
                .pNext(MemoryUtil.NULL)
                .flags(createInfo.getCreateFlagsValue())
                .pStages(shaderStages)
                .pGroups(vkGroupInfo.position(0))
                .maxPipelineRayRecursionDepth(createInfo.maxPipelineRayRecursionDepth)
                .layout(createInfo.getPipelineLayout().getPipelineLayout());
        LongBuffer pPipelines = MemoryUtil.memAllocLong(1);

        int result = org.lwjgl.vulkan.KHRRayTracingPipeline.vkCreateRayTracingPipelinesKHR(deviceInstance, VK12.VK_NULL_HANDLE, VK12.VK_NULL_HANDLE, vkCreateInfo, null, pPipelines);
        shaderStages.free();
        vkCreateInfo.free();
        VulkanBackend.assertResult(result);
        RayTracingPipeline rayTracingPipeline = new RayTracingPipeline(pPipelines.get(0), createInfo);
        MemoryUtil.memFree(pPipelines);
        return rayTracingPipeline;
    }

    @Override
    public void getRayTracingShaderGroupHandlesKHR(Pipeline pipeline, int firstGroup, int groupCount, ByteBuffer buffer) {
        int result = org.lwjgl.vulkan.KHRRayTracingPipeline.vkGetRayTracingShaderGroupHandlesKHR(deviceInstance, pipeline.getPipeline(), firstGroup, groupCount, buffer);
        VulkanBackend.assertResult(result);
    }

    @Override
    public DeviceOrHostAddress createSBT(DeviceMemory allocator, KHRAccelerationStructure asExtension, RayTracingPipeline rayTracingPipeline, ByteBuffer groupHandleBuffer) {
        int baseAlignment = rayTracingProperties.shaderGroupBaseAlignment;
        int handleAlignment = rayTracingProperties.shaderGroupHandleAlignment;
        int handleSize = rayTracingProperties.shaderGroupHandleSize;
        int groupCount = groupHandleBuffer.remaining() / handleSize;

        int[] sbtSizes = rayTracingPipeline.getSBTSizes(handleAlignment, groupCount);
        int[] alignedSizes = getAlignedSizes(sbtSizes, baseAlignment);
        int alignedTotal = alignedSizes[0] + alignedSizes[1] + alignedSizes[2] + alignedSizes[3];
        int flags = BitFlags.getFlagsValue(Vulkan10.BufferUsageFlagBit.VK_BUFFER_USAGE_SHADER_BINDING_TABLE_BIT_KHR, Vulkan10.BufferUsageFlagBit.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
                Vulkan10.BufferUsageFlagBit.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
        MemoryBuffer memBuff = allocator.createBuffer(alignedTotal, flags);
        Memory memory = allocator.allocateMemory(memBuff.allocationSize, BitFlags.getFlagsValue(Vulkan10.MemoryPropertyFlagBit.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT),
                BitFlags.getFlagsValue(Vulkan12.MemoryAllocateFlagBits.VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT), 0);
        allocator.bindBufferMemory(memBuff, memory, 0);

        // Create the backing sbt byte buffer
        ByteBuffer sbt = Buffers.createByteBuffer((int) memBuff.size);
        // Copy the handles into sbt - do this copy since we may want to add user data.
        putHandles(groupCount, groupHandleBuffer, sbt, alignedSizes[0]); // raygen
        putHandles(groupCount, groupHandleBuffer, sbt, alignedSizes[1]); // miss
        putHandles(groupCount, groupHandleBuffer, sbt, alignedSizes[2]); // hit
        putHandles(groupCount, groupHandleBuffer, sbt, alignedSizes[3]); // callable

        DeviceOrHostAddress sbtAdress = asExtension.getBufferDeviceAddress(memBuff);
        rayTracingPipeline.setSBT(sbt.position(0), sbtAdress, alignedSizes, groupCount);
        return sbtAdress;
    }

    private int[] getAlignedSizes(int[] sbtSizes, int baseAlignment) {
        int[] alignedSizes = new int[4];
        for (int i = 0; i < sbtSizes.length; i++) {
            int size = sbtSizes[i];
            int sizeUp = size + (baseAlignment - 1);
            int xor = 0x0ffffffff ^ (baseAlignment - 1);
            alignedSizes[i] = sizeUp & xor;
        }
        return alignedSizes;
    }

    private void putHandles(int groupCount, ByteBuffer handle, ByteBuffer sbt, int sbtStride) {
        int pos = sbt.position();
        for (int i = 0; i < groupCount; i++) {
            int limit = handle.position() + Long.BYTES;
            handle.limit(limit);
            sbt.put(handle);
            sbt.position(pos + sbtStride);
        }
    }

    @Override
    public void cmdTraceRaysKHR(StridedDeviceAddressRegionKHR raygenShaderBindingTable, StridedDeviceAddressRegionKHR missShaderBindingTable, StridedDeviceAddressRegionKHR hitShaderBindingTable,
            StridedDeviceAddressRegionKHR callableShaderBindingTable, int width, int height, int depth, LWJGLVulkan12Queue queue) {

        VkStridedDeviceAddressRegionKHR raySBT = getStridedAdress(raygenShaderBindingTable);
        VkStridedDeviceAddressRegionKHR missSBT = getStridedAdress(missShaderBindingTable);
        VkStridedDeviceAddressRegionKHR hitSBT = getStridedAdress(hitShaderBindingTable);
        VkStridedDeviceAddressRegionKHR callableSBT = getStridedAdress(callableShaderBindingTable);

        org.lwjgl.vulkan.KHRRayTracingPipeline.vkCmdTraceRaysKHR(queue.getCommandBuffer(), raySBT, missSBT, hitSBT, callableSBT, width, height, depth);

        raySBT.free();
        missSBT.free();
        hitSBT.free();
        callableSBT.free();
    }

    private VkStridedDeviceAddressRegionKHR getStridedAdress(StridedDeviceAddressRegionKHR sbt) {
        VkStridedDeviceAddressRegionKHR vkSBT = VkStridedDeviceAddressRegionKHR.calloc()
                .set(sbt.deviceAddress, sbt.stride, sbt.size);
        return vkSBT;
    }

}
