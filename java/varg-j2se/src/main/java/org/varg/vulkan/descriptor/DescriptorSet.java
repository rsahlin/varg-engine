
package org.varg.vulkan.descriptor;

import java.nio.LongBuffer;

import org.eclipse.jdt.annotation.NonNull;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;

public class DescriptorSet {

    private final LongBuffer descriptorSetBuffer;
    private final DescriptorSetLayout layout;

    /**
     * Creates a descriptorset for a layout
     * 
     * @param pDescriptorSet
     * @param layout
     * @throws IllegalArgumentException If layoutBindings contain more than one value
     */
    public DescriptorSet(long pDescriptorSet, @NonNull DescriptorSetLayout layout) {
        if (pDescriptorSet == 0 || layout == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        descriptorSetBuffer = Buffers.createLongBuffer(1);
        descriptorSetBuffer.put(pDescriptorSet);
        this.layout = layout;
    }

    /**
     * Returns the descriptorset pointer
     * 
     * @return
     */
    public long getDescriptorSet() {
        return descriptorSetBuffer.get(0);
    }

    /**
     * Returns the descriptorset buffer at position 0, the buffer containing the pointer to the descriptorset
     * 
     * @return
     */
    public LongBuffer getDescriptorSetBuffer() {
        descriptorSetBuffer.position(0);
        return descriptorSetBuffer;
    }

    /**
     * Returns the layout
     * 
     * @return
     */
    public DescriptorSetLayout getLayout() {
        return layout;
    }

    /**
     * Returns the layout binding
     * 
     * @return
     */
    public DescriptorSetLayoutBinding getLayoutBinding() {
        return layout.getBinding();
    }

    @Override
    public String toString() {
        return layout.toString();
    }
}
