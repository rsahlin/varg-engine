package org.varg.vulkan.extensions;

import org.gltfio.lib.BitFlag;
import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Queue;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.memory.MemoryBuffer;
import org.varg.vulkan.structs.Handle;
import org.varg.vulkan.structs.PlatformStruct;
import org.varg.vulkan.structs.QueryPool;

/**
 * VK_KHR_acceleration_structure extension
 */
public abstract class KHRAccelerationStructure<Q extends Queue> {

    /**
     * Provided by VK_KHR_acceleration_structure
     * 
     * @param infos
     * @param buildRangeInfos
     */
    public abstract void cmdBuildAccelerationStructuresKHR(Q queue, AccelerationStructureBuildGeometryInfoKHR[] infos,
            AccelerationStructureBuildRangeInfoKHR[] buildRangeInfos);

    /**
     * Provided by VK_KHR_acceleration_structure
     * 
     * @param queue
     * @param queryType
     * @param queryPool
     * @param firstQuery
     * @param accelerationStructures
     */
    public abstract void cmdWriteAccelerationStructuresPropertiesKHR(Q queue, Vulkan10.QueryType queryType, QueryPool queryPool, int firstQuery, AccelerationStructureKHR accelerationStructures);

    /**
     * Query 64 bit buffer address which buffer memory can be accessed in a shader
     * 
     * @param Info
     * @return
     */
    public abstract KHRAccelerationStructure.DeviceOrHostAddress getBufferDeviceAddress(MemoryBuffer buffer);

    /**
     * Creates an accelerationstructure
     * Provided by VK_KHR_acceleration_structure
     * 
     * @param pCreateInfo
     * @return
     */
    public abstract AccelerationStructureKHR
            createAccelerationStructureKHR(AccelerationStructureCreateInfoKHR createInfo);

    /**
     * Retrieve the required size for an acceleration structure
     * Provided by VK_KHR_acceleration_structure
     * 
     * @param buildInfo
     * @param maxPrimitiveCounts
     * @return
     */
    public abstract AccelerationStructureBuildSizesInfoKHR getAccelerationStructureBuildSizesKHR(
            AccelerationStructureBuildTypeKHR buildType, AccelerationStructureBuildGeometryInfoKHR buildInfo,
            int... maxPrimitiveCounts);

    public enum BufferUsageFlagBit implements BitFlag {
        VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_BUILD_INPUT_READ_ONLY_BIT_KHR(0x80000),
        VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_STORAGE_BIT_KHR(0x100000);

        public int value;

