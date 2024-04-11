package org.varg.vulkan.extensions;

import org.gltfio.lib.AllowPublic;
import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.PlatformStruct;

/**
 * The vkPhysicalDeviceFragmentShadingRatePropertiesKHR
 */
public class PhysicalDeviceFragmentShadingRatePropertiesKHR extends PlatformStruct {

    @AllowPublic
    public Extent2D minFragmentShadingRateAttachmentTexelSize;
    @AllowPublic
    public Extent2D maxFragmentShadingRateAttachmentTexelSize;
    @AllowPublic
    public int maxFragmentShadingRateAttachmentTexelSizeAspectRatio;
    @AllowPublic
    public boolean primitiveFragmentShadingRateWithMultipleViewports;
    @AllowPublic
    public boolean layeredShadingRateAttachments;
    @AllowPublic
    public boolean fragmentShadingRateNonTrivialCombinerOps;
    @AllowPublic
    public Extent2D maxFragmentSize;
    @AllowPublic
    public int maxFragmentSizeAspectRatio;
    @AllowPublic
    public int maxFragmentShadingRateCoverageSamples;
    @AllowPublic
    public Vulkan10.SampleCountFlagBit maxFragmentShadingRateRasterizationSamples;
    @AllowPublic
    public boolean fragmentShadingRateWithShaderDepthStencilWrites;
    @AllowPublic
    public boolean fragmentShadingRateWithSampleMask;
    @AllowPublic
    public boolean fragmentShadingRateWithShaderSampleMask;
    @AllowPublic
    public boolean fragmentShadingRateWithConservativeRasterization;
    @AllowPublic
    public boolean fragmentShadingRateWithFragmentShaderInterlock;
    @AllowPublic
    public boolean fragmentShadingRateWithCustomSampleLocations;
    @AllowPublic
    public boolean fragmentShadingRateStrictMultiplyCombiner;

    public PhysicalDeviceFragmentShadingRatePropertiesKHR(Object properties) {
        copyFieldsFromStruct(properties, PhysicalDeviceFragmentShadingRatePropertiesKHR.class);
    }

}
