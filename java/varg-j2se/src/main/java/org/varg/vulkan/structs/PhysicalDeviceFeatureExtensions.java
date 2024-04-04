
package org.varg.vulkan.structs;

import org.varg.vulkan.extensions.EXTRobustness2.PhysicalDeviceRobustness2FeaturesEXT;
import org.varg.vulkan.extensions.PhysicalDeviceAccelerationStructureFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceFragmentShadingRateFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderFeaturesEXT;

public class PhysicalDeviceFeatureExtensions extends PlatformStruct {

    protected PhysicalDeviceMeshShaderFeaturesEXT meshShaderFeatures;
    protected PhysicalDevice16BitStorageFeatures storage16BitFeatures;
    protected PhysicalDevice8BitStorageFeatures storage8BitFeatures;
    protected PhysicalDeviceShaderFloat16Int8Features shaderFloat16Features;
    protected PhysicalDeviceRobustness2FeaturesEXT robustness2Features;
    protected PhysicalDeviceFragmentShadingRateFeaturesKHR fragmentShadingRateFeatures;
    protected PhysicalDeviceAccelerationStructureFeaturesKHR accelerationStructureFeatures;

    /**
     * Additional fields from extensions
     */
    boolean indexTypeUint8;
    boolean conditionalRendering;

    public final boolean hasIndexTypeUint8() {
        return indexTypeUint8;
    }

    public final boolean hasConditionalRendering() {
        return conditionalRendering;
    }

    public final PhysicalDeviceFragmentShadingRateFeaturesKHR getPhysicalDeviceFragmentShadingRateFeaturesKHR() {
        return fragmentShadingRateFeatures;
    }

    public final PhysicalDeviceMeshShaderFeaturesEXT getPhysicalDeviceMeshShaderFeaturesEXT() {
        return meshShaderFeatures;
    }

    public final PhysicalDevice16BitStorageFeatures getPhysicalDevice16BitStorageFeatures() {
        return storage16BitFeatures;
    }

    public final PhysicalDevice8BitStorageFeatures getPhysicalDevice8BitStorageFeatures() {
        return storage8BitFeatures;
    }

    public final PhysicalDeviceShaderFloat16Int8Features getPhysicalDeviceShaderFloat16Int8Features() {
        return shaderFloat16Features;
    }

    public final PhysicalDeviceRobustness2FeaturesEXT getPhysicalDeviceRobustness2Features() {
        return robustness2Features;
    }

    public final PhysicalDeviceAccelerationStructureFeaturesKHR getPhysicalDeviceAccelerationStructureFeatures() {
        return accelerationStructureFeatures;
    }

}
