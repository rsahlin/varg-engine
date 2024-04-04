
package org.varg.vulkan.pipeline;

import java.nio.LongBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.descriptor.DescriptorSetLayout;
import org.varg.vulkan.structs.PushConstantRange;

/**
 * Wrapper for VkPipelineLayoutCreateInfo
 *
 */
public class PipelineLayoutCreateInfo {
    /**
     * This contains the order of the 'sets' accessed from the shaders. Eg index 0 is set = 0 in the shader.
     */
    private final LongBuffer setLayoutPointers;
    // Reserved for future use - VkPipelineLayoutCreateFlags flags;
    final PushConstantRange[] pushConstantRanges;

    public PipelineLayoutCreateInfo(DescriptorSetLayout[] descriptorSetLayout, PushConstantRange[] pushConstants) {
        if (descriptorSetLayout == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        setLayoutPointers = Buffers.createLongBuffer(descriptorSetLayout.length);
        for (DescriptorSetLayout setLayout : descriptorSetLayout) {
            setLayoutPointers.put(setLayout.getDescriptorSetLayoutBuffer());
        }
        pushConstantRanges = pushConstants;
    }

    /**
     * Returns the buffer holding the setlayout pointers, buffer positioned at 0
     * 
     * @return
     */
    public LongBuffer getSetLayoutPointers() {
        setLayoutPointers.position(0);
        return setLayoutPointers;
    }

    /**
     * Returns the number of setlayouts
     * 
     * @return
     */
    public int getSetLayoutCount() {
        return setLayoutPointers.capacity();
    }

    /**
     * Returns the array of pushconstants
     * 
     * @return
     */
    public PushConstantRange[] getPushConstantRanges() {
        return pushConstantRanges;
    }

}
