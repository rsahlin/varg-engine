package org.varg.vulkan.pipeline;

import org.varg.vulkan.extensions.KHRRayTracingPipeline.RayTracingPipelineCreateInfoKHR;

/**
 * Needs extension VK_KHR_ray_tracing_pipeline
 */
public class RayTracingPipeline extends Pipeline {

    public RayTracingPipeline(long pipeline, RayTracingPipelineCreateInfoKHR createInfo) {
        super(pipeline, createInfo.getPipelineLayout());
    }

}
