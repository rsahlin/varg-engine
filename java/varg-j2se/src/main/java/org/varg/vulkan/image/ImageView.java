
package org.varg.vulkan.image;

import java.nio.LongBuffer;

import org.gltfio.gltf2.JSONTexture.ComponentMapping;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.ImageViewType;

/**
 * Wrapper for VkImageView
 *
 */
public class ImageView {

    public final Image image;
    public final ImageViewType type;
    public final ComponentMapping components;
    public final ImageSubresourceRange subresourceRange;

    private final LongBuffer viewBuffer = Buffers.createLongBuffer(1);

    public ImageView(ImageViewCreateInfo createInfo, long view) {
        if (view == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.image = createInfo.image;
        this.type = createInfo.type;
        this.components = createInfo.components;
        this.subresourceRange = createInfo.subresourceRange;
        viewBuffer.put(view);
    }

    /**
     * Returns the native pointer to the imageview
     * 
     * @return
     */
    public long getImageView() {
        return viewBuffer.get(0);
    }

    /**
     * Returns the image
     * 
     * @return
     */
    public Image getImage() {
        return image;
    }

}
