package org.varg.vulkan.extensions;

import org.gltfio.lib.AllowPublic;
import org.varg.vulkan.structs.PlatformStruct;

/**
 * The vkPhysicalDeviceFragmentShadingRateFeaturesKHR
 */
public class PhysicalDeviceFragmentShadingRateFeaturesKHR extends PlatformStruct {

    @AllowPublic
    public boolean pipelineFragmentShadingRate;
    @AllowPublic
    public boolean primitiveFragmentShadingRate;
    @AllowPublic
    public boolean attachmentFragmentShadingRate;

    public PhysicalDeviceFragmentShadingRateFeaturesKHR(boolean pipelineFragmentShadingRate,
            boolean primitiveFragmentShadingRate, boolean attachmentFragmentShadingRate) {
        this.pipelineFragmentShadingRate = pipelineFragmentShadingRate;
        this.primitiveFragmentShadingRate = primitiveFragmentShadingRate;
        this.attachmentFragmentShadingRate = attachmentFragmentShadingRate;
    }

    public PhysicalDeviceFragmentShadingRateFeaturesKHR(Object features) {
        super.copyBooleanFieldsFromStruct(features, PhysicalDeviceFragmentShadingRateFeaturesKHR.class);
    }

}
