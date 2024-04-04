package org.varg.vulkan.extensions;

import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.structs.Extent2D;
import org.varg.vulkan.structs.PlatformStruct;

/**
 * The vkPhysicalDeviceFragmentShadingRatePropertiesKHR
 */
public class PhysicalDeviceFragmentShadingRatePropertiesKHR extends PlatformStruct {

    public Extent2D minFragmentShadingRateAttachmentTexelSize;
    public Extent2D maxFragmentShadingRateAttachmentTexelSize;
    public int maxFragmentShadingRateAttachmentTexelSizeAspectRatio;
    public boolean primitiveFragmentShadingRateWithMultipleViewports;
    public boolean layeredShadingRateAttachments;
    public boolean fragmentShadingRateNonTrivialCombinerOps;
    public Extent2D maxFragmentSize;
    public int maxFragmentSizeAspectRatio;
    public int maxFragmentShadingRateCoverageSamples;
    public Vulkan10.SampleCountFlagBit maxFragmentShadingRateRasterizationSamples;
    public boolean fragmentShadingRateWithShaderDepthStencilWrites;
    public boolean fragmentShadingRateWithSampleMask;
    public boolean fragmentShadingRateWithShaderSampleMask;
    public boolean fragmentShadingRateWithConservativeRasterization;
    public boolean fragmentShadingRateWithFragmentShaderInterlock;
    public boolean fragmentShadingRateWithCustomSampleLocations;
    public boolean fragmentShadingRateStrictMultiplyCombiner;

    public PhysicalDeviceFragmentShadingRatePropertiesKHR(Object properties) {
        copyFieldsFromStruct(properties, PhysicalDeviceFragmentShadingRatePropertiesKHR.class);
    }

}
