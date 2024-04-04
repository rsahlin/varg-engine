
package org.varg.vulkan.structs;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan10.CompositeAlphaFlagBitsKHR;
import org.varg.vulkan.Vulkan10.ImageUsageFlagBits;
import org.varg.vulkan.Vulkan10.SurfaceTransformFlagBitsKHR;

public final class SurfaceCapabilitiesKHR {

    public int getMinImageCount() {
        return minImageCount;
    }

    public int getMaxImageCount() {
        return maxImageCount;
    }

    public Extent2D getCurrentExtent() {
        return currentExtent;
    }

    public Extent2D getMinImageExtent() {
        return minImageExtent;
    }

    public Extent2D getMaxImageExtent() {
        return maxImageExtent;
    }

    public int getMaxImageArrayLayers() {
        return maxImageArrayLayers;
    }

    public int getSupportedTransformsValue() {
        return BitFlags.getFlagsValue(supportedTransforms);
    }

    public SurfaceTransformFlagBitsKHR[] getSupportedTransforms() {
        return supportedTransforms;
    }

    public SurfaceTransformFlagBitsKHR getCurrentTransform() {
        return currentTransform;
    }

    public CompositeAlphaFlagBitsKHR[] getSupportedCompositeAlpha() {
        return supportedCompositeAlpha;
    }

    public ImageUsageFlagBits[] getSupportedUsageFlags() {
        return supportedUsageFlags;
    }

    public final int minImageCount;
    public final int maxImageCount;
    public final Extent2D currentExtent;
    public final Extent2D minImageExtent;
    public final Extent2D maxImageExtent;
    public final int maxImageArrayLayers;
    final SurfaceTransformFlagBitsKHR[] supportedTransforms;
    final SurfaceTransformFlagBitsKHR currentTransform;
    final CompositeAlphaFlagBitsKHR[] supportedCompositeAlpha;
    final ImageUsageFlagBits[] supportedUsageFlags;

    public SurfaceCapabilitiesKHR(int minImageCount, int maxImageCount, Extent2D minImageExtent,
            Extent2D maxImageExtent, Extent2D currentExtent, int maxImageArrayLayers,
            SurfaceTransformFlagBitsKHR[] supportedTransforms, SurfaceTransformFlagBitsKHR currentTransform,
            CompositeAlphaFlagBitsKHR[] supportedCompositeAlpha, ImageUsageFlagBits[] usageFlags) {
        this.minImageCount = minImageCount;
        this.maxImageCount = maxImageCount;
        this.minImageExtent = minImageExtent;
        this.maxImageExtent = maxImageExtent;
        this.currentExtent = currentExtent;
        this.maxImageArrayLayers = maxImageArrayLayers;
        this.supportedTransforms = supportedTransforms;
        this.currentTransform = currentTransform;
        this.supportedCompositeAlpha = supportedCompositeAlpha;
        this.supportedUsageFlags = usageFlags;
    }

}
