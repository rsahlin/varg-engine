package org.varg.vulkan.extensions;

import org.gltfio.lib.AllowPublic;
import org.varg.vulkan.structs.PlatformStruct;

/**
 * Implementation of VkPhysicalDeviceMeshShaderPropertiesEXT
 *
 */
public class PhysicalDeviceMeshShaderPropertiesEXT extends PlatformStruct {

    public PhysicalDeviceMeshShaderPropertiesEXT(Object properties) {
        copyFieldsFromStruct(properties, PhysicalDeviceMeshShaderPropertiesEXT.class);
        // TODO - remove when compiler uses properties
        maxPreferredTaskWorkGroupInvocations = 128;
    }

    @AllowPublic
    public int maxTaskWorkGroupTotalCount;
    @AllowPublic
    public final int[] maxTaskWorkGroupCount = new int[3];
    @AllowPublic
    public int maxTaskWorkGroupInvocations;
    @AllowPublic
    public final int[] maxTaskWorkGroupSize = new int[3];
    @AllowPublic
    public int maxTaskPayloadSize;
    @AllowPublic
    public int maxTaskSharedMemorySize;
    @AllowPublic
    public int maxTaskPayloadAndSharedMemorySize;
    @AllowPublic
    public int maxMeshWorkGroupTotalCount;
    @AllowPublic
    public final int[] maxMeshWorkGroupCount = new int[3];
    @AllowPublic
    public int maxMeshWorkGroupInvocations;
    @AllowPublic
    public final int[] maxMeshWorkGroupSize = new int[3];
    @AllowPublic
    public int maxMeshSharedMemorySize;
    @AllowPublic
    public int maxMeshPayloadAndSharedMemorySize;
    @AllowPublic
    public int maxMeshOutputMemorySize;
    @AllowPublic
    public int maxMeshPayloadAndOutputMemorySize;
    @AllowPublic
    public int maxMeshOutputComponents;
    @AllowPublic
    public int maxMeshOutputVertices;
    @AllowPublic
    public int maxMeshOutputPrimitives;
    @AllowPublic
    public int maxMeshOutputLayers;
    @AllowPublic
    public int maxMeshMultiviewViewCount;
    @AllowPublic
    public int meshOutputPerVertexGranularity;
    @AllowPublic
    public int meshOutputPerPrimitiveGranularity;
    @AllowPublic
    public int maxPreferredTaskWorkGroupInvocations;
    @AllowPublic
    public int maxPreferredMeshWorkGroupInvocations;
    @AllowPublic
    public boolean prefersLocalInvocationVertexOutput;
    @AllowPublic
    public boolean prefersLocalInvocationPrimitiveOutput;
    @AllowPublic
    public boolean prefersCompactVertexOutput;
    @AllowPublic
    public boolean prefersCompactPrimitiveOutput;

}
