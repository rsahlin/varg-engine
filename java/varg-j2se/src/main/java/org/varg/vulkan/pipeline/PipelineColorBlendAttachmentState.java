
package org.varg.vulkan.pipeline;

import org.varg.vulkan.Vulkan10.BlendFactor;
import org.varg.vulkan.Vulkan10.BlendOp;
import org.varg.vulkan.Vulkan10.ColorComponentFlagBit;

public class PipelineColorBlendAttachmentState {

    public final boolean blendEnable;
    public final BlendFactor srcColorBlendFactor;
    public final BlendFactor dstColorBlendFactor;
    public final BlendOp colorBlendOp;
    public final BlendFactor srcAlphaBlendFactor;
    public final BlendFactor dstAlphaBlendFactor;
    public final BlendOp alphaBlendOp;
    /**
     * Mask for ColorComponentFlagBits
     */
    public final int colorWriteMask;

    public PipelineColorBlendAttachmentState(boolean blendEnable) {
        colorWriteMask = ColorComponentFlagBit.VK_COLOR_COMPONENT_R_BIT.value |
                ColorComponentFlagBit.VK_COLOR_COMPONENT_G_BIT.value |
                ColorComponentFlagBit.VK_COLOR_COMPONENT_B_BIT.value |
                ColorComponentFlagBit.VK_COLOR_COMPONENT_A_BIT.value;
        this.blendEnable = blendEnable;
        srcColorBlendFactor = BlendFactor.VK_BLEND_FACTOR_ONE;
        dstColorBlendFactor = BlendFactor.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
        colorBlendOp = BlendOp.VK_BLEND_OP_ADD;
        srcAlphaBlendFactor = BlendFactor.VK_BLEND_FACTOR_SRC_ALPHA;
        dstAlphaBlendFactor = BlendFactor.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
        alphaBlendOp = BlendOp.VK_BLEND_OP_MAX;
    }

}
