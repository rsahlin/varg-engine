
package org.varg.vulkan.structs;

import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.VulkanBackend.APIVersion;

/**
 * Abstraction of VkPhysicalDeviceProperties for physical device properties
 *
 */
public interface PhysicalDeviceProperties {

    enum PhysicalDeviceType {
        VK_PHYSICAL_DEVICE_TYPE_OTHER(0),
        VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU(1),
        VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU(2),
        VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU(3),
        VK_PHYSICAL_DEVICE_TYPE_CPU(4);

        public final int value;

        PhysicalDeviceType(int value) {
            this.value = value;
        }

        public static PhysicalDeviceType get(int value) {
            for (PhysicalDeviceType type : PhysicalDeviceType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }

    };

    /**
     * Returns the device type
     * 
     * @return The VkPhysicalDeviceType of the device
     */
    PhysicalDeviceType getDeviceType();

    /**
     * Returns the name of the device
     * 
     * @return
     */
    String getDeviceName();

    /**
     * Returns the API version as defined in VkPhysicalDevice
     * 
     * @return
     */
    APIVersion getAPIVersion();

    /**
     * Returns the device limits
     * 
     * @return
     */
    DeviceLimits getLimits();

    /**
     * Returns the properties for the specified extension, or null if none exist or extension has not been enabled.
     * 
     * @param extension
     * @return
     */
    PlatformStruct getProperties(Vulkan10.VulkanExtension extension);

}
