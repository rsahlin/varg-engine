package org.varg.lwjgl3.vulkan;

import java.util.ArrayList;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;
import org.varg.BackendException;
import org.varg.renderer.Renderers;
import org.varg.vulkan.Features;
import org.varg.vulkan.LogicalDevice;
import org.varg.vulkan.PhysicalDevice;
import org.varg.vulkan.structs.QueueFamilyProperties;
import org.varg.window.J2SEWindow.WindowHandle;

public class LWJGL3Vulkan13Backend extends LWJGL3Vulkan12Backend {

    /**
     * Internal constructor - DO NOT USE
     * 
     * @param version
     * @param window
     * @throws BackendException
     */
    public LWJGL3Vulkan13Backend(Renderers version, WindowHandle window) throws BackendException {
        super(version, window);
    }

    @Override
    protected LogicalDevice<VkDevice> createLogicalDevice(PhysicalDevice device, QueueFamilyProperties selectedQueue,
            Features requestedFeatures) {

        ArrayList<Long> extensionList = setFeatureExtensions(requestedFeatures.getPhysicalDeviceFeatureExtensions());
        // The extension to inlcude is the last one added
        long firstExtension = extensionList.size() > 0 ? extensionList.get(extensionList.size() - 1) : MemoryUtil.NULL;
        VkPhysicalDeviceVulkan13Features vulkan13Features = VkPhysicalDeviceVulkan13Features.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_3_FEATURES)
                .pNext(firstExtension);
        VkPhysicalDeviceVulkan12Features vulkan12Features = VkPhysicalDeviceVulkan12Features.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_2_FEATURES)
                .pNext(vulkan13Features.address());
        VkPhysicalDeviceVulkan11Features vulkan11Features = VkPhysicalDeviceVulkan11Features.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_1_FEATURES)
                .pNext(vulkan12Features.address());
        VkPhysicalDeviceFeatures2 features2 = VkPhysicalDeviceFeatures2.calloc()
                .sType(VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2)
                .pNext(vulkan11Features.address());
        return internalCreateLogicalDevice(device, selectedQueue, requestedFeatures, features2,
                vulkan11Features, vulkan12Features, vulkan13Features);
    }

}
