package org.varg.vulkan.extensions;

import org.gltfio.lib.AllowPublic;
import org.varg.vulkan.structs.PlatformStruct;

/**
 * The PhysicalDeviceRayTracingPipelineFeaturesKHR struct
 */
public class PhysicalDeviceRayTracingPipelineFeaturesKHR extends PlatformStruct {

    @AllowPublic
    public boolean rayTracingPipeline;
    @AllowPublic
    public boolean rayTracingPipelineShaderGroupHandleCaptureReplay;
    @AllowPublic
    public boolean rayTracingPipelineShaderGroupHandleCaptureReplayMixed;
    @AllowPublic
    public boolean rayTracingPipelineTraceRaysIndirect;
    @AllowPublic
    public boolean rayTraversalPrimitiveCulling;

    public PhysicalDeviceRayTracingPipelineFeaturesKHR(boolean rayTracingPipeline, boolean rayTracingPipelineShaderGroupHandleCaptureReplay, boolean rayTracingPipelineShaderGroupHandleCaptureReplayMixed,
            boolean rayTracingPipelineTraceRaysIndirect, boolean rayTraversalPrimitiveCulling) {
        this.rayTracingPipeline = rayTracingPipeline;
        this.rayTracingPipelineShaderGroupHandleCaptureReplay = rayTracingPipelineShaderGroupHandleCaptureReplay;
        this.rayTracingPipelineShaderGroupHandleCaptureReplayMixed = rayTracingPipelineShaderGroupHandleCaptureReplayMixed;
        this.rayTracingPipelineTraceRaysIndirect = rayTracingPipelineTraceRaysIndirect;
        this.rayTraversalPrimitiveCulling = rayTraversalPrimitiveCulling;
    }

    public PhysicalDeviceRayTracingPipelineFeaturesKHR(Object features) {
        super.copyBooleanFieldsFromStruct(features, PhysicalDeviceRayTracingPipelineFeaturesKHR.class);
    }

}
