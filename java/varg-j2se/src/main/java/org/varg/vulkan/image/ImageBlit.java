
package org.varg.vulkan.image;

import org.varg.vulkan.structs.Offset3D;

/**
 * Wrapper for VkImageBlit
 *
 */
public class ImageBlit {

    public final ImageSubresourceLayers srcSubresource;
    public final Offset3D[] srcOffsets;
    public final ImageSubresourceLayers dstSubresource;
    public final Offset3D[] dstOffsets;

    public ImageBlit(ImageSubresourceLayers srcSubresource, Offset3D[] srcOffsets,
            ImageSubresourceLayers dstSubresource, Offset3D[] dstOffsets) {
        this.srcSubresource = srcSubresource;
        this.srcOffsets = srcOffsets;
        this.dstSubresource = dstSubresource;
        this.dstOffsets = dstOffsets;
    }

}
