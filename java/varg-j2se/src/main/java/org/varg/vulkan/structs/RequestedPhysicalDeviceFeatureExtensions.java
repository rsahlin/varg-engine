package org.varg.vulkan.structs;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.extensions.EXTRobustness2.PhysicalDeviceRobustness2FeaturesEXT;
import org.varg.vulkan.extensions.PhysicalDeviceAccelerationStructureFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceFragmentShadingRateFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderFeaturesEXT;

public class RequestedPhysicalDeviceFeatureExtensions extends PhysicalDeviceFeatureExtensions {

    private final PhysicalDeviceFeatureExtensions available;

    RequestedPhysicalDeviceFeatureExtensions(PhysicalDeviceFeatureExtensions available) {
        this.available = available;
    }

    /**
     * Enables support for 16 bit storage
     * 
     * @param features
     */
    protected void add16BitStorage(PhysicalDevice16BitStorageFeatures features) {
        if (!available.getPhysicalDevice16BitStorageFeatures()
                .checkBooleansTrue(features, PhysicalDevice16BitStorageFeatures.class.getDeclaredFields())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Available features does not match requested");
        }
        storage16BitFeatures = features;
    }

    /**
     * Enables support for 8 bit storage
     * 
     * @param features
     */
    protected void add8BitStorage(PhysicalDevice8BitStorageFeatures features) {
        if (!available.getPhysicalDevice8BitStorageFeatures()
                .checkBooleansTrue(features, PhysicalDevice8BitStorageFeatures.class.getDeclaredFields())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Available features does not match requested");
        }
        storage8BitFeatures = features;
    }

    /**
     * Enables support for Shader float16 or int8
     * 
     * @param features
     */
    protected void addShaderFloat16Int8(PhysicalDeviceShaderFloat16Int8Features features) {
        if (!available.getPhysicalDeviceShaderFloat16Int8Features().checkBooleansTrue(features,
                PhysicalDeviceShaderFloat16Int8Features.class.getDeclaredFields())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Available features does not match requested");
        }
        shaderFloat16Features = features;
    }

    /**
     * Adds the VK_EXT_mesh_shader extension with the features
     * 
     * @param features
     */
    protected void addEXTMeshShader(PhysicalDeviceMeshShaderFeaturesEXT features) {
        if (!available.getPhysicalDeviceMeshShaderFeaturesEXT()
                .checkBooleansTrue(features, PhysicalDeviceMeshShaderFeaturesEXT.class.getDeclaredFields())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Available features does not match requested");
        }
        meshShaderFeatures = features;
    }

    /**
     * Adds VK_KHR_fragment_shading_rate extension with the features
     * 
     * @param features
     */
    protected void addKHRFragmentShadingRate(PhysicalDeviceFragmentShadingRateFeaturesKHR features) {
        if (!available.getPhysicalDeviceFragmentShadingRateFeaturesKHR().checkBooleansTrue(features,
                PhysicalDeviceFragmentShadingRateFeaturesKHR.class.getDeclaredFields())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Available features does not match requested");
        }
        fragmentShadingRateFeatures = features;
    }

    /**
     * Adds the khraccelerationstructure extension and features
     * 
     * @param features
     */
    protected void addKHRAccelerationStructure(PhysicalDeviceAccelerationStructureFeaturesKHR features) {
        if (!available.getPhysicalDeviceAccelerationStructureFeatures().checkBooleansTrue(features,
                PhysicalDeviceAccelerationStructureFeaturesKHR.class.getDeclaredFields())) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Available features does not match requested");
        }
        accelerationStructureFeatures = features;
    }

    /**
     * Enables null descriptor, part of Robustness2 extension
     */
    protected void enableNullDescriptor() {
        if (!available.getPhysicalDeviceRobustness2Features().nullDescriptor) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "No support for nullDescriptor (Robustness2)");
        }
        if (robustness2Features == null) {
            robustness2Features = new PhysicalDeviceRobustness2FeaturesEXT(false, false, true);
        } else {
            robustness2Features.nullDescriptor = true;
        }
    }

}
