
package org.varg.vulkan.memory;

import java.nio.LongBuffer;

import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.Vulkan10.BufferUsageFlagBit;

/**
 * The vulkan buffer (VkBuffer) - this can exist without a memory bound to it
 *
 */
public class MemoryBuffer {

    // VkBuffer
    private final LongBuffer pointer = Buffers.createLongBuffer(1);
    /**
     * Size of this buffer - NOT the memory
     */
    public final long size;
    /**
     * The size of the memory allocation required
     */
    public final long allocationSize;
    /**
     * Alignment of buffer (in memory)
     */
    public final long alignment;
    public final int usage;
    private Memory memory;
    private long boundOffset;

    /**
     * 
     * @param bufferPointer The VkBuffer handle
     * @param size
     * @param alignment
     * @param allocationSize
     * @param usage
     */
    public MemoryBuffer(long bufferPointer, long size, long alignment, long allocationSize, int usage) {
        if (bufferPointer == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Pointer is 0");
        }
        this.size = size;
        this.alignment = alignment;
        this.allocationSize = allocationSize;
        this.usage = usage;
        pointer.put(bufferPointer);
    }

    /**
     * Returns the native pointer (to VkBuffer handle)
     * 
     * @return
     */
    public long getPointer() {
        return pointer.get(0);
    }

    /**
     * Call to bind memory at bindOffset
     * 
     * @param memory
     * @param bindOffset
     */
    public void bindMemory(Memory bindMemory, long bindOffset) {
        this.memory = bindMemory;
        this.boundOffset = bindOffset;
    }

    /**
     * Returns true if this memory is bound, bind memory by calling {@link #bindMemory(Memory, long)}
     * 
     * @return
     */
    public boolean isBound() {
        return memory != null;
    }

    /**
     * Returns the bound memory
     * 
     * @return
     */
    public Memory getBoundMemory() {
        return memory;
    }

    /**
     * Returns the bind offset
     * 
     * @return
     */
    public long getBoundOffset() {
        return boundOffset;
    }

    /**
     * Unbind this memory
     */
    public void unbind() {
        pointer.position(0);
        pointer.put(0L);
        boundOffset = Constants.NO_VALUE;
    }

    @Override
    public String toString() {
        return "Size " + size + ", alignment " + alignment + ", boundoffset "
                + boundOffset + " isbound " + isBound() + ",  usageflags " +
                BitFlags.toString(BitFlags.getBitFlags(usage, BufferUsageFlagBit.values()).toArray(
                        new BufferUsageFlagBit[0]));
    }

}
