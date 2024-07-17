package org.varg.lwjgl3.vulkan.extensions;

import java.nio.LongBuffer;

import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAccelerationStructureBuildGeometryInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureBuildRangeInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureBuildSizesInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureCreateInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureDeviceAddressInfoKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryAabbsDataKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryDataKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryInstancesDataKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryKHR;
import org.lwjgl.vulkan.VkAccelerationStructureGeometryTrianglesDataKHR;
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
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Buffer must be created with VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT");
        }
        VkBufferDeviceAddressInfo info = VkBufferDeviceAddressInfo.calloc()
                .sType(VK12.VK_STRUCTURE_TYPE_BUFFER_DEVICE_ADDRESS_INFO)
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
        org.lwjgl.vulkan.KHRAccelerationStructure.vkCreateAccelerationStructureKHR(deviceInstance, vkInfo, null, pointer);
        long asPointer = pointer.get(0);
        VkAccelerationStructureDeviceAddressInfoKHR vkAdressInfo = VkAccelerationStructureDeviceAddressInfoKHR.calloc()
                .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_DEVICE_ADDRESS_INFO_KHR)
                .accelerationStructure(asPointer);
        long asReference = org.lwjgl.vulkan.KHRAccelerationStructure.vkGetAccelerationStructureDeviceAddressKHR(deviceInstance, vkAdressInfo);
        AccelerationStructureKHR result = new AccelerationStructureKHR(asPointer, asReference, createInfo);
        vkInfo.free();
        vkAdressInfo.free();
        Logger.d(getClass(), "Created AccelerationStructureKHR with size: " + createInfo.size + ", offset: " + createInfo.offset + ", type: " + createInfo.type);
        return result;
    }

    @Override
    public AccelerationStructureBuildSizesInfoKHR getAccelerationStructureBuildSizesKHR(AccelerationStructureBuildTypeKHR buildType, AccelerationStructureBuildGeometryInfoKHR buildInfo, int maxPrimitiveCount, GeometryTypeKHR type) {

        VkAccelerationStructureBuildGeometryInfoKHR.Buffer vkInfo = VkAccelerationStructureBuildGeometryInfoKHR.calloc(1);
        VkAccelerationStructureGeometryKHR.Buffer vkGeometry = VkAccelerationStructureGeometryKHR.calloc(1);
        LWJGL3KHRAccelerationStructure.setGeometry(buildInfo.geometries, vkGeometry);
        LWJGL3KHRAccelerationStructure.setBuildGeometryInfo(buildInfo, vkInfo.get(0), vkGeometry.position(0));

        VkAccelerationStructureBuildSizesInfoKHR sizeInfo = VkAccelerationStructureBuildSizesInfoKHR.calloc()
                .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_BUILD_SIZES_INFO_KHR)
                .pNext(MemoryUtil.NULL);

        org.lwjgl.vulkan.KHRAccelerationStructure.vkGetAccelerationStructureBuildSizesKHR(deviceInstance, buildType.value, vkInfo.get(0), new int[] { maxPrimitiveCount }, sizeInfo);

        AccelerationStructureBuildSizesInfoKHR sizes = new AccelerationStructureBuildSizesInfoKHR(sizeInfo.accelerationStructureSize(), sizeInfo.updateScratchSize(), sizeInfo.buildScratchSize());
        // TODO - will this free referenced structs?
        vkInfo.free();
        vkGeometry.free();
        sizeInfo.free();
        return sizes;
    }

    private static void setGeometry(AccelerationStructureGeometryDataKHR geometries, VkAccelerationStructureGeometryKHR.Buffer vkGeometryBuffer) {
        VkDeviceOrHostAddressConstKHR deviceAddress = VkDeviceOrHostAddressConstKHR.calloc().deviceAddress(geometries.data.deviceAddress);
        VkAccelerationStructureGeometryDataKHR geometryData = VkAccelerationStructureGeometryDataKHR.calloc();
        switch (geometries.geometryType) {
            case VK_GEOMETRY_TYPE_AABBS_KHR:
                AccelerationStructureGeometryAabbsDataKHR aabbData = (AccelerationStructureGeometryAabbsDataKHR) geometries;
                VkAccelerationStructureGeometryAabbsDataKHR vkAABB = VkAccelerationStructureGeometryAabbsDataKHR
                        .calloc()
                        .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_GEOMETRY_AABBS_DATA_KHR)
                        .pNext(MemoryUtil.NULL)
                        .data(deviceAddress)
                        .stride(aabbData.stride);
                geometryData.aabbs(vkAABB);
                break;
            case VK_GEOMETRY_TYPE_TRIANGLES_KHR:
                AccelerationStructureGeometryTrianglesDataKHR trianglesData = (AccelerationStructureGeometryTrianglesDataKHR) geometries;
                VkAccelerationStructureGeometryTrianglesDataKHR vkTriangles = VkAccelerationStructureGeometryTrianglesDataKHR.calloc()
                        .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_GEOMETRY_TRIANGLES_DATA_KHR)
                        .pNext(MemoryUtil.NULL)
                        .vertexData(deviceAddress)
                        .vertexFormat(trianglesData.vertexFormat.value)
                        .maxVertex(trianglesData.maxVertex)
                        .vertexStride(trianglesData.vertexStride)
                        .indexType(Vulkan10.IndexType.VK_INDEX_TYPE_NONE_KHR.value);
                if (trianglesData.indexType != null) {
                    VkDeviceOrHostAddressConstKHR indexAdress = VkDeviceOrHostAddressConstKHR.calloc().deviceAddress(trianglesData.indexData.deviceAddress);
                    vkTriangles.indexType(trianglesData.indexType.value)
                            .indexData(indexAdress);
                }
                geometryData.triangles(vkTriangles);
                break;

            case VK_GEOMETRY_TYPE_INSTANCES_KHR:
                VkAccelerationStructureGeometryInstancesDataKHR vkInstances = VkAccelerationStructureGeometryInstancesDataKHR.calloc()
                        .sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_GEOMETRY_INSTANCES_DATA_KHR)
                        .pNext(MemoryUtil.NULL)
                        .data(deviceAddress)
                        .arrayOfPointers(false);
                geometryData.instances(vkInstances);
                break;
            default:
                throw new IllegalArgumentException();
        }
        VkAccelerationStructureGeometryKHR vkGeometry = vkGeometryBuffer.get();
        vkGeometry.sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_GEOMETRY_KHR)
                .pNext(MemoryUtil.NULL)
                .geometryType(geometries.geometryType.value)
                .geometry(geometryData)
                .flags(0);
    }

    private static void setBuildGeometryInfo(AccelerationStructureBuildGeometryInfoKHR buildInfo, VkAccelerationStructureBuildGeometryInfoKHR vkInfo, VkAccelerationStructureGeometryKHR.Buffer vkGeometry) {
        /**
         * The srcAccelerationStructure, dstAccelerationStructure, and mode members of pBuildInfo are ignored.
         * Any VkDeviceOrHostAddressKHR or VkDeviceOrHostAddressConstKHR members of pBuildInfo are ignored by this
         * command, except that the hostAddress member of
         * VkAccelerationStructureGeometryTrianglesDataKHR::transformData will be examined to check if it is NULL.
         */
        VkDeviceOrHostAddressKHR deviceAddress = null;
        if (buildInfo.scratchData != 0) {
            deviceAddress = VkDeviceOrHostAddressKHR.calloc();
            deviceAddress.deviceAddress(buildInfo.scratchData);
        }
        vkInfo.sType(org.lwjgl.vulkan.KHRAccelerationStructure.VK_STRUCTURE_TYPE_ACCELERATION_STRUCTURE_BUILD_GEOMETRY_INFO_KHR)
                .pNext(MemoryUtil.NULL)
                .type(buildInfo.type.value)
                .geometryCount(vkGeometry.remaining())
                .pGeometries(vkGeometry)
                .mode(buildInfo.mode.value)
                .dstAccelerationStructure(buildInfo.dstAccelerationStructure != null ? buildInfo.dstAccelerationStructure.getHandle() : 0)
                .flags(buildInfo.flags);
        if (deviceAddress != null) {
            vkInfo.scratchData(deviceAddress);
        }
    }

    @Override
    public void cmdBuildAccelerationStructuresKHR(LWJGLVulkan12Queue queue, AccelerationStructureBuildGeometryInfoKHR[] infos, AccelerationStructureBuildRangeInfoKHR[] buildRangeInfos) {
        if (infos.length != buildRangeInfos.length) {
            throw new IllegalArgumentException();
        }
        VkAccelerationStructureBuildRangeInfoKHR.Buffer rangeBuffer = VkAccelerationStructureBuildRangeInfoKHR.calloc(buildRangeInfos.length);
        PointerBuffer pointers = PointerBuffer.allocateDirect(buildRangeInfos.length);

        VkAccelerationStructureGeometryKHR.Buffer vkGeometry = VkAccelerationStructureGeometryKHR.calloc(1);
        VkAccelerationStructureBuildGeometryInfoKHR.Buffer vkInfo = VkAccelerationStructureBuildGeometryInfoKHR.calloc(buildRangeInfos.length);
        // TODO - check that all infos use the same AccelerationStructureGeometryDataKHR geometry
        LWJGL3KHRAccelerationStructure.setGeometry(infos[0].geometries, vkGeometry.position(0));

        for (int i = 0; i < buildRangeInfos.length; i++) {
            AccelerationStructureBuildRangeInfoKHR rangeInfo = buildRangeInfos[i];
            AccelerationStructureBuildGeometryInfoKHR info = infos[i];
            if (info.dstAccelerationStructure == null || info.scratchData == 0) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null value");
            }
            if (info.dstAccelerationStructure.getCreateInfo().type != info.type) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Type does not match");
            }
            if (info.mode != infos[0].mode) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Mode does not match");
            }
            LWJGL3KHRAccelerationStructure.setBuildGeometryInfo(info, vkInfo.get(), vkGeometry.position(0));

            VkAccelerationStructureBuildRangeInfoKHR instance = rangeBuffer.get()
                    .primitiveCount(rangeInfo.primitiveCount)
                    .primitiveOffset(rangeInfo.primitiveOffset)
                    .firstVertex(rangeInfo.firstVertex)
                    .transformOffset(rangeInfo.transformOffset);
            pointers.put(instance.address());
        }
        pointers.position(0);
        vkInfo.position(0);
        VkCommandBuffer cmd = queue.getCommandBuffer();
        org.lwjgl.vulkan.KHRAccelerationStructure.vkCmdBuildAccelerationStructuresKHR(cmd, vkInfo, pointers);
        Logger.d(getClass(), "Created " + buildRangeInfos.length + " acceleration structures, using  mode " + infos[0].mode + " for AS type " + infos[0].type);
        vkInfo.free();
        rangeBuffer.free();
    }

    @Override
    public void cmdWriteAccelerationStructuresPropertiesKHR(LWJGLVulkan12Queue queue, QueryType queryType, QueryPool queryPool, int firstQuery, AccelerationStructureKHR accelerationStructures) {
        VkCommandBuffer cmd = queue.getCommandBuffer();
        org.lwjgl.vulkan.KHRAccelerationStructure.vkCmdWriteAccelerationStructuresPropertiesKHR(cmd, accelerationStructures.getHandleBuffer(), queryType.value, queryPool.getHandle(), firstQuery);
    }

}
