package org.varg.lwjgl3.vulkan;

import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Logger;
import org.lwjgl.vulkan.VkMemoryHeap;
import org.lwjgl.vulkan.VkMemoryType;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.varg.vulkan.Vulkan10.MemoryHeapFlagBits;
import org.varg.vulkan.structs.PhysicalDeviceMemoryProperties;

public class LWJGLPhysicalDeviceMemoryProperties extends PhysicalDeviceMemoryProperties {

    public LWJGLPhysicalDeviceMemoryProperties(VkPhysicalDeviceMemoryProperties vkProperties) {
        int memCount = vkProperties.memoryTypeCount();
        int heapCount = vkProperties.memoryHeapCount();

        memoryTypes = new MemoryType[memCount];
        memoryHeap = new MemoryHeap[heapCount];
        for (int i = 0; i < memCount; i++) {
            VkMemoryType vkType = vkProperties.memoryTypes(i);
            memoryTypes[i] = new MemoryType(vkType.propertyFlags(), vkType.heapIndex());
        }
        for (int i = 0; i < heapCount; i++) {
            VkMemoryHeap vkHeap = vkProperties.memoryHeaps(i);
            MemoryHeapFlagBits[] flags = BitFlags.getBitFlags(vkHeap.flags(), MemoryHeapFlagBits.values())
                    .toArray(new MemoryHeapFlagBits[0]);
            memoryHeap[i] = new MemoryHeap(vkHeap.size(), flags);
        }
        Logger.d(getClass(), "Created physical memory properties for " + memCount + " memory types:");
        for (int i = 0; i < memCount; i++) {
            Logger.d(getClass(), "Memory type " + i + "\n" + memoryTypes[i].toString());
        }
        Logger.d(getClass(), "Created physical heap properties for " + heapCount + " heap types:");
        for (int i = 0; i < heapCount; i++) {
            Logger.d(getClass(), "Heap type " + i + "\n" + memoryHeap[i].toString());
        }
    }

}
