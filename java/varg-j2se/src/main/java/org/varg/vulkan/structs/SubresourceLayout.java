
package org.varg.vulkan.structs;

public class SubresourceLayout {

    public final long offset;
    public final long size;
    public final long rowPitch;
    public final long arrayPitch;
    public final long depthPitch;

    public SubresourceLayout(long offset, long size, long rowPitch, long arrayPitch, long depthPitch) {
        this.offset = offset;
        this.size = size;
        this.rowPitch = rowPitch;
        this.arrayPitch = arrayPitch;
        this.depthPitch = depthPitch;
    }

}
