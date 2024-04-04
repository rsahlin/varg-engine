
package org.varg.vulkan.pipeline;

import org.varg.vulkan.Vulkan10.PrimitiveTopology;

public class PipelineInputAssemblyStateCreateInfo {

    // Reserved for future use VkPipelineInputAssemblyStateCreateFlags flags;
    public final PrimitiveTopology topology;
    public final boolean primitiveRestartEnable;

    public PipelineInputAssemblyStateCreateInfo(PrimitiveTopology topology, boolean primitiveRestartEnable) {
        this.topology = topology;
        this.primitiveRestartEnable = primitiveRestartEnable;
    }

}
