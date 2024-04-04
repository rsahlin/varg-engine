package org.varg.vulkan.extensions;

import org.varg.vulkan.structs.HDRMetadata;

/**
 * VK_EXT_hdr_metadata extension
 *
 */
public abstract class EXTHDRMetadata {

    /**
     * Sets the HDRMetadata to the swapchain, this will call the EXTHdrMetadata.vkSetHdrMetadataEXT method.
     * 
     * @param swapChain
     * @param metadata
     */
    public abstract void setHDRMetadataEXT(KHRSwapchain<?> swapChain, HDRMetadata metadata);

}
