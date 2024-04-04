
package org.varg.vulkan.structs;

/**
 * Wrapper for VkSampler
 *
 */
public class Sampler extends Handle<SamplerCreateInfo> {

    public Sampler(long sampler, SamplerCreateInfo samplerInfo) {
        super(sampler, samplerInfo);
    }

}
