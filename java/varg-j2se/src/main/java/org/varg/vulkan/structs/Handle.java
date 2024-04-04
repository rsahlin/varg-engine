package org.varg.vulkan.structs;

import java.nio.LongBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;

public abstract class Handle<C> {

    protected final LongBuffer handle = Buffers.createLongBuffer(1);
    protected final C createInfo;

    public Handle(long handle, C createInfo) {
        if (handle == 0 || createInfo == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        this.handle.put(handle);
        this.createInfo = createInfo;
    }

    /**
     * Returns the native pointer (handle)
     * 
     * @return
     */
    public long getHandle() {
        return handle.get(0);
    }

    /**
     * Returns the createinfo
     * 
     * @return
     */
    public C getCreateInfo() {
        return createInfo;
    }

    /**
     * Returns the buffer containing the handle at position 0
     * 
     * @return
     */
    public LongBuffer getHandleBuffer() {
        return handle.position(0);
    }

}
