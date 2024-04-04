
package org.varg.vulkan.descriptor;

import org.gltfio.lib.BitFlags;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10;

public class DescriptorSetLayoutCreateInfo {

    final Vulkan10.DescriptorSetLayoutCreateFlagBit[] flags;
    final DescriptorSetLayoutBinding descriptorSetlayout;

    public DescriptorSetLayoutCreateInfo(Vulkan10.DescriptorSetLayoutCreateFlagBit[] flags,
            DescriptorSetLayoutBinding descriptorSetlayout) {
        if (descriptorSetlayout == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.flags = flags;
        this.descriptorSetlayout = descriptorSetlayout;
    }

    /**
     * Returns the value of the DescriptorSetLayoutCreateFlagBit flags
     * 
     * @return
     */
    public int getFlagsValue() {
        return BitFlags.getFlagsValue(flags);
    }

    /**
     * Returns the array of DescriptorSetLayoutCreateFlagBits
     * 
     * @return
     */
    public Vulkan10.DescriptorSetLayoutCreateFlagBit[] getFlags() {
        return flags;
    }

    /**
     * Returns the setlayout
     * 
     * @return
     */
    public DescriptorSetLayoutBinding getDescriptorSetLayout() {
        return descriptorSetlayout;
    }

}
