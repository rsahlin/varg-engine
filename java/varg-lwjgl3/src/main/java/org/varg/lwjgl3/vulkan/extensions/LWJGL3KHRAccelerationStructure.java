package org.varg.lwjgl3.vulkan.extensions;

import java.nio.LongBuffer;

import org.gltfio.lib.ErrorMessage;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAccelerationStructureBuildGeometryInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureBuildRangeInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureBuildSizesInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureCreateInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryAabbsDataKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryDataKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryInstancesDataKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryKHR;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceOrHostAddressConstKHR;
import org.lwjgl.vulkan.VkDeviceOrHostAddressKHR;
import org.varg.lwjgl3.vulkan.LWJGLVulkan12Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.QueryType;
import org.varg.vulkan.extensions.KHRAccelerationStructure;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.structs.QueryPool;

public class LWJGL3KHRAccelerationStructure extends KHRAccelerationStructure<LWJGLVulkan12Queue> {

    private final VkDevice deviceInstance;

    public LWJGL3KHRAccelerationStructure(VkDevice deviceInstance) {
        if (deviceInstance == null) {
            throw new IllegalArgumentException();
        }
        this.deviceInstance = deviceInstance;
    }

    @Override
    public DeviceOrHostAddress getBufferDeviceAddress(MemoryBuffer buffer) {
        if ((buffer.usage & Vulkan10.BufferUsageFlagBit.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT.value) == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message +
                    "Buffer must be created with VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT");
        }
        VkBufferDeviceAddressInfo info = VkBufferDeviceAddressInfo.calloc();
        info.sType(VK12.VK_STRUCTURE_TYPE_BUFFER_DEVICE_ADDRESS_INFO)
                .buffer(buffer.getPointer());

