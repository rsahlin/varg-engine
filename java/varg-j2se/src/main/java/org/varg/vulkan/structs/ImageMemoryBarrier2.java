package org.varg.vulkan.structs;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan13.AccessFlagBits2;
import org.varg.vulkan.Vulkan13.PipelineStateFlagBits2;
import org.varg.vulkan.image.Image;
import org.varg.vulkan.image.ImageSubresourceRange;

public class ImageMemoryBarrier2 {

    PipelineStateFlagBits2[] srcStageMask;
    AccessFlagBits2[] srcAccessMask;
    PipelineStateFlagBits2[] dstStageMask;
    AccessFlagBits2[] dstAccessMask;
    public final ImageLayout oldLayout;
    public final ImageLayout newLayout;
    public final int srcQueueFamilyIndex;
    public final int dstQueueFamilyIndex;
    Image image;
    ImageSubresourceRange subresourceRange;

    public ImageMemoryBarrier2(PipelineStateFlagBits2[] srcStageMask, AccessFlagBits2[] srcAccessMask,
            PipelineStateFlagBits2[] dstStageMask, AccessFlagBits2[] dstAccessMask, ImageLayout oldLayout,
            ImageLayout newLayout, int srcQueue, int dstQueue, Image image, ImageSubresourceRange subresourceRange) {

        this.srcStageMask = srcStageMask;
        this.srcAccessMask = srcAccessMask;
        this.dstStageMask = dstStageMask;
        this.dstAccessMask = dstAccessMask;
        this.oldLayout = oldLayout;
        this.newLayout = newLayout;
        this.srcQueueFamilyIndex = srcQueue;
        this.dstQueueFamilyIndex = dstQueue;
        this.image = image;
        this.subresourceRange = subresourceRange;
    }

    /**
     * Returns the source stage mask
     * 
     * @return
     */
    public long getSrcStageMask() {
        return BitFlags.getFlagsLongValue(srcStageMask);
    }

    /**
     * Returns the sestination stage mask
     * 
     * @return
     */
    public long getDestStageMask() {
        return BitFlags.getFlagsLongValue(dstStageMask);
    }

    /**
     * Returns the src access mask
     * 
     * @return
     */
    public long getSrcAccessMask() {
        return BitFlags.getFlagsLongValue(srcAccessMask);
    }

    /**
     * Returns the dst access mask
     * 
     * @return
     */
    public long getDstAccessMask() {
        return BitFlags.getFlagsLongValue(dstAccessMask);
    }

}
