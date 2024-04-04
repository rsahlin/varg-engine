
package org.varg.vulkan.pipeline;

import org.varg.vulkan.Vulkan10.SampleCountFlagBit;

/**
 * Wrapper for VkPipelineMultisampleStateCreateInfo
 *
 */
public class PipelineMultisampleStateCreateInfo {
    // Reserved for future use - VkPipelineMultisampleStateCreateFlags flags;
    public final SampleCountFlagBit rasterizationSamples;
    public final boolean sampleShadingEnable;
    public final float minSampleShading;
    public final int[] sampleMask;
    public final boolean alphaToCoverageEnable;
    public final boolean alphaToOneEnable;

    public PipelineMultisampleStateCreateInfo(SampleCountFlagBit samples, boolean sampleShading, float minSamples) {
        rasterizationSamples = samples;
        sampleShadingEnable = sampleShading;
        minSampleShading = minSamples;
        sampleMask = null;
        alphaToCoverageEnable = false;
        alphaToOneEnable = false;
    }

    @Override
    public String toString() {
        return "Samples " + rasterizationSamples + ", sampleShadingEnable : " + sampleShadingEnable
                + ", minSampleShading : " + minSampleShading;
    }

}
