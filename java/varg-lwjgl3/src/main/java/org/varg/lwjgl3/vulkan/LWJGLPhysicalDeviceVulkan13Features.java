package org.varg.lwjgl3.vulkan;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;
import org.varg.vulkan.structs.PhysicalDeviceVulkan13Features;

public class LWJGLPhysicalDeviceVulkan13Features extends PhysicalDeviceVulkan13Features {

    public LWJGLPhysicalDeviceVulkan13Features() {
    }

    public LWJGLPhysicalDeviceVulkan13Features(VkPhysicalDevice device) {

        VkPhysicalDeviceVulkan13Features vulkan13Features = VkPhysicalDeviceVulkan13Features.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_3_FEATURES)
                .pNext(MemoryUtil.NULL);
        VkPhysicalDeviceVulkan12Features vulkan12Features = VkPhysicalDeviceVulkan12Features.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_2_FEATURES)
                .pNext(vulkan13Features.address());
        VkPhysicalDeviceVulkan11Features vulkan11Features = VkPhysicalDeviceVulkan11Features.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_1_FEATURES)
                .pNext(vulkan12Features.address());

        VkPhysicalDeviceFeatures2 features2 = VkPhysicalDeviceFeatures2.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2)
                .pNext(vulkan11Features.address());

        VK12.vkGetPhysicalDeviceFeatures2(device, features2);
        VkPhysicalDeviceFeatures deviceFeatures = features2.features();
        getFeatures(deviceFeatures);
        getVulkan11Features(vulkan11Features);
        getVulkan12Features(vulkan12Features);
        getVulkan13Features(vulkan13Features);
    }
}
