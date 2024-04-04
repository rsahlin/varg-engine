
package org.varg.lwjgl3.vulkan;

import org.eclipse.jdt.annotation.NonNull;
import org.lwjgl.vulkan.VkDevice;
import org.varg.vulkan.Features;
import org.varg.vulkan.LogicalDevice;
import org.varg.vulkan.Vulkan10.VulkanExtension;
import org.varg.vulkan.structs.PhysicalDeviceProperties;

public class LWJGLVulkanLogicalDevice extends LogicalDevice<VkDevice> {

    public LWJGLVulkanLogicalDevice(@NonNull VkDevice device,
            @NonNull PhysicalDeviceProperties physicalDeviceProperties, @NonNull Features deviceFeatures,
            @NonNull VulkanExtension[] enabledExtensions) {
        super(device, physicalDeviceProperties, deviceFeatures, enabledExtensions);
    }
}
