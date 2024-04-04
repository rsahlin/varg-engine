
package org.varg.vulkan.image;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan10.AccessFlagBit;
import org.varg.vulkan.Vulkan10.ImageLayout;

public class ImageMemoryBarrier {

    public final AccessFlagBit[] srcAccessMask;
    public final AccessFlagBit[] dstAccessMask;
    public final ImageLayout oldLayout;
    public final ImageLayout newLayout;
    public final int srcQueueFamilyIndex;
    public final int dstQueueFamilyIndex;
    public final Image image;
    public final ImageSubresourceRange subresourceRange;

    public ImageMemoryBarrier(AccessFlagBit[] srcAccessMask, AccessFlagBit[] dstAccessMask,
            ImageLayout oldLayout, ImageLayout newLayout, int srcQueueFamilyIndex, int dstQueueFamilyIndex,
            Image image, ImageSubresourceRange subresourceRange) {
        this.srcAccessMask = srcAccessMask;
        this.dstAccessMask = dstAccessMask;
        this.oldLayout = oldLayout;
        this.newLayout = newLayout;
        this.srcQueueFamilyIndex = srcQueueFamilyIndex;
        this.dstQueueFamilyIndex = dstQueueFamilyIndex;
        this.image = image;
        this.subresourceRange = subresourceRange;
    }

    /**
     * Returns the src access mask
     * 
     * @return
     */
    public int getSrcAccessMaskValue() {
        return BitFlags.getFlagsValue(srcAccessMask);
    }

    /**
     * Returns the dst access mask
     * 
     * @return
     */
    public int getDstAccessMaskValue() {
        return BitFlags.getFlagsValue(dstAccessMask);
    }

    /**
     * Call this after the barrier command has been issued to update the layout in the image
     */
    public void updateLayout() {
        image.updateLayout(newLayout, subresourceRange);
    }

}
