package org.varg.vulkan.extensions;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import org.gltfio.lib.AllowPublic;
import org.gltfio.lib.BitFlag;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.uniform.BindBuffer;
import org.varg.vulkan.IndirectDrawCalls;
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
    public abstract void cmdBuildAccelerationStructuresKHR(Q queue, AccelerationStructureBuildGeometryInfoKHR[] info, AccelerationStructureBuildRangeInfoKHR[] buildRangeInfos);

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
    public abstract AccelerationStructureKHR createAccelerationStructureKHR(AccelerationStructureCreateInfoKHR createInfo);

    /**
     * Retrieve the required size for an acceleration structure
     * Provided by VK_KHR_acceleration_structure
     * 
     * @param buildInfo
     * @param maxPrimitiveCounts
     * @param type The geometry type to use
     * @return
     */
    public abstract AccelerationStructureBuildSizesInfoKHR getAccelerationStructureBuildSizesKHR(AccelerationStructureBuildTypeKHR buildType, AccelerationStructureBuildGeometryInfoKHR buildInfo, int maxPrimitiveCount, GeometryTypeKHR type);

    public enum BufferUsageFlagBit implements BitFlag {
        VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_BUILD_INPUT_READ_ONLY_BIT_KHR(0x80000),
        VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_STORAGE_BIT_KHR(0x100000);

        public final int value;

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

        public final int value;

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

        public final int value;

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

        public final int value;

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

        public final int value;

        GeometryTypeKHR(int value) {
            this.value = value;
        }

    }

    public enum BuildAccelerationStructureModeKHR {
        VK_BUILD_ACCELERATION_STRUCTURE_MODE_BUILD_KHR(0),
        VK_BUILD_ACCELERATION_STRUCTURE_MODE_UPDATE_KHR(1);

        public final int value;

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

        public final int value;

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

        public final int value;

        AccelerationStructureTypeKHR(int value) {
            this.value = value;
        }
    }

    public enum GeometryInstanceFlagBitsKHR implements BitFlag {

        VK_GEOMETRY_INSTANCE_TRIANGLE_FACING_CULL_DISABLE_BIT_KHR(0x00000001),
        VK_GEOMETRY_INSTANCE_TRIANGLE_FLIP_FACING_BIT_KHR(0x00000002),
        VK_GEOMETRY_INSTANCE_FORCE_OPAQUE_BIT_KHR(0x00000004),
        VK_GEOMETRY_INSTANCE_FORCE_NO_OPAQUE_BIT_KHR(0x000000089),
        // Provided by VK_EXT_opacity_micromap
        VK_GEOMETRY_INSTANCE_FORCE_OPACITY_MICROMAP_2_STATE_EXT(0x00000010),
        // Provided by VK_EXT_opacity_micromap
        VK_GEOMETRY_INSTANCE_DISABLE_OPACITY_MICROMAPS_EXT(0x00000020),
        VK_GEOMETRY_INSTANCE_TRIANGLE_FRONT_COUNTERCLOCKWISE_BIT_KHR(VK_GEOMETRY_INSTANCE_TRIANGLE_FLIP_FACING_BIT_KHR.value),
        // Provided by VK_NV_ray_tracing
        VK_GEOMETRY_INSTANCE_TRIANGLE_CULL_DISABLE_BIT_NV(VK_GEOMETRY_INSTANCE_TRIANGLE_FACING_CULL_DISABLE_BIT_KHR.value),
        // Provided by VK_NV_ray_tracing
        VK_GEOMETRY_INSTANCE_TRIANGLE_FRONT_COUNTERCLOCKWISE_BIT_NV(VK_GEOMETRY_INSTANCE_TRIANGLE_FRONT_COUNTERCLOCKWISE_BIT_KHR.value),
        // Provided by VK_NV_ray_tracing
        VK_GEOMETRY_INSTANCE_FORCE_OPAQUE_BIT_NV(VK_GEOMETRY_INSTANCE_FORCE_OPAQUE_BIT_KHR.value),
        // Provided by VK_NV_ray_tracing
        VK_GEOMETRY_INSTANCE_FORCE_NO_OPAQUE_BIT_NV(VK_GEOMETRY_INSTANCE_FORCE_NO_OPAQUE_BIT_KHR.value);

        GeometryInstanceFlagBitsKHR(int value) {
            this.value = value;
        }

        public final int value;

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    public abstract static class AccelerationStructureGeometryDataKHR {

        public final int stride;
        public final DeviceOrHostAddress data;
        public final GeometryTypeKHR geometryType;

        protected AccelerationStructureGeometryDataKHR(DeviceOrHostAddress data, int stride, GeometryTypeKHR geometryType) {
            this.data = data;
            this.stride = stride;
            this.geometryType = geometryType;
        }

        public abstract int getMaxPrimitiveCount(int index);

    }

    public static class AccelerationStructureGeometryTrianglesDataKHR extends AccelerationStructureGeometryDataKHR {
        public final Vulkan10.Format vertexFormat;
        public final int vertexStride;
        public final int maxVertex;
        public final Vulkan10.IndexType indexType;
        public final DeviceOrHostAddress indexData;
        public final DeviceOrHostAddress transformData;
        private final int[] indirectCommands;

        /**
         * Use this for arrayed indirectdrawcalls
         * 
         * @param vertexFormat
         * @param vertexData
         * @param vertexStride
         * @param maxVertex
         */
        public AccelerationStructureGeometryTrianglesDataKHR(Vulkan10.Format vertexFormat, DeviceOrHostAddress vertexData, int vertexStride, int maxVertex, int[] indirectCommands) {
            super(vertexData, vertexFormat.sizeInBytes, GeometryTypeKHR.VK_GEOMETRY_TYPE_TRIANGLES_KHR);
            this.vertexFormat = vertexFormat;
            this.vertexStride = vertexStride;
            this.maxVertex = maxVertex;
            this.indexType = null;
            this.indexData = null;
            this.transformData = null;
            this.indirectCommands = indirectCommands;
        }

        /**
         * Use this for indexed indirectdrawcalls
         * 
         * @param vertexFormat
         * @param vertexData
         * @param vertexStride
         * @param maxVertex
         * @param indexType
         * @param indexData
         */
        public AccelerationStructureGeometryTrianglesDataKHR(Vulkan10.Format vertexFormat, DeviceOrHostAddress vertexData, int vertexStride, int maxVertex, Vulkan10.IndexType indexType, DeviceOrHostAddress indexData,
                int[] indirectCommands) {
            super(vertexData, vertexFormat.sizeInBytes, GeometryTypeKHR.VK_GEOMETRY_TYPE_TRIANGLES_KHR);
            this.vertexFormat = vertexFormat;
            this.vertexStride = vertexStride;
            this.maxVertex = maxVertex;
            this.indexType = indexType;
            this.indexData = indexData;
            this.transformData = null;
            this.indirectCommands = indirectCommands;
        }

        public int getDrawCallCount() {
            return indexType != null ? indirectCommands.length / IndirectDrawCalls.DRAW_INDEXED_INDIRECT_COMMAND_SIZE : indirectCommands.length / IndirectDrawCalls.DRAW_INDIRECT_COMMAND_SIZE;
        }

        public AccelerationStructureBuildRangeInfoKHR createBuildRangeInfo(int primitiveIndex) {
            AccelerationStructureBuildRangeInfoKHR result = null;
            int cmdIndex = getCmdIndex(primitiveIndex);
            if (indexType != null) {
                /**
                 * INDEXED:
                 * indexCount
                 * instanceCount
                 * firstIndex
                 * vertexOffset
                 * firstInstance
                 */
                result = new AccelerationStructureBuildRangeInfoKHR(primitiveIndex, indirectCommands[cmdIndex] / 3, indirectCommands[cmdIndex + 2] / indexType.sizeInBytes(), indirectCommands[cmdIndex + 3], 0);

            } else {
                /**
                 * vertexCount,
                 * instanceCount,
                 * firstVertex,
                 * firstInstance
                 */
                result = new AccelerationStructureBuildRangeInfoKHR(primitiveIndex, indirectCommands[cmdIndex], indirectCommands[cmdIndex + 2]);
            }
            return result;
        }

        private int getCmdIndex(int index) {
            return indexType != null ? index * IndirectDrawCalls.DRAW_INDEXED_INDIRECT_COMMAND_SIZE : index * IndirectDrawCalls.DRAW_INDIRECT_COMMAND_SIZE;
        }

        @Override
        public int getMaxPrimitiveCount(int index) {
            return indirectCommands[getCmdIndex(index)] / 3;
        }

    }

    public static class TransformMatrixKHR {
        float[][] matrix = new float[3][4];
    }

    public static class AccelerationStructureGeometryAabbsDataKHR extends AccelerationStructureGeometryDataKHR {

        public static final int AABB_SIZE_IN_BYTES = 6 * 4;

        public AccelerationStructureGeometryAabbsDataKHR(DeviceOrHostAddress data, int primitiveCount) {
            super(data, AABB_SIZE_IN_BYTES, GeometryTypeKHR.VK_GEOMETRY_TYPE_AABBS_KHR);
        }

        @Override
        public int getMaxPrimitiveCount(int index) {
            return 1;
        }

    }

    public static class AccelerationStructureGeometryInstancesDataKHR extends AccelerationStructureGeometryDataKHR {

        AccelerationStructureInstanceKHR[] instances;

        public AccelerationStructureGeometryInstancesDataKHR(DeviceOrHostAddress data, AccelerationStructureInstanceKHR[] instances) {
            super(data, AccelerationStructureInstanceKHR.INSTANCE_SIZE_IN_BYTES, GeometryTypeKHR.VK_GEOMETRY_TYPE_INSTANCES_KHR);
            this.instances = instances;
        }

        @Override
        public int getMaxPrimitiveCount(int index) {
            return 1;
        }

    }

    public static class AccelerationStructureInstanceKHR {
        /**
         * VkTransformMatrixKHR transform matrix is a 3x4 row-major affine transformation matrix.
         * uint32 instanceCustomIndex:24 and mask:8;
         * uint32 instanceShaderBindingTableRecordOffset:24; and flags:8;
         * uint64_t accelerationStructureReference;
         * 
         */

        public static final int CUSTOMINDEX_AND_MASK_BYTE_OFFSET = 48;
        public static final int SBT_HIT_OFFSET_AND_FLAGS_BYTE_OFFSET = 52;
        public static final int AS_REFERENCE_BYTE_OFFSET = 7 * 8;
        public static final int INSTANCE_SIZE_IN_BYTES = (3 * 4 + 1 + 1) * Integer.BYTES + Long.BYTES;

        public final int primitiveId;

        public AccelerationStructureInstanceKHR(AccelerationStructureBuildRangeInfoKHR rangeInfo) {
            this.primitiveId = rangeInfo.primitiveID;
        }
    }

    public static class AccelerationStructureGeometryKHR {
        public final GeometryTypeKHR geometryType;
        public final AccelerationStructureGeometryDataKHR geometry;

        public AccelerationStructureGeometryKHR(GeometryTypeKHR geometryType, AccelerationStructureGeometryDataKHR geometry) {
            this.geometryType = geometryType;
            this.geometry = geometry;
        }

        public static AccelerationStructureGeometryDataKHR[] getGeometries(AccelerationStructureGeometryDataKHR[] geometries, GeometryTypeKHR type) {
            ArrayList<AccelerationStructureGeometryDataKHR> list = new ArrayList<AccelerationStructureGeometryDataKHR>();
            for (AccelerationStructureGeometryDataKHR geometry : geometries) {
                if (geometry.geometryType == type) {
                    list.add(geometry);
                }
            }
            return list.toArray(new AccelerationStructureGeometryDataKHR[0]);
        }
    }

    public static class AccelerationStructureBuildGeometryInfoKHR extends PlatformStruct {
        public final AccelerationStructureTypeKHR type;
        public final int flags;
        public final BuildAccelerationStructureModeKHR mode;
        /**
         * srcAccelerationStructure is a pointer to an existing acceleration structure that is to be used to update the
         * dst acceleration structure when mode is VK_BUILD_ACCELERATION_STRUCTURE_MODE_UPDATE_KHR
         */
        @AllowPublic
        public AccelerationStructureKHR srcAccelerationStructure;
        @AllowPublic
        public AccelerationStructureKHR dstAccelerationStructure;
        /**
         * If type is VK_ACCELERATION_STRUCTURE_TYPE_BOTTOM_LEVEL_KHR then geometryType of geometries MUST be the same.
         */
        public final AccelerationStructureGeometryDataKHR geometries;
        @AllowPublic
        public long scratchData;

        public AccelerationStructureBuildGeometryInfoKHR(AccelerationStructureTypeKHR type, AccelerationStructureGeometryDataKHR geometries, BuildAccelerationStructureFlagBitsKHR... flags) {
            this.type = type;
            this.flags = BitFlags.getFlagsValue(flags);
            this.mode = BuildAccelerationStructureModeKHR.VK_BUILD_ACCELERATION_STRUCTURE_MODE_BUILD_KHR;
            this.geometries = geometries;
        }

        /**
         * 
         * @param type
         * @param geometries
         * @param dstAccelerationStructure
         * @param scratchData
         * @param flags
         */
        public AccelerationStructureBuildGeometryInfoKHR(AccelerationStructureTypeKHR type, AccelerationStructureGeometryDataKHR geometries, AccelerationStructureKHR dstAccelerationStructure, long scratchData,
                BuildAccelerationStructureFlagBitsKHR... flags) {
            if (dstAccelerationStructure == null || scratchData == 0) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null value");
            }
            this.type = type;
            this.dstAccelerationStructure = dstAccelerationStructure;
            this.scratchData = scratchData;
            this.flags = BitFlags.getFlagsValue(flags);
            this.mode = BuildAccelerationStructureModeKHR.VK_BUILD_ACCELERATION_STRUCTURE_MODE_BUILD_KHR;
            this.geometries = geometries;
        }

        /**
         * Sets the AS data
         * 
         * @param srcAccelerationStruct
         * @param srcScratchData
         */
        public void setAccelerationStruct(AccelerationStructureKHR srcAccelerationStruct, long srcScratchData) {
            if (this.scratchData != 0 || this.dstAccelerationStructure != null) {
                throw new IllegalArgumentException();
            }
            this.scratchData = srcScratchData;
            this.dstAccelerationStructure = srcAccelerationStruct;
        }

    }

    public static class AccelerationStructureBuildRangeInfoKHR extends PlatformStruct {

        public final int primitiveID;
        public final int primitiveCount;
        public final int primitiveOffset;
        public final int firstVertex;
        public final int transformOffset;

        /**
         * Use for as instance
         * 
         * @param primitiveCount
         * @param primitiveOffset
         */
        public AccelerationStructureBuildRangeInfoKHR(int primitiveCount, int primitiveOffset) {
            primitiveID = -1;
            this.primitiveCount = primitiveCount;
            this.primitiveOffset = primitiveOffset;
            firstVertex = 0;
            transformOffset = 0;
        }

        public AccelerationStructureBuildRangeInfoKHR(int primitiveId, int primitiveCount, int primitiveOffset) {
            this.primitiveID = primitiveId;
            this.primitiveCount = primitiveCount;
            this.primitiveOffset = primitiveOffset;
            this.firstVertex = 0;
            this.transformOffset = 0;
        }

        public AccelerationStructureBuildRangeInfoKHR(int primitiveId, int primitiveCount, int primitiveOffset, int firstVertex, int transformOffset) {
            this.primitiveID = primitiveId;
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

        public AccelerationStructureBuildSizesInfoKHR(long accelerationStructureSize, long updateScratchSize, long buildScratchSize) {
            this.accelerationStructureSize = accelerationStructureSize;
            this.updateScratchSize = updateScratchSize;
            this.buildScratchSize = buildScratchSize;
        }

    }

    public static class AccelerationStructureKHR extends Handle<AccelerationStructureCreateInfoKHR> {

        public final long asReference;

        public AccelerationStructureKHR(long adress, long asReference, AccelerationStructureCreateInfoKHR createInfo) {
            super(adress, createInfo);
            this.asReference = asReference;
        }

    }

    public static class AccelerationStructureCreateInfoKHR {
        public final int createFlags;
        public final MemoryBuffer buffer;
        /**
         * Must be multiple of 256
         */
        public final long offset;
        public final long size;
        public final AccelerationStructureTypeKHR type;
        /**
         * deviceAddress is the device address requested for the acceleration structure if the
         * accelerationStructureCaptureReplay feature is being used. If deviceAddress is zero, no specific address
         * is
         * requested
         */
        public final long deviceAddress;

        public AccelerationStructureCreateInfoKHR(MemoryBuffer buffer, long size, long offset, AccelerationStructureTypeKHR type, AccelerationStructureCreateFlagBitsKHR... flagBits) {
            this.buffer = buffer;
            this.size = size;
            this.type = type;
            this.offset = offset;
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

        public MemoryBuffer getMemoryBuffer() {
            return buffer;
        }

    }

    /**
     * Creates a bindbuffer for accelerationstructure handles, this is used in descriptorset updates
     * 
     * @param toplevelHandles
     * @return
     */
    public static BindBuffer createDescriptorHandleBuffer(AccelerationStructureKHR... toplevelHandles) {
        ByteBuffer bb = Buffers.createByteBuffer(toplevelHandles.length * Long.BYTES);
        LongBuffer handles = bb.asLongBuffer();
        for (AccelerationStructureKHR as : toplevelHandles) {
            handles.put(as.getHandle());
        }
        BindBuffer asHandle = new BindBuffer(bb);
        return asHandle;
    }

}
