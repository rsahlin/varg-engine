
package org.varg.vulkan.image;

import org.eclipse.jdt.annotation.NonNull;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.ImageCreateFlagBits;
import org.varg.vulkan.Vulkan10.ImageLayout;
import org.varg.vulkan.Vulkan10.ImageTiling;
import org.varg.vulkan.Vulkan10.ImageType;
import org.varg.vulkan.Vulkan10.ImageUsageFlagBits;
import org.varg.vulkan.Vulkan10.SampleCountFlagBit;
import org.varg.vulkan.Vulkan10.SharingMode;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.Extent3D;

public class ImageCreateInfo {

    public final ImageCreateFlagBits[] flags;
    public final ImageType imageType;
    public final int formatValue;
    public final Vulkan10.Format format;
    public final Extent3D extent;
    public final int mipLevels;
    // Must be > 0
    public final int arrayLayers;
    public final SampleCountFlagBit samples;
    public final ImageTiling tiling;
    public final ImageUsageFlagBits[] usage;
    public final SharingMode sharingMode;
    public final ImageLayout initialLayout;
    public final int queueFamilyIndexCount;
    private final int[] pQueueFamilyIndices;

    /**
     * Creates a new imagecreateinfo with no ImageCreateFlagBits, ImageType.VK_IMAGE_TYPE_2D,
     * ImageTiling.VK_IMAGE_TILING_OPTIMAL, ImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, SharingMode.VK_SHARING_MODE_EXCLUSIVE
     * SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT
     * 
     * @param format
     * @param extent
     * @param usage
     */
    public ImageCreateInfo(int formatValue, @NonNull Extent2D extent, @NonNull ImageUsageFlagBits[] usage,
            @NonNull SampleCountFlagBit samples) {
        this.flags = null;
        this.imageType = ImageType.VK_IMAGE_TYPE_2D;
        this.formatValue = formatValue;
        this.format = Vulkan10.Format.get(formatValue);
        this.extent = new Extent3D(extent);
        this.usage = usage;
        this.tiling = ImageTiling.VK_IMAGE_TILING_OPTIMAL;
        this.initialLayout = ImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
        this.sharingMode = SharingMode.VK_SHARING_MODE_EXCLUSIVE;
        this.samples = samples;
        this.queueFamilyIndexCount = 0;
        this.pQueueFamilyIndices = null;
        this.mipLevels = 1;
        this.arrayLayers = 1;
    }

    public ImageCreateInfo(int formatValue, @NonNull Extent2D extent, @NonNull ImageUsageFlagBits[] usage,
            @NonNull ImageTiling tiling) {

        this.flags = null;
        this.imageType = ImageType.VK_IMAGE_TYPE_2D;
        this.formatValue = formatValue;
        this.format = Vulkan10.Format.get(formatValue);
        this.extent = new Extent3D(extent);
        this.usage = usage;
        this.tiling = tiling;
        this.initialLayout = ImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
        this.sharingMode = SharingMode.VK_SHARING_MODE_EXCLUSIVE;
        this.samples = SampleCountFlagBit.VK_SAMPLE_COUNT_1_BIT;
        this.queueFamilyIndexCount = 0;
        this.pQueueFamilyIndices = null;
        this.mipLevels = 1;
        this.arrayLayers = 1;

    }

    public ImageCreateInfo(@NonNull ImageCreateFlagBits[] flags, @NonNull ImageType imageType,
            int formatValue, @NonNull Extent3D extent,
            int mipLevels, int arrayLayers, @NonNull SampleCountFlagBit samples, @NonNull ImageTiling tiling,
            ImageUsageFlagBits[] usage,
            ImageLayout initialLayout) {
        if (arrayLayers <= 0) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + " Arraylayers must be > 0 but is " + arrayLayers);
        }
        if (mipLevels <= 0) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + " Miplevels must be > 0 but is " + mipLevels);
        }
        if (extent.depth <= 0) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + " Depth must be > 0 but is " + extent.depth);
        }
        this.flags = flags;
        this.imageType = imageType;
        this.formatValue = formatValue;
        this.format = Vulkan10.Format.get(formatValue);
        this.extent = extent;
        this.mipLevels = mipLevels;
        this.arrayLayers = arrayLayers;
        this.samples = samples;
        this.tiling = tiling;
        this.usage = usage;
        this.initialLayout = initialLayout;
        sharingMode = SharingMode.VK_SHARING_MODE_EXCLUSIVE;
        queueFamilyIndexCount = 0;
        pQueueFamilyIndices = null;
    }

}
