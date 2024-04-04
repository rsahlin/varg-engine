
package org.varg.vulkan.framebuffer;

import org.gltfio.lib.ErrorMessage;

public class FrameBuffer {

    public final long handle;
    public final FramebufferCreateInfo createInfo;

    public FrameBuffer(long handle, FramebufferCreateInfo createInfo) {
        if (handle == 0 || createInfo == null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Handle is 0 or FrameBufferCreateInfo is null");
        }
        this.handle = handle;
        this.createInfo = createInfo;
    }

}
