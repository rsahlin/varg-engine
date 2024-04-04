
package org.varg.vulkan.image;

import org.varg.vulkan.Vulkan10.ImageAspectFlagBit;

/**
 * Wrapper for VkImageSubresourceLayers
 *
 */
public class ImageSubresourceLayers {

    public final ImageAspectFlagBit[] aspectMask;
    public final int mipLevel;
    public final int baseArrayLayer;
    public final int layerCount;

    public ImageSubresourceLayers() {
        this.mipLevel = 0;
        this.baseArrayLayer = 0;
        this.layerCount = 1;
        this.aspectMask = new ImageAspectFlagBit[] { ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT };
    }

    public ImageSubresourceLayers(ImageAspectFlagBit[] aspectMask, int mipLevel, int baseArrayLayer, int layerCount) {
        this.aspectMask = aspectMask;
        this.mipLevel = mipLevel;
        this.baseArrayLayer = baseArrayLayer;
        this.layerCount = layerCount;
    }

    public ImageSubresourceLayers(ImageAspectFlagBit aspectMask, int mipLevel, int baseArrayLayer, int layerCount) {
        this.aspectMask = new ImageAspectFlagBit[] { aspectMask };
        this.mipLevel = mipLevel;
        this.baseArrayLayer = baseArrayLayer;
        this.layerCount = layerCount;
    }

    /**
     * Creates a subresourcelayer for {@link ImageAspectFlagBit#VK_IMAGE_ASPECT_COLOR_BIT}, baseArrayLayer = 0 and
     * 
     * @param mipLevel
     * @param arrayLayers
     */
    public ImageSubresourceLayers(int mipLevel, int arrayLayers) {
        this.mipLevel = mipLevel;
        this.baseArrayLayer = 0;
        this.layerCount = arrayLayers;
        this.aspectMask = new ImageAspectFlagBit[] { ImageAspectFlagBit.VK_IMAGE_ASPECT_COLOR_BIT };
    }

    /**
     * Returns the image aspect flagbits
     * 
     * @return
     */
    public ImageAspectFlagBit[] getAspectMask() {
        return aspectMask;
    }

    /**
     * Returns the number of mip levels
     * 
     * @return
     */
    public int getMipLevel() {
        return mipLevel;
    }

    /**
     * Returns the base array layer for this subresource
     * 
     * @return
     */
    public int getBaseArrayLayer() {
        return baseArrayLayer;
    }

    /**
     * Returns the number of layers in this subresource
     * 
     * @return
     */
    public int getLayerCount() {
        return layerCount;
    }

}
