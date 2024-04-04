package org.varg.vulkan.structs;

import org.varg.vulkan.Vulkan10.DependencyFlagBit;

public class DependencyInfo {

    public final DependencyFlagBit[] dependencyFlags;
    // MemoryBarrier2 memoryBarriers;
    private final BufferMemoryBarrier2 bufferMemoryBarriers;
    private final ImageMemoryBarrier2 imageMemoryBarriers;

    public DependencyInfo(BufferMemoryBarrier2 bufferBarriers, ImageMemoryBarrier2 imageBarriers,
            DependencyFlagBit... flags) {
        this.dependencyFlags = flags;
        this.bufferMemoryBarriers = bufferBarriers;
        this.imageMemoryBarriers = imageBarriers;
    }

    /**
     * Returns the image memory barriers or null
     * 
     * @return
     */
    public ImageMemoryBarrier2 getImageMemoryBarriers() {
        return imageMemoryBarriers;
    }

    /**
     * Returns the buffer memory barriers or null
     * 
     * @return
     */
    public BufferMemoryBarrier2 getBufferMemoryBarriers() {
        return bufferMemoryBarriers;
    }

}
