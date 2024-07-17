
package org.varg.vulkan.descriptor;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.memory.MemoryBuffer;

/**
 * Collection of data used when updating descriptorsets.
 */
public class DescriptorBufferInfo {

    private MemoryBuffer buffer;
    public final long offset;
    public final long range;

    DescriptorBufferInfo() {
        offset = 0;
        range = 0;
    }

    public DescriptorBufferInfo(MemoryBuffer buffer, long offset, long range) {
        if (buffer == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.buffer = buffer;
        this.offset = offset;
        this.range = range;
    }

    public MemoryBuffer getBuffer() {
        return buffer;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

}