        BufferUsageFlagBit(int value) {
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

    public enum AccelerationStructureCreateFlagBitsKHR implements BitFlag {
        VK_ACCELERATION_STRUCTURE_CREATE_DEVICE_ADDRESS_CAPTURE_REPLAY_BIT_KHR(1),
        // Provided by VK_EXT_descriptor_buffer
        VK_ACCELERATION_STRUCTURE_CREATE_DESCRIPTOR_BUFFER_CAPTURE_REPLAY_BIT_EXT(8),
        // Provided by VK_NV_ray_tracing_motion_blur
        VK_ACCELERATION_STRUCTURE_CREATE_MOTION_BIT_NV(4);

        public int value;

        AccelerationStructureCreateFlagBitsKHR(int value) {
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

    public enum AccelerationStructureBuildTypeKHR implements BitFlag {
        VK_ACCELERATION_STRUCTURE_BUILD_TYPE_HOST_KHR(0),
        VK_ACCELERATION_STRUCTURE_BUILD_TYPE_DEVICE_KHR(1),
        VK_ACCELERATION_STRUCTURE_BUILD_TYPE_HOST_OR_DEVICE_KHR(2);

        public int value;

        AccelerationStructureBuildTypeKHR(int value) {
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

    public enum GeometryFlagBitsKHR implements BitFlag {
        VK_GEOMETRY_OPAQUE_BIT_KHR(1),
        VK_GEOMETRY_NO_DUPLICATE_ANY_HIT_INVOCATION_BIT_KHR(2);

        public int value;

        GeometryFlagBitsKHR(int value) {
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

    public enum GeometryTypeKHR {
        VK_GEOMETRY_TYPE_TRIANGLES_KHR(0),
        VK_GEOMETRY_TYPE_AABBS_KHR(1),
        VK_GEOMETRY_TYPE_INSTANCES_KHR(2);

        public int value;

        GeometryTypeKHR(int value) {
            this.value = value;
        }

    }

    public enum BuildAccelerationStructureModeKHR {
        VK_BUILD_ACCELERATION_STRUCTURE_MODE_BUILD_KHR(0),
        VK_BUILD_ACCELERATION_STRUCTURE_MODE_UPDATE_KHR(1);

        public int value;

        BuildAccelerationStructureModeKHR(int value) {
            this.value = value;
        }
    }

    public enum BuildAccelerationStructureFlagBitsKHR implements BitFlag {
        VK_BUILD_ACCELERATION_STRUCTURE_ALLOW_UPDATE_BIT_KHR(0x00000001),
        VK_BUILD_ACCELERATION_STRUCTURE_ALLOW_COMPACTION_BIT_KHR(0x00000002),
        VK_BUILD_ACCELERATION_STRUCTURE_PREFER_FAST_TRACE_BIT_KHR(0x00000004),
        VK_BUILD_ACCELERATION_STRUCTURE_PREFER_FAST_BUILD_BIT_KHR(0x00000008),
        VK_BUILD_ACCELERATION_STRUCTURE_LOW_MEMORY_BIT_KHR(0x00000010),
        // Provided by VK_NV_ray_tracing_motion_blur
        VK_BUILD_ACCELERATION_STRUCTURE_MOTION_BIT_NV(0x00000020),
        // Provided by VK_EXT_opacity_micromap
        VK_BUILD_ACCELERATION_STRUCTURE_ALLOW_OPACITY_MICROMAP_UPDATE_EXT(0x00000040),
        // Provided by VK_EXT_opacity_micromap
        VK_BUILD_ACCELERATION_STRUCTURE_ALLOW_DISABLE_OPACITY_MICROMAPS_EXT(0x00000080),
        // Provided by VK_EXT_opacity_micromap
        VK_BUILD_ACCELERATION_STRUCTURE_ALLOW_OPACITY_MICROMAP_DATA_UPDATE_EXT(0x00000100),
        // Provided by VK_NV_displacement_micromap
        VK_BUILD_ACCELERATION_STRUCTURE_ALLOW_DISPLACEMENT_MICROMAP_UPDATE_NV(0x00000200),
        // Provided by VK_KHR_ray_tracing_position_fetch
        VK_BUILD_ACCELERATION_STRUCTURE_ALLOW_DATA_ACCESS_KHR(0x00000800);

        public int value;

        BuildAccelerationStructureFlagBitsKHR(int value) {
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

    public enum AccelerationStructureTypeKHR {
        VK_ACCELERATION_STRUCTURE_TYPE_TOP_LEVEL_KHR(0),
        VK_ACCELERATION_STRUCTURE_TYPE_BOTTOM_LEVEL_KHR(1),
        VK_ACCELERATION_STRUCTURE_TYPE_GENERIC_KHR(2);

        public int value;

        AccelerationStructureTypeKHR(int value) {
            this.value = value;
        }
    }

    public static class AccelerationStructureGeometryDataKHR {

        public final int stride;
        public final DeviceOrHostAddress data;

        protected AccelerationStructureGeometryDataKHR(DeviceOrHostAddress data, int stride) {
            this.data = data;
            this.stride = stride;
        }
    }

    public static class AccelerationStructureGeometryTrianglesDataKHR {
        Vulkan10.Format vertexFormat;
        long vertexData;
        int vertexStride;
        int maxVertex;
        Vulkan10.IndexType indexType;
        long indexData;
        long transformData;
    }

    public static class TransformMatrixKHR {
        float[][] matrix = new float[3][4];
    }

    public static class AccelerationStructureGeometryAabbsDataKHR extends AccelerationStructureGeometryDataKHR {

        public static final int AABB_SIZE_IN_BYTES = 6 * 4;

        public AccelerationStructureGeometryAabbsDataKHR(DeviceOrHostAddress data) {
            super(data, AABB_SIZE_IN_BYTES);
        }
    }

    public static class AccelerationStructureGeometryInstancesDataKHR extends AccelerationStructureGeometryDataKHR {

        boolean arrayOfPointers;
        long data;

        public static final int INSTANCE_SIZE_IN_BYTES = (3 * 4 + 4) * 4 + (8 * 4);

        public AccelerationStructureGeometryInstancesDataKHR(DeviceOrHostAddress data) {
            super(data, INSTANCE_SIZE_IN_BYTES);
        }
    }

    public static class AccelerationStructureGeometryKHR {
        public final GeometryTypeKHR geometryType;
        public final AccelerationStructureGeometryDataKHR geometry;
        public final int flags;

        public AccelerationStructureGeometryKHR(GeometryTypeKHR geometryType,
                AccelerationStructureGeometryDataKHR geometry, GeometryFlagBitsKHR... flags) {
            this.geometryType = geometryType;
            this.geometry = geometry;
            this.flags = BitFlags.getFlagsValue(flags);
        }

    }

    public static class AccelerationStructureBuildGeometryInfoKHR extends PlatformStruct {
        public final AccelerationStructureTypeKHR type;
        public final int flags;
        public BuildAccelerationStructureModeKHR mode;
        /**
         * srcAccelerationStructure is a pointer to an existing acceleration structure that is to be used to update the
         * dst acceleration structure when mode is VK_BUILD_ACCELERATION_STRUCTURE_MODE_UPDATE_KHR
         */
        public AccelerationStructureKHR srcAccelerationStructure;
        public AccelerationStructureKHR dstAccelerationStructure;
        public int geometryCount;
        public AccelerationStructureGeometryKHR geometries;
        public DeviceOrHostAddress scratchData;

        public AccelerationStructureBuildGeometryInfoKHR(AccelerationStructureTypeKHR type,
                AccelerationStructureGeometryKHR geometries, int geometryCount,
                BuildAccelerationStructureFlagBitsKHR... flags) {
            this.type = type;
            this.flags = BitFlags.getFlagsValue(flags);
            this.mode = BuildAccelerationStructureModeKHR.VK_BUILD_ACCELERATION_STRUCTURE_MODE_BUILD_KHR;
            this.geometries = geometries;
            this.geometryCount = geometryCount;
        }

        public AccelerationStructureBuildGeometryInfoKHR(AccelerationStructureTypeKHR type,
                BuildAccelerationStructureModeKHR mode,
                AccelerationStructureKHR srcAccelerationStructure,
                AccelerationStructureKHR dstAccelerationStructure,
                int geometryCount, AccelerationStructureGeometryKHR geometries,
                BuildAccelerationStructureFlagBitsKHR... flags) {
            this.type = type;
            this.mode = mode;
            this.srcAccelerationStructure = srcAccelerationStructure;
            this.dstAccelerationStructure = dstAccelerationStructure;
            this.geometryCount = geometryCount;
            this.geometries = geometries;
            this.flags = BitFlags.getFlagsValue(flags);
        }

        public void setAccelerationStruct(AccelerationStructureKHR accelerationStruct,
                DeviceOrHostAddress scratchData) {
            if (this.scratchData != null || this.dstAccelerationStructure != null) {
                throw new IllegalArgumentException();
            }
            this.scratchData = scratchData;
            this.dstAccelerationStructure = accelerationStruct;
        }

    }

    public static class AccelerationStructureBuildRangeInfoKHR extends PlatformStruct {

        public final int primitiveCount;
        public final int primitiveOffset;
        public final int firstVertex;
        public final int transformOffset;

        public AccelerationStructureBuildRangeInfoKHR(int primitiveCount, int primitiveOffset) {
            this.primitiveCount = primitiveCount;
            this.primitiveOffset = primitiveOffset;
            this.firstVertex = 0;
            this.transformOffset = 0;
        }

        public AccelerationStructureBuildRangeInfoKHR(int primitiveCount, int primitiveOffset, int firstVertex,
                int transformOffset) {
            this.primitiveCount = primitiveCount;
            this.primitiveOffset = primitiveOffset;
            this.firstVertex = firstVertex;
            this.transformOffset = transformOffset;
        }
    }

    public static class AccelerationStructureBuildSizesInfoKHR {
        /**
         * the size in bytes required in a VkAccelerationStructureKHR for a build or update operation
         */
        public final long accelerationStructureSize;
        /**
         * the size in bytes required in a scratch buffer for an update operation
         */
        public final long updateScratchSize;
        /**
         * the size in bytes required in a scratch buffer for a build operation
         */
        public final long buildScratchSize;

        private final AccelerationStructureBuildGeometryInfoKHR buildInfo;

        public AccelerationStructureBuildSizesInfoKHR(long accelerationStructureSize, long updateScratchSize,
                long buildScratchSize, AccelerationStructureBuildGeometryInfoKHR buildInfo) {
            this.accelerationStructureSize = accelerationStructureSize;
            this.updateScratchSize = updateScratchSize;
            this.buildScratchSize = buildScratchSize;
            this.buildInfo = buildInfo;
        }

        public AccelerationStructureBuildGeometryInfoKHR getBuildInfo() {
            return buildInfo;
        }

    }

    public static class AccelerationStructureKHR extends Handle<AccelerationStructureCreateInfoKHR> {

        public AccelerationStructureKHR(long adress, AccelerationStructureCreateInfoKHR createInfo) {
            super(adress, createInfo);
        }

    }

    public static class AccelerationStructureCreateInfoKHR {
        public final int createFlags;
        public final MemoryBuffer buffer;
        /**
         * Must be multiple of 256
         */
        public final int offset;
        public final long size;
        public final AccelerationStructureTypeKHR type;
        /**
         * deviceAddress is the device address requested for the acceleration structure if the
         * accelerationStructureCaptureReplay feature is being used. If deviceAddress is zero, no specific address
         * is
         * requested
         */
        public final long deviceAddress;

        public AccelerationStructureCreateInfoKHR(MemoryBuffer buffer, long size, AccelerationStructureTypeKHR type,
                AccelerationStructureCreateFlagBitsKHR... flagBits) {
            this.buffer = buffer;
            this.size = size;
            this.type = type;
            this.offset = 0;
            this.createFlags = BitFlags.getFlagsValue(flagBits);
            this.deviceAddress = 0;
        }

    }

    public static class DeviceOrHostAddress {

        private final MemoryBuffer buffer;
        public final long deviceAddress;
        public final long hostAddress;

        public DeviceOrHostAddress(MemoryBuffer buffer, long deviceAddress) {
            this.buffer = buffer;
            this.deviceAddress = deviceAddress;
            this.hostAddress = 0;
        }

    }

}