        long result = VK12.vkGetBufferDeviceAddress(deviceInstance, info);
        info.free();
        return new org.varg.vulkan.extensions.KHRAccelerationStructure.DeviceOrHostAddress(buffer, result);
    }

    @Override
    public AccelerationStructureKHR createAccelerationStructureKHR(AccelerationStructureCreateInfoKHR createInfo) {
        VkAccelerationStructureCreateInfoKHR vkInfo = VkAccelerationStructureCreateInfoKHR.calloc()
                .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_CREATE_INFO_KHR)
                .pNext(MemoryUtil.NULL)
                .createFlags(createInfo.createFlags)
                .buffer(createInfo.buffer.getPointer())
                .offset(createInfo.offset)
                .size(createInfo.size)
                .type(createInfo.type.value)
                .deviceAddress(createInfo.deviceAddress);
        LongBuffer pointer = BufferUtils.createLongBuffer(1);
        org.lwjgl.vulkan.KHRAccelerationStructure.vkCreateAccelerationStructureKHR(deviceInstance, vkInfo, null,
                pointer);
        AccelerationStructureKHR result = new AccelerationStructureKHR(pointer.get(0), createInfo);
        vkInfo.free();
        return result;
    }

    @Override
    public AccelerationStructureBuildSizesInfoKHR getAccelerationStructureBuildSizesKHR(
            AccelerationStructureBuildTypeKHR buildType, AccelerationStructureBuildGeometryInfoKHR buildInfo,
            int... maxPrimitiveCounts) {

        VkAccelerationStructureBuildGeometryInfoKHR vkInfo = VkAccelerationStructureBuildGeometryInfoKHR.calloc();
        VkAccelerationStructureGeometryKHR.Buffer vkGeometry = VkAccelerationStructureGeometryKHR.calloc(1);
        setGeometry(buildInfo.geometries, vkGeometry);
        setBuildGeometryInfo(buildInfo, vkInfo, vkGeometry);

        VkAccelerationStructureBuildSizesInfoKHR sizeInfo = VkAccelerationStructureBuildSizesInfoKHR.calloc()
                .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_BUILD_SIZES_INFO_KHR)
                .pNext(MemoryUtil.NULL);

        org.lwjgl.vulkan.KHRAccelerationStructure.vkGetAccelerationStructureBuildSizesKHR(deviceInstance,
                buildType.value, vkInfo, maxPrimitiveCounts, sizeInfo);

        AccelerationStructureBuildSizesInfoKHR sizes = new AccelerationStructureBuildSizesInfoKHR(
                sizeInfo.accelerationStructureSize(), sizeInfo.updateScratchSize(), sizeInfo.buildScratchSize(),
                buildInfo);
        // TODO - will this free referenced structs?
        vkInfo.free();
        vkGeometry.free();
        sizeInfo.free();
        return sizes;
    }

    private void setGeometry(AccelerationStructureGeometryKHR geometry,
            VkAccelerationStructureGeometryKHR.Buffer vkGeometry) {
        VkDeviceOrHostAddressConstKHR deviceAddress = VkDeviceOrHostAddressConstKHR.calloc()
                .deviceAddress(geometry.geometry.data.deviceAddress);

        VkAccelerationStructureGeometryDataKHR geometryData = VkAccelerationStructureGeometryDataKHR.calloc();

        switch (geometry.geometryType) {
            case VK_GEOMETRY_TYPE_AABBS_KHR:
                VkAccelerationStructureGeometryAabbsDataKHR vkAABB = VkAccelerationStructureGeometryAabbsDataKHR
                        .calloc()
                        .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_GEOMETRY_AABBS_DATA_KHR)
                        .pNext(MemoryUtil.NULL)
                        .data(deviceAddress)
                        .stride(geometry.geometry.stride);
                geometryData.aabbs(vkAABB);
                break;
            case VK_GEOMETRY_TYPE_INSTANCES_KHR:
                VkAccelerationStructureGeometryInstancesDataKHR vkInstances =
                        VkAccelerationStructureGeometryInstancesDataKHR.calloc()
                                .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_GEOMETRY_INSTANCES_DATA_KHR)
                                .pNext(MemoryUtil.NULL)
                                .data(deviceAddress)
                                .arrayOfPointers(false);
                geometryData.instances(vkInstances);
                break;
            default:
                throw new IllegalArgumentException();
        }

        vkGeometry
                .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_GEOMETRY_KHR)
                .pNext(MemoryUtil.NULL)
                .geometryType(geometry.geometryType.value)
                .geometry(geometryData)
                .flags(geometry.flags);
    }

    private void setBuildGeometryInfo(AccelerationStructureBuildGeometryInfoKHR buildInfo,
            VkAccelerationStructureBuildGeometryInfoKHR vkInfo, VkAccelerationStructureGeometryKHR.Buffer vkGeometry) {
        /**
         * The srcAccelerationStructure, dstAccelerationStructure, and mode members of pBuildInfo are ignored.
         * Any VkDeviceOrHostAddressKHR or VkDeviceOrHostAddressConstKHR members of pBuildInfo are ignored by this
         * command, except that the hostAddress member of
         * VkAccelerationStructureGeometryTrianglesDataKHR::transformData will be examined to check if it is NULL.
         */
        VkDeviceOrHostAddressKHR deviceAddress = null;
        if (buildInfo.scratchData != null) {
            deviceAddress = VkDeviceOrHostAddressKHR.calloc();
            deviceAddress.deviceAddress(buildInfo.scratchData.deviceAddress);
        }
        vkInfo.sType(
                org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_BUILD_GEOMETRY_INFO_KHR)
                .pNext(MemoryUtil.NULL)
                .type(buildInfo.type.value)
                .geometryCount(buildInfo.geometryCount)
                .pGeometries(vkGeometry)
                .dstAccelerationStructure(
                        buildInfo.dstAccelerationStructure != null ? buildInfo.dstAccelerationStructure.getHandle() : 0)
                .flags(buildInfo.flags);
        if (deviceAddress != null) {
            vkInfo.scratchData(deviceAddress);
        }
    }

    @Override
    public void cmdBuildAccelerationStructuresKHR(LWJGLVulkan12Queue queue,
            AccelerationStructureBuildGeometryInfoKHR[] infos,
            AccelerationStructureBuildRangeInfoKHR[] buildRangeInfos) {

        VkAccelerationStructureBuildGeometryInfoKHR.Buffer infoBuffer =
                VkAccelerationStructureBuildGeometryInfoKHR.calloc(infos.length);

        VkAccelerationStructureBuildRangeInfoKHR.Buffer rangeBuffer =
                VkAccelerationStructureBuildRangeInfoKHR.calloc(buildRangeInfos.length);

        PointerBuffer pointers = PointerBuffer.allocateDirect(infos.length);

        for (int i = 0; i < infos.length; i++) {
            AccelerationStructureBuildGeometryInfoKHR info = infos[i];
            if (info.dstAccelerationStructure == null || info.scratchData == null) {
                throw new IllegalArgumentException();
            }
            if (info.dstAccelerationStructure.getCreateInfo().type != info.type) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Type does not match");
            }
            VkAccelerationStructureGeometryKHR.Buffer vkGeometry = VkAccelerationStructureGeometryKHR.calloc(1);
            setGeometry(info.geometries, vkGeometry);
            setBuildGeometryInfo(info, infoBuffer.get(), vkGeometry);

            AccelerationStructureBuildRangeInfoKHR rangeInfo = buildRangeInfos[i];
            VkAccelerationStructureBuildRangeInfoKHR instance = rangeBuffer.get()
                    .primitiveCount(rangeInfo.primitiveCount)
                    .primitiveOffset(rangeInfo.primitiveOffset)
                    .firstVertex(rangeInfo.firstVertex)
                    .transformOffset(rangeInfo.transformOffset);
            pointers.put(instance.address());
        }
        infoBuffer.position(0);
        pointers.position(0);
        VkCommandBuffer cmd = queue.getCommandBuffer();
        org.lwjgl.vulkan.KHRAccelerationStructure.vkCmdBuildAccelerationStructuresKHR(cmd, infoBuffer, pointers);
        infoBuffer.free();
        rangeBuffer.free();
    }

    @Override
    public void cmdWriteAccelerationStructuresPropertiesKHR(LWJGLVulkan12Queue queue, QueryType queryType, QueryPool queryPool, int firstQuery, AccelerationStructureKHR accelerationStructures) {
        VkCommandBuffer cmd = queue.getCommandBuffer();
        org.lwjgl.vulkan.KHRAccelerationStructure.vkCmdWriteAccelerationStructuresPropertiesKHR(cmd, accelerationStructures.getHandleBuffer(), queryType.value, queryPool.getHandle(), firstQuery);
    }

}
