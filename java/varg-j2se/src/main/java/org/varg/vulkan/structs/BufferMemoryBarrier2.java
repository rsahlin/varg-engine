package org.varg.vulkan.structs;

import org.gltfio.lib.BitFlags;
import org.varg.vulkan.Vulkan13.AccessFlagBits2;
import org.varg.vulkan.Vulkan13.PipelineStateFlagBits2;
import org.varg.vulkan.memory.MemoryBuffer;

public class BufferMemoryBarrier2 {

    public final int srcQueueFamilyIndex;
    public final int dstQueueFamilyIndex;
    private final PipelineStateFlagBits2[] srcStageMask;
    private final AccessFlagBits2[] srcAccessMask;
    private final PipelineStateFlagBits2[] dstStageMask;
    private final AccessFlagBits2[] dstAccessMask;
    private final MemoryBuffer buffer;

    public BufferMemoryBarrier2(PipelineStateFlagBits2[] srcStageMask,
            AccessFlagBits2[] srcAccessMask, PipelineStateFlagBits2[] dstStageMask,
            AccessFlagBits2[] dstAccessMask, int srcQueue, int dstQueue, MemoryBuffer buffer) {

        this.srcStageMask = srcStageMask;
        this.srcAccessMask = srcAccessMask;
        this.dstStageMask = dstStageMask;
        this.dstAccessMask = dstAccessMask;
        this.srcQueueFamilyIndex = srcQueue;
        this.dstQueueFamilyIndex = dstQueue;
        this.buffer = buffer;

    }

    /**
     * Returns the source stage mask
     * 
     * @return
     */
    public long getSrcStageMask() {
        return BitFlags.getFlagsLongValue(srcStageMask);
    }

    /**
     * Returns the sestination stage mask
     * 
     * @return
     */
    public long getDestStageMask() {
        return BitFlags.getFlagsLongValue(dstStageMask);
    }

    /**
     * Returns the src access mask
     * 
     * @return
     */
    public long getSrcAccessMask() {
        return BitFlags.getFlagsLongValue(srcAccessMask);
    }

    /**
     * Returns the dst access mask
     * 
     * @return
     */
    public long getDstAccessMask() {
        return BitFlags.getFlagsLongValue(dstAccessMask);
    }

    /**
     * Returns the buffer
     * 
     * @return
     */
    public MemoryBuffer getBuffer() {
        return buffer;
    }

}
