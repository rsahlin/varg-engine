
package org.varg.vulkan.cmd;

import org.varg.vulkan.Vulkan10.QueryControlFlagBits;
import org.varg.vulkan.Vulkan10.QueryPipelineStatisticFlagBits;
import org.varg.vulkan.framebuffer.FrameBuffer;
import org.varg.vulkan.renderpass.RenderPass;

public class CommandBufferInheritanceInfo {

    final RenderPass renderPass;
    final int subpass;
    final FrameBuffer framebuffer;
    final boolean occlusionQueryEnable;
    final QueryControlFlagBits[] queryFlags;
    final QueryPipelineStatisticFlagBits[] pipelineStatistics;

    public CommandBufferInheritanceInfo(RenderPass renderPass, int subpass, FrameBuffer frameBuffer,
            boolean occlusionQueryEnable,
            QueryControlFlagBits[] queryFlags, QueryPipelineStatisticFlagBits[] pipelineStatistics) {
        this.renderPass = renderPass;
        this.subpass = subpass;
        this.framebuffer = frameBuffer;
        this.occlusionQueryEnable = occlusionQueryEnable;
        this.queryFlags = queryFlags;
        this.pipelineStatistics = pipelineStatistics;

    }

}
