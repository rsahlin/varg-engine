
package org.varg.vulkan;

import java.nio.ByteBuffer;

import org.gltfio.lib.ErrorMessage;

/**
 * Methods to get native handles from plain java
 *
 */
public class NativeBuffer {

    public interface Handle {
        long getNativeHandle(ByteBuffer byteBuffer);
    }

    private static Handle handle;
    public final ByteBuffer byteBuffer;
    public final long buffer;

    public NativeBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.buffer = getNativeHandle(byteBuffer);
    }

    /**
     * Returns the native handle from the bytebuffer
     * 
     * @param nativeBuffer
     * @return
     */
    private long getNativeHandle(ByteBuffer nativeBuffer) {
        if (handle == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Handle implementation not set");
        }
        return handle.getNativeHandle(nativeBuffer);
    }

    public static void setHandle(Handle handle) {
        NativeBuffer.handle = handle;
    }

}
