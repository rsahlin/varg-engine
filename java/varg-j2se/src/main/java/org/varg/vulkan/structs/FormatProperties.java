
package org.varg.vulkan.structs;

import org.gltfio.lib.BitFlags;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.FormatFeatureFlagBits;
import org.varg.vulkan.Vulkan10.ImageTiling;

public class FormatProperties {

    public final FormatFeatureFlagBits[] linearTilingFeatures;
    public final FormatFeatureFlagBits[] optimalTilingFeatures;
    public final FormatFeatureFlagBits[] bufferFeatures;

    public FormatProperties(FormatFeatureFlagBits[] linearTilingFeatures,
            FormatFeatureFlagBits[] optimalTilingFeatures,
            FormatFeatureFlagBits[] bufferFeatures) {
        this.linearTilingFeatures = linearTilingFeatures;
        this.optimalTilingFeatures = optimalTilingFeatures;
        this.bufferFeatures = bufferFeatures;
    }

    /**
     * Returns the format features for the specified tiling
     * 
     * @param tiling
     * @return
     * @throws IllegalArgumentException if tiling is null
     */
    public FormatFeatureFlagBits[] getFormatFeatures(ImageTiling tiling) {
        if (tiling == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        switch (tiling) {
            case VK_IMAGE_TILING_OPTIMAL:
                return optimalTilingFeatures;
            case VK_IMAGE_TILING_LINEAR:
                return linearTilingFeatures;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + tiling);
        }
    }

    /**
     * Returns if the features are supported in tiling.
     * 
     * @param tiling
     * @param features
     * @return
     */
    public boolean supportsFeature(ImageTiling tiling, FormatFeatureFlagBits... features) {
        FormatFeatureFlagBits[] flags = getFormatFeatures(tiling);
        for (FormatFeatureFlagBits feature : features) {
            if (!BitFlags.contains(flags, feature.value)) {
                return false;
            }
        }
        return true;
    }

}
