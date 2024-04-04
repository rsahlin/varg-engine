
package org.varg.vulkan.pipeline;

import org.varg.vulkan.Vulkan10.CompareOp;
import org.varg.vulkan.structs.StencilOpState;

/**
 * Wrapper For VkPipelineDepthStencilStateCreateInfo
 *
 */
public class PipelineDepthStencilStateCreateInfo {

    // Reserved for future use - VkPipelineDepthStencilStateCreateFlags flags;
    public final boolean depthTestEnable;
    public final boolean depthWriteEnable;
    public final CompareOp depthCompareOp;
    public final boolean depthBoundsTestEnable;
    public final boolean stencilTestEnable;
    public final float minDepthBounds;
    public final float maxDepthBounds;
    StencilOpState front;
    StencilOpState back;

    public PipelineDepthStencilStateCreateInfo(CompareOp depthCompareOp) {
        this.depthCompareOp = depthCompareOp;
        this.depthTestEnable = true;
        this.depthWriteEnable = true;
        this.depthBoundsTestEnable = false;
        this.stencilTestEnable = false;
        this.minDepthBounds = 0;
        this.maxDepthBounds = 1.0f;

    }

    public PipelineDepthStencilStateCreateInfo(float minDepthBounds, float maxDepthBounds,
            CompareOp depthCompareOp, boolean depthBoundsTestEnable, boolean stencilTestEnable) {
        this.depthTestEnable = true;
        this.depthWriteEnable = true;
        this.minDepthBounds = minDepthBounds;
        this.maxDepthBounds = maxDepthBounds;
        this.depthCompareOp = depthCompareOp;
        this.depthBoundsTestEnable = depthBoundsTestEnable;
        this.stencilTestEnable = stencilTestEnable;
    }

}
