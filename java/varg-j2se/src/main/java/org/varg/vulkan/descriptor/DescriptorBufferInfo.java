
package org.varg.vulkan.descriptor;

import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.memory.MemoryBuffer;

public class DescriptorBufferInfo {

    public final MemoryBuffer buffer;
    public final long offset;
    public final long range;

    public DescriptorBufferInfo(MemoryBuffer buffer, long offset, long range) {
        if (buffer == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.buffer = buffer;
        this.offset = offset;
        this.range = range;
    }

}
