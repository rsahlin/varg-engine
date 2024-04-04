
package org.varg.vulkan.image;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;

public class ImageSubresourceRange {

    public final ImageAspectFlagBit[] aspectMask;
    public final int baseMipLevel;
    public final int levelCount;
    public final int baseArrayLayer;
    public final int layerCount;

    public ImageSubresourceRange(ImageSubresourceLayers subresource) {
        aspectMask = subresource.aspectMask.clone();
        baseMipLevel = subresource.mipLevel;
        levelCount = 1;
        baseArrayLayer = subresource.baseArrayLayer;
        layerCount = subresource.layerCount;
    }

    /**
     * Creates a default image subresource range with aspect color bit,
     * baseMipLevel 0
     * levelCount 1
     * baseArrayLayer 0
     * layerCount 1
     */
    public ImageSubresourceRange() {
        aspectMask = new ImageAspectFlagBit[] { ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT };
        baseMipLevel = 0;
        levelCount = 1;
        baseArrayLayer = 0;
        layerCount = 1;
    }

    public ImageSubresourceRange(ImageAspectFlagBit[] aspectMask, int baseMipLevel, int levelCount, int baseArrayLayer,
            int layerCount) {
        this.aspectMask = aspectMask;
        this.baseMipLevel = baseMipLevel;
        this.levelCount = levelCount;
        this.baseArrayLayer = baseArrayLayer;
        this.layerCount = layerCount;
    }

    public ImageSubresourceRange(ImageAspectFlagBit aspectMask, int baseMipLevel, int levelCount, int baseArrayLayer,
            int layerCount) {
        this.aspectMask = new ImageAspectFlagBit[] { aspectMask };
        this.baseMipLevel = baseMipLevel;
        this.levelCount = levelCount;
        this.baseArrayLayer = baseArrayLayer;
        this.layerCount = layerCount;
    }

    /**
     * Returns the aspect mask
     * 
     * @return
     */
    public int getAspectMaskValue() {
        return BitFlags.getFlagsValue(aspectMask);
    }

}
