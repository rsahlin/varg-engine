
package org.varg.vulkan.descriptor;

import org.varg.vulkan.Vulkan10;
import org.varg.vulkan.Vulkan10.DescriptorType;

public class DescriptorPoolSize {
    final Vulkan10.DescriptorType type;
    final int descriptorCount;

    public DescriptorPoolSize(DescriptorType type, int descriptorCount) {
        this.type = type;
        this.descriptorCount = descriptorCount > 0 ? descriptorCount : 1;
    }

    /**
     * Returns the descriptortype
     * 
     * @return
     */
    public Vulkan10.DescriptorType getType() {
        return type;
    }

    /**
     * Returns the descriptorcount
     * 
     * @return
     */
    public int getDescriptorCount() {
        return descriptorCount;
    }

}
