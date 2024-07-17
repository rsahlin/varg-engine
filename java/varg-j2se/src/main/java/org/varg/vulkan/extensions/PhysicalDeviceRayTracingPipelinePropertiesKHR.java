package org.varg.vulkan.extensions;

import org.gltfio.lib.AllowPublic;
import org.varg.vulkan.structs.PlatformStruct;

/**
 * Wrapper for PhysicalDeviceRayTracingPipelinePropertiesKHR
 */
public class PhysicalDeviceRayTracingPipelinePropertiesKHR extends PlatformStruct {

    public PhysicalDeviceRayTracingPipelinePropertiesKHR(Object properties) {
        copyFieldsFromStruct(properties, PhysicalDeviceRayTracingPipelinePropertiesKHR.class);
    }

    @AllowPublic
    public int shaderGroupHandleSize;
    @AllowPublic
    public int maxRayRecursionDepth;
    @AllowPublic
    public int maxShaderGroupStride;
    @AllowPublic
    public int shaderGroupBaseAlignment;
    @AllowPublic
    public int shaderGroupHandleCaptureReplaySize;
    @AllowPublic
    public int maxRayDispatchInvocationCount;
    @AllowPublic
    public int shaderGroupHandleAlignment;
    @AllowPublic
    public int maxRayHitAttributeSize;

}
