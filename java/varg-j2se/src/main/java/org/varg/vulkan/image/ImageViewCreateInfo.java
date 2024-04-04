
package org.varg.vulkan.image;

import org.gltfio.gltf2.JSONTexture.ComponentMapping;
import org.varg.vulkan.Vulkan10.ImageViewType;

/**
 * Wrapper for VkImageViewCreateInfo
 *
 */
public class ImageViewCreateInfo {

    public final ImageViewType type;
    public final int formatValue;
    public final ComponentMapping components;
    public final ImageSubresourceRange subresourceRange;
    public final Image image;

    public ImageViewCreateInfo(Image image, ImageViewType type, int formatValue, ComponentMapping components,
            ImageSubresourceRange subresourceRange) {
        this.image = image;
        this.type = type;
        this.formatValue = formatValue;
        this.components = components;
        this.subresourceRange = subresourceRange;
    }

}
