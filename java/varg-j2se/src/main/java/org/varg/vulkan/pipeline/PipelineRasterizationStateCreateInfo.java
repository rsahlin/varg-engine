
package org.varg.vulkan.pipeline;

import org.varg.vulkan.Vulkan10.CullModeFlagBit;
import org.varg.vulkan.Vulkan10.FrontFace;
import org.varg.vulkan.Vulkan10.PolygonMode;

/**
 * Wrapper for VkPipelineRasterizationStateCreateInfo
 *
 */
public class PipelineRasterizationStateCreateInfo {

    // Reserved for future use - VkPipelineRasterizationStateCreateFlags flags;
    boolean depthClampEnable;
    boolean rasterizerDiscardEnable;
    public final PolygonMode polygonMode;
    public final CullModeFlagBit cullMode;
    public final FrontFace frontFace;
    boolean depthBiasEnable;
    float depthBiasConstantFactor;
    float depthBiasClamp;
    float depthBiasSlopeFactor;
    public final float lineWidth;

    public PipelineRasterizationStateCreateInfo(PolygonMode polygonMode, CullModeFlagBit cullMode, FrontFace frontFace,
            float lineWidht) {
        this.polygonMode = polygonMode;
        this.cullMode = cullMode;
        this.frontFace = frontFace;
        this.lineWidth = lineWidht;
    }

}
