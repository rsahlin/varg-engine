
package org.varg.vulkan.structs;

import java.util.ArrayList;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan10.MemoryHeapFlagBits;

public class PhysicalDeviceMemoryProperties {

    public static class MemoryType {

        public final int flags;
        final int heapIndex;

        public MemoryType(int flags, int heapIndex) {
            this.flags = flags;
            this.heapIndex = heapIndex;
        }

        @Override
        public String toString() {
            return "Heapindex: " + heapIndex + "\n" + BitFlags.toString(flags);
        }
    }

    public static class MemoryHeap {

        final long size;
        final MemoryHeapFlagBits[] flags;

        public MemoryHeap(long size, MemoryHeapFlagBits[] flags) {
            this.size = size;
            this.flags = flags;
        }

        @Override
        public String toString() {
            return "Size : " + size + "\n" + BitFlags.toString(flags);
        }
    }

    protected MemoryType[] memoryTypes;
    protected MemoryHeap[] memoryHeap;

    /**
     * Returns the index to matching physical memory type
     * 
     * @param memoryPropertyFlags The wanted MemoryPropertyFlagBit
     * @return The memory type index with matching memory property flags
     */
    public int getMemoryTypeIndex(int memoryPropertyFlags) {
        ArrayList<Integer> memoryIndexList = new ArrayList<>();
        for (int i = 0; i < memoryTypes.length; i++) {
            if ((memoryPropertyFlags & memoryTypes[i].flags) == memoryPropertyFlags) {
                memoryIndexList.add(i);
            }
        }
        if (memoryIndexList.size() == 0) {
            return -1;
        }
        return memoryIndexList.get(0);
    }

    /**
     * Returns the memory type for a memory type index
     * 
     * @param memoryTypeIndex
     * @return
     */
    public MemoryType getMemoryType(int memoryTypeIndex) {
        return memoryTypeIndex >= 0 && memoryTypeIndex < memoryTypes.length ? memoryTypes[memoryTypeIndex] : null;
    }

}
