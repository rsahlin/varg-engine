
package org.varg.vulkan.pipeline;

import org.varg.vulkan.Vulkan10.LogicOp;

public class PipelineColorBlendStateCreateInfo {

    // Reserved for future use - VkPipelineColorBlendStateCreateFlags flags;
    boolean logicOpEnable;
    LogicOp logicOp;
    public final PipelineColorBlendAttachmentState[] pAttachments;
    float[] blendConstants;

    public PipelineColorBlendStateCreateInfo(boolean blendEnable) {
        pAttachments = new PipelineColorBlendAttachmentState[] { new PipelineColorBlendAttachmentState(blendEnable) };
    }

}
