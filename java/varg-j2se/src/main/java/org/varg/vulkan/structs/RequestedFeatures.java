package org.varg.vulkan.structs;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Features;
import org.varg.vulkan.Vulkan10.Extension;
import org.varg.vulkan.Vulkan10.Vulkan11Extension;
import org.varg.vulkan.Vulkan10.Vulkan12Extension;
import org.varg.vulkan.Vulkan10.VulkanExtension;
import org.varg.vulkan.extensions.PhysicalDeviceAccelerationStructureFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceFragmentShadingRateFeaturesKHR;
import org.varg.vulkan.extensions.PhysicalDeviceMeshShaderFeaturesEXT;
import org.varg.vulkan.extensions.PhysicalDeviceRayTracingPipelineFeaturesKHR;
import org.varg.vulkan.structs.PhysicalDeviceFeatures.VulkanFeatures;
import org.varg.vulkan.structs.PhysicalDeviceFeatures.VulkanPhysicalDeviceFeatures;
import org.varg.vulkan.structs.PhysicalDeviceVulkan11Features.Vulkan11Features;
import org.varg.vulkan.structs.PhysicalDeviceVulkan12Features.Vulkan12Features;

/**
 * This class handles the features extensions that are requested
 *
 */
public class RequestedFeatures {

    private final ExtensionProperties[] availableExtensions;
    private final ArrayList<ExtensionProperties> requestedExtensionList = new ArrayList<>();
    RequestedPhysicalDeviceFeatures requestedFeatures;
    RequestedPhysicalDeviceFeatureExtensions requestedFeatureExtensions;

    public RequestedFeatures(@NonNull Features availableFeatures) {
        requestedFeatures = new RequestedPhysicalDeviceFeatures(availableFeatures.getPhysicalDeviceFeatures());
        this.availableExtensions = availableFeatures.getExtensions();
        this.requestedFeatureExtensions = new RequestedPhysicalDeviceFeatureExtensions(availableFeatures
                .getPhysicalDeviceFeatureExtensions());
    }

