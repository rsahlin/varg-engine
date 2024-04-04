
package org.varg.vulkan.descriptor;

import org.gltfio.lib.BitFlags;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.DescriptorPoolCreateFlagBits;

public class DescriptorPoolCreateInfo {
    final DescriptorPoolCreateFlagBits[] flags;
    final int maxSets;
    final DescriptorPoolSize[] pPoolSizes;

    public DescriptorPoolCreateInfo(DescriptorPoolCreateFlagBits[] flags, int maxSets,
            DescriptorPoolSize[] pPoolSizes) {
        if (pPoolSizes == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        if (maxSets <= 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "maxSets = " + maxSets);
        }
        this.flags = flags;
        this.maxSets = maxSets;
        this.pPoolSizes = pPoolSizes;
    }

    /**
     * Returns the value of the DescriptorPoolCreateFlagBits
     * 
     * @return
     */
    public int getFlagsValue() {
        return BitFlags.getFlagsValue(flags);
    }

    /**
     * Returns the max number of set
     * 
     * @return
     */
    public int getMaxSets() {
        return maxSets;
    }

    /**
     * Returns an array of descriptorpool sizes
     * 
     * @return
     */
    public DescriptorPoolSize[] getpPoolSizes() {
        return pPoolSizes;
    }

}
