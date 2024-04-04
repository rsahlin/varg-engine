package org.varg.lwjgl3.vulkan.extensions;

import org.eclipse.jdt.annotation.NonNull;
import org.varg.vulkan.extensions.KHRFragmentShadingRate;
import org.varg.vulkan.extensions.PhysicalDeviceFragmentShadingRateFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceFragmentShadingRatePropertiesKHR;

public class LWJGL3KHRFragmentShadingRate extends KHRFragmentShadingRate {

    public LWJGL3KHRFragmentShadingRate(
            @NonNull PhysicalDeviceFragmentShadingRateFeaturesKHR fragmentShadingRateFeatures,
            @NonNull PhysicalDeviceFragmentShadingRatePropertiesKHR fragmentShadingRateProperties) {
        super(fragmentShadingRateFeatures, fragmentShadingRateProperties);
    }

}
