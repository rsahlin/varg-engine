
package org.varg.vulkan.pipeline;

import org.varg.vulkan.structs.Rect2D;
import org.varg.vulkan.structs.Viewport;

/**
 * Wrapper for VkPipelineViewportStateCreateInfo
 * 
 */
public class PipelineViewportStateCreateInfo {

    // Reserved for future use - VkPipelineViewportStateCreateFlags flags;
    public final Viewport[] viewports;
    public final Rect2D pScissors;

    public PipelineViewportStateCreateInfo(Viewport[] viewports, Rect2D pScissors) {
        this.viewports = viewports;
        this.pScissors = pScissors;
    }

}
