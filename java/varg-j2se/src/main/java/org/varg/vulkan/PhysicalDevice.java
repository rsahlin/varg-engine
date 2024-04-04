
package org.varg.vulkan;

import org.varg.vulkan.structs.ExtensionProperties;
import org.varg.vulkan.structs.PhysicalDeviceProperties;
import org.varg.vulkan.structs.QueueFamilyProperties;

/**
 * Abstraction VkPhysicalDevice for Khronos Vulkan physical device
 *
 */
public interface PhysicalDevice {

    /**
     * Returns the physical device properties
     * 
     * @return
     */
    PhysicalDeviceProperties getProperties();

    /**
     * Returns the physical device features - NOTE - these are the physical device features not the logicaldevice
     * features.
     * In order to check what features are actually enabled the features from the logical device MUST be used.
     * 
     * @return The physical device features, ie features that MAY be supported by a logical device.
     */
    Features getFeatures();

    /**
     * Returns the supported queue families of the device
     * 
     * @return
     */
    QueueFamilyProperties[] getQueueFamilyProperties();

    /**
     * Returns the supported extension properties
     * 
     * @return
     */
    ExtensionProperties[] getExtensionProperties();

    /**
     * Returns the extension property if this device supportes the named extension
     * 
     * @param extensionName
     * @return The extension property, or null if not supported
     */
    ExtensionProperties getExtension(String extensionName);

}
