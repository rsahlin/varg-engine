package org.varg.vulkan.structs;

import org.gltfio.lib.ErrorMessage;

public class RequestedPhysicalDeviceFeatures extends PhysicalDeviceVulkan13Features {

    private final PhysicalDeviceFeatures available;
    
    public RequestedPhysicalDeviceFeatures(PhysicalDeviceFeatures available) {
        if (available == null) {
            throw new IllegalAccessError(ErrorMessage.INVALID_VALUE.message + "available features is null");
        }
        this.available = available;
    }
    
    protected void setFeatures(VulkanPhysicalDeviceFeatures... requested) {
        if (!available.hasFeatures(requested)) {
            throw new IllegalArgumentException();
        }
        setBooleanFields(true, requested);
    }
    
}