    /**
     * Adds the extension to requested extensions if there is support for the extension, otherwise an exception
     * is thrown - internal method, do NOT use
     * 
     * @param extension
     */
    public void addExtension(VulkanExtension extension) {
        ExtensionProperties add = ExtensionProperties.get(extension.getName(), availableExtensions);
        if (add == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "No support for " + extension);
        }
        if (!requestedExtensionList.contains(add)) {
            requestedExtensionList.add(add);
        }
    }

    /**
     * Adds the VK_KHR_swapchain extension
     * 
     */
    public void addKHRSwapChain() {
        addExtension(Extension.VK_KHR_swapchain);
    }

    /**
     * Adds the VK_EXT_hdr_metadata extension
     */
    public void addEXTHdrMetadata() {
        addExtension(Extension.VK_EXT_hdr_metadata);
    }

    /**
     * Adds the VK_EXT_mesh_shader extension with the features
     * 
     * @param features
     */
    public void addEXTMeshShader(PhysicalDeviceMeshShaderFeaturesEXT features) {
        addExtension(Extension.VK_EXT_mesh_shader);
        requestedFeatureExtensions.addEXTMeshShader(features);
    }

    /**
     * Adds the VK_KHR_ray_tracing_pipeline extension with the features.
     * 
     * @param features
     */
    public void addKHRRayTracing(PhysicalDeviceRayTracingPipelineFeaturesKHR features) {
        addExtension(Extension.VK_KHR_ray_tracing_pipeline);
        requestedFeatureExtensions.addKHRRayTracing(features);
    }

    /**
     * Adds VK_KHR_fragment_rate_shading extension with the requested features
     * 
     * @param features
     */
    public void addKHRFragmentShadingRate(PhysicalDeviceFragmentShadingRateFeaturesKHR features) {
        addExtension(Extension.VK_KHR_fragment_shading_rate);
        requestedFeatureExtensions.addKHRFragmentShadingRate(features);
    }

    /**
     * Enables support for 8 bit storage
     * 
     * @param features
     */
    public void add8BitStorage(boolean storageBuffer8BitAccess, boolean uniformAndStorageBuffer8BitAccess,
            boolean storagePushConstant8) {
        PhysicalDevice8BitStorageFeatures storageFeatures = new PhysicalDevice8BitStorageFeatures(storageBuffer8BitAccess, uniformAndStorageBuffer8BitAccess, storagePushConstant8);
        requestedFeatureExtensions.add8BitStorage(storageFeatures);
        if (storageBuffer8BitAccess) {
            requestedFeatures.setFeatures(Vulkan12Features.storageBuffer8BitAccess);
        }
        if (uniformAndStorageBuffer8BitAccess) {
            requestedFeatures.setFeatures(Vulkan12Features.uniformAndStorageBuffer8BitAccess);
        }
        if (storagePushConstant8) {
            requestedFeatures.setFeatures(Vulkan12Features.storagePushConstant8);
        }
        addExtension(Vulkan11Extension.VK_KHR_storage_buffer_storage_class);
        addExtension(Vulkan12Extension.VK_KHR_8bit_storage);
    }

    /**
     * Enables support for 16 bit storage
     * 
     * @param features
     */
    public void add16BitStorage(boolean storageBuffer16BitAccess, boolean uniformAndStorageBuffer16BitAccess, boolean storagePushConstant16, boolean storageInputOutput16) {
        PhysicalDevice16BitStorageFeatures storageFeatures = new PhysicalDevice16BitStorageFeatures(storageBuffer16BitAccess, uniformAndStorageBuffer16BitAccess, storagePushConstant16, storageInputOutput16);
        requestedFeatureExtensions.add16BitStorage(storageFeatures);
        if (storageBuffer16BitAccess) {
            requestedFeatures.setFeatures(Vulkan11Features.storageBuffer16BitAccess);
        }
        if (uniformAndStorageBuffer16BitAccess) {
            requestedFeatures.setFeatures(Vulkan11Features.uniformAndStorageBuffer16BitAccess);
        }
        if (storagePushConstant16) {
            requestedFeatures.setFeatures(Vulkan11Features.storagePushConstant16);
        }
        if (storageInputOutput16) {
            requestedFeatures.setFeatures(Vulkan11Features.storageInputOutput16);
        }
        addExtension(Vulkan11Extension.VK_KHR_storage_buffer_storage_class);
        addExtension(Vulkan12Extension.VK_KHR_8bit_storage);
    }

    /**
     * Enables support for null descriptor - part of robustness2 extension
     */
    public void enableNullDescriptor() {
        addExtension(Extension.VK_EXT_robustness2);
        requestedFeatureExtensions.enableNullDescriptor();
    }

    /**
     * Enables raytracing pipeline and the extensions needed to support ray tracing.
     */
    public void addRayTracing(PhysicalDeviceAccelerationStructureFeaturesKHR accelerationFeatures) {
        addExtension(Extension.VK_KHR_acceleration_structure);
        addExtension(Extension.VK_KHR_deferred_host_operations);
        addExtension(Extension.VK_KHR_ray_tracing_pipeline);
        requestedFeatureExtensions.addKHRAccelerationStructure(accelerationFeatures);
        requestedFeatures.setFeatures(Vulkan12Features.bufferDeviceAddress);
    }

    /**
     * Enable support for shader float16 or int8
     * 
     * @param shaderFloat16
     * @param shaderInt8
     */
    public void addShaderFloat16Int8(boolean shaderFloat16, boolean shaderInt8, boolean shaderInt16) {
        PhysicalDeviceShaderFloat16Int8Features shaderFeatures = new PhysicalDeviceShaderFloat16Int8Features(
                shaderFloat16, shaderInt8);
        requestedFeatureExtensions.addShaderFloat16Int8(shaderFeatures);
        if (shaderFloat16) {
            requestedFeatures.setFeatures(Vulkan12Features.shaderFloat16);
        }
        if (shaderInt8) {
            requestedFeatures.setFeatures(Vulkan12Features.shaderInt8);
        }
        if (shaderInt16) {
            requestedFeatures.setFeatures(VulkanFeatures.shaderInt16);
        }
    }

    /**
     * Returns the requested extensions
     * 
     * @return
     */
    public ExtensionProperties[] getRequestedExtensions() {
        return requestedExtensionList.toArray(new ExtensionProperties[0]);
    }

    /**
     * Sets the features, throws exception if not supported - NOTE this MUST only be called for features
     * that does not have any other granularity than on/off.
     * 
     * @param requested
     */
    public void setFeatures(VulkanPhysicalDeviceFeatures[] requested) {
        requestedFeatures.setFeatures(requested);
    }

    /**
     * Returns the requested features and extensions
     * 
     * @return
     */
    public Features getFeatures() {
        return new Features(requestedFeatures, requestedFeatureExtensions, requestedExtensionList.toArray(new ExtensionProperties[0]));
    }

}
