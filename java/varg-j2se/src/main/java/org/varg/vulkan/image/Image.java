
package org.varg.vulkan.image;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.ImageCreateFlagBits;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.ImageTiling;
import org.varg.vulkan.Vulkan10.ImageUsageFlagBits;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;
import org.varg.vulkan.structs.Extent3D;
import org.varg.vulkan.structs.SubresourceLayout;

/**
 * Wrapper for VkImage
 * This object is treated as equal if the pointer returned by getImage()
 * is the same.
 * Hashcode is generated from pointer returned by getImage()
 *
 */
public class Image {

    public final long pointer;
    private final ImageCreateInfo createInfo;
    private final SubresourceLayout resourceLayout;
    private HashMap<Integer, ImageLayout[]> layout = new HashMap<Integer, ImageLayout[]>();

    public Image(long image, ImageCreateInfo createInfo, SubresourceLayout resourceLayout) {
        if (image == 0 || createInfo == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        pointer = image;
        this.createInfo = createInfo;
        this.resourceLayout = resourceLayout;
        for (int layer = 0; layer < createInfo.arrayLayers; layer++) {
            ImageLayout[] mipLayouts = new ImageLayout[createInfo.mipLevels];
            for (int level = 0; level < createInfo.mipLevels; level++) {
                mipLayouts[level] = createInfo.initialLayout;
            }
            layout.put(layer, mipLayouts);
        }

    }

    /**
     * Returns the createinfo
     * 
     * @return
     */
    public ImageCreateInfo getCreateInfo() {
        return createInfo;
    }

    /**
     * Returns the SubresourceLayout of the image
     * 
     * @return
     */
    public SubresourceLayout getSubresourceLayout() {
        return resourceLayout;
    }

    /**
     * Returns the imagelayout for the subresourcelayer, or null if none exits
     * 
     * @param subLayer
     * @return
     */
    public ImageLayout getLayout(ImageSubresourceLayers subLayer) {
        return getLayout(subLayer.mipLevel, subLayer.baseArrayLayer);
    }

    /**
     * Returns the imagelayout for the subresourcerange, or null if none exists
     * 
     * @param subresource
     * @return
     */
    public ImageLayout getLayout(ImageSubresourceRange subresource) {
        return getLayout(subresource.baseMipLevel, subresource.baseArrayLayer);
    }

    /**
     * Returns the current layout for the specified level and layer - or null if none exist
     * 
     * @param mipLevel
     * @param arrayLayer
     * @return
     */
    public ImageLayout getLayout(int mipLevel, int arrayLayer) {
        ImageLayout[] mipLayouts = layout.get(arrayLayer);
        if (mipLayouts != null) {
            return mipLayouts[mipLevel];
        }
        return null;
    }

    /**
     * Returns the imagetiling of the image
     * 
     * @return
     */
    public ImageTiling getTiling() {
        return createInfo.tiling;
    }

    /**
     * Internal method - do not use
     * 
     * @param newLayout
     * @param subresource
     */
    void updateLayout(@NonNull ImageLayout newLayout, @NonNull ImageSubresourceRange subresource) {
        ImageLayout[] mipLayouts = layout.get(subresource.baseArrayLayer);
        for (int i = subresource.baseMipLevel; i < subresource.baseMipLevel + subresource.levelCount; i++) {
            mipLayouts[i] = newLayout;
        }
    }

    /**
     * Returns the extent of the image
     * 
     * @return
     */
    public Extent3D getExtent() {
        return createInfo.extent;
    }

    /**
     * Returns the format of the image
     * 
     * @return
     */
    public int getFormatValue() {
        return createInfo.formatValue;
    }

    /**
     * Returns the format
     * 
     * @return
     */
    public Vulkan10.Format getFormat() {
        return createInfo.format;
    }

    /**
     * Returns the image usage flagbits
     * 
     * @return
     */
    public ImageUsageFlagBits[] getUsageFlags() {
        return createInfo.usage;
    }

    /**
     * Returns the image create flagbits
     * 
     * @return
     */
    public ImageCreateFlagBits[] getCreateFlags() {
        return createInfo.flags;
    }

    /**
     * Returns the number of miplevels
     * 
     * @return
     */
    public int getMipLevels() {
        return createInfo.mipLevels;
    }

    /**
     * Returns the number of arraylayers
     * 
     * @return
     */
    public int getArrayLayers() {
        return createInfo.arrayLayers;
    }

    /**
     * Returns the sample count
     * 
     * @return
     */
    public SampleCountFlagBit getSamples() {
        return createInfo.samples;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (pointer ^ (pointer >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        Image other = (Image) obj;
        return pointer == other.pointer;
    }

}
