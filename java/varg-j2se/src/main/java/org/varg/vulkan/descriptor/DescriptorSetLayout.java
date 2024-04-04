
package org.varg.vulkan.descriptor;

import java.nio.LongBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10;

/**
 * Wrapper for VkDescriptorSetLayout
 *
 */
public class DescriptorSetLayout {

    private final LongBuffer descriptorSetLayoutBuffer = Buffers.createLongBuffer(1);
    private final DescriptorSetLayoutBinding descriptorSetLayout;
    private final Vulkan10.DescriptorSetLayoutCreateFlagBit[] flags;

    public DescriptorSetLayout(long pDescriptorSetLayout, DescriptorSetLayoutBinding descriptorSetLayout,
            Vulkan10.DescriptorSetLayoutCreateFlagBit[] flags) {
        if (pDescriptorSetLayout == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        descriptorSetLayoutBuffer.put(pDescriptorSetLayout);
        this.descriptorSetLayout = descriptorSetLayout;
        this.flags = flags;
    }

    /**
     * Returns the descriptorsetlayout pointer
     * 
     * @return
     */
    public long getDescriptorSetLayout() {
        return descriptorSetLayoutBuffer.get(0);
    }

    /**
     * Returns the buffer containing the descriptorsetlayout pointer, positioned at beginning
     * 
     * @return
     */
    public LongBuffer getDescriptorSetLayoutBuffer() {
        descriptorSetLayoutBuffer.position(0);
        return descriptorSetLayoutBuffer;
    }

    /**
     * Returns the laytout binding
     * 
     * @return
     */
    public DescriptorSetLayoutBinding getBinding() {
        return descriptorSetLayout;
    }

    @Override
    public String toString() {
        return descriptorSetLayout.toString();
    }

}
