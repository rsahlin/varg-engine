
package org.varg.lwjgl3.vulkan;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.varg.vulkan.NativeBuffer.Handle;

public class LWJGLNativeBuffer implements Handle {

    @Override
    public long getNativeHandle(ByteBuffer byteBuffer) {
        return MemoryUtil.memAddress(byteBuffer);
    }

}
