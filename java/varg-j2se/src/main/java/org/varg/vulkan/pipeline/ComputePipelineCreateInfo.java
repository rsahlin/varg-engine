
package org.varg.vulkan.pipeline;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.vulkan.Vulkan10.PipelineCreateFlagBit;

/**
 * Wrapper for VkComputePipelineCreateInfo
 *
 */
public class ComputePipelineCreateInfo extends PipelineCreateInfo {

    public ComputePipelineCreateInfo(PipelineCreateFlagBit[] flags, @NonNull PipelineShaderStageCreateInfo[] stages,
            PipelineLayout layout) {
        super(flags, stages, layout, -1, null);
    }

}
