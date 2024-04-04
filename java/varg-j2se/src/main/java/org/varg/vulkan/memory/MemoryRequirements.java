
package org.varg.vulkan.memory;

public class MemoryRequirements {

    public final long size;
    public final long alignment;
    private final int memoryTypeBits;

    public MemoryRequirements(long size, long alignment, int memoryTypeBits) {
        this.size = size;
        this.alignment = alignment;
        this.memoryTypeBits = memoryTypeBits;
    }

}
