
package org.varg.vulkan.memory;

import org.gltfio.lib.BitFlags;
import org.gltfio.lib.ErrorMessage;

/**
 * Memory object is treated as equal when the pointer to memory is the same, hashCode is generated only
 * by using the memory pointer.
 * This is so that this object can be used to track one memory pointer.
 *
 */
public class Memory {

    public final long pointer;
    public final long size;
    public final int memoryProperties;
    public final int allocateFlags;
    private long mappedPointer = 0;

    /**
     * Creates a new memory
     * 
     * @param size
     * @memoryProperties
     * @param memoryPointer
     */
    public Memory(long size, int memoryProperties, long memoryPointer) {
        if (memoryPointer == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Pointer is 0");
        }
        this.size = size;
        this.pointer = memoryPointer;
        this.memoryProperties = memoryProperties;
        this.allocateFlags = 0;
    }

    /**
     * Creates a new memory
     * 
     * @param size
     * @memoryProperties
     * @param memoryPointer
     */
    public Memory(long size, int memoryProperties, long memoryPointer, int allocateFlags) {
        if (memoryPointer == 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Pointer is 0");
        }
        this.size = size;
        this.pointer = memoryPointer;
        this.memoryProperties = memoryProperties;
        this.allocateFlags = allocateFlags;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (pointer ^ (pointer >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        Memory other = (Memory) obj;
        return pointer == other.pointer;
    }

    /**
     * Internal method do not use!
     * 
     * @param setMappedPointer
     */
    public void setMapped(long setMappedPointer) {
        mappedPointer = setMappedPointer;
    }

    /**
     * Returns true if this Memory has been mapped, call {@link #setMapped(long)} to map memory
     * 
     * @return
     */
    public boolean isMapped() {
        return mappedPointer != 0;
    }

    /**
     * Returns the pointer to the mapped memory
     * 
     * @return
     */
    public long getMapped() {
        return mappedPointer;
    }

    @Override
    public String toString() {
        return "Memory size " + size + ", usage " + BitFlags.toString(memoryProperties) + ", mapped " + isMapped()
                + ", pointer " + pointer;
    }

}
