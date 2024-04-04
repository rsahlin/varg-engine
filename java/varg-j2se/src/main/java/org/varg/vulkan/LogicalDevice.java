
package org.varg.vulkan;

import org.eclipse.jdt.annotation.NonNull;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.VulkanExtension;
import org.varg.vulkan.structs.PhysicalDeviceProperties;

/**
 * Device instance for Vulkan implementations
 *
 * @param T The platform object for vulkan device instance.
 */
public abstract class LogicalDevice<T> {

    private final Features features;
    private final PhysicalDeviceProperties physicalDeviceProperties;
    private final T deviceInstance;
    /**
     * The extensions enabled in the logical device
     */
    private final VulkanExtension[] enabledExtensions;

    public LogicalDevice(@NonNull T device, @NonNull PhysicalDeviceProperties physicalDeviceProperties,
            @NonNull Features deviceFeatures, @NonNull VulkanExtension[] enabledExtensions) {
        if (device == null || physicalDeviceProperties == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.deviceInstance = device;
        this.physicalDeviceProperties = physicalDeviceProperties;
        this.features = deviceFeatures;
        this.enabledExtensions = enabledExtensions;
    }

    /**
     * Returns the platform device instance
     * 
     * @return
     */
    public T getDeviceInstance() {
        return deviceInstance;
    }

    /**
     * Returns the physical device properties
     * 
     * @return
     */
    public PhysicalDeviceProperties getPhysicalDeviceProperties() {
        return physicalDeviceProperties;
    }

    /**
     * Returns the enabled device features for this logical device, these are the features that are supported
     * at runtime.
     * 
     * @return
     */
    public Features getFeatures() {
        return features;
    }

    /**
     * Returns a clone of the array containing enabled extensions on this logical device.
     * 
     * @return
     */
    public VulkanExtension[] getEnabledExtensions() {
        return enabledExtensions.clone();
    }

}
