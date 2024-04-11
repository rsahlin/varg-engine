package org.varg.vulkan.memory;

import java.nio.ByteBuffer;

import org.gltfio.data.VertexBuffer;
import org.gltfio.gltf2.JSONBuffer;
import org.gltfio.gltf2.JSONBufferView;
import org.gltfio.lib.ErrorMessage;

/**
 * Collection of vulkan memory, memorybuffer(s) and offsets - this is a mapping from glTF buffers-to GPU storage
 */
public class VertexMemory {

    public enum Mode {
        BUFFERVIEWS(),
        FLATTENED(),
        BUFFERS();
    }

    private MemoryBuffer[] memoryBuffers;
    private ByteBuffer[] buffers;
    private int[] offsets;
    private final Mode mode;
    private ByteBuffer[] indexBuffers;
    private MemoryBuffer[] indexMemory;

    public VertexMemory(MemoryBuffer[] memoryBuffers, JSONBufferView[] bufferViews) {
        mode = Mode.BUFFERVIEWS;
        this.memoryBuffers = memoryBuffers;
        this.offsets = null;
        this.buffers = new ByteBuffer[bufferViews.length];
        for (int i = 0; i < buffers.length; i++) {
            this.buffers[i] = bufferViews[i].getReadByteBuffer(0);
        }
    }

    public VertexMemory(MemoryBuffer[] indexMemory, MemoryBuffer[] memoryBuffers, JSONBuffer[] indexBuffers,
            JSONBuffer[] vertexBuffers) {
        mode = Mode.BUFFERS;
        this.memoryBuffers = memoryBuffers;
        this.indexMemory = indexMemory;
        this.buffers = new ByteBuffer[vertexBuffers.length];
        for (int i = 0; i < buffers.length; i++) {
            this.buffers[i] = vertexBuffers[i] != null ? vertexBuffers[i].getAsReadBuffer() : null;
        }
        this.indexBuffers = new ByteBuffer[indexBuffers.length];
        for (int i = 0; i < indexBuffers.length; i++) {
            this.indexBuffers[i] = indexBuffers[i] != null ? indexBuffers[i].getAsReadBuffer() : null;
        }
    }

    public VertexMemory(MemoryBuffer[] indexMemory, VertexBuffer[] indexSourceBuffers, MemoryBuffer[] attributeBuffers,
            VertexBuffer[] sourceBuffers) {
        if (attributeBuffers.length != sourceBuffers.length) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Arrays must match in size");
        }
        this.memoryBuffers = attributeBuffers;
        this.mode = Mode.BUFFERS;
        this.buffers = new ByteBuffer[sourceBuffers.length];
        for (int i = 0; i < sourceBuffers.length; i++) {
            this.buffers[i] = sourceBuffers[i] != null ? sourceBuffers[i].getAsReadOnlyBuffer() : null;
        }
        if (indexMemory != null) {
            this.indexMemory = indexMemory;
            this.indexBuffers = new ByteBuffer[indexSourceBuffers.length];
            for (int i = 0; i < indexMemory.length; i++) {
                this.indexBuffers[i] = indexSourceBuffers[i] != null ? indexSourceBuffers[i].getAsReadOnlyBuffer()
                        : null;
            }
        }
    }

    /**
     * Returns the array of offset
     * 
     * @return
     */
    public int[] getOffsets() {
        return offsets;
    }

    /**
     * Returns the array with memorybuffer
     * 
     * @return
     */
    public MemoryBuffer[] getMemoryBuffers() {
        return memoryBuffers;
    }

    /**
     * Returns the array of bytebuffers
     * 
     * @return
     */
    public ByteBuffer[] getBuffers() {
        return buffers;
    }

    /**
     * Returns the array with indexbuffers
     * 
     * @return
     */
    public ByteBuffer[] getIndexBuffers() {
        return indexBuffers;
    }

    /**
     * Returns the index memory device memory buffers
     * 
     * @return
     */
    public MemoryBuffer[] getIndexMemoryBuffers() {
        return indexMemory;
    }

    /**
     * Returns the mode
     * 
     * @return
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Only use when mode is FLATTENED
     * 
     * @return
     */
    public MemoryBuffer getMemoryBuffer() {
        if (mode != Mode.FLATTENED) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", mode must be " + Mode.FLATTENED);
        }
        return memoryBuffers[0];
    }

    /**
     * Returns the memorybuffer for the buffer view index - if mode is flattened this will return the whole memory.
     * 
     * @param bufferViewIndex
     * @return
     */
    public MemoryBuffer getMemoryBuffer(int bufferViewIndex) {
        switch (mode) {
            case BUFFERVIEWS:
                return memoryBuffers[bufferViewIndex];
            case FLATTENED:
                return memoryBuffers[0];
            default:
                throw new IllegalArgumentException(mode.name());
        }
    }

    /**
     * Returns the offset into the buffer memory for the buffer view index.
     * 
     * @param bufferViewIndex
     * @return
     */
    public int getMemoryBufferOffset(int bufferViewIndex) {
        return offsets != null ? offsets[bufferViewIndex] : 0;
    }

    /**
     * Frees the device memory
     * 
     * @param deviceMemory
     */
    public void freeDeviceMemory(DeviceMemory deviceMemory) {
        if (memoryBuffers != null) {
            deviceMemory.freeBuffer(memoryBuffers);
            memoryBuffers = null;
        }
        if (indexMemory != null) {
            deviceMemory.freeBuffer(indexMemory);
            indexMemory = null;
        }
    }

}
