
package org.varg.vulkan.vertex;

import java.nio.LongBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.varg.vulkan.memory.MemoryBuffer;

/**
 * The data needed to record a CmdBindVertexBuffers
 *
 */
public class BindVertexBuffers {

    public final int firstBinding;
    private final LongBuffer offsetsBuffer;
    private final LongBuffer pointerBuffer;

    public BindVertexBuffers(int firstBinding, MemoryBuffer[] buffers, long[] offsets) {
        if (buffers == null || offsets == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        if (buffers.length != offsets.length) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Size of buffers and offsets does not match");
        }
        this.firstBinding = firstBinding;
        offsetsBuffer = Buffers.createLongBuffer(buffers.length);
        offsetsBuffer.put(offsets);
        pointerBuffer = Buffers.createLongBuffer(offsets.length);
        for (MemoryBuffer b : buffers) {
            pointerBuffer.put(b != null ? b.getPointer() : 0);
        }
    }

    /**
     * Returns the buffer containing the native pointer to vertexbuffer, positioned at 0
     * 
     * @return
     */
    public LongBuffer getBuffers() {
        pointerBuffer.position(0);
        return pointerBuffer;
    }

    /**
     * Returns the buffer containing offsets, positioned at 0
     * 
     * @return
     */
    public LongBuffer getOffsets() {
        offsetsBuffer.position(0);
        return offsetsBuffer;
    }

}
